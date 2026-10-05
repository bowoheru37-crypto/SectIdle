package com.sect.idle.gameplay;

import com.sect.idle.models.Building;
import java.util.ArrayList;
import java.util.List;

/**
 * BaseDefenseManager - Clash of Clans inspired Base Defense Layout, Walls, and Defense Towers.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BaseDefenseManager {
    private static BaseDefenseManager instance;

    public static class DefenseStructure {
        public String id;
        public String name;
        public int type; // 0=Wall, 1=Arrow Tower, 2=Qi Cannon, 3=Trap
        public int level;
        public int hp;
        public int maxHp;
        public int atk;

        public DefenseStructure(String id, String name, int type, int level, int maxHp, int atk) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.level = level;
            this.hp = maxHp;
            this.maxHp = maxHp;
            this.atk = atk;
        }
    }

    private final List<DefenseStructure> defenses;

    public static synchronized BaseDefenseManager getInstance() {
        if (instance == null) {
            instance = new BaseDefenseManager();
        }
        return instance;
    }

    private BaseDefenseManager() {
        defenses = new ArrayList<DefenseStructure>();
        initDefaultDefenses();
    }

    private void initDefaultDefenses() {
        defenses.add(new DefenseStructure("d_1", "Iron-Wood Defense Wall", 0, 1, 500, 0));
        defenses.add(new DefenseStructure("d_2", "Spiritual Qi Arrow Tower", 1, 1, 300, 45));
        defenses.add(new DefenseStructure("d_3", "Nine-Yang Flame Cannon", 2, 1, 450, 90));
    }

    public List<DefenseStructure> getDefenses() {
        return defenses;
    }

    public boolean buildStructure(String name, int type) {
        SectData data = SectData.getInstance();
        if (data.spiritStones < 500) return false;

        data.spend(500, 0, 0);
        String id = "d_" + (defenses.size() + 1);
        int hp = 300 + (type * 100);
        int atk = type == 0 ? 0 : 30 * type;
        defenses.add(new DefenseStructure(id, name, type, 1, hp, atk));
        return true;
    }

    public int getTotalDefensePower() {
        int total = 0;
        for (int i = 0; i < defenses.size(); i++) {
            DefenseStructure d = defenses.get(i);
            total += d.atk + (d.hp / 10);
        }
        return total;
    }
}
