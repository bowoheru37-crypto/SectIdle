package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.sect.idle.systems.CameraSystem;

/**
 * Fake3D v1.0 - Pseudo-3D rendering techniques for 2D Canvas.
 */
public final class Fake3D {
    private final Paint shadowPaint;
    private final Paint heightPaint;
    private final RectF tempRect;

    public Fake3D() {
        this.shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shadowPaint.setColor(0x40000000);
        this.heightPaint = new Paint();
        this.tempRect = new RectF();
    }

    public void renderShadow(Canvas canvas, CameraSystem cam, float wx, float wy, float wWidth, float wHeight, float height) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sw = wWidth * cam.zoom;
        float sh = wHeight * cam.zoom * 0.3f;
        float shOffset = height * cam.zoom * 0.5f;
        tempRect.set(sx - sw * 0.5f, sy - sh * 0.5f + shOffset, sx + sw * 0.5f, sy + sh * 0.5f + shOffset);
        shadowPaint.setAlpha((int)(120 * Math.max(0f, 1f - height / 100f)));
        canvas.drawOval(tempRect, shadowPaint);
    }

    public void renderExtrudedRect(Canvas canvas, CameraSystem cam, float wx, float wy, float wWidth, float wHeight, float wDepth, int topColor, int sideColor) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sw = wWidth * cam.zoom;
        float sh = wHeight * cam.zoom;
        float sd = wDepth * cam.zoom;

        if (sx + sw < -sd || sx - sw > canvas.getWidth() + sd || sy + sh < -sd || sy - sh > canvas.getHeight() + sd) return;

        heightPaint.setColor(sideColor);
        tempRect.set(sx - sw * 0.5f - sd, sy - sh * 0.5f, sx - sw * 0.5f, sy + sh * 0.5f);
        canvas.drawRect(tempRect, heightPaint);
        tempRect.set(sx + sw * 0.5f, sy - sh * 0.5f, sx + sw * 0.5f + sd, sy + sh * 0.5f);
        canvas.drawRect(tempRect, heightPaint);
        tempRect.set(sx - sw * 0.5f, sy - sh * 0.5f - sd, sx + sw * 0.5f, sy + sh * 0.5f);
        heightPaint.setColor(topColor);
        canvas.drawRect(tempRect, heightPaint);
    }

    public void renderIsoBlock(Canvas canvas, CameraSystem cam, float wx, float wy, float size, float height, int topColor, int leftColor, int rightColor) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float s = size * cam.zoom;
        float h = height * cam.zoom;

        float topX = sx;
        float topY = sy - h;
        float hw = s * 0.5f;
        float hh = s * 0.25f;

        tempRect.set(topX - hw, topY - hh, topX + hw, topY + hh);
        heightPaint.setColor(topColor);
        canvas.drawOval(tempRect, heightPaint);

        heightPaint.setColor(leftColor);
        canvas.drawRect(topX - hw, topY, topX, topY + h, heightPaint);

        heightPaint.setColor(rightColor);
        canvas.drawRect(topX, topY, topX + hw, topY + h, heightPaint);
    }

    public void renderReflection(Canvas canvas, CameraSystem cam, float wx, float wy, float wWidth, float wHeight, int color, float alpha) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sw = wWidth * cam.zoom;
        float sh = wHeight * cam.zoom;
        tempRect.set(sx - sw * 0.5f, sy, sx + sw * 0.5f, sy + sh);
        heightPaint.setColor(color);
        heightPaint.setAlpha((int)(alpha * 255));
        canvas.drawRect(tempRect, heightPaint);
        heightPaint.setAlpha(255);
    }

    public void renderSpecular(Canvas canvas, CameraSystem cam, float wx, float wy, float radius, int color, float intensity) {
        if (canvas == null || cam == null) return;
        float sx = cam.worldToScreenX(wx);
        float sy = cam.worldToScreenY(wy);
        float sr = radius * cam.zoom;
        shadowPaint.setColor(color);
        shadowPaint.setAlpha((int)(intensity * 200));
        tempRect.set(sx - sr, sy - sr * 0.3f, sx + sr, sy + sr * 0.3f);
        canvas.drawOval(tempRect, shadowPaint);
        shadowPaint.setAlpha(255);
    }

    public void renderParallaxBackground(Canvas canvas, CameraSystem cam) {
        if (canvas == null || cam == null) return;
        int width = canvas.getWidth();
        int height = canvas.getHeight();

        float camX = cam.pos.x * 0.1f;
        float camY = cam.pos.y * 0.1f;

        // Multi-Layer Mountain Parallax (Aesthetic Xianxia Atmosphere)
        heightPaint.setColor(0xFF1E2638); // Distant spiritual mountain range
        tempRect.set(0, height * 0.3f - (camY * 0.2f), width, height);
        canvas.drawRect(tempRect, heightPaint);

        heightPaint.setColor(0xFF0F172A); // Midground mist
        tempRect.set(0, height * 0.5f - (camY * 0.5f), width, height);
        canvas.drawRect(tempRect, heightPaint);
    }
}
