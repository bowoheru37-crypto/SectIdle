import React, { useState, useEffect, useRef, useMemo, useCallback } from 'react';
import { Building, Disciple, DiscipleBondType, MarketItem, SectEventLog, TechNode } from './types/game';
import {
  REALMS,
  SEASONS,
  createInitialBuildings,
} from './constants/gameData';
import { soundManager } from './utils/audio';
import {
  PerformanceMode,
  PersistedSectState,
  STORAGE_KEY,
  advanceDailyMissions,
  applyOfflineHours,
  calculateDiscipleEfficiency,
  calculateTotalPower,
  claimDailyMissionReward,
  createDefaultState,
  createDisciple,
  createStarterDisciples,
  detectDevicePerformanceTier,
  forgeDiscipleBond,
  formatDayKey,
  formatEventLog,
  generateDailyMissions,
  getBondLabel,
  getOptimalTaskForDisciple,
  loadPersistedSectState,
  savePersistedSectState,
  simulateDailyTick,
  stepSectHour,
  syncDailyMissionsForNewDay,
  triggerCelebration,
} from './core/sectEngine';

import { SectOverview } from './components/SectOverview';
import { DiscipleRoster } from './components/DiscipleRoster';
import { CultivationChamber } from './components/CultivationChamber';
import { BattleArena } from './components/BattleArena';
import { SectWarCampaign } from './components/SectWarCampaign';
import { GrandTournament } from './components/GrandTournament';
import { AlchemyForge } from './components/AlchemyForge';
import { Marketplace } from './components/Marketplace';
import { TechTree } from './components/TechTree';
import { TribulationGame } from './components/TribulationGame';
import { SlashMiniGameModal } from './components/SlashMiniGameModal';
import { AscensionModal } from './components/AscensionModal';
import { ChimeraStudioModal } from './components/ChimeraStudioModal';
import { ModalShell } from './components/ModalShell';

import {
  Castle,
  Users,
  Sparkles,
  Swords,
  Flame,
  Trophy,
  ShoppingBag,
  BookOpen,
  Volume2,
  VolumeX,
  Music,
  RotateCcw,
  Award,
  Gamepad2,
  Calendar,
  Gauge,
} from 'lucide-react';

const PERF_MODES: PerformanceMode[] = ['auto', 'eco', 'balanced', 'ultra'];

function initAppState(): { state: PersistedSectState; offlineReport: string | null } {
  const loaded = loadPersistedSectState();
  const elapsedMs = Math.max(0, Date.now() - loaded.lastSavedAt);
  const { state, hoursAdvanced, daysAdvanced } = applyOfflineHours(loaded, elapsedMs, 1500);
  const offlineReport =
    hoursAdvanced >= 4
      ? `Resumed cultivation after ${hoursAdvanced} sect hours (${daysAdvanced} days). Sect production synchronized offline.`
      : null;
  return { state, offlineReport };
}

export const App: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'overview' | 'disciples' | 'cultivation' | 'tech'>('overview');

  const initialBoot = useMemo(() => initAppState(), []);
  const [gameState, setGameState] = useState<PersistedSectState>(initialBoot.state);

  const {
    sectName,
    resources,
    time,
    disciples,
    buildings,
    techTree,
    marketListings,
    dailyMissions,
    dailyMissionDayKey,
    ascensionStats,
    perfMode,
    audioEnabled,
    bgmEnabled,
  } = gameState;

  const perfTier = useMemo(() => detectDevicePerformanceTier(perfMode), [perfMode]);

  // Modals
  const [isBattleOpen, setIsBattleOpen] = useState<boolean>(false);
  const [isWarOpen, setIsWarOpen] = useState<boolean>(false);
  const [isTournamentOpen, setIsTournamentOpen] = useState<boolean>(false);
  const [isAlchemyOpen, setIsAlchemyOpen] = useState<boolean>(false);
  const [isMarketOpen, setIsMarketOpen] = useState<boolean>(false);
  const [isAscensionOpen, setIsAscensionOpen] = useState<boolean>(false);
  const [isStudioOpen, setIsStudioOpen] = useState<boolean>(false);
  const [isResetConfirmOpen, setIsResetConfirmOpen] = useState<boolean>(false);
  const [slashMiniGameConfig, setSlashMiniGameConfig] = useState<{
    isOpen: boolean;
    type: 'garden' | 'alchemy';
  }>({ isOpen: false, type: 'garden' });
  const [tribulationDisciple, setTribulationDisciple] = useState<Disciple | null>(null);

  // Event Logs
  const [eventLogs, setEventLogs] = useState<SectEventLog[]>(() => {
    const base: SectEventLog[] = [
      {
        id: 'log_0',
        timestamp: `Y${initialBoot.state.time.year} M${initialBoot.state.time.month} D${initialBoot.state.time.day}`,
        message: 'Cloud Mist Sect sanctuary established. May your Dao lineage endure through eternity.',
        type: 'info',
      },
    ];
    if (initialBoot.offlineReport) {
      base.unshift({
        id: 'log_offline',
        timestamp: `Y${initialBoot.state.time.year} M${initialBoot.state.time.month} D${initialBoot.state.time.day}`,
        message: initialBoot.offlineReport,
        type: 'reward',
      });
    }
    return base;
  });

  const isDirtyRef = useRef<boolean>(false);
  const stateRef = useRef<PersistedSectState>(gameState);
  const hiddenSinceRef = useRef<number | null>(null);
  const lastEncounterDayKeyRef = useRef<string>('');

  stateRef.current = gameState;

  const updateGameState = useCallback(
    (updater: (prev: PersistedSectState) => PersistedSectState) => {
      isDirtyRef.current = true;
      setGameState(updater);
    },
    []
  );

  const addEvent = useCallback(
    (msg: string, type: SectEventLog['type'] = 'info') => {
      const currentTime = stateRef.current.time;
      const newLog = formatEventLog(msg, currentTime, type);
      setEventLogs(prev => [newLog, ...prev.slice(0, 30)]);
    },
    []
  );

  // Apply performance tier to <html data-perf="eco|balanced|ultra">
  useEffect(() => {
    if (typeof document !== 'undefined') {
      document.documentElement.dataset.perf = perfTier;
    }
  }, [perfTier]);

  // Sync audio preference into soundManager
  useEffect(() => {
    soundManager.setSoundEnabled(audioEnabled);
  }, [audioEnabled]);

  const handleToggleSound = () => {
    const next = !audioEnabled;
    soundManager.setSoundEnabled(next);
    updateGameState(prev => ({ ...prev, audioEnabled: next }));
  };

  const handleToggleBgm = () => {
    const next = !bgmEnabled;
    soundManager.toggleBgm(next);
    updateGameState(prev => ({ ...prev, bgmEnabled: next }));
  };

  const handleCyclePerfMode = () => {
    soundManager.playClick();
    const idx = PERF_MODES.indexOf(perfMode);
    const nextMode = PERF_MODES[(idx + 1) % PERF_MODES.length];
    updateGameState(prev => ({ ...prev, perfMode: nextMode }));
    addEvent(`Resource profile set to ${nextMode.toUpperCase()} (${detectDevicePerformanceTier(nextMode)} tier).`, 'info');
  };

  // Visibility-aware primary game loop (paused in background, catches up cleanly on return)
  useEffect(() => {
    const intervalMs = perfTier === 'eco' ? 2400 : 1500;
    const loop = setInterval(() => {
      if (typeof document !== 'undefined' && document.hidden) return;

      let pendingEncounter: { msg: string; timeKey: string; timeSnap: { year: number; month: number; day: number } } | null = null;
      let pendingBondEvent: { msg: string; timeSnap: { year: number; month: number; day: number } } | null = null;

      isDirtyRef.current = true;
      setGameState(prev => {
        const { nextTime, dayPassed } = stepSectHour(prev.time);
        if (!dayPassed) {
          return { ...prev, time: nextTime };
        }

        const daily = simulateDailyTick({
          resources: prev.resources,
          disciples: prev.disciples,
          buildings: prev.buildings,
          marketListings: prev.marketListings,
          ascensionStats: prev.ascensionStats,
        });

        const dayKey = formatDayKey(nextTime);
        const progressedMissions = advanceDailyMissions(prev.dailyMissions, 'work_ticks', 1);
        const syncedMissions = syncDailyMissionsForNewDay(
          progressedMissions,
          prev.dailyMissionDayKey,
          dayKey
        );

        if (daily.randomEncounter) {
          pendingEncounter = {
            msg: daily.randomEncounter,
            timeKey: `${nextTime.year}-${nextTime.month}-${nextTime.day}`,
            timeSnap: { year: nextTime.year, month: nextTime.month, day: nextTime.day },
          };
        }
        if (daily.bondEvent) {
          pendingBondEvent = {
            msg: daily.bondEvent,
            timeSnap: { year: nextTime.year, month: nextTime.month, day: nextTime.day },
          };
        }

        return {
          ...prev,
          time: nextTime,
          resources: daily.resources,
          disciples: daily.disciples,
          marketListings: daily.marketListings,
          dailyMissions: syncedMissions.missions,
          dailyMissionDayKey: syncedMissions.dayKey,
        };
      });

      if (pendingEncounter) {
        const enc: { msg: string; timeKey: string; timeSnap: { year: number; month: number; day: number } } = pendingEncounter;
        if (lastEncounterDayKeyRef.current !== enc.timeKey) {
          lastEncounterDayKeyRef.current = enc.timeKey;
          setEventLogs(prev => [formatEventLog(enc.msg, enc.timeSnap, 'info'), ...prev.slice(0, 30)]);
        }
      }
      if (pendingBondEvent) {
        const bEvt: { msg: string; timeSnap: { year: number; month: number; day: number } } = pendingBondEvent;
        setEventLogs(prev => [formatEventLog(bEvt.msg, bEvt.timeSnap, 'reward'), ...prev.slice(0, 30)]);
      }
    }, intervalMs);

    return () => clearInterval(loop);
  }, [perfTier]);

  // Dirty-flag autosave + visibility pause/resume catch-up & beforeunload flush
  useEffect(() => {
    const saver = setInterval(() => {
      if (!isDirtyRef.current) return;
      savePersistedSectState(stateRef.current);
      isDirtyRef.current = false;
    }, 15000);

    const onVisibilityChange = () => {
      if (document.hidden) {
        hiddenSinceRef.current = Date.now();
        savePersistedSectState(stateRef.current);
        isDirtyRef.current = false;
      } else if (hiddenSinceRef.current !== null) {
        const elapsed = Date.now() - hiddenSinceRef.current;
        hiddenSinceRef.current = null;
        if (elapsed >= 3000) {
          const caught = applyOfflineHours(stateRef.current, elapsed, 1500);
          if (caught.hoursAdvanced > 0) {
            isDirtyRef.current = true;
            setGameState(caught.state);
          }
        }
      }
    };

    const onBeforeUnload = () => {
      savePersistedSectState(stateRef.current);
    };

    document.addEventListener('visibilitychange', onVisibilityChange);
    window.addEventListener('beforeunload', onBeforeUnload);

    return () => {
      clearInterval(saver);
      document.removeEventListener('visibilitychange', onVisibilityChange);
      window.removeEventListener('beforeunload', onBeforeUnload);
    };
  }, []);

  // Building handlers
  const handleUpgradeBuilding = (bId: number) => {
    const target = buildings.find(b => b.id === bId);
    if (!target || resources.spiritStones < target.upgradeCost) return;

    updateGameState(prev => ({
      ...prev,
      resources: {
        ...prev.resources,
        spiritStones: prev.resources.spiritStones - target.upgradeCost,
      },
      buildings: prev.buildings.map(b =>
        b.id === bId
          ? { ...b, level: b.level + 1, upgradeCost: Math.round(b.upgradeCost * 1.55) }
          : b
      ),
      dailyMissions: advanceDailyMissions(prev.dailyMissions, 'build_or_upgrade', 1),
    }));
    addEvent(`Upgraded ${target.name} to Level ${target.level + 1}! Daily revenue increased.`, 'reward');
  };

  const handleBuildBuilding = (bId: number) => {
    const target = buildings.find(b => b.id === bId);
    if (!target || target.isBuilt || resources.spiritStones < target.cost) return;

    updateGameState(prev => ({
      ...prev,
      resources: {
        ...prev.resources,
        spiritStones: prev.resources.spiritStones - target.cost,
      },
      buildings: prev.buildings.map(b =>
        b.id === bId ? { ...b, isBuilt: true, level: 1 } : b
      ),
      dailyMissions: advanceDailyMissions(prev.dailyMissions, 'build_or_upgrade', 1),
    }));
    addEvent(`Erected ${target.name}! Sect infrastructure expanded.`, 'reward');
  };

  const handleBuildingAction = (b: Building) => {
    if (b.name.includes('Garden')) {
      setSlashMiniGameConfig({ isOpen: true, type: 'garden' });
    } else if (b.name.includes('Alchemy')) {
      setIsAlchemyOpen(true);
    }
  };

  // Daily Missions handlers
  const handleClaimDailyMission = (missionId: string) => {
    const currentSnapshot = stateRef.current;
    const { claimed } = claimDailyMissionReward(currentSnapshot, missionId);
    if (!claimed) return;

    updateGameState(prev => claimDailyMissionReward(prev, missionId).nextState);
    triggerCelebration(perfTier, { particleCount: 45, spread: 50 });
    const rewardLabel =
      claimed.rewardType === 'spiritStones'
        ? `+${claimed.rewardAmount} Spirit Stones`
        : `+${claimed.rewardAmount} Sect EXP`;
    addEvent(`Daily Mission "${claimed.title}" claimed (${rewardLabel})!`, 'reward');
  };

  const handleRerollDailyMissions = () => {
    if (resources.spiritStones < 50) {
      addEvent('Insufficient Spirit Stones (50 SS needed to draw a fresh Daily Scroll).', 'warning');
      return;
    }
    updateGameState(prev => {
      const freshPool = generateDailyMissions(formatDayKey(prev.time));
      const usedKinds = new Set(
        prev.dailyMissions.filter(m => m.claimed).map(m => m.kind)
      );
      const nextMissions = prev.dailyMissions.map((m, idx) => {
        if (m.claimed) return m;
        const candidate = freshPool.find(f => !usedKinds.has(f.kind)) ?? freshPool[idx % freshPool.length];
        usedKinds.add(candidate.kind);
        return candidate;
      });
      return {
        ...prev,
        resources: { ...prev.resources, spiritStones: prev.resources.spiritStones - 50 },
        dailyMissions: nextMissions,
      };
    });
    addEvent('Drew a fresh Daily Mission Scroll with new randomized rewards.', 'info');
  };

  // Disciple management
  const handleAssignTask = (discipleId: string, task: number) => {
    updateGameState(prev => {
      const reassigned = prev.disciples.map(d =>
        d.id === discipleId ? { ...d, currentTask: task } : d
      );
      return {
        ...prev,
        disciples: reassigned.map(d => ({
          ...d,
          taskEfficiency: calculateDiscipleEfficiency(d, d.currentTask, reassigned),
        })),
      };
    });
  };

  const handleAutoAssignAll = () => {
    soundManager.playBreakthrough();
    updateGameState(prev => {
      const optimalAssigned = prev.disciples.map(d => ({
        ...d,
        currentTask: getOptimalTaskForDisciple(d),
      }));
      return {
        ...prev,
        disciples: optimalAssigned.map(d => ({
          ...d,
          taskEfficiency: calculateDiscipleEfficiency(d, d.currentTask, optimalAssigned),
        })),
      };
    });
    addEvent('Grandmaster AI optimized the sect workforce based on disciple talents.', 'info');
  };

  const handleForgeBond = (idA: string, idB: string, bondType: DiscipleBondType) => {
    const discA = disciples.find(d => d.id === idA);
    const discB = disciples.find(d => d.id === idB);
    if (!discA || !discB) return;

    updateGameState(prev => ({
      ...prev,
      disciples: forgeDiscipleBond(prev.disciples, idA, idB, bondType).disciples,
    }));
    addEvent(
      `🤝 ${discA.name} and ${discB.name} formed a ${getBondLabel(bondType)} bond! Shared tasks now grant synergy bonuses.`,
      'reward'
    );
  };

  const handleRecruitNew = () => {
    if (resources.spiritStones < 200) {
      addEvent('Insufficient spirit stones (200 SS needed to recruit).', 'warning');
      return;
    }
    if (disciples.length >= resources.maxDisciples) {
      addEvent('Sect quarters are at full disciple capacity.', 'warning');
      return;
    }
    const candidate = createDisciple();
    updateGameState(prev => {
      const nextDisciples = [...prev.disciples, candidate];
      return {
        ...prev,
        disciples: nextDisciples,
        resources: {
          ...prev.resources,
          spiritStones: prev.resources.spiritStones - 200,
          sectPower: calculateTotalPower(nextDisciples, prev.ascensionStats.combatMultiplier),
        },
      };
    });
    soundManager.playBreakthrough();
    triggerCelebration(perfTier, { particleCount: 60, spread: 50 });
    addEvent(`Welcomed gifted disciple ${candidate.name} (${candidate.talentName}) into the sect!`, 'reward');
  };

  const handleBestowQi = (discipleId: string, amount = 150) => {
    updateGameState(prev => ({
      ...prev,
      disciples: prev.disciples.map(d =>
        d.id === discipleId ? { ...d, realmExp: Math.min(2000, d.realmExp + amount) } : d
      ),
      dailyMissions: advanceDailyMissions(prev.dailyMissions, 'cultivate_qi', 1),
    }));
  };

  const handleSummonFairy = (discipleId: string) => {
    if (resources.essence < 20) {
      addEvent('Insufficient spiritual essence (20 Essence needed).', 'warning');
      return;
    }
    const fairyNames = ['Snow Sprite', 'Jade Butterfly', 'Flame Pixie', 'Lotus Spirit'];
    const fName = fairyNames[Math.floor(Math.random() * fairyNames.length)];

    updateGameState(prev => {
      const nextList = prev.disciples.map(d => {
        if (d.id !== discipleId) return d;
        return {
          ...d,
          fairy: {
            name: fName,
            element: 'Spirit',
            level: 1,
            exp: 0,
            bond: 20,
            maxBond: 100,
            efficiencyBonus: 15,
            cultivationBonus: 10,
            combatBonus: 12,
            luckBonus: 5,
          },
        };
      });
      return {
        ...prev,
        resources: { ...prev.resources, essence: prev.resources.essence - 20 },
        disciples: nextList.map(d =>
          d.id === discipleId
            ? { ...d, taskEfficiency: calculateDiscipleEfficiency(d, d.currentTask, nextList) }
            : d
        ),
      };
    });
    addEvent(`Spiritual companion ${fName} bonded with disciple!`, 'reward');
  };

  const handleBuyMarketItem = (item: MarketItem) => {
    if (resources.spiritStones < item.price || item.stock <= 0) return;
    updateGameState(prev => ({
      ...prev,
      resources: { ...prev.resources, spiritStones: prev.resources.spiritStones - item.price },
      marketListings: prev.marketListings.map(it =>
        it.id === item.id ? { ...it, stock: it.stock - 1 } : it
      ),
    }));
    addEvent(`Purchased ${item.itemName} for ${item.price} Spirit Stones.`, 'info');
  };

  const handleSellResource = (type: 'herbs' | 'ores', amount: number, earnings: number) => {
    updateGameState(prev => ({
      ...prev,
      resources: {
        ...prev.resources,
        spiritHerbs: type === 'herbs' ? Math.max(0, prev.resources.spiritHerbs - amount) : prev.resources.spiritHerbs,
        spiritOres: type === 'ores' ? Math.max(0, prev.resources.spiritOres - amount) : prev.resources.spiritOres,
        spiritStones: prev.resources.spiritStones + earnings,
      },
    }));
    addEvent(`Sold surplus commodities for +${earnings} Spirit Stones.`, 'reward');
  };

  const handleResearchTech = (tech: TechNode) => {
    if (resources.sectExp < tech.costExp || tech.isResearched) return;
    updateGameState(prev => ({
      ...prev,
      resources: { ...prev.resources, sectExp: prev.resources.sectExp - tech.costExp },
      techTree: prev.techTree.map(t => (t.id === tech.id ? { ...t, isResearched: true } : t)),
    }));
    addEvent(`Mastered Dao Enlightenment: ${tech.name}! Permanent sect buffs activated.`, 'reward');
  };

  const handleTribulationSuccess = () => {
    if (!tribulationDisciple) return;
    const targetName = tribulationDisciple.name;
    const targetNextRealm = Math.min(REALMS.length - 1, tribulationDisciple.realm + 1);

    updateGameState(prev => {
      const nextDisciples = prev.disciples.map(d => {
        if (d.id !== tribulationDisciple.id) return d;
        const nextMaxHp = d.maxHp + 90;
        return {
          ...d,
          realm: Math.min(REALMS.length - 1, d.realm + 1),
          realmExp: 0,
          atk: d.atk + 25,
          def: d.def + 18,
          maxHp: nextMaxHp,
          hp: nextMaxHp,
        };
      });
      return {
        ...prev,
        disciples: nextDisciples,
        resources: {
          ...prev.resources,
          sectPower: calculateTotalPower(nextDisciples, prev.ascensionStats.combatMultiplier),
        },
      };
    });
    addEvent(`⚡ ${targetName} survived Heavenly Tribulation and advanced to ${REALMS[targetNextRealm]}!`, 'breakthrough');
    setTribulationDisciple(null);
  };

  const handleSectRealmBreakthrough = () => {
    const nextRealmIdx = Math.min(REALMS.length - 1, resources.sectRealm + 1);
    updateGameState(prev => ({
      ...prev,
      resources: {
        ...prev.resources,
        sectRealm: Math.min(REALMS.length - 1, prev.resources.sectRealm + 1),
        sectRealmExp: 0,
        jade: prev.resources.jade + 50,
      },
    }));
    addEvent(`🌟 The entire Sect ascended to ${REALMS[nextRealmIdx]} realm! Received +50 Celestial Jade!`, 'breakthrough');
  };

  const handleConfirmAscension = (earnedEssence: number) => {
    updateGameState(prev => {
      const nextCombatMult = prev.ascensionStats.combatMultiplier + earnedEssence * 0.03;
      const starterDisciples = createStarterDisciples();
      const dayKey = formatDayKey(prev.time);
      return {
        ...prev,
        ascensionStats: {
          totalAscensions: prev.ascensionStats.totalAscensions + 1,
          ancestralEssence: prev.ascensionStats.ancestralEssence + earnedEssence,
          incomeMultiplier: prev.ascensionStats.incomeMultiplier + earnedEssence * 0.05,
          combatMultiplier: nextCombatMult,
        },
        resources: {
          spiritStones: 1200,
          spiritHerbs: 150,
          spiritOres: 80,
          jade: prev.resources.jade,
          essence: 30 + earnedEssence,
          sectPower: calculateTotalPower(starterDisciples, nextCombatMult),
          sectExp: 300,
          sectRealm: 0,
          sectRealmExp: 0,
          sectRank: 1,
          maxDisciples: 12,
        },
        buildings: createInitialBuildings(),
        disciples: starterDisciples,
        dailyMissions: generateDailyMissions(dayKey),
        dailyMissionDayKey: dayKey,
      };
    });

    setIsAscensionOpen(false);
    addEvent(`Ascended into Immortal Cycle! Gained +${earnedEssence} Ancestral Essence.`, 'breakthrough');
  };

  const confirmResetGame = () => {
    if (typeof window !== 'undefined' && typeof window.localStorage !== 'undefined') {
      window.localStorage.removeItem(STORAGE_KEY);
    }
    const fresh = createDefaultState();
    isDirtyRef.current = false;
    setGameState(fresh);
    setEventLogs([
      {
        id: 'log_reset',
        timestamp: 'Y1 M1 D1',
        message: 'Fresh cultivation lineage founded at Cloud Mist Mountain.',
        type: 'info',
      },
    ]);
    setIsResetConfirmOpen(false);
  };

  return (
    <div className="min-h-screen bg-[#05060b] text-slate-100 flex flex-col">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-40 bg-slate-950/90 border-b border-slate-800 backdrop-blur-md px-4 sm:px-6 py-2.5">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
          {/* Logo & Sect Title */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-600 via-amber-400 to-yellow-300 flex items-center justify-center text-slate-950 font-bold shadow-md shadow-amber-500/20" aria-hidden="true">
              ⚡
            </div>
            <div>
              <h1 className="font-serif-title font-bold text-sm sm:text-base text-amber-300 tracking-wide">
                {sectName}
              </h1>
              <div className="flex items-center gap-2 text-[11px] text-slate-400 font-mono tabular-nums">
                <span>{REALMS[resources.sectRealm]}</span>
                <span>•</span>
                <span>Rank #{resources.sectRank}</span>
                <span>•</span>
                <span className="text-amber-400">Power: {resources.sectPower}</span>
              </div>
            </div>
          </div>

          {/* Core Resources Ticker */}
          <div className="flex flex-wrap items-center gap-2 sm:gap-3 text-xs font-mono tabular-nums">
            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span aria-hidden="true">💎</span>
              <span className="font-bold text-amber-400">{resources.spiritStones}</span>
              <span className="text-slate-400 text-[10px]">SS</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span aria-hidden="true">✨</span>
              <span className="font-bold text-cyan-400">{resources.jade}</span>
              <span className="text-slate-400 text-[10px]">Jade</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span aria-hidden="true">🌿</span>
              <span className="font-bold text-emerald-400">{resources.spiritHerbs}</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span aria-hidden="true">🪨</span>
              <span className="font-bold text-slate-300">{resources.spiritOres}</span>
            </div>

            <div className="hidden sm:flex items-center gap-1.5 bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl text-slate-400 text-[11px]">
              <Calendar className="w-3.5 h-3.5 text-indigo-400" />
              <span>Y{time.year} M{time.month} D{time.day} ({SEASONS[time.season]})</span>
            </div>
          </div>

          {/* Quick Controls */}
          <div className="flex items-center gap-1.5">
            <button
              type="button"
              onClick={handleCyclePerfMode}
              title={`Performance Mode: ${perfMode} (${perfTier})`}
              aria-label={`Performance Mode ${perfMode}`}
              className={`px-2 py-1.5 rounded-lg border text-[11px] font-mono font-bold flex items-center gap-1 transition ${
                perfTier === 'eco'
                  ? 'bg-emerald-500/15 border-emerald-500/40 text-emerald-300'
                  : 'bg-slate-800/80 border-slate-700 text-slate-300 hover:text-white'
              }`}
            >
              <Gauge className="w-3.5 h-3.5" />
              <span className="uppercase">{perfMode === 'auto' ? perfTier : perfMode}</span>
            </button>

            <button
              type="button"
              onClick={handleToggleSound}
              title={audioEnabled ? 'Mute SFX' : 'Enable SFX'}
              aria-label={audioEnabled ? 'Mute sound effects' : 'Enable sound effects'}
              className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
            >
              {audioEnabled ? <Volume2 className="w-4 h-4 text-cyan-400" /> : <VolumeX className="w-4 h-4" />}
            </button>

            <button
              type="button"
              onClick={handleToggleBgm}
              title={bgmEnabled ? 'Disable Guzheng BGM' : 'Play Guzheng BGM'}
              aria-label={bgmEnabled ? 'Disable background music' : 'Play background music'}
              className={`p-2 rounded-lg transition ${
                bgmEnabled ? 'text-amber-400 bg-amber-500/10' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              <Music className="w-4 h-4" />
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsStudioOpen(true);
              }}
              title="Chimera Console Exporter"
              className="px-2.5 py-1.5 bg-slate-800 hover:bg-slate-700 text-cyan-300 font-bold text-xs rounded-xl border border-slate-700 flex items-center gap-1.5 transition"
            >
              <Gamepad2 className="w-3.5 h-3.5" />
              <span className="hidden md:inline">Chimera Studio</span>
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsAscensionOpen(true);
              }}
              title="Sect Ascension Prestige"
              className="px-2.5 py-1.5 bg-amber-500/15 hover:bg-amber-500/25 border border-amber-500/30 text-amber-300 font-bold text-xs rounded-xl flex items-center gap-1.5 transition"
            >
              <Award className="w-3.5 h-3.5" />
              <span className="hidden md:inline">Ascend</span>
            </button>

            <button
              type="button"
              onClick={() => setIsResetConfirmOpen(true)}
              title="Reset Lineage"
              aria-label="Reset sect lineage"
              className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-lg transition"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto w-full p-4 sm:p-6 flex-1 flex flex-col gap-5">
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
          <nav
            role="tablist"
            aria-label="Sect Management Sections"
            className="flex flex-wrap items-center gap-1 bg-slate-900/60 p-1 rounded-2xl border border-slate-800"
          >
            {(
              [
                { id: 'overview', label: 'Sect Domain', icon: Castle },
                { id: 'disciples', label: `Disciples (${disciples.length})`, icon: Users },
                { id: 'cultivation', label: 'Cultivation Chamber', icon: Sparkles },
                { id: 'tech', label: 'Dao Tech Tree', icon: BookOpen },
              ] as const
            ).map(tab => {
              const Icon = tab.icon;
              const selected = activeTab === tab.id;
              return (
                <button
                  key={tab.id}
                  type="button"
                  role="tab"
                  aria-selected={selected}
                  onClick={() => {
                    soundManager.playClick();
                    setActiveTab(tab.id);
                  }}
                  className={`flex items-center gap-2 px-3.5 sm:px-4 py-2 rounded-xl text-xs font-bold transition ${
                    selected
                      ? 'bg-amber-500 text-slate-950 shadow-md'
                      : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
                  }`}
                >
                  <Icon className="w-4 h-4" />
                  {tab.label}
                </button>
              );
            })}
          </nav>

          <div className="flex flex-wrap items-center gap-2">
            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsBattleOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-gradient-to-r from-red-600 to-rose-600 hover:from-red-500 text-white font-bold text-xs rounded-xl shadow-md transition"
            >
              <Swords className="w-4 h-4" />
              ATB Battle
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsWarOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-gradient-to-r from-purple-600 to-indigo-600 hover:from-purple-500 text-white font-bold text-xs rounded-xl shadow-md transition"
            >
              <Flame className="w-4 h-4" />
              Sect War
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsTournamentOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-gradient-to-r from-amber-600 to-yellow-600 hover:from-amber-500 text-white font-bold text-xs rounded-xl shadow-md transition"
            >
              <Trophy className="w-4 h-4" />
              Tournament
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsAlchemyOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-purple-300 font-bold text-xs rounded-xl border border-slate-700 transition"
            >
              <span aria-hidden="true">💊</span>
              Alchemy
            </button>

            <button
              type="button"
              onClick={() => {
                soundManager.playClick();
                setIsMarketOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-amber-300 font-bold text-xs rounded-xl border border-slate-700 transition"
            >
              <ShoppingBag className="w-4 h-4" />
              Bazaar
            </button>
          </div>
        </div>

        {activeTab === 'overview' && (
          <SectOverview
            sectName={sectName}
            buildings={buildings}
            disciples={disciples}
            spiritStones={resources.spiritStones}
            dailyMissions={dailyMissions}
            dailyMissionDayKey={dailyMissionDayKey}
            onUpgradeBuilding={handleUpgradeBuilding}
            onBuildBuilding={handleBuildBuilding}
            onBuildingAction={handleBuildingAction}
            onClaimDailyMission={handleClaimDailyMission}
            onRerollDailyMissions={handleRerollDailyMissions}
          />
        )}

        {activeTab === 'disciples' && (
          <DiscipleRoster
            disciples={disciples}
            onAssignTask={handleAssignTask}
            onAutoAssignAll={handleAutoAssignAll}
            onRecruitNew={handleRecruitNew}
            onOpenTribulation={d => setTribulationDisciple(d)}
            onSummonFairy={handleSummonFairy}
            onForgeBond={handleForgeBond}
            maxDisciples={resources.maxDisciples}
          />
        )}

        {activeTab === 'cultivation' && (
          <CultivationChamber
            disciples={disciples}
            sectRealm={resources.sectRealm}
            sectRealmExp={resources.sectRealmExp}
            onGatherQi={() => {
              updateGameState(prev => ({
                ...prev,
                resources: {
                  ...prev.resources,
                  sectRealmExp: Math.min(1000, prev.resources.sectRealmExp + 20),
                  spiritStones: prev.resources.spiritStones + 10,
                },
                dailyMissions: advanceDailyMissions(prev.dailyMissions, 'cultivate_qi', 1),
              }));
            }}
            onBestowQi={handleBestowQi}
            onOpenTribulation={d => setTribulationDisciple(d)}
            onSectRealmBreakthrough={handleSectRealmBreakthrough}
          />
        )}

        {activeTab === 'tech' && (
          <TechTree
            techTree={techTree}
            sectExp={resources.sectExp}
            perfTier={perfTier}
            onResearchTech={handleResearchTech}
          />
        )}

        <div className="bg-slate-950/80 border border-slate-800 rounded-2xl p-4">
          <div className="flex items-center justify-between text-xs font-bold text-slate-400 mb-2">
            <span>Sect Affairs Chronicle</span>
            <span className="font-mono text-[10px] text-slate-500">Live Annals</span>
          </div>

          <div className="space-y-1.5 max-h-24 overflow-y-auto pr-1 text-xs font-mono">
            {eventLogs.map(log => (
              <div
                key={log.id}
                className={`flex items-start gap-2 ${
                  log.type === 'breakthrough'
                    ? 'text-amber-300 font-bold'
                    : log.type === 'reward'
                    ? 'text-emerald-400 font-semibold'
                    : log.type === 'warning'
                    ? 'text-red-400'
                    : 'text-slate-300'
                }`}
              >
                <span className="text-slate-500 shrink-0 text-[10px]">[{log.timestamp}]</span>
                <span>{log.message}</span>
              </div>
            ))}
          </div>
        </div>
      </main>

      {isBattleOpen && (
        <BattleArena
          playerDisciples={disciples}
          sectRealm={resources.sectRealm}
          combatMultiplier={ascensionStats.combatMultiplier}
          perfTier={perfTier}
          onBattleVictory={(stones, exp, jade) => {
            updateGameState(prev => ({
              ...prev,
              resources: {
                ...prev.resources,
                spiritStones: prev.resources.spiritStones + stones,
                sectExp: prev.resources.sectExp + exp,
                jade: prev.resources.jade + jade,
                sectRealmExp: Math.min(1000, prev.resources.sectRealmExp + 40),
              },
              dailyMissions: advanceDailyMissions(prev.dailyMissions, 'win_battle', 1),
            }));
            addEvent(`Defeated mountain fiends! Gained +${stones} SS, +${exp} EXP, +${jade} Jade.`, 'reward');
            setIsBattleOpen(false);
          }}
          onBattleDefeat={() => {
            addEvent('Sect forces retreated from demonic horde to recover.', 'warning');
            setIsBattleOpen(false);
          }}
          onExit={() => setIsBattleOpen(false)}
        />
      )}

      {isWarOpen && (
        <SectWarCampaign
          isOpen={isWarOpen}
          onClose={() => setIsWarOpen(false)}
          sectPower={resources.sectPower}
          sectRealm={resources.sectRealm}
          disciples={disciples}
          perfTier={perfTier}
          onWarCompleted={(won, loot) => {
            if (won) {
              updateGameState(prev => ({
                ...prev,
                resources: {
                  ...prev.resources,
                  spiritStones: prev.resources.spiritStones + loot.stones,
                  spiritHerbs: prev.resources.spiritHerbs + loot.herbs,
                  spiritOres: prev.resources.spiritOres + loot.ores,
                  jade: prev.resources.jade + loot.jade,
                  sectRank: Math.max(1, prev.resources.sectRank + 1),
                },
                dailyMissions: advanceDailyMissions(prev.dailyMissions, 'win_battle', 1),
              }));
              addEvent(`Crushing victory in Sect War! Plundered +${loot.stones} SS & +${loot.jade} Jade.`, 'reward');
            } else {
              addEvent('Sect campaign halted by rival patriarch defense.', 'warning');
            }
          }}
        />
      )}

      {isTournamentOpen && (
        <GrandTournament
          isOpen={isTournamentOpen}
          onClose={() => setIsTournamentOpen(false)}
          disciples={disciples}
          perfTier={perfTier}
          onTournamentFinished={(won, prizeStones, prizeJade, prizeRep) => {
            updateGameState(prev => ({
              ...prev,
              resources: {
                ...prev.resources,
                spiritStones: prev.resources.spiritStones + prizeStones,
                jade: prev.resources.jade + prizeJade,
              },
              dailyMissions: won
                ? advanceDailyMissions(prev.dailyMissions, 'win_battle', 1)
                : prev.dailyMissions,
            }));
            if (won) {
              addEvent(`Arena Champion crown secured! +${prizeStones} SS, +${prizeJade} Jade, +${prizeRep} Rep.`, 'reward');
            } else {
              addEvent(`Contender fought valiantly. Consolation +${prizeStones} SS awarded.`, 'info');
            }
          }}
        />
      )}

      {isAlchemyOpen && (
        <AlchemyForge
          isOpen={isAlchemyOpen}
          onClose={() => setIsAlchemyOpen(false)}
          spiritHerbs={resources.spiritHerbs}
          disciples={disciples}
          perfTier={perfTier}
          onCraftPill={(herbCost, pillName, exp, success) => {
            updateGameState(prev => ({
              ...prev,
              resources: {
                ...prev.resources,
                spiritHerbs: Math.max(0, prev.resources.spiritHerbs - herbCost),
                sectExp: prev.resources.sectExp + exp,
              },
              dailyMissions: advanceDailyMissions(prev.dailyMissions, 'alchemy_craft', 1),
            }));
            if (success) {
              addEvent(`Cauldron successfully refined: ${pillName}! (+${exp} Dao EXP)`, 'reward');
            } else {
              addEvent(`Alchemy cauldron explosion! ${herbCost} herbs lost to smoke.`, 'warning');
            }
          }}
          onOpenSlashMiniGame={() => {
            setIsAlchemyOpen(false);
            setSlashMiniGameConfig({ isOpen: true, type: 'alchemy' });
          }}
        />
      )}

      {isMarketOpen && (
        <Marketplace
          isOpen={isMarketOpen}
          onClose={() => setIsMarketOpen(false)}
          marketListings={marketListings}
          spiritStones={resources.spiritStones}
          onBuyItem={handleBuyMarketItem}
          onSellResource={handleSellResource}
          spiritHerbs={resources.spiritHerbs}
          spiritOres={resources.spiritOres}
        />
      )}

      {isAscensionOpen && (
        <AscensionModal
          isOpen={isAscensionOpen}
          onClose={() => setIsAscensionOpen(false)}
          sectPower={resources.sectPower}
          spiritStones={resources.spiritStones}
          sectRealm={resources.sectRealm}
          totalAscensions={ascensionStats.totalAscensions}
          ancestralEssence={ascensionStats.ancestralEssence}
          incomeMultiplier={ascensionStats.incomeMultiplier}
          combatMultiplier={ascensionStats.combatMultiplier}
          perfTier={perfTier}
          onConfirmAscension={handleConfirmAscension}
        />
      )}

      {isStudioOpen && (
        <ChimeraStudioModal
          isOpen={isStudioOpen}
          onClose={() => setIsStudioOpen(false)}
          sectName={sectName}
        />
      )}

      {slashMiniGameConfig.isOpen && (
        <SlashMiniGameModal
          isOpen={slashMiniGameConfig.isOpen}
          onClose={() => setSlashMiniGameConfig({ isOpen: false, type: 'garden' })}
          buildingType={slashMiniGameConfig.type}
          perfTier={perfTier}
          onFinished={(score, maxCombo) => {
            if (slashMiniGameConfig.type === 'garden') {
              const gainedHerbs = score * 8;
              updateGameState(prev => ({
                ...prev,
                resources: { ...prev.resources, spiritHerbs: prev.resources.spiritHerbs + gainedHerbs },
              }));
              addEvent(`Herb Slash mini-game completed! +${gainedHerbs} Spirit Herbs harvested with ${maxCombo}x combo!`, 'reward');
            } else {
              const gainedStones = score * 25;
              updateGameState(prev => ({
                ...prev,
                resources: { ...prev.resources, spiritStones: prev.resources.spiritStones + gainedStones },
              }));
              addEvent(`Alchemy Cauldron mini-game completed! +${gainedStones} Spirit Stones refined with ${maxCombo}x combo!`, 'reward');
            }
          }}
        />
      )}

      {tribulationDisciple && (
        <TribulationGame
          discipleName={tribulationDisciple.name}
          fromRealm={REALMS[tribulationDisciple.realm]}
          toRealm={REALMS[tribulationDisciple.realm + 1] || 'Heavenly Dao'}
          perfTier={perfTier}
          onSuccess={handleTribulationSuccess}
          onCancel={() => setTribulationDisciple(null)}
        />
      )}

      <ModalShell
        isOpen={isResetConfirmOpen}
        onClose={() => setIsResetConfirmOpen(false)}
        maxWidth="sm"
        bodyClassName="p-6 space-y-4"
        title={<span className="text-amber-300">Reset Cultivation Lineage?</span>}
      >
        <p className="text-xs text-slate-300 leading-relaxed">
          Start a fresh cultivation lineage? Current sect progress will be reset.
        </p>
        <div className="flex justify-end gap-2 pt-2">
          <button
            type="button"
            onClick={() => setIsResetConfirmOpen(false)}
            className="px-4 py-2 rounded-xl text-xs font-bold bg-slate-800 hover:bg-slate-700 text-slate-300 transition"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={confirmResetGame}
            className="px-4 py-2 rounded-xl text-xs font-bold bg-red-600 hover:bg-red-500 text-white transition"
          >
            Reset Sect
          </button>
        </div>
      </ModalShell>
    </div>
  );
};

export default App;
