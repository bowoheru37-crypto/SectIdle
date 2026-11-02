import { Disciple } from '../types/game';
import { PERSONALITY_NAMES, TALENT_NAMES, ELEMENT_NAMES, ELEMENT_COLORS } from '../constants/gameData';

const FIRST_NAMES = ['Li', 'Zhang', 'Wang', 'Chen', 'Liu', 'Zhao', 'Sun', 'Wu', 'Huang', 'Xiao', 'Lin', 'Ye', 'Chu', 'Feng', 'Han'];
const GIVEN_NAMES = ['Yun', 'Wei', 'Fang', 'Ming', 'Hua', 'Kai', 'Mei', 'Jian', 'Long', 'Feng', 'Tian', 'Xue', 'Chen', 'Bao', 'Hao'];

export function generateRandomDisciple(overrides?: Partial<Disciple>): Disciple {
  const isMale = Math.random() > 0.45;
  const firstName = FIRST_NAMES[Math.floor(Math.random() * FIRST_NAMES.length)];
  const givenName = GIVEN_NAMES[Math.floor(Math.random() * GIVEN_NAMES.length)];
  const name = `${firstName} ${givenName}`;

  const str = 10 + Math.floor(Math.random() * 32);
  const agi = 10 + Math.floor(Math.random() * 32);
  const intel = 10 + Math.floor(Math.random() * 32);
  const lck = 5 + Math.floor(Math.random() * 25);
  const vit = 10 + Math.floor(Math.random() * 30);
  const wis = 10 + Math.floor(Math.random() * 28);
  const cha = 5 + Math.floor(Math.random() * 25);

  const element = 1 + Math.floor(Math.random() * (ELEMENT_NAMES.length - 1));
  const personality = 1 + Math.floor(Math.random() * (PERSONALITY_NAMES.length - 1));
  const talentType = 1 + Math.floor(Math.random() * (TALENT_NAMES.length - 1));
  const talentGrade = 1 + Math.floor(Math.random() * 5);
  const talentName = `${TALENT_NAMES[talentType]} (Grade ${talentGrade})`;

  const realm = 0; // Mortal
  const realmExp = 0;
  const level = 1;

  // Combat calculation
  const maxHp = 100 + vit * 10 + str * 5;
  const maxMp = 50 + intel * 8 + wis * 5;
  const atk = str * 3 + agi * 1;
  const def = vit * 2 + str * 1;
  const spd = agi * 2;
  const critRate = lck * 2;
  const critDmg = 150 + str;
  const dodge = agi + lck;
  const accuracy = wis * 2 + agi;

  const baseWage = 40 + realm * 25 + Math.floor((str + agi + intel + lck + vit + wis + cha) / 7) + talentGrade * 8;

  const id = `d_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;

  return {
    id,
    name,
    isMale,
    age: 16 + Math.floor(Math.random() * 12),
    lifespan: 90 + Math.floor(Math.random() * 50),
    level,
    exp: 0,
    maxExp: 100,
    stats: { str, agi, intel, lck, vit, wis, cha },
    realm,
    realmExp,
    element,
    personality,
    talentType,
    talentGrade,
    talentName,
    mood: 75,
    stress: 10,
    energy: 100,
    maxEnergy: 100,
    loyalty: 60 + Math.floor(Math.random() * 30),
    reputation: 0,
    relationshipSect: 60,
    dailyWage: baseWage,
    currentTask: 0, // Idle initially
    taskEfficiency: 60,
    taskLevel: 1,
    hp: maxHp,
    maxHp,
    mp: maxMp,
    maxMp,
    atk,
    def,
    spd,
    critRate,
    critDmg,
    dodge,
    accuracy,
    hobby: Math.floor(Math.random() * 8),
    totalBattles: 0,
    battlesWon: 0,
    kills: 0,
    tournamentsWon: 0,
    posX: 250 + Math.random() * 300,
    posY: 200 + Math.random() * 200,
    ...overrides
  };
}

// Port CultivationAiEngine: autonomous optimal task assignment
export function getOptimalTaskForDisciple(d: Disciple): number {
  const highest = Math.max(d.stats.str, d.stats.intel, d.stats.vit, d.stats.agi, d.stats.cha);
  if (highest === d.stats.intel) {
    return d.stats.intel > 28 ? 3 : 8; // Alchemy : Research
  }
  if (highest === d.stats.str) {
    return 2; // Crafting
  }
  if (highest === d.stats.vit) {
    return 5; // Mining
  }
  if (highest === d.stats.agi) {
    return 10; // Exploring
  }
  if (highest === d.stats.cha) {
    return 9; // Trading
  }
  return 1; // Farming
}

export function calculateEfficiency(d: Disciple, task: number): number {
  if (task === 0) return 0;
  let base = 50;
  switch (task) {
    case 1: // Farming
      base += Math.floor(d.stats.str / 2 + d.stats.vit / 4);
      break;
    case 2: // Crafting
      base += Math.floor(d.stats.agi / 2 + d.stats.intel / 4);
      break;
    case 3: // Alchemy
      base += Math.floor(d.stats.intel / 2 + d.stats.wis / 4);
      break;
    case 4: // Cultivation
      base += Math.floor((d.stats.intel + d.stats.wis) / 4);
      break;
    case 5: // Mining
      base += Math.floor(d.stats.str / 2 + d.stats.vit / 3);
      break;
    case 6: // Training
      base += Math.floor(d.stats.str / 3 + d.stats.agi / 3 + d.stats.vit / 3);
      break;
    case 7: // Guard
      base += Math.floor(d.stats.str / 3 + d.stats.vit / 3 + d.stats.cha / 4);
      break;
    case 8: // Research
      base += Math.floor(d.stats.intel / 2 + d.stats.wis / 3);
      break;
    case 9: // Trading
      base += Math.floor(d.stats.cha / 2 + d.stats.lck / 3 + d.stats.intel / 4);
      break;
    case 10: // Exploring
      base += Math.floor(d.stats.agi / 2 + d.stats.lck / 3 + d.stats.wis / 4);
      break;
  }
  if (d.energy < 30) base -= 20;
  if (d.stress > 70) base -= 15;
  if (d.mood < 40) base -= 10;
  return Math.min(100, Math.max(15, base));
}
