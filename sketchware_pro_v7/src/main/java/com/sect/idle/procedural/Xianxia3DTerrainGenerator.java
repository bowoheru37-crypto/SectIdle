package com.sect.idle.procedural;

import android.graphics.Color;
import com.sect.idle.ui.Ps23DGameView.Polygon3D;
import com.sect.idle.ui.Ps23DGameView.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Xianxia / Xuanhuan / Donghua 3D Immortal Cultivation World Terrain Generator.
 * Creates Floating Spirit Islands, Sword Peaks, Spirit Rivers, and Heavenly Shrines.
 * Pure Java (0 lambdas/streams) for Sketchware Pro v7.0.0 & Itel A70 (1GB RAM).
 */
public class Xianxia3DTerrainGenerator {

    public static List<Polygon3D> generateXianxiaWorld() {
        List<Polygon3D> polygons = new ArrayList<Polygon3D>();

        // 1. Main Sect Floating Immortal Island (Central Peak)
        for (int x = -150; x <= 150; x += 50) {
            for (int z = -150; z <= 150; z += 50) {
                float dist = (float) Math.sqrt(x * x + z * z);
                if (dist <= 160f) {
                    float height = (float) Math.cos(dist / 40.0) * 20f;
                    Vec3[] quadTop = new Vec3[] {
                        new Vec3(x, height, z),
                        new Vec3(x + 45, height, z),
                        new Vec3(x + 45, height, z + 45),
                        new Vec3(x, height, z + 45)
                    };
                    int greenCol = Color.rgb(34, 139, 34);
                    polygons.add(new Polygon3D(quadTop, greenCol));

                    // Underside Floating Cliff Rock (Tapering downwards into clouds)
                    Vec3[] cliffWall = new Vec3[] {
                        new Vec3(x, height, z + 45),
                        new Vec3(x + 45, height, z + 45),
                        new Vec3(x + 22, height - 80f, z + 22),
                        new Vec3(x + 22, height - 80f, z + 22)
                    };
                    int rockCol = Color.rgb(105, 105, 105);
                    polygons.add(new Polygon3D(cliffWall, rockCol));
                }
            }
        }

        // 2. Heavenly Sword Shrine Peak (Floating North Island)
        for (int x = -60; x <= 60; x += 40) {
            for (int z = 250; z <= 350; z += 40) {
                Vec3[] swordPeakQuad = new Vec3[] {
                    new Vec3(x, 40f, z),
                    new Vec3(x + 35, 40f, z),
                    new Vec3(x + 35, 40f, z + 35),
                    new Vec3(x, 40f, z + 35)
                };
                int goldCol = Color.rgb(218, 165, 32);
                polygons.add(new Polygon3D(swordPeakQuad, goldCol));
            }
        }

        // 3. Spirit Qi Cloud Fog Plane
        Vec3[] cloudPlane = new Vec3[] {
            new Vec3(-500, -120, -500),
            new Vec3(500, -120, -500),
            new Vec3(500, -120, 500),
            new Vec3(-500, -120, 500)
        };
        int cloudCol = Color.argb(120, 240, 248, 255);
        polygons.add(new Polygon3D(cloudPlane, cloudCol));

        return polygons;
    }
}
