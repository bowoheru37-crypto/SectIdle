package com.sect.idle.gameplay;

import java.util.ArrayList;

/**
 * SecretManager - Manages secret unlockables and achievements.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class SecretManager {
    private static final ArrayList<String> unlockedSecrets = new ArrayList<String>();

    private SecretManager() {}

    public static boolean checkUnlock(String secretKey) {
        if (secretKey == null) return false;
        if (!unlockedSecrets.contains(secretKey)) {
            unlockedSecrets.add(secretKey);
            return true;
        }
        return false;
    }

    public static boolean isUnlocked(String secretKey) {
        return unlockedSecrets.contains(secretKey);
    }
}
