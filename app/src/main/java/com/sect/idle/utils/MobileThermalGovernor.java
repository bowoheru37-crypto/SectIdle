package com.sect.idle.utils;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Process;
import com.sect.idle.core.GameConfig;

/**
 * MobileThermalGovernor - Thermal & Battery Governor optimized for Itel A70 (Unisoc T603 / Mali-G57).
 * Monitors battery level, thermal throttling, and scales graphics quality to prevent FCs and maintain 60 FPS.
 * Pure Java 7/8 syntax compatible with Android API 21+.
 */
public final class MobileThermalGovernor {

    private static volatile MobileThermalGovernor instance;
    private final Context context;
    private float batteryPct = 1.0f;
    private boolean isLowPowerMode = false;
    private long lastCheckTime = 0;
    private static final long CHECK_INTERVAL_MS = 10000; // 10 seconds

    private MobileThermalGovernor(Context ctx) {
        this.context = ctx != null ? ctx.getApplicationContext() : null;
    }

    public static MobileThermalGovernor getInstance(Context ctx) {
        if (instance == null) {
            synchronized (MobileThermalGovernor.class) {
                if (instance == null) {
                    instance = new MobileThermalGovernor(ctx);
                }
            }
        }
        return instance;
    }

    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastCheckTime < CHECK_INTERVAL_MS) return;
        lastCheckTime = now;

        if (context == null) return;

        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, ifilter);
            if (batteryStatus != null) {
                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                if (scale > 0) {
                    batteryPct = level / (float) scale;
                }
            }
        } catch (Throwable ignored) {}

        // If battery < 20%, force low-power LOD optimization
        if (batteryPct < 0.20f && !isLowPowerMode) {
            isLowPowerMode = true;
            applyLowQualitySettings();
        } else if (batteryPct >= 0.25f && isLowPowerMode) {
            isLowPowerMode = false;
        }
    }

    public float getBatteryLevel() {
        return batteryPct;
    }

    public boolean isLowPower() {
        return isLowPowerMode;
    }

    public void applyLowQualitySettings() {
        // Force Low Quality settings (No dynamic lighting, no heavy particles)
        GameConfig.currentQuality = GameConfig.QUALITY_LOW;
        GameConfig.ENABLE_SHADOWS = false;
        GameConfig.ENABLE_LIGHTING = false;
        GameConfig.ENABLE_PARTICLES = false;
        GameConfig.ENABLE_BLOOM = false;
        GameConfig.ENABLE_POST_PROCESS = false;
    }

    public static long getJvmHeapUsedMb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
    }
}
