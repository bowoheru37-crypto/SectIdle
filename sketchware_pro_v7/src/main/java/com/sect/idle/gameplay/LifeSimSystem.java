package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * LifeSimSystem - The Sims inspired needs management (Stamina, Happiness, Social, Spirit Cleanse) & gifting.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class LifeSimSystem {
    private static LifeSimSystem instance;

    public static synchronized LifeSimSystem getInstance() {
        if (instance == null) {
            instance = new LifeSimSystem();
        }
        return instance;
    }

    private LifeSimSystem() {}

    public void updateDiscipleNeeds(Disciple d) {
        if (d == null || !d.isAlive()) return;

        // Energy/Stamina depletion based on task
        if (d.currentTask != 0) { // Working
            d.energy = Math.max(0, d.energy - 2);
            d.stress = Math.min(100, d.stress + 1);
        } else { // Idle / Resting
            d.energy = Math.min(d.maxEnergy, d.energy + 5);
            d.stress = Math.max(0, d.stress - 3);
        }

        // Mood calculation
        if (d.stress > 80 || d.energy < 20) {
            d.mood = Math.max(0, d.mood - 5);
        } else if (d.stress < 20 && d.energy > 70) {
            d.mood = Math.min(100, d.mood + 2);
        }

        // Loyalty decay or boost
        if (d.mood < 20) {
            d.loyalty = Math.max(0, d.loyalty - 1);
        } else if (d.mood > 80) {
            d.loyalty = Math.min(100, d.loyalty + 1);
        }
    }

    public boolean giveGift(Disciple d, String giftType) {
        if (d == null) return false;

        if ("spirit_wine".equals(giftType)) {
            d.mood = Math.min(100, d.mood + 20);
            d.loyalty = Math.min(100, d.loyalty + 5);
            return true;
        } else if ("spirit_herb".equals(giftType)) {
            d.energy = Math.min(d.maxEnergy, d.energy + 30);
            return true;
        } else if ("cleansing_talisman".equals(giftType)) {
            d.stress = Math.max(0, d.stress - 40);
            return true;
        }

        return false;
    }
}
