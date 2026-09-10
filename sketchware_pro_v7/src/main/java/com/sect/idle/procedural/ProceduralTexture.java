package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.LinearGradient;
import android.graphics.RadialGradient;
import com.sect.idle.core.MathUtils;

/**
 * ProceduralTexture v3.0 - Runtime texture generator for terrain and effects.
 */
public final class ProceduralTexture {
    private ProceduralTexture() {}

    private static final Paint PAINT = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF RECT = new RectF();
    private static final Path PATH = new Path();

    public static Bitmap create(int w, int h, int type, long seed) {
        w = MathUtils.nextPowerOfTwo(Math.max(16, w));
        h = MathUtils.nextPowerOfTwo(Math.max(16, h));
        Bitmap.Config cfg = Bitmap.Config.ARGB_8888;
        Bitmap bmp = Bitmap.createBitmap(w, h, cfg);
        Canvas c = new Canvas(bmp);
        java.util.Random rand = new java.util.Random(seed);

        switch (type) {
            case 0: drawWood(c, w, h, rand); break;
            case 1: drawStone(c, w, h, rand); break;
            case 2: drawGrass(c, w, h, rand); break;
            case 3: drawWater(c, w, h, rand); break;
            case 4: drawMetal(c, w, h, rand); break;
            case 5: drawFire(c, w, h, rand); break;
            case 6: drawIce(c, w, h, rand); break;
            case 7: drawCloud(c, w, h, rand); break;
            case 8: drawDirt(c, w, h, rand); break;
            case 9: drawBrick(c, w, h, rand); break;
            case 10: drawTile(c, w, h, rand); break;
            case 11: drawRoof(c, w, h, rand); break;
            case 12: drawSand(c, w, h, rand); break;
            case 13: drawLava(c, w, h, rand); break;
            case 14: drawMoss(c, w, h, rand); break;
            default: drawNoise(c, w, h, rand, 0xFF555555, 0xFF333333);
        }
        return bmp;
    }

    private static void drawWood(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF5D4037); c.drawRect(0, 0, w, h, PAINT);
        PAINT.setStrokeWidth(2f);
        for (int i = 0; i < h; i += 3 + rand.nextInt(4)) {
            PAINT.setColor(MathUtils.mixColor(0xFF4E342E, 0xFF6D4C41, rand.nextFloat()));
            c.drawLine(0, i, w, i + rand.nextInt(3) - 1, PAINT);
        }
        for (int i = 0; i < 8; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(0xFF3E2723);
            c.drawCircle(x, y, 1 + rand.nextInt(3), PAINT);
        }
    }

    private static void drawStone(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF616161); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 40; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h), s = 2 + rand.nextInt(8);
            PAINT.setColor(MathUtils.mixColor(0xFF757575, 0xFF424242, rand.nextFloat()));
            RECT.set(x, y, x + s, y + s); c.drawRect(RECT, PAINT);
        }
        PAINT.setColor(0xFF424242); PAINT.setStrokeWidth(1f);
        for (int i = 0; i < 6; i++) {
            int x = rand.nextInt(w); c.drawLine(x, 0, x + rand.nextInt(10) - 5, h, PAINT);
        }
    }

    private static void drawGrass(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF2E7D32); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 60; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(MathUtils.mixColor(0xFF388E3C, 0xFF1B5E20, rand.nextFloat()));
            int len = 3 + rand.nextInt(6);
            c.drawLine(x, y, x + rand.nextInt(3) - 1, y - len, PAINT);
        }
        for (int i = 0; i < 15; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(0xFF4CAF50);
            c.drawCircle(x, y, 1 + rand.nextInt(2), PAINT);
        }
    }

    private static void drawWater(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF1565C0); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 20; i++) {
            int y = rand.nextInt(h);
            PAINT.setColor(MathUtils.mixColor(0xFF1976D2, 0xFF0D47A1, rand.nextFloat()));
            PAINT.setStrokeWidth(1 + rand.nextInt(2));
            c.drawLine(0, y, w, y + rand.nextInt(4) - 2, PAINT);
        }
        PAINT.setColor(0xFF42A5F5); PAINT.setStrokeWidth(1f);
        for (int i = 0; i < 10; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            c.drawCircle(x, y, 1 + rand.nextInt(2), PAINT);
        }
    }

    private static void drawMetal(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF78909C); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < h; i += 2) {
            PAINT.setColor(MathUtils.mixColor(0xFF90A4AE, 0xFF546E7A, (i / (float)h)));
            c.drawLine(0, i, w, i, PAINT);
        }
        for (int i = 0; i < 12; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(0xFFB0BEC5);
            c.drawCircle(x, y, 1, PAINT);
        }
    }

    private static void drawFire(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFB71C1C); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 30; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h / 2);
            int color = rand.nextFloat() > 0.5f ? 0xFFFF5722 : (rand.nextFloat() > 0.5f ? 0xFFFF9800 : 0xFFFFEB3B);
            PAINT.setColor(color);
            RECT.set(x, h - y - 5, x + 3 + rand.nextInt(4), h - y);
            c.drawRect(RECT, PAINT);
        }
    }

    private static void drawIce(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFE0F7FA); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 15; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(0xFFB2EBF2);
            int s = 4 + rand.nextInt(12);
            RECT.set(x, y, x + s, y + s); c.drawRect(RECT, PAINT);
        }
        PAINT.setColor(0xFFFFFFFF); PAINT.setStrokeWidth(1f);
        for (int i = 0; i < 8; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            c.drawLine(x, y, x + 5 + rand.nextInt(10), y + rand.nextInt(5), PAINT);
        }
    }

    private static void drawCloud(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFEEEEEE); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 8; i++) {
            int cx = rand.nextInt(w), cy = rand.nextInt(h);
            int r = 8 + rand.nextInt(16);
            PAINT.setColor(0xFFFFFFFF);
            c.drawCircle(cx, cy, r, PAINT);
            c.drawCircle(cx + r * 0.6f, cy, r * 0.8f, PAINT);
            c.drawCircle(cx - r * 0.6f, cy, r * 0.8f, PAINT);
        }
    }

    private static void drawDirt(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF5D4037); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 50; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h), s = 1 + rand.nextInt(4);
            PAINT.setColor(MathUtils.mixColor(0xFF795548, 0xFF3E2723, rand.nextFloat()));
            RECT.set(x, y, x + s, y + s); c.drawRect(RECT, PAINT);
        }
    }

    private static void drawBrick(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF8D6E63); c.drawRect(0, 0, w, h, PAINT);
        PAINT.setColor(0xFF5D4037); PAINT.setStrokeWidth(2f);
        int bw = w / 4, bh = h / 4;
        for (int y = 0; y < h; y += bh) {
            int offset = (y / bh) % 2 == 0 ? 0 : bw / 2;
            for (int x = -offset; x < w; x += bw) {
                RECT.set(x + 1, y + 1, x + bw - 1, y + bh - 1);
                PAINT.setColor(MathUtils.mixColor(0xFFA1887F, 0xFF6D4C41, rand.nextFloat()));
                c.drawRect(RECT, PAINT);
            }
        }
    }

    private static void drawTile(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFEEEEEE); c.drawRect(0, 0, w, h, PAINT);
        PAINT.setColor(0xFF9E9E9E); PAINT.setStrokeWidth(1f);
        int tw = w / 4;
        for (int x = 0; x < w; x += tw) c.drawLine(x, 0, x, h, PAINT);
        for (int y = 0; y < h; y += tw) c.drawLine(0, y, w, y, PAINT);
        for (int i = 0; i < 20; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(0xFFBDBDBD); c.drawCircle(x, y, 1, PAINT);
        }
    }

    private static void drawRoof(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFB71C1C); c.drawRect(0, 0, w, h, PAINT);
        PAINT.setColor(0xFF8D6E63); PAINT.setStrokeWidth(2f);
        int rh = h / 5;
        for (int y = 0; y < h; y += rh) {
            c.drawLine(0, y, w, y, PAINT);
            for (int x = 0; x < w; x += 12) {
                PATH.reset();
                PATH.moveTo(x, y); PATH.lineTo(x + 6, y + rh / 2); PATH.lineTo(x + 12, y);
                PAINT.setColor(MathUtils.mixColor(0xFFC62828, 0xFF8E0000, rand.nextFloat()));
                c.drawPath(PATH, PAINT);
            }
        }
    }

    private static void drawSand(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFFFF59D); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 80; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(MathUtils.mixColor(0xFFFFF176, 0xFFFBC02D, rand.nextFloat()));
            c.drawCircle(x, y, 1 + rand.nextInt(2), PAINT);
        }
    }

    private static void drawLava(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFFBF360C); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 25; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            int color = rand.nextFloat() > 0.6f ? 0xFFFF3D00 : 0xFFFF6D00;
            PAINT.setColor(color);
            RECT.set(x, y, x + 4 + rand.nextInt(8), y + 2 + rand.nextInt(4));
            c.drawRect(RECT, PAINT);
        }
    }

    private static void drawMoss(Canvas c, int w, int h, java.util.Random rand) {
        PAINT.setColor(0xFF33691E); c.drawRect(0, 0, w, h, PAINT);
        for (int i = 0; i < 40; i++) {
            int x = rand.nextInt(w), y = rand.nextInt(h);
            PAINT.setColor(MathUtils.mixColor(0xFF558B2F, 0xFF1B5E20, rand.nextFloat()));
            int s = 2 + rand.nextInt(6);
            c.drawCircle(x, y, s, PAINT);
        }
    }

    private static void drawNoise(Canvas c, int w, int h, java.util.Random rand, int c1, int c2) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                PAINT.setColor(MathUtils.mixColor(c1, c2, rand.nextFloat()));
                c.drawPoint(x, y, PAINT);
            }
        }
    }

    public static Bitmap createGradient(int w, int h, int c1, int c2, boolean radial) {
        w = MathUtils.nextPowerOfTwo(Math.max(16, w));
        h = MathUtils.nextPowerOfTwo(Math.max(16, h));
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        if (radial) {
            PAINT.setShader(new RadialGradient(w * 0.5f, h * 0.5f, Math.max(w, h) * 0.7f, c1, c2, Shader.TileMode.CLAMP));
        } else {
            PAINT.setShader(new LinearGradient(0, 0, w, h, c1, c2, Shader.TileMode.CLAMP));
        }
        c.drawRect(0, 0, w, h, PAINT);
        PAINT.setShader(null);
        return bmp;
    }

    public static Bitmap createNoiseTexture(int w, int h, float scale, int octaves, int c1, int c2) {
        w = MathUtils.nextPowerOfTwo(Math.max(16, w));
        h = MathUtils.nextPowerOfTwo(Math.max(16, h));
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                float n = MathUtils.fbm2D(x * scale, y * scale, octaves, 0.5f);
                bmp.setPixel(x, y, MathUtils.mixColor(c1, c2, n));
            }
        }
        return bmp;
    }
}
