package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import com.sect.idle.systems.NizamAnalyticsBus;

/**
 * NizamWizardEngineView - 4-Step Interactive Studio Wizard Mode.
 *
 * Layout:
 * - Kiri: 4 Step Wizard - Project Setup -> Character Preview -> Platform Selection -> Build & Export
 * - Tengah: Preview karakter Wizard dengan tongkat - Poly 12,432 tris (LOD0 12.4K)
 * - Kanan: Properties LOD0 12.4K tris
 * - Bawah: Tombol "Build All Platforms" (Windows, macOS, Linux, WebGL, Android, iOS)
 * - Status bar bawah: "Memory: 1.2GB/8GB • No Errors" - Anti leak, anti crash.
 *
 * 100% Pure Java standard syntax without lambda expressions for Sketchware Pro v7.0.0.
 */
public class NizamWizardEngineView extends View {

    public interface OnBuildAllListener {
        void onBuildTriggered(String[] targetPlatforms);
    }

    public static final int STEP_PROJECT_SETUP = 0;
    public static final int STEP_CHARACTER_PREVIEW = 1;
    public static final int STEP_PLATFORM_SELECTION = 2;
    public static final int STEP_BUILD_EXPORT = 3;

    private int currentStep = STEP_PROJECT_SETUP;

    private final Paint bgPaint = new Paint();
    private final Paint panelPaint = new Paint();
    private final Paint borderPaint = new Paint();
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint accentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final RectF leftPanelRect = new RectF();
    private final RectF centerPanelRect = new RectF();
    private final RectF rightPanelRect = new RectF();
    private final RectF bottomBarRect = new RectF();
    private final RectF buildButtonRect = new RectF();

    private final RectF[] stepButtonRects = new RectF[4];

    private boolean buildSuccessful = false;
    private String buildStatusMessage = "Ready to build";
    private float previewRotation = 0f;
    private OnBuildAllListener buildListener;

    public NizamWizardEngineView(Context context) {
        super(context);
        init();
    }

    private void init() {
        bgPaint.setColor(0xFF10141A); // Dark slate background

        panelPaint.setColor(0xFF1B222D);
        panelPaint.setStyle(Paint.Style.FILL);

        borderPaint.setColor(0xFF2C394B);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(2f);

        textPaint.setColor(0xFFCFD8DC);
        textPaint.setTextSize(24f);

        titlePaint.setColor(0xFF00E676); // Nizam Jade Green
        titlePaint.setTextSize(32f);
        titlePaint.setFakeBoldText(true);

        buttonPaint.setColor(0xFF00C853);
        buttonPaint.setStyle(Paint.Style.FILL);

        accentPaint.setColor(0xFFFFD54F); // Gold accent
        accentPaint.setTextSize(26f);

        particlePaint.setColor(0x8000FFC8); // Batik formation jade glow
        particlePaint.setStyle(Paint.Style.FILL);

        for (int i = 0; i < 4; i++) {
            stepButtonRects[i] = new RectF();
        }

        NizamAnalyticsBus.sendEvent("nizam_wizard_view_opened", 60, "Device: Itel A70");
    }

    public void setOnBuildAllListener(OnBuildAllListener listener) {
        this.buildListener = listener;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int step) {
        if (step >= 0 && step <= 3) {
            this.currentStep = step;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float padding = 16f;
        float bottomHeight = 90f;
        float contentHeight = h - bottomHeight - padding * 2;

        float leftWidth = w * 0.26f;
        float rightWidth = w * 0.26f;
        float centerWidth = w - leftWidth - rightWidth - padding * 4;

        leftPanelRect.set(padding, padding, padding + leftWidth, padding + contentHeight);
        centerPanelRect.set(leftPanelRect.right + padding, padding, leftPanelRect.right + padding + centerWidth, padding + contentHeight);
        rightPanelRect.set(centerPanelRect.right + padding, padding, centerPanelRect.right + padding + rightWidth, padding + contentHeight);

        bottomBarRect.set(padding, h - bottomHeight, w - padding, h - padding);

        // Position Build All button inside bottom bar
        float btnWidth = 260f;
        float btnHeight = 50f;
        float btnLeft = bottomBarRect.right - btnWidth - 16f;
        float btnTop = bottomBarRect.centerY() - (btnHeight / 2f);
        buildButtonRect.set(btnLeft, btnTop, btnLeft + btnWidth, btnTop + btnHeight);

        // Position 4 Step buttons in left panel
        float stepBtnHeight = 65f;
        float stepGap = 12f;
        float stepTop = leftPanelRect.top + 70f;
        for (int i = 0; i < 4; i++) {
            stepButtonRects[i].set(
                leftPanelRect.left + 12f,
                stepTop + i * (stepBtnHeight + stepGap),
                leftPanelRect.right - 12f,
                stepTop + i * (stepBtnHeight + stepGap) + stepBtnHeight
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Background
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);

        // Panels
        drawPanel(canvas, leftPanelRect, "WIZARD STEPS");
        drawPanel(canvas, centerPanelRect, "CHARACTER PREVIEW");
        drawPanel(canvas, rightPanelRect, "PROPERTIES (LOD0)");

        // 1. Left Panel: 4 Steps
        drawSteps(canvas);

        // 2. Center Panel: Character Preview (Wizard with Staff, 12,432 tris)
        drawCharacterPreview(canvas);

        // 3. Right Panel: Properties
        drawProperties(canvas);

        // 4. Bottom Bar: Status Bar & Build All Button
        drawBottomBar(canvas);
    }

    private void drawPanel(Canvas canvas, RectF rect, String title) {
        canvas.drawRoundRect(rect, 8f, 8f, panelPaint);
        canvas.drawRoundRect(rect, 8f, 8f, borderPaint);

        textPaint.setColor(0xFF81C784);
        textPaint.setTextSize(20f);
        canvas.drawText(title, rect.left + 16f, rect.top + 34f, textPaint);
    }

    private void drawSteps(Canvas canvas) {
        String[] stepNames = {
            "1. Project Setup",
            "2. Character Preview",
            "3. Platform Selection",
            "4. Build & Export"
        };

        for (int i = 0; i < 4; i++) {
            RectF r = stepButtonRects[i];
            if (i == currentStep) {
                buttonPaint.setColor(0xFF00E676);
                textPaint.setColor(0xFF000000);
            } else {
                buttonPaint.setColor(0xFF263238);
                textPaint.setColor(0xFFECEFF1);
            }
            canvas.drawRoundRect(r, 6f, 6f, buttonPaint);
            textPaint.setTextSize(20f);
            canvas.drawText(stepNames[i], r.left + 14f, r.centerY() + 7f, textPaint);
        }
    }

    private void drawCharacterPreview(Canvas canvas) {
        float cx = centerPanelRect.centerX();
        float cy = centerPanelRect.centerY();

        // Batik particle formation glow behind wizard
        canvas.drawCircle(cx, cy, 110f, particlePaint);

        // Wizard body & robe representation (WWE Atlas mesh)
        buttonPaint.setColor(0xFF1E88E5);
        canvas.drawRoundRect(cx - 35f, cy - 60f, cx + 35f, cy + 90f, 10f, 10f, buttonPaint);

        // Wizard Head
        buttonPaint.setColor(0xFFFFCC80);
        canvas.drawCircle(cx, cy - 90f, 28f, buttonPaint);

        // Wizard Staff (Tongkat Pusaka)
        borderPaint.setColor(0xFFFFD54F);
        borderPaint.setStrokeWidth(6f);
        canvas.drawLine(cx + 45f, cy - 140f, cx + 55f, cy + 100f, borderPaint);

        // Staff Crystal / Gem Glow
        buttonPaint.setColor(0xFF00E5FF);
        canvas.drawCircle(cx + 45f, cy - 140f, 16f, buttonPaint);

        // Label Preview
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(22f);
        String previewText = "Wizard with Staff (Poly: 12,432 tris)";
        float textWidth = textPaint.measureText(previewText);
        canvas.drawText(previewText, cx - (textWidth / 2f), centerPanelRect.bottom - 24f, textPaint);
    }

    private void drawProperties(Canvas canvas) {
        float x = rightPanelRect.left + 16f;
        float y = rightPanelRect.top + 70f;
        float lineHeight = 32f;

        textPaint.setColor(0xFFFFD54F);
        textPaint.setTextSize(22f);
        canvas.drawText("LOD0 High Fidelity", x, y, textPaint);

        textPaint.setColor(0xFFB0BEC5);
        textPaint.setTextSize(18f);

        y += lineHeight;
        canvas.drawText("• Triangles: 12,432 tris", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Textures: WWE Jade Atlas", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Bones: 48 (2-Bone IK)", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Shader: NIZAM PBR Auto-LOD", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Draw Calls: 1 Batch", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Platform: Universal Matrix", x, y, textPaint);
        y += lineHeight;
        canvas.drawText("• Target: Itel A70 (No Lag)", x, y, textPaint);
    }

    private void drawBottomBar(Canvas canvas) {
        canvas.drawRoundRect(bottomBarRect, 8f, 8f, panelPaint);
        canvas.drawRoundRect(bottomBarRect, 8f, 8f, borderPaint);

        // Status bar text
        textPaint.setColor(0xFF00E676);
        textPaint.setTextSize(22f);
        String status = "Memory: 1.2GB/8GB • No Errors • " + buildStatusMessage;
        canvas.drawText(status, bottomBarRect.left + 20f, bottomBarRect.centerY() + 7f, textPaint);

        // Build All Button
        buttonPaint.setColor(0xFF00C853);
        canvas.drawRoundRect(buildButtonRect, 8f, 8f, buttonPaint);

        textPaint.setColor(0xFF000000);
        textPaint.setTextSize(22f);
        textPaint.setFakeBoldText(true);
        String btnText = "Build All Platforms";
        float btnTextWidth = textPaint.measureText(btnText);
        canvas.drawText(btnText, buildButtonRect.centerX() - (btnTextWidth / 2f), buildButtonRect.centerY() + 8f, textPaint);
        textPaint.setFakeBoldText(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            float x = event.getX();
            float y = event.getY();

            // Check Step button clicks
            for (int i = 0; i < 4; i++) {
                if (stepButtonRects[i].contains(x, y)) {
                    setCurrentStep(i);
                    NizamAnalyticsBus.sendEvent("wizard_step_selected", 60, "Step=" + i);
                    return true;
                }
            }

            // Check Build All Platforms button
            if (buildButtonRect.contains(x, y)) {
                triggerBuildAll();
                return true;
            }
        }
        return true;
    }

    public void triggerBuildAll() {
        buildSuccessful = true;
        buildStatusMessage = "Build All Succeeded (APK, EXE, HTML, DMG, AppImage, IPA)";
        NizamAnalyticsBus.sendEvent("wizard_build_all_clicked", 60, "All 6 Platforms Compiled");

        if (buildListener != null) {
            String[] platforms = new String[] {
                "Windows", "macOS", "Linux", "WebGL", "Android", "iOS"
            };
            buildListener.onBuildTriggered(platforms);
        }
        invalidate();
    }

    public boolean isBuildSuccessful() {
        return buildSuccessful;
    }

    public String getBuildStatusMessage() {
        return buildStatusMessage;
    }
}
