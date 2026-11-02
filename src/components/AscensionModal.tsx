import React from 'react';
import { soundManager } from '../utils/audio';
import { Award, Zap, RotateCcw, X, ShieldAlert } from 'lucide-react';
import confetti from 'canvas-confetti';

interface AscensionModalProps {
  isOpen: boolean;
  onClose: () => void;
  sectPower: number;
  spiritStones: number;
  sectRealm: number;
  totalAscensions: number;
  ancestralEssence: number;
  incomeMultiplier: number;
  combatMultiplier: number;
  onConfirmAscension: (earnedEssence: number) => void;
}

export const AscensionModal: React.FC<AscensionModalProps> = ({
  isOpen,
  onClose,
  sectPower,
  spiritStones,
  sectRealm,
  totalAscensions,
  ancestralEssence,
  incomeMultiplier,
  combatMultiplier,
  onConfirmAscension,
}) => {
  if (!isOpen) return null;

  // Pending essence calculation from AscensionSystem.java:
  // essenceFromPower = totalPower / 1000
  // essenceFromStones = totalStones / 10000
  const essenceFromPower = Math.floor(sectPower / 1000);
  const essenceFromStones = Math.floor(spiritStones / 10000);
  const pendingEssence = Math.max(1, essenceFromPower + essenceFromStones);

  const canAscend = sectRealm >= 3 || pendingEssence >= 10;

  const handleAscend = () => {
    soundManager.playBreakthrough();
    confetti({ particleCount: 150, spread: 100, origin: { y: 0.5 } });
    onConfirmAscension(pendingEssence);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-lg bg-slate-900 border border-amber-500/40 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
              <Award className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-amber-300 text-lg">
                Heavenly Sect Ascension
              </h3>
              <p className="text-xs text-slate-400">
                Transcend mortal cycles and reincarnate into an Ancient Immortal Sect
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

        {/* Content */}
        <div className="p-6 space-y-4 text-xs">
          {/* Multiplier Status */}
          <div className="grid grid-cols-2 gap-3 bg-slate-950 p-4 rounded-xl border border-slate-800">
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Total Ascensions</span>
              <strong className="text-slate-100 font-mono text-base">{totalAscensions}</strong>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Current Ancestral Essence</span>
              <strong className="text-amber-400 font-mono text-base">✨ {ancestralEssence}</strong>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Permanent Revenue Boost</span>
              <strong className="text-emerald-400 font-mono text-sm">+{(incomeMultiplier * 100 - 100).toFixed(0)}%</strong>
            </div>
            <div>
              <span className="text-[10px] text-slate-500 block uppercase">Permanent Combat Boost</span>
              <strong className="text-red-400 font-mono text-sm">+{(combatMultiplier * 100 - 100).toFixed(0)}%</strong>
            </div>
          </div>

          {/* Pending Yield */}
          <div className="p-4 bg-gradient-to-r from-amber-950/40 to-slate-950 border border-amber-500/30 rounded-xl space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-slate-200 font-bold">New Ancestral Essence on Ascension:</span>
              <span className="text-amber-400 font-mono font-bold text-lg">+{pendingEssence} Essence</span>
            </div>
            <p className="text-[11px] text-slate-400">
              Derived from {sectPower} Sect Power and {spiritStones} Spirit Stones accumulated.
            </p>
          </div>

          {/* Warning / Explanation */}
          <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-400 text-[11px] leading-relaxed flex items-start gap-2.5">
            <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
            <span>
              Ascension resets disciples and pavilions to Day 1, while preserving your Celestial Jade, Tech Tree masteries, and permanently compounding all revenue & disciple combat power!
            </span>
          </div>

          {/* Button */}
          <button
            onClick={handleAscend}
            disabled={!canAscend}
            className={`w-full py-3.5 rounded-xl font-bold text-xs transition shadow-lg flex items-center justify-center gap-2 ${
              canAscend
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed'
            }`}
          >
            <RotateCcw className="w-4 h-4" />
            {canAscend ? `Transcend Cycle & Reincarnate (+${pendingEssence} Essence)` : 'Requires Core Formation Realm (Realm 3+) or 10+ Essence'}
          </button>
        </div>
      </div>
    </div>
  );
};
