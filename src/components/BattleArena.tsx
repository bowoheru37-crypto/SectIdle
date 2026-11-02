import React, { useState, useEffect, useRef } from 'react';
import { Disciple, BattleUnit } from '../types/game';
import { soundManager } from '../utils/audio';
import { ELEMENT_NAMES, ELEMENT_COLORS } from '../constants/gameData';
import { Swords, ShieldAlert, Award, Play, FastForward, RotateCcw } from 'lucide-react';
import confetti from 'canvas-confetti';

interface BattleArenaProps {
  playerDisciples: Disciple[];
  sectRealm: number;
  onBattleVictory: (stones: number, exp: number, jade: number) => void;
  onBattleDefeat: () => void;
  onExit: () => void;
}

export const BattleArena: React.FC<BattleArenaProps> = ({
  playerDisciples,
  sectRealm,
  onBattleVictory,
  onBattleDefeat,
  onExit,
}) => {
  const [battleUnits, setBattleUnits] = useState<BattleUnit[]>([]);
  const [combatLogs, setCombatLogs] = useState<string[]>([]);
  const [battleSpeed, setBattleSpeed] = useState<number>(1);
  const [isBattleRunning, setIsBattleRunning] = useState<boolean>(true);
  const [battleOver, setBattleOver] = useState<boolean>(false);
  const [isVictory, setIsVictory] = useState<boolean>(false);
  const [round, setRound] = useState<number>(1);
  const [rewards, setRewards] = useState<{ stones: number; exp: number; jade: number }>({ stones: 0, exp: 0, jade: 0 });

  const unitsRef = useRef<BattleUnit[]>([]);
  const isRunningRef = useRef<boolean>(true);
  const logsRef = useRef<string[]>([]);
  const logEndRef = useRef<HTMLDivElement | null>(null);

  const addLog = (msg: string) => {
    logsRef.current = [...logsRef.current.slice(-40), msg];
    setCombatLogs([...logsRef.current]);
  };

  // Initialize Battle
  const initBattle = () => {
    logsRef.current = [];
    setCombatLogs([]);
    setIsBattleRunning(true);
    isRunningRef.current = true;
    setBattleOver(false);
    setIsVictory(false);
    setRound(1);

    // Build Player units
    const teamPlayer: BattleUnit[] = (playerDisciples.length > 0 ? playerDisciples.slice(0, 3) : [
      {
        id: 'guardian_d',
        name: 'Sect Guardian',
        stats: { str: 28, agi: 22, intel: 20, lck: 15, vit: 25, wis: 18, cha: 16 },
        realm: sectRealm,
        element: 1,
        hp: 350,
        maxHp: 350,
        mp: 150,
        maxMp: 150,
        atk: 84,
        def: 55,
        spd: 45,
        critRate: 30,
        critDmg: 180,
      } as unknown as Disciple
    ]).map((d, idx) => ({
      id: `p_${idx}`,
      name: d.name,
      isPlayer: true,
      hp: d.hp || d.maxHp,
      maxHp: d.maxHp,
      mp: d.mp || d.maxMp,
      maxMp: d.maxMp,
      atk: d.atk,
      def: d.def,
      spd: d.spd,
      critRate: d.critRate,
      critDmg: d.critDmg,
      element: d.element || 1,
      actionBar: 20 + idx * 10,
      maxActionBar: Math.max(100, 220 - d.spd),
      isAlive: true,
      discipleId: d.id,
      avatarSeed: idx
    }));

    // Build Enemy units
    const enemyNames = ['Corrupted Mountain Fiend', 'Demonic Nether Wolf', 'Bloodfang Viper', 'Chaos Wraith'];
    const enemyCount = Math.min(3, 2 + Math.floor(sectRealm / 2));
    const teamEnemy: BattleUnit[] = [];

    for (let i = 0; i < enemyCount; i++) {
      const eName = enemyNames[i % enemyNames.length];
      const baseHp = 180 + sectRealm * 95 + i * 40;
      const baseAtk = 45 + sectRealm * 22 + i * 10;
      const baseDef = 28 + sectRealm * 14;
      const baseSpd = 35 + sectRealm * 10;
      teamEnemy.push({
        id: `e_${i}`,
        name: eName,
        isPlayer: false,
        hp: baseHp,
        maxHp: baseHp,
        mp: 80,
        maxMp: 80,
        atk: baseAtk,
        def: baseDef,
        spd: baseSpd,
        critRate: 15,
        critDmg: 150,
        element: (i % 5) + 1,
        actionBar: Math.floor(Math.random() * 40),
        maxActionBar: Math.max(100, 220 - baseSpd),
        isAlive: true,
        avatarSeed: 10 + i
      });
    }

    const allUnits = [...teamPlayer, ...teamEnemy];
    unitsRef.current = allUnits;
    setBattleUnits([...allUnits]);

    addLog(`⚔️ Battle begins! Sect Vanguard faces ${enemyCount} mountain fiends!`);

    // Check elemental synergy
    const elemMap: Record<number, number> = {};
    teamPlayer.forEach(u => {
      elemMap[u.element] = (elemMap[u.element] || 0) + 1;
    });
    Object.entries(elemMap).forEach(([elem, count]) => {
      if (count >= 2) {
        addLog(`✨ Elemental Resonance Triggered! ${count} disciples resonate with ${ELEMENT_NAMES[Number(elem)]} (+15% ATK/DEF)!`);
        teamPlayer.forEach(u => {
          if (u.element === Number(elem)) {
            u.atk = Math.floor(u.atk * 1.15);
            u.def = Math.floor(u.def * 1.15);
          }
        });
      }
    });

    soundManager.playSwordStrike();
  };

  useEffect(() => {
    initBattle();
  }, []);

  // Battle tick loop
  useEffect(() => {
    if (!isBattleRunning || battleOver) return;

    const intervalTime = Math.max(100, Math.floor(500 / battleSpeed));

    const stepBattle = () => {
      if (!isRunningRef.current) return;

      const units = unitsRef.current;
      let stateChanged = false;

      // Charge action bars
      for (const u of units) {
        if (!u.isAlive) continue;
        u.actionBar = Math.min(u.maxActionBar, u.actionBar + Math.max(1, Math.floor(u.spd / 4)));
        stateChanged = true;

        if (u.actionBar >= u.maxActionBar) {
          // Unit can act!
          u.actionBar = 0;
          performAttack(u);
          break; // One turn per cycle for smooth animations
        }
      }

      if (stateChanged) {
        setBattleUnits([...units]);
      }

      checkBattleState();
    };

    const timer = setInterval(stepBattle, intervalTime);
    return () => clearInterval(timer);
  }, [isBattleRunning, battleSpeed, battleOver]);

  // Turn execution
  const performAttack = (actor: BattleUnit) => {
    const units = unitsRef.current;
    // Find opposing targets
    const opponents = units.filter(u => u.isPlayer !== actor.isPlayer && u.isAlive);
    if (opponents.length === 0) return;

    const target = opponents[Math.floor(Math.random() * opponents.length)];

    let comboMult = 1.0;
    // Martial Arts 10-Hit Juggle Combo
    if (Math.random() < 0.28) {
      comboMult = 1.6;
      addLog(`🥊 ${actor.name} unleashes 10-Hit Aerial Juggle Combo on ${target.name}!`);
      soundManager.playSwordStrike();
    }

    // Desperation Super Combo / Qi Clash (under 25% HP)
    if (actor.hp < actor.maxHp * 0.25 && Math.random() < 0.45) {
      comboMult = 2.4;
      addLog(`⚡ DESPERATION QI CLASH! ${actor.name} burns dantian essence for a supreme heavenly burst!`);
      soundManager.playCriticalStrike();
    }

    // Damage Formula
    let rawDmg = actor.atk * comboMult;
    const isCrit = Math.random() * 100 < actor.critRate;
    if (isCrit) {
      rawDmg *= actor.critDmg / 100;
      soundManager.playCriticalStrike();
    } else {
      soundManager.playSwordStrike();
    }

    const mitigation = 100 / (100 + target.def);
    const finalDmg = Math.max(1, Math.floor(rawDmg * mitigation));

    target.hp = Math.max(0, target.hp - finalDmg);

    if (isCrit) {
      addLog(`💥 CRITICAL STRIKE! ${actor.name} strikes ${target.name} for ${finalDmg} DMG!`);
    } else {
      addLog(`${actor.name} attacks ${target.name} for ${finalDmg} DMG.`);
    }

    // Heavenly Execution finisher
    if (target.hp <= 0) {
      target.isAlive = false;
      addLog(`💀 HEAVENLY EXECUTION! ${actor.name} utterly vanquished ${target.name}!`);
    }
  };

  const checkBattleState = () => {
    const units = unitsRef.current;
    const playersAlive = units.some(u => u.isPlayer && u.isAlive);
    const enemiesAlive = units.some(u => !u.isPlayer && u.isAlive);

    if (!playersAlive || !enemiesAlive) {
      isRunningRef.current = false;
      setIsBattleRunning(false);
      setBattleOver(true);

      if (playersAlive) {
        setIsVictory(true);
        const rewardStones = 300 + sectRealm * 120;
        const rewardExp = 50 + sectRealm * 25;
        const rewardJade = 10 + sectRealm * 5;
        setRewards({ stones: rewardStones, exp: rewardExp, jade: rewardJade });
        addLog(`🏆 VICTORY! The sect forces triumphed! Earned +${rewardStones} SS, +${rewardExp} EXP, +${rewardJade} Jade!`);
        soundManager.playBreakthrough();
        confetti({ particleCount: 100, spread: 70, origin: { y: 0.6 } });
      } else {
        setIsVictory(false);
        addLog(`☠️ DEFEAT! Sect disciples retreated into the mountain array to recuperate.`);
      }
    }
  };

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [combatLogs]);

  const playerUnits = battleUnits.filter(u => u.isPlayer);
  const enemyUnits = battleUnits.filter(u => !u.isPlayer);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md p-4">
      <div className="relative w-full max-w-4xl bg-slate-900 border border-amber-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col h-[88vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-3.5 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
              <Swords className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                ATB Sect Combat Arena
              </h3>
              <p className="text-xs text-slate-400">
                Turn-based combat simulation • Round {round}
              </p>
            </div>
          </div>

          {/* Speed & Controls */}
          <div className="flex items-center gap-2">
            <div className="flex bg-slate-800/80 p-0.5 rounded-lg border border-slate-700">
              {[1, 2, 4].map(s => (
                <button
                  key={s}
                  onClick={() => {
                    soundManager.playClick();
                    setBattleSpeed(s);
                  }}
                  className={`px-2.5 py-1 text-xs font-mono font-bold rounded-md transition ${
                    battleSpeed === s ? 'bg-amber-500 text-slate-950' : 'text-slate-300 hover:text-white'
                  }`}
                >
                  {s}x
                </button>
              ))}
            </div>

            <button
              onClick={initBattle}
              title="Restart Battle"
              className="p-2 text-slate-400 hover:text-amber-400 hover:bg-slate-800 rounded-lg transition"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Combat Field */}
        <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4 p-5 overflow-y-auto bg-gradient-to-b from-slate-950/60 to-slate-900/90">
          {/* Left: Sect Disciples */}
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-emerald-400 border-b border-emerald-500/20 pb-1.5 uppercase tracking-wider">
              <span>Sect Vanguard (Team)</span>
              <span>Qi Energy</span>
            </div>
            {playerUnits.map(unit => {
              const hpPct = Math.max(0, Math.min(100, (unit.hp / unit.maxHp) * 100));
              const atbPct = Math.max(0, Math.min(100, (unit.actionBar / unit.maxActionBar) * 100));
              return (
                <div
                  key={unit.id}
                  className={`p-3.5 rounded-xl border transition-all ${
                    unit.isAlive
                      ? 'bg-slate-800/60 border-slate-700/80 shadow-md'
                      : 'bg-slate-900/40 border-slate-800/40 opacity-40'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">🥋</span>
                      <div>
                        <div className="font-bold text-sm text-slate-100 flex items-center gap-1.5">
                          {unit.name}
                          <span
                            className="text-[10px] px-1.5 py-0.5 rounded-xs font-mono"
                            style={{
                              backgroundColor: `${ELEMENT_COLORS[unit.element]}22`,
                              color: ELEMENT_COLORS[unit.element],
                            }}
                          >
                            {ELEMENT_NAMES[unit.element]}
                          </span>
                        </div>
                        <div className="text-[11px] text-slate-400">
                          ATK: {unit.atk} • DEF: {unit.def} • SPD: {unit.spd}
                        </div>
                      </div>
                    </div>
                    <div className="text-right font-mono text-xs">
                      <span className={hpPct < 25 ? 'text-red-400 font-bold animate-pulse' : 'text-emerald-400'}>
                        {unit.hp}
                      </span>
                      <span className="text-slate-500"> / {unit.maxHp} HP</span>
                    </div>
                  </div>

                  {/* HP Bar */}
                  <div className="w-full h-2.5 bg-slate-950 rounded-full overflow-hidden border border-slate-700/50 mb-1.5">
                    <div
                      className={`h-full transition-all duration-200 ${
                        hpPct < 25 ? 'bg-red-500' : hpPct < 60 ? 'bg-amber-500' : 'bg-emerald-500'
                      }`}
                      style={{ width: `${hpPct}%` }}
                    />
                  </div>

                  {/* ATB Gauge */}
                  <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono">
                    <span>ATB</span>
                    <div className="flex-1 h-1.5 bg-slate-950 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-cyan-400 transition-all duration-150"
                        style={{ width: `${atbPct}%` }}
                      />
                    </div>
                    <span>{Math.floor(atbPct)}%</span>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Right: Mountain Fiends & Enemies */}
          <div className="space-y-3">
            <div className="flex items-center justify-between text-xs font-bold text-red-400 border-b border-red-500/20 pb-1.5 uppercase tracking-wider">
              <span>Demonic Invaders</span>
              <span>Fiend Vitality</span>
            </div>
            {enemyUnits.map(unit => {
              const hpPct = Math.max(0, Math.min(100, (unit.hp / unit.maxHp) * 100));
              const atbPct = Math.max(0, Math.min(100, (unit.actionBar / unit.maxActionBar) * 100));
              return (
                <div
                  key={unit.id}
                  className={`p-3.5 rounded-xl border transition-all ${
                    unit.isAlive
                      ? 'bg-slate-800/60 border-red-900/40 shadow-md'
                      : 'bg-slate-900/40 border-slate-800/40 opacity-40'
                  }`}
                >
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xl">👹</span>
                      <div>
                        <div className="font-bold text-sm text-slate-100 flex items-center gap-1.5">
                          {unit.name}
                          <span
                            className="text-[10px] px-1.5 py-0.5 rounded-xs font-mono"
                            style={{
                              backgroundColor: `${ELEMENT_COLORS[unit.element]}22`,
                              color: ELEMENT_COLORS[unit.element],
                            }}
                          >
                            {ELEMENT_NAMES[unit.element]}
                          </span>
                        </div>
                        <div className="text-[11px] text-slate-400">
                          ATK: {unit.atk} • DEF: {unit.def} • SPD: {unit.spd}
                        </div>
                      </div>
                    </div>
                    <div className="text-right font-mono text-xs">
                      <span className={hpPct < 25 ? 'text-red-400 font-bold animate-pulse' : 'text-red-300'}>
                        {unit.hp}
                      </span>
                      <span className="text-slate-500"> / {unit.maxHp} HP</span>
                    </div>
                  </div>

                  {/* HP Bar */}
                  <div className="w-full h-2.5 bg-slate-950 rounded-full overflow-hidden border border-slate-700/50 mb-1.5">
                    <div
                      className="h-full bg-red-600 transition-all duration-200"
                      style={{ width: `${hpPct}%` }}
                    />
                  </div>

                  {/* ATB Gauge */}
                  <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono">
                    <span>ATB</span>
                    <div className="flex-1 h-1.5 bg-slate-950 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-amber-400 transition-all duration-150"
                        style={{ width: `${atbPct}%` }}
                      />
                    </div>
                    <span>{Math.floor(atbPct)}%</span>
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Live Combat Log Window */}
        <div className="h-44 bg-slate-950 border-t border-slate-800 p-3.5 flex flex-col font-mono text-xs">
          <div className="text-[11px] uppercase tracking-wider text-slate-400 font-bold mb-1.5 flex items-center justify-between">
            <span>Combat Chronology</span>
            <span className="text-[10px] text-slate-500">Auto-scrolling</span>
          </div>
          <div className="flex-1 overflow-y-auto space-y-1 pr-2 scrollbar-thin">
            {combatLogs.map((log, i) => (
              <div
                key={i}
                className={`py-0.5 ${
                  log.includes('CRITICAL') || log.includes('DESPERATION')
                    ? 'text-amber-300 font-semibold'
                    : log.includes('EXECUTION')
                    ? 'text-purple-300 font-bold'
                    : log.includes('VICTORY')
                    ? 'text-emerald-400 font-bold'
                    : log.includes('DEFEAT')
                    ? 'text-red-400 font-bold'
                    : 'text-slate-300'
                }`}
              >
                {log}
              </div>
            ))}
            <div ref={logEndRef} />
          </div>
        </div>

        {/* Footer Actions / Victory Modal */}
        {battleOver ? (
          <div className="p-4 bg-slate-950 border-t border-slate-800 flex items-center justify-between animate-fade-in">
            <div className="flex items-center gap-2">
              {isVictory ? (
                <>
                  <Award className="w-5 h-5 text-amber-400" />
                  <span className="text-emerald-400 font-bold text-sm">
                    Battle Triumphant! +{rewards.stones} SS, +{rewards.exp} Exp, +{rewards.jade} Jade
                  </span>
                </>
              ) : (
                <>
                  <ShieldAlert className="w-5 h-5 text-red-400" />
                  <span className="text-red-400 font-bold text-sm">
                    Defeat! Sect disciples retreated safely to cultivate.
                  </span>
                </>
              )}
            </div>

            <button
              onClick={() => {
                if (isVictory) {
                  onBattleVictory(rewards.stones, rewards.exp, rewards.jade);
                } else {
                  onBattleDefeat();
                }
              }}
              className={`px-6 py-2.5 rounded-xl font-bold text-sm transition shadow-lg ${
                isVictory
                  ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
                  : 'bg-slate-800 hover:bg-slate-700 text-slate-200'
              }`}
            >
              {isVictory ? 'Collect Rewards & Return' : 'Return to Sect'}
            </button>
          </div>
        ) : (
          <div className="p-3 bg-slate-950 border-t border-slate-800 flex justify-end">
            <button
              onClick={onExit}
              className="text-xs text-slate-400 hover:text-white px-3 py-1.5 rounded-lg hover:bg-slate-800 transition"
            >
              Flee Battle
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
