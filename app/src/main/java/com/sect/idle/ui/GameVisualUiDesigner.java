package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Game Visual UI Designer - Optimized for Itel A70 (1GB RAM) & Sketchware Pro v7.0.0.
 * Pure Java (0 lambdas/streams) visual layout editor for Mobile & Console HUDs.
 */
public class GameVisualUiDesigner extends View {

    public static class UiComponent {
        public String id;
        public String type; // "BUTTON", "HEALTH_BAR", "DIALOG", "MINIMAP", "PANEL"
        public float x, y, width, height;
        public int color;
        public boolean isSelected;

        public UiComponent(String id, String type, float x, float y, float width, float height, int color) {
            this.id = id;
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.color = color;
            this.isSelected = false;
        }

        public boolean contains(float touchX, float touchY) {
            return touchX >= x && touchX <= (x + width) && touchY >= y && touchY <= (y + height);
        }
    }

    private List<UiComponent> components;
    private UiComponent selectedComponent;
    private Paint paint;
    private Paint textPaint;
    private float lastTouchX;
    private float lastTouchY;
    private boolean isDragging;
    private int gridSize;

    // Pre-allocated RectF to eliminate GC object churn in component rendering
    private final RectF tempCompRect = new RectF();

    public GameVisualUiDesigner(Context context) {
        super(context);
        init();
    }

    public GameVisualUiDesigner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        components = new ArrayList<UiComponent>();
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(24f);
        gridSize = 20;

        // Default HUD components setup
        components.add(new UiComponent("hp_bar", "HEALTH_BAR", 40, 40, 240, 36, Color.RED));
        components.add(new UiComponent("btn_attack", "BUTTON", 40, 500, 100, 100, Color.BLUE));
        components.add(new UiComponent("btn_skill", "BUTTON", 160, 500, 100, 100, Color.GREEN));
        components.add(new UiComponent("dialog_frame", "DIALOG", 40, 340, 400, 120, Color.DKGRAY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background grid
        paint.setColor(Color.argb(40, 200, 200, 200));
        paint.setStrokeWidth(1f);
        for (int x = 0; x < getWidth(); x += gridSize) {
            canvas.drawLine(x, 0, x, getHeight(), paint);
        }
        for (int y = 0; y < getHeight(); y += gridSize) {
            canvas.drawLine(0, y, getWidth(), y, paint);
        }

        // Render UI Components
        for (int i = 0; i < components.size(); i++) {
            UiComponent comp = components.get(i);

            // Reuse pre-allocated RectF object
            tempCompRect.set(comp.x, comp.y, comp.x + comp.width, comp.y + comp.height);

            // Draw Component fill
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(comp.color);
            canvas.drawRoundRect(tempCompRect, 8f, 8f, paint);

            // Draw border selection
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(comp.isSelected ? 4f : 2f);
            paint.setColor(comp.isSelected ? Color.YELLOW : Color.WHITE);
            canvas.drawRoundRect(tempCompRect, 8f, 8f, paint);

            // Draw text label
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText(comp.id, comp.x + 8, comp.y + 24, textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                selectedComponent = null;
                for (int i = components.size() - 1; i >= 0; i--) {
                    UiComponent comp = components.get(i);
                    comp.isSelected = false;
                    if (selectedComponent == null && comp.contains(x, y)) {
                        comp.isSelected = true;
                        selectedComponent = comp;
                        isDragging = true;
                        lastTouchX = x;
                        lastTouchY = y;
                    }
                }
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if (isDragging && selectedComponent != null) {
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    selectedComponent.x += dx;
                    selectedComponent.y += dy;

                    // Snap to grid
                    selectedComponent.x = Math.round(selectedComponent.x / gridSize) * gridSize;
                    selectedComponent.y = Math.round(selectedComponent.y / gridSize) * gridSize;

                    lastTouchX = x;
                    lastTouchY = y;
                    invalidate();
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isDragging = false;
                break;
        }
        return true;
    }

    public List<UiComponent> getComponents() {
        return components;
    }
}
