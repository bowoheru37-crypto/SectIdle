package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import java.util.ArrayList;
import java.util.List;

/**
 * RuneSystem - Summoners War inspired Rune Set Equipment & Disciple Grade Evolution (3★ -> 6★).
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class RuneSystem {
    private static RuneSystem instance;

    public static final int RUNE_SET_SWIFT = 1;  // +25% Speed
    public static final int RUNE_SET_FATAL = 2;  // +35% ATK
    public static final int RUNE_SET_DESPAIR = 3;// +15% Stun Chance

    public static class Rune {
        public String id;
        public String name;
        public int slot; // 1 to 6
        public int setType;
        public int star;
        public int bonusAtk;
        public int bonusSpd;

        public Rune(String id, String name, int slot, int setType, int star, int bonusAtk, int bonusSpd) {
            this.id = id;
            this.name = name;
            this.slot = slot;
            this.setType = setType;
            this.star = star;
            this.bonusAtk = bonusAtk;
            this.bonusSpd = bonusSpd;
        }
    }

    public static synchronized RuneSystem getInstance() {
        if (instance == null) {
            instance = new RuneSystem();
        }
        return instance;
    }

    private RuneSystem() {}

    public boolean evolveDiscipleStar(Disciple d) {
        if (d == null) return false;
        SectData data = SectData.getInstance();
        if (data.spiritStones < 2000) return false;

        data.spend(2000, 0, 0);
        d.talentGrade = Math.min(6, d.talentGrade + 1);
        d.str += 10;
        d.agi += 10;
        d.intel += 10;
        d.vit += 10;
        d.recalcCombat();
        return true;
    }

    public void applyRuneSetBonus(Disciple d, List<Rune> equippedRunes) {
        if (d == null || equippedRunes == null) return;

        int swiftCount = 0;
        int fatalCount = 0;

        for (int i = 0; i < equippedRunes.size(); i++) {
            Rune r = equippedRunes.get(i);
            if (r.setType == RUNE_SET_SWIFT) swiftCount++;
            if (r.setType == RUNE_SET_FATAL) fatalCount++;
        }

        if (swiftCount >= 4) {
            d.spd = (int) (d.spd * 1.25f);
        }
        if (fatalCount >= 4) {
            d.atk = (int) (d.atk * 1.35f);
        }
        d.recalcCombat();
    }
}
