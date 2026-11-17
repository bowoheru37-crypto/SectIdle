package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import com.sect.idle.core.GameLoop;
import com.sect.idle.gameplay.BattleEngine;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.utils.ExceptionManager;

/**
 * BattleView - Double-Buffered SurfaceView adapter backed by the unified BattleCanvasRenderer.
 * 100% Pure Java 7 & Sketchware Pro v7.0.0 Compatible (Zero Lambdas, Zero Streams).
 */
public final class BattleView extends SurfaceView implements GameLoop.GameLoopCallback {

    private final GameLoop gameLoop;
    private final BattleCanvasRenderer renderer;
    private final BattleCanvasRenderer.FeedbackListener feedbackListener;

    public BattleView(Context context) {
        this(context, null);
    }

    public BattleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.renderer = new BattleCanvasRenderer(true);
        this.gameLoop = new GameLoop(getHolder(), this);
        this.feedbackListener = new BattleCanvasRenderer.FeedbackListener() {
            @Override
            public void onButtonClick() {
                try { AudioManager.get(getContext()).playSfx(AudioManager.SFX_CLICK); } catch (Throwable ignored) {}
            }

            @Override
            public void onUnitSelected() {
                try { AudioManager.get(getContext()).playSfx(AudioManager.SFX_CLICK); } catch (Throwable ignored) {}
            }

            @Override
            public void onSlashPoint() {
                try { AudioManager.get(getContext()).playSfx(AudioManager.SFX_SWORD_SPAR); } catch (Throwable ignored) {}
            }

            @Override
            public void onSlashCrit() {
                try { AudioManager.get(getContext()).playSfx(AudioManager.SFX_CRITICAL_STRIKE); } catch (Throwable ignored) {}
            }
        };
        initDefaultBattle();
    }

    public GameLoop getGameLoop() {
        return gameLoop;
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
    public void onSurfaceCreated() {
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        renderer.onSizeChanged(width, height);
    }

    @Override
    public void onSurfaceDestroyed() {
        renderer.releaseResources();
    }

    @Override
    public void onGameUpdate(float dt) {
        boolean paused = (gameLoop != null && gameLoop.isPaused());
        renderer.update(dt, paused);
    }

    @Override
    public void onGameRender(Canvas canvas) {
        renderFrameDirect(canvas);
    }

    public void startRenderThread() {
        if (gameLoop != null) {
            gameLoop.start();
        }
    }

    public void stopRenderThread() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void pauseRender() {
        if (gameLoop != null) {
            gameLoop.pause();
        }
    }

    public void resumeRender() {
        if (gameLoop != null) {
            gameLoop.resume();
        }
    }

    public void renderFrameDirect(Canvas canvas) {
        if (canvas == null) return;
        try {
            boolean paused = (gameLoop != null && gameLoop.isPaused());
            renderer.render(canvas, paused);
        } catch (Throwable t) {
            ExceptionManager.get().reportException(t, "BattleView", "Error in renderFrameDirect", ExceptionManager.LEVEL_ERROR);
        }
    }

    public void addSlashPoint(float x, float y) {
        renderer.addSlashPoint(x, y, feedbackListener);
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
                if (gameLoop != null && gameLoop.isPaused()) {
                    resumeRender();
                } else {
                    pauseRender();
                }
            }
        }, feedbackListener);
        return handled || super.onTouchEvent(event);
    }
}
