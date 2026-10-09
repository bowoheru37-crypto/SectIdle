package com.sect.idle.procedural;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * VoxelMeshBuilder - 3D Voxel Engine & Isometric Mesh Projection.
 * Synthesizes 3D voxel volumes (X,Y,Z) and projects them into 3D isometric asset sprites with lighting.
 * Pure Java standard without lambdas.
 */
public final class VoxelMeshBuilder {
    private final int sizeX, sizeY, sizeZ;
    private final int[][][] voxels; // Color ARGB per voxel

    private final Paint topPaint;
    private final Paint leftPaint;
    private final Paint rightPaint;
    private final Path path;

    public VoxelMeshBuilder(int sx, int sy, int sz) {
        this.sizeX = sx;
        this.sizeY = sy;
        this.sizeZ = sz;
        this.voxels = new int[sx][sy][sz];

        this.topPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        this.path = new Path();
    }

    public void clear() {
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    voxels[x][y][z] = 0;
                }
            }
        }
    }

    public void setVoxel(int x, int y, int z, int color) {
        if (x >= 0 && x < sizeX && y >= 0 && y < sizeY && z >= 0 && z < sizeZ) {
            voxels[x][y][z] = color;
        }
    }

    public void addBox(int x1, int y1, int z1, int x2, int y2, int z2, int color) {
        for (int x = Math.max(0, x1); x <= Math.min(sizeX - 1, x2); x++) {
            for (int y = Math.max(0, y1); y <= Math.min(sizeY - 1, y2); y++) {
                for (int z = Math.max(0, z1); z <= Math.min(sizeZ - 1, z2); z++) {
                    voxels[x][y][z] = color;
                }
            }
        }
    }

    public void addCylinder(int cx, int cy, int z1, int z2, int radius, int color) {
        int rSq = radius * radius;
        for (int x = 0; x < sizeX; x++) {
            for (int y = 0; y < sizeY; y++) {
                int dx = x - cx;
                int dy = y - cy;
                if (dx * dx + dy * dy <= rSq) {
                    for (int z = Math.max(0, z1); z <= Math.min(sizeZ - 1, z2); z++) {
                        voxels[x][y][z] = color;
                    }
                }
            }
        }
    }

    /**
     * Projects the 3D voxel model into an isometric Bitmap asset.
     */
    public Bitmap projectToIsometricBitmap(int outputW, int outputH) {
        Bitmap bmp = Bitmap.createBitmap(outputW, outputH, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);

        float centerX = outputW * 0.5f;
        float centerY = outputH * 0.70f;
        float voxelSize = Math.min(outputW, outputH) / (float) (Math.max(sizeX, sizeY) * 2.2f);
        float halfV = voxelSize * 0.5f;
        float quarterV = voxelSize * 0.25f;

        // Depth sorted rendering order (X + Y + Z)
        for (int sum = 0; sum < sizeX + sizeY + sizeZ; sum++) {
            for (int x = 0; x < sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    int z = sum - x - y;
                    if (z < 0 || z >= sizeZ) continue;

                    int color = voxels[x][y][z];
                    if (color == 0) continue;

                    // Compute isometric projection point
                    float isoX = centerX + (x - y) * halfV;
                    float isoY = centerY + (x + y) * quarterV - z * (voxelSize * 0.6f);

                    renderVoxelCube(canvas, isoX, isoY, voxelSize, color);
                }
            }
        }

        return bmp;
    }

    private void renderVoxelCube(Canvas canvas, float cx, float cy, float vSize, int baseColor) {
        float halfW = vSize * 0.5f;
        float halfH = vSize * 0.25f;
        float depth = vSize * 0.6f;

        int topColor = baseColor;
        int leftColor = darkenColor(baseColor, 0.75f);
        int rightColor = darkenColor(baseColor, 0.55f);

        // 1. Top Diamond Face
        path.reset();
        path.moveTo(cx, cy - halfH);
        path.lineTo(cx + halfW, cy);
        path.lineTo(cx, cy + halfH);
        path.lineTo(cx - halfW, cy);
        path.close();
        topPaint.setColor(topColor);
        canvas.drawPath(path, topPaint);

        // 2. Left Face
        path.reset();
        path.moveTo(cx - halfW, cy);
        path.lineTo(cx, cy + halfH);
        path.lineTo(cx, cy + halfH + depth);
        path.lineTo(cx - halfW, cy + depth);
        path.close();
        leftPaint.setColor(leftColor);
        canvas.drawPath(path, leftPaint);

        // 3. Right Face
        path.reset();
        path.moveTo(cx, cy + halfH);
        path.lineTo(cx + halfW, cy);
        path.lineTo(cx + halfW, cy + depth);
        path.lineTo(cx, cy + halfH + depth);
        path.close();
        rightPaint.setColor(rightColor);
        canvas.drawPath(path, rightPaint);
    }

    private int darkenColor(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
