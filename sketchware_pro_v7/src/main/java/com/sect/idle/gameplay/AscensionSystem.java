package com.sect.idle.gameplay;

import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;

/**
 * AscensionSystem - Sect Reincarnation / Prestige System.
 * Resets sect progress while rewarding Ancestral Essence for permanent multiplier upgrades.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class AscensionSystem {
    private static AscensionSystem instance;

    public long totalAscensions = 0;
    public long ancestralEssence = 0;
    public float permanentIncomeMultiplier = 1.0f;
    public float permanentCombatMultiplier = 1.0f;

    public static synchronized AscensionSystem getInstance() {
        if (instance == null) {
            instance = new AscensionSystem();
        }
        return instance;
    }

    private AscensionSystem() {}

    public long calculatePendingEssence() {
        SectData data = SectData.getInstance();
        long totalPower = data.sectPower;
        long totalStones = data.spiritStones;
        long essenceFromPower = totalPower / 1000;
        long essenceFromStones = totalStones / 10000;
        return essenceFromPower + essenceFromStones;
    }

    public boolean canAscend() {
        SectData data = SectData.getInstance();
        return data.sectRealm >= 3 || calculatePendingEssence() >= 10;
    }

    public boolean performAscension() {
        if (!canAscend()) return false;

        long earnedEssence = calculatePendingEssence();
        ancestralEssence += earnedEssence;
        totalAscensions++;

        // Recalculate permanent multipliers
        permanentIncomeMultiplier = 1.0f + (ancestralEssence * 0.05f); // +5% per essence
        permanentCombatMultiplier = 1.0f + (ancestralEssence * 0.03f); // +3% per essence

        // Preserve Jade, VIP, and Ascension stats, then reset SectData
        SectData data = SectData.getInstance();
        long savedJade = data.jade;

        data.reset();
        data.jade = savedJade;
        data.sectRealm = 0;

        // Apply permanent multipliers
        data.recalculateEconomy();
        return true;
    }
}
