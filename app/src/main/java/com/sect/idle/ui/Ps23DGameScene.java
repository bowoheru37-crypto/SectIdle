package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;

/**
 * PS2 3D Game Scene Controller & Manager
 * Pure Java (0 lambdas/streams) for Sketchware Pro v7.0.0 & Itel A70 1GB RAM.
 */
public class Ps23DGameScene {

    private Context context;
    private Ps23DGameView gameView;
    private GameVisualUiDesigner uiDesigner;
    private boolean isOverlayDesignerActive;

    public Ps23DGameScene(Context context) {
        this.context = context;
        this.gameView = new Ps23DGameView(context);
        this.uiDesigner = new GameVisualUiDesigner(context);
        this.isOverlayDesignerActive = false;
    }

    public View getGameView() {
        return gameView;
    }

    public GameVisualUiDesigner getUiDesigner() {
        return uiDesigner;
    }

    public void setOverlayDesignerActive(boolean active) {
        this.isOverlayDesignerActive = active;
        this.uiDesigner.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    public boolean isOverlayDesignerActive() {
        return isOverlayDesignerActive;
    }
}
