import React, { useState } from 'react';
import { Disciple } from '../types/game';
import { soundManager } from '../utils/audio';
import { Trophy, Swords, Zap, Award, X } from 'lucide-react';
import confetti from 'canvas-confetti';

interface GrandTournamentProps {
  isOpen: boolean;
  onClose: () => void;
  disciples: Disciple[];
  onTournamentFinished: (won: boolean, prizeStones: number, prizeJade: number, prizeRep: number) => void;
}

export const GrandTournament: React.FC<GrandTournamentProps> = ({
  isOpen,
  onClose,
  disciples,
  onTournamentFinished,
}) => {
  const [selectedDiscipleId, setSelectedDiscipleId] = useState<string>(
    disciples.length > 0 ? disciples[0].id : ''
  );
  const [tier, setTier] = useState<number>(0); // 0: Bronze, 1: Silver, 2: Gold Grandmaster
  const [isDueling, setIsDueling] = useState<boolean>(false);
  const [tournamentLogs, setTournamentLogs] = useState<string[]>([]);
  const [result, setResult] = useState<{
    won: boolean;
    roundsWon: number;
    stones: number;
    jade: number;
    rep: number;
  } | null>(null);

  if (!isOpen) return null;

  const selected = disciples.find(d => d.id === selectedDiscipleId) || disciples[0];

  const handleStartDuel = () => {
    if (!selected) return;
    setIsDueling(true);
    setTournamentLogs([]);
    setResult(null);

    soundManager.playSwordStrike();

    const logs: string[] = [];
    logs.push(`🥋 Grand Tournament Stage (Tier ${tier + 1}) begins!`);
    logs.push(`${selected.name} enters the sacred arena platform.`);

    setTimeout(() => {
      let roundsWon = 0;
      const enemyBase = 120 + tier * 160;

      for (let r = 1; r <= 3; r++) {
        const discPower = selected.atk * 1.5 + selected.spd + (Math.random() * 40 - 20);
        const opponentPower = enemyBase + r * 35 + (Math.random() * 30 - 15);

        // Check for Critical Strike
        if (Math.random() < 0.45) {
          soundManager.playCriticalStrike();
          logs.push(`⚡ ROUND ${r}: ${selected.name} executed a CRITICAL DAO STRIKE!`);
        }

        if (discPower >= opponentPower) {
          roundsWon++;
          logs.push(`Round ${r}: ${selected.name} outmaneuvered the rival contender!`);
        } else {
          logs.push(`Round ${r}: Rival champion parried and landed a decisive blow.`);
        }
      }

      const won = roundsWon >= 2;
      const prizeStones = won ? 350 * (tier + 1) : 50 * (tier + 1);
      const prizeJade = won ? 12 * (tier + 1) : 0;
      const prizeRep = won ? 30 * (tier + 1) : 5;

      if (won) {
        soundManager.playBreakthrough();
        confetti({ particleCount: 90, spread: 70 });
        logs.push(`🏆 VICTORY! ${selected.name} claimed the Grand Tournament crown!`);
      } else {
        soundManager.playSwordStrike();
        logs.push(`Eliminated in stage ${roundsWon + 1}. Consolation rewards granted.`);
      }

      setTournamentLogs(logs);
      setResult({ won, roundsWon, stones: prizeStones, jade: prizeJade, rep: prizeRep });
      setIsDueling(false);
      onTournamentFinished(won, prizeStones, prizeJade, prizeRep);
    }, 1000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-xl bg-slate-900 border border-amber-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
              <Trophy className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                Grand Sect Arena Tournament
              </h3>
              <p className="text-xs text-slate-400">
                Send your strongest disciple into 1v1 elimination duels
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Body */}
        <div className="p-6 space-y-4 overflow-y-auto max-h-[75vh]">
          {/* Select Champion */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Select Sect Representative
            </label>
            <div className="grid grid-cols-2 gap-2 max-h-40 overflow-y-auto pr-1">
              {disciples.map(d => (
                <button
                  key={d.id}
                  onClick={() => {
                    soundManager.playClick();
                    setSelectedDiscipleId(d.id);
                  }}
                  className={`p-2.5 text-left rounded-xl border text-xs transition ${
                    selected?.id === d.id
                      ? 'bg-amber-500/10 border-amber-500 text-amber-300'
                      : 'bg-slate-800/40 border-slate-700/60 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <div className="font-bold truncate">{d.name}</div>
                  <div className="text-[10px] text-slate-400 mt-0.5">
                    ATK: {d.atk} • SPD: {d.spd} • {d.battlesWon} Wins
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Tier Selection */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Tournament Tier
            </label>
            <div className="grid grid-cols-3 gap-2.5">
              {[
                { title: 'Bronze Division', reward: '350 SS • 12 Jade' },
                { title: 'Silver Division', reward: '700 SS • 24 Jade' },
                { title: 'Gold Grandmaster', reward: '1050 SS • 36 Jade' },
              ].map((t, idx) => (
                <button
                  key={idx}
                  onClick={() => {
                    soundManager.playClick();
                    setTier(idx);
                  }}
                  className={`p-2.5 rounded-xl border text-center transition ${
                    tier === idx
                      ? 'bg-amber-500/15 border-amber-500 text-amber-300'
                      : 'bg-slate-800/40 border-slate-700 text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  <div className="font-bold text-xs">{t.title}</div>
                  <div className="text-[10px] text-slate-400 mt-1">{t.reward}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Live Duel Log */}
          {tournamentLogs.length > 0 && (
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 font-mono text-xs space-y-1 max-h-36 overflow-y-auto">
              {tournamentLogs.map((log, i) => (
                <div
                  key={i}
                  className={
                    log.includes('CRITICAL') || log.includes('VICTORY')
                      ? 'text-amber-300 font-bold'
                      : 'text-slate-300'
                  }
                >
                  {log}
                </div>
              ))}
            </div>
          )}

          {/* Result Card */}
          {result && (
            <div
              className={`p-3.5 rounded-xl border flex items-center justify-between text-xs font-bold ${
                result.won
                  ? 'bg-emerald-950/40 border-emerald-500/50 text-emerald-300'
                  : 'bg-slate-800 border-slate-700 text-slate-300'
              }`}
            >
              <div className="flex items-center gap-2">
                <Award className="w-4 h-4 text-amber-400" />
                <span>Rounds Won: {result.roundsWon}/3</span>
              </div>
              <div className="flex gap-2 font-mono">
                <span>+{result.stones} SS</span>
                {result.jade > 0 && <span>+{result.jade} Jade</span>}
                <span>+{result.rep} Rep</span>
              </div>
            </div>
          )}

          <button
            onClick={handleStartDuel}
            disabled={isDueling || !selected}
            className={`w-full py-3.5 rounded-xl font-bold text-sm transition shadow-lg flex items-center justify-center gap-2 ${
              isDueling
                ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                : 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
            }`}
          >
            <Swords className="w-4 h-4" />
            {isDueling ? 'Duel In Progress...' : 'Enter Champion Into Arena'}
          </button>
        </div>
      </div>
    </div>
  );
};
