import assert from 'node:assert/strict';
import test from 'node:test';
import {
  MAX_OFFLINE_TICKS,
  advanceDailyMissions,
  applyOfflineHours,
  calculateDiscipleEfficiency,
  calculateExpectedWage,
  calculatePendingAscension,
  claimDailyMissionReward,
  computeDiscipleCombatStats,
  createBattleRoster,
  createDefaultState,
  createDisciple,
  forgeDiscipleBond,
  getActiveBondBonusForDisciple,
  loadPersistedSectState,
  processDailyBonds,
  generateDailyMissions,
  simulateDailyTick,
  stepAtbBattle,
  stepSectHour,
  syncDailyMissionsForNewDay,
} from '../src/core/sectEngine.ts';
import coreSpec from '../shared/sect_core_spec.json' with { type: 'json' };

test('shared canonical spec contains consistent realms, tasks, targets, and starter disciples', () => {
  assert.ok(coreSpec.realms.length >= 10);
  assert.equal(coreSpec.tasks.length, 11);
  assert.ok(coreSpec.targets.length >= 10);
  assert.equal(coreSpec.starterDisciples.length, 3);
  assert.equal(coreSpec.buildings.length, 8);
});

test('computeDiscipleCombatStats & createDisciple match canonical Android Java formulas', () => {
  const d = createDisciple(
    {
      name: 'Lin Chen',
      realm: 2,
      personality: 1, // Arrogant (1.5x wage)
      talentGrade: 3,
      element: 3, // Wood (+15% Farming)
      currentTask: 1, // Farming
      stats: { str: 20, agi: 16, intel: 14, lck: 10, vit: 18, wis: 12, cha: 10 },
    },
    () => 0.5
  );

  const combat = computeDiscipleCombatStats(d.stats, 2, 1, false);
  assert.equal(d.maxHp, 100 + 18 * 10 + 20 * 5 + 2 * 50);
  assert.equal(d.atk, 20 * 3 + 16 + 2 * 15);
  assert.equal(d.def, 18 * 2 + 20 + 2 * 10);
  assert.equal(d.spd, 16 * 2 + 2 * 5);
  assert.equal(d.maxHp, combat.maxHp);

  const expectedWage = calculateExpectedWage(d.stats, 2, 1, 3);
  assert.equal(d.dailyWage, expectedWage);

  // Task 1 Farming: base 50 + str/2(10) + vit/4(4) + Wood bonus(15) = 79
  assert.equal(calculateDiscipleEfficiency(d, 1), 79);
  assert.equal(calculateDiscipleEfficiency(d, 0), 0);
});

test('stepSectHour and simulateDailyTick advance calendar and sect economy deterministically', () => {
  const state = createDefaultState();
  const h23 = stepSectHour({ year: 1, month: 1, day: 1, hour: 23, season: 0 });
  assert.equal(h23.dayPassed, true);
  assert.deepEqual(h23.nextTime, { year: 1, month: 1, day: 2, hour: 0, season: 0 });

  const monthRoll = stepSectHour({ year: 1, month: 12, day: 30, hour: 23, season: 3 });
  assert.equal(monthRoll.dayPassed, true);
  assert.deepEqual(monthRoll.nextTime, { year: 2, month: 1, day: 1, hour: 0, season: 0 });

  const daily = simulateDailyTick({
    resources: state.resources,
    disciples: state.disciples,
    buildings: state.buildings,
    marketListings: state.marketListings,
    ascensionStats: state.ascensionStats,
    rng: () => 0.5,
  });

  assert.ok(daily.resources.sectExp > state.resources.sectExp);
  assert.equal(daily.marketListings.length, state.marketListings.length);
  assert.equal(daily.randomEncounter, null);
});

test('applyOfflineHours caps catch-up ticks to MAX_OFFLINE_TICKS and advances resources safely', () => {
  const state = createDefaultState();
  const hugeElapsedMs = 1000 * 60 * 60 * 24;
  const res = applyOfflineHours(state, hugeElapsedMs, 1500);
  assert.equal(res.hoursAdvanced, MAX_OFFLINE_TICKS);
  assert.equal(res.daysAdvanced, 2);
  assert.ok(res.state.resources.sectExp > state.resources.sectExp);
});

test('loadPersistedSectState sanitizes corrupted inputs, bonds, dailyMissions, and ascensionStats', () => {
  const payload = JSON.stringify({
    sectName: 'Azure Peak',
    resources: { spiritStones: -500, sectRealm: 99, maxDisciples: 999 },
    time: { year: -3, month: 40, day: 0, hour: 99, season: 9 },
    ascensionStats: {
      totalAscensions: 4,
      ancestralEssence: 45,
      incomeMultiplier: 2.25,
      combatMultiplier: 1.9,
    },
    perfMode: 'eco',
  });

  const restored = loadPersistedSectState(payload);
  assert.equal(restored.sectName, 'Azure Peak');
  assert.equal(restored.resources.spiritStones, 0);
  assert.equal(restored.resources.sectRealm, coreSpec.realms.length - 1);
  assert.equal(restored.resources.maxDisciples, 50);
  assert.equal(restored.time.year, 1);
  assert.equal(restored.time.month, 12);
  assert.equal(restored.time.day, 1);
  assert.equal(restored.time.hour, 23);
  assert.equal(restored.ascensionStats.totalAscensions, 4);
  assert.equal(restored.ascensionStats.ancestralEssence, 45);
  assert.equal(restored.perfMode, 'eco');
  assert.equal(restored.dailyMissions.length, 3);
  assert.ok(Array.isArray(restored.disciples[0].bonds));
});

test('ATB battle engine runs to deterministic victory or defeat without hanging', () => {
  const strong = createDisciple({
    name: 'Elder',
    element: 1,
    hp: 900,
    maxHp: 900,
    atk: 160,
    def: 90,
    spd: 75,
  });
  const partner = createDisciple({
    name: 'Junior',
    element: 1,
    hp: 700,
    maxHp: 700,
    atk: 120,
    def: 70,
    spd: 65,
  });

  const roster = createBattleRoster([strong, partner], 0, 1.0, () => 0.4);
  assert.ok(roster.synergyMessages.some(m => m.includes('Elemental Resonance')));

  let units = roster.units;
  let finished = false;
  let playerWon = false;
  for (let i = 0; i < 400; i++) {
    const step = stepAtbBattle(units, () => 0.4);
    units = step.units;
    if (step.finished) {
      finished = true;
      playerWon = step.playerWon;
      break;
    }
  }
  assert.equal(finished, true);
  assert.equal(playerWon, true);
});

test('calculatePendingAscension matches Android AscensionSystem thresholds', () => {
  const low = calculatePendingAscension(1200, 5000, 0);
  assert.equal(low.pendingEssence, 1);
  assert.equal(low.canAscend, false);

  const highRealm = calculatePendingAscension(1500, 1000, 3);
  assert.equal(highRealm.canAscend, true);

  const highEssence = calculatePendingAscension(8000, 25000, 1);
  assert.equal(highEssence.pendingEssence, 10);
  assert.equal(highEssence.canAscend, true);
});

test('Disciple bonds (Sect Siblings & Master-Disciple) grant shared-task efficiency and combat bonuses', () => {
  const d1 = createDisciple({
    id: 'disc_a',
    name: 'Senior Brother',
    realm: 2,
    currentTask: 4, // Cultivation
    stats: { str: 16, agi: 14, intel: 20, lck: 10, vit: 16, wis: 16, cha: 10 },
  }, () => 0.5);

  const d2 = createDisciple({
    id: 'disc_b',
    name: 'Junior Sister',
    realm: 0,
    currentTask: 1, // Farming (different task initially)
    stats: { str: 14, agi: 14, intel: 16, lck: 10, vit: 14, wis: 12, cha: 10 },
  }, () => 0.5);

  // Forge bond (inferred master_disciple due to realm diff >= 1)
  const forged = forgeDiscipleBond([d1, d2], 'disc_a', 'disc_b');
  assert.equal(forged.bondType, 'master_disciple');

  const [bondedA, bondedB] = forged.disciples;
  // Working different tasks => 0 active co-task bonus
  assert.equal(getActiveBondBonusForDisciple(bondedA, forged.disciples).efficiencyBonus, 0);

  // Assign Junior Sister to Cultivation (task 4) alongside Senior Brother
  const aligned = [bondedA, { ...bondedB, currentTask: 4 }];
  const activeBonus = getActiveBondBonusForDisciple(aligned[0], aligned);
  assert.equal(activeBonus.efficiencyBonus, 8);
  assert.equal(activeBonus.qiBonus, 10);
  assert.equal(
    calculateDiscipleEfficiency(aligned[0], 4, aligned),
    calculateDiscipleEfficiency(d1, 4) + 8
  );

  // Daily bond processing deepens affinity & levels up bond
  let progressed = aligned;
  for (let i = 0; i < 6; i++) {
    progressed = processDailyBonds(progressed, () => 0.9).disciples;
  }
  assert.ok((progressed[0].bonds?.[0].level ?? 1) >= 2);

  // Battle synergy triggers when bonded partners enter battle together
  const battle = createBattleRoster(aligned, 0, 1.0, () => 0.5);
  assert.ok(battle.synergyMessages.some(m => m.includes('Sect Bond Synergy')));
});

test('Daily Missions generate randomized Spirit Stones or Sect EXP rewards, track progress, and claim cleanly', () => {
  const baseState = createDefaultState();
  const missions = generateDailyMissions('Y1-M1-D1', () => 0.2);
  assert.equal(missions.length, 3);
  assert.ok(missions.every(m => m.rewardType === 'spiritStones' || m.rewardType === 'sectExp'));
  assert.ok(missions.every(m => m.rewardAmount >= 120));

  const targetMission = {
    id: 'dm_test_1',
    title: 'Expand Mountain Pavilions',
    description: 'Construct or upgrade a sect pavilion.',
    kind: 'build_or_upgrade' as const,
    progress: 0,
    target: 1,
    rewardType: 'spiritStones' as const,
    rewardAmount: 315,
    claimed: false,
  };

  const progressed = advanceDailyMissions([targetMission], 'build_or_upgrade', 1);
  assert.equal(progressed[0].progress, 1);

  const stateWithMission = {
    ...baseState,
    dailyMissions: progressed,
  };

  const claimedRes = claimDailyMissionReward(stateWithMission, 'dm_test_1');
  assert.ok(claimedRes.claimed);
  assert.equal(claimedRes.claimed?.claimed, true);
  assert.equal(
    claimedRes.nextState.resources.spiritStones,
    baseState.resources.spiritStones + 315
  );

  // Claimed slots refresh automatically when a new day begins
  const synced = syncDailyMissionsForNewDay(
    claimedRes.nextState.dailyMissions,
    'Y1-M1-D1',
    'Y1-M1-D2',
    () => 0.7
  );
  assert.equal(synced.dayKey, 'Y1-M1-D2');
  assert.equal(synced.missions.length, 3);
  assert.equal(synced.missions[0].claimed, false);
});

