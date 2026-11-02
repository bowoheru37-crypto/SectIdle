export const REALMS = [
  'Mortal',
  'Qi Refining',
  'Foundation Establishment',
  'Core Formation',
  'Nascent Soul',
  'Soul Transformation',
  'Void Refinement',
  'Body Integration',
  'Tribulation Transcendence',
  'True Immortal',
  'Golden Immortal',
  'Primordial Immortal',
  'Dao Ancestor',
  'Heavenly Dao'
];

export const TASK_NAMES = [
  'Idle',
  'Farming',
  'Crafting',
  'Alchemy',
  'Cultivation',
  'Mining',
  'Training',
  'Guard',
  'Research',
  'Trading',
  'Exploring'
];

export const TASK_COLORS = [
  '#94a3b8', // Idle
  '#22c55e', // Farming
  '#f97316', // Crafting
  '#3b82f6', // Alchemy
  '#a855f7', // Cultivation
  '#854d0e', // Mining
  '#ef4444', // Training
  '#64748b', // Guard
  '#06b6d4', // Research
  '#eab308', // Trading
  '#ec4899'  // Exploring
];

export const TASK_BASE_INCOME = [0, 50, 80, 120, 0, 60, 0, 0, 0, 150, 30];

export const PERSONALITY_NAMES = [
  'None', 'Arrogant', 'Diligent', 'Mysterious', 'Merchant', 'Loyal',
  'Lazy', 'Cold-blooded', 'Fighter', 'Scholar', 'Orphan', 'Kind',
  'Ruthless', 'Wise', 'Curious', 'Proud'
];

export const TALENT_NAMES = [
  'None', 'Body Tempering', 'Mind Clarity', 'Spirit Resonance', 'Heavenly Luck', 'Dual Yin-Yang', 'Chaos Origin', 'Heaven Decree'
];

export const ELEMENT_NAMES = [
  'None', 'Fire', 'Water', 'Wood', 'Metal', 'Earth',
  'Light', 'Shadow', 'Wind', 'Ice', 'Lightning'
];

export const ELEMENT_COLORS = [
  '#94a3b8', '#f97316', '#38bdf8', '#22c55e', '#cbd5e1',
  '#a16207', '#fef08a', '#64748b', '#2dd4bf', '#0ea5e9', '#eab308'
];

export const SEASONS = ['Spring', 'Summer', 'Autumn', 'Winter'];

export const RIVAL_SECTS = [
  { name: 'Blood Demon Sect', desc: 'Bloodthirsty demonic cultivators wielding blood spears.', basePower: 450 },
  { name: 'Nine Nether Palace', desc: 'Ghostly apparitions and soul-reaping scythes.', basePower: 700 },
  { name: 'Celestial Sword Sect', desc: 'Upright sword immortals testing our resolve.', basePower: 1100 },
  { name: 'Thunder Cloud Pavilion', desc: 'Masters of lightning arrays and storm tribulation.', basePower: 1600 },
  { name: 'Asura Valley', desc: 'Fierce battle fiends with unbreakable golden bodies.', basePower: 2400 },
  { name: 'Myriad Beast Manor', desc: 'Tamer of primordial mythical beasts and dragons.', basePower: 3500 }
];

export const ALCHEMY_RECIPES = [
  { name: 'Qi Gathering Pill', herbCost: 20, baseRate: 85, exp: 25, desc: 'Accelerates Qi refinement for disciples.' },
  { name: 'Foundation Establishment Pill', herbCost: 50, baseRate: 75, exp: 50, desc: 'Stabilizes meridians for realm breakthrough.' },
  { name: 'Body Tempering Elixir', herbCost: 100, baseRate: 65, exp: 80, desc: 'Forges iron bones and increases HP & ATK.' },
  { name: 'Spirit Cleansing Pill', herbCost: 250, baseRate: 55, exp: 120, desc: 'Purges dantian impurities, restoring energy.' },
  { name: 'Nascent Soul Pill', herbCost: 600, baseRate: 45, exp: 200, desc: 'Nurtures nascent spiritual avatars.' },
  { name: 'Void Transcending Pill', herbCost: 1500, baseRate: 35, exp: 350, desc: 'Pierces spatial realms and voids.' },
  { name: 'Immortal Ascension Pellet', herbCost: 4000, baseRate: 25, exp: 600, desc: 'Legendary heavenly elixir granting supreme dao insight.' }
];

export const INITIAL_BUILDINGS = [
  { id: 0, type: 0, name: 'Main Sect Hall', level: 1, maxLevel: 10, cost: 500, upgradeCost: 400, workers: 0, maxWorkers: 5, efficiency: 1.2, isBuilt: true, posX: 350, posY: 200, icon: 'palace' },
  { id: 1, type: 1, name: 'Dao Library', level: 0, maxLevel: 10, cost: 300, upgradeCost: 300, workers: 0, maxWorkers: 4, efficiency: 1.0, isBuilt: false, posX: 180, posY: 180, icon: 'book' },
  { id: 2, type: 2, name: 'Alchemy Lab', level: 0, maxLevel: 10, cost: 400, upgradeCost: 400, workers: 0, maxWorkers: 3, efficiency: 1.0, isBuilt: false, posX: 520, posY: 180, icon: 'flame' },
  { id: 3, type: 3, name: 'Spirit Forge', level: 0, maxLevel: 10, cost: 450, upgradeCost: 450, workers: 0, maxWorkers: 4, efficiency: 1.0, isBuilt: false, posX: 620, posY: 320, icon: 'hammer' },
  { id: 4, type: 4, name: 'Spirit Herb Garden', level: 0, maxLevel: 10, cost: 200, upgradeCost: 200, workers: 0, maxWorkers: 6, efficiency: 1.0, isBuilt: false, posX: 140, posY: 340, icon: 'sprout' },
  { id: 5, type: 5, name: 'Spirit Stone Mine', level: 0, maxLevel: 10, cost: 350, upgradeCost: 350, workers: 0, maxWorkers: 6, efficiency: 1.0, isBuilt: false, posX: 260, posY: 420, icon: 'pickaxe' },
  { id: 6, type: 6, name: 'Sect Bazaar', level: 0, maxLevel: 10, cost: 500, upgradeCost: 500, workers: 0, maxWorkers: 4, efficiency: 1.0, isBuilt: false, posX: 460, posY: 420, icon: 'store' },
  { id: 7, type: 7, name: 'Martial Arena', level: 0, maxLevel: 10, cost: 600, upgradeCost: 600, workers: 0, maxWorkers: 8, efficiency: 1.0, isBuilt: false, posX: 360, posY: 330, icon: 'swords' }
];

export const INITIAL_TECH_TREE = [
  { id: 'tech_1', name: 'Spiritual Irrigation', era: 0, costExp: 400, isResearched: false, description: '+25% Spirit Herb harvest yield and +10% farming speed.' },
  { id: 'tech_2', name: 'Five-Element Crucible', era: 1, costExp: 1000, isResearched: false, description: '+30% Alchemy crafting speed & +15% pill quality chance.' },
  { id: 'tech_3', name: 'Grand Mountain Spirit Array', era: 2, costExp: 2500, isResearched: false, description: '+50% Sect Qi Density & disciples cultivate 50% faster.' },
  { id: 'tech_4', name: 'Heavenly Dao Resonance', era: 3, costExp: 6000, isResearched: false, description: 'Permanently increases sect realm breakthrough success rate by 25%.' },
  { id: 'tech_5', name: 'Automated Sword Defense', era: 2, costExp: 3500, isResearched: false, description: '+35% base combat stats for all disciples in sect wars.' }
];

export const INITIAL_MARKET = [
  { id: 'm_1', itemName: 'Low-Grade Spirit Grass', itemType: 1, rarity: 0, price: 15, originalPrice: 15, stock: 80, maxStock: 100, demand: 1.0 },
  { id: 'm_2', itemName: 'Nine-Yang Spirit Ore', itemType: 2, rarity: 1, price: 35, originalPrice: 35, stock: 50, maxStock: 60, demand: 1.1 },
  { id: 'm_3', itemName: 'Qi Condensation Pill', itemType: 3, rarity: 1, price: 120, originalPrice: 120, stock: 25, maxStock: 30, demand: 1.2 },
  { id: 'm_4', itemName: 'Iron Flying Sword', itemType: 4, rarity: 2, price: 450, originalPrice: 450, stock: 8, maxStock: 10, demand: 0.95 },
  { id: 'm_5', itemName: 'Celestial Jade Pendant', itemType: 5, rarity: 3, price: 900, originalPrice: 900, stock: 4, maxStock: 5, demand: 1.4 },
  { id: 'm_6', itemName: 'Iron-Wood Timber', itemType: 6, rarity: 0, price: 20, originalPrice: 20, stock: 120, maxStock: 150, demand: 0.8 }
];

export const INITIAL_QUESTS = [
  { id: 'q_1', title: 'Cultivate 3 Disciples', description: 'Recruit or assign at least 3 disciples to tasks.', rewardSS: 400, rewardJade: 15, progress: 1, target: 3, completed: false, type: 0 },
  { id: 'q_2', title: 'Conquer Demonic Incursion', description: 'Win a battle against corrupted mountain fiends.', rewardSS: 600, rewardJade: 25, progress: 0, target: 1, completed: false, type: 1 },
  { id: 'q_3', title: 'Harvest 100 Spirit Herbs', description: 'Cultivate or gather 100 herbs in the garden.', rewardSS: 500, rewardJade: 20, progress: 0, target: 100, completed: false, type: 2 },
  { id: 'q_4', title: 'Breakthrough to Qi Refining', description: 'Advance at least one disciple to Qi Refining realm.', rewardSS: 800, rewardJade: 40, progress: 0, target: 1, completed: false, type: 3 }
];
