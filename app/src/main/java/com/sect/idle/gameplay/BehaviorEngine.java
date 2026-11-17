package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BehaviorEngine - Social interactions, disciple bonds (Sect Siblings / Master-Disciple), and mood dynamics.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BehaviorEngine {
    public static final String BOND_SIBLING_PREFIX = "SECT_SIBLINGS:";
    public static final String BOND_MASTER_PREFIX = "MASTER_DISCIPLE:";

    private BehaviorEngine() {}

    public static void updateBehavior(Disciple d, ArrayList<Disciple> allDisciples) {
        if (d == null || !d.isAlive() || allDisciples == null || allDisciples.size() <= 1) return;

        if (RNG.chance(20)) {
            int targetIdx = RNG.nextInt(allDisciples.size());
            Disciple target = allDisciples.get(targetIdx);
            if (target != null && target != d && target.isAlive()) {
                if (d.personality == target.personality || RNG.chance(60)) {
                    d.mood = Math.min(100, d.mood + 5);
                    d.stress = Math.max(0, d.stress - 5);
                    target.mood = Math.min(100, target.mood + 5);
                    if (d.currentTask != GameConfig.TASK_NONE && d.currentTask == target.currentTask) {
                        developOrDeepenBond(d, target);
                    }
                } else {
                    d.stress = Math.min(100, d.stress + 5);
                    target.stress = Math.min(100, target.stress + 5);
                }
            }
        }
    }

    public static void developOrDeepenBond(Disciple a, Disciple b) {
        if (a == null || b == null || a == b || a.id == null || b.id == null) return;
        if (hasBondWith(a, b.id) || hasBondWith(b, a.id)) return;
        String prefix = (Math.abs(a.realm - b.realm) >= 1 || Math.abs(a.age - b.age) >= 6)
                ? BOND_MASTER_PREFIX
                : BOND_SIBLING_PREFIX;
        if (a.relationships.size() < 4) a.relationships.add(prefix + b.id);
        if (b.relationships.size() < 4) b.relationships.add(prefix + a.id);
        a.mood = Math.min(100, a.mood + 4);
        b.mood = Math.min(100, b.mood + 4);
    }

    public static boolean hasBondWith(Disciple d, String partnerId) {
        if (d == null || partnerId == null) return false;
        for (int i = 0; i < d.relationships.size(); i++) {
            String rel = d.relationships.get(i);
            if (rel != null && (rel.equals(BOND_SIBLING_PREFIX + partnerId) || rel.equals(BOND_MASTER_PREFIX + partnerId))) {
                return true;
            }
        }
        return false;
    }

    public static int getSharedTaskBondBonus(Disciple d, ArrayList<Disciple> allDisciples) {
        if (d == null || d.currentTask == GameConfig.TASK_NONE || allDisciples == null || d.relationships.isEmpty()) {
            return 0;
        }
        int bonus = 0;
        for (int i = 0; i < d.relationships.size(); i++) {
            String rel = d.relationships.get(i);
            if (rel == null) continue;
            boolean isMaster = rel.startsWith(BOND_MASTER_PREFIX);
            boolean isSibling = rel.startsWith(BOND_SIBLING_PREFIX);
            if (!isMaster && !isSibling) continue;
            String partnerId = isMaster ? rel.substring(BOND_MASTER_PREFIX.length()) : rel.substring(BOND_SIBLING_PREFIX.length());
            for (int j = 0; j < allDisciples.size(); j++) {
                Disciple partner = allDisciples.get(j);
                if (partner != null && partner.isAlive() && partnerId.equals(partner.id) && partner.currentTask == d.currentTask) {
                    bonus += isMaster ? 8 : 6;
                    break;
                }
            }
        }
        return Math.min(25, bonus);
    }
}
