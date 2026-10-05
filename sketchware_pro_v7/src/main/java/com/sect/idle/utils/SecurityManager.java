package com.sect.idle.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

/**
 * SecurityManager v2.0 - Save data integrity check and anti-tampering checksum.
 */
public final class SecurityManager {
    private static final String PREFS_SEC = "IdleSectSec";
    private static final String KEY_HASH = "data_hash";
    private static final String KEY_SALT = "salt";
    private static final String KEY_DEVICE = "device_id";
    private static SecurityManager instance;

    private final SharedPreferences prefs;
    private final String deviceId;
    private final CRC32 crc32;

    private SecurityManager(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(PREFS_SEC, Context.MODE_PRIVATE);
        String devId = "";
        try {
            devId = Settings.Secure.getString(ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception ignored) {}
        deviceId = devId != null ? devId : "unknown_device";
        crc32 = new CRC32();
        if (!prefs.contains(KEY_SALT)) {
            prefs.edit().putString(KEY_SALT, generateSalt()).apply();
        }
    }

    public static synchronized SecurityManager get(Context ctx) {
        if (instance == null) instance = new SecurityManager(ctx);
        return instance;
    }

    private String generateSalt() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) sb.append((char)(65 + (int)(Math.random() * 26)));
        return sb.toString();
    }

    public String computeHash(String data) {
        if (data == null) data = "";
        String salt = prefs.getString(KEY_SALT, "DEFAULT");
        String combined = data + salt + deviceId;
        crc32.reset();
        crc32.update(combined.getBytes());
        return Long.toHexString(crc32.getValue());
    }

    public String computeMD5(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder(32);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return computeHash(data);
        }
    }

    public void saveHash(String data) {
        prefs.edit().putString(KEY_HASH, computeHash(data)).apply();
    }

    public boolean verifyHash(String data) {
        String saved = prefs.getString(KEY_HASH, "");
        return saved.equals(computeHash(data));
    }

    public boolean verifyIntegrity(String data, String expectedHash) {
        return expectedHash != null && expectedHash.equals(computeHash(data));
    }

    public String obfuscate(String data) {
        if (data == null) return "";
        String salt = prefs.getString(KEY_SALT, "DEFAULT");
        if (salt.length() == 0) salt = "DEFAULT";
        StringBuilder sb = new StringBuilder(data.length() * 2);
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            char s = salt.charAt(i % salt.length());
            sb.append((char)(c ^ s));
        }
        return sb.toString();
    }

    public String deobfuscate(String data) {
        return obfuscate(data);
    }

    public boolean isDeviceChanged() {
        String saved = prefs.getString(KEY_DEVICE, "");
        if (saved.isEmpty()) {
            prefs.edit().putString(KEY_DEVICE, deviceId).apply();
            return false;
        }
        return !saved.equals(deviceId);
    }

    public void reset() {
        prefs.edit().clear().apply();
        instance = null;
    }
}
