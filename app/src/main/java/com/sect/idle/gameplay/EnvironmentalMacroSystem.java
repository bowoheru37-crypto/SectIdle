package com.sect.idle.gameplay;

import com.sect.idle.core.GameTime;
import com.sect.idle.systems.RNG;

/**
 * EnvironmentalMacroSystem - Macro/Micro Environmental Simulation Engine.
 * Dynamically updates weather, seasons, and spirit aura density, modifying disciple cultivation efficiency,
 * alchemy yields, farming outputs, and market prices.
 * Standard pure Java implementation without lambdas.
 */
public final class EnvironmentalMacroSystem {
    public static final int WEATHER_CLEAR        = 0;
    public static final int WEATHER_RAIN         = 1;
    public static final int WEATHER_SPIRIT_MIST  = 2;
    public static final int WEATHER_LOTUS_STORM  = 3;

    public static final int SEASON_SPRING = 0;
    public static final int SEASON_SUMMER = 1;
    public static final int SEASON_AUTUMN = 2;
    public static final int SEASON_WINTER = 3;

    private int currentWeather = WEATHER_CLEAR;
    private int currentSeason = SEASON_SPRING;
    private float auraDensity = 1.0f; // Multiplier 0.5x to 3.0x
    private float updateTimer = 0f;

    public EnvironmentalMacroSystem() {}

    public void update(float dt, GameTime time, SectData data) {
        updateTimer += dt;
        if (updateTimer < 5.0f) return; // Update macro environment every 5 seconds
        updateTimer = 0f;

        // Season calculation based on game month
        if (time != null) {
            int month = time.month;
            if (month >= 3 && month <= 5) currentSeason = SEASON_SPRING;
            else if (month >= 6 && month <= 8) currentSeason = SEASON_SUMMER;
            else if (month >= 9 && month <= 11) currentSeason = SEASON_AUTUMN;
            else currentSeason = SEASON_WINTER;
        }

        // Random weather transition
        if (RNG.chance(15)) {
            currentWeather = RNG.nextInt(4);
        }

        // Aura density fluctuation
        switch (currentWeather) {
            case WEATHER_SPIRIT_MIST:
                auraDensity = 2.2f;
                break;
            case WEATHER_LOTUS_STORM:
                auraDensity = 1.8f;
                break;
            case WEATHER_RAIN:
                auraDensity = 1.2f;
                break;
            default:
                auraDensity = 1.0f;
                break;
        }

        if (currentSeason == SEASON_SPRING) auraDensity *= 1.25f;
        if (currentSeason == SEASON_WINTER) auraDensity *= 0.75f;
    }

    public float getCultivationSpeedMultiplier() {
        return auraDensity;
    }

    public float getFarmingYieldMultiplier() {
        if (currentWeather == WEATHER_RAIN) return 1.5f;
        if (currentSeason == SEASON_AUTUMN) return 1.4f;
        if (currentSeason == SEASON_WINTER) return 0.5f;
        return 1.0f;
    }

    public float getAlchemySuccessBonus() {
        if (currentWeather == WEATHER_SPIRIT_MIST) return 0.25f; // +25% success
        return 0f;
    }

    public int getCurrentWeather() {
        return currentWeather;
    }

    public int getCurrentSeason() {
        return currentSeason;
    }

    public float getAuraDensity() {
        return auraDensity;
    }
}
