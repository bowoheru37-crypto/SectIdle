package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;
import java.util.List;

/**
 * BehemothHuntManager - Monster Hunter inspired Legendary Beast Raids, Carving, and Artifact Crafting.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BehemothHuntManager {
    private static BehemothHuntManager instance;

    public static class Behemoth {
        public String id;
        public String name;
        public int element;
        public int hp;
        public int maxHp;
        public int power;
        public String carveMaterial;

        public Behemoth(String id, String name, int element, int maxHp, int power, String carveMaterial) {
            this.id = id;
            this.name = name;
            this.element = element;
            this.hp = maxHp;
            this.maxHp = maxHp;
            this.power = power;
            this.carveMaterial = carveMaterial;
        }
    }

    private final List<Behemoth> beasts;
    public int carvedScales = 0;
    public int carvedHorns = 0;

    public static synchronized BehemothHuntManager getInstance() {
        if (instance == null) {
            instance = new BehemothHuntManager();
        }
        return instance;
    }

    private BehemothHuntManager() {
        beasts = new ArrayList<Behemoth>();
        initBeasts();
    }

    private void initBeasts() {
        beasts.add(new Behemoth("b_1", "Ancient Flame Dragon", 1, 10000, 1500, "Dragon Scale"));
        beasts.add(new Behemoth("b_2", "Glacial Kirin", 9, 15000, 2800, "Kirin Horn"));
        beasts.add(new Behemoth("b_3", "Thunderous Behemoth", 10, 25000, 5000, "Thunder Core"));
    }

    public List<Behemoth> getBeasts() {
        return beasts;
    }

    public boolean huntBehemoth(String beastId, List<Disciple> party) {
        if (party == null || party.isEmpty()) return false;

        int totalPower = 0;
        for (int i = 0; i < party.size(); i++) {
            Disciple d = party.get(i);
            if (d != null && d.isAlive()) {
                totalPower += (int) d.getPowerRating();
            }
        }

        for (int i = 0; i < beasts.size(); i++) {
            Behemoth b = beasts.get(i);
            if (b.id.equals(beastId)) {
                if (totalPower >= b.power) {
                    // Successful Hunt & Carve
                    carvedScales += RNG.nextInt(2, 5);
                    carvedHorns += RNG.nextInt(1, 3);
                    SectData.getInstance().earn(1000, 200, 100);
                    return true;
                }
            }
        }
        return false;
    }
}
