package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

/**
 * IdleSectAssetGenerator - AI Procedural Asset Generator for Idle Sect v2.0 (Itel A70 Ultra).
 * Generates Nusantara Temple assets, Chibi Zhao Kai, Sect Guardian, Demonic Wolf,
 * batik gold borders, and building stamps into a compact memory footprint (<96MB budget).
 * Pure Java 7/8 syntax compatible with Android API 21+.
 */
public final class IdleSectAssetGenerator {

    // Color design tokens
    public static final int COLOR_BG = 0xFF1A0F2E;        // Ungu tua Cloud Mist Sect
    public static final int COLOR_BATIK_GOLD = 0xFFFFD700; // Border Gold
    public static final int COLOR_CYAN = 0xFF00E5FF;       // Cyan Accent
    public static final int COLOR_JADE_GREEN = 0xFF00FF88; // Jade Green
    public static final int COLOR_SS_GOLD = 0xFFFFC107;    // Spirit Stone Gold
    public static final int COLOR_JADE_BLUE = 0xFF00BCD4;  // Jade Blue

    private IdleSectAssetGenerator() {}

    /**
     * Generates a 9-patch style decorative Batik Gold border background bitmap.
     */
    public static Bitmap createBatikCard(int w, int h, int bgColor, int borderColor) {
        if (w <= 0 || h <= 0) return null;
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        // Fill background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(bgColor);
        RectF rect = new RectF(4, 4, w - 4, h - 4);
        canvas.drawRoundRect(rect, 12, 12, paint);

        // Outer batik border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(borderColor);
        canvas.drawRoundRect(rect, 12, 12, paint);

        // Corner ornaments (Batik corner motif)
        paint.setStrokeWidth(1.5f);
        float sz = 16f;
        // Top-left
        canvas.drawLine(8, 8 + sz, 8, 8, paint);
        canvas.drawLine(8, 8, 8 + sz, 8, paint);
        canvas.drawCircle(8 + 6, 8 + 6, 2f, paint);

        // Top-right
        canvas.drawLine(w - 8 - sz, 8, w - 8, 8, paint);
        canvas.drawLine(w - 8, 8, w - 8, 8 + sz, paint);
        canvas.drawCircle(w - 14, 8 + 6, 2f, paint);

        // Bottom-left
        canvas.drawLine(8, h - 8 - sz, 8, h - 8, paint);
        canvas.drawLine(8, h - 8, 8 + sz, h - 8, paint);
        canvas.drawCircle(8 + 6, h - 14, 2f, paint);

        // Bottom-right
        canvas.drawLine(w - 8 - sz, h - 8, w - 8, h - 8, paint);
        canvas.drawLine(w - 8, h - 8 - sz, w - 8, h - 8, paint);
        canvas.drawCircle(w - 14, h - 14, 2f, paint);

        return bmp;
    }

    /**
     * Generates Chibi Zhao Kai portrait (Mind Root Cultivator, Cyan & Jade robes).
     */
    public static Bitmap createZhaoKaiChibi(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = size * 0.5f;
        float cy = size * 0.5f;

        // Aura ring
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.04f);
        paint.setColor(COLOR_CYAN);
        canvas.drawCircle(cx, cy, size * 0.44f, paint);

        // Background circle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF231545);
        canvas.drawCircle(cx, cy, size * 0.42f, paint);

        // Robe body
        paint.setColor(COLOR_JADE_BLUE);
        Path body = new Path();
        body.moveTo(cx - size * 0.28f, size * 0.88f);
        body.lineTo(cx, size * 0.52f);
        body.lineTo(cx + size * 0.28f, size * 0.88f);
        body.close();
        canvas.drawPath(body, paint);

        // Golden sash
        paint.setColor(COLOR_BATIK_GOLD);
        canvas.drawRect(cx - size * 0.12f, size * 0.72f, cx + size * 0.12f, size * 0.76f, paint);

        // Head / Face
        paint.setColor(0xFFFFE0BD); // skin tone
        canvas.drawCircle(cx, size * 0.42f, size * 0.22f, paint);

        // Hair (Topknot & bangs)
        paint.setColor(0xFF1A1A24);
        canvas.drawArc(new RectF(cx - size * 0.23f, size * 0.20f, cx + size * 0.23f, size * 0.46f), 180, 180, true, paint);
        // Topknot bun
        canvas.drawCircle(cx, size * 0.20f, size * 0.08f, paint);
        // Jade hairpin
        paint.setColor(COLOR_JADE_GREEN);
        canvas.drawLine(cx - size * 0.10f, size * 0.19f, cx + size * 0.10f, size * 0.17f, paint);

        // Eyes (Wise expressive eyes)
        paint.setColor(0xFF1A102E);
        canvas.drawCircle(cx - size * 0.08f, size * 0.42f, size * 0.03f, paint);
        canvas.drawCircle(cx + size * 0.08f, size * 0.42f, size * 0.03f, paint);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx - size * 0.07f, size * 0.41f, size * 0.012f, paint);
        canvas.drawCircle(cx + size * 0.09f, size * 0.41f, size * 0.012f, paint);

        // Gentle smile
        paint.setColor(0xFFB35959);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2f);
        canvas.drawArc(new RectF(cx - size * 0.04f, size * 0.47f, cx + size * 0.04f, size * 0.52f), 0, 180, false, paint);

        return bmp;
    }

    /**
     * Generates Sect Guardian Chibi (Jade Armor, Golden Blade).
     */
    public static Bitmap createSectGuardianChibi(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = size * 0.5f;
        float cy = size * 0.5f;

        // Aura shield
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.04f);
        paint.setColor(COLOR_BATIK_GOLD);
        canvas.drawCircle(cx, cy, size * 0.44f, paint);

        // Background circle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF1E2838);
        canvas.drawCircle(cx, cy, size * 0.42f, paint);

        // Jade Armor Torso
        paint.setColor(0xFF00897B);
        Path armor = new Path();
        armor.moveTo(cx - size * 0.30f, size * 0.88f);
        armor.lineTo(cx, size * 0.54f);
        armor.lineTo(cx + size * 0.30f, size * 0.88f);
        armor.close();
        canvas.drawPath(armor, paint);

        // Golden Pauldrons
        paint.setColor(COLOR_BATIK_GOLD);
        canvas.drawCircle(cx - size * 0.22f, size * 0.62f, size * 0.08f, paint);
        canvas.drawCircle(cx + size * 0.22f, size * 0.62f, size * 0.08f, paint);

        // Head & Helmet
        paint.setColor(0xFFFFD1A4);
        canvas.drawCircle(cx, size * 0.42f, size * 0.20f, paint);

        // Guardian Helm
        paint.setColor(0xFF004D40);
        Path helm = new Path();
        helm.moveTo(cx - size * 0.22f, size * 0.40f);
        helm.lineTo(cx, size * 0.16f);
        helm.lineTo(cx + size * 0.22f, size * 0.40f);
        helm.close();
        canvas.drawPath(helm, paint);

        // Eyes (Stern glowing cyan)
        paint.setColor(COLOR_CYAN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx - size * 0.07f, size * 0.42f, size * 0.03f, paint);
        canvas.drawCircle(cx + size * 0.07f, size * 0.42f, size * 0.03f, paint);

        // Guardian Spear/Blade
        paint.setColor(COLOR_BATIK_GOLD);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.03f);
        canvas.drawLine(cx + size * 0.25f, size * 0.15f, cx + size * 0.25f, size * 0.85f, paint);
        paint.setStyle(Paint.Style.FILL);
        Path blade = new Path();
        blade.moveTo(cx + size * 0.25f, size * 0.05f);
        blade.lineTo(cx + size * 0.20f, size * 0.22f);
        blade.lineTo(cx + size * 0.30f, size * 0.22f);
        blade.close();
        canvas.drawPath(blade, paint);

        return bmp;
    }

    /**
     * Generates Demonic Wolf / Corrupted Fiend battle avatar.
     */
    public static Bitmap createDemonicWolf(int size) {
        Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = size * 0.5f;
        float cy = size * 0.5f;

        // Dark demonic red aura
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size * 0.04f);
        paint.setColor(0xFFFF1744);
        canvas.drawCircle(cx, cy, size * 0.44f, paint);

        // Dark fill
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF2A0812);
        canvas.drawCircle(cx, cy, size * 0.42f, paint);

        // Wolf head shape
        paint.setColor(0xFF3E2723);
        Path head = new Path();
        head.moveTo(cx - size * 0.26f, size * 0.35f);
        head.lineTo(cx, size * 0.65f);
        head.lineTo(cx + size * 0.26f, size * 0.35f);
        head.lineTo(cx, size * 0.28f);
        head.close();
        canvas.drawPath(head, paint);

        // Wolf ears
        Path earL = new Path();
        earL.moveTo(cx - size * 0.24f, size * 0.36f);
        earL.lineTo(cx - size * 0.32f, size * 0.16f);
        earL.lineTo(cx - size * 0.12f, size * 0.28f);
        earL.close();
        canvas.drawPath(earL, paint);

        Path earR = new Path();
        earR.moveTo(cx + size * 0.24f, size * 0.36f);
        earR.lineTo(cx + size * 0.32f, size * 0.16f);
        earR.lineTo(cx + size * 0.12f, size * 0.28f);
        earR.close();
        canvas.drawPath(earR, paint);

        // Glowing red eyes
        paint.setColor(0xFFFF1744);
        canvas.drawCircle(cx - size * 0.08f, size * 0.42f, size * 0.035f, paint);
        canvas.drawCircle(cx + size * 0.08f, size * 0.42f, size * 0.035f, paint);

        // Fangs
        paint.setColor(Color.WHITE);
        Path fangL = new Path();
        fangL.moveTo(cx - size * 0.05f, size * 0.56f);
        fangL.lineTo(cx - size * 0.03f, size * 0.63f);
        fangL.lineTo(cx - size * 0.01f, size * 0.56f);
        fangL.close();
        canvas.drawPath(fangL, paint);

        Path fangR = new Path();
        fangR.moveTo(cx + size * 0.01f, size * 0.56f);
        fangR.lineTo(cx + size * 0.03f, size * 0.63f);
        fangR.lineTo(cx + size * 0.05f, size * 0.56f);
        fangR.close();
        canvas.drawPath(fangR, paint);

        return bmp;
    }

    /**
     * Generates Nusantara Temple / Candi Prambanan Isometric Pavilion building stamp.
     */
    public static Bitmap createPrambananBuilding(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = w * 0.5f;

        // Base stone plinth (isometric diamond)
        paint.setColor(0xFF37474F);
        Path base = new Path();
        base.moveTo(cx, h * 0.70f);
        base.lineTo(w * 0.88f, h * 0.82f);
        base.lineTo(cx, h * 0.96f);
        base.lineTo(w * 0.12f, h * 0.82f);
        base.close();
        canvas.drawPath(base, paint);

        // Temple stone tiers (Prambanan multi-stepped spire)
        paint.setColor(0xFF455A64);
        int tiers = 4;
        for (int i = 0; i < tiers; i++) {
            float tY = h * (0.65f - i * 0.12f);
            float halfW = (w * 0.35f) * (1f - (i * 0.18f));
            float tierH = h * 0.11f;

            // Tier body
            paint.setColor((i % 2 == 0) ? 0xFF546E7A : 0xFF37474F);
            canvas.drawRect(cx - halfW, tY, cx + halfW, tY + tierH, paint);

            // Gold ornament band
            paint.setColor(COLOR_BATIK_GOLD);
            canvas.drawRect(cx - halfW, tY, cx + halfW, tY + 4, paint);
        }

        // Pinnacle Ratna jewel
        paint.setColor(COLOR_BATIK_GOLD);
        Path ratna = new Path();
        ratna.moveTo(cx, h * 0.10f);
        ratna.lineTo(cx - w * 0.06f, h * 0.20f);
        ratna.lineTo(cx + w * 0.06f, h * 0.20f);
        ratna.close();
        canvas.drawPath(ratna, paint);

        // Glowing spirit light at entrance
        paint.setColor(COLOR_CYAN);
        canvas.drawCircle(cx, h * 0.73f, w * 0.08f, paint);

        return bmp;
    }

    /**
     * Generates Bamboo Martial Arena building stamp.
     */
    public static Bitmap createMartialArena(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = w * 0.5f;

        // Wood / Bamboo ring platform
        paint.setColor(0xFF6D4C41);
        Path platform = new Path();
        platform.moveTo(cx, h * 0.40f);
        platform.lineTo(w * 0.90f, h * 0.65f);
        platform.lineTo(cx, h * 0.90f);
        platform.lineTo(w * 0.10f, h * 0.65f);
        platform.close();
        canvas.drawPath(platform, paint);

        // Red martial mat inside
        paint.setColor(0xFFC62828);
        Path mat = new Path();
        mat.moveTo(cx, h * 0.48f);
        mat.lineTo(w * 0.78f, h * 0.65f);
        mat.lineTo(cx, h * 0.82f);
        mat.lineTo(w * 0.22f, h * 0.65f);
        mat.close();
        canvas.drawPath(mat, paint);

        // Yin-yang / Cultivation ring in center
        paint.setColor(COLOR_BATIK_GOLD);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        canvas.drawCircle(cx, h * 0.65f, w * 0.14f, paint);

        // Bamboo banner posts
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF33691E);
        canvas.drawRect(w * 0.15f, h * 0.25f, w * 0.18f, h * 0.65f, paint);
        canvas.drawRect(w * 0.82f, h * 0.25f, w * 0.85f, h * 0.65f, paint);

        // Banners
        paint.setColor(COLOR_BATIK_GOLD);
        Path bannerL = new Path();
        bannerL.moveTo(w * 0.18f, h * 0.28f);
        bannerL.lineTo(w * 0.32f, h * 0.35f);
        bannerL.lineTo(w * 0.18f, h * 0.42f);
        bannerL.close();
        canvas.drawPath(bannerL, paint);

        return bmp;
    }

    /**
     * Generates Crystal Spirit Mine building stamp.
     */
    public static Bitmap createSpiritMine(int w, int h) {
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float cx = w * 0.5f;

        // Dark cavern rock
        paint.setColor(0xFF263238);
        Path rock = new Path();
        rock.moveTo(w * 0.15f, h * 0.85f);
        rock.lineTo(w * 0.25f, h * 0.45f);
        rock.lineTo(cx, h * 0.30f);
        rock.lineTo(w * 0.78f, h * 0.40f);
        rock.lineTo(w * 0.88f, h * 0.85f);
        rock.close();
        canvas.drawPath(rock, paint);

        // Cave entrance
        paint.setColor(0xFF0F1417);
        canvas.drawCircle(cx, h * 0.72f, w * 0.20f, paint);

        // Glowing Spirit Crystal cluster
        paint.setColor(COLOR_CYAN);
        Path crystal1 = new Path();
        crystal1.moveTo(cx - 15, h * 0.60f);
        crystal1.lineTo(cx - 8, h * 0.32f);
        crystal1.lineTo(cx, h * 0.60f);
        crystal1.close();
        canvas.drawPath(crystal1, paint);

        paint.setColor(COLOR_JADE_GREEN);
        Path crystal2 = new Path();
        crystal2.moveTo(cx + 5, h * 0.62f);
        crystal2.lineTo(cx + 18, h * 0.38f);
        crystal2.lineTo(cx + 25, h * 0.62f);
        crystal2.close();
        canvas.drawPath(crystal2, paint);

        return bmp;
    }
}
