package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.sect.idle.systems.TextureAtlas;
import java.util.HashMap;
import java.util.Map;

/**
 * TextureAtlasBuilder v1.0 - Packs multiple bitmaps into a single texture atlas.
 */
public final class TextureAtlasBuilder {
    private final Map<String, Bitmap> bitmaps = new HashMap<>();
    private int padding = 2;

    public TextureAtlasBuilder setPadding(int p) {
        this.padding = Math.max(0, p);
        return this;
    }

    public TextureAtlasBuilder add(String name, Bitmap bmp) {
        if (name != null && bmp != null && !bmp.isRecycled()) {
            bitmaps.put(name, bmp);
        }
        return this;
    }

    public TextureAtlas build() {
        if (bitmaps.isEmpty()) return null;
        int totalW = 0, maxH = 0;
        for (Bitmap b : bitmaps.values()) {
            totalW += b.getWidth() + padding;
            if (b.getHeight() > maxH) maxH = b.getHeight();
        }

        int atlasW = Math.max(64, totalW);
        int atlasH = Math.max(64, maxH + padding * 2);
        Bitmap atlasBmp = Bitmap.createBitmap(atlasW, atlasH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(atlasBmp);
        TextureAtlas atlas = new TextureAtlas(atlasBmp);

        int currentX = padding;
        for (Map.Entry<String, Bitmap> entry : bitmaps.entrySet()) {
            Bitmap b = entry.getValue();
            canvas.drawBitmap(b, currentX, padding, null);
            atlas.defineRegion(entry.getKey(), currentX, padding, b.getWidth(), b.getHeight());
            currentX += b.getWidth() + padding;
        }

        return atlas;
    }
}
