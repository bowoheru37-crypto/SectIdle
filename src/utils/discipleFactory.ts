// Unified facade re-exporting from single-source sectEngine (zero duplicate logic)
export {
  createDisciple as generateRandomDisciple,
  getOptimalTaskForDisciple,
  calculateDiscipleEfficiency as calculateEfficiency,
  computeDiscipleCombatStats,
  calculateExpectedWage,
  getActiveBondBonusForDisciple,
  getBondLabel,
  forgeDiscipleBond,
} from '../core/sectEngine';
