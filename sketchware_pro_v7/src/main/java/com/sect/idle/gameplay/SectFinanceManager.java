package com.sect.idle.gameplay;

import com.sect.idle.systems.RNG;

/**
 * SectFinanceManager - Sect Financial Management & Macro Market System.
 * Manages sect treasury budget allocations, trade route revenue, auction house price index,
 * and commercial expansion.
 * Standard pure Java implementation without lambdas.
 */
public final class SectFinanceManager {
    public float cultivationSubsidyRatio = 0.25f;
    public float researchBudgetRatio     = 0.25f;
    public float defenseBudgetRatio      = 0.25f;
    public float treasuryReserveRatio    = 0.25f;

    public int activeTradeRoutes = 2;
    public float marketPriceIndex = 1.0f; // Multiplier on buy/sell items
    private float updateTimer = 0f;

    public SectFinanceManager() {}

    public void update(float dt, SectData data, EnvironmentalMacroSystem env) {
        updateTimer += dt;
        if (updateTimer < 10.0f) return; // Process finance tick every 10s
        updateTimer = 0f;

        if (data == null) return;

        // Dynamic market price index fluctuation based on season and trade routes
        float basePrice = 1.0f;
        if (env != null && env.getCurrentSeason() == EnvironmentalMacroSystem.SEASON_WINTER) {
            basePrice += 0.20f; // Price inflation in winter
        }
        basePrice += (activeTradeRoutes * 0.05f); // Trade route bonus
        basePrice += RNG.nextFloat(-0.05f, 0.05f);
        marketPriceIndex = Math.max(0.5f, Math.min(2.5f, basePrice));

        // Calculate passive trade route revenue
        long tradeRevenue = (long) (activeTradeRoutes * 120L * marketPriceIndex);
        data.spiritStones += tradeRevenue;
    }

    public void setBudgetAllocation(float cult, float res, float def, float resv) {
        float sum = cult + res + def + resv;
        if (sum <= 0.001f) return;
        cultivationSubsidyRatio = cult / sum;
        researchBudgetRatio = res / sum;
        defenseBudgetRatio = def / sum;
        treasuryReserveRatio = resv / sum;
    }

    public void addTradeRoute() {
        if (activeTradeRoutes < 10) {
            activeTradeRoutes++;
        }
    }

    public long getAdjustedItemPrice(long basePrice, boolean isBuying) {
        if (isBuying) {
            return (long) (basePrice * marketPriceIndex);
        } else {
            return (long) (basePrice * (marketPriceIndex * 0.85f));
        }
    }
}
