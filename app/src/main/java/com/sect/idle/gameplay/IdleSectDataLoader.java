package com.sect.idle.gameplay;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * IdleSectDataLoader - Parses and manages Idle Sect v2.0 data models.
 * Pure Java 7/8 syntax compatible with Android API 21+.
 */
public final class IdleSectDataLoader {

    private static IdleSectDataLoader instance;

    // Sect info
    public String sectName = "Cloud Mist Sect";
    public String sectRealm = "Mortal";
    public int ss = 1000;
    public int jade = 50;
    public int essence = 20;
    public String gameTime = "Y1 M1 D2 07:00 Spring";
    public String incomePerDay = "24/day";

    // Disciples
    public final List<DiscipleData> disciples = new ArrayList<DiscipleData>();

    // Buildings
    public final List<BuildingData> buildings = new ArrayList<BuildingData>();

    // Battle
    public String battleRound = "Round 1 | Turn 25";
    public final List<String> battleLog = new ArrayList<String>();
    public int guardianHp = 540;
    public int guardianMaxHp = 720;
    public int guardianSp = 100;
    public int guardianMaxSp = 100;

    public static class DiscipleData {
        public String id;
        public String name;
        public String realm;
        public String stars;
        public int str, agi, intStat, lck;
        public String talent;
        public String personality;
        public String wage;
        public String element;
        public String portrait;
    }

    public static class BuildingData {
        public String id;
        public String name;
        public String icon;
        public String income;
        public int cost;
    }

    private IdleSectDataLoader() {}

    public static synchronized IdleSectDataLoader get() {
        if (instance == null) {
            instance = new IdleSectDataLoader();
        }
        return instance;
    }

    public boolean loadFromAssets(Context context) {
        if (context == null) return false;
        try {
            InputStream is = context.getAssets().open("Data/idle_sect_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            int read = is.read(buffer);
            is.close();
            if (read <= 0) return false;
            String jsonStr = new String(buffer, StandardCharsets.UTF_8);
            return parseJson(jsonStr);
        } catch (Exception e) {
            // Fallback to default in-memory state
            initDefaults();
            return false;
        }
    }

    public boolean parseJson(String jsonStr) {
        try {
            JSONObject root = new JSONObject(jsonStr);
            if (root.has("sect")) {
                JSONObject sect = root.getJSONObject("sect");
                sectName = sect.optString("name", "Cloud Mist Sect");
                sectRealm = sect.optString("realm", "Mortal");
                incomePerDay = sect.optString("income", "24/day");
                gameTime = sect.optString("time", "Y1 M1 D2 07:00 Spring");
                if (sect.has("resources")) {
                    JSONObject res = sect.getJSONObject("resources");
                    ss = res.optInt("SS", 1000);
                    jade = res.optInt("Jade", 50);
                    essence = res.optInt("Essence", 20);
                }
            }

            if (root.has("disciples")) {
                disciples.clear();
                JSONArray arr = root.getJSONArray("disciples");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    DiscipleData d = new DiscipleData();
                    d.id = obj.optString("id", "disciple_" + i);
                    d.name = obj.optString("name", "Disciple");
                    d.realm = obj.optString("realm", "Qi Refining");
                    d.stars = obj.optString("stars", "1-Star");
                    if (obj.has("stats")) {
                        JSONObject st = obj.getJSONObject("stats");
                        d.str = st.optInt("STR", 10);
                        d.agi = st.optInt("AGI", 10);
                        d.intStat = st.optInt("INT", 10);
                        d.lck = st.optInt("LCK", 10);
                    }
                    d.talent = obj.optString("talent", "None");
                    d.personality = obj.optString("personality", "Neutral");
                    d.wage = obj.optString("wage", "50 SS/day");
                    d.element = obj.optString("element", "None");
                    d.portrait = obj.optString("portrait", "");
                    disciples.add(d);
                }
            }

            if (root.has("buildings")) {
                buildings.clear();
                JSONArray arr = root.getJSONArray("buildings");
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.getJSONObject(i);
                    BuildingData b = new BuildingData();
                    b.id = obj.optString("id", "bld_" + i);
                    b.name = obj.optString("name", "Building");
                    b.icon = obj.optString("icon", "");
                    b.income = obj.optString("income", "");
                    b.cost = obj.optInt("cost", 100);
                    buildings.add(b);
                }
            }

            if (root.has("battle")) {
                JSONObject b = root.getJSONObject("battle");
                battleRound = b.optString("round", "Round 1 | Turn 25");
                if (b.has("log")) {
                    battleLog.clear();
                    JSONArray logArr = b.getJSONArray("log");
                    for (int i = 0; i < logArr.length(); i++) {
                        battleLog.add(logArr.getString(i));
                    }
                }
            }
            return true;
        } catch (Exception e) {
            initDefaults();
            return false;
        }
    }

    private void initDefaults() {
        sectName = "Cloud Mist Sect";
        ss = 1000;
        jade = 50;
        essence = 20;
        incomePerDay = "24/day";
        if (disciples.isEmpty()) {
            DiscipleData zk = new DiscipleData();
            zk.id = "zhao_kai";
            zk.name = "Zhao Kai";
            zk.realm = "Qi Refining";
            zk.stars = "2-Star";
            zk.str = 11;
            zk.agi = 21;
            zk.intStat = 60;
            zk.lck = 9;
            zk.talent = "Mind Root (Grade 4)";
            zk.personality = "Wise";
            zk.wage = "146 SS/day";
            disciples.add(zk);
        }
    }
}
