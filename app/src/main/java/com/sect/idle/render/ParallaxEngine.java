package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import com.sect.idle.systems.CameraSystem;

/**
 * ParallaxEngine - Multi-Layer 3D Parallax Xianxia Background Renderer.
 * Synthesizes distant celestial mountains, floating spirit isles, mist clouds, and aura dust.
 * Pure Java standard implementation without lambdas.
 */
public final class ParallaxEngine {
    private final Paint skyPaint;
    private final Paint mountainPaint;
    private final Paint islandPaint;
    private final Paint mistPaint;
    private final Paint starPaint;

    private final Path path;
    private final RectF rect;
    private float animTimer = 0f;

    public ParallaxEngine() {
        this.skyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mountainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.islandPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.mistPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        this.path = new Path();
        this.rect = new RectF();
    }

    public void update(float dt) {
        animTimer += dt;
    }

    public void render(Canvas canvas, CameraSystem camera, int screenW, int screenH) {
        if (canvas == null) return;

        float camX = (camera != null) ? camera.pos.x : 0f;
        float camY = (camera != null) ? camera.pos.y : 0f;

        // Layer 0: Sky Gradient & Dao Starfield
        skyPaint.setShader(new LinearGradient(0, 0, 0, screenH, 0xFF0F0A1A, 0xFF231038, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, screenW, screenH, skyPaint);

        starPaint.setColor(0xCCFFFFFF);
        for (int i = 0; i < 30; i++) {
            float sx = (i * 97 + camX * 0.02f) % screenW;
            float sy = (i * 53 + camY * 0.02f) % (screenH * 0.6f);
            float alpha = (float) Math.abs(Math.sin(animTimer * 2f + i)) * 200f + 55f;
            starPaint.setAlpha((int) alpha);
            canvas.drawCircle(sx, sy, 1.5f, starPaint);
        }

        // Layer 1: Distant Xianxia Mountain Range (Parallax 0.08)
        mountainPaint.setColor(0xFF1E1435);
        float mOffX = camX * 0.08f;
        float mOffY = camY * 0.08f;

        path.reset();
        path.moveTo(0, screenH);
        for (int x = 0; x <= screenW; x += 40) {
            float py = screenH * 0.45f + (float) Math.sin((x + mOffX) * 0.008f) * 60f + mOffY * 0.1f;
            path.lineTo(x, py);
        }
        path.lineTo(screenW, screenH);
        path.close();
        canvas.drawPath(path, mountainPaint);

        // Layer 2: Floating Celestial Isles (Parallax 0.18)
        islandPaint.setColor(0xFF2A1B4E);
        for (int i = 0; i < 3; i++) {
            float ix = (i * 320f - mOffX * 2.2f) % (screenW + 300f);
            if (ix < -150f) ix += screenW + 300f;
            float iy = screenH * 0.30f + i * 80f + (float) Math.sin(animTimer + i) * 12f - mOffY * 0.2f;

            rect.set(ix - 80f, iy - 20f, ix + 80f, iy + 20f);
            canvas.drawOval(rect, islandPaint);

            // Floating island under-root
            path.reset();
            path.moveTo(ix - 60f, iy);
            path.lineTo(ix, iy + 45f);
            path.lineTo(ix + 60f, iy);
            path.close();
            canvas.drawPath(path, islandPaint);
        }

        // Layer 3: Spirit Mist Clouds & Aura Dust (Parallax 0.35)
        mistPaint.setColor(0x2000E5FF);
        for (int i = 0; i < 5; i++) {
            float cx = ((i * 220f + animTimer * 25f) - camX * 0.35f) % (screenW + 400f);
            if (cx < -200f) cx += screenW + 400f;
            float cy = screenH * 0.50f + i * 60f - camY * 0.35f;

            rect.set(cx - 150f, cy - 35f, cx + 150f, cy + 35f);
            canvas.drawOval(rect, mistPaint);
        }
    }
}
