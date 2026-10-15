package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * FarmingSystem - Herb cultivation and harvest calculation.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class FarmingSystem {
    private FarmingSystem() {}

    public static class HarvestResult {
        public long herbs;
        public int quality;
        public long spiritStonesBonus;
        public int expGained;

        public HarvestResult(long herbs, int quality, long spiritStonesBonus, int expGained) {
            this.herbs = herbs;
            this.quality = quality;
            this.spiritStonesBonus = spiritStonesBonus;
            this.expGained = expGained;
        }
    }

    public static HarvestResult harvest(Disciple worker, float hours, float bonusMultiplier) {
        float baseYield = 10f * hours;
        float workerEfficiency = 1.0f;
        int quality = 1;

        SectData data = SectData.getInstance();
        float seasonMultiplier = 1.0f;
        if (data.time != null) {
            // Spring=1.2, Summer=1.5, Autumn=1.0, Winter=0.6
            int season = data.time.season;
            if (season == 0) seasonMultiplier = 1.2f;
            else if (season == 1) seasonMultiplier = 1.5f;
            else if (season == 2) seasonMultiplier = 1.0f;
            else if (season == 3) seasonMultiplier = 0.6f;
        }

        if (worker != null) {
            workerEfficiency = (worker.taskEfficiency / 100f) * (1.0f + worker.intel * 0.02f);
            if (RNG.chance(worker.lck * 2)) {
                quality = 2; // High quality herb
            }
        }

        long totalHerbs = (long)(baseYield * workerEfficiency * bonusMultiplier * seasonMultiplier);
        if (totalHerbs < 1) totalHerbs = 1;

        long stonesBonus = quality > 1 ? (totalHerbs * 2) : 0;
        int exp = (int)(totalHerbs * 3);

        if (worker != null) {
            worker.addExp(exp);
        }

        data.earn(stonesBonus, totalHerbs, 0);

        return new HarvestResult(totalHerbs, quality, stonesBonus, exp);
    }
}
