import React, { useState } from 'react';
import { Building, DailyMission, Disciple } from '../types/game';
import { soundManager } from '../utils/audio';
import {
  Sparkles,
  Hammer,
  Pickaxe,
  BookOpen,
  Sprout,
  Store,
  Swords,
  Flame,
  Castle,
  Play,
} from 'lucide-react';
import { ModalShell } from './ModalShell';
import { DailyMissions } from './DailyMissions';

interface SectOverviewProps {
  sectName?: string;
  buildings: Building[];
  disciples: Disciple[];
  spiritStones: number;
  dailyMissions: DailyMission[];
  dailyMissionDayKey: string;
  onUpgradeBuilding: (buildingId: number) => void;
  onBuildBuilding: (buildingId: number) => void;
  onBuildingAction: (building: Building) => void;
  onClaimDailyMission: (missionId: string) => void;
  onRerollDailyMissions: () => void;
}

export const SectOverview: React.FC<SectOverviewProps> = ({
  sectName = 'Cloud Mist Mountain',
  buildings,
  disciples,
  spiritStones,
  dailyMissions,
  dailyMissionDayKey,
  onUpgradeBuilding,
  onBuildBuilding,
  onBuildingAction,
  onClaimDailyMission,
  onRerollDailyMissions,
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
      <div className="relative w-full min-h-[460px] bg-gradient-to-b from-slate-950 via-slate-900 to-indigo-950/40 border border-slate-800 rounded-3xl overflow-hidden p-4 sm:p-6 shadow-2xl flex flex-col justify-between gap-4">
        <div className="absolute inset-0 pointer-events-none opacity-20 bg-[radial-gradient(#38bdf8_1px,transparent_1px)] [background-size:24px_24px]" />
        <div className="absolute top-10 left-1/2 -translate-x-1/2 w-96 h-96 bg-cyan-500/5 rounded-full blur-3xl pointer-events-none" />

        <div className="relative z-10 flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2 px-3 py-1.5 bg-slate-900/80 border border-slate-800 rounded-xl backdrop-blur-md">
            <Castle className="w-3.5 h-3.5 text-amber-400" aria-hidden="true" />
            <span className="font-serif-title font-bold text-xs text-amber-300">
              {sectName} • Spiritual Domain
            </span>
          </div>

          <div className="text-xs text-slate-400 bg-slate-900/80 px-3 py-1.5 rounded-xl border border-slate-800">
            Select buildings to upgrade, harvest, or refine
          </div>
        </div>

        <div className="relative z-10 grid grid-cols-2 sm:grid-cols-4 gap-3 sm:gap-4 my-auto">
          {buildings.map(b => (
            <button
              key={b.id}
              type="button"
              onClick={() => {
                soundManager.playClick();
                setSelectedBuilding(b);
              }}
              className={`relative group text-left cursor-pointer p-4 rounded-2xl border transition-all duration-200 backdrop-blur-md flex flex-col justify-between h-36 ${
                b.isBuilt
                  ? 'bg-slate-900/80 border-slate-700/80 hover:border-amber-500/80 hover:bg-slate-800/90 shadow-lg'
                  : 'bg-slate-950/60 border-dashed border-slate-800 hover:border-slate-600 opacity-75 hover:opacity-100'
              }`}
            >
              <div className="flex items-start justify-between w-full">
                <div className="p-2.5 bg-slate-950/90 border border-slate-800 rounded-xl group-hover:scale-105 transition-transform">
                  {getBuildingIcon(b.icon)}
                </div>
                {b.isBuilt ? (
                  <span className="text-amber-300 text-[11px] font-mono font-bold tabular-nums">
                    Lv.{b.level}
                  </span>
                ) : (
                  <span className="text-slate-400 text-[11px] font-mono">
                    Unbuilt
                  </span>
                )}
              </div>

              <div>
                <h4 className="font-serif-title font-bold text-xs text-slate-100 group-hover:text-amber-300 transition-colors">
                  {b.name}
                </h4>
                <p className="text-[11px] text-slate-400 mt-0.5 tabular-nums">
                  {b.isBuilt ? `+${b.level * 25} SS/day • ${b.workers} Disciples` : `Cost: ${b.cost} SS`}
                </p>
              </div>

              <div className="text-[10px] font-bold text-cyan-400 flex items-center gap-1 opacity-0 group-hover:opacity-100 group-focus-visible:opacity-100 transition-opacity">
                <Sparkles className="w-3 h-3" /> Inspect & Manage
              </div>
            </button>
          ))}
        </div>

        <div className="relative z-10 flex flex-wrap items-center justify-between gap-2 text-xs bg-slate-950/80 border border-slate-800/80 px-4 py-2 rounded-2xl backdrop-blur-md">
          <div className="flex items-center gap-2 text-slate-300">
            <span>Cultivating In Domain:</span>
            <div className="flex -space-x-1 overflow-hidden">
              {disciples.slice(0, 6).map(d => (
                <div
                  key={d.id}
                  title={`${d.name} (${d.isMale ? 'Male' : 'Female'})`}
                  className="w-6 h-6 rounded-full bg-slate-800 border border-slate-700 flex items-center justify-center text-xs"
                >
                  {d.isMale ? '🧑‍🦳' : '👩‍🦳'}
                </div>
              ))}
            </div>
            <span className="text-[11px] text-slate-400 tabular-nums">{disciples.length} disciples</span>
          </div>

          <div className="text-[11px] text-emerald-400 font-mono tabular-nums">
            Mountain Qi Flow: Normal (+100%)
          </div>
        </div>
      </div>

      <DailyMissions
        missions={dailyMissions}
        dayKey={dailyMissionDayKey}
        spiritStones={spiritStones}
        onClaimMission={onClaimDailyMission}
        onRerollMissions={onRerollDailyMissions}
      />

      <ModalShell
        isOpen={Boolean(selectedBuilding)}
        onClose={() => setSelectedBuilding(null)}
        maxWidth="md"
        bodyClassName="p-6 space-y-4"
        icon={
          selectedBuilding && (
            <div className="p-2.5 bg-slate-950 border border-slate-800 rounded-xl">
              {getBuildingIcon(selectedBuilding.icon)}
            </div>
          )
        }
        title={selectedBuilding?.name ?? ''}
        subtitle={
          selectedBuilding?.isBuilt
            ? `Level ${selectedBuilding.level} Pavilion`
            : 'Unconstructed Lot'
        }
      >
        {selectedBuilding && (
          <>
            <div className="space-y-3 text-xs text-slate-300">
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Daily Revenue:</span>
                <strong className="text-amber-400">
                  +{selectedBuilding.isBuilt ? selectedBuilding.level * 25 : 0} SS/day
                </strong>
              </div>
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Assigned Cultivators:</span>
                <strong>
                  {selectedBuilding.workers} / {selectedBuilding.maxWorkers}
                </strong>
              </div>
              <div className="flex justify-between bg-slate-950 p-2.5 rounded-xl border border-slate-800 font-mono">
                <span>Upgrade Cost:</span>
                <strong className="text-cyan-400">{selectedBuilding.upgradeCost} Spirit Stones</strong>
              </div>
            </div>

            {(selectedBuilding.name.includes('Garden') || selectedBuilding.name.includes('Alchemy')) &&
              selectedBuilding.isBuilt && (
                <button
                  type="button"
                  onClick={() => {
                    const target = selectedBuilding;
                    setSelectedBuilding(null);
                    onBuildingAction(target);
                  }}
                  className="w-full py-2.5 bg-gradient-to-r from-emerald-600 to-teal-600 hover:from-emerald-500 text-white font-bold rounded-xl text-xs flex items-center justify-center gap-2 shadow-md transition"
                >
                  <Play className="w-3.5 h-3.5" />
                  {selectedBuilding.name.includes('Garden')
                    ? 'Play Herb Slash Mini-Game'
                    : 'Open Cauldron Concoction'}
                </button>
              )}

            {selectedBuilding.isBuilt ? (
              <button
                type="button"
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
                type="button"
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
          </>
        )}
      </ModalShell>
    </div>
  );
};
