package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.sect.idle.systems.SpriteSheet;

/**
 * SpriteSheetCreator v1.0 - Combines multiple frames into a unified SpriteSheet.
 */
public final class SpriteSheetCreator {
    private SpriteSheetCreator() {}

    public static SpriteSheet create(Bitmap[] frames, int fw, int fh, int cols, int rows) {
        if (frames == null || frames.length == 0 || fw <= 0 || fh <= 0) return null;
        int sheetW = fw * cols;
        int sheetH = fh * rows;
        Bitmap sheetBmp = Bitmap.createBitmap(sheetW, sheetH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(sheetBmp);

        for (int i = 0; i < frames.length && i < cols * rows; i++) {
            if (frames[i] != null && !frames[i].isRecycled()) {
                int col = i % cols;
                int row = i / cols;
                canvas.drawBitmap(frames[i], col * fw, row * fh, null);
            }
        }

        return new SpriteSheet(sheetBmp, fw, fh);
    }
}
