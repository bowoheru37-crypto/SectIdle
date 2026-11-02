import React from 'react';
import { TechNode } from '../types/game';
import { soundManager } from '../utils/audio';
import { BookOpen, CheckCircle, Sparkles, Lock, ArrowRight } from 'lucide-react';
import confetti from 'canvas-confetti';

interface TechTreeProps {
  techTree: TechNode[];
  sectExp: number;
  onResearchTech: (tech: TechNode) => void;
}

export const TechTree: React.FC<TechTreeProps> = ({
  techTree,
  sectExp,
  onResearchTech,
}) => {
  const eraNames = [
    'Mortal Realm Dawn',
    'Qi Refining Awakening',
    'Core Formation Dominance',
    'Immortal Ascendance Era',
  ];

  const researchedCount = techTree.filter(t => t.isResearched).length;
  const currentEra = Math.min(3, Math.floor(researchedCount / 2));

  return (
    <div className="space-y-5">
      {/* Header */}
      <div className="bg-slate-900/80 border border-slate-800 p-5 rounded-2xl flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div>
          <h2 className="font-serif-title font-bold text-slate-100 text-lg flex items-center gap-2">
            <BookOpen className="w-5 h-5 text-cyan-400" />
            Civilization Dao Research Tree
          </h2>
          <p className="text-xs text-slate-400">
            Advance sect civilization eras, unlock automated spirit arrays, and boost global yields
          </p>
        </div>

        <div className="bg-slate-950 px-4 py-2 rounded-xl border border-slate-800 text-xs">
          <span className="text-slate-500 block text-[10px] uppercase tracking-wider">Current Dao Era</span>
          <span className="font-serif-title font-bold text-cyan-400 text-sm">
            {eraNames[currentEra]}
          </span>
        </div>
      </div>

      {/* Available Sect Exp */}
      <div className="bg-slate-950 border border-slate-800 px-5 py-3 rounded-xl flex items-center justify-between text-xs font-mono">
        <span className="text-slate-400">Available Sect Enlightenment (EXP):</span>
        <span className="text-amber-400 font-bold text-sm">📜 {sectExp} EXP</span>
      </div>

      {/* Tech Nodes List */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {techTree.map(tech => {
          const canAfford = sectExp >= tech.costExp && !tech.isResearched;
          const isUnlockedEra = tech.era <= currentEra;

          return (
            <div
              key={tech.id}
              className={`p-5 rounded-2xl border transition-all ${
                tech.isResearched
                  ? 'bg-emerald-950/20 border-emerald-500/40 shadow-sm'
                  : isUnlockedEra
                  ? 'bg-slate-900/70 border-slate-800 hover:border-cyan-500/50'
                  : 'bg-slate-950/40 border-slate-900 opacity-50'
              }`}
            >
              <div className="flex items-start justify-between gap-3 mb-2">
                <div>
                  <div className="font-serif-title font-bold text-sm text-slate-100 flex items-center gap-1.5">
                    {tech.name}
                    {tech.isResearched && <CheckCircle className="w-4 h-4 text-emerald-400" />}
                  </div>
                  <span className="text-[10px] text-cyan-400 font-mono block mt-0.5">
                    Era: {eraNames[tech.era]}
                  </span>
                </div>

                {!tech.isResearched && (
                  <span className="text-xs font-mono font-bold text-amber-400 bg-slate-950 px-2 py-1 rounded-md border border-slate-800">
                    {tech.costExp} EXP
                  </span>
                )}
              </div>

              <p className="text-xs text-slate-300 leading-relaxed mb-4">
                {tech.description}
              </p>

              {tech.isResearched ? (
                <div className="text-[11px] font-bold text-emerald-400 flex items-center gap-1">
                  <CheckCircle className="w-3.5 h-3.5" /> Comprehended into Sect Dao Core
                </div>
              ) : isUnlockedEra ? (
                <button
                  onClick={() => {
                    if (canAfford) {
                      soundManager.playBreakthrough();
                      confetti({ particleCount: 50, spread: 50 });
                      onResearchTech(tech);
                    }
                  }}
                  disabled={!canAfford}
                  className={`w-full py-2.5 rounded-xl text-xs font-bold transition flex items-center justify-center gap-1.5 shadow-sm ${
                    canAfford
                      ? 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 text-slate-950'
                      : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                  }`}
                >
                  <Sparkles className="w-3.5 h-3.5" />
                  {canAfford ? 'Comprehend Dao Technology' : 'Insufficient Sect EXP'}
                </button>
              ) : (
                <div className="text-[11px] text-slate-500 flex items-center gap-1">
                  <Lock className="w-3.5 h-3.5" /> Locked until previous Dao Eras are mastered
                </div>
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
};
