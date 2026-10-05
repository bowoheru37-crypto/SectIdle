package com.sect.idle.gameplay;

import java.util.ArrayList;
import java.util.List;

/**
 * TerritoryManager - GTA & RoTK inspired Territory Control and Black Market Turf System.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TerritoryManager {
    private static TerritoryManager instance;

    public static class TerritoryNode {
        public String id;
        public String name;
        public int requiredPower;
        public boolean isControlled;
        public long dailyIncomeSS;

        public TerritoryNode(String id, String name, int requiredPower, long dailyIncomeSS) {
            this.id = id;
            this.name = name;
            this.requiredPower = requiredPower;
            this.dailyIncomeSS = dailyIncomeSS;
            this.isControlled = false;
        }
    }

    private final List<TerritoryNode> territories;

    public static synchronized TerritoryManager getInstance() {
        if (instance == null) {
            instance = new TerritoryManager();
        }
        return instance;
    }

    private TerritoryManager() {
        territories = new ArrayList<TerritoryNode>();
        initTerritories();
    }

    private void initTerritories() {
        territories.add(new TerritoryNode("t_1", "Dragon Vein Spirit Mine", 1200, 1000));
        territories.add(new TerritoryNode("t_2", "Black Market District", 2500, 2500));
        territories.add(new TerritoryNode("t_3", "Ancient Ruin Valley", 5000, 6000));
    }

    public List<TerritoryNode> getTerritories() {
        return territories;
    }

    public boolean captureTerritory(String id) {
        SectData data = SectData.getInstance();
        for (int i = 0; i < territories.size(); i++) {
            TerritoryNode t = territories.get(i);
            if (t.id.equals(id) && !t.isControlled) {
                if (data.sectPower >= t.requiredPower) {
                    t.isControlled = true;
                    return true;
                }
            }
        }
        return false;
    }

    public void collectTerritoryYield() {
        SectData data = SectData.getInstance();
        for (int i = 0; i < territories.size(); i++) {
            TerritoryNode t = territories.get(i);
            if (t.isControlled) {
                data.spiritStones += t.dailyIncomeSS;
            }
        }
    }
}
