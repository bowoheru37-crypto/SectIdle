package com.sect.idle.gameplay;

import java.util.ArrayList;
import java.util.List;

/**
 * TechTreeSystem - Civilization inspired Dao Research & Era Advancement System.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TechTreeSystem {
    private static TechTreeSystem instance;

    public static final int ERA_MORTAL = 0;
    public static final int ERA_QI_REFINING = 1;
    public static final int ERA_CORE_FORMATION = 2;
    public static final int ERA_IMMORTAL_ASCENDANCE = 3;

    public static class TechNode {
        public String id;
        public String name;
        public int era;
        public long researchCostExp;
        public boolean isResearched;
        public String description;

        public TechNode(String id, String name, int era, long researchCostExp, String description) {
            this.id = id;
            this.name = name;
            this.era = era;
            this.researchCostExp = researchCostExp;
            this.isResearched = false;
            this.description = description;
        }
    }

    public int currentEra = ERA_MORTAL;
    public long currentResearchProgress = 0;
    public TechNode activeTech = null;

    private final List<TechNode> techList;

    public static synchronized TechTreeSystem getInstance() {
        if (instance == null) {
            instance = new TechTreeSystem();
        }
        return instance;
    }

    private TechTreeSystem() {
        techList = new ArrayList<TechNode>();
        initTechNodes();
    }

    private void initTechNodes() {
        techList.add(new TechNode("tech_1", "Spiritual Irrigation", ERA_MORTAL, 500, "+20% Farming Crop Yield"));
        techList.add(new TechNode("tech_2", "Automated Alchemy Furnace", ERA_QI_REFINING, 1500, "+30% Alchemy Crafting Speed"));
        techList.add(new TechNode("tech_3", "Grand Spirit Array", ERA_CORE_FORMATION, 4000, "+50% Sect Qi Density & Cultivation Speed"));
        techList.add(new TechNode("tech_4", "Heavenly Dao Link", ERA_IMMORTAL_ASCENDANCE, 10000, "Unlocks Automated Divine Tribulation Shielding"));
    }

    public List<TechNode> getTechList() {
        return techList;
    }

    public boolean selectTechToResearch(String techId) {
        for (int i = 0; i < techList.size(); i++) {
            TechNode node = techList.get(i);
            if (node.id.equals(techId) && !node.isResearched && node.era <= currentEra) {
                activeTech = node;
                return true;
            }
        }
        return false;
    }

    public void addResearchPoints(long points) {
        if (activeTech == null || activeTech.isResearched) return;

        currentResearchProgress += points;
        if (currentResearchProgress >= activeTech.researchCostExp) {
            activeTech.isResearched = true;
            currentResearchProgress = 0;
            checkEraAdvancement();
            activeTech = null;
        }
    }

    private void checkEraAdvancement() {
        int researchedCount = 0;
        for (int i = 0; i < techList.size(); i++) {
            if (techList.get(i).isResearched) {
                researchedCount++;
            }
        }

        if (researchedCount >= 3 && currentEra < ERA_IMMORTAL_ASCENDANCE) {
            currentEra++;
            SectData.getInstance().earn(5000, 500, 250);
        }
    }
}
