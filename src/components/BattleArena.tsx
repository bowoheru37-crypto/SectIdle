import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Disciple, BattleUnit } from '../types/game';
import { soundManager } from '../utils/audio';
import { ELEMENT_NAMES, ELEMENT_COLORS } from '../constants/gameData';
import {
  ResolvedPerformanceTier,
  createBattleRoster,
  stepAtbBattle,
  triggerCelebration,
} from '../core/sectEngine';
import { Swords, ShieldAlert, Award, RotateCcw } from 'lucide-react';
import { ModalShell } from './ModalShell';

interface BattleArenaProps {
  playerDisciples: Disciple[];
  sectRealm: number;
  combatMultiplier?: number;
  perfTier?: ResolvedPerformanceTier;
  onBattleVictory: (stones: number, exp: number, jade: number) => void;
  onBattleDefeat: () => void;
  onExit: () => void;
}

interface CombatUnitCardProps {
  unit: BattleUnit;
}

const CombatUnitCard: React.FC<CombatUnitCardProps> = ({ unit }) => {
  const hpPct = Math.max(0, Math.min(100, (unit.hp / unit.maxHp) * 100));
  const atbPct = Math.max(0, Math.min(100, (unit.actionBar / unit.maxActionBar) * 100));

  const hpBarColor = unit.isPlayer
    ? hpPct < 25
      ? 'bg-red-500'
      : hpPct < 60
      ? 'bg-amber-500'
      : 'bg-emerald-500'
    : 'bg-red-600';

  const hpTextColor =
    hpPct < 25
      ? 'text-red-400 font-bold animate-pulse'
      : unit.isPlayer
      ? 'text-emerald-400'
      : 'text-red-300';

  return (
    <div
      className={`p-3.5 rounded-xl border transition-all ${
        unit.isAlive
          ? unit.isPlayer
            ? 'bg-slate-800/60 border-slate-700/80 shadow-md'
            : 'bg-slate-800/60 border-red-900/40 shadow-md'
          : 'bg-slate-900/40 border-slate-800/40 opacity-40'
      }`}
    >
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2 min-w-0">
          <span className="text-xl" aria-hidden="true">
            {unit.isPlayer ? '🥋' : '👹'}
          </span>
          <div className="min-w-0">
            <div className="font-bold text-sm text-slate-100 flex items-center gap-1.5 flex-wrap">
              <span className="truncate">{unit.name}</span>
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
        <div className="text-right font-mono text-xs shrink-0">
          <span className={hpTextColor}>{unit.hp}</span>
          <span className="text-slate-500"> / {unit.maxHp} HP</span>
        </div>
      </div>

      <div className="w-full h-2.5 bg-slate-950 rounded-full overflow-hidden border border-slate-700/50 mb-1.5">
        <div className={`h-full transition-all duration-200 ${hpBarColor}`} style={{ width: `${hpPct}%` }} />
      </div>

      <div className="flex items-center gap-2 text-[10px] text-slate-400 font-mono">
        <span>ATB</span>
        <div className="flex-1 h-1.5 bg-slate-950 rounded-full overflow-hidden">
          <div
            className={`h-full transition-all duration-150 ${unit.isPlayer ? 'bg-cyan-400' : 'bg-amber-400'}`}
            style={{ width: `${atbPct}%` }}
          />
        </div>
        <span>{Math.floor(atbPct)}%</span>
      </div>
    </div>
  );
};

export const BattleArena: React.FC<BattleArenaProps> = ({
  playerDisciples,
  sectRealm,
  combatMultiplier = 1.0,
  perfTier = 'balanced',
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
  const [rewards, setRewards] = useState<{ stones: number; exp: number; jade: number }>({
    stones: 0,
    exp: 0,
    jade: 0,
  });

  const unitsRef = useRef<BattleUnit[]>([]);
  const logEndRef = useRef<HTMLDivElement | null>(null);

  const appendLogs = useCallback((entries: string[]) => {
    if (entries.length === 0) return;
    setCombatLogs(prev => [...prev, ...entries].slice(-40));
  }, []);

  const initBattle = useCallback(() => {
    const { units, synergyMessages } = createBattleRoster(playerDisciples, sectRealm, combatMultiplier);
    unitsRef.current = units;
    setBattleUnits(units);
    setCombatLogs(synergyMessages);
    setIsBattleRunning(true);
    setBattleOver(false);
    setIsVictory(false);
    setRound(1);
    setRewards({ stones: 0, exp: 0, jade: 0 });
    soundManager.playSwordStrike();
  }, [playerDisciples, sectRealm, combatMultiplier]);

  useEffect(() => {
    initBattle();
  }, [initBattle]);

  useEffect(() => {
    if (!isBattleRunning || battleOver) return;

    const intervalTime = Math.max(100, Math.floor(500 / battleSpeed));

    const timer = setInterval(() => {
      if (typeof document !== 'undefined' && document.hidden) return;

      const step = stepAtbBattle(unitsRef.current);
      unitsRef.current = step.units;
      setBattleUnits(step.units);

      if (step.soundEvent === 'crit') {
        soundManager.playCriticalStrike();
      } else if (step.soundEvent === 'strike') {
        soundManager.playSwordStrike();
      }

      if (step.acted) {
        setRound(r => r + 1);
      }

      const outgoingLogs = [...step.logs];

      if (step.finished) {
        setIsBattleRunning(false);
        setBattleOver(true);

        if (step.playerWon) {
          const rewardStones = 300 + sectRealm * 120;
          const rewardExp = 50 + sectRealm * 25;
          const rewardJade = 10 + sectRealm * 5;
          setIsVictory(true);
          setRewards({ stones: rewardStones, exp: rewardExp, jade: rewardJade });
          outgoingLogs.push(
            `🏆 VICTORY! The sect forces triumphed! Earned +${rewardStones} SS, +${rewardExp} EXP, +${rewardJade} Jade!`
          );
          soundManager.playBreakthrough();
          triggerCelebration(perfTier, { particleCount: 90, spread: 70, origin: { y: 0.6 } });
        } else {
          setIsVictory(false);
          outgoingLogs.push(`☠️ DEFEAT! Sect disciples retreated into the mountain array to recuperate.`);
        }
      }

      appendLogs(outgoingLogs);
    }, intervalTime);

    return () => clearInterval(timer);
  }, [isBattleRunning, battleSpeed, battleOver, sectRealm, perfTier, appendLogs]);

  useEffect(() => {
    logEndRef.current?.scrollIntoView({ behavior: perfTier === 'eco' ? 'auto' : 'smooth' });
  }, [combatLogs, perfTier]);

  const playerUnits = battleUnits.filter(u => u.isPlayer);
  const enemyUnits = battleUnits.filter(u => !u.isPlayer);

  return (
    <ModalShell
      isOpen={true}
      onClose={onExit}
      maxWidth="4xl"
      borderClassName="border-amber-500/30"
      containerClassName="h-[88vh]"
      bodyClassName="flex-1 flex flex-col overflow-hidden"
      closeLabel="Close battle arena"
      icon={
        <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
          <Swords className="w-5 h-5 text-amber-400" />
        </div>
      }
      title="ATB Sect Combat Arena"
      subtitle={`Turn-based combat simulation • Turn ${round}`}
      headerRight={
        <div className="flex items-center gap-2">
          <div className="flex bg-slate-800/80 p-0.5 rounded-lg border border-slate-700" role="group" aria-label="Battle speed">
            {[1, 2, 4].map(s => (
              <button
                key={s}
                type="button"
                onClick={() => {
                  soundManager.playClick();
                  setBattleSpeed(s);
                }}
                aria-pressed={battleSpeed === s}
                className={`px-2.5 py-1 text-xs font-mono font-bold rounded-md transition ${
                  battleSpeed === s ? 'bg-amber-500 text-slate-950' : 'text-slate-300 hover:text-white'
                }`}
              >
                {s}x
              </button>
            ))}
          </div>
          <button
            type="button"
            onClick={initBattle}
            aria-label="Restart Battle"
            title="Restart Battle"
            className="p-2 text-slate-400 hover:text-amber-400 hover:bg-slate-800 rounded-lg transition"
          >
            <RotateCcw className="w-4 h-4" />
          </button>
        </div>
      }
      footer={
        battleOver ? (
          <div className="p-4 bg-slate-950 border-t border-slate-800 flex flex-wrap items-center justify-between gap-3 animate-fade-in">
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
              type="button"
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
              type="button"
              onClick={onExit}
              className="text-xs text-slate-400 hover:text-white px-3 py-1.5 rounded-lg hover:bg-slate-800 transition"
            >
              Flee Battle
            </button>
          </div>
        )
      }
    >
      <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4 p-5 overflow-y-auto bg-gradient-to-b from-slate-950/60 to-slate-900/90">
        <div className="space-y-3">
          <div className="flex items-center justify-between text-xs font-bold text-emerald-400 border-b border-emerald-500/20 pb-1.5 uppercase tracking-wider">
            <span>Sect Vanguard (Team)</span>
            <span>Qi Energy</span>
          </div>
          {playerUnits.map(unit => (
            <CombatUnitCard key={unit.id} unit={unit} />
          ))}
        </div>

        <div className="space-y-3">
          <div className="flex items-center justify-between text-xs font-bold text-red-400 border-b border-red-500/20 pb-1.5 uppercase tracking-wider">
            <span>Demonic Invaders</span>
            <span>Fiend Vitality</span>
          </div>
          {enemyUnits.map(unit => (
            <CombatUnitCard key={unit.id} unit={unit} />
          ))}
        </div>
      </div>

      <div className="h-44 bg-slate-950 border-t border-slate-800 p-3.5 flex flex-col font-mono text-xs shrink-0">
        <div className="text-[11px] uppercase tracking-wider text-slate-400 font-bold mb-1.5 flex items-center justify-between">
          <span>Combat Chronology</span>
          <span className="text-[10px] text-slate-500">Auto-scrolling</span>
        </div>
        <div className="flex-1 overflow-y-auto space-y-1 pr-2">
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
    </ModalShell>
  );
};
