package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.systems.TileMap;

/**
 * TileSetEditor v1.0 - Utility for procedural tile manipulation and autotiling.
 */
public final class TileSetEditor {
    private final TileMap tileMap;

    public TileSetEditor(TileMap map) {
        this.tileMap = map;
    }

    public void generateTerrain(int seed) {
        if (tileMap == null) return;
        java.util.Random rand = new java.util.Random(seed);
        int w = tileMap.getWidth();
        int h = tileMap.getHeight();

        // Base ground
        tileMap.fillRect(0, 0, w, h, 1);

        // Procedural grass patches
        for (int i = 0; i < 20; i++) {
            int cx = rand.nextInt(w);
            int cy = rand.nextInt(h);
            int rad = 2 + rand.nextInt(4);
            tileMap.fillRect(cx - rad, cy - rad, rad * 2, rad * 2, 2);
        }

        // Procedural water stream
        int streamY = h / 2;
        for (int x = 0; x < w; x++) {
            streamY += rand.nextInt(3) - 1;
            streamY = Math.max(2, Math.min(h - 3, streamY));
            tileMap.fillRect(x, streamY, 1, 2, 4);
        }
    }
}
