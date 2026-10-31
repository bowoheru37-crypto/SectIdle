package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.sect.idle.core.GameState;
import com.sect.idle.systems.CameraSystem;

/**
 * MarketplaceScene - Categorized Xianxia Item Trading & Auction Scene.
 * Pure Java standard without lambdas for Sketchware Pro v7.0.0 compatibility.
 */
public final class MarketplaceScene {
    private final Context context;
    private float time = 0f;
    private int activeTab = 0; // 0=Pills, 1=Artifacts, 2=Talismans

    private final Paint bgPaint;
    private final Paint cardBgPaint;
    private final Paint cardBorderPaint;
    private final Paint titlePaint;
    private final Paint textPaint;
    private final Paint tabBtnPaint;
    private final Paint activeTabPaint;

    private final RectF mainCardRect;
    private final RectF[] tabRects;
    private final RectF backBtnRect;

    private static final String[] TAB_NAMES = {"💊 Pills", "🗡️ Artifacts", "📜 Talismans"};
    private static final String[][] ITEMS = {
        {"Golden Core Pill - 500 Stones", "Nine Spirit Herb - 200 Stones", "Qi Gathering Elixir - 100 Stones"},
        {"Dragon Flame Sword - 1500 Stones", "Celestial Shield - 1200 Stones", "Sky Ring - 800 Stones"},
        {"Thunderbolt Talisman - 300 Stones", "Invisibility Rune - 250 Stones", "Speed Talisman - 150 Stones"}
    };

    public MarketplaceScene(Context ctx) {
        this.context = ctx;

        bgPaint = new Paint();
        bgPaint.setColor(0xFF0A0A15);

        cardBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBgPaint.setColor(0xEE120A24);

        cardBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cardBorderPaint.setStyle(Paint.Style.STROKE);
        cardBorderPaint.setStrokeWidth(1.5f);
        cardBorderPaint.setColor(0x60FFD700);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFFFFD700);
        titlePaint.setTextSize(24f);
        titlePaint.setTypeface(Typeface.DEFAULT_BOLD);
        titlePaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(13f);

        tabBtnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tabBtnPaint.setColor(0xFF1A1A2E);

        activeTabPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        activeTabPaint.setColor(0xFF2E2E5D);

        mainCardRect = new RectF();
        tabRects = new RectF[3];
        for (int i = 0; i < 3; i++) tabRects[i] = new RectF();
        backBtnRect = new RectF();
    }

    public void update(float dt) {
        time += dt;
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        canvas.drawRect(0, 0, w, h, bgPaint);

        float cx = w * 0.5f;
        float cy = h * 0.5f;
        float cardW = Math.min(w * 0.90f, 400f);
        float cardH = Math.min(h * 0.85f, 540f);

        mainCardRect.set(cx - cardW * 0.5f, cy - cardH * 0.5f, cx + cardW * 0.5f, cy + cardH * 0.5f);
        canvas.drawRoundRect(mainCardRect, 18f, 18f, cardBgPaint);
        canvas.drawRoundRect(mainCardRect, 18f, 18f, cardBorderPaint);

        canvas.drawText("🏛️ HEAVENLY MARKETPLACE", cx, mainCardRect.top + 38f, titlePaint);

        // Render Tabs
        float tabW = (cardW - 40f) / 3f;
        for (int i = 0; i < 3; i++) {
            float tx = mainCardRect.left + 20f + i * tabW;
            tabRects[i].set(tx, mainCardRect.top + 55f, tx + tabW - 4f, mainCardRect.top + 90f);

            canvas.drawRoundRect(tabRects[i], 8f, 8f, activeTab == i ? activeTabPaint : tabBtnPaint);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(TAB_NAMES[i], tabRects[i].centerX(), tabRects[i].centerY() + 4f, textPaint);
        }

        // Render Item Listings
        textPaint.setTextAlign(Paint.Align.LEFT);
        float itemY = mainCardRect.top + 130f;
        String[] currentItems = ITEMS[activeTab];
        for (int i = 0; i < currentItems.length; i++) {
            canvas.drawText("• " + currentItems[i], mainCardRect.left + 24f, itemY, textPaint);
            itemY += 40f;
        }

        // Back Button
        backBtnRect.set(mainCardRect.left + 20f, mainCardRect.bottom - 54f, mainCardRect.right - 20f, mainCardRect.bottom - 16f);
        canvas.drawRoundRect(backBtnRect, 12f, 12f, tabBtnPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(0xFFFFD700);
        canvas.drawText("🏯 Back to Sect Realm", backBtnRect.centerX(), backBtnRect.centerY() + 4f, textPaint);
        textPaint.setColor(0xFFFFFFFF);
    }

    public void onTouchDown(float x, float y) {
        for (int i = 0; i < 3; i++) {
            if (tabRects[i].contains(x, y)) activeTab = i;
        }

        if (backBtnRect.contains(x, y) && context instanceof GameActivity) {
            ((GameActivity) context).sceneManager.setState(GameState.SECT);
        }
    }
}
