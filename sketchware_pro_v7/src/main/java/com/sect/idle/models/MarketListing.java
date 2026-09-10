package com.sect.idle.models;

import androidx.annotation.NonNull;

public class MarketListing {
    public final String id;
    public final String itemName;
    public final int itemType;
    public final int rarity;
    public long price;
    public final long originalPrice;
    public int stock;
    public final int maxStock;
    public float demand; // 0.5=low, 1.0=normal, 2.0=high
    public int refreshTimer;
    public boolean isPlayerListing;
    public String sellerName;

    private static final float PRICE_FLOOR = 0.5f;
    private static final float PRICE_CEIL = 2.5f;

    public MarketListing(@NonNull String id, @NonNull String name, int type, int rarity, long price, int stock) {
        this.id = id; this.itemName = name; this.itemType = type;
        this.rarity = rarity; this.price = price; this.originalPrice = price;
        this.stock = stock; this.maxStock = Math.max(1, stock); this.demand = 1.0f;
        this.refreshTimer = 24; this.isPlayerListing = false; this.sellerName = "Market";
    }

    public void updatePrice() {
        float ratio = (float)stock / maxStock;
        float scarcityMod = 2.0f - ratio;
        float newPrice = originalPrice * demand * scarcityMod;
        price = (long)Math.max(originalPrice * PRICE_FLOOR, Math.min(newPrice, originalPrice * PRICE_CEIL));
    }

    public void tick() {
        refreshTimer--;
        if (refreshTimer <= 0) {
            refreshTimer = 24;
            stock = Math.min(maxStock, stock + Math.max(1, maxStock / 4));
            demand = 0.5f + (float)Math.random() * 1.5f;
            updatePrice();
        }
    }
}
