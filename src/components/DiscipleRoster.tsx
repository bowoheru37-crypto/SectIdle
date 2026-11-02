import React, { useState } from 'react';
import { Disciple } from '../types/game';
import {
  TASK_NAMES,
  TASK_COLORS,
  REALMS,
  PERSONALITY_NAMES,
  ELEMENT_NAMES,
  ELEMENT_COLORS,
} from '../constants/gameData';
import { soundManager } from '../utils/audio';
import { getOptimalTaskForDisciple, calculateEfficiency } from '../utils/discipleFactory';
import {
  Users,
  UserPlus,
  Sparkles,
  Zap,
  Heart,
  Flame,
  Shield,
  Briefcase,
  Bot,
  X,
  Smile,
  Compass,
} from 'lucide-react';

interface DiscipleRosterProps {
  disciples: Disciple[];
  onAssignTask: (discipleId: string, task: number) => void;
  onAutoAssignAll: () => void;
  onRecruitNew: () => void;
  onOpenTribulation: (disciple: Disciple) => void;
  onSummonFairy: (discipleId: string) => void;
  maxDisciples: number;
}

export const DiscipleRoster: React.FC<DiscipleRosterProps> = ({
  disciples,
  onAssignTask,
  onAutoAssignAll,
  onRecruitNew,
  onOpenTribulation,
  onSummonFairy,
  maxDisciples,
}) => {
  const [selectedDisciple, setSelectedDisciple] = useState<Disciple | null>(null);
  const [taskPickerDisciple, setTaskPickerDisciple] = useState<Disciple | null>(null);

  return (
    <div className="space-y-4">
      {/* Top action bar */}
      <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-900/80 border border-slate-800 p-4 rounded-2xl shadow-sm">
        <div>
          <h2 className="font-serif-title font-bold text-slate-100 text-lg flex items-center gap-2">
            <Users className="w-5 h-5 text-amber-400" />
            Sect Disciples ({disciples.length} / {maxDisciples})
          </h2>
          <p className="text-xs text-slate-400">
            Train, cultivate, assign tasks, and guide disciples through realm ascensions
          </p>
        </div>

        <div className="flex items-center gap-2.5">
          <button
            onClick={() => {
              soundManager.playClick();
              onAutoAssignAll();
            }}
            className="flex items-center gap-1.5 px-3.5 py-2 bg-indigo-600/20 hover:bg-indigo-600/30 border border-indigo-500/30 text-indigo-300 font-bold text-xs rounded-xl transition"
          >
            <Bot className="w-4 h-4" />
            AI Auto-Assign Workforce
          </button>

          <button
            onClick={() => {
              soundManager.playClick();
              onRecruitNew();
            }}
            disabled={disciples.length >= maxDisciples}
            className={`flex items-center gap-1.5 px-4 py-2 rounded-xl text-xs font-bold transition shadow-md ${
              disciples.length < maxDisciples
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed'
            }`}
          >
            <UserPlus className="w-4 h-4" />
            Recruit Disciple
          </button>
        </div>
      </div>

      {/* Disciples Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {disciples.map(d => {
          const isReadyBreakthrough = d.realmExp >= 1000 && d.realm < REALMS.length - 1;
          const currentTaskName = TASK_NAMES[d.currentTask];
          const taskColor = TASK_COLORS[d.currentTask];
          const elementColor = ELEMENT_COLORS[d.element];

          return (
            <div
              key={d.id}
              className="bg-slate-900/70 border border-slate-800 hover:border-slate-700/80 rounded-2xl p-4 flex flex-col justify-between transition-all shadow-md hover:shadow-slate-950/50"
            >
              <div>
                {/* Header info */}
                <div className="flex items-start justify-between gap-2 mb-2.5">
                  <div className="flex items-center gap-3">
                    <div
                      className="w-11 h-11 rounded-xl flex items-center justify-center text-xl font-bold border"
                      style={{
                        backgroundColor: `${elementColor}15`,
                        borderColor: `${elementColor}40`,
                      }}
                    >
                      {d.isMale ? '🧑‍🦳' : '👩‍🦳'}
                    </div>
                    <div>
                      <div className="font-bold text-sm text-slate-100 flex items-center gap-1.5">
                        {d.name}
                        {d.fairy && (
                          <span title={`Fairy Companion: ${d.fairy.name}`} className="text-xs">
                            🧚
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400">
                        {REALMS[d.realm]} • {d.age} yrs • {PERSONALITY_NAMES[d.personality]}
                      </div>
                    </div>
                  </div>

                  <span
                    className="text-[10px] px-2 py-0.5 rounded-full font-mono font-bold"
                    style={{
                      backgroundColor: `${elementColor}22`,
                      color: elementColor,
                      border: `1px solid ${elementColor}44`,
                    }}
                  >
                    {ELEMENT_NAMES[d.element]}
                  </span>
                </div>

                {/* Talent & Stats */}
                <div className="bg-slate-950/60 rounded-xl p-2.5 mb-3 border border-slate-800/80 space-y-1.5 text-xs">
                  <div className="flex justify-between text-[11px]">
                    <span className="text-slate-400">Talent:</span>
                    <span className="text-amber-300 font-semibold">{d.talentName}</span>
                  </div>

                  <div className="grid grid-cols-4 gap-1 text-[11px] font-mono text-center pt-1 border-t border-slate-800/60">
                    <div>
                      <span className="text-slate-500 block text-[9px]">STR</span>
                      <strong className="text-red-400">{d.stats.str}</strong>
                    </div>
                    <div>
                      <span className="text-slate-500 block text-[9px]">AGI</span>
                      <strong className="text-cyan-400">{d.stats.agi}</strong>
                    </div>
                    <div>
                      <span className="text-slate-500 block text-[9px]">INT</span>
                      <strong className="text-indigo-400">{d.stats.intel}</strong>
                    </div>
                    <div>
                      <span className="text-slate-500 block text-[9px]">LCK</span>
                      <strong className="text-amber-400">{d.stats.lck}</strong>
                    </div>
                  </div>
                </div>

                {/* Energy & Mood Bars */}
                <div className="grid grid-cols-2 gap-2 mb-3 text-[10px]">
                  <div>
                    <div className="flex justify-between text-slate-400 mb-0.5">
                      <span>Energy</span>
                      <span className="font-mono">{d.energy}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-cyan-400"
                        style={{ width: `${d.energy}%` }}
                      />
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between text-slate-400 mb-0.5">
                      <span>Mood</span>
                      <span className="font-mono">{d.mood}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div
                        className="h-full bg-pink-400"
                        style={{ width: `${d.mood}%` }}
                      />
                    </div>
                  </div>
                </div>

                {/* Current Task & Wage */}
                <div className="flex items-center justify-between text-xs bg-slate-950/40 p-2 rounded-lg border border-slate-800/50 mb-3">
                  <div className="flex items-center gap-1.5">
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: taskColor }}
                    />
                    <span className="text-slate-300 font-medium">Task: {currentTaskName}</span>
                  </div>
                  <span className="text-[11px] font-mono text-slate-400">{d.dailyWage} SS/day</span>
                </div>
              </div>

              {/* Bottom Actions */}
              <div className="space-y-1.5 pt-2 border-t border-slate-800">
                {isReadyBreakthrough ? (
                  <button
                    onClick={() => {
                      soundManager.playClick();
                      onOpenTribulation(d);
                    }}
                    className="w-full py-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs rounded-xl transition flex items-center justify-center gap-1.5 shadow-md animate-pulse"
                  >
                    <Zap className="w-4 h-4" />
                    Enter Lightning Tribulation Breakthrough!
                  </button>
                ) : (
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      onClick={() => {
                        soundManager.playClick();
                        setTaskPickerDisciple(d);
                      }}
                      className="py-1.5 px-3 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs rounded-xl transition"
                    >
                      Assign Task
                    </button>
                    <button
                      onClick={() => {
                        soundManager.playClick();
                        setSelectedDisciple(d);
                      }}
                      className="py-1.5 px-3 bg-slate-800/60 hover:bg-slate-700 text-cyan-300 font-bold text-xs rounded-xl border border-slate-700 transition"
                    >
                      View Details
                    </button>
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Task Picker Modal */}
      {taskPickerDisciple && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-md p-4">
          <div className="relative w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl p-5 shadow-2xl">
            <div className="flex items-center justify-between mb-3 border-b border-slate-800 pb-2">
              <h3 className="font-serif-title font-bold text-slate-100 text-base">
                Assign Task to {taskPickerDisciple.name}
              </h3>
              <button
                onClick={() => setTaskPickerDisciple(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="grid grid-cols-2 gap-2 max-h-72 overflow-y-auto pr-1">
              {TASK_NAMES.map((tName, tIdx) => {
                const eff = calculateEfficiency(taskPickerDisciple, tIdx);
                const isSelected = taskPickerDisciple.currentTask === tIdx;
                return (
                  <button
                    key={tIdx}
                    onClick={() => {
                      soundManager.playClick();
                      onAssignTask(taskPickerDisciple.id, tIdx);
                      setTaskPickerDisciple(null);
                    }}
                    className={`p-2.5 text-left rounded-xl border transition ${
                      isSelected
                        ? 'bg-amber-500/10 border-amber-500 text-amber-300'
                        : 'bg-slate-800/40 border-slate-700 hover:bg-slate-800 text-slate-300'
                    }`}
                  >
                    <div className="font-bold text-xs flex items-center gap-1.5">
                      <span
                        className="w-2 h-2 rounded-full"
                        style={{ backgroundColor: TASK_COLORS[tIdx] }}
                      />
                      {tName}
                    </div>
                    {tIdx > 0 && (
                      <div className="text-[10px] text-slate-400 mt-1">Efficiency: {eff}%</div>
                    )}
                  </button>
                );
              })}
            </div>

            {/* AI Recommendation tip */}
            <div className="mt-4 p-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-indigo-300 flex items-center justify-between">
              <span>
                AI Optimal: <strong>{TASK_NAMES[getOptimalTaskForDisciple(taskPickerDisciple)]}</strong>
              </span>
              <button
                onClick={() => {
                  soundManager.playClick();
                  const opt = getOptimalTaskForDisciple(taskPickerDisciple);
                  onAssignTask(taskPickerDisciple.id, opt);
                  setTaskPickerDisciple(null);
                }}
                className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-lg text-[11px]"
              >
                Apply Optimal
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Disciple Full Details Modal */}
      {selectedDisciple && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
          <div className="relative w-full max-w-lg bg-slate-900 border border-cyan-500/30 rounded-2xl p-6 shadow-2xl overflow-y-auto max-h-[85vh]">
            <div className="flex items-start justify-between border-b border-slate-800 pb-3 mb-4">
              <div className="flex items-center gap-3">
                <span className="text-3xl">{selectedDisciple.isMale ? '🧑‍🦳' : '👩‍🦳'}</span>
                <div>
                  <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                    {selectedDisciple.name}
                  </h3>
                  <div className="text-xs text-slate-400">
                    {REALMS[selectedDisciple.realm]} • {ELEMENT_NAMES[selectedDisciple.element]} Element
                  </div>
                </div>
              </div>
              <button
                onClick={() => setSelectedDisciple(null)}
                className="text-slate-400 hover:text-white"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            {/* Combat Specs */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 mb-4">
              <h4 className="text-xs font-bold text-amber-400 uppercase tracking-wider mb-2">
                Combat Potency & Attributes
              </h4>
              <div className="grid grid-cols-3 gap-2 text-xs font-mono">
                <div>HP: <strong className="text-emerald-400">{selectedDisciple.maxHp}</strong></div>
                <div>MP: <strong className="text-cyan-400">{selectedDisciple.maxMp}</strong></div>
                <div>ATK: <strong className="text-red-400">{selectedDisciple.atk}</strong></div>
                <div>DEF: <strong className="text-blue-400">{selectedDisciple.def}</strong></div>
                <div>SPD: <strong className="text-amber-400">{selectedDisciple.spd}</strong></div>
                <div>CRIT: <strong className="text-purple-400">{selectedDisciple.critRate}%</strong></div>
              </div>
            </div>

            {/* Seven Core Dao Attributes */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 mb-4">
              <h4 className="text-xs font-bold text-cyan-400 uppercase tracking-wider mb-2">
                Seven Pillars of Dao Root
              </h4>
              <div className="grid grid-cols-4 gap-2 text-center text-xs font-mono">
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">STR</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.str}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">AGI</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.agi}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">INT</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.intel}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">LCK</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.lck}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">VIT</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.vit}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">WIS</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.wis}</span>
                </div>
                <div className="p-1.5 bg-slate-900 rounded-lg">
                  <span className="text-[10px] text-slate-500 block">CHA</span>
                  <span className="font-bold text-slate-200">{selectedDisciple.stats.cha}</span>
                </div>
              </div>
            </div>

            {/* Spiritual Fairy Companion */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 mb-4">
              <div className="flex items-center justify-between mb-2">
                <h4 className="text-xs font-bold text-pink-400 uppercase tracking-wider flex items-center gap-1.5">
                  <span>🧚 Spiritual Fairy Companion</span>
                </h4>
                {!selectedDisciple.fairy && (
                  <button
                    onClick={() => {
                      soundManager.playBreakthrough();
                      onSummonFairy(selectedDisciple.id);
                      setSelectedDisciple(null);
                    }}
                    className="px-2.5 py-1 bg-pink-600/20 hover:bg-pink-600/30 border border-pink-500/40 text-pink-300 rounded-lg text-xs font-bold transition"
                  >
                    Summon Fairy (20 Essence)
                  </button>
                )}
              </div>
              {selectedDisciple.fairy ? (
                <div className="text-xs text-slate-300 space-y-1">
                  <div>Name: <strong className="text-pink-300">{selectedDisciple.fairy.name}</strong> ({selectedDisciple.fairy.element})</div>
                  <div className="text-slate-400 text-[11px]">
                    Grants +{selectedDisciple.fairy.efficiencyBonus}% Work Efficiency & +{selectedDisciple.fairy.cultivationBonus}% Qi Speed
                  </div>
                </div>
              ) : (
                <p className="text-xs text-slate-500">No spiritual fairy has bonded with this disciple yet.</p>
              )}
            </div>

            <button
              onClick={() => setSelectedDisciple(null)}
              className="w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold rounded-xl text-xs transition"
            >
              Close Record
            </button>
          </div>
        </div>
      )}
    </div>
  );
};
