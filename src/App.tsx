import React, { useState, useEffect, useRef } from 'react';
import {
  SectResources,
  GameTime,
  Disciple,
  Building,
  MarketItem,
  TechNode,
  Quest,
  SectEventLog,
} from './types/game';
import {
  REALMS,
  SEASONS,
  INITIAL_BUILDINGS,
  INITIAL_MARKET,
  INITIAL_TECH_TREE,
  INITIAL_QUESTS,
  TASK_NAMES,
} from './constants/gameData';
import { soundManager } from './utils/audio';
import { generateRandomDisciple, getOptimalTaskForDisciple, calculateEfficiency } from './utils/discipleFactory';

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
  CheckCircle,
} from 'lucide-react';
import confetti from 'canvas-confetti';

const STORAGE_KEY = 'idle_sect_save_v6';

export const App: React.FC = () => {
  // Navigation tabs: 'overview' | 'disciples' | 'cultivation' | 'tech'
  const [activeTab, setActiveTab] = useState<'overview' | 'disciples' | 'cultivation' | 'tech'>('overview');

  // Sect Core State
  const [sectName, setSectName] = useState<string>('Cloud Mist Sect');
  const [resources, setResources] = useState<SectResources>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.resources) return parsed.resources;
      } catch (e) {}
    }
    return {
      spiritStones: 1200,
      spiritHerbs: 150,
      spiritOres: 80,
      jade: 60,
      essence: 30,
      sectPower: 1200,
      sectExp: 450,
      sectRealm: 0,
      sectRealmExp: 150,
      sectRank: 1,
      maxDisciples: 12,
    };
  });

  const [time, setTime] = useState<GameTime>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.time) return parsed.time;
      } catch (e) {}
    }
    return { year: 1, month: 1, day: 1, hour: 8, season: 0 };
  });

  const [disciples, setDisciples] = useState<Disciple[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.disciples) return parsed.disciples;
      } catch (e) {}
    }
    // 3 starter disciples from original data
    return [
      generateRandomDisciple({ name: 'Zhao Kai', currentTask: 1 }), // Farming
      generateRandomDisciple({ name: 'Zhang Wei', currentTask: 3 }), // Alchemy
      generateRandomDisciple({ name: 'Li Yun', currentTask: 7 }),    // Guard
    ];
  });

  const [buildings, setBuildings] = useState<Building[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.buildings) return parsed.buildings;
      } catch (e) {}
    }
    return INITIAL_BUILDINGS;
  });

  const [techTree, setTechTree] = useState<TechNode[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.techTree) return parsed.techTree;
      } catch (e) {}
    }
    return INITIAL_TECH_TREE;
  });

  const [marketListings, setMarketListings] = useState<MarketItem[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.marketListings) return parsed.marketListings;
      } catch (e) {}
    }
    return INITIAL_MARKET;
  });

  const [quests, setQuests] = useState<Quest[]>(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    if (saved) {
      try {
        const parsed = JSON.parse(saved);
        if (parsed.quests) return parsed.quests;
      } catch (e) {}
    }
    return INITIAL_QUESTS;
  });

  // Ascension Prestige Data
  const [ascensionStats, setAscensionStats] = useState({
    totalAscensions: 0,
    ancestralEssence: 0,
    incomeMultiplier: 1.0,
    combatMultiplier: 1.0,
  });

  // Audio Toggles
  const [isAudioEnabled, setIsAudioEnabled] = useState<boolean>(true);
  const [isBgmEnabled, setIsBgmEnabled] = useState<boolean>(false);

  // Modals
  const [isBattleOpen, setIsBattleOpen] = useState<boolean>(false);
  const [isWarOpen, setIsWarOpen] = useState<boolean>(false);
  const [isTournamentOpen, setIsTournamentOpen] = useState<boolean>(false);
  const [isAlchemyOpen, setIsAlchemyOpen] = useState<boolean>(false);
  const [isMarketOpen, setIsMarketOpen] = useState<boolean>(false);
  const [isAscensionOpen, setIsAscensionOpen] = useState<boolean>(false);
  const [isStudioOpen, setIsStudioOpen] = useState<boolean>(false);
  const [slashMiniGameConfig, setSlashMiniGameConfig] = useState<{
    isOpen: boolean;
    type: 'garden' | 'alchemy';
  }>({ isOpen: false, type: 'garden' });
  const [tribulationDisciple, setTribulationDisciple] = useState<Disciple | null>(null);

  // Event Logs
  const [eventLogs, setEventLogs] = useState<SectEventLog[]>([
    {
      id: 'log_0',
      timestamp: 'Y1 M1 D1',
      message: 'Cloud Mist Sect sanctuary established. May your Dao lineage endure through eternity.',
      type: 'info',
    },
  ]);

  const addEvent = (msg: string, type: SectEventLog['type'] = 'info') => {
    const newLog: SectEventLog = {
      id: `log_${Date.now()}_${Math.random().toString(36).substr(2, 4)}`,
      timestamp: `Y${time.year} M${time.month} D${time.day}`,
      message: msg,
      type,
    };
    setEventLogs(prev => [newLog, ...prev.slice(0, 30)]);
  };

  // Sound toggles
  const handleToggleSound = () => {
    const next = !isAudioEnabled;
    setIsAudioEnabled(next);
    soundManager.enabled = next;
  };

  const handleToggleBgm = () => {
    const next = !isBgmEnabled;
    setIsBgmEnabled(next);
    soundManager.toggleBgm(next);
  };

  // Primary Game Loop (runs every 1.5 seconds = 1 Sect Hour)
  useEffect(() => {
    const loop = setInterval(() => {
      // 1. Advance Game Time
      setTime(prev => {
        let h = prev.hour + 1;
        let d = prev.day;
        let m = prev.month;
        let y = prev.year;
        let s = prev.season;

        if (h >= 24) {
          h = 0;
          d += 1;
          // Daily Day Tick
          processDailyTick();
        }
        if (d > 30) {
          d = 1;
          m += 1;
          s = (s + 1) % 4; // Season changes every month
        }
        if (m > 12) {
          m = 1;
          y += 1;
        }

        return { year: y, month: m, day: d, hour: h, season: s };
      });
    }, 1500);

    return () => clearInterval(loop);
  }, []);

  // Process Daily Sect Calculations
  const processDailyTick = () => {
    // Disciples income & wages
    setDisciples(prevDisciples => {
      let dailyGainSS = 0;
      let dailyWagesSS = 0;

      const updated = prevDisciples.map(d => {
        d.taskEfficiency = calculateEfficiency(d, d.currentTask);
        const taskYield = d.currentTask > 0 ? 30 + Math.floor((d.taskEfficiency / 100) * 50) : 0;
        dailyGainSS += taskYield;
        dailyWagesSS += d.dailyWage;

        // Qi accumulation for cultivators
        if (d.currentTask === 4) {
          d.realmExp += 40 + Math.floor(d.stats.intel / 4);
        }

        return { ...d };
      });

      // Buildings income
      setBuildings(prevBuildings => {
        let buildingRevenue = 0;
        prevBuildings.forEach(b => {
          if (b.isBuilt) buildingRevenue += b.level * 25;
        });

        const net = Math.floor((dailyGainSS + buildingRevenue - dailyWagesSS) * ascensionStats.incomeMultiplier);

        setResources(r => ({
          ...r,
          spiritStones: Math.max(0, r.spiritStones + net),
          sectExp: r.sectExp + 10,
          sectPower: calculateTotalPower(updated),
        }));

        return prevBuildings;
      });

      return updated;
    });

    // Market prices fluctuation
    setMarketListings(prev =>
      prev.map(it => {
        const trend = (Math.random() - 0.48) * 0.15;
        const newDemand = Math.min(2.2, Math.max(0.6, it.demand + trend));
        const newPrice = Math.round(it.originalPrice * newDemand);
        return {
          ...it,
          demand: newDemand,
          price: newPrice,
          stock: Math.min(it.maxStock, it.stock + Math.floor(Math.random() * 4)),
        };
      })
    );

    // Random Sect Encounter
    if (Math.random() < 0.25) {
      const encounters = [
        'A traveling Daoist merchant halted at the mountain gate, offering spirit wares.',
        'Spiritual mist condensed over the Lotus Pond, accelerating disciple Qi absorption.',
        'Wild spirit beasts were repelled by the outer mountain ward.',
        'Disciples achieved mutual dao enlightenment during morning sword practice.',
      ];
      const ev = encounters[Math.floor(Math.random() * encounters.length)];
      addEvent(ev, 'info');
    }
  };

  const calculateTotalPower = (discList: Disciple[]) => {
    return discList.reduce((acc, d) => acc + Math.floor((d.atk + d.def + d.spd) * 0.6 + d.maxHp * 0.1), 300);
  };

  // Auto-Save every 15 seconds
  useEffect(() => {
    const saver = setInterval(() => {
      const payload = {
        resources,
        time,
        disciples,
        buildings,
        techTree,
        marketListings,
        quests,
        ascensionStats,
      };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
    }, 15000);

    return () => clearInterval(saver);
  }, [resources, time, disciples, buildings, techTree, marketListings, quests, ascensionStats]);

  // Handle building upgrades
  const handleUpgradeBuilding = (bId: number) => {
    setBuildings(prev =>
      prev.map(b => {
        if (b.id === bId && resources.spiritStones >= b.upgradeCost) {
          setResources(r => ({ ...r, spiritStones: r.spiritStones - b.upgradeCost }));
          addEvent(`Upgraded ${b.name} to Level ${b.level + 1}! Daily revenue increased.`, 'reward');
          return {
            ...b,
            level: b.level + 1,
            upgradeCost: Math.round(b.upgradeCost * 1.55),
          };
        }
        return b;
      })
    );
  };

  const handleBuildBuilding = (bId: number) => {
    setBuildings(prev =>
      prev.map(b => {
        if (b.id === bId && resources.spiritStones >= b.cost) {
          setResources(r => ({ ...r, spiritStones: r.spiritStones - b.cost }));
          addEvent(`Erected ${b.name}! Sect infrastructure expanded.`, 'reward');
          return {
            ...b,
            isBuilt: true,
            level: 1,
          };
        }
        return b;
      })
    );
  };

  // Building special triggers
  const handleBuildingAction = (b: Building) => {
    if (b.name.includes('Garden')) {
      setSlashMiniGameConfig({ isOpen: true, type: 'garden' });
    } else if (b.name.includes('Alchemy')) {
      setIsAlchemyOpen(true);
    }
  };

  // Assign task to a disciple
  const handleAssignTask = (discipleId: string, task: number) => {
    setDisciples(prev =>
      prev.map(d => {
        if (d.id === discipleId) {
          const updated = { ...d, currentTask: task };
          updated.taskEfficiency = calculateEfficiency(updated, task);
          return updated;
        }
        return d;
      })
    );
  };

  // AI Auto Assign all disciples
  const handleAutoAssignAll = () => {
    soundManager.playBreakthrough();
    setDisciples(prev =>
      prev.map(d => {
        const opt = getOptimalTaskForDisciple(d);
        const updated = { ...d, currentTask: opt };
        updated.taskEfficiency = calculateEfficiency(updated, opt);
        return updated;
      })
    );
    addEvent('Grandmaster AI optimized the sect workforce based on disciple talents.', 'info');
  };

  // Recruit new disciple
  const handleRecruitNew = () => {
    if (resources.spiritStones < 200) {
      addEvent('Insufficient spirit stones (200 SS needed to recruit).', 'warning');
      return;
    }
    const candidate = generateRandomDisciple();
    setResources(r => ({ ...r, spiritStones: r.spiritStones - 200 }));
    setDisciples(prev => [...prev, candidate]);
    soundManager.playBreakthrough();
    confetti({ particleCount: 60, spread: 50 });
    addEvent(`Welcomed gifted disciple ${candidate.name} (${candidate.talentName}) into the sect!`, 'reward');
  };

  // Summon Fairy
  const handleSummonFairy = (discipleId: string) => {
    if (resources.essence < 20) {
      addEvent('Insufficient spiritual essence (20 Essence needed).', 'warning');
      return;
    }
    setResources(r => ({ ...r, essence: r.essence - 20 }));
    const fairyNames = ['Snow Sprite', 'Jade Butterfly', 'Flame Pixie', 'Lotus Spirit'];
    const fName = fairyNames[Math.floor(Math.random() * fairyNames.length)];

    setDisciples(prev =>
      prev.map(d => {
        if (d.id === discipleId) {
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
        }
        return d;
      })
    );
    addEvent(`Spiritual companion ${fName} bonded with disciple!`, 'reward');
  };

  // Buy item from Bazaar
  const handleBuyMarketItem = (item: MarketItem) => {
    if (resources.spiritStones < item.price || item.stock <= 0) return;
    setResources(r => ({ ...r, spiritStones: r.spiritStones - item.price }));
    setMarketListings(prev =>
      prev.map(it => (it.id === item.id ? { ...it, stock: it.stock - 1 } : it))
    );
    addEvent(`Purchased ${item.itemName} for ${item.price} Spirit Stones.`, 'info');
  };

  // Sell resources to merchants
  const handleSellResource = (type: 'herbs' | 'ores', amount: number, earnings: number) => {
    setResources(r => ({
      ...r,
      spiritHerbs: type === 'herbs' ? r.spiritHerbs - amount : r.spiritHerbs,
      spiritOres: type === 'ores' ? r.spiritOres - amount : r.spiritOres,
      spiritStones: r.spiritStones + earnings,
    }));
    addEvent(`Sold surplus commodities for +${earnings} Spirit Stones.`, 'reward');
  };

  // Research Tech
  const handleResearchTech = (tech: TechNode) => {
    if (resources.sectExp < tech.costExp) return;
    setResources(r => ({ ...r, sectExp: r.sectExp - tech.costExp }));
    setTechTree(prev =>
      prev.map(t => (t.id === tech.id ? { ...t, isResearched: true } : t))
    );
    addEvent(`Mastered Dao Enlightenment: ${tech.name}! Permanent sect buffs activated.`, 'reward');
  };

  // Breakthrough disciple after winning Tribulation
  const handleTribulationSuccess = () => {
    if (!tribulationDisciple) return;
    setDisciples(prev =>
      prev.map(d => {
        if (d.id === tribulationDisciple.id) {
          const nextRealm = Math.min(REALMS.length - 1, d.realm + 1);
          return {
            ...d,
            realm: nextRealm,
            realmExp: 0,
            atk: d.atk + 25,
            def: d.def + 18,
            maxHp: d.maxHp + 90,
            hp: d.maxHp + 90,
          };
        }
        return d;
      })
    );
    addEvent(`⚡ ${tribulationDisciple.name} survived Heavenly Tribulation and advanced to ${REALMS[tribulationDisciple.realm + 1]}!`, 'breakthrough');
    setTribulationDisciple(null);
  };

  // Sect Realm Breakthrough
  const handleSectRealmBreakthrough = () => {
    setResources(r => ({
      ...r,
      sectRealm: Math.min(REALMS.length - 1, r.sectRealm + 1),
      sectRealmExp: 0,
      jade: r.jade + 50,
    }));
    addEvent(`🌟 The entire Sect ascended to ${REALMS[resources.sectRealm + 1]} realm! Received +50 Celestial Jade!`, 'breakthrough');
  };

  // Ascension / Reincarnation
  const handleConfirmAscension = (earnedEssence: number) => {
    setAscensionStats(prev => ({
      totalAscensions: prev.totalAscensions + 1,
      ancestralEssence: prev.ancestralEssence + earnedEssence,
      incomeMultiplier: prev.incomeMultiplier + earnedEssence * 0.05,
      combatMultiplier: prev.combatMultiplier + earnedEssence * 0.03,
    }));

    // Reset progress while keeping Jade and Essence
    const savedJade = resources.jade;
    setResources({
      spiritStones: 1200,
      spiritHerbs: 150,
      spiritOres: 80,
      jade: savedJade,
      essence: 30 + earnedEssence,
      sectPower: 1200,
      sectExp: 300,
      sectRealm: 0,
      sectRealmExp: 0,
      sectRank: 1,
      maxDisciples: 12,
    });

    setBuildings(INITIAL_BUILDINGS);
    setDisciples([
      generateRandomDisciple({ name: 'Zhao Kai', currentTask: 1 }),
      generateRandomDisciple({ name: 'Zhang Wei', currentTask: 3 }),
    ]);

    setIsAscensionOpen(false);
    addEvent(`Ascended into Immortal Cycle! Gained +${earnedEssence} Ancestral Essence.`, 'breakthrough');
  };

  // Reset Game
  const handleResetGame = () => {
    if (confirm('Start a fresh cultivation lineage? Current sect progress will be reset.')) {
      localStorage.removeItem(STORAGE_KEY);
      window.location.reload();
    }
  };

  return (
    <div className="min-h-screen bg-[#05060b] text-slate-100 flex flex-col">
      {/* Top Navigation Bar */}
      <header className="sticky top-0 z-40 bg-slate-950/90 border-b border-slate-800 backdrop-blur-md px-4 sm:px-6 py-2.5">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-3">
          {/* Logo & Sect Title */}
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-amber-600 via-amber-400 to-yellow-300 flex items-center justify-center text-slate-950 font-bold shadow-md shadow-amber-500/20">
              ⚡
            </div>
            <div>
              <h1 className="font-serif-title font-bold text-sm sm:text-base text-amber-300 tracking-wide">
                {sectName}
              </h1>
              <div className="flex items-center gap-2 text-[11px] text-slate-400 font-mono">
                <span>{REALMS[resources.sectRealm]}</span>
                <span>•</span>
                <span>Rank #{resources.sectRank}</span>
                <span>•</span>
                <span className="text-amber-400">Power: {resources.sectPower}</span>
              </div>
            </div>
          </div>

          {/* Core Resources Ticker */}
          <div className="flex flex-wrap items-center gap-2 sm:gap-3 text-xs font-mono">
            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span>💎</span>
              <span className="font-bold text-amber-400">{resources.spiritStones}</span>
              <span className="text-slate-500 text-[10px]">SS</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span>✨</span>
              <span className="font-bold text-cyan-400">{resources.jade}</span>
              <span className="text-slate-500 text-[10px]">Jade</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span>🌿</span>
              <span className="font-bold text-emerald-400">{resources.spiritHerbs}</span>
            </div>

            <div className="bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl flex items-center gap-1.5 shadow-xs">
              <span>🪨</span>
              <span className="font-bold text-slate-300">{resources.spiritOres}</span>
            </div>

            {/* Time / Season */}
            <div className="hidden sm:flex items-center gap-1.5 bg-slate-900/90 border border-slate-800 px-2.5 py-1 rounded-xl text-slate-400 text-[11px]">
              <Calendar className="w-3.5 h-3.5 text-indigo-400" />
              <span>Y{time.year} M{time.month} D{time.day} ({SEASONS[time.season]})</span>
            </div>
          </div>

          {/* Top Quick Actions (Sound, Studio, Ascension) */}
          <div className="flex items-center gap-1.5">
            <button
              onClick={handleToggleSound}
              title={isAudioEnabled ? 'Mute SFX' : 'Enable SFX'}
              className="p-2 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
            >
              {isAudioEnabled ? <Volume2 className="w-4 h-4 text-cyan-400" /> : <VolumeX className="w-4 h-4" />}
            </button>

            <button
              onClick={handleToggleBgm}
              title={isBgmEnabled ? 'Disable Guzheng BGM' : 'Play Guzheng BGM'}
              className={`p-2 rounded-lg transition ${
                isBgmEnabled ? 'text-amber-400 bg-amber-500/10' : 'text-slate-400 hover:text-white hover:bg-slate-800'
              }`}
            >
              <Music className="w-4 h-4" />
            </button>

            <button
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
              onClick={handleResetGame}
              title="Reset Lineage"
              className="p-2 text-slate-500 hover:text-red-400 hover:bg-slate-800 rounded-lg transition"
            >
              <RotateCcw className="w-4 h-4" />
            </button>
          </div>
        </div>
      </header>

      {/* Main Container */}
      <main className="max-w-7xl mx-auto w-full p-4 sm:p-6 flex-1 flex flex-col gap-5">
        {/* Navigation Tabs Bar */}
        <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
          <nav className="flex items-center gap-1 bg-slate-900/60 p-1 rounded-2xl border border-slate-800">
            <button
              onClick={() => {
                soundManager.playClick();
                setActiveTab('overview');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'overview'
                  ? 'bg-amber-500 text-slate-950 shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Castle className="w-4 h-4" />
              Sect Domain
            </button>

            <button
              onClick={() => {
                soundManager.playClick();
                setActiveTab('disciples');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'disciples'
                  ? 'bg-amber-500 text-slate-950 shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Users className="w-4 h-4" />
              Disciples ({disciples.length})
            </button>

            <button
              onClick={() => {
                soundManager.playClick();
                setActiveTab('cultivation');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'cultivation'
                  ? 'bg-amber-500 text-slate-950 shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <Sparkles className="w-4 h-4" />
              Cultivation Chamber
            </button>

            <button
              onClick={() => {
                soundManager.playClick();
                setActiveTab('tech');
              }}
              className={`flex items-center gap-2 px-4 py-2 rounded-xl text-xs font-bold transition ${
                activeTab === 'tech'
                  ? 'bg-amber-500 text-slate-950 shadow-md'
                  : 'text-slate-400 hover:text-white hover:bg-slate-800/60'
              }`}
            >
              <BookOpen className="w-4 h-4" />
              Dao Tech Tree
            </button>
          </nav>

          {/* Quick Trigger Action Buttons */}
          <div className="flex items-center gap-2">
            <button
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
              onClick={() => {
                soundManager.playClick();
                setIsAlchemyOpen(true);
              }}
              className="flex items-center gap-1.5 px-3.5 py-2 bg-slate-800 hover:bg-slate-700 text-purple-300 font-bold text-xs rounded-xl border border-slate-700 transition"
            >
              <span>💊</span>
              Alchemy
            </button>

            <button
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

        {/* Tab Content Display */}
        {activeTab === 'overview' && (
          <SectOverview
            buildings={buildings}
            disciples={disciples}
            spiritStones={resources.spiritStones}
            onUpgradeBuilding={handleUpgradeBuilding}
            onBuildBuilding={handleBuildBuilding}
            onBuildingAction={handleBuildingAction}
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
            maxDisciples={resources.maxDisciples}
          />
        )}

        {activeTab === 'cultivation' && (
          <CultivationChamber
            disciples={disciples}
            sectRealm={resources.sectRealm}
            sectRealmExp={resources.sectRealmExp}
            onGatherQi={() => {
              setResources(r => ({
                ...r,
                sectRealmExp: Math.min(1000, r.sectRealmExp + 20),
                spiritStones: r.spiritStones + 10,
              }));
            }}
            onOpenTribulation={d => setTribulationDisciple(d)}
            onSectRealmBreakthrough={handleSectRealmBreakthrough}
          />
        )}

        {activeTab === 'tech' && (
          <TechTree
            techTree={techTree}
            sectExp={resources.sectExp}
            onResearchTech={handleResearchTech}
          />
        )}

        {/* Live Chronology & Sect Affairs Log */}
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

      {/* Modals & Sub-systems */}
      {isBattleOpen && (
        <BattleArena
          playerDisciples={disciples}
          sectRealm={resources.sectRealm}
          onBattleVictory={(stones, exp, jade) => {
            setResources(r => ({
              ...r,
              spiritStones: r.spiritStones + stones,
              sectExp: r.sectExp + exp,
              jade: r.jade + jade,
              sectRealmExp: r.sectRealmExp + 40,
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
          onWarCompleted={(won, loot) => {
            if (won) {
              setResources(r => ({
                ...r,
                spiritStones: r.spiritStones + loot.stones,
                spiritHerbs: r.spiritHerbs + loot.herbs,
                spiritOres: r.spiritOres + loot.ores,
                jade: r.jade + loot.jade,
                sectRank: Math.max(1, r.sectRank + 1),
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
          onTournamentFinished={(won, prizeStones, prizeJade, prizeRep) => {
            setResources(r => ({
              ...r,
              spiritStones: r.spiritStones + prizeStones,
              jade: r.jade + prizeJade,
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
          onCraftPill={(herbCost, pillName, exp, success) => {
            setResources(r => ({
              ...r,
              spiritHerbs: r.spiritHerbs - herbCost,
              sectExp: r.sectExp + exp,
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
          onFinished={(score, maxCombo) => {
            if (slashMiniGameConfig.type === 'garden') {
              const gainedHerbs = score * 8;
              setResources(r => ({ ...r, spiritHerbs: r.spiritHerbs + gainedHerbs }));
              addEvent(`Herb Slash mini-game completed! +${gainedHerbs} Spirit Herbs harvested with ${maxCombo}x combo!`, 'reward');
            } else {
              const gainedStones = score * 25;
              setResources(r => ({ ...r, spiritStones: r.spiritStones + gainedStones }));
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
          onSuccess={handleTribulationSuccess}
          onCancel={() => setTribulationDisciple(null)}
        />
      )}
    </div>
  );
};

export default App;
