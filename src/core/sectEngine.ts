import confetti from 'canvas-confetti';
import type {
  BattleUnit,
  Building,
  DailyMission,
  DailyMissionKind,
  DailyMissionRewardType,
  Disciple,
  DiscipleBond,
  DiscipleBondType,
  DiscipleStats,
  GameTime,
  MarketItem,
  Quest,
  SectEventLog,
  SectResources,
  TechNode,
} from '../types/game';
import {
  ELEMENT_NAMES,
  NAME_POOL,
  PERSONALITY_NAMES,
  REALMS,
  SECT_DEFAULTS,
  STARTER_DISCIPLES,
  TALENT_NAMES,
  TASK_BASE_INCOME,
  createInitialBuildings,
  createInitialMarket,
  createInitialQuests,
  createInitialTechTree,
} from '../constants/gameData';

export type PerformanceMode = 'auto' | 'eco' | 'balanced' | 'ultra';
export type ResolvedPerformanceTier = 'eco' | 'balanced' | 'ultra';

export interface AscensionStats {
  totalAscensions: number;
  ancestralEssence: number;
  incomeMultiplier: number;
  combatMultiplier: number;
}

export interface PersistedSectState {
  version: number;
  sectName: string;
  resources: SectResources;
  time: GameTime;
  disciples: Disciple[];
  buildings: Building[];
  techTree: TechNode[];
  marketListings: MarketItem[];
  quests: Quest[];
  dailyMissions: DailyMission[];
  dailyMissionDayKey: string;
  ascensionStats: AscensionStats;
  perfMode: PerformanceMode;
  audioEnabled: boolean;
  bgmEnabled: boolean;
  lastSavedAt: number;
}

export const STORAGE_KEY = 'idle_sect_save_v6';
export const SAVE_SCHEMA_VERSION = 6;
export const MAX_OFFLINE_TICKS = 48; // Cap catch-up to 48 sect hours (2 sect days) to keep resume instant

export function clamp(val: number, min: number, max: number): number {
  if (!Number.isFinite(val)) return min;
  return val < min ? min : val > max ? max : val;
}

export function detectDevicePerformanceTier(mode: PerformanceMode = 'auto'): ResolvedPerformanceTier {
  if (mode === 'eco' || mode === 'balanced' || mode === 'ultra') {
    return mode;
  }
  if (typeof window === 'undefined' || typeof navigator === 'undefined') {
    return 'balanced';
  }
  const nav = navigator as Navigator & {
    deviceMemory?: number;
    connection?: { saveData?: boolean };
  };
  const reducedMotion =
    typeof window.matchMedia === 'function' &&
    window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  const saveData = Boolean(nav.connection?.saveData);
  const lowMemory = typeof nav.deviceMemory === 'number' && nav.deviceMemory <= 2;
  const lowCores = typeof nav.hardwareConcurrency === 'number' && nav.hardwareConcurrency <= 4;

  if (reducedMotion || saveData || lowMemory || lowCores) {
    return 'eco';
  }
  if (typeof nav.deviceMemory === 'number' && nav.deviceMemory >= 8 && (nav.hardwareConcurrency ?? 4) >= 8) {
    return 'ultra';
  }
  return 'balanced';
}

export function triggerCelebration(tier: ResolvedPerformanceTier, options?: { particleCount?: number; spread?: number; origin?: { y: number } }): void {
  if (tier === 'eco' || typeof window === 'undefined') return;
  const count = tier === 'balanced' ? Math.min(35, options?.particleCount ?? 60) : (options?.particleCount ?? 70);
  try {
    confetti({
      particleCount: count,
      spread: options?.spread ?? 60,
      origin: options?.origin,
      disableForReducedMotion: true,
    });
  } catch {
    // Graceful no-op in headless/test environments without canvas confetti context
  }
}

// ============================================================================
// UNIFIED DISCIPLE STATS, EFFICIENCY, WAGE & FACTORY (Matches Android Java)
// ============================================================================

export function computeDiscipleCombatStats(
  stats: DiscipleStats,
  realm: number,
  level = 1,
  scaleByLevel = false
): Pick<Disciple, 'maxHp' | 'maxMp' | 'atk' | 'def' | 'spd' | 'critRate' | 'critDmg' | 'dodge' | 'accuracy'> {
  const safeRealm = Math.max(0, realm);
  const safeLevel = Math.max(1, level);
  const levelBonusHp = scaleByLevel ? safeLevel * 25 : 0;
  const levelBonusMp = scaleByLevel ? safeLevel * 15 : 0;
  const levelBonusAtk = scaleByLevel ? safeLevel * 5 : 0;
  const levelBonusDef = scaleByLevel ? safeLevel * 3 : 0;
  const levelBonusSpd = scaleByLevel ? safeLevel * 2 : 0;

  return {
    maxHp: 100 + stats.vit * 10 + stats.str * 5 + safeRealm * 50 + levelBonusHp,
    maxMp: 50 + stats.intel * 8 + stats.wis * 5 + safeRealm * 20 + levelBonusMp,
    atk: stats.str * 3 + stats.agi + safeRealm * 15 + levelBonusAtk,
    def: stats.vit * 2 + stats.str + safeRealm * 10 + levelBonusDef,
    spd: stats.agi * 2 + safeRealm * 5 + levelBonusSpd,
    critRate: stats.lck * 2,
    critDmg: 150 + stats.str,
    dodge: stats.agi + stats.lck,
    accuracy: stats.wis * 2 + stats.agi,
  };
}

export function calculateExpectedWage(
  stats: DiscipleStats,
  realm: number,
  personality: number,
  talentGrade: number
): number {
  const statAvg = Math.floor((stats.str + stats.agi + stats.intel + stats.lck + stats.vit + stats.wis + stats.cha) / 7);
  let base = 50 + realm * 30 + statAvg;
  switch (personality) {
    case 1: // Arrogant
      base = Math.floor(base * 1.5);
      break;
    case 4: // Merchant
      base = Math.floor(base * 1.3);
      break;
    case 6: // Lazy
      base = Math.floor(base * 0.7);
      break;
    case 10: // Orphan
      base = Math.floor(base * 0.6);
      break;
    case 15: // Proud
      base = Math.floor(base * 1.4);
      break;
  }
  base += talentGrade * 10;
  return Math.max(1, base);
}

export function getBondLabel(type: DiscipleBondType): string {
  return type === 'master_disciple' ? 'Master-Disciple' : 'Sect Siblings';
}

export function inferBondType(a: Disciple, b: Disciple): DiscipleBondType {
  return Math.abs(a.realm - b.realm) >= 1 || Math.abs(a.age - b.age) >= 6
    ? 'master_disciple'
    : 'sect_siblings';
}

export interface ActiveBondBonus {
  efficiencyBonus: number;
  qiBonus: number;
  activePartners: Array<{
    id: string;
    name: string;
    type: DiscipleBondType;
    level: number;
  }>;
}

export function getActiveBondBonusForDisciple(
  disciple: Disciple,
  allDisciples?: Disciple[],
  taskOverride?: number
): ActiveBondBonus {
  const bonds = disciple.bonds;
  if (!bonds || bonds.length === 0 || !allDisciples || allDisciples.length === 0) {
    return { efficiencyBonus: 0, qiBonus: 0, activePartners: [] };
  }

  const effectiveTask = taskOverride !== undefined ? taskOverride : disciple.currentTask;
  if (effectiveTask <= 0) {
    return { efficiencyBonus: 0, qiBonus: 0, activePartners: [] };
  }

  const byId = new Map<string, Disciple>();
  for (const candidate of allDisciples) {
    byId.set(candidate.id, candidate);
  }

  let efficiencyBonus = 0;
  let qiBonus = 0;
  const activePartners: ActiveBondBonus['activePartners'] = [];

  for (const bond of bonds) {
    const partner = byId.get(bond.partnerId);
    if (!partner || partner.currentTask !== effectiveTask) continue;
    const lvl = clamp(bond.level, 1, 5);
    const effPerBond =
      bond.type === 'master_disciple' ? 8 + (lvl - 1) * 2 : 6 + (lvl - 1) * 2;
    const qiPerBond = bond.type === 'master_disciple' ? 10 * lvl : 6 * lvl;
    efficiencyBonus += effPerBond;
    qiBonus += qiPerBond;
    activePartners.push({
      id: partner.id,
      name: partner.name,
      type: bond.type,
      level: lvl,
    });
  }

  return {
    efficiencyBonus: Math.min(25, efficiencyBonus),
    qiBonus: Math.min(60, qiBonus),
    activePartners,
  };
}

export function getBattleBondBonusForDisciple(
  disciple: Disciple,
  party: Disciple[]
): { atkBonus: number; defBonus: number; partnerNames: string[] } {
  const bonds = disciple.bonds;
  if (!bonds || bonds.length === 0 || party.length <= 1) {
    return { atkBonus: 0, defBonus: 0, partnerNames: [] };
  }
  const byId = new Map<string, Disciple>();
  for (const member of party) {
    byId.set(member.id, member);
  }
  let atkBonus = 0;
  let defBonus = 0;
  const partnerNames: string[] = [];
  for (const bond of bonds) {
    const partner = byId.get(bond.partnerId);
    if (!partner) continue;
    const lvl = clamp(bond.level, 1, 5);
    const statBoost = bond.type === 'master_disciple' ? 5 * lvl : 4 * lvl;
    atkBonus += statBoost;
    defBonus += statBoost;
    partnerNames.push(partner.name);
  }
  return { atkBonus, defBonus, partnerNames };
}

export function calculateDiscipleEfficiency(
  d: Disciple,
  task: number,
  allDisciples?: Disciple[]
): number {
  if (task === 0) return 0;
  let base = 50;
  const s = d.stats;
  switch (task) {
    case 1: base += Math.floor(s.str / 2 + s.vit / 4); break;
    case 2: base += Math.floor(s.agi / 2 + s.intel / 4); break;
    case 3: base += Math.floor(s.intel / 2 + s.wis / 4); break;
    case 4: base += Math.floor((s.intel + s.wis) / 4); break;
    case 5: base += Math.floor(s.str / 2 + s.vit / 3); break;
    case 6: base += Math.floor(s.str / 3 + s.agi / 3 + s.vit / 3); break;
    case 7: base += Math.floor(s.str / 3 + s.vit / 3 + s.cha / 4); break;
    case 8: base += Math.floor(s.intel / 2 + s.wis / 3); break;
    case 9: base += Math.floor(s.cha / 2 + s.lck / 3 + s.intel / 4); break;
    case 10: base += Math.floor(s.agi / 2 + s.lck / 3 + s.wis / 4); break;
  }

  // Element bonuses matching Disciple.java
  if (task === 3 && (d.element === 1 || d.element === 2)) base += 10;
  if (task === 1 && d.element === 3) base += 15;
  if (task === 5 && d.element === 5) base += 15;
  if (task === 2 && d.element === 4) base += 15;

  if (d.energy < 30) base -= 20;
  if (d.stress > 70) base -= 15;
  if (d.mood < 40) base -= 10;
  if (d.fairy) base += d.fairy.efficiencyBonus;

  if (allDisciples && allDisciples.length > 1) {
    base += getActiveBondBonusForDisciple(d, allDisciples, task).efficiencyBonus;
  }

  return clamp(base, 15, 115);
}

export function getOptimalTaskForDisciple(d: Disciple): number {
  const { str, intel, vit, agi, cha } = d.stats;
  const highest = Math.max(str, intel, vit, agi, cha);
  if (highest === intel) return intel > 28 ? 3 : 8; // Alchemy : Research
  if (highest === str) return 2; // Crafting
  if (highest === vit) return 5; // Mining
  if (highest === agi) return 10; // Exploring
  if (highest === cha) return 9; // Trading
  return 1; // Farming
}

export function createDisciple(overrides?: Partial<Disciple>, rng: () => number = Math.random): Disciple {
  const isMale = overrides?.isMale ?? rng() > 0.45;
  const firstName = NAME_POOL.first[Math.floor(rng() * NAME_POOL.first.length)];
  const givenName = NAME_POOL.given[Math.floor(rng() * NAME_POOL.given.length)];
  const name = overrides?.name ?? `${firstName} ${givenName}`;

  const stats: DiscipleStats = overrides?.stats ?? {
    str: 10 + Math.floor(rng() * 32),
    agi: 10 + Math.floor(rng() * 32),
    intel: 10 + Math.floor(rng() * 32),
    lck: 5 + Math.floor(rng() * 25),
    vit: 10 + Math.floor(rng() * 30),
    wis: 10 + Math.floor(rng() * 28),
    cha: 5 + Math.floor(rng() * 25),
  };

  const element = overrides?.element ?? (1 + Math.floor(rng() * (ELEMENT_NAMES.length - 1)));
  const personality = overrides?.personality ?? (1 + Math.floor(rng() * (PERSONALITY_NAMES.length - 1)));
  const talentType = overrides?.talentType ?? (1 + Math.floor(rng() * (TALENT_NAMES.length - 1)));
  const talentGrade = overrides?.talentGrade ?? (1 + Math.floor(rng() * 5));
  const talentName = overrides?.talentName ?? `${TALENT_NAMES[talentType]} (Grade ${talentGrade})`;
  const realm = overrides?.realm ?? 0;
  const level = overrides?.level ?? 1;

  const combat = computeDiscipleCombatStats(stats, realm, level, false);
  const dailyWage = overrides?.dailyWage ?? calculateExpectedWage(stats, realm, personality, talentGrade);
  const id = overrides?.id ?? `d_${Date.now()}_${Math.floor(rng() * 1e6).toString(36)}`;

  const baseDisciple: Disciple = {
    id,
    name,
    isMale,
    age: 16 + Math.floor(rng() * 12),
    lifespan: 90 + Math.floor(rng() * 50),
    level,
    exp: 0,
    maxExp: 100,
    stats,
    realm,
    realmExp: 0,
    element,
    personality,
    talentType,
    talentGrade,
    talentName,
    mood: 75,
    stress: 10,
    energy: 100,
    maxEnergy: 100,
    loyalty: 60 + Math.floor(rng() * 30),
    reputation: 0,
    relationshipSect: 60,
    dailyWage,
    currentTask: 0,
    taskEfficiency: 60,
    taskLevel: 1,
    hp: combat.maxHp,
    maxHp: combat.maxHp,
    mp: combat.maxMp,
    maxMp: combat.maxMp,
    atk: combat.atk,
    def: combat.def,
    spd: combat.spd,
    critRate: combat.critRate,
    critDmg: combat.critDmg,
    dodge: combat.dodge,
    accuracy: combat.accuracy,
    hobby: Math.floor(rng() * 8),
    bonds: overrides?.bonds ?? [],
    totalBattles: 0,
    battlesWon: 0,
    kills: 0,
    tournamentsWon: 0,
    posX: 250 + rng() * 300,
    posY: 200 + rng() * 200,
    ...overrides,
  };

  baseDisciple.taskEfficiency = calculateDiscipleEfficiency(baseDisciple, baseDisciple.currentTask);
  return baseDisciple;
}

export function forgeDiscipleBond(
  disciples: Disciple[],
  idA: string,
  idB: string,
  preferredType?: DiscipleBondType
): { disciples: Disciple[]; bondType: DiscipleBondType | null } {
  if (!idA || !idB || idA === idB) {
    return { disciples, bondType: null };
  }
  const discA = disciples.find(d => d.id === idA);
  const discB = disciples.find(d => d.id === idB);
  if (!discA || !discB) {
    return { disciples, bondType: null };
  }
  if ((discA.bonds ?? []).some(b => b.partnerId === idB)) {
    return { disciples, bondType: null };
  }

  const bondType = preferredType ?? inferBondType(discA, discB);
  const withBonds = disciples.map(d => {
    if (d.id === idA) {
      const nextBond: DiscipleBond = { partnerId: idB, type: bondType, level: 1, affinity: 25 };
      return { ...d, bonds: [...(d.bonds ?? []), nextBond] };
    }
    if (d.id === idB) {
      const nextBond: DiscipleBond = { partnerId: idA, type: bondType, level: 1, affinity: 25 };
      return { ...d, bonds: [...(d.bonds ?? []), nextBond] };
    }
    return d;
  });

  return {
    disciples: withBonds.map(d => ({
      ...d,
      taskEfficiency: calculateDiscipleEfficiency(d, d.currentTask, withBonds),
    })),
    bondType,
  };
}

export function processDailyBonds(
  disciples: Disciple[],
  rng: () => number = Math.random
): { disciples: Disciple[]; bondEvent: string | null } {
  if (disciples.length < 2) {
    return { disciples, bondEvent: null };
  }

  const next = disciples.map(d => ({
    ...d,
    bonds: (d.bonds ?? []).map(b => ({ ...b })),
  }));
  let bondEvent: string | null = null;

  for (let i = 0; i < next.length; i++) {
    const a = next[i];
    if (a.currentTask <= 0) continue;
    for (let j = i + 1; j < next.length; j++) {
      const b = next[j];
      if (b.currentTask !== a.currentTask) continue;

      const bondAB = a.bonds.find(bond => bond.partnerId === b.id);
      const bondBA = b.bonds.find(bond => bond.partnerId === a.id);

      if (bondAB && bondBA) {
        for (const bond of [bondAB, bondBA]) {
          if (bond.level < 5) {
            bond.affinity += 15;
            if (bond.affinity >= 100) {
              bond.affinity -= 100;
              bond.level += 1;
              if (!bondEvent) {
                bondEvent = `🤝 ${a.name} & ${b.name}'s ${getBondLabel(bond.type)} bond deepened to Lv.${bond.level}!`;
              }
            }
          }
        }
      } else if (!bondAB && !bondBA && a.bonds.length < 4 && b.bonds.length < 4 && rng() < 0.35) {
        const type = inferBondType(a, b);
        a.bonds.push({ partnerId: b.id, type, level: 1, affinity: 20 });
        b.bonds.push({ partnerId: a.id, type, level: 1, affinity: 20 });
        if (!bondEvent) {
          bondEvent = `🤝 Shared labor sparked a ${getBondLabel(type)} bond between ${a.name} and ${b.name}!`;
        }
      }
    }
  }

  return { disciples: next, bondEvent };
}

export function createStarterDisciples(rng: () => number = Math.random): Disciple[] {
  const starters = STARTER_DISCIPLES.map((s, idx) =>
    createDisciple({ id: `starter_${idx}`, name: s.name, currentTask: s.currentTask, isMale: s.isMale }, rng)
  );
  if (starters.length >= 3) {
    return forgeDiscipleBond(starters, starters[0].id, starters[2].id, 'sect_siblings').disciples;
  }
  return starters;
}

// ============================================================================
// ECONOMY, SIMULATION & BATTLE LOGIC
// ============================================================================

export function calculateTotalPower(disciples: Disciple[], combatMultiplier = 1.0): number {
  const base = disciples.reduce(
    (acc, d) => acc + Math.floor((d.atk + d.def + d.spd) * 0.6 + d.maxHp * 0.1),
    300
  );
  return Math.floor(base * Math.max(1, combatMultiplier));
}

export function getDiscipleTaskIncome(d: Disciple): number {
  if (d.currentTask <= 0) return 0;
  const base = TASK_BASE_INCOME[clamp(d.currentTask, 0, TASK_BASE_INCOME.length - 1)] || 0;
  const taskYield = base > 0
    ? Math.floor((base * d.taskEfficiency * (100 + d.taskLevel * 5)) / 10000)
    : 30 + Math.floor((d.taskEfficiency / 100) * 50);
  return Math.max(10, taskYield);
}

export function calculateDailyEconomySummary(
  disciples: Disciple[],
  buildings: Building[],
  incomeMultiplier = 1.0
): { grossIncome: number; wages: number; buildingIncome: number; netDaily: number } {
  let grossIncome = 0;
  let wages = 0;
  for (const d of disciples) {
    grossIncome += getDiscipleTaskIncome(d);
    wages += d.dailyWage;
  }
  let buildingIncome = 0;
  for (const b of buildings) {
    if (b.isBuilt) buildingIncome += b.level * 25;
  }
  const netDaily = Math.floor((grossIncome + buildingIncome - wages) * Math.max(1, incomeMultiplier));
  return { grossIncome, wages, buildingIncome, netDaily };
}

export function advanceMarketListings(listings: MarketItem[], rng: () => number = Math.random): MarketItem[] {
  return listings.map(it => {
    const trend = (rng() - 0.48) * 0.15;
    const newDemand = Math.min(2.2, Math.max(0.6, Number((it.demand + trend).toFixed(3))));
    const newPrice = Math.max(1, Math.round(it.originalPrice * newDemand));
    return {
      ...it,
      demand: newDemand,
      price: newPrice,
      stock: Math.min(it.maxStock, it.stock + Math.floor(rng() * 4)),
    };
  });
}

export interface DailySimulationResult {
  resources: SectResources;
  disciples: Disciple[];
  marketListings: MarketItem[];
  randomEncounter: string | null;
  bondEvent: string | null;
}

const DAILY_ENCOUNTERS = [
  'A traveling Daoist merchant halted at the mountain gate, offering spirit wares.',
  'Spiritual mist condensed over the Lotus Pond, accelerating disciple Qi absorption.',
  'Wild spirit beasts were repelled by the outer mountain ward.',
  'Disciples achieved mutual dao enlightenment during morning sword practice.',
];

export function simulateDailyTick(params: {
  resources: SectResources;
  disciples: Disciple[];
  buildings: Building[];
  marketListings: MarketItem[];
  ascensionStats: AscensionStats;
  rng?: () => number;
}): DailySimulationResult {
  const rng = params.rng ?? Math.random;
  const bondPass = processDailyBonds(params.disciples, rng);
  const bondedDisciples = bondPass.disciples;

  const updatedDisciples = bondedDisciples.map(d => {
    const bondBonus = getActiveBondBonusForDisciple(d, bondedDisciples);
    const taskEfficiency = calculateDiscipleEfficiency(d, d.currentTask, bondedDisciples);
    const qiGain = d.currentTask === 4 ? 40 + Math.floor(d.stats.intel / 4) + bondBonus.qiBonus : 0;
    const nextEnergy = clamp(d.energy + (d.currentTask === 0 ? 15 : -5), 10, d.maxEnergy);
    return {
      ...d,
      taskEfficiency,
      energy: nextEnergy,
      realmExp: Math.min(2000, d.realmExp + qiGain),
    };
  });

  const econ = calculateDailyEconomySummary(
    updatedDisciples,
    params.buildings,
    params.ascensionStats.incomeMultiplier
  );

  const cultCount = updatedDisciples.filter(d => d.currentTask === 4).length;

  const updatedResources: SectResources = {
    ...params.resources,
    spiritStones: Math.max(0, params.resources.spiritStones + econ.netDaily),
    sectExp: params.resources.sectExp + 10 + cultCount * 2,
    sectRealmExp: Math.min(1000, params.resources.sectRealmExp + cultCount * 3),
    sectPower: calculateTotalPower(updatedDisciples, params.ascensionStats.combatMultiplier),
  };

  const updatedMarket = advanceMarketListings(params.marketListings, rng);
  const randomEncounter =
    rng() < 0.25 ? DAILY_ENCOUNTERS[Math.floor(rng() * DAILY_ENCOUNTERS.length)] : null;

  return {
    resources: updatedResources,
    disciples: updatedDisciples,
    marketListings: updatedMarket,
    randomEncounter,
    bondEvent: bondPass.bondEvent,
  };
}

export function stepSectHour(prev: GameTime): { nextTime: GameTime; dayPassed: boolean } {
  let hour = prev.hour + 1;
  let day = prev.day;
  let month = prev.month;
  let year = prev.year;
  let season = prev.season;
  let dayPassed = false;

  if (hour >= 24) {
    hour = 0;
    day += 1;
    dayPassed = true;
  }
  if (day > 30) {
    day = 1;
    month += 1;
    season = (season + 1) % 4;
  }
  if (month > 12) {
    month = 1;
    year += 1;
  }

  return {
    nextTime: { year, month, day, hour, season },
    dayPassed,
  };
}

export function applyOfflineHours(
  state: PersistedSectState,
  elapsedMs: number,
  tickIntervalMs = 1500
): { state: PersistedSectState; hoursAdvanced: number; daysAdvanced: number } {
  const rawHours = Math.floor(elapsedMs / tickIntervalMs);
  const hoursAdvanced = clamp(rawHours, 0, MAX_OFFLINE_TICKS);
  if (hoursAdvanced <= 0) {
    return { state, hoursAdvanced: 0, daysAdvanced: 0 };
  }

  let time = state.time;
  let resources = state.resources;
  let disciples = state.disciples;
  let marketListings = state.marketListings;
  let dailyMissions = state.dailyMissions;
  let dailyMissionDayKey = state.dailyMissionDayKey;
  let daysAdvanced = 0;

  for (let i = 0; i < hoursAdvanced; i++) {
    const step = stepSectHour(time);
    time = step.nextTime;
    if (step.dayPassed) {
      daysAdvanced += 1;
      const daily = simulateDailyTick({
        resources,
        disciples,
        buildings: state.buildings,
        marketListings,
        ascensionStats: state.ascensionStats,
      });
      resources = daily.resources;
      disciples = daily.disciples;
      marketListings = daily.marketListings;
      dailyMissions = advanceDailyMissions(dailyMissions, 'work_ticks', 1);
      const nextDayKey = formatDayKey(time);
      const synced = syncDailyMissionsForNewDay(dailyMissions, dailyMissionDayKey, nextDayKey);
      dailyMissions = synced.missions;
      dailyMissionDayKey = synced.dayKey;
    }
  }

  return {
    state: {
      ...state,
      time,
      resources,
      disciples,
      marketListings,
      dailyMissions,
      dailyMissionDayKey,
    },
    hoursAdvanced,
    daysAdvanced,
  };
}

export function calculatePendingAscension(sectPower: number, spiritStones: number, sectRealm: number): {
  pendingEssence: number;
  canAscend: boolean;
} {
  const essenceFromPower = Math.floor(Math.max(0, sectPower) / 1000);
  const essenceFromStones = Math.floor(Math.max(0, spiritStones) / 10000);
  const pendingEssence = Math.max(1, essenceFromPower + essenceFromStones);
  return {
    pendingEssence,
    canAscend: sectRealm >= 3 || pendingEssence >= 10,
  };
}

// ============================================================================
// ATB BATTLE PURE SIMULATION STEP (Shared & Deterministic)
// ============================================================================

export function createBattleRoster(
  playerDisciples: Disciple[],
  sectRealm: number,
  combatMultiplier = 1.0,
  rng: () => number = Math.random
): { units: BattleUnit[]; synergyMessages: string[] } {
  const sourcePlayers: Disciple[] =
    playerDisciples.length > 0
      ? playerDisciples.slice(0, 3)
      : [
          createDisciple({
            id: 'guardian_d',
            name: 'Sect Guardian',
            realm: sectRealm,
            element: 1,
            stats: { str: 28, agi: 22, intel: 20, lck: 15, vit: 25, wis: 18, cha: 16 },
            hp: 350,
            maxHp: 350,
            mp: 150,
            maxMp: 150,
            atk: 84,
            def: 55,
            spd: 45,
            critRate: 30,
            critDmg: 180,
          }),
        ];

  const mult = Math.max(1, combatMultiplier);
  const bondBattleNames: string[] = [];
  const teamPlayer: BattleUnit[] = sourcePlayers.map((d, idx) => {
    const battleBond = getBattleBondBonusForDisciple(d, sourcePlayers);
    if (battleBond.partnerNames.length > 0) {
      bondBattleNames.push(d.name);
    }
    return {
      id: `p_${idx}`,
      name: d.name,
      isPlayer: true,
      hp: d.hp || d.maxHp,
      maxHp: d.maxHp,
      mp: d.mp || d.maxMp,
      maxMp: d.maxMp,
      atk: Math.floor((d.atk + battleBond.atkBonus) * mult),
      def: Math.floor((d.def + battleBond.defBonus) * mult),
      spd: d.spd,
      critRate: d.critRate,
      critDmg: d.critDmg,
      element: d.element || 1,
      actionBar: 20 + idx * 10,
      maxActionBar: Math.max(100, 220 - d.spd),
      isAlive: true,
      discipleId: d.id,
      avatarSeed: idx,
    };
  });

  const enemyNames = ['Corrupted Mountain Fiend', 'Demonic Nether Wolf', 'Bloodfang Viper', 'Chaos Wraith'];
  const enemyCount = Math.min(3, 2 + Math.floor(sectRealm / 2));
  const teamEnemy: BattleUnit[] = [];

  for (let i = 0; i < enemyCount; i++) {
    const baseHp = 180 + sectRealm * 95 + i * 40;
    const baseAtk = 45 + sectRealm * 22 + i * 10;
    const baseDef = 28 + sectRealm * 14;
    const baseSpd = 35 + sectRealm * 10;
    teamEnemy.push({
      id: `e_${i}`,
      name: enemyNames[i % enemyNames.length],
      isPlayer: false,
      hp: baseHp,
      maxHp: baseHp,
      mp: 80,
      maxMp: 80,
      atk: baseAtk,
      def: baseDef,
      spd: baseSpd,
      critRate: 15,
      critDmg: 150,
      element: (i % 5) + 1,
      actionBar: Math.floor(rng() * 40),
      maxActionBar: Math.max(100, 220 - baseSpd),
      isAlive: true,
      avatarSeed: 10 + i,
    });
  }

  const synergyMessages: string[] = [
    `⚔️ Battle begins! Sect Vanguard faces ${enemyCount} mountain fiends!`,
  ];

  if (bondBattleNames.length > 0) {
    synergyMessages.push(
      `🤝 Sect Bond Synergy active for ${bondBattleNames.join(', ')} (+ATK/+DEF)!`
    );
  }

  const elemCounts: Record<number, number> = {};
  for (const u of teamPlayer) {
    elemCounts[u.element] = (elemCounts[u.element] || 0) + 1;
  }
  for (const [elemStr, count] of Object.entries(elemCounts)) {
    const elemId = Number(elemStr);
    if (count >= 2) {
      synergyMessages.push(
        `✨ Elemental Resonance Triggered! ${count} disciples resonate with ${ELEMENT_NAMES[elemId] ?? 'Dao'} (+15% ATK/DEF)!`
      );
      for (const u of teamPlayer) {
        if (u.element === elemId) {
          u.atk = Math.floor(u.atk * 1.15);
          u.def = Math.floor(u.def * 1.15);
        }
      }
    }
  }

  return { units: [...teamPlayer, ...teamEnemy], synergyMessages };
}

export function stepAtbBattle(
  units: BattleUnit[],
  rng: () => number = Math.random
): {
  units: BattleUnit[];
  logs: string[];
  soundEvent: 'strike' | 'crit' | null;
  acted: boolean;
  finished: boolean;
  playerWon: boolean;
} {
  const nextUnits = units.map(u => ({ ...u }));
  const logs: string[] = [];
  let soundEvent: 'strike' | 'crit' | null = null;
  let acted = false;

  for (const actor of nextUnits) {
    if (!actor.isAlive) continue;
    actor.actionBar = Math.min(actor.maxActionBar, actor.actionBar + Math.max(1, Math.floor(actor.spd / 4)));

    if (actor.actionBar >= actor.maxActionBar) {
      actor.actionBar = 0;
      acted = true;
      const opponents = nextUnits.filter(u => u.isPlayer !== actor.isPlayer && u.isAlive);
      if (opponents.length > 0) {
        const target = opponents[Math.floor(rng() * opponents.length)];
        let comboMult = 1.0;

        if (rng() < 0.28) {
          comboMult = 1.6;
          logs.push(`🥊 ${actor.name} unleashes 10-Hit Aerial Juggle Combo on ${target.name}!`);
        }
        if (actor.hp < actor.maxHp * 0.25 && rng() < 0.45) {
          comboMult = 2.4;
          logs.push(`⚡ DESPERATION QI CLASH! ${actor.name} burns dantian essence for a supreme heavenly burst!`);
          soundEvent = 'crit';
        }

        let rawDmg = actor.atk * comboMult;
        const isCrit = rng() * 100 < actor.critRate;
        if (isCrit) {
          rawDmg *= actor.critDmg / 100;
          soundEvent = 'crit';
        } else if (!soundEvent) {
          soundEvent = 'strike';
        }

        const mitigation = 100 / (100 + target.def);
        const finalDmg = Math.max(1, Math.floor(rawDmg * mitigation));
        target.hp = Math.max(0, target.hp - finalDmg);

        logs.push(
          isCrit
            ? `💥 CRITICAL STRIKE! ${actor.name} strikes ${target.name} for ${finalDmg} DMG!`
            : `${actor.name} attacks ${target.name} for ${finalDmg} DMG.`
        );

        if (target.hp <= 0) {
          target.isAlive = false;
          logs.push(`💀 HEAVENLY EXECUTION! ${actor.name} utterly vanquished ${target.name}!`);
        }
      }
      break;
    }
  }

  const playersAlive = nextUnits.some(u => u.isPlayer && u.isAlive);
  const enemiesAlive = nextUnits.some(u => !u.isPlayer && u.isAlive);
  const finished = !playersAlive || !enemiesAlive;

  return {
    units: nextUnits,
    logs,
    soundEvent,
    acted,
    finished,
    playerWon: playersAlive,
  };
}

// ============================================================================
// DAILY MISSIONS ENGINE (Randomized Spirit Stones & Sect EXP Rewards)
// ============================================================================

interface DailyMissionTemplate {
  kind: DailyMissionKind;
  title: string;
  description: string;
  target: number;
}

const DAILY_MISSION_POOL: DailyMissionTemplate[] = [
  {
    kind: 'cultivate_qi',
    title: 'Morning Qi Circulation',
    description: 'Channel mountain Qi or bestow Qi to disciples in the Cultivation Chamber.',
    target: 3,
  },
  {
    kind: 'build_or_upgrade',
    title: 'Expand Mountain Pavilions',
    description: 'Construct or upgrade a sect pavilion in the Spiritual Domain.',
    target: 1,
  },
  {
    kind: 'win_battle',
    title: 'Defend the Mountain Gate',
    description: 'Claim victory in ATB Battle, Sect War, or Tournament combat.',
    target: 1,
  },
  {
    kind: 'alchemy_craft',
    title: 'Cauldron Refinement',
    description: 'Attempt elixir refinement in the Alchemy Forge.',
    target: 1,
  },
  {
    kind: 'work_ticks',
    title: 'Steadfast Sect Stewardship',
    description: 'Guide your disciples through daily labor cycles.',
    target: 2,
  },
];

export function formatDayKey(time: Pick<GameTime, 'year' | 'month' | 'day'>): string {
  return `Y${time.year}-M${time.month}-D${time.day}`;
}

function createMissionFromTemplate(
  template: DailyMissionTemplate,
  dayKey: string,
  slotIdx: number,
  rng: () => number
): DailyMission {
  const rewardType: DailyMissionRewardType = rng() < 0.55 ? 'spiritStones' : 'sectExp';
  const rewardAmount =
    rewardType === 'spiritStones'
      ? 180 + Math.floor(rng() * 6) * 45 // 180 to 405 Spirit Stones
      : 120 + Math.floor(rng() * 5) * 40; // 120 to 280 Sect EXP

  return {
    id: `dm_${dayKey}_${slotIdx}_${template.kind}`,
    title: template.title,
    description: template.description,
    kind: template.kind,
    progress: 0,
    target: template.target,
    rewardType,
    rewardAmount,
    claimed: false,
  };
}

export function generateDailyMissions(
  dayKey: string,
  rng: () => number = Math.random
): DailyMission[] {
  const pool = [...DAILY_MISSION_POOL];
  for (let i = pool.length - 1; i > 0; i--) {
    const j = Math.floor(rng() * (i + 1));
    const tmp = pool[i];
    pool[i] = pool[j];
    pool[j] = tmp;
  }
  return pool.slice(0, 3).map((tpl, idx) => createMissionFromTemplate(tpl, dayKey, idx, rng));
}

export function advanceDailyMissions(
  missions: DailyMission[],
  kind: DailyMissionKind,
  amount = 1
): DailyMission[] {
  if (amount <= 0) return missions;
  let changed = false;
  const next = missions.map(m => {
    if (m.claimed || m.kind !== kind || m.progress >= m.target) return m;
    changed = true;
    return {
      ...m,
      progress: Math.min(m.target, m.progress + amount),
    };
  });
  return changed ? next : missions;
}

export function claimDailyMissionReward(
  state: PersistedSectState,
  missionId: string
): { nextState: PersistedSectState; claimed: DailyMission | null } {
  const target = state.dailyMissions.find(m => m.id === missionId);
  if (!target || target.claimed || target.progress < target.target) {
    return { nextState: state, claimed: null };
  }

  const nextMissions = state.dailyMissions.map(m =>
    m.id === missionId ? { ...m, claimed: true } : m
  );

  const nextResources: SectResources = {
    ...state.resources,
    spiritStones:
      target.rewardType === 'spiritStones'
        ? state.resources.spiritStones + target.rewardAmount
        : state.resources.spiritStones,
    sectExp:
      target.rewardType === 'sectExp'
        ? state.resources.sectExp + target.rewardAmount
        : state.resources.sectExp,
  };

  return {
    nextState: {
      ...state,
      resources: nextResources,
      dailyMissions: nextMissions,
    },
    claimed: { ...target, claimed: true },
  };
}

export function syncDailyMissionsForNewDay(
  missions: DailyMission[],
  currentDayKey: string,
  nextDayKey: string,
  rng: () => number = Math.random
): { missions: DailyMission[]; dayKey: string; refreshedCount: number } {
  if (currentDayKey === nextDayKey && missions.length === 3) {
    return { missions, dayKey: currentDayKey, refreshedCount: 0 };
  }

  // Keep in-progress/unclaimed missions intact while replenishing any claimed slots
  const freshPool = generateDailyMissions(nextDayKey, rng);
  let refreshedCount = 0;
  const usedKinds = new Set(missions.filter(m => !m.claimed).map(m => m.kind));

  const nextMissions = [0, 1, 2].map(idx => {
    const existing = missions[idx];
    if (existing && !existing.claimed) {
      return existing;
    }
    refreshedCount += 1;
    const unused = freshPool.find(c => !usedKinds.has(c.kind)) ?? freshPool[idx];
    usedKinds.add(unused.kind);
    return unused;
  });

  return { missions: nextMissions, dayKey: nextDayKey, refreshedCount };
}

// ============================================================================
// SINGLE-PASS SAVE & SANITIZATION (Zero Redundant JSON Parses)
// ============================================================================

export function createDefaultState(): PersistedSectState {
  const starterDisciples = createStarterDisciples();
  const r = SECT_DEFAULTS.resources;
  const t = SECT_DEFAULTS.initialTime;
  const dayKey = formatDayKey(t);
  return {
    version: SAVE_SCHEMA_VERSION,
    sectName: SECT_DEFAULTS.name,
    resources: {
      spiritStones: r.spiritStones,
      spiritHerbs: r.spiritHerbs,
      spiritOres: r.spiritOres,
      jade: r.jade,
      essence: r.essence,
      sectPower: calculateTotalPower(starterDisciples),
      sectExp: r.sectExp,
      sectRealm: r.sectRealm,
      sectRealmExp: r.sectRealmExp,
      sectRank: r.sectRank,
      maxDisciples: r.maxDisciples,
    },
    time: { ...t },
    disciples: starterDisciples,
    buildings: createInitialBuildings(),
    techTree: createInitialTechTree(),
    marketListings: createInitialMarket(),
    quests: createInitialQuests(),
    dailyMissions: generateDailyMissions(dayKey),
    dailyMissionDayKey: dayKey,
    ascensionStats: {
      totalAscensions: 0,
      ancestralEssence: 0,
      incomeMultiplier: 1.0,
      combatMultiplier: 1.0,
    },
    perfMode: 'auto',
    audioEnabled: true,
    bgmEnabled: false,
    lastSavedAt: Date.now(),
  };
}

export function loadPersistedSectState(rawJson?: string | null): PersistedSectState {
  const defaults = createDefaultState();
  const raw =
    rawJson !== undefined
      ? rawJson
      : typeof window !== 'undefined' && typeof window.localStorage !== 'undefined'
      ? window.localStorage.getItem(STORAGE_KEY)
      : null;

  if (!raw) return defaults;

  try {
    const parsed = JSON.parse(raw) as Partial<PersistedSectState> | null;
    if (!parsed || typeof parsed !== 'object') return defaults;

    const r = parsed.resources;
    const sanitizedResources: SectResources = r && typeof r === 'object'
      ? {
          spiritStones: Math.max(0, Number(r.spiritStones) || 0),
          spiritHerbs: Math.max(0, Number(r.spiritHerbs) || 0),
          spiritOres: Math.max(0, Number(r.spiritOres) || 0),
          jade: Math.max(0, Number(r.jade) || 0),
          essence: Math.max(0, Number(r.essence) || 0),
          sectPower: Math.max(0, Number(r.sectPower) || defaults.resources.sectPower),
          sectExp: Math.max(0, Number(r.sectExp) || 0),
          sectRealm: clamp(Math.floor(Number(r.sectRealm) || 0), 0, REALMS.length - 1),
          sectRealmExp: Math.max(0, Number(r.sectRealmExp) || 0),
          sectRank: Math.max(1, Math.floor(Number(r.sectRank) || 1)),
          maxDisciples: clamp(Math.floor(Number(r.maxDisciples) || 12), 3, 50),
        }
      : defaults.resources;

    const t = parsed.time;
    const sanitizedTime: GameTime = t && typeof t === 'object'
      ? {
          year: Math.max(1, Math.floor(Number(t.year) || 1)),
          month: clamp(Math.floor(Number(t.month) || 1), 1, 12),
          day: clamp(Math.floor(Number(t.day) || 1), 1, 30),
          hour: clamp(Math.floor(Number(t.hour) || 0), 0, 23),
          season: clamp(Math.floor(Number(t.season) || 0), 0, 3),
        }
      : defaults.time;

    const a = parsed.ascensionStats;
    const sanitizedAscension: AscensionStats = a && typeof a === 'object'
      ? {
          totalAscensions: Math.max(0, Math.floor(Number(a.totalAscensions) || 0)),
          ancestralEssence: Math.max(0, Math.floor(Number(a.ancestralEssence) || 0)),
          incomeMultiplier: Math.max(1.0, Number(a.incomeMultiplier) || 1.0),
          combatMultiplier: Math.max(1.0, Number(a.combatMultiplier) || 1.0),
        }
      : defaults.ascensionStats;

    const validDisciples =
      Array.isArray(parsed.disciples) && parsed.disciples.length > 0
        ? parsed.disciples.map(d => ({
            ...d,
            bonds: Array.isArray(d.bonds) ? d.bonds : [],
          }))
        : defaults.disciples;

    const validBuildings =
      Array.isArray(parsed.buildings) && parsed.buildings.length > 0
        ? parsed.buildings
        : defaults.buildings;

    const validTech =
      Array.isArray(parsed.techTree) && parsed.techTree.length > 0
        ? parsed.techTree
        : defaults.techTree;

    const validMarket =
      Array.isArray(parsed.marketListings) && parsed.marketListings.length > 0
        ? parsed.marketListings
        : defaults.marketListings;

    const validQuests =
      Array.isArray(parsed.quests) && parsed.quests.length > 0
        ? parsed.quests
        : defaults.quests;

    const currentDayKey = formatDayKey(sanitizedTime);
    const validDailyMissions =
      Array.isArray(parsed.dailyMissions) && parsed.dailyMissions.length > 0
        ? parsed.dailyMissions.map(m => ({
            ...m,
            progress: Math.max(0, Math.min(Number(m.target) || 1, Number(m.progress) || 0)),
            target: Math.max(1, Number(m.target) || 1),
            rewardAmount: Math.max(1, Number(m.rewardAmount) || 100),
            rewardType: m.rewardType === 'sectExp' ? ('sectExp' as const) : ('spiritStones' as const),
            claimed: Boolean(m.claimed),
          }))
        : generateDailyMissions(currentDayKey);

    const perfMode: PerformanceMode =
      parsed.perfMode === 'eco' || parsed.perfMode === 'balanced' || parsed.perfMode === 'ultra' || parsed.perfMode === 'auto'
        ? parsed.perfMode
        : 'auto';

    return {
      version: SAVE_SCHEMA_VERSION,
      sectName: typeof parsed.sectName === 'string' && parsed.sectName.trim() ? parsed.sectName.slice(0, 32) : defaults.sectName,
      resources: sanitizedResources,
      time: sanitizedTime,
      disciples: validDisciples,
      buildings: validBuildings,
      techTree: validTech,
      marketListings: validMarket,
      quests: validQuests,
      dailyMissions: validDailyMissions,
      dailyMissionDayKey:
        typeof parsed.dailyMissionDayKey === 'string' && parsed.dailyMissionDayKey
          ? parsed.dailyMissionDayKey
          : currentDayKey,
      ascensionStats: sanitizedAscension,
      perfMode,
      audioEnabled: typeof parsed.audioEnabled === 'boolean' ? parsed.audioEnabled : true,
      bgmEnabled: typeof parsed.bgmEnabled === 'boolean' ? parsed.bgmEnabled : false,
      lastSavedAt: typeof parsed.lastSavedAt === 'number' ? parsed.lastSavedAt : Date.now(),
    };
  } catch {
    return defaults;
  }
}

export function savePersistedSectState(state: PersistedSectState): void {
  if (typeof window === 'undefined' || typeof window.localStorage === 'undefined') return;
  try {
    const payload: PersistedSectState = {
      ...state,
      version: SAVE_SCHEMA_VERSION,
      lastSavedAt: Date.now(),
    };
    window.localStorage.setItem(STORAGE_KEY, JSON.stringify(payload));
  } catch {
    // Ignore storage quota errors gracefully
  }
}

export function formatEventLog(
  message: string,
  time: Pick<GameTime, 'year' | 'month' | 'day'>,
  type: SectEventLog['type'] = 'info'
): SectEventLog {
  return {
    id: `log_${Date.now()}_${Math.random().toString(36).slice(2, 6)}`,
    timestamp: `Y${time.year} M${time.month} D${time.day}`,
    message,
    type,
  };
}
