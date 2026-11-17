package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.utils.ExceptionManager;

/**
 * BattleSurfaceView - Double-Buffered SurfaceView adapter backed by the unified BattleCanvasRenderer.
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private SurfaceHolder surfaceHolder;
    private Thread renderThread;
    private volatile boolean isRunning = false;
    private volatile boolean isPaused = false;
    private final Object pauseLock = new Object();
    private final BattleCanvasRenderer renderer;

    public BattleSurfaceView(Context context) {
        this(context, null);
    }

    public BattleSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.renderer = new BattleCanvasRenderer(true);
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        initDefaultBattle();
    }

    public void initDefaultBattle() {
        renderer.initDefaultBattle("Immortal Han", "Fairy Nangong", "Nether Fiend General", "Nine Abyss Behemoth");
    }

    public void setBattleEngine(BattleEngine engine) {
        renderer.setBattleEngine(engine);
    }

    public BattleEngine getBattleEngine() {
        return renderer.getBattleEngine();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        this.surfaceHolder = holder;
        startRenderThread();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        renderer.onSizeChanged(width, height);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopRenderThread();
        renderer.releaseResources();
    }

    public synchronized void startRenderThread() {
        if (renderThread != null && renderThread.isAlive()) return;
        isRunning = true;
        isPaused = false;
        renderThread = new Thread(this, "BattleSurfaceView-RenderLoop");
        renderThread.setPriority(Thread.MAX_PRIORITY);
        renderThread.start();
    }

    public synchronized void stopRenderThread() {
        isRunning = false;
        if (renderThread != null) {
            try {
                renderThread.join(400);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            renderThread = null;
        }
    }

    public void pauseRender() {
        isPaused = true;
    }

    public void resumeRender() {
        synchronized (pauseLock) {
            isPaused = false;
            pauseLock.notifyAll();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        final long targetFrameNanos = 16_666_666L;

        while (isRunning) {
            if (isPaused) {
                synchronized (pauseLock) {
                    while (isPaused && isRunning) {
                        try {
                            pauseLock.wait(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                }
            }

            long now = System.nanoTime();
            float dt = (now - lastTime) / 1_000_000_000.0f;
            if (dt > 0.1f) dt = 0.1f;
            lastTime = now;

            renderer.update(dt, isPaused);
            renderFrame();

            long elapsed = System.nanoTime() - now;
            long sleepNanos = targetFrameNanos - elapsed;
            if (sleepNanos > 1_000_000L) {
                try {
                    Thread.sleep(sleepNanos / 1_000_000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void renderFrame() {
        if (surfaceHolder == null || !surfaceHolder.getSurface().isValid()) return;

        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            if (canvas == null) return;
            renderer.render(canvas, isPaused);
        } catch (Throwable t) {
            ExceptionManager.get().reportException(t, "BattleSurfaceView", "Error in renderFrame", ExceptionManager.LEVEL_ERROR);
        } finally {
            if (canvas != null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Throwable ignored) {}
            }
        }
    }

    public void addSlashPoint(float x, float y) {
        renderer.addSlashPoint(x, y, null);
    }

    public void spawnHitSparks(float x, float y, int color) {
        renderer.spawnHitSparks(x, y, color, 4);
    }

    public void spawnFloatingDamage(float x, float y, int amount, boolean isCrit) {
        renderer.spawnFloatingDamage(x, y, amount, isCrit);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean handled = renderer.handleTouchEvent(event, new Runnable() {
            @Override
            public void run() {
                if (isPaused) {
                    resumeRender();
                } else {
                    pauseRender();
                }
            }
        }, null);
        return handled || super.onTouchEvent(event);
    }
}
