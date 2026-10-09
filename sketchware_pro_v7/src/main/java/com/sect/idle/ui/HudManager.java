package com.sect.idle.ui;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.render.Camera3D;
import com.sect.idle.systems.NumberFormatter;

/**
 * HudManager - Modern Minimalist MMORPG HUD & UI Overlay System.
 * Renders sleek top resource bars, circular minimap with weather badge, floating quick action pill bar,
 * and clean sliding inspection drawers.
 * Pure Java standard without lambdas.
 */
public final class HudManager {
    private final Paint bgPaint;
    private final Paint borderPaint;
    private final Paint textPaint;
    private final Paint accentPaint;
    private final Paint minimapDotPaint;
    private final Paint gradientShaderPaint;

    private final RectF r1;
    private final RectF r2;
    private final StringBuilder sb1;
    private final StringBuilder sb2;

    public static final int ACTION_WAR        = 1;
    public static final int ACTION_ARENA      = 2;
    public static final int ACTION_BATTLE     = 3;
    public static final int ACTION_RECRUIT    = 4;
    public static final int ACTION_MARKET     = 5;
    public static final int ACTION_MENU       = 6;

    public HudManager() {
        this.bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.borderPaint.setStyle(Paint.Style.STROKE);
        this.borderPaint.setStrokeWidth(1.5f);

        this.textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        this.accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.minimapDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.gradientShaderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        this.r1 = new RectF();
        this.r2 = new RectF();
        this.sb1 = new StringBuilder(64);
        this.sb2 = new StringBuilder(64);
    }

    public void renderHud(Canvas canvas, SectData data, Camera3D camera, int screenW, int screenH) {
        if (canvas == null || data == null) return;

        renderTopResourceHeader(canvas, data, screenW);
        renderCircularMinimap(canvas, data, camera, screenW);
        renderFloatingBottomActionBar(canvas, screenW, screenH);
    }

    private void renderTopResourceHeader(Canvas canvas, SectData data, int W) {
        float headerH = 64f;

        // Glassmorphism translucent background
        bgPaint.setColor(0xCC0D081A);
        r1.set(12f, 10f, W - 140f, headerH);
        canvas.drawRoundRect(r1, 16f, 16f, bgPaint);

        borderPaint.setColor(0x40FFD700);
        canvas.drawRoundRect(r1, 16f, 16f, borderPaint);

        // Sect Name & Realm Tag
        textPaint.setTextSize(13f);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.LEFT);
        sb1.setLength(0);
        sb1.append(data.sectName != null ? data.sectName : "Immortal Sect");
        sb1.append(" [").append(GameConfig.getRealmName(data.sectRealm)).append("]");
        canvas.drawText(sb1.toString(), 24f, 32f, textPaint);

        // Resource Pills
        textPaint.setTextSize(11f);

        // Spirit Stones
        sb1.setLength(0);
        sb1.append("💎 ").append(NumberFormatter.format(data.spiritStones, sb2));
        textPaint.setColor(0xFFFFD700);
        canvas.drawText(sb1.toString(), 24f, 52f, textPaint);

        // Jade
        sb1.setLength(0);
        sb1.append("🟢 ").append(NumberFormatter.format(data.jade, sb2));
        textPaint.setColor(0xFF00E5FF);
        canvas.drawText(sb1.toString(), 160f, 52f, textPaint);

        // Essence
        sb1.setLength(0);
        sb1.append("⚡ ").append(NumberFormatter.format(data.essence, sb2));
        textPaint.setColor(0xFFEA80FC);
        canvas.drawText(sb1.toString(), 280f, 52f, textPaint);
    }

    private void renderCircularMinimap(Canvas canvas, SectData data, Camera3D camera, int W) {
        float radius = 54f;
        float cx = W - radius - 16f;
        float cy = radius + 10f;

        // Circular background
        bgPaint.setColor(0xDD0A0A15);
        canvas.drawCircle(cx, cy, radius, bgPaint);

        borderPaint.setColor(0xFFFFD700);
        borderPaint.setStrokeWidth(2f);
        canvas.drawCircle(cx, cy, radius, borderPaint);

        // Weather / Season Badge
        bgPaint.setColor(0xEE1E1435);
        r1.set(cx - radius + 4, cy + radius - 18, cx + radius - 4, cy + radius + 2);
        canvas.drawRoundRect(r1, 8f, 8f, bgPaint);

        textPaint.setTextSize(9f);
        textPaint.setColor(0xFF00E5FF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        String season = data.time != null ? data.time.getDisplay() : "Spring";
        canvas.drawText("🌸 " + season, cx, cy + radius - 4, textPaint);

        // Center player/sect dot
        minimapDotPaint.setColor(0xFFFFD700);
        canvas.drawCircle(cx, cy, 3f, minimapDotPaint);
    }

    private void renderFloatingBottomActionBar(Canvas canvas, int W, int H) {
        float barH = 50f;
        float barY = H - barH - 12f;

        bgPaint.setColor(0xEE120A24);
        r1.set(16f, barY, W - 16f, barY + barH);
        canvas.drawRoundRect(r1, 25f, 25f, bgPaint);

        borderPaint.setColor(0x60FFD700);
        canvas.drawRoundRect(r1, 25f, 25f, borderPaint);

        // Render Pill Buttons
        float buttonW = (W - 48f) / 6f;

        renderPillButton(canvas, 20f, barY + 6f, buttonW - 4f, 38f, "⚔️ War", 0xFFFF5252);
        renderPillButton(canvas, 20f + buttonW, barY + 6f, buttonW - 4f, 38f, "🏆 Arena", 0xFFFFD700);
        renderPillButton(canvas, 20f + buttonW * 2, barY + 6f, buttonW - 4f, 38f, "💥 Battle", 0xFFE53935);
        renderPillButton(canvas, 20f + buttonW * 3, barY + 6f, buttonW - 4f, 38f, "👥 Recruit", 0xFF43A047);
        renderPillButton(canvas, 20f + buttonW * 4, barY + 6f, buttonW - 4f, 38f, "🏛️ Market", 0xFF1E88E5);
        renderPillButton(canvas, 20f + buttonW * 5, barY + 6f, buttonW - 4f, 38f, "⚙️ Menu", 0xFF8E24AA);
    }

    private void renderPillButton(Canvas canvas, float x, float y, float w, float h, String label, int accentColor) {
        r2.set(x, y, x + w, y + h);

        accentPaint.setColor(0x33000000);
        canvas.drawRoundRect(r2, 19f, 19f, accentPaint);

        borderPaint.setColor(accentColor);
        borderPaint.setStrokeWidth(1.2f);
        canvas.drawRoundRect(r2, 19f, 19f, borderPaint);

        textPaint.setTextSize(10f);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, x + w * 0.5f, y + h * 0.62f, textPaint);
    }

    public int onTouchHud(float x, float y, int W, int H) {
        float barH = 50f;
        float barY = H - barH - 12f;

        if (y >= barY && y <= barY + barH) {
            float buttonW = (W - 48f) / 6f;
            int index = (int) ((x - 20f) / buttonW);
            switch (index) {
                case 0: return ACTION_WAR;
                case 1: return ACTION_ARENA;
                case 2: return ACTION_BATTLE;
                case 3: return ACTION_RECRUIT;
                case 4: return ACTION_MARKET;
                case 5: return ACTION_MENU;
            }
        }
        return 0;
    }

    public void renderDiscipleDrawer(Canvas canvas, Disciple d, int W, int H) {
        if (canvas == null || d == null) return;

        float panelW = Math.min(320f, W * 0.85f);
        float panelH = 180f;
        float panelX = 16f;
        float panelY = H - 240f;

        bgPaint.setColor(0xF0120A24);
        r1.set(panelX, panelY, panelX + panelW, panelY + panelH);
        canvas.drawRoundRect(r1, 16f, 16f, bgPaint);

        borderPaint.setColor(0xFFFFD700);
        canvas.drawRoundRect(r1, 16f, 16f, borderPaint);

        textPaint.setTextSize(14f);
        textPaint.setColor(0xFFFFD700);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("👤 " + (d.name != null ? d.name : "Disciple"), panelX + 16f, panelY + 28f, textPaint);

        textPaint.setTextSize(11f);
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText("Realm: " + GameConfig.getRealmName(d.realm), panelX + 16f, panelY + 52f, textPaint);
        canvas.drawText("Element: " + GameConfig.getElementName(d.element), panelX + 16f, panelY + 70f, textPaint);
        canvas.drawText("HP: " + d.hp + " / " + d.maxHp, panelX + 16f, panelY + 88f, textPaint);
        canvas.drawText("Current Task: " + GameConfig.getTaskName(d.currentTask), panelX + 16f, panelY + 106f, textPaint);
    }

    public void renderBuildingDrawer(Canvas canvas, Building b, int W, int H) {
        if (canvas == null || b == null) return;

        float panelW = Math.min(320f, W * 0.85f);
        float panelH = 160f;
        float panelX = 16f;
        float panelY = H - 220f;

        bgPaint.setColor(0xF0120A24);
        r1.set(panelX, panelY, panelX + panelW, panelY + panelH);
        canvas.drawRoundRect(r1, 16f, 16f, bgPaint);

        borderPaint.setColor(0xFF00E5FF);
        canvas.drawRoundRect(r1, 16f, 16f, borderPaint);

        textPaint.setTextSize(14f);
        textPaint.setColor(0xFF00E5FF);
        textPaint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText("🏛️ " + (b.name != null ? b.name : "Building"), panelX + 16f, panelY + 28f, textPaint);

        textPaint.setTextSize(11f);
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText("Level: " + b.level, panelX + 16f, panelY + 52f, textPaint);
        canvas.drawText("Status: " + (b.isBuilt ? "Active" : "Unbuilt"), panelX + 16f, panelY + 70f, textPaint);
    }
}
