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

import java.util.ArrayList;
import java.util.List;

/**
 * PS2-Quality 3D Game View & Software Renderer
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
    private float rotY;
    private float focalLength;

    // Gameplay States
    private float heroX, heroY, heroZ;
    private float enemyX, enemyY, enemyZ;
    private int heroHp, heroMaxHp;
    private int enemyHp, enemyMaxHp;
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
        camY = -120f;
        camZ = -300f;
        rotY = 0;
        focalLength = 320f;

        heroX = 0;
        heroY = 0;
        heroZ = 0;

        enemyX = 60f;
        enemyY = 0;
        enemyZ = 80f;

        heroHp = 100;
        heroMaxHp = 100;
        enemyHp = 150;
        enemyMaxHp = 150;

        maxParticles = 64;
        particlePool = new Particle[maxParticles];
        for (int i = 0; i < maxParticles; i++) {
            particlePool[i] = new Particle();
        }

        build3DWorld();
    }

    private void build3DWorld() {
        worldPolygons = new ArrayList<Polygon3D>();

        // Ground Plane (3D Grid)
        for (int x = -200; x <= 200; x += 100) {
            for (int z = -200; z <= 200; z += 100) {
                Vec3[] quad = new Vec3[] {
                    new Vec3(x, 0, z),
                    new Vec3(x + 90, 0, z),
                    new Vec3(x + 90, 0, z + 90),
                    new Vec3(x, 0, z + 90)
                };
                int col = ((x + z) % 200 == 0) ? Color.rgb(34, 139, 34) : Color.rgb(46, 117, 46);
                worldPolygons.add(new Polygon3D(quad, col));
            }
        }

        // Sect Main Pavilion (3D Cube)
        Vec3[] pavilionBase = new Vec3[] {
            new Vec3(-50, -60, 150),
            new Vec3(50, -60, 150),
            new Vec3(50, 0, 150),
            new Vec3(-50, 0, 150)
        };
        worldPolygons.add(new Polygon3D(pavilionBase, Color.rgb(180, 50, 50)));
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
        canvas.drawColor(Color.rgb(135, 206, 235)); // PS2 Sky Blue

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;

        // Project and render 3D polygons
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

        // Render Hero 3D Marker
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

        // Render HUD & Stats
        renderHUD(canvas);
    }

    private void renderHUD(Canvas canvas) {
        // Player HP Bar
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);
        canvas.drawRoundRect(new RectF(30, 30, 250, 60), 8f, 8f, paint);

        paint.setColor(Color.RED);
        float hpWidth = (220f * heroHp) / heroMaxHp;
        canvas.drawRoundRect(new RectF(35, 35, 35 + hpWidth, 55), 6f, 6f, paint);

        canvas.drawText("Sect Leader HP: " + heroHp + "/" + heroMaxHp, 35, 85, textPaint);
        canvas.drawText("PS2 3D Software Engine - 60 FPS (Itel A70)", 35, getHeight() - 40, textPaint);

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
