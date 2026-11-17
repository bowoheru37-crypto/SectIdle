import coreSpec from '../../shared/sect_core_spec.json';
import type { Building, MarketItem, Quest, TechNode } from '../types/game';

export const CORE_SPEC = coreSpec;

export const SECT_DEFAULTS = coreSpec.sect;
export const REALMS: string[] = coreSpec.realms;
export const SEASONS: string[] = coreSpec.seasons;

export const TASK_NAMES: string[] = coreSpec.tasks.map(t => t.name);
export const TASK_COLORS: string[] = coreSpec.tasks.map(t => t.color);
export const TASK_BASE_INCOME: number[] = coreSpec.tasks.map(t => t.baseIncome);

export const PERSONALITY_NAMES: string[] = coreSpec.personalities;
export const TALENT_NAMES: string[] = coreSpec.talents;

export const ELEMENT_NAMES: string[] = coreSpec.elements.map(e => e.name);
export const ELEMENT_COLORS: string[] = coreSpec.elements.map(e => e.color);

export const NAME_POOL = coreSpec.namePool;
export const STARTER_DISCIPLES = coreSpec.starterDisciples;

export const RIVAL_SECTS = coreSpec.rivalSects;
export const ALCHEMY_RECIPES = coreSpec.alchemyRecipes;
export const CONSOLE_TARGETS = coreSpec.targets;

export const INITIAL_BUILDINGS: Building[] = coreSpec.buildings.map(b => ({ ...b }));
export const INITIAL_TECH_TREE: TechNode[] = coreSpec.techTree.map(t => ({ ...t }));
export const INITIAL_MARKET: MarketItem[] = coreSpec.market.map(m => ({ ...m }));
export const INITIAL_QUESTS: Quest[] = coreSpec.quests.map(q => ({ ...q }));

export function createInitialBuildings(): Building[] {
  return coreSpec.buildings.map(b => ({ ...b }));
}

export function createInitialMarket(): MarketItem[] {
  return coreSpec.market.map(m => ({ ...m }));
}

export function createInitialTechTree(): TechNode[] {
  return coreSpec.techTree.map(t => ({ ...t }));
}

export function createInitialQuests(): Quest[] {
  return coreSpec.quests.map(q => ({ ...q }));
}
