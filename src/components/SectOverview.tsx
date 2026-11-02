import React, { useState } from 'react';
import { Building, Disciple } from '../types/game';
import { soundManager } from '../utils/audio';
import {
  Sparkles,
  ArrowUpCircle,
  Hammer,
  Pickaxe,
  BookOpen,
  Sprout,
  Store,
  Swords,
  Flame,
  Castle,
  X,
  Play,
} from 'lucide-react';

interface SectOverviewProps {
  buildings: Building[];
  disciples: Disciple[];
  spiritStones: number;
  onUpgradeBuilding: (buildingId: number) => void;
  onBuildBuilding: (buildingId: number) => void;
  onBuildingAction: (building: Building) => void;
}

export const SectOverview: React.FC<SectOverviewProps> = ({
  buildings,
  disciples,
  spiritStones,
  onUpgradeBuilding,
  onBuildBuilding,
  onBuildingAction,
}) => {
  const [selectedBuilding, setSelectedBuilding] = useState<Building | null>(null);

  const getBuildingIcon = (iconName: string) => {
    switch (iconName) {
      case 'palace': return <Castle className="w-6 h-6 text-amber-400" />;
      case 'book': return <BookOpen className="w-6 h-6 text-cyan-400" />;
      case 'flame': return <Flame className="w-6 h-6 text-purple-400" />;
      case 'hammer': return <Hammer className="w-6 h-6 text-orange-400" />;
      case 'sprout': return <Sprout className="w-6 h-6 text-emerald-400" />;
      case 'pickaxe': return <Pickaxe className="w-6 h-6 text-yellow-400" />;
      case 'store': return <Store className="w-6 h-6 text-blue-400" />;
      case 'swords': return <Swords className="w-6 h-6 text-red-400" />;
      default: return <Castle className="w-6 h-6 text-amber-400" />;
    }
  };

  return (
    <div className="space-y-4">
      {/* Visual Sect Ground Layout (Xianxia Mountain Domain) */}
      <div className="relative w-full h-[480px] bg-gradient-to-b from-slate-950 via-slate-900 to-indigo-950/40 border border-slate-800 rounded-3xl overflow-hidden p-6 shadow-2xl flex flex-col justify-between">
        {/* Background Mountain silhouettes & celestial clouds */}
        <div className="absolute inset-0 pointer-events-none opacity-20 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:24px_24px]" />
        <div className="absolute top-10 left-1/2 -translate-x-1/2 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />

        {/* Sect Banner Top */}
        <div className="relative z-10 flex items-center justify-between">
          <div className="flex items-center gap-2 px-3 py-1.5 bg-slate-900/80 border border-slate-800 rounded-xl backdrop-blur-md">
            <span className="w-2 h-2 rounded-full bg-emerald-400 animate-ping" />
            <span className="font-serif-title font-bold text-xs text-amber-300">
              Cloud Mist Mountain • Spiritual Domain
            </span>
          </div>

          <div className="text-xs text-slate-400 bg-slate-900/80 px-3 py-1.5 rounded-xl border border-slate-800">
            Click buildings to upgrade, harvest, or refine!
          </div>
        </div>

        {/* Interactive Buildings Array Layout */}
        <div className="relative z-10 grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 my-auto">
          {buildings.map(b => {
            const canAfford = spiritStones >= b.upgradeCost;
            return (
              <div
                key={b.id}
                onClick={() => {
                  soundManager.playClick();
                  setSelectedBuilding(b);
                }}
                className={`relative group cursor-pointer p-4 rounded-2xl border transition-all duration-300 backdrop-blur-md flex flex-col justify-between h-36 ${
                  b.isBuilt
                    ? 'bg-slate-900/80 border-slate-700/80 hover:border-amber-500/80 hover:bg-slate-800/90 shadow-lg'
                    : 'bg-slate-950/60 border-dashed border-slate-800 hover:border-slate-600 opacity-70 hover:opacity-100'
                }`}
              >
                {/* Header icon + level tag */}
                <div className="flex items-start justify-between">
                  <div className="p-2.5 bg-slate-950/90 border border-slate-800 rounded-xl group-hover:scale-105 transition-transform">
                    {getBuildingIcon(b.icon)}
                  </div>
                  {b.isBuilt ? (
                    <span className="px-2 py-0.5 bg-amber-500/10 border border-amber-500/30 text-amber-300 text-[10px] font-mono font-bold rounded-full">
                      Lv.{b.level}
                    </span>
                  ) : (
                    <span className="px-2 py-0.5 bg-slate-800 text-slate-400 text-[10px] font-mono rounded-full">
                      Unbuilt
                    </span>
                  )}
                </div>

                {/* Building name & daily income */}
                <div>
                  <h4 className="font-serif-title font-bold text-xs text-slate-100 group-hover:text-amber-300 transition-colors">
                    {b.name}
                  </h4>
                  <p className="text-[10px] text-slate-400 mt-0.5">
                    {b.isBuilt ? `+${b.level * 25} SS/day • ${b.workers} Disciples` : `Cost: ${b.cost} SS`}
                  </p>
                </div>

                {/* Action button hover hint */}
                <div className="text-[10px] font-bold text-cyan-400 flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                  <Sparkles className="w-3 h-3" /> Inspect & Manage
                </div>
              </div>
            );
          })}
        </div>

        {/* Wandering Disciples Presence Bar */}
        <div className="relative z-10 flex items-center justify-between text-xs bg-slate-950/80 border border-slate-800/80 px-4 py-2 rounded-2xl backdrop-blur-md">
          <div className="flex items-center gap-2 text-slate-300">
            <span>Cultivating In Domain:</span>
            <div className="flex -space-x-1 overflow-hidden">
              {disciples.slice(0, 6).map((d, i) => (
                <div
                  key={d.id}
                  title={`${d.name} (${d.isMale ? 'Male' : 'Female'})`}
                  className="w-6 h-6 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xs"
                >
                  {d.isMale ? '🧑‍🦳' : '👩‍🦳'}
                </div>
              ))}
            </div>
            <span className="text-[11px] text-slate-500">+{disciples.length} disciples</span>
          </div>

          <div className="text-[11px] text-emerald-400 font-mono">
            Mountain Qi Flow: Normal (+100%)
          </div>
        </div>
      </div>

      {/* Building Details & Upgrade Modal */}
      {selectedBuilding && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4">
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-6 shadow-2xl">
            <div className="flex items-start justify-between border-b border-slate-800 pb-3 mb-4">
              <div className="flex items-center gap-3">
                <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl">
                  {getBuildingIcon(selectedBuilding.icon)}
                </div>
                <div>
                  <h3 className="font-serif-title font-bold text-slate-100 text-base">
                    {selectedBuilding.name}
                  </h3>
                  <div className="text-xs text-slate-400">
                    {selectedBuilding.isBuilt ? `Level ${selectedBuilding.level} Pavilion` : 'Unconstructed Lot'}
                  </div>
                </div>
              </div>
              <button
                onClick={() => setSelectedBuilding(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="space-y-3 mb-5 text-xs text-slate-300">
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Daily Revenue:</span>
                <strong className="text-amber-400">
                  +{selectedBuilding.isBuilt ? selectedBuilding.level * 25 : 0} SS/day
                </strong>
              </div>
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Assigned Cultivators:</span>
                <strong>{selectedBuilding.workers} / {selectedBuilding.maxWorkers}</strong>
              </div>
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Upgrade Cost:</span>
                <strong className="text-cyan-400">{selectedBuilding.upgradeCost} Spirit Stones</strong>
              </div>
            </div>

            {/* Special Action Button for Gardens / Alchemy / Arena */}
            {(selectedBuilding.name.includes('Garden') || selectedBuilding.name.includes('Alchemy')) && selectedBuilding.isBuilt && (
              <button
                onClick={() => {
                  setSelectedBuilding(null);
                  onBuildingAction(selectedBuilding);
                }}
                className="w-full mb-3 py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white font-bold rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition"
              >
                <Play className="w-3.5 h-3.5" />
                {selectedBuilding.name.includes('Garden') ? 'Play Herb Slash Mini-Game' : 'Open Cauldron Concoction'}
              </button>
            )}

            {/* Build or Upgrade Button */}
            {selectedBuilding.isBuilt ? (
              <button
                onClick={() => {
                  soundManager.playBreakthrough();
                  onUpgradeBuilding(selectedBuilding.id);
                  setSelectedBuilding(null);
                }}
                disabled={spiritStones < selectedBuilding.upgradeCost}
                className={`w-full py-3 rounded-xl font-bold text-xs transition shadow-lg ${
                  spiritStones >= selectedBuilding.upgradeCost
                    ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
                    : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                }`}
              >
                Upgrade to Lv.{selectedBuilding.level + 1} ({selectedBuilding.upgradeCost} SS)
              </button>
            ) : (
              <button
                onClick={() => {
                  soundManager.playBreakthrough();
                  onBuildBuilding(selectedBuilding.id);
                  setSelectedBuilding(null);
                }}
                disabled={spiritStones < selectedBuilding.cost}
                className={`w-full py-3 rounded-xl font-bold text-xs transition shadow-lg ${
                  spiritStones >= selectedBuilding.cost
                    ? 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 text-slate-950'
                    : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                }`}
              >
                Erect Pavilion ({selectedBuilding.cost} SS)
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
