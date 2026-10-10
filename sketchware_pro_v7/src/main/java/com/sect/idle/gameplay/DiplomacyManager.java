package com.sect.idle.gameplay;

import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;
import java.util.List;

/**
 * DiplomacyManager - Romance of the Three Kingdoms & GTA inspired Sect Diplomacy, Alliance, Tributary, and Black Market system.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DiplomacyManager {
    private static DiplomacyManager instance;

    public static class ExternalSect {
        public String id;
        public String name;
        public int power;
        public int relationScore; // -100 (Hostile) to +100 (Allied)
        public boolean isAlly;
        public boolean isTributary;
        public long dailyTributeStones;

        public ExternalSect(String id, String name, int power, int relationScore) {
            this.id = id;
            this.name = name;
            this.power = power;
            this.relationScore = relationScore;
            this.isAlly = relationScore >= 60;
            this.isTributary = false;
            this.dailyTributeStones = 0;
        }
    }

    private final List<ExternalSect> externalSects;

    public static synchronized DiplomacyManager getInstance() {
        if (instance == null) {
            instance = new DiplomacyManager();
        }
        return instance;
    }

    private DiplomacyManager() {
        externalSects = new ArrayList<ExternalSect>();
        initDefaultSects();
    }

    private void initDefaultSects() {
        externalSects.add(new ExternalSect("sect_1", "Heavenly Sword Sect", 1500, 20));
        externalSects.add(new ExternalSect("sect_2", "Blood Demon Pavilion", 2200, -50));
        externalSects.add(new ExternalSect("sect_3", "Verdant Lotus Monastery", 1100, 40));
        externalSects.add(new ExternalSect("sect_4", "Shadow Net Syndicate", 1800, -20));
    }

    public List<ExternalSect> getExternalSects() {
        return externalSects;
    }

    public boolean formAlliance(String sectId) {
        for (int i = 0; i < externalSects.size(); i++) {
            ExternalSect s = externalSects.get(i);
            if (s.id.equals(sectId)) {
                if (s.relationScore >= 50) {
                    s.isAlly = true;
                    s.relationScore = Math.min(100, s.relationScore + 20);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean demandTribute(String sectId) {
        SectData data = SectData.getInstance();
        for (int i = 0; i < externalSects.size(); i++) {
            ExternalSect s = externalSects.get(i);
            if (s.id.equals(sectId)) {
                if (data.sectPower > s.power * 1.3f) {
                    s.isTributary = true;
                    s.dailyTributeStones = s.power / 2;
                    s.relationScore = Math.max(-100, s.relationScore - 30);
                    return true;
                }
            }
        }
        return false;
    }

    public Disciple poachRivalOfficer(String sectId) {
        SectData data = SectData.getInstance();
        if (data.jade < 200) return null; // Cost 200 Jade for poaching

        for (int i = 0; i < externalSects.size(); i++) {
            ExternalSect s = externalSects.get(i);
            if (s.id.equals(sectId)) {
                data.jade -= 200;
                Disciple officer = TalentSystem.generateRecruitDisciple(3); // Epic disciple
                officer.name = "Defector from " + s.name;
                data.addDisciple(officer);
                s.relationScore = Math.max(-100, s.relationScore - 40);
                return officer;
            }
        }
        return null;
    }

    public void processDailyDiplomacy() {
        SectData data = SectData.getInstance();
        for (int i = 0; i < externalSects.size(); i++) {
            ExternalSect s = externalSects.get(i);
            if (s.isTributary && s.dailyTributeStones > 0) {
                data.spiritStones += s.dailyTributeStones;
            }
        }
    }
}
