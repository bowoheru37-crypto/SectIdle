package com.sect.idle.utils;

import com.sect.idle.core.MathUtils;

/**
 * DataValidator v2.0 - Runtime validation for game entities and save states.
 */
public final class DataValidator {
    private DataValidator() {}

    public static boolean isValidString(String s, int maxLen) {
        return s != null && s.length() <= maxLen && !s.contains("|") && !s.contains("\\n");
    }

    public static String sanitizeString(String s, int maxLen) {
        if (s == null) return "";
        s = s.replace("|", "").replace("\\n", " ").trim();
        if (s.length() > maxLen) s = s.substring(0, maxLen);
        return s;
    }

    public static boolean isValidInt(int value, int min, int max) {
        return value >= min && value <= max;
    }

    public static int clampInt(int value, int min, int max) {
        return MathUtils.clamp(value, min, max);
    }

    public static boolean isValidFloat(float value, float min, float max) {
        return !Float.isNaN(value) && !Float.isInfinite(value) && value >= min && value <= max;
    }

    public static float clampFloat(float value, float min, float max) {
        if (Float.isNaN(value) || Float.isInfinite(value)) return min;
        return MathUtils.clamp(value, min, max);
    }

    public static boolean isValidColor(int color) {
        return (color >>> 24) != 0;
    }

    public static boolean isValidArray(Object[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidArray(int[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidArray(float[] arr, int maxLen) {
        return arr != null && arr.length <= maxLen;
    }

    public static boolean isValidName(String name) {
        if (name == null || name.isEmpty() || name.length() > 20) return false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c) && c != ' ' && c != '-' && c != '_') return false;
        }
        return true;
    }

    public static boolean isValidPosition(float x, float y, float maxRange) {
        return !Float.isNaN(x) && !Float.isNaN(y) && !Float.isInfinite(x) && !Float.isInfinite(y)
            && Math.abs(x) <= maxRange && Math.abs(y) <= maxRange;
    }

    public static boolean isValidPercentage(float p) {
        return !Float.isNaN(p) && !Float.isInfinite(p) && p >= 0f && p <= 1f;
    }

    public static boolean isValidId(int id, int maxId) {
        return id >= 0 && id < maxId;
    }

    public static String validateSaveData(String data) {
        if (data == null) return null;
        if (data.length() > 500000) return null;
        if (data.contains("<script") || data.contains("javascript:")) return null;
        return data;
    }

    public static class ValidationResult {
        public boolean valid;
        public String error;
        public int errorCode;

        public ValidationResult(boolean v, String e, int code) {
            valid = v; error = e; errorCode = code;
        }

        public static ValidationResult ok() { return new ValidationResult(true, null, 0); }
        public static ValidationResult fail(String e, int code) { return new ValidationResult(false, e, code); }
    }

    public static ValidationResult validateDiscipleData(String name, int realm, int hp, int maxHp, int element) {
        if (!isValidName(name)) return ValidationResult.fail("Invalid name", 1);
        if (!isValidInt(realm, 0, 14)) return ValidationResult.fail("Invalid realm", 2);
        if (!isValidInt(hp, 0, maxHp)) return ValidationResult.fail("Invalid HP", 3);
        if (!isValidInt(element, 0, 11)) return ValidationResult.fail("Invalid element", 4);
        return ValidationResult.ok();
    }
}
