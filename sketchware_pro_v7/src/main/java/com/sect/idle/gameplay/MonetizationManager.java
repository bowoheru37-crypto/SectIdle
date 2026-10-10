package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;
import java.util.List;

/**
 * MonetizationManager - Manages Purchases, VIP, Rewarded Boosts, and Gacha Banner.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class MonetizationManager {
    private static MonetizationManager instance;

    // Billing / Ad callback listeners for external SDK integration (Google Play Billing / AdMob)
    public interface PurchaseCallback {
        void onSuccess(String productId);
        void onFailure(String productId, String error);
    }

    public interface AdCallback {
        void onAdRewarded(String adType, int rewardValue);
        void onAdFailed(String adType, String error);
    }

    // VIP Pass Status
    public boolean vipActive = false;
    public long vipExpirationTime = 0; // Epoch millis
    public long lastVipClaimDay = -1;

    // Rewarded Boosts (In-game timestamp or system timestamp)
    public long speedBoostEndTime = 0; // 2x speed active until
    public float activeSpeedMultiplier = 1.0f;

    // Gacha Pity Counters
    public int gachaPityCounter = 0;
    public static final int GACHA_PITY_THRESHOLD = 50; // Guaranteed Mythic on 50th pull
    public static final long GACHA_SINGLE_COST_JADE = 100;
    public static final long GACHA_TEN_COST_JADE = 900;

    // Callbacks
    private PurchaseCallback purchaseCallback;
    private AdCallback adCallback;

    public static synchronized MonetizationManager getInstance() {
        if (instance == null) {
            instance = new MonetizationManager();
        }
        return instance;
    }

    private MonetizationManager() {}

    public void setPurchaseCallback(PurchaseCallback cb) {
        this.purchaseCallback = cb;
    }

    public void setAdCallback(AdCallback cb) {
        this.adCallback = cb;
    }

    // --- VIP MANAGEMENT ---
    public boolean isVipActive() {
        if (vipActive) {
            if (System.currentTimeMillis() > vipExpirationTime) {
                vipActive = false;
                return false;
            }
            return true;
        }
        return false;
    }

    public void activateVipDays(int days) {
        long duration = days * 86400000L;
        if (isVipActive()) {
            vipExpirationTime += duration;
        } else {
            vipActive = true;
            vipExpirationTime = System.currentTimeMillis() + duration;
        }
    }

    public boolean claimDailyVipReward() {
        if (!isVipActive()) return false;
        SectData data = SectData.getInstance();
        if (data.time != null) {
            long currentDay = data.time.totalDays;
            if (lastVipClaimDay < currentDay) {
                lastVipClaimDay = currentDay;
                data.jade += 100;
                data.spiritStones += 5000;
                data.earn(5000, 500, 250);
                return true;
            }
        }
        return false;
    }

    // --- REWARDED AD BOOSTS ---
    public void watchAdForSpeedBoost(int minutes) {
        // Local Mock execution
        triggerAdReward("speed_boost", minutes);
    }

    public void watchAdForInstantResources() {
        triggerAdReward("instant_resources", 1);
    }

    public void watchAdForFreeGacha() {
        triggerAdReward("free_gacha", 1);
    }

    public void triggerAdReward(String adType, int param) {
        SectData data = SectData.getInstance();
        if ("speed_boost".equals(adType)) {
            long durationMs = param * 60 * 1000L;
            speedBoostEndTime = System.currentTimeMillis() + durationMs;
            activeSpeedMultiplier = 2.0f;
            if (adCallback != null) adCallback.onAdRewarded(adType, param);
        } else if ("instant_resources".equals(adType)) {
            long yieldSS = data.dailyIncomeSS * 2;
            if (yieldSS < 2000) yieldSS = 2000;
            data.spiritStones += yieldSS;
            data.spiritHerbs += 200;
            data.spiritOres += 100;
            if (adCallback != null) adCallback.onAdRewarded(adType, (int) yieldSS);
        } else if ("free_gacha".equals(adType)) {
            performSingleGachaPull(true);
            if (adCallback != null) adCallback.onAdRewarded(adType, 1);
        }
    }

    public float getGameSpeedMultiplier() {
        if (System.currentTimeMillis() < speedBoostEndTime) {
            return 2.0f;
        }
        return 1.0f;
    }

    // --- IN-APP PURCHASES (MOCK / GOOGLE PLAY BILLING READY) ---
    public boolean buyJadePack(int packId) {
        long jadeAmount = 0;
        long priceUsd = 0;
        String productId = "jade_pack_" + packId;

        switch (packId) {
            case 1:
                jadeAmount = 500;
                break;
            case 2:
                jadeAmount = 1200;
                break;
            case 3:
                jadeAmount = 3000;
                break;
            case 4:
                jadeAmount = 8000;
                break;
            default:
                jadeAmount = 100;
                break;
        }

        // Apply Jade
        SectData.getInstance().jade += jadeAmount;
        if (purchaseCallback != null) {
            purchaseCallback.onSuccess(productId);
        }
        return true;
    }

    public boolean buyVipPass() {
        activateVipDays(30);
        if (purchaseCallback != null) {
            purchaseCallback.onSuccess("vip_monthly_pass");
        }
        return true;
    }

    // --- GACHA DISCIPLE RECRUITMENT BANNER ---
    public Disciple performSingleGachaPull(boolean isFree) {
        SectData data = SectData.getInstance();
        if (!isFree) {
            if (data.jade < GACHA_SINGLE_COST_JADE) {
                return null;
            }
            data.jade -= GACHA_SINGLE_COST_JADE;
        }

        gachaPityCounter++;
        int rarity = GameConfig.RARITY_COMMON;

        if (gachaPityCounter >= GACHA_PITY_THRESHOLD) {
            rarity = GameConfig.RARITY_MYTHIC;
            gachaPityCounter = 0;
        } else {
            int roll = RNG.nextInt(1000); // 0-999
            if (roll < 5) { // 0.5% Mythic
                rarity = GameConfig.RARITY_MYTHIC;
                gachaPityCounter = 0;
            } else if (roll < 30) { // 2.5% Legend
                rarity = GameConfig.RARITY_LEGEND;
            } else if (roll < 100) { // 7% Epic
                rarity = GameConfig.RARITY_EPIC;
            } else if (roll < 300) { // 20% Rare
                rarity = GameConfig.RARITY_RARE;
            } else if (roll < 600) { // 30% Uncommon
                rarity = GameConfig.RARITY_UNCOMMON;
            } else {
                rarity = GameConfig.RARITY_COMMON;
            }
        }

        Disciple d = TalentSystem.generateRecruitDisciple(rarity);
        if (d != null) {
            data.addDisciple(d);
        }
        return d;
    }

    public List<Disciple> performTenGachaPulls() {
        SectData data = SectData.getInstance();
        if (data.jade < GACHA_TEN_COST_JADE) {
            return null;
        }
        data.jade -= GACHA_TEN_COST_JADE;

        List<Disciple> results = new ArrayList<Disciple>();
        for (int i = 0; i < 10; i++) {
            gachaPityCounter++;
            int rarity = GameConfig.RARITY_COMMON;

            if (gachaPityCounter >= GACHA_PITY_THRESHOLD) {
                rarity = GameConfig.RARITY_MYTHIC;
                gachaPityCounter = 0;
            } else {
                int roll = RNG.nextInt(1000);
                if (roll < 5) {
                    rarity = GameConfig.RARITY_MYTHIC;
                    gachaPityCounter = 0;
                } else if (roll < 30) {
                    rarity = GameConfig.RARITY_LEGEND;
                } else if (roll < 100) {
                    rarity = GameConfig.RARITY_EPIC;
                } else if (roll < 300) {
                    rarity = GameConfig.RARITY_RARE;
                } else if (roll < 600) {
                    rarity = GameConfig.RARITY_UNCOMMON;
                } else {
                    rarity = GameConfig.RARITY_COMMON;
                }
            }

            Disciple d = TalentSystem.generateRecruitDisciple(rarity);
            if (d != null) {
                data.addDisciple(d);
                results.add(d);
            }
        }
        return results;
    }
}
