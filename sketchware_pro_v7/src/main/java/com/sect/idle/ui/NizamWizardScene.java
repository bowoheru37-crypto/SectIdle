package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.systems.CameraSystem;

/**
 * NizamWizardScene - Full-screen scene adapter integrating NizamWizardEngineView into SceneManager.
 * Zero GC allocation during render loops.
 * 100% Pure Java standard syntax without lambda expressions for Sketchware Pro v7.0.0.
 */
public final class NizamWizardScene {
    private final Context context;
    private final NizamWizardEngineView wizardView;
    private int screenW = 720;
    private int screenH = 1280;

    public NizamWizardScene(Context ctx) {
        this.context = ctx;
        this.wizardView = new NizamWizardEngineView(ctx);
        this.wizardView.layout(0, 0, screenW, screenH);
    }

    public void update(float dt) {
        // Continuous updates if needed
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w > 0 && h > 0 && (w != screenW || h != screenH)) {
            screenW = w;
            screenH = h;
            wizardView.layout(0, 0, w, h);
        }
        wizardView.draw(canvas);
    }

    public void onTouchDown(float x, float y) {
        android.view.MotionEvent event = android.view.MotionEvent.obtain(
            System.currentTimeMillis(), System.currentTimeMillis(),
            android.view.MotionEvent.ACTION_DOWN, x, y, 0
        );
        wizardView.onTouchEvent(event);
        event.recycle();
    }

    public void onTouchUp(float x, float y) {
        android.view.MotionEvent event = android.view.MotionEvent.obtain(
            System.currentTimeMillis(), System.currentTimeMillis(),
            android.view.MotionEvent.ACTION_UP, x, y, 0
        );
        wizardView.onTouchEvent(event);
        event.recycle();
    }

    public NizamWizardEngineView getWizardView() {
        return wizardView;
    }
}
