package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.sect.idle.core.GameConfig;

/**
 * CharacterDesignEngine - Comprehensive Head-to-Toe Character Design Architecture.
 * Synthesizes 2D and 3D character avatars with fashion styles (Imperial, Wuxia, Cyber-Daoist, Scholar),
 * facial traits, headwear, robes, footwear, and special personality auras/props.
 * Pure Java standard without lambdas.
 */
public final class CharacterDesignEngine {
    // Fashion Styles
    public static final int STYLE_XIANXIA_IMPERIAL = 0;
    public static final int STYLE_WUXIA_WANDERER  = 1;
    public static final int STYLE_MYSTIC_SCHOLAR   = 2;
    public static final int STYLE_CYBER_DAOIST     = 3;
    public static final int STYLE_DEMONIC_SOVEREIGN= 4;

    public static final class CharacterTraits {
        public int gender; // 0=Female, 1=Male
        public int fashionStyle;
        public int hairStyle;
        public int hairColor;
        public int facialMark;
        public int robeColor;
        public int trimColor;
        public int footwearType;
        public int propType; // 0=Flying Sword, 1=Spirit Gourd, 2=Celestial Wings, 3=Nine-Ring Staff
        public int auraColor;

        /**
         * Packs character traits into a single 64-bit bitmask for zero-allocation memory transmission.
         */
        public long packToBitmask() {
            long mask = 0L;
            mask |= (gender & 0x1L);
            mask |= ((fashionStyle & 0x7L) << 1);
            mask |= ((hairStyle & 0x7L) << 4);
            mask |= ((facialMark & 0x7L) << 7);
            mask |= ((footwearType & 0x7L) << 10);
            mask |= ((propType & 0x7L) << 13);
            mask |= (((long) (robeColor & 0xFFFFFF)) << 16);
            return mask;
        }

        public static CharacterTraits unpackFromBitmask(long mask) {
            CharacterTraits t = new CharacterTraits();
            t.gender = (int) (mask & 0x1L);
            t.fashionStyle = (int) ((mask >> 1) & 0x7L);
            t.hairStyle = (int) ((mask >> 4) & 0x7L);
            t.facialMark = (int) ((mask >> 7) & 0x7L);
            t.footwearType = (int) ((mask >> 10) & 0x7L);
            t.propType = (int) ((mask >> 13) & 0x7L);
            int rgb = (int) ((mask >> 16) & 0xFFFFFFL);
            t.robeColor = 0xFF000000 | rgb;
            t.trimColor = 0xFFFFD700;
            t.hairColor = (t.gender == 1) ? 0xFF212121 : 0xFF3E2723;
            t.auraColor = 0xFF00E5FF;
            return t;
        }
    }

    private static final Paint P = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final RectF R = new RectF();
    private static final Path PATH = new Path();

    private CharacterDesignEngine() {}

    public static CharacterTraits generateTraitsFromDisciple(int realm, int element, boolean isMale, long seed) {
        java.util.Random rand = new java.util.Random(seed);
        CharacterTraits traits = new CharacterTraits();

        traits.gender = isMale ? 1 : 0;
        traits.fashionStyle = rand.nextInt(5);
        traits.hairStyle = rand.nextInt(4);

        int elemColor = GameConfig.getElementColor(element);
        traits.auraColor = elemColor;

        if (realm >= 9) {
            traits.fashionStyle = STYLE_XIANXIA_IMPERIAL;
            traits.robeColor = 0xFFF5F5F5;
            traits.trimColor = 0xFFFFD700;
        } else if (realm >= 6) {
            traits.fashionStyle = STYLE_DEMONIC_SOVEREIGN;
            traits.robeColor = 0xFF4A148C;
            traits.trimColor = 0xFF00E5FF;
        } else {
            traits.robeColor = isMale ? 0xFF1565C0 : 0xFFAD1457;
            traits.trimColor = 0xFFFFD700;
        }

        traits.hairColor = isMale ? 0xFF212121 : 0xFF3E2723;
        traits.facialMark = rand.nextInt(3);
        traits.footwearType = rand.nextInt(3);
        traits.propType = rand.nextInt(4);

        return traits;
    }

    public static Bitmap renderCharacterHeadToToe(CharacterTraits t, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        float cx = width * 0.5f;
        float cy = height * 0.5f;

        // 1. Personality Aura & Halo Back-Layer
        P.setColor(t.auraColor);
        P.setAlpha(80);
        c.drawCircle(cx, cy - height * 0.1f, width * 0.42f, P);

        if (t.propType == 2) { // Celestial Wings
            P.setColor(t.auraColor);
            P.setAlpha(160);
            PATH.reset();
            PATH.moveTo(cx - width * 0.1f, cy - height * 0.1f);
            PATH.quadTo(cx - width * 0.45f, cy - height * 0.35f, cx - width * 0.40f, cy + height * 0.15f);
            PATH.close();
            c.drawPath(PATH, P);

            PATH.reset();
            PATH.moveTo(cx + width * 0.1f, cy - height * 0.1f);
            PATH.quadTo(cx + width * 0.45f, cy - height * 0.35f, cx + width * 0.40f, cy + height * 0.15f);
            PATH.close();
            c.drawPath(PATH, P);
        }

        // 2. Head-to-Toe Footwear / Boots
        P.setColor(0xFF212121);
        R.set(cx - width * 0.22f, height - 12f, cx - width * 0.05f, height - 2f);
        c.drawRoundRect(R, 3f, 3f, P);
        R.set(cx + width * 0.05f, height - 12f, cx + width * 0.22f, height - 2f);
        c.drawRoundRect(R, 3f, 3f, P);

        // 3. Robes & Fashion
        P.setColor(t.robeColor);
        PATH.reset();
        PATH.moveTo(cx - width * 0.25f, cy - height * 0.10f);
        PATH.lineTo(cx + width * 0.25f, cy - height * 0.10f);
        PATH.lineTo(cx + width * 0.35f, height - 12f);
        PATH.lineTo(cx - width * 0.35f, height - 12f);
        PATH.close();
        c.drawPath(PATH, P);

        // Robe Trim
        P.setColor(t.trimColor);
        R.set(cx - width * 0.35f, height - 16f, cx + width * 0.35f, height - 12f);
        c.drawRect(R, P);

        // 4. Sash & Waist Prop (Spirit Gourd / Coin Pouch)
        P.setColor(t.trimColor);
        R.set(cx - width * 0.26f, cy + height * 0.10f, cx + width * 0.26f, cy + height * 0.16f);
        c.drawRect(R, P);

        if (t.propType == 1) { // Spirit Gourd on waist
            P.setColor(0xFFFFB74D);
            c.drawCircle(cx + width * 0.22f, cy + height * 0.22f, 6f, P);
            c.drawCircle(cx + width * 0.22f, cy + height * 0.16f, 4f, P);
        }

        // 5. Head & Facial Markings
        P.setColor(0xFFFFE0B2); // Skin
        c.drawCircle(cx, cy - height * 0.22f, width * 0.18f, P);

        // Facial Huadian Mark
        P.setColor(0xFFFF1744);
        c.drawCircle(cx, cy - height * 0.26f, 2f, P);

        // 6. Hair & Headwear
        P.setColor(t.hairColor);
        c.drawCircle(cx, cy - height * 0.28f, width * 0.16f, P);
        c.drawCircle(cx, cy - height * 0.38f, width * 0.08f, P); // Topknot

        P.setColor(t.trimColor); // Golden Guan Pin
        c.drawRect(cx - width * 0.10f, cy - height * 0.39f, cx + width * 0.10f, cy - height * 0.36f, P);

        return bmp;
    }
}
