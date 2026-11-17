import React from 'react';
import { soundManager } from '../utils/audio';
import {
  ResolvedPerformanceTier,
  calculatePendingAscension,
  triggerCelebration,
} from '../core/sectEngine';
import { Award, RotateCcw, ShieldAlert } from 'lucide-react';
import { ModalShell } from './ModalShell';

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
  perfTier?: ResolvedPerformanceTier;
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
  perfTier = 'balanced',
  onConfirmAscension,
}) => {
  const { pendingEssence, canAscend } = calculatePendingAscension(sectPower, spiritStones, sectRealm);

  const handleAscend = () => {
    soundManager.playBreakthrough();
    triggerCelebration(perfTier, { particleCount: 120, spread: 100, origin: { y: 0.5 } });
    onConfirmAscension(pendingEssence);
  };

  return (
    <ModalShell
      isOpen={isOpen}
      onClose={onClose}
      maxWidth="lg"
      borderClassName="border-amber-500/40"
      bodyClassName="p-6 space-y-4 text-xs overflow-y-auto max-h-[75vh]"
      icon={
        <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
          <Award className="w-5 h-5 text-amber-400" />
        </div>
      }
      title={<span className="text-amber-300">Heavenly Sect Ascension</span>}
      subtitle="Transcend mortal cycles and reincarnate into an Ancient Immortal Sect"
    >
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

      <div className="p-4 bg-gradient-to-r from-amber-950/40 to-slate-950 border border-amber-500/30 rounded-xl space-y-2">
        <div className="flex items-center justify-between">
          <span className="text-slate-200 font-bold">New Ancestral Essence on Ascension:</span>
          <span className="text-amber-400 font-mono font-bold text-lg">+{pendingEssence} Essence</span>
        </div>
        <p className="text-[11px] text-slate-400">
          Derived from {sectPower} Sect Power and {spiritStones} Spirit Stones accumulated.
        </p>
      </div>

      <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl text-slate-400 text-[11px] leading-relaxed flex items-start gap-2.5">
        <ShieldAlert className="w-4 h-4 text-amber-400 shrink-0 mt-0.5" />
        <span>
          Ascension resets disciples and pavilions to Day 1, while preserving your Celestial Jade, Tech Tree masteries, and permanently compounding all revenue & disciple combat power!
        </span>
      </div>

      <button
        type="button"
        onClick={handleAscend}
        disabled={!canAscend}
        className={`w-full py-3.5 rounded-xl font-bold text-xs transition shadow-lg flex items-center justify-center gap-2 ${
          canAscend
            ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
            : 'bg-slate-800 text-slate-500 cursor-not-allowed'
        }`}
      >
        <RotateCcw className="w-4 h-4" />
        {canAscend
          ? `Transcend Cycle & Reincarnate (+${pendingEssence} Essence)`
          : 'Requires Core Formation Realm (Realm 3+) or 10+ Essence'}
      </button>
    </ModalShell>
  );
};
