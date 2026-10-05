package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.sect.idle.core.GameConfig;
import com.sect.idle.core.MathUtils;

/**
 * SpriteMaker v3.0 - Procedural sprite generation for Xianxia characters, buildings, items, effects.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
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

        // Xianxia Daoist Robe Colors by Realm
        int robeColor = male ? 0xFF1E88E5 : 0xFFEC407A;
        int robeTrimColor = 0xFFFFFFFF;
        if (realm >= 9) {
            robeColor = 0xFFF5F5F5; // Transcendent Immortal White
            robeTrimColor = 0xFFFFD700; // Gold trim
        } else if (realm >= 7) {
            robeColor = 0xFFFFD700; // Imperial Gold
            robeTrimColor = 0xFF8E24AA; // Purple trim
        } else if (realm >= 4) {
            robeColor = 0xFF7B1FA2; // Mystic Purple
            robeTrimColor = 0xFF00E5FF; // Cyan trim
        } else if (realm >= 2) {
            robeColor = male ? 0xFF1976D2 : 0xFFC2185B;
            robeTrimColor = 0xFFFFE082;
        }

        // 1. Transcendent Cultivation Halo / Spiritual Aura for higher realms
        if (realm >= 2) {
            P.setColor(MathUtils.setAlpha(elemColor, realm >= 6 ? 0.40f : 0.22f));
            c.drawCircle(cx, cy - 2, w * (0.42f + Math.min(0.08f, realm * 0.01f)), P);
            if (realm >= 5) {
                // Secondary concentric Dao ring
                P.setColor(MathUtils.setAlpha(0xFFFFD700, 0.30f));
                P.setStyle(Paint.Style.STROKE);
                P.setStrokeWidth(1.5f);
                c.drawCircle(cx, cy - 2, w * 0.46f, P);
                P.setStyle(Paint.Style.FILL);
            }
        }

        // 2. Flying Sword floating behind the back (Xianxia Yujian)
        if (realm >= 3 || task == GameConfig.TASK_TRAINING || task == GameConfig.TASK_GUARD || task == GameConfig.TASK_EXPLORING) {
            P.setColor(0xFFCFD8DC);
            c.save();
            c.rotate(-22f, cx - w * 0.22f, cy - h * 0.15f);
            // Blade
            c.drawRect(cx - w * 0.24f, cy - h * 0.42f, cx - w * 0.20f, cy + h * 0.22f, P);
            // Guard & Hilt
            P.setColor(0xFFFFD700);
            c.drawRect(cx - w * 0.28f, cy + h * 0.12f, cx - w * 0.16f, cy + h * 0.16f, P);
            P.setColor(0xFFD32F2F); // Red sword tassel
            c.drawCircle(cx - w * 0.22f, cy + h * 0.24f, 2f, P);
            c.restore();
            P.setColor(robeColor);
        }

        // 3. Ground Shadow
        P.setColor(0x40000000);
        R.set(cx - w * 0.35f, h - 8, cx + w * 0.35f, h - 2);
        c.drawOval(R, P);

        // 4. Floating Celestial Ribbons (Piaodai)
        P.setColor(MathUtils.setAlpha(robeTrimColor, 0.75f));
        PATH.reset();
        PATH.moveTo(cx - w * 0.28f, cy - h * 0.1f);
        PATH.quadTo(cx - w * 0.44f, cy + h * 0.15f, cx - w * 0.32f, h - 6);
        PATH.quadTo(cx - w * 0.38f, cy + h * 0.12f, cx - w * 0.26f, cy);
        PATH.close();
        c.drawPath(PATH, P);

        PATH.reset();
        PATH.moveTo(cx + w * 0.28f, cy - h * 0.1f);
        PATH.quadTo(cx + w * 0.44f, cy + h * 0.15f, cx + w * 0.32f, h - 6);
        PATH.quadTo(cx + w * 0.38f, cy + h * 0.12f, cx + w * 0.26f, cy);
        PATH.close();
        c.drawPath(PATH, P);

        // 5. Body / Daoist Robe
        P.setColor(robeColor);
        PATH.reset();
        PATH.moveTo(cx - w * 0.24f, cy - 2);
        PATH.lineTo(cx + w * 0.24f, cy - 2);
        PATH.lineTo(cx + w * 0.34f, h - 8);
        PATH.lineTo(cx - w * 0.34f, h - 8);
        PATH.close();
        c.drawPath(PATH, P);

        // Robe Hem / Trim
        P.setColor(robeTrimColor);
        R.set(cx - w * 0.34f, h - 11, cx + w * 0.34f, h - 8);
        c.drawRect(R, P);

        // Robe Collar / Crossed Lapels
        P.setColor(robeTrimColor);
        PATH.reset();
        PATH.moveTo(cx - w * 0.12f, cy - 4);
        PATH.lineTo(cx + 2, cy + h * 0.12f);
        PATH.lineTo(cx - 2, cy + h * 0.12f);
        PATH.lineTo(cx + w * 0.12f, cy - 4);
        PATH.close();
        c.drawPath(PATH, P);

        // 6. Sash / Jade Belt
        P.setColor(elemColor);
        R.set(cx - w * 0.25f, cy + h * 0.12f, cx + w * 0.25f, cy + h * 0.18f);
        c.drawRect(R, P);

        // Jade Pendant (Yupei)
        P.setColor(0xFF80CBC4);
        c.drawCircle(cx, cy + h * 0.22f, 2.5f, P);
        P.setColor(0xFFFF5252); // Red tassel string
        c.drawLine(cx, cy + h * 0.22f, cx, cy + h * 0.28f, P);

        // 7. Head
        P.setColor(0xFFFFE0B2);
        c.drawCircle(cx, cy - h * 0.18f, w * 0.20f, P);

        // 8. Hair & Daoist Hair Crown (Guan)
        P.setColor(male ? 0xFF212121 : 0xFF2C1810);
        if (male) {
            c.drawCircle(cx, cy - h * 0.24f, w * 0.17f, P);
            // Top knot / hair bun
            c.drawCircle(cx, cy - h * 0.36f, w * 0.08f, P);
            // Golden Hairpin / Guan
            P.setColor(0xFFFFD700);
            c.drawRect(cx - w * 0.12f, cy - h * 0.38f, cx + w * 0.12f, cy - h * 0.35f, P);
        } else {
            // Flowing twin loops / hair ribbons
            R.set(cx - w * 0.24f, cy - h * 0.36f, cx + w * 0.24f, cy - h * 0.04f);
            c.drawOval(R, P);
            // Jade hair ornament
            P.setColor(0xFF80CBC4);
            c.drawCircle(cx - w * 0.15f, cy - h * 0.26f, 2.5f, P);
            c.drawCircle(cx + w * 0.15f, cy - h * 0.26f, 2.5f, P);
        }

        // 9. Facial features
        P.setColor(0xFF212121);
        c.drawCircle(cx - 4.5f, cy - h * 0.17f, 1.5f, P);
        c.drawCircle(cx + 4.5f, cy - h * 0.17f, 1.5f, P);

        // Red Forehead Huadian (Spiritual mark for realm >= 3)
        if (realm >= 3) {
            P.setColor(0xFFFF1744);
            c.drawCircle(cx, cy - h * 0.22f, 1.2f, P);
        }

        return bmp;
    }

    public static Bitmap createBuilding(int size, int type, int level, long seed) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        float s = size * 0.5f;

        // Shadow
        P.setColor(0x40000000);
        R.set(4, size - 14, size - 4, size - 2);
        c.drawOval(R, P);

        // Building theme colors (Xianxia Sect architecture)
        int baseWallColor = 0xFF8D6E63;
        int primaryRoofColor = 0xFFD32F2F;
        int accentColor = 0xFFFFD700;

        switch (type % 6) {
            case 0: // Main Sect Hall (Grand Golden Palace)
                primaryRoofColor = 0xFFFFC107; baseWallColor = 0xFF6D4C41; accentColor = 0xFFFFD700; break;
            case 1: // Scripture Library (Deep Celestial Azure)
                primaryRoofColor = 0xFF1976D2; baseWallColor = 0xFF455A64; accentColor = 0xFF00E5FF; break;
            case 2: // Spirit Herb Garden / Alchemy (Emerald & Jade)
                primaryRoofColor = 0xFF2E7D32; baseWallColor = 0xFF5D4037; accentColor = 0xFF69F0AE; break;
            case 3: // Artifact Forge / Spirit Mine (Molten Bronze & Crimson)
                primaryRoofColor = 0xFFE64A19; baseWallColor = 0xFF3E2723; accentColor = 0xFFFFAB40; break;
            case 4: // Immortal Beast / Soul Chamber (Mystic Violet)
                primaryRoofColor = 0xFF7B1FA2; baseWallColor = 0xFF37474F; accentColor = 0xFFEA80FC; break;
            default: // Martial Arena / Pagoda
                primaryRoofColor = 0xFFC2185B; baseWallColor = 0xFF4E342E; accentColor = 0xFFFFD700; break;
        }

        // 1. Foundation Stone Plinth
        P.setColor(0xFF424242);
        R.set(s - size * 0.42f, size - 16, s + size * 0.42f, size - 6);
        c.drawRoundRect(R, 3, 3, P);

        // 2. Main Wooden Pavilion Walls
        P.setColor(baseWallColor);
        R.set(s - size * 0.36f, s - size * 0.05f, s + size * 0.36f, size - 14);
        c.drawRoundRect(R, 4, 4, P);

        // Red Lacquered Timber Pillars
        P.setColor(0xFFB71C1C);
        c.drawRect(s - size * 0.35f, s - size * 0.05f, s - size * 0.30f, size - 14, P);
        c.drawRect(s + size * 0.30f, s - size * 0.05f, s + size * 0.35f, size - 14, P);

        // Grand Daoist Entrance & Calligraphy Plaque
        P.setColor(0xFF212121);
        R.set(s - 10, size - 32, s + 10, size - 14);
        c.drawRoundRect(R, 2, 2, P);
        // Golden plaque (Bian'e)
        P.setColor(0xFFFFD700);
        R.set(s - 12, s + 2, s + 12, s + 10);
        c.drawRoundRect(R, 2, 2, P);

        // 3. Lower Pagoda Roof with Flying Eaves (Feiyan)
        P.setColor(primaryRoofColor);
        PATH.reset();
        PATH.moveTo(s, s - size * 0.16f);
        PATH.lineTo(size - 2, s + 4);
        PATH.lineTo(s + size * 0.42f, s - 2);
        PATH.lineTo(s - size * 0.42f, s - 2);
        PATH.lineTo(2, s + 4);
        PATH.close();
        c.drawPath(PATH, P);

        // 4. Upper Pagoda Tower (Multi-tier roof for higher levels)
        if (level >= 3) {
            // Upper wall
            P.setColor(baseWallColor);
            R.set(s - size * 0.22f, s - size * 0.32f, s + size * 0.22f, s - size * 0.14f);
            c.drawRect(R, P);

            // Upper roof
            P.setColor(primaryRoofColor);
            PATH.reset();
            PATH.moveTo(s, 6);
            PATH.lineTo(size - 8, s - size * 0.24f);
            PATH.lineTo(s + size * 0.28f, s - size * 0.28f);
            PATH.lineTo(s - size * 0.28f, s - size * 0.28f);
            PATH.lineTo(8, s - size * 0.24f);
            PATH.close();
            c.drawPath(PATH, P);
        } else {
            // Single Tier Main Roof
            P.setColor(primaryRoofColor);
            PATH.reset();
            PATH.moveTo(s, 6);
            PATH.lineTo(size - 4, s - size * 0.12f);
            PATH.lineTo(4, s - size * 0.12f);
            PATH.close();
            c.drawPath(PATH, P);
        }

        // 5. Golden Roof Ridges & Pinnacle Spirit Pearl
        P.setColor(accentColor);
        c.drawCircle(s, 6, 4f, P); // Roof finial pearl

        // Hanging Spirit Lanterns on Eaves
        P.setColor(0xFFFF3D00);
        c.drawCircle(6, s + 6, 3f, P);
        c.drawCircle(size - 6, s + 6, 3f, P);
        P.setColor(0xFFFFD700);
        c.drawCircle(6, s + 6, 1.5f, P);
        c.drawCircle(size - 6, s + 6, 1.5f, P);

        return bmp;
    }

    public static Bitmap createItem(int size, int type, int rarity, int element) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        float s = size * 0.5f;

        // Background glow
        int rColor = GameConfig.RARITY_COLORS[Math.min(rarity, GameConfig.RARITY_COLORS.length - 1)];
        P.setColor(MathUtils.setAlpha(rColor, 0.28f));
        c.drawCircle(s, s, s * 0.88f, P);

        // Outer Rarity Ring
        P.setColor(MathUtils.setAlpha(rColor, 0.85f));
        P.setStyle(Paint.Style.STROKE);
        P.setStrokeWidth(1.5f);
        c.drawCircle(s, s, s * 0.82f, P);
        P.setStyle(Paint.Style.FILL);

        switch (type) {
            case 0: // Spirit Pill / Elixir (Dan Pill with 9-vein aura)
                P.setColor(rColor);
                c.drawCircle(s, s, s * 0.45f, P);
                // Golden Pill Core & Spiraling Dao Veins
                P.setColor(0xFFFFD700);
                c.drawCircle(s - 2, s - 2, s * 0.22f, P);
                P.setColor(0xFFFFFFFF);
                c.drawCircle(s - 3, s - 3, 2.5f, P);
                // Pill swirl lines
                P.setColor(MathUtils.setAlpha(0xFFFFFFFF, 0.6f));
                P.setStyle(Paint.Style.STROKE);
                P.setStrokeWidth(1.2f);
                c.drawArc(new RectF(s - s * 0.35f, s - s * 0.35f, s + s * 0.35f, s + s * 0.35f), 30, 140, false, P);
                P.setStyle(Paint.Style.FILL);
                break;

            case 1: // Spirit Herb / Celestial Flower
                P.setColor(0xFF66BB6A);
                PATH.reset();
                PATH.moveTo(s, s - s * 0.60f); // Top petal
                PATH.lineTo(s + s * 0.38f, s - s * 0.10f);
                PATH.lineTo(s + s * 0.28f, s + s * 0.45f);
                PATH.lineTo(s - s * 0.28f, s + s * 0.45f);
                PATH.lineTo(s - s * 0.38f, s - s * 0.10f);
                PATH.close();
                c.drawPath(PATH, P);
                // Radiant flower center
                P.setColor(0xFFFFEE58);
                c.drawCircle(s, s - 2, s * 0.18f, P);
                P.setColor(0xFF2E7D32); // Stem
                c.drawRect(s - 1.5f, s + s * 0.30f, s + 1.5f, s + s * 0.65f, P);
                break;

            case 2: // Daoist Talisman (Fulu)
                P.setColor(0xFFFFEB3B); // Yellow talisman paper
                R.set(s - s * 0.32f, s - s * 0.55f, s + s * 0.32f, s + s * 0.55f);
                c.drawRoundRect(R, 2, 2, P);
                // Crimson Cinnabar Calligraphy Runes
                P.setColor(0xFFD50000);
                P.setStyle(Paint.Style.STROKE);
                P.setStrokeWidth(1.8f);
                c.drawLine(s, s - s * 0.40f, s, s - s * 0.10f, P);
                c.drawCircle(s, s, s * 0.14f, P);
                c.drawLine(s - 4, s + s * 0.20f, s + 4, s + s * 0.20f, P);
                c.drawLine(s, s + s * 0.20f, s, s + s * 0.40f, P);
                P.setStyle(Paint.Style.FILL);
                break;

            case 3: // Spirit Cauldron / Alchemy Furnace (Ding)
                P.setColor(0xFF8D6E63);
                // Cauldron bowl
                R.set(s - s * 0.45f, s - s * 0.20f, s + s * 0.45f, s + s * 0.35f);
                c.drawOval(R, P);
                // Rim
                P.setColor(0xFFFFD700);
                R.set(s - s * 0.48f, s - s * 0.28f, s + s * 0.48f, s - s * 0.16f);
                c.drawRect(R, P);
                // 3 legs
                P.setColor(0xFF5D4037);
                c.drawRect(s - s * 0.38f, s + s * 0.28f, s - s * 0.25f, s + s * 0.55f, P);
                c.drawRect(s + s * 0.25f, s + s * 0.28f, s + s * 0.38f, s + s * 0.55f, P);
                c.drawRect(s - s * 0.06f, s + s * 0.32f, s + s * 0.06f, s + s * 0.58f, P);
                // Alchemy Flame
                P.setColor(0xFFFF5722);
                c.drawCircle(s, s - s * 0.05f, s * 0.18f, P);
                break;

            case 4: // Flying Sword / Immortal Blade
                P.setColor(0xFFECEFF1);
                c.save();
                c.rotate(45f, s, s);
                // Blade
                c.drawRect(s - 2.5f, s - s * 0.72f, s + 2.5f, s + s * 0.25f, P);
                // Tip
                PATH.reset();
                PATH.moveTo(s - 2.5f, s - s * 0.72f);
                PATH.lineTo(s, s - s * 0.88f);
                PATH.lineTo(s + 2.5f, s - s * 0.72f);
                PATH.close();
                c.drawPath(PATH, P);
                // Golden Crossguard
                P.setColor(0xFFFFD700);
                c.drawRect(s - 7f, s + s * 0.25f, s + 7f, s + s * 0.32f, P);
                // Hilt & Pommel
                P.setColor(0xFF37474F);
                c.drawRect(s - 2f, s + s * 0.32f, s + 2f, s + s * 0.55f, P);
                P.setColor(0xFFFFD700);
                c.drawCircle(s, s + s * 0.58f, 2.5f, P);
                // Red Ribbon Tassel
                P.setColor(0xFFFF1744);
                c.drawCircle(s, s + s * 0.70f, 2f, P);
                c.restore();
                break;

            default: // Spirit Ore / Crystal
                P.setColor(0xFF00E5FF);
                PATH.reset();
                PATH.moveTo(s, s - s * 0.60f);
                PATH.lineTo(s + s * 0.50f, s - s * 0.15f);
                PATH.lineTo(s + s * 0.35f, s + s * 0.50f);
                PATH.lineTo(s - s * 0.35f, s + s * 0.50f);
                PATH.lineTo(s - s * 0.50f, s - s * 0.15f);
                PATH.close();
                c.drawPath(PATH, P);
                // Crystal facets
                P.setColor(0xFFE0F7FA);
                c.drawLine(s, s - s * 0.60f, s, s + s * 0.50f, P);
                break;
        }

        return bmp;
    }
}
