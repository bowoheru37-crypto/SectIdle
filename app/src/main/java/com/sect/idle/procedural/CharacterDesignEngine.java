package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.sect.idle.core.GameConfig;

/**
 * CharacterDesignEngine - WWE Smackdown x Cultivator Fusion Architecture.
 * Synthesizes 2D and 3D character avatars with Body Cultivator vs Spirit/Martial Cultivator morphs,
 * muscle flex & boots system (WWE base lace-up, rogue bamboo, jade dragon scale, combat scale),
 * fauld jade scales, and knee compression buffers.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class CharacterDesignEngine {
    // Fashion Styles
    public static final int STYLE_XIANXIA_IMPERIAL = 0;
    public static final int STYLE_WUXIA_WANDERER  = 1;
    public static final int STYLE_MYSTIC_SCHOLAR   = 2;
    public static final int STYLE_CYBER_DAOIST     = 3;
    public static final int STYLE_DEMONIC_SOVEREIGN= 4;

    // Boot & Footwear Archetypes (WWE x Cultivator Fusion)
    public static final int BOOT_WWE_WRESTLING_LACEUP  = 0; // Black leather + gold buckles
    public static final int BOOT_CULTIVATOR_ROGUE_BAMBOO= 1; // Rustic canvas + bamboo weave
    public static final int BOOT_JADE_DRAGON_SCALE     = 2; // Saintess/Pill Master glowing dragon scale
    public static final int BOOT_COMBAT_SCALE_MALE     = 3; // Male body cultivator armored combat boots

    // Cultivator Physique Archetypes
    public static final int PHYSIQUE_SPIRIT_LEAN  = 0; // Lean martial/spirit, Bianca Belair type
    public static final int PHYSIQUE_BODY_WRESTLER= 1; // Brock Lesnar / Shi Hao type (90-100% muscle mass)

    public static final class CharacterTraits {
        public int gender; // 0=Female, 1=Male
        public int physiqueType; // 0=Lean, 1=Body Cultivator
        public int fashionStyle;
        public int hairStyle;
        public int hairColor;
        public int facialMark;
        public int robeColor;
        public int trimColor;
        public int footwearType; // 0 to 3 boot types
        public int propType;
        public int auraColor;
        public float muscleFlexScale; // 0.0f to 1.0f (muscle flex morph)

        public CharacterTraits() {
            this.gender = 1;
            this.physiqueType = PHYSIQUE_BODY_WRESTLER;
            this.fashionStyle = STYLE_XIANXIA_IMPERIAL;
            this.hairStyle = 0;
            this.hairColor = 0xFF212121;
            this.facialMark = 0;
            this.robeColor = 0xFF1565C0;
            this.trimColor = 0xFFFFD700;
            this.footwearType = BOOT_WWE_WRESTLING_LACEUP;
            this.propType = 0;
            this.auraColor = 0xFF00E5FF;
            this.muscleFlexScale = 0.0f;
        }

        public long packToBitmask() {
            long mask = 0L;
            mask |= (gender & 0x1L);
            mask |= ((physiqueType & 0x1L) << 1);
            mask |= ((fashionStyle & 0x7L) << 2);
            mask |= ((hairStyle & 0x7L) << 5);
            mask |= ((facialMark & 0x7L) << 8);
            mask |= ((footwearType & 0x7L) << 11);
            mask |= ((propType & 0x7L) << 14);
            mask |= (((long) (robeColor & 0xFFFFFF)) << 17);
            return mask;
        }

        public static CharacterTraits unpackFromBitmask(long mask) {
            CharacterTraits t = new CharacterTraits();
            t.gender = (int) (mask & 0x1L);
            t.physiqueType = (int) ((mask >> 1) & 0x1L);
            t.fashionStyle = (int) ((mask >> 2) & 0x7L);
            t.hairStyle = (int) ((mask >> 5) & 0x7L);
            t.facialMark = (int) ((mask >> 8) & 0x7L);
            t.footwearType = (int) ((mask >> 11) & 0x7L);
            t.propType = (int) ((mask >> 14) & 0x7L);
            int rgb = (int) ((mask >> 17) & 0xFFFFFFL);
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
        traits.physiqueType = isMale ? (rand.nextBoolean() ? PHYSIQUE_BODY_WRESTLER : PHYSIQUE_SPIRIT_LEAN) : PHYSIQUE_SPIRIT_LEAN;
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
        traits.footwearType = rand.nextInt(4);
        traits.propType = rand.nextInt(4);
        traits.muscleFlexScale = traits.physiqueType == PHYSIQUE_BODY_WRESTLER ? 0.25f : 0.05f;

        return traits;
    }

    public static Bitmap renderCharacterHeadToToe(CharacterTraits t, int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);

        float cx = width * 0.5f;
        float cy = height * 0.5f;

        boolean isMale = (t.gender == 1);
        boolean isBody = (t.physiqueType == PHYSIQUE_BODY_WRESTLER);
        float flex = t.muscleFlexScale;

        // 1. Background Aura
        P.setColor(t.auraColor);
        P.setAlpha(70);
        c.drawCircle(cx, cy - height * 0.1f, width * 0.44f, P);

        // 2. Legs & WWE Boots (Hip to Feet)
        float legWidth = (isMale ? 0.16f : 0.11f) * width * (1.0f + flex * 0.15f);
        float legH = height * 0.35f;
        float legY = height - legH - 4f;

        // Thighs / Quadriceps
        P.setColor(isBody ? 0xFFFFCC80 : 0xFFFFE0B2);
        R.set(cx - legWidth * 2.2f, legY, cx - legWidth * 0.3f, legY + legH * 0.6f);
        c.drawRoundRect(R, 6f, 6f, P);
        R.set(cx + legWidth * 0.3f, legY, cx + legWidth * 2.2f, legY + legH * 0.6f);
        c.drawRoundRect(R, 6f, 6f, P);

        // Knee Pads with Jade Gem
        P.setColor(0xFF1E88E5);
        if (isBody) P.setColor(0xFFFFD700);
        R.set(cx - legWidth * 2.3f, legY + legH * 0.52f, cx - legWidth * 0.2f, legY + legH * 0.70f);
        c.drawRoundRect(R, 4f, 4f, P);
        R.set(cx + legWidth * 0.2f, legY + legH * 0.52f, cx + legWidth * 2.3f, legY + legH * 0.70f);
        c.drawRoundRect(R, 4f, 4f, P);

        // Central Jade Stone on Knee
        P.setColor(0xFF00E676);
        c.drawCircle(cx - legWidth * 1.25f, legY + legH * 0.61f, 3.5f, P);
        c.drawCircle(cx + legWidth * 1.25f, legY + legH * 0.61f, 3.5f, P);

        // WWE Boots Rendering
        switch (t.footwearType) {
            case BOOT_JADE_DRAGON_SCALE:
                P.setColor(0xFF00BFA5); // Glowing Jade
                break;
            case BOOT_COMBAT_SCALE_MALE:
                P.setColor(0xFF263238); // Armored Bronze/Dark Steel
                break;
            case BOOT_CULTIVATOR_ROGUE_BAMBOO:
                P.setColor(0xFF795548); // Rustic Canvas / Bamboo
                break;
            case BOOT_WWE_WRESTLING_LACEUP:
            default:
                P.setColor(0xFF1A1A1A); // Black WWE Leather
                break;
        }

        float bootTop = legY + legH * 0.68f;
        float bootBottom = height - 2f;
        R.set(cx - legWidth * 2.3f, bootTop, cx - legWidth * 0.2f, bootBottom);
        c.drawRoundRect(R, 4f, 4f, P);
        R.set(cx + legWidth * 0.2f, bootTop, cx + legWidth * 2.3f, bootBottom);
        c.drawRoundRect(R, 4f, 4f, P);

        // Boot Gold Buckle Trim
        P.setColor(0xFFFFD700);
        c.drawRect(cx - legWidth * 2.3f, bootTop + 4f, cx - legWidth * 0.2f, bootTop + 7f, P);
        c.drawRect(cx + legWidth * 0.2f, bootTop + 4f, cx + legWidth * 2.3f, bootTop + 7f, P);

        // 3. Torso / Robe / Loincloth
        float shoulderW = (isMale ? 0.32f : 0.24f) * width * (1.0f + flex * 0.12f);
        float torsoTop = cy - height * 0.18f;

        P.setColor(t.robeColor);
        PATH.reset();
        PATH.moveTo(cx - shoulderW, torsoTop);
        PATH.lineTo(cx + shoulderW, torsoTop);
        PATH.lineTo(cx + shoulderW * (isMale ? 0.85f : 1.25f), legY + 8f);
        PATH.lineTo(cx - shoulderW * (isMale ? 0.85f : 1.25f), legY + 8f);
        PATH.close();
        c.drawPath(PATH, P);

        // Gold Cloud Embroidery & Belt
        P.setColor(t.trimColor);
        R.set(cx - shoulderW * 0.9f, legY - 6f, cx + shoulderW * 0.9f, legY + 2f);
        c.drawRoundRect(R, 2f, 2f, P);

        // 4. Head & Face
        P.setColor(0xFFFFE0B2);
        c.drawCircle(cx, cy - height * 0.26f, width * (isMale ? 0.16f : 0.14f), P);

        // Hair
        P.setColor(t.hairColor);
        c.drawCircle(cx, cy - height * 0.31f, width * 0.15f, P);
        c.drawCircle(cx, cy - height * 0.40f, width * 0.08f, P); // Topknot

        P.setColor(t.trimColor);
        c.drawRect(cx - width * 0.08f, cy - height * 0.41f, cx + width * 0.08f, cy - height * 0.39f, P);

        return bmp;
    }
}
