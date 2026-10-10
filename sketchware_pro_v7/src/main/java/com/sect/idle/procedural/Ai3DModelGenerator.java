package com.sect.idle.procedural;

import android.graphics.Bitmap;
import com.sect.idle.core.GameConfig;
import java.util.HashMap;
import java.util.Map;

/**
 * Ai3DModelGenerator - AI 3D Model Asset Generation System.
 * Synthesizes 3D isometric models for sect buildings, disciples, weapons, and pets
 * using procedural AI rules and voxel mesh projection.
 * Standard pure Java implementation without lambdas.
 */
public final class Ai3DModelGenerator {
    private static final int MODEL_RES = 128; // 128x128 3D isometric asset sprite resolution
    private static final Map<String, Bitmap> modelCache = new HashMap<String, Bitmap>();
    private static final int MAX_CACHE_SIZE = 32; // Keeps RAM usage under 1GB limits

    private Ai3DModelGenerator() {}

    public static Bitmap generateBuilding3D(int type, int level, int element) {
        String cacheKey = "b3d_" + type + "_" + level + "_" + element;
        if (modelCache.containsKey(cacheKey)) {
            Bitmap cached = modelCache.get(cacheKey);
            if (cached != null && !cached.isRecycled()) return cached;
        }

        VoxelMeshBuilder builder = new VoxelMeshBuilder(16, 16, 16);
        builder.clear();

        int wallColor = 0xFF5D4037;
        int roofColor = 0xFFD32F2F;
        int accentColor = GameConfig.getElementColor(element);

        // 1. Base Foundation Plinth
        builder.addBox(1, 1, 0, 14, 14, 1, 0xFF424242);

        // 2. Main Pavilion Walls
        builder.addBox(3, 3, 2, 12, 12, 7, wallColor);

        // 3. Main Entrance Doorway
        builder.addBox(6, 2, 2, 9, 3, 5, 0xFF212121);

        // 4. Eave Roofs (Multi-tier based on level)
        builder.addBox(2, 2, 8, 13, 13, 9, roofColor);
        if (level >= 3) {
            builder.addBox(4, 4, 10, 11, 11, 12, wallColor);
            builder.addBox(3, 3, 13, 12, 12, 14, roofColor);
            builder.addBox(7, 7, 15, 8, 8, 15, accentColor);
        } else {
            builder.addBox(7, 7, 10, 8, 8, 11, accentColor);
        }

        Bitmap result = builder.projectToIsometricBitmap(MODEL_RES, MODEL_RES);
        cacheBitmap(cacheKey, result);
        return result;
    }

    public static Bitmap generateDisciple3D(int realm, int element, boolean isMale) {
        String cacheKey = "d3d_" + realm + "_" + element + "_" + (isMale ? "m" : "f");
        if (modelCache.containsKey(cacheKey)) {
            Bitmap cached = modelCache.get(cacheKey);
            if (cached != null && !cached.isRecycled()) return cached;
        }

        VoxelMeshBuilder builder = new VoxelMeshBuilder(12, 12, 16);
        builder.clear();

        int elemColor = GameConfig.getElementColor(element);
        int robeColor = isMale ? 0xFF1976D2 : 0xFFC2185B;
        if (realm >= 8) robeColor = 0xFFFFD700;
        else if (realm >= 5) robeColor = 0xFF7B1FA2;

        // 1. Lower Robe / Legs
        builder.addBox(4, 4, 0, 7, 7, 5, robeColor);

        // 2. Torso / Sash
        builder.addBox(3, 3, 6, 8, 8, 10, robeColor);
        builder.addBox(3, 3, 6, 8, 8, 7, elemColor); // Elemental belt

        // 3. Head & Hair Crown
        builder.addBox(4, 4, 11, 7, 7, 13, 0xFFFFE0B2); // Skin
        builder.addBox(4, 4, 14, 7, 7, 15, isMale ? 0xFF212121 : 0xFF3E2723); // Hair

        // 4. Flying Sword floating at back
        if (realm >= 3) {
            builder.addBox(2, 5, 4, 2, 6, 14, 0xFFCFD8DC);
            builder.addBox(2, 4, 3, 2, 7, 4, 0xFFFFD700);
        }

        Bitmap result = builder.projectToIsometricBitmap(MODEL_RES, MODEL_RES);
        cacheBitmap(cacheKey, result);
        return result;
    }

    private static void cacheBitmap(String key, Bitmap bmp) {
        if (modelCache.size() >= MAX_CACHE_SIZE) {
            String firstKey = modelCache.keySet().iterator().next();
            modelCache.remove(firstKey);
        }
        modelCache.put(key, bmp);
    }

    public static void clearCache() {
        for (Bitmap bmp : modelCache.values()) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
            }
        }
        modelCache.clear();
    }
}
