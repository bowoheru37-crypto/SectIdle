package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.sect.idle.core.GameConfig;
import com.sect.idle.core.MathUtils;

/**
 * SpriteMaker v3.0 - Procedural sprite generation for characters, items, effects.
 */
public final class SpriteMaker {
    private SpriteMaker() {}
    
    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF R = new RectF();
    private static final Path PATH = new Path();

    public static Bitmap createDisciple(int w, int h, int element, int realm, boolean male, int task, long seed) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        java.util.Random rand = new java.util.Random(seed);

        float cx = w * 0.5f, cy = h * 0.5f;
        int elemColor = GameConfig.getElementColor(element);
        int robeColor = male ? 0xFF1E88E5 : 0xFFEC407A;
        if (realm >= 8) robeColor = 0xFFFFD700;
        else if (realm >= 4) robeColor = 0xFF9C27B0;

        // Shadow
        P.setColor(0x40000000);
        R.set(cx - w * 0.35f, h - 8, cx + w * 0.35f, h - 2);
        c.drawOval(R, P);

        // Body / Robe
        P.setColor(robeColor);
        PATH.reset();
        PATH.moveTo(cx - w * 0.25f, cy);
        PATH.lineTo(cx + w * 0.25f, cy);
        PATH.lineTo(cx + w * 0.35f, h - 8);
        PATH.lineTo(cx - w * 0.35f, h - 8);
        PATH.close();
        c.drawPath(PATH, P);

        // Sash / Belt
        P.setColor(elemColor);
        R.set(cx - w * 0.26f, cy + h * 0.12f, cx + w * 0.26f, cy + h * 0.18f);
        c.drawRect(R, P);

        // Head
        P.setColor(0xFFFFE0B2);
        c.drawCircle(cx, cy - h * 0.18f, w * 0.2f, P);

        // Hair
        P.setColor(male ? 0xFF212121 : 0xFF3E2723);
        if (male) {
            c.drawCircle(cx, cy - h * 0.25f, w * 0.16f, P);
        } else {
            R.set(cx - w * 0.22f, cy - h * 0.35f, cx + w * 0.22f, cy - h * 0.05f);
            c.drawOval(R, P);
        }

        // Eyes
        P.setColor(0xFF212121);
        c.drawCircle(cx - 4, cy - h * 0.18f, 1.5f, P);
        c.drawCircle(cx + 4, cy - h * 0.18f, 1.5f, P);

        // Element Aura
        if (realm >= 2) {
            P.setColor(MathUtils.setAlpha(elemColor, 0.3f));
            c.drawCircle(cx, cy, w * 0.45f, P);
        }

        return bmp;
    }

    public static Bitmap createItem(int size, int type, int rarity, int element) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        float s = size * 0.5f;

        // Background glow
        int rColor = GameConfig.RARITY_COLORS[Math.min(rarity, GameConfig.RARITY_COLORS.length - 1)];
        P.setColor(MathUtils.setAlpha(rColor, 0.25f));
        c.drawCircle(s, s, s * 0.85f, P);

        int eColor = GameConfig.getElementColor(element);
        P.setColor(eColor);

        switch (type) {
            case 0: // Consumable (Pill)
                P.setColor(0xFFE91E63);
                c.drawCircle(s, s, s * 0.45f, P);
                P.setColor(0xFFFFFFFF);
                c.drawCircle(s - 3, s - 3, 2, P);
                break;
            case 1: // Material (Herb / Crystal)
                PATH.reset();
                PATH.moveTo(s, s - s * 0.5f);
                PATH.lineTo(s + s * 0.4f, s + s * 0.4f);
                PATH.lineTo(s - s * 0.4f, s + s * 0.4f);
                PATH.close();
                c.drawPath(PATH, P);
                break;
            case 4: // Weapon / Sword
                P.setColor(0xFFB0BEC5);
                c.drawRect(s - 2, s - s * 0.6f, s + 2, s + s * 0.4f, P);
                P.setColor(0xFFFFD700);
                c.drawRect(s - 6, s + s * 0.2f, s + 6, s + s * 0.28f, P);
                break;
            default:
                c.drawCircle(s, s, s * 0.4f, P);
                break;
        }

        return bmp;
    }
}
