import React, { useState } from 'react';
import { RIVAL_SECTS } from '../constants/gameData';
import { Disciple } from '../types/game';
import { soundManager } from '../utils/audio';
import { Shield, Swords, Skull, Award, X, Flame } from 'lucide-react';
import confetti from 'canvas-confetti';

interface SectWarCampaignProps {
  isOpen: boolean;
  onClose: () => void;
  sectPower: number;
  sectRealm: number;
  disciples: Disciple[];
  onWarCompleted: (victory: boolean, loot: { stones: number; herbs: number; ores: number; jade: number }) => void;
}

export const SectWarCampaign: React.FC<SectWarCampaignProps> = ({
  isOpen,
  onClose,
  sectPower,
  sectRealm,
  disciples,
  onWarCompleted,
}) => {
  const [selectedRival, setSelectedRival] = useState(0);
  const [difficultyTier, setDifficultyTier] = useState<number>(0); // 0: Raid, 1: Assault, 2: Total War
  const [isFighting, setIsFighting] = useState<boolean>(false);
  const [warResult, setWarResult] = useState<{
    won: boolean;
    msg: string;
    loot: { stones: number; herbs: number; ores: number; jade: number };
    critLeader?: string;
  } | null>(null);

  if (!isOpen) return null;

  const rival = RIVAL_SECTS[selectedRival];
  const tierNames = ['Border Skirmish / Raid', 'Grand Fortress Assault', 'Total Sect Annihilation'];
  const riskLabels = ['Low Risk', 'Moderate Risk', 'Extreme Mortal Peril'];

  // Power required
  const enemyRequiredPower = rival.basePower + difficultyTier * 650 + sectRealm * 300;
  const winProbability = Math.min(95, Math.max(5, Math.floor((sectPower / enemyRequiredPower) * 70)));

  const handleLaunchWar = () => {
    soundManager.playSwordStrike();
    setIsFighting(true);
    setWarResult(null);

    setTimeout(() => {
      const roll = Math.random() * 100;
      const won = roll <= winProbability;

      const loot = won
        ? {
            stones: 600 * (difficultyTier + 1) + (selectedRival + 1) * 150,
            herbs: 120 * (difficultyTier + 1),
            ores: 60 * (difficultyTier + 1),
            jade: 15 * (difficultyTier + 1) + (selectedRival + 1) * 5,
          }
        : { stones: 0, herbs: 0, ores: 0, jade: 0 };

      const leader = disciples.length > 0 ? disciples[0].name : 'Sect Patriarch';

      if (won) {
        soundManager.playBreakthrough();
        confetti({ particleCount: 110, spread: 80 });
      } else {
        soundManager.playSwordStrike();
      }

      setWarResult({
        won,
        msg: won
          ? `Crushing victory against ${rival.name}! Plundered their mountain treasury and subjugated their border arrays!`
          : `Defeated by ${rival.name}'s elders. Sect forces retreated to recuperate under the mountain formation.`,
        loot,
        critLeader: won && disciples.length > 0 ? leader : undefined,
      });

      setIsFighting(false);
      onWarCompleted(won, loot);
    }, 1200);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-red-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-red-500/10 border border-red-500/30 rounded-xl">
              <Flame className="w-5 h-5 text-red-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                Sect Warfare & Conquest
              </h3>
              <p className="text-xs text-slate-400">
                Command disciples in territorial conquest against rival cultivation sects
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

        {/* Body Content */}
        <div className="p-6 space-y-5 overflow-y-auto max-h-[75vh]">
          {/* Rival Sect Picker */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Select Target Rival Sect
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {RIVAL_SECTS.map((r, idx) => (
                <button
                  key={r.name}
                  onClick={() => {
                    soundManager.playClick();
                    setSelectedRival(idx);
                  }}
                  className={`p-3 text-left rounded-xl border transition ${
                    selectedRival === idx
                      ? 'bg-red-950/40 border-red-500 text-red-200 shadow-md'
                      : 'bg-slate-800/40 border-slate-700/60 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <div className="font-bold text-sm truncate">{r.name}</div>
                  <div className="text-[11px] text-slate-400 mt-1">Base Power: {r.basePower}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Rival Info Card */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 flex flex-col sm:flex-row items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2">
                <Skull className="w-4 h-4 text-red-400" />
                <span className="font-serif-title font-bold text-slate-100 text-base">{rival.name}</span>
              </div>
              <p className="text-xs text-slate-400 mt-1">{rival.desc}</p>
            </div>
            <div className="text-right sm:border-l sm:border-slate-800 sm:pl-4 min-w-[140px]">
              <span className="text-[10px] text-slate-500 uppercase tracking-wider font-semibold block">Rival Power</span>
              <span className="font-mono text-lg font-bold text-red-400">{enemyRequiredPower}</span>
            </div>
          </div>

          {/* Campaign Tier Selector */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Campaign Intensity
            </label>
            <div className="grid grid-cols-3 gap-3">
              {[0, 1, 2].map(tier => (
                <button
                  key={tier}
                  onClick={() => {
                    soundManager.playClick();
                    setDifficultyTier(tier);
                  }}
                  className={`p-3 rounded-xl border text-center transition ${
                    difficultyTier === tier
                      ? 'bg-amber-500/10 border-amber-500 text-amber-300'
                      : 'bg-slate-800/40 border-slate-700 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <div className="font-bold text-xs">{tierNames[tier]}</div>
                  <div className="text-[10px] text-slate-400 mt-1">{riskLabels[tier]}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Power Balance Comparison */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4">
            <div className="flex items-center justify-between text-xs mb-2">
              <span className="text-slate-300">
                Our Sect Total Power: <strong className="text-amber-400 font-mono">{sectPower}</strong>
              </span>
              <span className="text-slate-300">
                Estimated Win Rate: <strong className={winProbability >= 50 ? 'text-emerald-400 font-mono' : 'text-red-400 font-mono'}>{winProbability}%</strong>
              </span>
            </div>
            <div className="w-full h-3 bg-slate-800 rounded-full overflow-hidden flex">
              <div
                className="bg-emerald-500 transition-all duration-300"
                style={{ width: `${winProbability}%` }}
              />
              <div
                className="bg-red-600 transition-all duration-300"
                style={{ width: `${100 - winProbability}%` }}
              />
            </div>
          </div>

          {/* Outcome Result Notification */}
          {warResult && (
            <div
              className={`p-4 rounded-xl border animate-fade-in ${
                warResult.won
                  ? 'bg-emerald-950/40 border-emerald-500/50 text-emerald-200'
                  : 'bg-red-950/40 border-red-500/50 text-red-200'
              }`}
            >
              <div className="flex items-center gap-2 font-bold text-sm mb-1">
                {warResult.won ? <Award className="w-4 h-4 text-emerald-400" /> : <Skull className="w-4 h-4 text-red-400" />}
                {warResult.won ? 'CAMPAIGN VICTORIOUS' : 'CAMPAIGN REPULSED'}
              </div>
              <p className="text-xs text-slate-300 mb-2">{warResult.msg}</p>
              {warResult.won && (
                <div className="flex flex-wrap gap-3 font-mono text-xs text-emerald-300 bg-emerald-950/60 p-2.5 rounded-lg border border-emerald-500/30">
                  <span>💎 +{warResult.loot.stones} SS</span>
                  <span>🌿 +{warResult.loot.herbs} Herbs</span>
                  <span>🪨 +{warResult.loot.ores} Ores</span>
                  <span>✨ +{warResult.loot.jade} Jade</span>
                </div>
              )}
            </div>
          )}

          {/* Action Button */}
          <button
            onClick={handleLaunchWar}
            disabled={isFighting}
            className={`w-full py-3.5 rounded-xl font-bold text-sm transition shadow-lg flex items-center justify-center gap-2 ${
              isFighting
                ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                : 'bg-gradient-to-r from-red-600 via-red-500 to-amber-600 hover:from-red-500 hover:to-amber-500 text-white'
            }`}
          >
            <Swords className="w-4 h-4" />
            {isFighting ? 'Marshalling Sect Legions...' : 'Launch Sect Campaign'}
          </button>
        </div>
      </div>
    </div>
  );
};
