import React, { useState } from 'react';
import { ALCHEMY_RECIPES } from '../constants/gameData';
import { Disciple } from '../types/game';
import { soundManager } from '../utils/audio';
import { Flame, Sparkles, AlertCircle, CheckCircle, X, Award } from 'lucide-react';
import confetti from 'canvas-confetti';

interface AlchemyForgeProps {
  isOpen: boolean;
  onClose: () => void;
  spiritHerbs: number;
  disciples: Disciple[];
  onCraftPill: (herbCost: number, pillName: string, exp: number, success: boolean) => void;
  onOpenSlashMiniGame: () => void;
}

export const AlchemyForge: React.FC<AlchemyForgeProps> = ({
  isOpen,
  onClose,
  spiritHerbs,
  disciples,
  onCraftPill,
  onOpenSlashMiniGame,
}) => {
  const [selectedRecipeIndex, setSelectedRecipeIndex] = useState<number>(0);
  const [selectedWorkerId, setSelectedWorkerId] = useState<string>(
    disciples.length > 0 ? disciples[0].id : ''
  );
  const [isCrafting, setIsCrafting] = useState<boolean>(false);
  const [craftResult, setCraftResult] = useState<{
    success: boolean;
    quality: number;
    pillsMade: number;
    message: string;
  } | null>(null);

  if (!isOpen) return null;

  const recipe = ALCHEMY_RECIPES[selectedRecipeIndex];
  const worker = disciples.find(d => d.id === selectedWorkerId);

  // Bonus rate from worker stats (INT/3 + LCK/4)
  const workerBonus = worker ? Math.floor(worker.stats.intel / 3 + worker.stats.lck / 4) : 0;
  const totalChance = Math.min(95, Math.max(10, recipe.baseRate + workerBonus));
  const canAfford = spiritHerbs >= recipe.herbCost;

  const handleConcoct = () => {
    if (!canAfford || isCrafting) return;
    setIsCrafting(true);
    setCraftResult(null);
    soundManager.playSwordStrike();

    setTimeout(() => {
      const roll = Math.random() * 100;
      const success = roll <= totalChance;

      let quality = 1;
      let pillsMade = 1;
      let expGained = recipe.exp;

      if (success) {
        if (Math.random() < 0.35 + (worker ? worker.stats.lck * 0.01 : 0)) {
          quality = 2; // High Grade
          pillsMade = 2;
          expGained *= 2;
        }
        if (Math.random() < 0.15 + (worker ? worker.stats.lck * 0.005 : 0)) {
          quality = 3; // Supreme Grade
          pillsMade = 3;
          expGained *= 3;
        }
        soundManager.playBreakthrough();
        confetti({ particleCount: 70, spread: 60 });
        setCraftResult({
          success: true,
          quality,
          pillsMade,
          message: `Success! Cauldron refined ${pillsMade}x ${recipe.name} (${quality === 3 ? 'Supreme Grade 🌟' : quality === 2 ? 'High Grade ⭐' : 'Standard Grade'})!`
        });
      } else {
        soundManager.playSwordStrike();
        setCraftResult({
          success: false,
          quality: 0,
          pillsMade: 0,
          message: 'Cauldron backlash! Black smoke erupted from the spirit fire and the batch was lost.'
        });
      }

      setIsCrafting(false);
      onCraftPill(recipe.herbCost, recipe.name, expGained, success);
    }, 1000);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-purple-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-purple-500/10 border border-purple-500/30 rounded-xl">
              <Flame className="w-5 h-5 text-purple-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                Sect Alchemy Pavilion
              </h3>
              <p className="text-xs text-slate-400">
                Refine sacred herbs into longevity & breakthrough pills
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
          {/* Quick swipe mini-game promo banner */}
          <div className="p-3 bg-gradient-to-r from-purple-950/40 via-amber-950/30 to-purple-950/40 border border-purple-500/30 rounded-xl flex items-center justify-between">
            <div>
              <div className="font-bold text-xs text-amber-300 flex items-center gap-1.5">
                <Sparkles className="w-3.5 h-3.5" />
                Want Instant Pill & Herb Bonuses?
              </div>
              <p className="text-[11px] text-slate-400">Play the Fruit-Slash mini-game to boost cauldron yields!</p>
            </div>
            <button
              onClick={onOpenSlashMiniGame}
              className="px-3.5 py-1.5 bg-purple-600 hover:bg-purple-500 text-white font-bold text-xs rounded-lg transition shadow-sm"
            >
              Play Slash Game
            </button>
          </div>

          {/* Herb Balance */}
          <div className="flex items-center justify-between bg-slate-950 border border-slate-800 rounded-xl px-4 py-2.5 text-xs">
            <span className="text-slate-400">Available Spirit Herbs:</span>
            <span className="font-mono text-emerald-400 font-bold text-sm">🌿 {spiritHerbs}</span>
          </div>

          {/* Select Pill Recipe */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Pill Formulas
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2">
              {ALCHEMY_RECIPES.map((r, idx) => (
                <button
                  key={r.name}
                  onClick={() => {
                    soundManager.playClick();
                    setSelectedRecipeIndex(idx);
                  }}
                  className={`p-3 text-left rounded-xl border transition ${
                    selectedRecipeIndex === idx
                      ? 'bg-purple-950/40 border-purple-500 text-purple-200 shadow-md'
                      : 'bg-slate-800/40 border-slate-700/60 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <div className="font-bold text-xs truncate">{r.name}</div>
                  <div className="text-[10px] text-slate-400 mt-1 flex justify-between">
                    <span>🌿 {r.herbCost} Herbs</span>
                    <span>{r.baseRate}%</span>
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Selected Recipe Details */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 space-y-2">
            <div className="flex items-center justify-between">
              <span className="font-serif-title font-bold text-slate-100 text-sm">{recipe.name}</span>
              <span className="text-xs font-mono text-purple-400">Base Rate: {recipe.baseRate}%</span>
            </div>
            <p className="text-xs text-slate-400">{recipe.desc}</p>
            <div className="flex items-center justify-between text-xs pt-2 border-t border-slate-800 text-slate-300">
              <span>Refining Cost: <strong className="font-mono text-emerald-400">{recipe.herbCost} Herbs</strong></span>
              <span>Dao EXP: <strong className="font-mono text-amber-400">+{recipe.exp}</strong></span>
            </div>
          </div>

          {/* Select Alchemist Worker */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Assign Master Alchemist
            </label>
            <div className="grid grid-cols-2 gap-2 max-h-32 overflow-y-auto pr-1">
              {disciples.map(d => (
                <button
                  key={d.id}
                  onClick={() => {
                    soundManager.playClick();
                    setSelectedWorkerId(d.id);
                  }}
                  className={`p-2 rounded-xl border text-left text-xs transition ${
                    selectedWorkerId === d.id
                      ? 'bg-purple-500/10 border-purple-500 text-purple-300'
                      : 'bg-slate-800/40 border-slate-700 text-slate-300 hover:bg-slate-800'
                  }`}
                >
                  <div className="font-bold truncate">{d.name}</div>
                  <div className="text-[10px] text-slate-400">
                    INT: {d.stats.intel} • LCK: {d.stats.lck} (+{Math.floor(d.stats.intel / 3 + d.stats.lck / 4)}%)
                  </div>
                </button>
              ))}
            </div>
          </div>

          {/* Total Success Probability Gauge */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-3 flex items-center justify-between text-xs">
            <span className="text-slate-400">Total Concoction Chance:</span>
            <div className="flex items-center gap-2">
              <div className="w-32 h-2.5 bg-slate-800 rounded-full overflow-hidden">
                <div
                  className="h-full bg-purple-500 transition-all duration-300"
                  style={{ width: `${totalChance}%` }}
                />
              </div>
              <span className="font-mono font-bold text-purple-300">{totalChance}%</span>
            </div>
          </div>

          {/* Craft Result Message */}
          {craftResult && (
            <div
              className={`p-3.5 rounded-xl border text-xs font-bold flex items-center gap-2 animate-fade-in ${
                craftResult.success
                  ? 'bg-emerald-950/40 border-emerald-500/50 text-emerald-300'
                  : 'bg-red-950/40 border-red-500/50 text-red-300'
              }`}
            >
              {craftResult.success ? <CheckCircle className="w-4 h-4 text-emerald-400" /> : <AlertCircle className="w-4 h-4 text-red-400" />}
              <span>{craftResult.message}</span>
            </div>
          )}

          {/* Action */}
          <button
            onClick={handleConcoct}
            disabled={!canAfford || isCrafting}
            className={`w-full py-3.5 rounded-xl font-bold text-sm transition shadow-lg flex items-center justify-center gap-2 ${
              !canAfford
                ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                : isCrafting
                ? 'bg-slate-800 text-slate-500'
                : 'bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 hover:to-indigo-500 text-white'
            }`}
          >
            <Flame className="w-4 h-4" />
            {isCrafting ? 'Refining Cauldron Spirit Flame...' : canAfford ? 'Ignite Cauldron & Refine Pill' : 'Insufficient Spirit Herbs'}
          </button>
        </div>
      </div>
    </div>
  );
};
