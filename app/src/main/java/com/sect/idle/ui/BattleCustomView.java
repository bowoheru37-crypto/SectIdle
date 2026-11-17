package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.MotionEvent;
import android.view.View;
import com.sect.idle.gameplay.BattleEngine;

/**
 * BattleCustomView - Lightweight Choreographer View adapter backed by the unified BattleCanvasRenderer.
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleCustomView extends View implements Choreographer.FrameCallback {

    private final BattleCanvasRenderer renderer;
    private boolean isPaused = false;
    private boolean isRunning = false;
    private long lastFrameTimeNanos = 0L;
    private Choreographer choreographer;

    public BattleCustomView(Context context) {
        this(context, null);
    }

    public BattleCustomView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.renderer = new BattleCanvasRenderer(false);
        this.renderer.shakeDecay = 0.88f;
        initDefaultSectBattle();
    }

    public void initDefaultSectBattle() {
        renderer.initDefaultBattle("Senior Brother Han", "Junior Sister Li", "Blood Ghost Cultist", "Iron Corpse Puppet");
    }

    public void setBattleEngine(BattleEngine engine) {
        renderer.setBattleEngine(engine);
    }

    public BattleEngine getBattleEngine() {
        return renderer.getBattleEngine();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startLoop();
    }

    @Override
    protected void onDetachedFromWindow() {
        stopLoop();
        renderer.releaseResources();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startLoop();
        } else {
            stopLoop();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        renderer.onSizeChanged(w, h);
    }

    public void startLoop() {
        if (isRunning) return;
        isRunning = true;
        choreographer = Choreographer.getInstance();
        if (choreographer != null) {
            choreographer.postFrameCallback(this);
        } else {
            postInvalidateOnAnimation();
        }
    }

    public void stopLoop() {
        isRunning = false;
        if (choreographer != null) {
            choreographer.removeFrameCallback(this);
        }
    }

    @Override
    public void doFrame(long frameTimeNanos) {
        if (!isRunning) return;

        float dt = 0.033f;
        if (lastFrameTimeNanos > 0L) {
            long delta = frameTimeNanos - lastFrameTimeNanos;
            dt = delta / 1_000_000_000.0f;
            if (dt > 0.1f) dt = 0.1f;
        }
        lastFrameTimeNanos = frameTimeNanos;

        renderer.update(dt, isPaused);
        invalidate();

        if (choreographer != null && isRunning) {
            choreographer.postFrameCallback(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        renderer.render(canvas, isPaused);
    }

    public void spawnHitVFX(float x, float y, int color) {
        renderer.spawnHitSparks(x, y, color, 6);
        renderer.shakeIntensity = 4f;
    }

    public void spawnDamageNumber(float x, float y, int amount, boolean isCrit) {
        renderer.spawnFloatingDamage(x, y, amount, isCrit);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = renderer.handleTouchEvent(event, new Runnable() {
            @Override
            public void run() {
                isPaused = !isPaused;
            }
        }, null);
        return handled || super.onTouchEvent(event);
    }
}
