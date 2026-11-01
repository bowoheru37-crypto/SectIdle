package com.sect.idle.fusion;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import com.sect.idle.systems.CameraSystem;

public final class NizamFusionScene {
    private final Context context;
    private final NizamFusionStudioView studioView;
    private int screenW = 720;
    private int screenH = 1280;

    public NizamFusionScene(Context ctx) {
        this.context = ctx;
        this.studioView = new NizamFusionStudioView(ctx);
        this.studioView.layout(0, 0, screenW, screenH);
    }

    public void update(float dt) {
        if (studioView.isPlaying()) {
            studioView.setCurrentFrame((studioView.getCurrentFrame() + 1) % 20);
        }
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null) return;
        int w = canvas.getWidth();
        int h = canvas.getHeight();
        if (w > 0 && h > 0 && (w != screenW || h != screenH)) {
            screenW = w;
            screenH = h;
            studioView.layout(0, 0, w, h);
        }
        studioView.draw(canvas);
    }

    public void onTouchDown(float x, float y) {
        android.view.MotionEvent event = android.view.MotionEvent.obtain(
            System.currentTimeMillis(), System.currentTimeMillis(),
            android.view.MotionEvent.ACTION_DOWN, x, y, 0
        );
        studioView.onTouchEvent(event);
        event.recycle();
    }

    public void onTouchUp(float x, float y) {
        android.view.MotionEvent event = android.view.MotionEvent.obtain(
            System.currentTimeMillis(), System.currentTimeMillis(),
            android.view.MotionEvent.ACTION_UP, x, y, 0
        );
        studioView.onTouchEvent(event);
        event.recycle();
    }

    public NizamFusionStudioView getStudioView() {
        return studioView;
    }
}
