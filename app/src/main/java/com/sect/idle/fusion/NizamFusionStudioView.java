package com.sect.idle.fusion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import com.sect.idle.systems.NizamAnalyticsBus;

/**
 * NizamFusionStudioView - MuGodot Engine Mobile 1-Screen Multi-panel Studio.
 * 100% Pure Java standard syntax without lambda expressions for Sketchware Pro v7.0.0.
 */
public class NizamFusionStudioView extends View {

    private final Paint bgPaint = new Paint();
    private final Paint panelPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint hitboxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timelinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF topBarRect = new RectF();
    private final RectF hierarchyPanelRect = new RectF();
    private final RectF centerPanelRect = new RectF();
    private final RectF eventPanelRect = new RectF();
    private final RectF timelinePanelRect = new RectF();

    private final RectF playButtonRect = new RectF();
    private final RectF buildButtonRect = new RectF();
    private final RectF hitboxVisualRect = new RectF(400, 250, 480, 330);

    private int currentFrame = 8;
    private final int totalFrames = 20;
    private boolean isPlaying = false;
    private float characterHealth = 100f;
    private float characterSpeed = 5.5f;

    private NizamMoveData currentMove = new NizamMoveData("Punch", 5, 8, 12, 15f);

    public NizamFusionStudioView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bgPaint.setColor(0xFF0F111A);
        panelPaint.setColor(0xFF181C27);
        panelPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(0xFF2A3245);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);

        textPaint.setColor(0xFFECEFF1);
        textPaint.setTextSize(20f);

        hitboxPaint.setColor(0x80FF1744);
        hitboxPaint.setStyle(Paint.Style.FILL);

        timelinePaint.setColor(0xFF2979FF);
        timelinePaint.setStyle(Paint.Style.FILL);

        buttonPaint.setColor(0xFF00E676);
        buttonPaint.setStyle(Paint.Style.FILL);

        NizamAnalyticsBus.sendEvent("fusion_studio_opened", 60, "Device: Itel A70 (MuGodot Engine Mobile)");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float pad = 12f;
        float topH = 55f;
        float bottomH = 160f;

        topBarRect.set(pad, pad, w - pad, pad + topH);

        float midTop = topBarRect.bottom + pad;
        float midBottom = h - bottomH - pad;

        float leftW = w * 0.28f;
        float rightW = w * 0.26f;
        float centerW = w - leftW - rightW - pad * 4;

        hierarchyPanelRect.set(pad, midTop, pad + leftW, midBottom);
        centerPanelRect.set(hierarchyPanelRect.right + pad, midTop, hierarchyPanelRect.right + pad + centerW, midBottom);
        eventPanelRect.set(centerPanelRect.right + pad, midTop, centerPanelRect.right + pad + rightW, midBottom);
        timelinePanelRect.set(pad, midBottom + pad, w - pad, h - pad);

        float btnW = 110f;
        float btnH = 40f;
        playButtonRect.set(topBarRect.right - btnW * 2 - pad * 2, topBarRect.centerY() - btnH / 2f,
                           topBarRect.right - btnW - pad * 2, topBarRect.centerY() + btnH / 2f);
        buildButtonRect.set(topBarRect.right - btnW - pad, topBarRect.centerY() - btnH / 2f,
                            topBarRect.right - pad, topBarRect.centerY() + btnH / 2f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        drawTopBar(canvas);
        drawHierarchy(canvas);
        drawCenterInspectorAndPreview(canvas);
        drawEventSystem(canvas);
        drawTimelineAndMoveEditor(canvas);
    }

    private void drawTopBar(Canvas canvas) {
        canvas.drawRoundRect(topBarRect, 8f, 8f, panelPaint);
        canvas.drawRoundRect(topBarRect, 8f, 8f, borderPaint);

        textPaint.setColor(0xFF00E676);
        textPaint.setTextSize(22f);
        textPaint.setFakeBoldText(true);
        canvas.drawText("⚡ MuGodot Mobile - NIZAM FUSION v3.0", topBarRect.left + 16f, topBarRect.centerY() + 7f, textPaint);
        textPaint.setFakeBoldText(false);

        buttonPaint.setColor(isPlaying ? 0xFFFF9100 : 0xFF00C853);
        canvas.drawRoundRect(playButtonRect, 6f, 6f, buttonPaint);
        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(18f);
        canvas.drawText(isPlaying ? "❚❚ Pause" : "▶ Play", playButtonRect.left + 18f, playButtonRect.centerY() + 6f, textPaint);

        buttonPaint.setColor(0xFF2979FF);
        canvas.drawRoundRect(buildButtonRect, 6f, 6f, buttonPaint);
        textPaint.setColor(0xFFFFFFFF);
        canvas.drawText("🚀 Build", buildButtonRect.left + 24f, buildButtonRect.centerY() + 6f, textPaint);
    }

    private void drawHierarchy(Canvas canvas) {
        drawPanel(canvas, hierarchyPanelRect, "SCENE HIERARCHY (Godot)");

        float x = hierarchyPanelRect.left + 16f;
        float y = hierarchyPanelRect.top + 60f;
        float stepY = 28f;

        textPaint.setColor(0xFFFFD54F);
        canvas.drawText("▼ Root [World]", x, y, textPaint);
        y += stepY;

        textPaint.setColor(0xFF80D8FF);
        canvas.drawText("  ▼ Player (Character)", x, y, textPaint);
        y += stepY;

        textPaint.setColor(0xFFCFD8DC);
        canvas.drawText("    ├─ Sprite (Atlas)", x, y, textPaint);
        y += stepY;
        canvas.drawText("    ├─ AnimationPlayer", x, y, textPaint);
        y += stepY;
        canvas.drawText("    ├─ CollisionShape (Hitbox)", x, y, textPaint);
        y += stepY;
        canvas.drawText("    └─ StateMachine (MUGEN)", x, y, textPaint);
        y += stepY;

        textPaint.setColor(0xFFFF8A80);
        canvas.drawText("  ► Enemy (AI Disciple)", x, y, textPaint);
    }

    private void drawCenterInspectorAndPreview(Canvas canvas) {
        drawPanel(canvas, centerPanelRect, "INSPECTOR & PREVIEW (Unity)");

        float cx = centerPanelRect.centerX();
        float cy = centerPanelRect.top + 130f;

        buttonPaint.setColor(0xFF37474F);
        canvas.drawCircle(cx, cy - 40f, 22f, buttonPaint);
        canvas.drawRoundRect(cx - 26f, cy - 18f, cx + 26f, cy + 70f, 8f, 8f, buttonPaint);

        if (currentMove.isHitboxActive(currentFrame)) {
            hitboxVisualRect.set(cx + 10f, cy - 20f, cx + 90f, cy + 50f);
            canvas.drawRoundRect(hitboxVisualRect, 6f, 6f, hitboxPaint);
            borderPaint.setColor(0xFFFF1744);
            canvas.drawRoundRect(hitboxVisualRect, 6f, 6f, borderPaint);
            borderPaint.setColor(0xFF2A3245);
        }

        float ctrlX = centerPanelRect.left + 16f;
        float ctrlY = centerPanelRect.bottom - 110f;

        textPaint.setColor(0xFFFFD54F);
        textPaint.setTextSize(18f);
        canvas.drawText("Properties (Zero Mono GC):", ctrlX, ctrlY, textPaint);
        ctrlY += 26f;

        textPaint.setColor(0xFFECEFF1);
        canvas.drawText("• Health: " + (int) characterHealth + " [Slider 100-200]", ctrlX, ctrlY, textPaint);
        ctrlY += 24f;
        canvas.drawText("• Speed: " + characterSpeed + " | Faction: Player", ctrlX, ctrlY, textPaint);
        ctrlY += 24f;
        canvas.drawText("[X] PhysicsEnabled   [X] InputController", ctrlX, ctrlY, textPaint);
    }

    private void drawEventSystem(Canvas canvas) {
        drawPanel(canvas, eventPanelRect, "EVENT WIZARD (RPG Maker)");

        float x = eventPanelRect.left + 14f;
        float y = eventPanelRect.top + 60f;
        float stepY = 28f;

        textPaint.setColor(0xFFFFAB40);
        textPaint.setTextSize(17f);
        canvas.drawText("Event: On Player HP < 30%", x, y, textPaint);
        y += stepY;

        textPaint.setColor(0xFF69F0AE);
        canvas.drawText("➔ Play Anim: Desperate", x, y, textPaint);
        y += stepY;
        canvas.drawText("➔ Spawn: Jade Mist", x, y, textPaint);
        y += stepY;
        canvas.drawText("➔ Telemetry: Low HP", x, y, textPaint);
        y += stepY * 1.5f;

        textPaint.setColor(0xFFB0BEC5);
        canvas.drawText("+ Add Action Trigger", x, y, textPaint);
    }

    private void drawTimelineAndMoveEditor(Canvas canvas) {
        drawPanel(canvas, timelinePanelRect, "MOVE EDITOR & TIMELINE (Fighter Maker + MUGEN)");

        float x = timelinePanelRect.left + 16f;
        float y = timelinePanelRect.top + 55f;

        textPaint.setColor(0xFF00E5FF);
        textPaint.setTextSize(18f);
        String info = "Move: " + currentMove.moveName +
                      " | Startup: " + currentMove.startupFrames + "f" +
                      " | Active: " + currentMove.activeFrames + "f" +
                      " | Recovery: " + currentMove.recoveryFrames + "f" +
                      " | Frame: " + currentFrame + "/" + totalFrames;
        canvas.drawText(info, x, y, textPaint);

        float barLeft = x;
        float barTop = y + 20f;
        float barW = timelinePanelRect.width() - 32f;
        float barH = 34f;

        float startupW = barW * (currentMove.startupFrames / (float) totalFrames);
        float activeW = barW * (currentMove.activeFrames / (float) totalFrames);

        timelinePaint.setColor(0xFFFF9100);
        canvas.drawRect(barLeft, barTop, barLeft + startupW, barTop + barH, timelinePaint);

        timelinePaint.setColor(0xFF00E5FF);
        canvas.drawRect(barLeft + startupW, barTop, barLeft + startupW + activeW, barTop + barH, timelinePaint);

        timelinePaint.setColor(0xFFFFD54F);
        canvas.drawRect(barLeft + startupW + activeW, barTop, barLeft + barW, barTop + barH, timelinePaint);

        float cursorX = barLeft + (barW * (currentFrame / (float) totalFrames));
        buttonPaint.setColor(0xFFFFFFFF);
        canvas.drawRect(cursorX - 2f, barTop - 4f, cursorX + 2f, barTop + barH + 4f, buttonPaint);
    }

    private void drawPanel(Canvas canvas, RectF rect, String title) {
        canvas.drawRoundRect(rect, 8f, 8f, panelPaint);
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint);

        textPaint.setColor(0xFF81C784);
        textPaint.setTextSize(18f);
        canvas.drawText(title, rect.left + 14f, rect.top + 30f, textPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            if (playButtonRect.contains(x, y)) {
                isPlaying = !isPlaying;
                NizamAnalyticsBus.sendEvent("fusion_play_toggled", 60, "IsPlaying=" + isPlaying);
                invalidate();
                return true;
            }

            if (buildButtonRect.contains(x, y)) {
                NizamAnalyticsBus.sendEvent("fusion_build_triggered", 60, "Targets: player.apk, player.exe, player.nimugen");
                invalidate();
                return true;
            }

            if (timelinePanelRect.contains(x, y)) {
                currentFrame = (currentFrame + 1) % totalFrames;
                invalidate();
                return true;
            }
        }
        return true;
    }

    public int getCurrentFrame() { return currentFrame; }
    public void setCurrentFrame(int f) { this.currentFrame = f; invalidate(); }
    public boolean isPlaying() { return isPlaying; }
    public void setPlaying(boolean p) { this.isPlaying = p; invalidate(); }
}
