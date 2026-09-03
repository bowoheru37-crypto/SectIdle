package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * WarSystem - Manages sect territory conquests, defensive wars against rival sects,
 * and ancient demonic horde battles.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class WarSystem {
    private static volatile WarSystem instance;

    public static WarSystem getInstance() {
        if (instance == null) {
            synchronized (WarSystem.class) {
                if (instance == null) {
                    instance = new WarSystem();
                }
            }
        }
        return instance;
    }

    public static class WarResult {
        public boolean won;
        public int repChange;
        public int stoneReward;
        public int herbReward;
        public int oreReward;
        public int jadeReward;
        public String targetSectName;
        public String message;
    }

    public static final WarResult RESULT_POOL = new WarResult();

    public static final String[] RIVAL_SECTS = {
        "Blood Demon Sect",
        "Nine Nether Palace",
        "Celestial Sword Sect",
        "Thunder Cloud Pavilion",
        "Asura Valley",
        "Myriad Beast Manor"
    };

    public WarResult launchSectCampaign(SectData s, int difficulty) {
        if (s == null) {
            RESULT_POOL.won = false;
            RESULT_POOL.message = "No sect data.";
            return RESULT_POOL;
        }

        String rival = RIVAL_SECTS[RNG.nextInt(RIVAL_SECTS.length)];
        RESULT_POOL.targetSectName = rival;

        int totalSectPower = 0;
        int fighterCount = 0;
        for (int i = 0; i < s.disciples.size(); i++) {
            Disciple d = s.disciples.get(i);
            if (d != null && d.isAlive()) {
                totalSectPower += (int) d.getPowerRating();
                fighterCount++;
            }
        }

        int enemyPower = 400 + (difficulty * 600) + (s.sectRealm * 300) + RNG.nextInt(-100, 200);
        boolean victory = totalSectPower >= enemyPower;
        RESULT_POOL.won = victory;

        if (victory) {
            RESULT_POOL.repChange = 50 + (difficulty * 25);
            RESULT_POOL.stoneReward = 500 * (difficulty + 1);
            RESULT_POOL.herbReward = 100 * (difficulty + 1);
            RESULT_POOL.oreReward = 50 * (difficulty + 1);
            RESULT_POOL.jadeReward = 15 * (difficulty + 1);

            s.earn(RESULT_POOL.stoneReward, RESULT_POOL.herbReward, RESULT_POOL.oreReward);
            s.jade += RESULT_POOL.jadeReward;
            s.sectRealmExp += 30 * (difficulty + 1);
            s.sectRank = Math.min(100, s.sectRank + 1);
            RESULT_POOL.message = "Crushing victory over " + rival + "! Plundered vast resources.";
        } else {
            RESULT_POOL.repChange = -15;
            RESULT_POOL.stoneReward = 0;
            RESULT_POOL.herbReward = 0;
            RESULT_POOL.oreReward = 0;
            RESULT_POOL.jadeReward = 0;
            RESULT_POOL.message = "Defeated by " + rival + ". Sect forces retreated to recuperate.";
        }

        // Apply battle outcomes to participating disciples
        for (int i = 0; i < s.disciples.size(); i++) {
            Disciple d = s.disciples.get(i);
            if (d != null && d.isAlive()) {
                d.reputation = Math.max(0, Math.min(10000, d.reputation + RESULT_POOL.repChange));
                d.totalBattles++;
                if (RESULT_POOL.won) {
                    d.battlesWon++;
                    d.totalBattlesWon++;
                    d.addExperience(80 * (difficulty + 1));
                } else {
                    d.addExperience(20);
                }
            }
        }

        s.recalculateEconomy();
        GameplayFeedbackDispatcher.getInstance().onWarCampaignCompleted(RESULT_POOL);

        // If strong victory, trigger critical leader strike effect
        if (victory && fighterCount > 0) {
            Disciple leader = s.disciples.get(0);
            if (leader != null) {
                int critDmg = (int) (leader.getPowerRating() * 3.5f + RNG.nextInt(100, 500));
                GameplayFeedbackDispatcher.getInstance().onCriticalStrike(leader.position.x, leader.position.y, leader.name, rival + " Patriarch", critDmg, RESULT_POOL.repChange, true);
            }
        }

        return RESULT_POOL;
    }
}
