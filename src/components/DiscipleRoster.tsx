import React, { useMemo, useState } from 'react';
import type { Disciple, DiscipleBondType } from '../types/game';
import {
  TASK_NAMES,
  TASK_COLORS,
  REALMS,
  PERSONALITY_NAMES,
  ELEMENT_NAMES,
  ELEMENT_COLORS,
} from '../constants/gameData';
import { soundManager } from '../utils/audio';
import {
  getOptimalTaskForDisciple,
  calculateEfficiency,
  getActiveBondBonusForDisciple,
  getBondLabel,
} from '../utils/discipleFactory';
import { Users, UserPlus, Zap, Bot, HeartHandshake } from 'lucide-react';
import { ModalShell } from './ModalShell';

interface DiscipleRosterProps {
  disciples: Disciple[];
  onAssignTask: (discipleId: string, task: number) => void;
  onAutoAssignAll: () => void;
  onRecruitNew: () => void;
  onOpenTribulation: (disciple: Disciple) => void;
  onSummonFairy: (discipleId: string) => void;
  onForgeBond: (idA: string, idB: string, bondType: DiscipleBondType) => void;
  maxDisciples: number;
}

export const DiscipleRoster: React.FC<DiscipleRosterProps> = ({
  disciples,
  onAssignTask,
  onAutoAssignAll,
  onRecruitNew,
  onOpenTribulation,
  onSummonFairy,
  onForgeBond,
  maxDisciples,
}) => {
  const [selectedDiscipleId, setSelectedDiscipleId] = useState<string | null>(null);
  const [taskPickerDiscipleId, setTaskPickerDiscipleId] = useState<string | null>(null);
  const [bondPartnerId, setBondPartnerId] = useState<string>('');
  const [bondTypeChoice, setBondTypeChoice] = useState<DiscipleBondType>('sect_siblings');

  const discipleById = useMemo(() => {
    const map = new Map<string, Disciple>();
    for (const d of disciples) {
      map.set(d.id, d);
    }
    return map;
  }, [disciples]);

  const activeDetailsDisciple = selectedDiscipleId ? discipleById.get(selectedDiscipleId) ?? null : null;
  const activeTaskPickerDisciple = taskPickerDiscipleId ? discipleById.get(taskPickerDiscipleId) ?? null : null;

  const availableBondPartners = useMemo(() => {
    if (!activeDetailsDisciple) return [];
    const existingIds = new Set((activeDetailsDisciple.bonds ?? []).map(b => b.partnerId));
    return disciples.filter(
      d => d.id !== activeDetailsDisciple.id && !existingIds.has(d.id)
    );
  }, [activeDetailsDisciple, disciples]);

  const resolvedBondPartnerId = useMemo(() => {
    if (availableBondPartners.length === 0) return '';
    if (availableBondPartners.some(p => p.id === bondPartnerId)) {
      return bondPartnerId;
    }
    return availableBondPartners[0].id;
  }, [availableBondPartners, bondPartnerId]);

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3 bg-slate-900/80 border border-slate-800 p-4 rounded-2xl shadow-sm">
        <div>
          <h2 className="font-serif-title font-bold text-slate-100 text-lg flex items-center gap-2">
            <Users className="w-5 h-5 text-amber-400" />
            Sect Disciples (<span className="tabular-nums">{disciples.length} / {maxDisciples}</span>)
          </h2>
          <p className="text-xs text-slate-400">
            Train, bond Sect Siblings &amp; Master-Disciple lineages, and guide realm ascensions
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-2.5">
          <button
            type="button"
            onClick={() => {
              soundManager.playClick();
              onAutoAssignAll();
            }}
            className="flex items-center gap-1.5 px-3.5 min-h-[40px] bg-indigo-600/20 hover:bg-indigo-600/30 border border-indigo-500/30 text-indigo-300 font-bold text-xs rounded-xl whitespace-nowrap transition"
          >
            <Bot className="w-4 h-4" />
            AI Auto-Assign Workforce
          </button>

          <button
            type="button"
            onClick={() => {
              soundManager.playClick();
              onRecruitNew();
            }}
            disabled={disciples.length >= maxDisciples}
            className={`flex items-center gap-1.5 px-4 min-h-[40px] rounded-xl text-xs font-bold whitespace-nowrap transition shadow-md ${
              disciples.length < maxDisciples
                ? 'bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950'
                : 'bg-slate-800 text-slate-500 cursor-not-allowed'
            }`}
          >
            <UserPlus className="w-4 h-4" />
            Recruit Disciple (200 SS)
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {disciples.map(d => {
          const isReadyBreakthrough = d.realmExp >= 1000 && d.realm < REALMS.length - 1;
          const currentTaskName = TASK_NAMES[d.currentTask] ?? 'Idle';
          const taskColor = TASK_COLORS[d.currentTask] ?? '#94a3b8';
          const elementColor = ELEMENT_COLORS[d.element] ?? '#94a3b8';
          const bondBonus = getActiveBondBonusForDisciple(d, disciples);
          const firstBond = (d.bonds ?? [])[0];
          const firstBondPartner = firstBond ? discipleById.get(firstBond.partnerId) : null;

          return (
            <div
              key={d.id}
              className="bg-slate-900/70 border border-slate-800 hover:border-slate-700/80 rounded-2xl p-4 flex flex-col justify-between transition-all shadow-md hover:shadow-slate-950/50"
            >
              <div>
                <div className="flex items-start justify-between gap-2 mb-2.5">
                  <div className="flex items-center gap-3 min-w-0">
                    <div
                      className="w-11 h-11 rounded-xl flex items-center justify-center text-xl font-bold border shrink-0"
                      style={{
                        backgroundColor: `${elementColor}15`,
                        borderColor: `${elementColor}40`,
                      }}
                    >
                      {d.isMale ? '🧑‍🦳' : '👩‍🦳'}
                    </div>
                    <div className="min-w-0">
                      <div className="font-bold text-sm text-slate-100 flex items-center gap-1.5">
                        <span className="truncate">{d.name}</span>
                        {d.fairy && (
                          <span title={`Fairy Companion: ${d.fairy.name}`} className="text-xs">
                            🧚
                          </span>
                        )}
                      </div>
                      <div className="text-[11px] text-slate-400 truncate">
                        {REALMS[d.realm]} • {d.age} yrs • {PERSONALITY_NAMES[d.personality]}
                      </div>
                    </div>
                  </div>

                  <span
                    className="text-[11px] font-mono font-bold shrink-0"
                    style={{ color: elementColor }}
                  >
                    {ELEMENT_NAMES[d.element]}
                  </span>
                </div>

                <div className="bg-slate-950/60 rounded-xl p-2.5 mb-3 border border-slate-800/80 space-y-1.5 text-xs">
                  <div className="flex justify-between gap-2 text-[11px]">
                    <span className="text-slate-400">Talent:</span>
                    <span className="text-amber-300 font-semibold truncate">{d.talentName}</span>
                  </div>

                  <div className="grid grid-cols-4 gap-1 text-[11px] font-mono tabular-nums text-center pt-1 border-t border-slate-800/60">
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

                <div className="grid grid-cols-2 gap-2 mb-3 text-[10px]">
                  <div>
                    <div className="flex justify-between text-slate-400 mb-0.5">
                      <span>Energy</span>
                      <span className="font-mono tabular-nums">{d.energy}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-cyan-400" style={{ width: `${d.energy}%` }} />
                    </div>
                  </div>
                  <div>
                    <div className="flex justify-between text-slate-400 mb-0.5">
                      <span>Mood</span>
                      <span className="font-mono tabular-nums">{d.mood}%</span>
                    </div>
                    <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                      <div className="h-full bg-pink-400" style={{ width: `${d.mood}%` }} />
                    </div>
                  </div>
                </div>

                <div className="space-y-1.5 mb-3">
                  <div className="flex items-center justify-between text-xs bg-slate-950/40 p-2 rounded-lg border border-slate-800/50">
                    <div className="flex items-center gap-1.5 min-w-0">
                      <span className="w-2.5 h-2.5 rounded-full shrink-0" style={{ backgroundColor: taskColor }} />
                      <span className="text-slate-300 font-medium truncate">
                        {currentTaskName}
                        {d.currentTask > 0 && (
                          <span className="ml-1.5 text-[11px] font-mono tabular-nums text-cyan-300">
                            {d.taskEfficiency}%
                          </span>
                        )}
                      </span>
                    </div>
                    <span className="text-[11px] font-mono tabular-nums text-slate-400 shrink-0">
                      {d.dailyWage} SS/d
                    </span>
                  </div>

                  {bondBonus.activePartners.length > 0 ? (
                    <div className="flex items-center gap-1.5 text-[11px] text-emerald-300 bg-emerald-950/30 border border-emerald-500/30 px-2 py-1 rounded-lg">
                      <HeartHandshake className="w-3.5 h-3.5 shrink-0" aria-hidden="true" />
                      <span className="truncate font-medium tabular-nums">
                        {getBondLabel(bondBonus.activePartners[0].type)} w/ {bondBonus.activePartners[0].name} (+{bondBonus.efficiencyBonus}% Eff)
                      </span>
                    </div>
                  ) : firstBond && firstBondPartner ? (
                    <div className="flex items-center gap-1.5 text-[11px] text-slate-400 px-1 truncate">
                      <HeartHandshake className="w-3.5 h-3.5 text-amber-400/80 shrink-0" aria-hidden="true" />
                      <span className="truncate">
                        {getBondLabel(firstBond.type)} · {firstBondPartner.name} (Share task for bonus)
                      </span>
                    </div>
                  ) : null}
                </div>
              </div>

              <div className="space-y-1.5 pt-2 border-t border-slate-800">
                {isReadyBreakthrough ? (
                  <button
                    type="button"
                    onClick={() => {
                      soundManager.playClick();
                      onOpenTribulation(d);
                    }}
                    className="w-full min-h-[40px] py-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs rounded-xl transition flex items-center justify-center gap-1.5 shadow-md"
                  >
                    <Zap className="w-4 h-4" />
                    Enter Lightning Tribulation Breakthrough!
                  </button>
                ) : (
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      onClick={() => {
                        soundManager.playClick();
                        setTaskPickerDiscipleId(d.id);
                      }}
                      className="min-h-[38px] py-1.5 px-3 bg-slate-800 hover:bg-slate-700 text-slate-200 font-bold text-xs rounded-xl transition"
                    >
                      Assign Task
                    </button>
                    <button
                      type="button"
                      onClick={() => {
                        soundManager.playClick();
                        setSelectedDiscipleId(d.id);
                      }}
                      className="min-h-[38px] py-1.5 px-3 bg-slate-800/60 hover:bg-slate-700 text-cyan-300 font-bold text-xs rounded-xl border border-slate-700 transition"
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
      <ModalShell
        isOpen={Boolean(activeTaskPickerDisciple)}
        onClose={() => setTaskPickerDiscipleId(null)}
        maxWidth="md"
        bodyClassName="p-5 space-y-4"
        title={activeTaskPickerDisciple ? `Assign Task to ${activeTaskPickerDisciple.name}` : ''}
      >
        {activeTaskPickerDisciple && (
          <>
            <div className="grid grid-cols-2 gap-2 max-h-72 overflow-y-auto pr-1">
              {TASK_NAMES.map((tName, tIdx) => {
                const eff = calculateEfficiency(activeTaskPickerDisciple, tIdx, disciples);
                const bondPreview = getActiveBondBonusForDisciple(
                  activeTaskPickerDisciple,
                  disciples,
                  tIdx
                );
                const isSelected = activeTaskPickerDisciple.currentTask === tIdx;
                return (
                  <button
                    key={tIdx}
                    type="button"
                    onClick={() => {
                      soundManager.playClick();
                      onAssignTask(activeTaskPickerDisciple.id, tIdx);
                      setTaskPickerDiscipleId(null);
                    }}
                    className={`p-2.5 text-left rounded-xl border transition ${
                      isSelected
                        ? 'bg-amber-500/10 border-amber-500 text-amber-300'
                        : 'bg-slate-800/40 border-slate-700 hover:bg-slate-800 text-slate-300'
                    }`}
                  >
                    <div className="font-bold text-xs flex items-center gap-1.5">
                      <span className="w-2 h-2 rounded-full" style={{ backgroundColor: TASK_COLORS[tIdx] }} />
                      {tName}
                    </div>
                    {tIdx > 0 && (
                      <div className="text-[10px] text-slate-400 mt-1 tabular-nums">
                        Efficiency: {eff}%
                        {bondPreview.efficiencyBonus > 0 && (
                          <span className="text-emerald-300 ml-1 font-semibold">
                            (+{bondPreview.efficiencyBonus}% Bond)
                          </span>
                        )}
                      </div>
                    )}
                  </button>
                );
              })}
            </div>

            <div className="p-2.5 bg-slate-950 border border-slate-800 rounded-xl text-xs text-indigo-300 flex items-center justify-between gap-2">
              <span>
                AI Optimal: <strong>{TASK_NAMES[getOptimalTaskForDisciple(activeTaskPickerDisciple)]}</strong>
              </span>
              <button
                type="button"
                onClick={() => {
                  soundManager.playClick();
                  const opt = getOptimalTaskForDisciple(activeTaskPickerDisciple);
                  onAssignTask(activeTaskPickerDisciple.id, opt);
                  setTaskPickerDiscipleId(null);
                }}
                className="px-2.5 py-1 bg-indigo-600 hover:bg-indigo-500 text-white font-bold rounded-lg text-[11px]"
              >
                Apply Optimal
              </button>
            </div>
          </>
        )}
      </ModalShell>

      {/* Disciple Full Details Modal */}
      <ModalShell
        isOpen={Boolean(activeDetailsDisciple)}
        onClose={() => setSelectedDiscipleId(null)}
        maxWidth="lg"
        borderClassName="border-cyan-500/30"
        bodyClassName="p-6 space-y-4 overflow-y-auto max-h-[82vh]"
        icon={
          activeDetailsDisciple && (
            <span className="text-3xl">{activeDetailsDisciple.isMale ? '🧑‍🦳' : '👩‍🦳'}</span>
          )
        }
        title={activeDetailsDisciple?.name ?? ''}
        subtitle={
          activeDetailsDisciple
            ? `${REALMS[activeDetailsDisciple.realm]} • ${ELEMENT_NAMES[activeDetailsDisciple.element]} Element`
            : undefined
        }
      >
        {activeDetailsDisciple && (
          <>
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5">
              <h4 className="text-xs font-bold text-amber-400 uppercase tracking-wider mb-2">
                Combat Potency &amp; Attributes
              </h4>
              <div className="grid grid-cols-3 gap-2 text-xs font-mono tabular-nums">
                <div>HP: <strong className="text-emerald-400">{activeDetailsDisciple.maxHp}</strong></div>
                <div>MP: <strong className="text-cyan-400">{activeDetailsDisciple.maxMp}</strong></div>
                <div>ATK: <strong className="text-red-400">{activeDetailsDisciple.atk}</strong></div>
                <div>DEF: <strong className="text-blue-400">{activeDetailsDisciple.def}</strong></div>
                <div>SPD: <strong className="text-amber-400">{activeDetailsDisciple.spd}</strong></div>
                <div>CRIT: <strong className="text-purple-400">{activeDetailsDisciple.critRate}%</strong></div>
              </div>
            </div>

            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5">
              <h4 className="text-xs font-bold text-cyan-400 uppercase tracking-wider mb-2">
                Seven Pillars of Dao Root
              </h4>
              <div className="grid grid-cols-4 gap-2 text-center text-xs font-mono tabular-nums">
                {(
                  [
                    ['STR', activeDetailsDisciple.stats.str],
                    ['AGI', activeDetailsDisciple.stats.agi],
                    ['INT', activeDetailsDisciple.stats.intel],
                    ['LCK', activeDetailsDisciple.stats.lck],
                    ['VIT', activeDetailsDisciple.stats.vit],
                    ['WIS', activeDetailsDisciple.stats.wis],
                    ['CHA', activeDetailsDisciple.stats.cha],
                  ] as const
                ).map(([label, val]) => (
                  <div key={label} className="p-1.5 bg-slate-900 rounded-lg">
                    <span className="text-[10px] text-slate-500 block">{label}</span>
                    <span className="font-bold text-slate-200">{val}</span>
                  </div>
                ))}
              </div>
            </div>

            {/* Sect Bonds & Lineage Section */}
            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5 space-y-3">
              <div className="flex items-center justify-between gap-2">
                <h4 className="text-xs font-bold text-emerald-400 uppercase tracking-wider flex items-center gap-1.5">
                  <HeartHandshake className="w-4 h-4" aria-hidden="true" />
                  Sect Bonds &amp; Synergy
                </h4>
                <span className="text-[11px] font-mono tabular-nums text-slate-400">
                  {(activeDetailsDisciple.bonds ?? []).length} Active Connections
                </span>
              </div>

              {(activeDetailsDisciple.bonds ?? []).length > 0 ? (
                <div className="space-y-2">
                  {(activeDetailsDisciple.bonds ?? []).map(bond => {
                    const partner = discipleById.get(bond.partnerId);
                    if (!partner) return null;
                    const coWorking =
                      activeDetailsDisciple.currentTask > 0 &&
                      partner.currentTask === activeDetailsDisciple.currentTask;
                    const bonusVal =
                      bond.type === 'master_disciple'
                        ? 8 + (bond.level - 1) * 2
                        : 6 + (bond.level - 1) * 2;
                    return (
                      <div
                        key={bond.partnerId}
                        className={`p-2.5 rounded-lg border text-xs flex flex-wrap items-center justify-between gap-2 ${
                          coWorking
                            ? 'bg-emerald-950/30 border-emerald-500/40'
                            : 'bg-slate-900/80 border-slate-800'
                        }`}
                      >
                        <div>
                          <div className="font-bold text-slate-100">
                            {partner.name}{' '}
                            <span className="text-amber-300 font-normal">
                              ({getBondLabel(bond.type)} · Lv.{bond.level})
                            </span>
                          </div>
                          <div className="text-[11px] text-slate-400 tabular-nums">
                            Task: {TASK_NAMES[partner.currentTask] ?? 'Idle'} • Affinity: {bond.affinity}/100
                          </div>
                        </div>

                        <span
                          className={`text-[11px] font-mono font-bold tabular-nums ${
                            coWorking ? 'text-emerald-300' : 'text-slate-400'
                          }`}
                        >
                          {coWorking ? `+${bonusVal}% Eff Active` : `+${bonusVal}% if co-working`}
                        </span>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <p className="text-xs text-slate-400">
                  No bonds formed yet. Assign disciples to the same task or forge a bond below.
                </p>
              )}

              {availableBondPartners.length > 0 && (
                <div className="pt-2 border-t border-slate-800/80 flex flex-col sm:flex-row gap-2">
                  <select
                    aria-label="Select disciple to bond with"
                    value={resolvedBondPartnerId}
                    onChange={e => setBondPartnerId(e.target.value)}
                    className="flex-1 bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-2 text-xs text-slate-100"
                  >
                    {availableBondPartners.map(partner => (
                      <option key={partner.id} value={partner.id}>
                        {partner.name} ({REALMS[partner.realm]})
                      </option>
                    ))}
                  </select>

                  <select
                    aria-label="Select bond relationship type"
                    value={bondTypeChoice}
                    onChange={e => setBondTypeChoice(e.target.value as DiscipleBondType)}
                    className="bg-slate-900 border border-slate-700 rounded-lg px-2.5 py-2 text-xs text-slate-100"
                  >
                    <option value="sect_siblings">Sect Siblings (+6% Eff)</option>
                    <option value="master_disciple">Master-Disciple (+8% Eff)</option>
                  </select>

                  <button
                    type="button"
                    onClick={() => {
                      if (!resolvedBondPartnerId) return;
                      soundManager.playBreakthrough();
                      onForgeBond(activeDetailsDisciple.id, resolvedBondPartnerId, bondTypeChoice);
                    }}
                    className="px-3.5 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-lg whitespace-nowrap transition"
                  >
                    Forge Bond
                  </button>
                </div>
              )}
            </div>

            <div className="bg-slate-950 border border-slate-800 rounded-xl p-3.5">
              <div className="flex items-center justify-between mb-2">
                <h4 className="text-xs font-bold text-pink-400 uppercase tracking-wider flex items-center gap-1.5">
                  <span>🧚 Spiritual Fairy Companion</span>
                </h4>
                {!activeDetailsDisciple.fairy && (
                  <button
                    type="button"
                    onClick={() => {
                      soundManager.playBreakthrough();
                      onSummonFairy(activeDetailsDisciple.id);
                    }}
                    className="px-2.5 py-1 bg-pink-600/20 hover:bg-pink-600/30 border border-pink-500/40 text-pink-300 rounded-lg text-xs font-bold transition"
                  >
                    Summon Fairy (20 Essence)
                  </button>
                )}
              </div>
              {activeDetailsDisciple.fairy ? (
                <div className="text-xs text-slate-300 space-y-1">
                  <div>
                    Name: <strong className="text-pink-300">{activeDetailsDisciple.fairy.name}</strong> (
                    {activeDetailsDisciple.fairy.element})
                  </div>
                  <div className="text-slate-400 text-[11px] tabular-nums">
                    Grants +{activeDetailsDisciple.fairy.efficiencyBonus}% Work Efficiency &amp; +
                    {activeDetailsDisciple.fairy.cultivationBonus}% Qi Speed
                  </div>
                </div>
              ) : (
                <p className="text-xs text-slate-400">
                  No spiritual fairy has bonded with this disciple yet.
                </p>
              )}
            </div>

            <button
              type="button"
              onClick={() => setSelectedDiscipleId(null)}
              className="w-full min-h-[40px] py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold rounded-xl text-xs transition"
            >
              Close Record
            </button>
          </>
        )}
      </ModalShell>
    </div>
  );
};
