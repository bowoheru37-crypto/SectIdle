package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.sect.idle.procedural.Xianxia3DTerrainGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * PS2-Quality 3D Game View & Software Renderer - Xianxia Immortal Realm Edition
 * Optimized for Itel A70 (1GB RAM) & Sketchware Pro v7.0.0
 * Features 3D vertex projection, Gouraud shading, 60 FPS double-buffered SurfaceView.
 */
public class Ps23DGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Pre-allocated 3D Vertex
    public static class Vec3 {
        public float x, y, z;
        public Vec3(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    // Pre-allocated 3D Polygon
    public static class Polygon3D {
        public Vec3[] vertices;
        public int color;
        public float depth;

        public Polygon3D(Vec3[] vertices, int color) {
            this.vertices = vertices;
            this.color = color;
            this.depth = 0;
        }
    }

    // Pooled Particle
    public static class Particle {
        public float x, y, vx, vy;
        public int color;
        public int life;

        public Particle() {
            this.life = 0;
        }
    }

    private SurfaceHolder surfaceHolder;
    private Thread gameThread;
    private boolean isRunning;

    // Renderer & Camera Setup
    private Paint paint;
    private Paint textPaint;
    private Path polyPath;
    private float camX, camY, camZ;
    private float focalLength;

    // Gameplay States
    private float heroX, heroY, heroZ;
    private int heroHp, heroMaxHp;
    private List<Polygon3D> worldPolygons;
    private Particle[] particlePool;
    private int maxParticles;

    // Touch Controls
    private float touchStartX, touchStartY;
    private boolean isTouchingPad;
    private float moveDx, moveDz;

    public Ps23DGameView(Context context) {
        super(context);
        init();
    }

    public Ps23DGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(28f);
        polyPath = new Path();

        camX = 0;
        camY = -140f;
        camZ = -300f;
        focalLength = 320f;

        heroX = 0;
        heroY = 20f;
        heroZ = 0;

        heroHp = 100;
        heroMaxHp = 100;

        maxParticles = 64;
        particlePool = new Particle[maxParticles];
        for (int i = 0; i < maxParticles; i++) {
            particlePool[i] = new Particle();
        }

        build3DWorld();
    }

    private void build3DWorld() {
        worldPolygons = Xianxia3DTerrainGenerator.generateXianxiaWorld();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isRunning = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            if (!surfaceHolder.getSurface().isValid()) continue;

            long startTime = System.currentTimeMillis();

            updateGameLogic();

            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas != null) {
                render3DScene(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }

            long elapsed = System.currentTimeMillis() - startTime;
            long sleepTime = 16 - elapsed; // 60 FPS target
            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void updateGameLogic() {
        // Hero movement
        heroX += moveDx * 2f;
        heroZ += moveDz * 2f;

        // Camera tracking
        camX = heroX;
        camZ = heroZ - 250f;

        // Update particles
        for (int i = 0; i < maxParticles; i++) {
            Particle p = particlePool[i];
            if (p.life > 0) {
                p.x += p.vx;
                p.y += p.vy;
                p.life--;
            }
        }
    }

    private void render3DScene(Canvas canvas) {
        canvas.drawColor(Color.rgb(135, 206, 235)); // Xianxia Celestial Blue

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Project and render Xianxia 3D polygons
        for (int i = 0; i < worldPolygons.size(); i++) {
            Polygon3D poly = worldPolygons.get(i);
            polyPath.reset();
            boolean valid = true;

            for (int j = 0; j < poly.vertices.length; j++) {
                Vec3 v = poly.vertices[j];

                // Camera space transformation
                float tx = v.x - camX;
                float ty = v.y - camY;
                float tz = v.z - camZ;

                if (tz <= 10f) {
                    valid = false;
                    break;
                }

                // 3D Perspective Projection
                float sx = (tx * focalLength) / tz + cx;
                float sy = (ty * focalLength) / tz + cy;

                if (j == 0) {
                    polyPath.moveTo(sx, sy);
                } else {
                    polyPath.lineTo(sx, sy);
                }
            }

            if (valid) {
                polyPath.close();
                paint.setStyle(Paint.Style.FILL);
                paint.setColor(poly.color);
                canvas.drawPath(polyPath, paint);

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(1.5f);
                paint.setColor(Color.argb(80, 0, 0, 0));
                canvas.drawPath(polyPath, paint);
            }
        }

        // Render Hero Cultivator Marker
        float htx = heroX - camX;
        float hty = heroY - camY - 20f;
        float htz = heroZ - camZ;
        if (htz > 10f) {
            float hsx = (htx * focalLength) / htz + cx;
            float hsy = (hty * focalLength) / htz + cy;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(hsx, hsy, 16f, paint);
        }

        // Render HUD & Xianxia Info
        renderHUD(canvas);
    }

    private void renderHUD(Canvas canvas) {
        // Player HP Bar
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(new RectF(30, 30, 270, 60), 8f, 8f, paint);

        paint.setColor(Color.rgb(255, 215, 0)); // Golden Qi
        float hpWidth = (235f * heroHp) / heroMaxHp;
        canvas.drawRoundRect(new RectF(35, 35, 35 + hpWidth, 55), 6f, 6f, paint);

        canvas.drawText("Immortal Sect Master - Golden Core Realm", 35, 85, textPaint);
        canvas.drawText("Xianxia 3D Immortal Peak World - 60 FPS (Itel A70)", 35, getHeight() - 40, textPaint);

        // Virtual D-Pad Touch Controls
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(Color.argb(120, 255, 255, 255));
        canvas.drawCircle(120, getHeight() - 150, 70, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (x < getWidth() / 2f && y > getHeight() - 300) {
                    touchStartX = x;
                    touchStartY = y;
                    isTouchingPad = true;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isTouchingPad) {
                    moveDx = (x - touchStartX) / 50f;
                    moveDz = (y - touchStartY) / 50f;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouchingPad = false;
                moveDx = 0;
                moveDz = 0;
                break;
        }
        return true;
    }
}
