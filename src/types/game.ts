export interface SectResources {
  spiritStones: number;
  spiritHerbs: number;
  spiritOres: number;
  jade: number;
  essence: number;
  sectPower: number;
  sectExp: number;
  sectRealm: number; // 0 to 13
  sectRealmExp: number;
  sectRank: number;
  maxDisciples: number;
}

export interface GameTime {
  year: number;
  month: number;
  day: number;
  hour: number;
  season: number; // 0: Spring, 1: Summer, 2: Autumn, 3: Winter
}

export interface DiscipleStats {
  str: number;
  agi: number;
  intel: number;
  lck: number;
  vit: number;
  wis: number;
  cha: number;
}

export interface Fairy {
  name: string;
  element: string;
  level: number;
  exp: number;
  bond: number;
  maxBond: number;
  efficiencyBonus: number;
  cultivationBonus: number;
  combatBonus: number;
  luckBonus: number;
}

export interface Equipment {
  id: string;
  name: string;
  type: 'weapon' | 'armor' | 'accessory' | 'artifact';
  atk: number;
  def: number;
  spd: number;
  crit: number;
  rarity: number;
}

export interface Disciple {
  id: string;
  name: string;
  title?: string;
  isMale: boolean;
  age: number;
  lifespan: number;
  level: number;
  exp: number;
  maxExp: number;
  stats: DiscipleStats;
  realm: number;
  realmExp: number;
  element: number;
  personality: number;
  talentType: number;
  talentGrade: number;
  talentName: string;
  mood: number;
  stress: number;
  energy: number;
  maxEnergy: number;
  loyalty: number;
  reputation: number;
  relationshipSect: number;
  dailyWage: number;
  currentTask: number;
  taskEfficiency: number;
  taskLevel: number;
  hp: number;
  maxHp: number;
  mp: number;
  maxMp: number;
  atk: number;
  def: number;
  spd: number;
  critRate: number;
  critDmg: number;
  dodge: number;
  accuracy: number;
  hobby: number;
  fairy?: Fairy;
  weapon?: Equipment;
  armor?: Equipment;
  accessory?: Equipment;
  artifact?: Equipment;
  totalBattles: number;
  battlesWon: number;
  kills: number;
  tournamentsWon: number;
  posX: number;
  posY: number;
}

export interface Building {
  id: number;
  type: number;
  name: string;
  level: number;
  maxLevel: number;
  cost: number;
  upgradeCost: number;
  workers: number;
  maxWorkers: number;
  efficiency: number;
  isBuilt: boolean;
  posX: number;
  posY: number;
  icon: string;
}

export interface MarketItem {
  id: string;
  itemName: string;
  itemType: number;
  rarity: number;
  price: number;
  originalPrice: number;
  stock: number;
  maxStock: number;
  demand: number; // 0.5 to 2.5
}

export interface TechNode {
  id: string;
  name: string;
  era: number;
  costExp: number;
  isResearched: boolean;
  description: string;
}

export interface BattleUnit {
  id: string;
  name: string;
  isPlayer: boolean;
  hp: number;
  maxHp: number;
  mp: number;
  maxMp: number;
  atk: number;
  def: number;
  spd: number;
  critRate: number;
  critDmg: number;
  element: number;
  actionBar: number;
  maxActionBar: number;
  isAlive: boolean;
  discipleId?: string;
  avatarSeed: number;
}

export interface Quest {
  id: string;
  title: string;
  description: string;
  rewardSS: number;
  rewardJade: number;
  progress: number;
  target: number;
  completed: boolean;
  type: number;
}

export interface SectEventLog {
  id: string;
  timestamp: string;
  message: string;
  type: 'info' | 'reward' | 'battle' | 'warning' | 'breakthrough';
}
