package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;

/**
 * TalentSystem - Innate talent generation and cultivation growth calculation.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TalentSystem {
    private TalentSystem() {}

    public static void generateTalent(Disciple d) {
        if (d == null) return;
        int type = RNG.nextInt(GameConfig.TAL_COUNT);
        d.talentType = type;
        d.talentGrade = RNG.nextInt(1, 5);
        d.talentName = GameConfig.TALENT_NAMES[type] + " Root (Grade " + d.talentGrade + ")";

        switch (type) {
            case GameConfig.TAL_BODY:
                d.str += 5 * d.talentGrade;
                d.vit += 5 * d.talentGrade;
                break;
            case GameConfig.TAL_MIND:
                d.intel += 6 * d.talentGrade;
                d.wis += 4 * d.talentGrade;
                break;
            case GameConfig.TAL_SPIRIT:
                d.maxMp += 30 * d.talentGrade;
                d.intel += 4 * d.talentGrade;
                break;
            case GameConfig.TAL_LUCK:
                d.lck += 6 * d.talentGrade;
                break;
            case GameConfig.TAL_DUAL:
                d.str += 3 * d.talentGrade;
                d.intel += 3 * d.talentGrade;
                break;
            case GameConfig.TAL_CHAOS:
                d.str += 3 * d.talentGrade;
                d.agi += 3 * d.talentGrade;
                d.intel += 3 * d.talentGrade;
                d.vit += 3 * d.talentGrade;
                break;
            case GameConfig.TAL_HEAVEN:
                d.str += 5 * d.talentGrade;
                d.agi += 5 * d.talentGrade;
                d.intel += 5 * d.talentGrade;
                d.lck += 5 * d.talentGrade;
                d.vit += 5 * d.talentGrade;
                break;
            default:
                break;
        }
        d.recalcCombat();
    }

    public static void applyGrowth(Disciple d) {
        if (d == null || !d.isAlive()) return;
        int growth = d.talentGrade > 0 ? d.talentGrade : 1;
        d.realmExp += growth * 5;
        if (d.realmExp >= GameConfig.REALM_EXP_CAP) {
            d.realmExp = 0;
            if (d.realm < GameConfig.REALM_MAX - 1) {
                d.realm++;
                d.str += 2;
                d.agi += 2;
                d.intel += 2;
                d.vit += 2;
                d.recalcCombat();
            }
        }
    }
}
