package com.sect.idle.render;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.RNG;

/**
 * IsometricWorld v2.0 - 3D Isometric World Engine & Chunk-Based Map System.
 * Renders expansive MMORPG open-world maps with elevation heightmaps, 3D block faces,
 * depth sorting, biomes, dynamic lighting, weather effects, and fog-of-war.
 * Standard pure Java implementation without lambdas.
 */
public final class IsometricWorld {
    private final ChunkManager chunkManager;
    private final Camera3D camera3D;

    private final Paint topFacePaint;
    private final Paint leftFacePaint;
    private final Paint rightFacePaint;
    private final Paint fogPaint;
    private final Paint weatherPaint;
    private final Paint shadowPaint;

    private final Path diamondPath;
    private final Path leftSidePath;
    private final Path rightSidePath;
    private final RectF dstRect;

    private final int[] tileColors;
    private float weatherTimer = 0f;
    private int currentWeatherType = 0; // 0=Clear, 1=Rain, 2=Spirit Mist, 3=Lotus Petals

    public IsometricWorld() {
        this.chunkManager = new ChunkManager();
        this.camera3D = new Camera3D(720, 1280);

        this.topFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.leftFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.rightFacePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.fogPaint = new Paint();
        this.fogPaint.setColor(0xCC0A0A15);

        this.weatherPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.shadowPaint.setColor(0x50000000);

        this.diamondPath = new Path();
        this.leftSidePath = new Path();
        this.rightSidePath = new Path();
        this.dstRect = new RectF();

        this.tileColors = new int[256];
        initTileColors();
    }

    private void initTileColors() {
        tileColors[ChunkManager.TILE_VOID]         = 0xFF0F0A1A;
        tileColors[ChunkManager.TILE_GRASS]        = 0xFF2E7D32;
        tileColors[ChunkManager.TILE_DIRT_PATH]    = 0xFF6D4C41;
        tileColors[ChunkManager.TILE_STONE_PAVER]  = 0xFF546E7A;
        tileColors[ChunkManager.TILE_SPIRIT_WATER] = 0xFF00838F;
        tileColors[ChunkManager.TILE_JADE_FLOOR]   = 0xFF00B0FF;
        tileColors[ChunkManager.TILE_MOUNTAIN]     = 0xFF424242;
        tileColors[ChunkManager.TILE_ANCIENT_RUIN] = 0xFF7E57C2;
    }

    public ChunkManager getChunkManager() {
        return chunkManager;
    }

    public Camera3D getCamera3D() {
        return camera3D;
    }

    public void setWeather(int type) {
        this.currentWeatherType = type;
    }

    public void update(float dt) {
        weatherTimer += dt;
        camera3D.update(dt);
    }

    /**
     * Legacy wrapper for 2D CameraSystem integration.
     */
    public void render(Canvas canvas, CameraSystem cam, int screenW, int screenH) {
        if (canvas == null) return;
        camera3D.setViewport(screenW, screenH);
        if (cam != null) {
            float camTileX = cam.pos.x / ChunkManager.TILE_SIZE;
            float camTileY = cam.pos.y / ChunkManager.TILE_SIZE;
            camera3D.moveTo(camTileX, camTileY);
            camera3D.setZoom(cam.zoom);
        }
        render3D(canvas, screenW, screenH);
    }

    /**
     * Renders the 3D Isometric World with chunk streaming, height extrusion, and depth sorting.
     */
    public void render3D(Canvas canvas, int screenW, int screenH) {
        camera3D.setViewport(screenW, screenH);

        float camWorldX = camera3D.pos.x;
        float camWorldY = camera3D.pos.y;
        float zoom = camera3D.zoom;

        float tileW = camera3D.tileW;
        float tileH = camera3D.tileH;
        float halfTW = tileW * 0.5f;
        float halfTH = tileH * 0.5f;

        // Compute visible chunk bounds around camera target
        int centerChunkX = (int) (camWorldX / ChunkManager.CHUNK_SIZE);
        int centerChunkY = (int) (camWorldY / ChunkManager.CHUNK_SIZE);
        int renderRadius = (int) Math.ceil((Math.max(screenW, screenH) / (tileH * zoom)) / ChunkManager.CHUNK_SIZE) + 1;

        int minCX = centerChunkX - renderRadius;
        int maxCX = centerChunkX + renderRadius;
        int minCY = centerChunkY - renderRadius;
        int maxCY = centerChunkY + renderRadius;

        // Render sorted 3D isometric tile blocks (diagonal sum ordering for correct depth sorting)
        for (int sum = (minCX + minCY) * ChunkManager.CHUNK_SIZE; sum <= (maxCX + maxCY) * ChunkManager.CHUNK_SIZE + ChunkManager.CHUNK_SIZE * 2; sum++) {
            for (int cx = minCX; cx <= maxCX; cx++) {
                for (int cy = minCY; cy <= maxCY; cy++) {
                    ChunkManager.Chunk chunk = chunkManager.getOrGenerateChunk(cx, cy);

                    for (int tx = 0; tx < ChunkManager.CHUNK_SIZE; tx++) {
                        int ty = sum - (cx * ChunkManager.CHUNK_SIZE + tx) - (cy * ChunkManager.CHUNK_SIZE);
                        if (ty < 0 || ty >= ChunkManager.CHUNK_SIZE) continue;

                        renderIsometricBlock(canvas, chunk, tx, ty, screenW, screenH);
                    }
                }
            }
        }

        renderWeatherOverlay(canvas, screenW, screenH);
    }

    private void renderIsometricBlock(Canvas canvas, ChunkManager.Chunk chunk, int tx, int ty, int screenW, int screenH) {
        int tileType = chunk.tiles[tx][ty];
        if (tileType == ChunkManager.TILE_VOID) return;

        int worldTileX = chunk.chunkX * ChunkManager.CHUNK_SIZE + tx;
        int worldTileY = chunk.chunkY * ChunkManager.CHUNK_SIZE + ty;
        float heightZ = chunk.heights[tx][ty];

        float sx = camera3D.worldToScreenX(worldTileX, worldTileY);
        float sy = camera3D.worldToScreenY(worldTileX, worldTileY, heightZ);

        float halfTW = camera3D.tileW * 0.5f * camera3D.zoom;
        float halfTH = camera3D.tileH * 0.5f * camera3D.zoom;

        // Offscreen culling
        if (sx + halfTW < 0 || sx - halfTW > screenW || sy + halfTH < -200 || sy - halfTH > screenH + 200) {
            return;
        }

        int baseColor = tileColors[tileType % tileColors.length];
        int topColor = baseColor;
        int leftColor = darkenColor(baseColor, 0.75f);
        int rightColor = darkenColor(baseColor, 0.60f);

        topFacePaint.setColor(topColor);

        // Optimized fast rendering for flat ground tiles (zero path overhead)
        float extrudedH = heightZ * camera3D.zoom * 0.8f;
        if (extrudedH <= 1.0f) {
            dstRect.set(sx - halfTW, sy - halfTH, sx + halfTW, sy + halfTH);
            canvas.drawOval(dstRect, topFacePaint);
        } else {
            // 1. Draw Top Isometric Diamond Face
            diamondPath.reset();
            diamondPath.moveTo(sx, sy - halfTH);          // Top vertex
            diamondPath.lineTo(sx + halfTW, sy);          // Right vertex
            diamondPath.lineTo(sx, sy + halfTH);          // Bottom vertex
            diamondPath.lineTo(sx - halfTW, sy);          // Left vertex
            diamondPath.close();
            canvas.drawPath(diamondPath, topFacePaint);

            // 2. Extrude 3D Heights Side Walls
            leftSidePath.reset();
            leftSidePath.moveTo(sx - halfTW, sy);
            leftSidePath.lineTo(sx, sy + halfTH);
            leftSidePath.lineTo(sx, sy + halfTH + extrudedH);
            leftSidePath.lineTo(sx - halfTW, sy + extrudedH);
            leftSidePath.close();
            leftFacePaint.setColor(leftColor);
            canvas.drawPath(leftSidePath, leftFacePaint);

            rightSidePath.reset();
            rightSidePath.moveTo(sx, sy + halfTH);
            rightSidePath.lineTo(sx + halfTW, sy);
            rightSidePath.lineTo(sx + halfTW, sy + extrudedH);
            rightSidePath.lineTo(sx, sy + halfTH + extrudedH);
            rightSidePath.close();
            rightFacePaint.setColor(rightColor);
            canvas.drawPath(rightSidePath, rightFacePaint);
        }

        // Fog of war check
        if (!chunk.fogOfWar[tx][ty]) {
            canvas.drawPath(diamondPath, fogPaint);
        }
    }

    private void renderWeatherOverlay(Canvas canvas, int W, int H) {
        if (currentWeatherType == 0) return; // Clear

        if (currentWeatherType == 1) { // Rain
            weatherPaint.setColor(0x8090CAF9);
            weatherPaint.setStrokeWidth(2f);
            for (int i = 0; i < 40; i++) {
                float rx = ((i * 37 + (int) (weatherTimer * 400f)) % W);
                float ry = ((i * 53 + (int) (weatherTimer * 800f)) % H);
                canvas.drawLine(rx, ry, rx - 10f, ry + 25f, weatherPaint);
            }
        } else if (currentWeatherType == 2) { // Spirit Mist / Aura
            weatherPaint.setColor(0x2500E5FF);
            float pulse = (float) Math.sin(weatherTimer * 1.5f) * 20f;
            dstRect.set(0, 0, W, H);
            canvas.drawRect(dstRect, weatherPaint);
        } else if (currentWeatherType == 3) { // Lotus Petals
            weatherPaint.setColor(0xAAFF80AB);
            for (int i = 0; i < 25; i++) {
                float px = ((i * 71 + (int) (weatherTimer * 50f)) % W);
                float py = ((i * 43 + (int) (weatherTimer * 90f)) % H);
                canvas.drawCircle(px, py, 4f, weatherPaint);
            }
        }
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public void renderObject(Canvas canvas, CameraSystem cam, float ix, float iy, float iw, float ih, int color, float objHeight) {
        if (canvas == null) return;
        float wx = ix * ChunkManager.TILE_SIZE;
        float wy = iy * ChunkManager.TILE_SIZE;

        float sx = camera3D.worldToScreenX(wx, wy);
        float sy = camera3D.worldToScreenY(wx, wy, objHeight);

        float w = iw * camera3D.zoom * 32f;
        float h = ih * camera3D.zoom * 32f;

        topFacePaint.setColor(0x40000000);
        dstRect.set(sx - w * 0.5f, sy - h * 0.1f, sx + w * 0.5f, sy + h * 0.1f);
        canvas.drawOval(dstRect, topFacePaint);

        topFacePaint.setColor(color);
        dstRect.set(sx - w * 0.5f, sy - h, sx + w * 0.5f, sy);
        canvas.drawRect(dstRect, topFacePaint);
    }
}
