package com.sect.idle.render;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.MathUtils;
import com.sect.idle.core.Vector2;

/**
 * Camera3D - 3D Isometric Camera projection system.
 * Supports smooth 3D isometric transforms, zoom, pitch, rotation, and screen-to-world mapping.
 * Written in standard pure Java without lambdas for Sketchware Pro compatibility.
 */
public final class Camera3D {
    public final Vector2 pos;
    public final Vector2 targetPos;
    public float zoom;
    public float targetZoom;
    public float pitch; // Pitch angle in degrees (default ~30 deg isometric view)
    public float rotation; // Yaw angle in degrees (0, 90, 180, 270)
    public int viewportW, viewportH;

    public float shakeIntensity;
    public int shakeDuration;
    private float trauma;
    private final Vector2 shakeOffset;
    private final java.util.Random rand;

    public float tileW = 64f;
    public float tileH = 32f;

    public Camera3D(int vw, int vh) {
        this.pos = new Vector2(2000f, 2000f);
        this.targetPos = new Vector2(2000f, 2000f);
        this.zoom = 1.0f;
        this.targetZoom = 1.0f;
        this.pitch = 30f;
        this.rotation = 0f;
        this.viewportW = Math.max(1, vw);
        this.viewportH = Math.max(1, vh);
        this.shakeOffset = new Vector2();
        this.rand = new java.util.Random();
    }

    public void setViewport(int w, int h) {
        this.viewportW = Math.max(1, w);
        this.viewportH = Math.max(1, h);
    }

    public void update(float dt) {
        dt = MathUtils.clamp(dt, 0.001f, 0.05f);

        // Smooth position interpolation
        float lerpSpeed = GameConfig.CAM_LERP;
        float omega = 2.0f / Math.max(0.01f, lerpSpeed);
        float t = MathUtils.clamp01(omega * dt);
        pos.lerp(targetPos.x, targetPos.y, t);

        // Smooth zoom interpolation
        zoom = MathUtils.approach(zoom, targetZoom, 3.0f * dt);
        zoom = MathUtils.clamp(zoom, GameConfig.CAM_MIN_ZOOM, GameConfig.CAM_MAX_ZOOM);

        // Camera Shake handling
        shakeOffset.set(0f, 0f);
        if (trauma > 0.001f) {
            float shake = trauma * trauma;
            shakeOffset.x = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity * shake;
            shakeOffset.y = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity * shake;
            trauma *= GameConfig.CAM_SHAKE_DECAY;
            if (trauma < 0.001f) trauma = 0f;
        } else if (shakeDuration > 0) {
            shakeOffset.x = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity;
            shakeOffset.y = (rand.nextFloat() - 0.5f) * 2f * shakeIntensity;
            shakeDuration--;
            if (shakeDuration <= 0) shakeIntensity = 0f;
        }
    }

    public void shake(float intensity, int duration) {
        this.shakeIntensity = Math.max(0f, intensity);
        this.shakeDuration = Math.max(0, duration);
        this.trauma = MathUtils.clamp01(intensity / 10f);
    }

    public void moveTo(float x, float y) {
        targetPos.set(x, y);
    }

    public void setZoom(float z) {
        targetZoom = MathUtils.clamp(z, GameConfig.CAM_MIN_ZOOM, GameConfig.CAM_MAX_ZOOM);
    }

    /**
     * Converts world coordinates (worldX, worldY, heightZ) into screen pixel coordinates (SX, SY)
     * using 3D isometric projection matrix math.
     */
    public float worldToScreenX(float wx, float wy) {
        float isoX = (wx - wy) * (tileW * 0.5f);
        float relX = isoX - (pos.x - pos.y) * (tileW * 0.5f);
        return viewportW * 0.5f + (relX + shakeOffset.x) * zoom;
    }

    public float worldToScreenY(float wx, float wy, float heightZ) {
        float isoY = (wx + wy) * (tileH * 0.5f) - heightZ;
        float relY = isoY - (pos.x + pos.y) * (tileH * 0.5f);
        return viewportH * 0.5f + (relY + shakeOffset.y) * zoom;
    }

    /**
     * Converts screen coordinates (SX, SY) back into 2D ground world coordinates.
     */
    public float screenToWorldX(float sx, float sy) {
        float relSX = (sx - viewportW * 0.5f) / zoom - shakeOffset.x;
        float relSY = (sy - viewportH * 0.5f) / zoom - shakeOffset.y;

        float isoX = relSX + (pos.x - pos.y) * (tileW * 0.5f);
        float isoY = relSY + (pos.x + pos.y) * (tileH * 0.5f);

        // Solve: isoX = (wx - wy) * halfTW, isoY = (wx + wy) * halfTH
        float halfTW = tileW * 0.5f;
        float halfTH = tileH * 0.5f;

        float wx = ((isoX / halfTW) + (isoY / halfTH)) * 0.5f;
        return wx;
    }

    public float screenToWorldY(float sx, float sy) {
        float relSX = (sx - viewportW * 0.5f) / zoom - shakeOffset.x;
        float relSY = (sy - viewportH * 0.5f) / zoom - shakeOffset.y;

        float isoX = relSX + (pos.x - pos.y) * (tileW * 0.5f);
        float isoY = relSY + (pos.x + pos.y) * (tileH * 0.5f);

        float halfTW = tileW * 0.5f;
        float halfTH = tileH * 0.5f;

        float wy = ((isoY / halfTH) - (isoX / halfTW)) * 0.5f;
        return wy;
    }
}
