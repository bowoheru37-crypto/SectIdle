package com.sect.idle.gameplay;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SaveManager - SharedPreferences-based persistence for game state.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SaveManager {
    private static final String PREF_NAME = "idle_sect_save";
    private static final String KEY_HAS_SAVE = "has_save";
    private static final String KEY_SPIRIT_STONES = "spirit_stones";
    private static final String KEY_SPIRIT_HERBS = "spirit_herbs";
    private static final String KEY_SPIRIT_ORES = "spirit_ores";
    private static final String KEY_SECT_REALM = "sect_realm";
    private static final String KEY_SECT_EXP = "sect_exp";

    private final SharedPreferences prefs;

    public SaveManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public boolean load() {
        if (!prefs.getBoolean(KEY_HAS_SAVE, false)) {
            return false;
        }

        SectData data = SectData.getInstance();
        data.spiritStones = prefs.getLong(KEY_SPIRIT_STONES, 1000L);
        data.spiritHerbs = prefs.getLong(KEY_SPIRIT_HERBS, 100L);
        data.spiritOres = prefs.getLong(KEY_SPIRIT_ORES, 50L);
        data.sectRealm = prefs.getInt(KEY_SECT_REALM, 0);
        data.sectExp = prefs.getLong(KEY_SECT_EXP, 0L);
        data.recalculateEconomy();
        return true;
    }

    public void save() {
        SectData data = SectData.getInstance();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_HAS_SAVE, true);
        editor.putLong(KEY_SPIRIT_STONES, data.spiritStones);
        editor.putLong(KEY_SPIRIT_HERBS, data.spiritHerbs);
        editor.putLong(KEY_SPIRIT_ORES, data.spiritOres);
        editor.putInt(KEY_SECT_REALM, data.sectRealm);
        editor.putLong(KEY_SECT_EXP, data.sectExp);
        editor.apply();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }

    public void shutdown() {
        save();
    }
}
