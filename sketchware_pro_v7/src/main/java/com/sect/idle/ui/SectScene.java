package com.sect.idle.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.app.ActivityManager;
import android.util.Log;

import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameTime;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.procedural.SpriteMaker;

import com.sect.idle.systems.CameraSystem;
import com.sect.idle.systems.NumberFormatter;
import com.sect.idle.systems.RNG;

public final class SectScene {
    private static final String TAG = "SectScene";

    private final Context appContext;
    private CameraSystem camera;

    private Bitmap atlas, tileset, parallaxBg, walkSheet;
    private boolean assetsReady = false;

    // ===== TILEMAP =====
    // FIX (world scale): peta diperbesar dari 48x48 -> 96x96 tile (3072 unit
    // -> 6144 unit). Kombinasi dengan building yang disebar lebih jauh dan
    // camera bounds yang diperluas (lihat GameView) supaya dunia terasa luas,
    // bukan cuma kotak kecil padat di tengah layar.
    private static final int TILE_SIZE = 64;
    private static final int MAP_TILES_X = 96;
    private static final int MAP_TILES_Y = 96;
    public static final float MAP_CENTER_X = 2000f;
    public static final float MAP_CENTER_Y = 2000f;
    private static final float MAP_ORIGIN_X = MAP_CENTER_X - (MAP_TILES_X * TILE_SIZE) * 0.5f;
    private static final float MAP_ORIGIN_Y = MAP_CENTER_Y - (MAP_TILES_Y * TILE_SIZE) * 0.5f;
    private final int[][] tileMap;
    private final int[][] tileVariation;

    private static final boolean USE_REAL_GROUND_TEXTURE = false;
    private static final int TILE_TYPE_COUNT_USED = 6;
    private static final int TILE_VARIATION_COUNT = 8;
    private boolean tilesetGroundValid = false;
    private int tilesetVariationCols = 1;

    private final Paint bgPaint, gridPaint, tilePaint, buildingPaint, buildingBorderPaint;
    private final Paint buildingGlowPaint, buildingTopPaint, buildingSidePaint;
    private final Paint buildingGhostPaint, buildingGhostTextPaint; // FIX: ghost state untuk unbuilt
    private final Paint disciplePaint, hpBgPaint, hpPaint, mpPaint;
    private final Paint uiPaint, uiTextPaint, selectionPaint, namePaint, auraPaint;
    private final Paint lightPaint, shadowPaint, particlePaint, floatingTextPaint;
    private final Paint minimapPaint, minimapDotPaint;
    private final Paint atlasIconPaint;
    private final Paint topBarGradientPaint;
    private final Paint tapDebugPaint; // FIX: visual feedback tap, hanya aktif saat GameConfig.DEBUG

    private final RectF r1, r2;
    private final Rect srcRect, dstRect;
    private final Rect atlasSrcRect;
    private final StringBuilder sb1, sb2;

    private final float[] bldgX, bldgY;
    private static final int MAX_BUILDINGS = GameConfig.MAX_BUILDINGS;
    // FIX (world scale): spacing antar building diperbesar dari 320 -> 480,
    // dan disusun melingkar bukan grid kaku 4-kolom -- terasa lebih seperti
    // kompleks sekte yang "dibangun", bukan kotak kosong berjejer.
    private static final float BLDG_SPACING = 480f;
    private static final float GRID = 200f, DISC_R = 18f, BLDG_S = 90f;
    private static final float BAR_W = 40f, BAR_H = 6f;
    private static final int UI_TOP_H = 76, UI_BOT_H = 58;

    // ===== SELECTION STATE =====
    private String selectedDiscipleId = null;
    private int selectedDisciple = -1;
    private int selectedBuilding = -1;
    private float animTime = 0f;

    private static final int MAX_PARTICLES = 30;
    private final float[] ptX, ptY, ptVX, ptVY, ptLife, ptMaxLife, ptSize;
    private final int[] ptColor;
    private int activeParticles = 0;

    private static final int MAX_FLOATING = 16;
    private final String[] ftText;
    private final float[] ftX, ftY, ftLife, ftMaxLife;
    private final int[] ftColor;
    private int activeFloating = 0;

    private final boolean lowEnd;
    private final boolean enableParticles;
    private final boolean enableLighting;
    private final boolean enableShadows;

    private static final int WALK_FRAME_COUNT = 6;
    private static final float ANIM_FPS = 8f;
    private boolean walkSheetGridValid = false;
    private int walkFrameW = 0;
    private int walkFrameH = 0;

    // FIX (world scale): minimap sekarang merepresentasikan world yang lebih
    // besar (lihat MINIMAP_WORLD_SIZE), otomatis mengikuti MAP_TILES.
    private static final float MINIMAP_SIZE = 120f;
    private static final float MINIMAP_Y = 10f;
    private static final float MINIMAP_WORLD_SIZE = MAP_TILES_X * TILE_SIZE * 1.3f;

    private boolean atlasReady = false;

    private static final int[][] BUILD_ICON_RECT_PX = {
        {0,   0, 32, 32}, {32,  0, 32, 32}, {64,  0, 32, 32}, {96,  0, 32, 32},
        {128, 0, 32, 32}, {160, 0, 32, 32}, {192, 0, 32, 32}, {224, 0, 32, 32},
        {-1, -1, -1, -1}, {-1, -1, -1, -1},
    };

    private static final int LIGHT_BAKE_SIZE = 128;
    private Bitmap lightBakedBitmap;
    private final Bitmap[] buildingIconCache = new Bitmap[16];
    private final java.util.HashMap<String, Bitmap> discipleSpriteCache = new java.util.HashMap<String, Bitmap>();

    private float frameZoom = 1f;
    private int buildingCount = 0;
    private int discipleCount = 0;
    private int tbStartX, tbStartY, tbEndX, tbEndY;

    private static final int SKY_NIGHT = 0xFF0F0A1A;
    private static final int SKY_DAWN  = 0xFFFFB08A;
    private static final int SKY_DAY   = 0xFFAEE0F0;
    private static final int SKY_DUSK  = 0xFFFF8A65;
    private final Paint dayNightOverlayPaint;
    private float cachedDayT = 0f;

    private static final int[] FALLBACK_TILE_COLORS = {0xFF1A0F2E, 0xFF004466, 0xFF333344, 0xFF2D1B4E, 0xFF3A2A50};

    public interface OnSelectionListener {
        void onDiscipleSelected(Disciple d);
        void onBuildingTapped(Building b, boolean isBuilt);
        void onBattleRequested();
        void onTournamentRequested();
        void onWarRequested();
        void onRecruitRequested();
        void onMarketRequested();
        void onMenuRequested();
    }
    private OnSelectionListener selectionListener;
    private final SlashMiniGame slashMiniGame;
    public void setSelectionListener(OnSelectionListener l) {
        this.selectionListener = l;
        Log.d(TAG, "setSelectionListener called, listener=" + (l != null));
    }
    public SlashMiniGame getSlashMiniGame() { return slashMiniGame; }

    private float lastTapWorldX = Float.NaN, lastTapWorldY = Float.NaN;
    private float tapMarkerLife = 0f;

    public SectScene(Context ctx) {
        this.appContext = ctx.getApplicationContext();
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        lowEnd = am != null && am.isLowRamDevice();
        enableParticles = !lowEnd || GameConfig.ENABLE_PARTICLES;
        enableLighting = !lowEnd && GameConfig.ENABLE_LIGHTING;
        enableShadows = !lowEnd && GameConfig.ENABLE_SHADOWS;

        int aaFlag = lowEnd ? 0 : Paint.ANTI_ALIAS_FLAG;

        bgPaint = new Paint(); bgPaint.setColor(0xFF0F0A1A);
        gridPaint = new Paint(); gridPaint.setColor(0xFF2D1B4E); gridPaint.setStrokeWidth(1f);
        tilePaint = new Paint(aaFlag); tilePaint.setFilterBitmap(true);
        buildingPaint = new Paint(aaFlag); buildingPaint.setColor(0xFF3A2A60);
        buildingBorderPaint = new Paint(aaFlag); buildingBorderPaint.setColor(0xFFFFD700);
        buildingBorderPaint.setStyle(Paint.Style.STROKE); buildingBorderPaint.setStrokeWidth(2f);
        buildingGlowPaint = new Paint(aaFlag); buildingGlowPaint.setColor(0x44FFD700);
        buildingGlowPaint.setStyle(Paint.Style.STROKE); buildingGlowPaint.setStrokeWidth(6f);
        buildingTopPaint = new Paint(aaFlag); buildingTopPaint.setColor(0xFF4A3A70);
        buildingSidePaint = new Paint(aaFlag); buildingSidePaint.setColor(0xFF2A1A50);

        buildingGhostPaint = new Paint(aaFlag);
        buildingGhostPaint.setColor(0xFF888888);
        buildingGhostPaint.setStyle(Paint.Style.STROKE);
        buildingGhostPaint.setStrokeWidth(2.5f);
        buildingGhostTextPaint = new Paint(aaFlag);
        buildingGhostTextPaint.setColor(0xFFCCCCCC);
        buildingGhostTextPaint.setTextAlign(Paint.Align.CENTER);
        buildingGhostTextPaint.setTypeface(Typeface.DEFAULT_BOLD);

        disciplePaint = new Paint(aaFlag);
        hpBgPaint = new Paint(); hpBgPaint.setColor(0xFFFF2222);
        hpPaint = new Paint(); hpPaint.setColor(0xFF00E676);
        mpPaint = new Paint(); mpPaint.setColor(0xFF448AFF);
        uiPaint = new Paint(aaFlag); uiPaint.setTypeface(Typeface.DEFAULT_BOLD);
        uiTextPaint = new Paint(aaFlag); uiTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        uiTextPaint.setTextAlign(Paint.Align.LEFT); uiTextPaint.setShadowLayer(2f, 1f, 1f, 0xFF000000);
        selectionPaint = new Paint(aaFlag); selectionPaint.setColor(0xFFFFD700);
        selectionPaint.setStyle(Paint.Style.STROKE); selectionPaint.setStrokeWidth(2f);
        namePaint = new Paint(aaFlag); namePaint.setColor(0xFFFFFFFF);
        namePaint.setTextAlign(Paint.Align.CENTER); namePaint.setTypeface(Typeface.DEFAULT_BOLD);
        namePaint.setShadowLayer(2f, 1f, 1f, 0xFF000000);
        auraPaint = new Paint(Paint.ANTI_ALIAS_FLAG); auraPaint.setStyle(Paint.Style.STROKE);
        lightPaint = new Paint(Paint.ANTI_ALIAS_FLAG); lightPaint.setFilterBitmap(true);
        shadowPaint = new Paint(); shadowPaint.setColor(0x60000000);
        particlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floatingTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        floatingTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
        floatingTextPaint.setTextAlign(Paint.Align.CENTER);
        floatingTextPaint.setShadowLayer(2f, 1f, 1f, 0xFF000000);
        minimapPaint = new Paint();
        minimapDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        atlasIconPaint = new Paint(aaFlag); atlasIconPaint.setFilterBitmap(true);
        dayNightOverlayPaint = new Paint();

        tapDebugPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tapDebugPaint.setColor(0xFF00FF00);
        tapDebugPaint.setStyle(Paint.Style.STROKE);
        tapDebugPaint.setStrokeWidth(3f);

        topBarGradientPaint = new Paint(aaFlag);
        topBarGradientPaint.setShader(new LinearGradient(
            0, 0, 0, UI_TOP_H, 0xFF2A1A4E, 0x001A0F2E, Shader.TileMode.CLAMP));

        r1 = new RectF(); r2 = new RectF();
        srcRect = new Rect(); dstRect = new Rect();
        atlasSrcRect = new Rect();
        sb1 = new StringBuilder(64); sb2 = new StringBuilder(64);

        bldgX = new float[MAX_BUILDINGS]; bldgY = new float[MAX_BUILDINGS];
        layoutBuildingsRadial();

        tileMap = new int[MAP_TILES_X][MAP_TILES_Y];
        tileVariation = new int[MAP_TILES_X][MAP_TILES_Y];
        generateTilemap();

        ptX = new float[MAX_PARTICLES]; ptY = new float[MAX_PARTICLES];
        ptVX = new float[MAX_PARTICLES]; ptVY = new float[MAX_PARTICLES];
        ptLife = new float[MAX_PARTICLES]; ptMaxLife = new float[MAX_PARTICLES];
        ptSize = new float[MAX_PARTICLES]; ptColor = new int[MAX_PARTICLES];

        ftText = new String[MAX_FLOATING];
        ftX = new float[MAX_FLOATING]; ftY = new float[MAX_FLOATING];
        ftLife = new float[MAX_FLOATING]; ftMaxLife = new float[MAX_FLOATING];
        ftColor = new int[MAX_FLOATING];

        if (enableLighting) {
            lightBakedBitmap = bakeRadialLightTexture(LIGHT_BAKE_SIZE);
        }
        this.slashMiniGame = new SlashMiniGame();
    }

    private void layoutBuildingsRadial() {
        int perRing = 6;
        for (int i = 0; i < MAX_BUILDINGS; i++) {
            int ring = i / perRing;
            int posInRing = i % perRing;
            float radius = BLDG_SPACING * (ring + 1);
            float angle = (float) (posInRing * (2.0 * Math.PI / perRing) + ring * 0.35);
            bldgX[i] = MAP_CENTER_X + (float) Math.cos(angle) * radius;
            bldgY[i] = MAP_CENTER_Y + (float) Math.sin(angle) * radius;
        }
    }

    private void generateTilemap() {
        final float d1Sq = 400f * 400f, d2Sq = 600f * 600f;
        for (int x = 0; x < MAP_TILES_X; x++) {
            for (int y = 0; y < MAP_TILES_Y; y++) {
                float worldX = MAP_ORIGIN_X + x * TILE_SIZE;
                float worldY = MAP_ORIGIN_Y + y * TILE_SIZE;
                float dxc = worldX - MAP_CENTER_X, dyc = worldY - MAP_CENTER_Y;
                float distSq = dxc * dxc + dyc * dyc;
                if (distSq < d1Sq) tileMap[x][y] = 4;
                else if (distSq < d2Sq) tileMap[x][y] = 0;
                else if (RNG.chance(15)) tileMap[x][y] = 3;
                else if (RNG.chance(8)) tileMap[x][y] = 1;
                else tileMap[x][y] = 0;
                tileVariation[x][y] = RNG.nextInt(TILE_VARIATION_COUNT);
            }
        }
    }

    public void setCamera(CameraSystem cam) { this.camera = cam; }
    public CameraSystem getCamera() { return camera; }

    public void setAtlas(Bitmap atlas, Bitmap tileset, Bitmap parallax, Bitmap walkSheet) {
        this.atlas = atlas; this.tileset = tileset;
        this.parallaxBg = parallax; this.walkSheet = walkSheet;
        this.assetsReady = (atlas != null && tileset != null);

        atlasReady = (atlas != null && !atlas.isRecycled()
                && atlas.getWidth() > 0 && atlas.getHeight() > 0);

        tilesetGroundValid = (tileset != null && !tileset.isRecycled()
                && tileset.getWidth() >= TILE_SIZE
                && tileset.getHeight() >= TILE_SIZE * TILE_TYPE_COUNT_USED);
        tilesetVariationCols = tilesetGroundValid
                ? Math.max(1, Math.min(TILE_VARIATION_COUNT, tileset.getWidth() / TILE_SIZE))
                : 1;

        if (walkSheet != null && !walkSheet.isRecycled()
                && walkSheet.getWidth() >= WALK_FRAME_COUNT && walkSheet.getHeight() > 0) {
            walkFrameW = walkSheet.getWidth() / WALK_FRAME_COUNT;
            walkFrameH = walkSheet.getHeight();
            walkSheetGridValid = walkFrameW > 0 && walkFrameH > 0;
        } else {
            walkSheetGridValid = false;
            walkFrameW = 0;
            walkFrameH = 0;
        }
    }

    private boolean getAtlasIconRect(int buildType, Rect out) {
        if (!atlasReady || buildType < 0 || buildType >= BUILD_ICON_RECT_PX.length) return false;
        int[] r = BUILD_ICON_RECT_PX[buildType];
        if (r[0] < 0 || r[1] < 0 || r[2] <= 0 || r[3] <= 0) return false;
        int left = r[0], top = r[1], right = r[0] + r[2], bottom = r[1] + r[3];
        if (right > atlas.getWidth() || bottom > atlas.getHeight()) return false;
        out.set(left, top, right, bottom);
        return true;
    }

    private Bitmap getBuildingIcon(int type) {
        int idx = GameConfig.clamp(type, 0, buildingIconCache.length - 1);
        if (buildingIconCache[idx] == null) {
            try {
                buildingIconCache[idx] = SpriteMaker.createBuilding(96, type, 0, type * 7919L);
            } catch (OutOfMemoryError e) {
                return null;
            }
        }
        return buildingIconCache[idx];
    }

    private Bitmap getDiscipleSprite(Disciple d) {
        if (d == null) return null;
        String key = (d.id != null ? d.id : "d") + "_" + d.realm + "_" + d.element + "_" + d.currentTask;
        Bitmap cached = discipleSpriteCache.get(key);
        if (cached == null || cached.isRecycled()) {
            try {
                cached = SpriteMaker.createDisciple(64, 64, d.element, d.realm, d.isMale, d.currentTask, d.id != null ? d.id.hashCode() : 42L);
                discipleSpriteCache.put(key, cached);
            } catch (OutOfMemoryError oom) {
                return null;
            }
        }
        return cached;
    }

    private static Bitmap bakeRadialLightTexture(int size) {
        try {
            Bitmap bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ALPHA_8);
            Canvas c = new Canvas(bmp);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            float r = size * 0.5f;
            p.setShader(new RadialGradient(r, r, r, 0xFFFFFFFF, 0x00FFFFFF, Shader.TileMode.CLAMP));
            c.drawCircle(r, r, r, p);
            return bmp;
        } catch (OutOfMemoryError oom) {
            return null;
        }
    }

    private void drawLightGlow(Canvas canvas, float cx, float cy, float radius, int color, int intensityAlpha) {
        if (lightBakedBitmap == null || lightBakedBitmap.isRecycled()) return;
        lightPaint.setColor(color);
        lightPaint.setAlpha(intensityAlpha);
        r1.set(cx - radius, cy - radius, cx + radius, cy + radius);
        canvas.drawBitmap(lightBakedBitmap, null, r1, lightPaint);
    }

    private static float safeZoom(CameraSystem cam) {
        float z = cam.zoom;
        return (Float.isNaN(z) || Float.isInfinite(z) || z < 0.05f) ? 0.5f : z;
    }

    private void computeTileBounds(CameraSystem cam, int W, int H) {
        float startX = cam.screenToWorldX(0f), startY = cam.screenToWorldY(0f);
        float endX = cam.screenToWorldX(W), endY = cam.screenToWorldY(H);
        tbStartX = Math.max(0, (int) ((startX - MAP_ORIGIN_X) / TILE_SIZE) - 1);
        tbStartY = Math.max(0, (int) ((startY - MAP_ORIGIN_Y) / TILE_SIZE) - 1);
        tbEndX = Math.min(MAP_TILES_X, (int) ((endX - MAP_ORIGIN_X) / TILE_SIZE) + 2);
        tbEndY = Math.min(MAP_TILES_Y, (int) ((endY - MAP_ORIGIN_Y) / TILE_SIZE) + 2);
    }

    public void update(float dt) {
        animTime += dt;
        if (tapMarkerLife > 0f) tapMarkerLife -= dt;
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.update(dt);
            return;
        }
        SectData data = SectData.getInstance();
        if (data == null) return;

        for (int i = 0, n = data.disciples.size(); i < n; i++) {
            Disciple d = data.disciples.get(i);
            if (d == null) continue;
            if (d.position.distSq(d.targetPos) > 4f) {
                d.moveTo(d.targetPos.x, d.targetPos.y, dt * 60f);
                d.isMoving = true;
                d.facing = d.targetPos.x > d.position.x ? 1 : -1;
            } else {
                d.isMoving = false;
            }
            if (d.isMoving) {
                d.animTimer += dt * ANIM_FPS;
                if (d.animTimer >= WALK_FRAME_COUNT) d.animTimer = 0f;
                d.animFrame = (int) d.animTimer;
            } else {
                d.animFrame = 0; d.animTimer = 0f;
            }
        }
        if (enableParticles) updateParticles(dt);
        updateFloatingTexts(dt);
    }

    private void updateParticles(float dt) {
        for (int i = activeParticles - 1; i >= 0; i--) {
            ptLife[i] -= dt;
            if (ptLife[i] <= 0) {
                int last = activeParticles - 1;
                if (i < last) {
                    ptX[i] = ptX[last]; ptY[i] = ptY[last];
                    ptVX[i] = ptVX[last]; ptVY[i] = ptVY[last];
                    ptLife[i] = ptLife[last]; ptMaxLife[i] = ptMaxLife[last];
                    ptSize[i] = ptSize[last]; ptColor[i] = ptColor[last];
                }
                activeParticles--;
                continue;
            }
            ptX[i] += ptVX[i] * dt; ptY[i] += ptVY[i] * dt; ptVY[i] += 10f * dt;
        }
        if (activeParticles < MAX_PARTICLES && RNG.chance(30)) {
            spawnParticle(MAP_CENTER_X + RNG.nextFloat(-300, 300), MAP_CENTER_Y + RNG.nextFloat(-300, 300),
                    RNG.nextFloat(-10, 10), RNG.nextFloat(-20, -5),
                    RNG.nextFloat(1f, 3f), 0xFFFFD700, RNG.nextFloat(2f, 5f));
        }
    }

    private void updateFloatingTexts(float dt) {
        for (int i = activeFloating - 1; i >= 0; i--) {
            ftLife[i] -= dt; ftY[i] -= 30f * dt;
            if (ftLife[i] <= 0) {
                int last = activeFloating - 1;
                if (i < last) {
                    ftText[i] = ftText[last]; ftX[i] = ftX[last];
                    ftY[i] = ftY[last]; ftLife[i] = ftLife[last];
                    ftMaxLife[i] = ftMaxLife[last]; ftColor[i] = ftColor[last];
                }
                activeFloating--;
            }
        }
    }

    private void spawnParticle(float x, float y, float vx, float vy, float size, int color, float life) {
        if (activeParticles >= MAX_PARTICLES) return;
        int i = activeParticles++;
        ptX[i] = x; ptY[i] = y; ptVX[i] = vx; ptVY[i] = vy;
        ptSize[i] = size; ptColor[i] = color; ptLife[i] = life; ptMaxLife[i] = life;
    }

    public void showFloatingText(String text, float x, float y, int color) {
        if (activeFloating >= MAX_FLOATING) return;
        int i = activeFloating++;
        ftText[i] = text; ftX[i] = x; ftY[i] = y;
        ftColor[i] = color; ftLife[i] = 2f; ftMaxLife[i] = 2f;
    }

    public void spawnExplosion(float x, float y, int color, int count) {
        if (!enableParticles) return;
        for (int i = 0; i < count && activeParticles < MAX_PARTICLES; i++) {
            float angle = RNG.nextFloat(0f, 6.283f);
            float speed = RNG.nextFloat(20f, 80f);
            spawnParticle(x, y, (float) Math.cos(angle) * speed, (float) Math.sin(angle) * speed,
                    RNG.nextFloat(2f, 6f), color, RNG.nextFloat(0.5f, 1.5f));
        }
    }

    private static int lerpColor(int c1, int c2, float t) {
        t = t < 0f ? 0f : (t > 1f ? 1f : t);
        int a1=(c1>>24)&0xFF, r1=(c1>>16)&0xFF, g1=(c1>>8)&0xFF, b1=c1&0xFF;
        int a2=(c2>>24)&0xFF, r2=(c2>>16)&0xFF, g2=(c2>>8)&0xFF, b2=c2&0xFF;
        int a=(int)(a1+(a2-a1)*t), r=(int)(r1+(r2-r1)*t), g=(int)(g1+(g2-g1)*t), b=(int)(b1+(b2-b1)*t);
        return (a<<24)|(r<<16)|(g<<8)|b;
    }

    private int computeSkyColor(float hour) {
        hour = hour % 24f; if (hour < 0f) hour += 24f;
        if (hour < 6f)  { cachedDayT = hour / 6f * 0.5f; return lerpColor(SKY_NIGHT, SKY_DAWN, hour / 6f); }
        if (hour < 12f) { cachedDayT = 0.5f + (hour - 6f) / 6f * 0.5f; return lerpColor(SKY_DAWN, SKY_DAY, (hour - 6f) / 6f); }
        if (hour < 18f) { cachedDayT = 1f - (hour - 12f) / 6f * 0.5f; return lerpColor(SKY_DAY, SKY_DUSK, (hour - 12f) / 6f); }
        cachedDayT = 0.5f - (hour - 18f) / 6f * 0.5f;
        return lerpColor(SKY_DUSK, SKY_NIGHT, (hour - 18f) / 6f);
    }

    private void renderDayNightOverlay(Canvas canvas, int W, int H) {
        int tint = cachedDayT > 0.5f ? SKY_DUSK : SKY_NIGHT;
        int alpha = cachedDayT > 0.5f ? (int) (25 * (1f - cachedDayT) * 2f) : (int) (70 * (1f - cachedDayT * 2f));
        if (alpha <= 0) return;
        dayNightOverlayPaint.setColor(tint);
        dayNightOverlayPaint.setAlpha(Math.max(0, Math.min(90, alpha)));
        canvas.drawRect(0, 0, W, H, dayNightOverlayPaint);
    }

    private int resolveSelectedDiscipleIndex(SectData data) {
        if (selectedDiscipleId == null || data.disciples == null) return -1;
        for (int i = 0, n = data.disciples.size(); i < n; i++) {
            Disciple d = data.disciples.get(i);
            if (d != null && selectedDiscipleId.equals(d.id)) return i;
        }
        selectedDiscipleId = null;
        return -1;
    }

    private void beginFrame(CameraSystem cam, SectData data) {
        this.camera = cam;
        frameZoom = safeZoom(cam);
        buildingCount = data.buildings != null ? Math.min(data.buildings.size(), bldgX.length) : 0;
        discipleCount = data.disciples != null ? data.disciples.size() : 0;
        selectedDisciple = resolveSelectedDiscipleIndex(data);
        bgPaint.setColor(computeSkyColor(data.time != null ? data.time.hour : 12));
    }

    public void render(Canvas canvas, Paint paint, CameraSystem cam) {
        if (canvas == null || cam == null) return;
        SectData data = SectData.getInstance();
        if (data == null) return;
        final int W = canvas.getWidth(), H = canvas.getHeight();
        if (W <= 0 || H <= 0) return;

        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.render(canvas, W, H);
            return;
        }

        beginFrame(cam, data);
        canvas.drawRect(0, 0, W, H, bgPaint);
        renderParallax(canvas, cam, W, H);
        renderTilemap(canvas, cam, W, H);
        renderGrid(canvas, cam, W, H);
        if (enableShadows) renderShadows(canvas, cam, data);
        renderBuildings(canvas, cam, data, W, H);
        renderDisciples(canvas, cam, data, W, H);
        if (enableParticles) renderParticles(canvas, cam);
        renderFloatingTexts(canvas, cam);
        if (enableLighting) renderLighting(canvas, cam, data, W, H);
        renderDayNightOverlay(canvas, W, H);
        renderUI(canvas, data, W, H);
        renderMinimap(canvas, data, W, H);
        if (GameConfig.DEBUG) renderTapDebugMarker(canvas, cam);
    }

    public void renderLayered(Canvas[] layers, Paint paint, CameraSystem cam, int W, int H) {
        if (cam == null) return;
        SectData data = SectData.getInstance();
        if (data == null) return;
        if (layers == null || layers.length < GameView.LAYER_COUNT) return;
        if (W <= 0 || H <= 0) return;

        if (slashMiniGame != null && slashMiniGame.isActive()) {
            if (layers[8] != null) slashMiniGame.render(layers[8], W, H);
            return;
        }

        beginFrame(cam, data);
        if (layers[0] != null) { layers[0].drawRect(0, 0, W, H, bgPaint); renderParallax(layers[0], cam, W, H); }
        if (layers[2] != null) { renderTilemap(layers[2], cam, W, H); renderGrid(layers[2], cam, W, H); }
        if (enableShadows && layers[3] != null) renderShadows(layers[3], cam, data);
        if (layers[4] != null) renderBuildings(layers[4], cam, data, W, H);
        if (layers[5] != null) renderDisciples(layers[5], cam, data, W, H);
        if (enableParticles && layers[6] != null) { renderParticles(layers[6], cam); renderFloatingTexts(layers[6], cam); }
        if (enableLighting && layers[7] != null) renderLighting(layers[7], cam, data, W, H);
        if (layers[8] != null) {
            renderDayNightOverlay(layers[8], W, H);
            renderUI(layers[8], data, W, H);
            renderMinimap(layers[8], data, W, H);
            if (GameConfig.DEBUG) renderTapDebugMarker(layers[8], cam);
        }
    }

    private void renderTapDebugMarker(Canvas canvas, CameraSystem cam) {
        if (tapMarkerLife <= 0f || Float.isNaN(lastTapWorldX)) return;
        float sx = cam.worldToScreenX(lastTapWorldX), sy = cam.worldToScreenY(lastTapWorldY);
        tapDebugPaint.setAlpha((int) (255 * Math.min(1f, tapMarkerLife)));
        canvas.drawCircle(sx, sy, 30f * cam.zoom, tapDebugPaint);
    }

    private void renderParallax(Canvas canvas, CameraSystem cam, int W, int H) {
        if (parallaxBg == null || parallaxBg.isRecycled()) return;
        int pw = parallaxBg.getWidth(), ph = parallaxBg.getHeight();
        if (pw <= 0 || ph <= 0) return;
        float parallaxX = cam.pos.x * 0.1f, parallaxY = cam.pos.y * 0.1f;
        float scale = Math.max((float) W / pw, (float) H / ph) * 1.2f;
        int drawW = Math.max(1, (int) (pw * scale));
        int drawH = Math.max(1, (int) (ph * scale));
        int offsetX = (int) (-parallaxX * 0.5f) % drawW;
        int offsetY = (int) (-parallaxY * 0.5f) % drawH;
        srcRect.set(0, 0, pw, ph);
        for (int x = -drawW + offsetX; x < W + drawW; x += drawW) {
            for (int y = -drawH + offsetY; y < H + drawH; y += drawH) {
                dstRect.set(x, y, x + drawW, y + drawH);
                canvas.drawBitmap(parallaxBg, srcRect, dstRect, tilePaint);
            }
        }
    }

    private void renderTilemap(Canvas canvas, CameraSystem cam, int W, int H) {
        if (!USE_REAL_GROUND_TEXTURE || !tilesetGroundValid) {
            renderTilemapFallback(canvas, cam, W, H);
            return;
        }
        computeTileBounds(cam, W, H);
        for (int tx = tbStartX; tx < tbEndX; tx++) {
            for (int ty = tbStartY; ty < tbEndY; ty++) {
                int tileType = tileMap[tx][ty];
                int var = tileVariation[tx][ty] % tilesetVariationCols;
                float worldX = MAP_ORIGIN_X + tx * TILE_SIZE;
                float worldY = MAP_ORIGIN_Y + ty * TILE_SIZE;
                float sx = cam.worldToScreenX(worldX), sy = cam.worldToScreenY(worldY);
                float size = TILE_SIZE * frameZoom;
                if (sx < -size || sx > W + size || sy < -size || sy > H + size) continue;
                int srcX = var * TILE_SIZE, srcY = tileType * TILE_SIZE;
                srcRect.set(srcX, srcY, srcX + TILE_SIZE, srcY + TILE_SIZE);
                dstRect.set((int) sx, (int) sy, (int) (sx + size), (int) (sy + size));
                canvas.drawBitmap(tileset, srcRect, dstRect, tilePaint);
            }
        }
    }

    private void renderTilemapFallback(Canvas canvas, CameraSystem cam, int W, int H) {
        computeTileBounds(cam, W, H);
        for (int tx = tbStartX; tx < tbEndX; tx++) {
            for (int ty = tbStartY; ty < tbEndY; ty++) {
                int tileType = tileMap[tx][ty];
                float worldX = MAP_ORIGIN_X + tx * TILE_SIZE;
                float worldY = MAP_ORIGIN_Y + ty * TILE_SIZE;
                float sx = cam.worldToScreenX(worldX), sy = cam.worldToScreenY(worldY);
                float size = TILE_SIZE * frameZoom;
                tilePaint.setColor(FALLBACK_TILE_COLORS[tileType % FALLBACK_TILE_COLORS.length]);
                canvas.drawRect(sx, sy, sx + size, sy + size, tilePaint);
            }
        }
    }

    private void renderGrid(Canvas canvas, CameraSystem cam, int W, int H) {
        float l = cam.screenToWorldX(0f), t = cam.screenToWorldY(0f);
        float r = cam.screenToWorldX(W), b = cam.screenToWorldY(H);
        float sx = (float) Math.floor(l / GRID) * GRID, ex = (float) Math.ceil(r / GRID) * GRID;
        float sy = (float) Math.floor(t / GRID) * GRID, ey = (float) Math.ceil(b / GRID) * GRID;
        gridPaint.setAlpha((int) (150 * Math.min(1f, frameZoom * 1.2f)));
        for (float x = sx; x <= ex; x += GRID) canvas.drawLine(cam.worldToScreenX(x), 0, cam.worldToScreenX(x), H, gridPaint);
        for (float y = sy; y <= ey; y += GRID) canvas.drawLine(0, cam.worldToScreenY(y), W, cam.worldToScreenY(y), gridPaint);
    }

    private void renderShadows(Canvas canvas, CameraSystem cam, SectData data) {
        for (int i = 0; i < buildingCount; i++) {
            Building b = data.buildings.get(i);
            if (b == null || !b.isBuilt) continue;
            float px = cam.worldToScreenX(bldgX[i]), py = cam.worldToScreenY(bldgY[i]);
            float size = BLDG_S * frameZoom, half = size * 0.5f, shadowOff = 8f * frameZoom;
            shadowPaint.setAlpha(80);
            r1.set(px - half + shadowOff, py - half + shadowOff, px + half + shadowOff, py + half + shadowOff);
            canvas.drawRoundRect(r1, 8f, 8f, shadowPaint);
        }
        for (int i = 0; i < discipleCount; i++) {
            Disciple d = data.disciples.get(i); if (d == null) continue;
            float px = cam.worldToScreenX(d.position.x), py = cam.worldToScreenY(d.position.y);
            float rad = DISC_R * frameZoom;
            shadowPaint.setAlpha(60);
            r1.set(px - rad * 0.8f, py + rad * 0.3f, px + rad * 0.8f, py + rad * 0.8f);
            canvas.drawOval(r1, shadowPaint);
        }
    }

    private void renderBuildings(Canvas canvas, CameraSystem cam, SectData data, int W, int H) {
        float zoom = frameZoom, size = BLDG_S * zoom, halfSize = size * 0.5f;
        uiTextPaint.setTextSize(14f * zoom); uiTextPaint.setTextAlign(Paint.Align.CENTER); uiTextPaint.setColor(0xFFFFD700);
        for (int i = 0; i < buildingCount; i++) {
            Building b = data.buildings.get(i); if (b == null) continue;
            b.posX = (int) bldgX[i]; b.posY = (int) bldgY[i];
            float px = cam.worldToScreenX(bldgX[i]), py = cam.worldToScreenY(bldgY[i]);
            if (px < -size || px > W + size || py < -size || py > H + size) continue;

            if (b.isBuilt && zoom > 0.4f) {
                float depth = 12f * zoom;
                buildingSidePaint.setAlpha(255);
                r1.set(px - halfSize, py - halfSize, px - halfSize + depth, py + halfSize);
                canvas.drawRect(r1, buildingSidePaint);
                r1.set(px + halfSize - depth, py - halfSize, px + halfSize, py + halfSize);
                canvas.drawRect(r1, buildingSidePaint);
                r1.set(px - halfSize, py - halfSize - depth, px + halfSize, py + halfSize - depth);
                canvas.drawRect(r1, buildingTopPaint);
            }

            r1.set(px - halfSize, py - halfSize, px + halfSize, py + halfSize);

            if (b.isBuilt) {
                buildingPaint.setAlpha(255);
                canvas.drawRoundRect(r1, 10f * zoom, 10f * zoom, buildingPaint);
                canvas.drawRoundRect(r1, 10f * zoom, 10f * zoom, buildingGlowPaint);
                float pulse = 1f + (float) Math.sin(animTime * 2f + i) * 0.05f;
                buildingGlowPaint.setAlpha((int) (50 * pulse));
                r1.inset(-4f * zoom * pulse, -4f * zoom * pulse);
                canvas.drawRoundRect(r1, 12f * zoom, 12f * zoom, buildingGlowPaint);
                buildingGlowPaint.setAlpha(68);
                r1.set(px - halfSize, py - halfSize, px + halfSize, py + halfSize);

                Bitmap icon = getBuildingIcon(b.type);
                if (icon != null && !icon.isRecycled()) {
                    float inset = size * 0.12f;
                    dstRect.set((int) (px - halfSize + inset), (int) (py - halfSize + inset),
                            (int) (px + halfSize - inset), (int) (py + halfSize - inset));
                    canvas.drawBitmap(icon, null, dstRect, atlasIconPaint);
                }
            } else {
                float ghostPulse = 0.5f + (float) Math.sin(animTime * 2.2f + i) * 0.3f;
                buildingGhostPaint.setAlpha((int) (150 + ghostPulse * 90));
                canvas.drawRoundRect(r1, 10f * zoom, 10f * zoom, buildingGhostPaint);
                if (zoom > 0.35f) {
                    buildingGhostTextPaint.setTextSize(11f * zoom);
                    buildingGhostTextPaint.setAlpha((int) (180 + ghostPulse * 75));
                    canvas.drawText("+ Build", px, py + 4f * zoom, buildingGhostTextPaint);
                }
            }

            if (i == selectedBuilding) canvas.drawRoundRect(r1, 10f * zoom, 10f * zoom, buildingBorderPaint);
            canvas.drawText(b.name != null ? b.name : "?", px, py - halfSize - 6f * zoom, uiTextPaint);
            if (zoom > 0.5f && b.isBuilt) {
                uiTextPaint.setTextSize(11f * zoom);
                canvas.drawText("Lv." + b.level, px, py + 4f * zoom, uiTextPaint);
                uiTextPaint.setTextSize(14f * zoom);
            }
        }
    }

    private void renderDisciples(Canvas canvas, CameraSystem cam, SectData data, int W, int H) {
        float zoom = frameZoom, rad = DISC_R * zoom, barW = BAR_W * zoom, barH = BAR_H * zoom;
        namePaint.setTextSize(12f * zoom);
        for (int i = 0; i < discipleCount; i++) {
            Disciple d = data.disciples.get(i); if (d == null) continue;
            float px = cam.worldToScreenX(d.position.x), py = cam.worldToScreenY(d.position.y);
            if (px < -rad * 3f || px > W + rad * 3f || py < -rad * 3f || py > H + rad * 3f) continue;
            if (d.realm >= 5 && !lowEnd) {
                float auraRadius = rad * (1.8f + (float) Math.sin(animTime * 3f + i) * 0.3f);
                auraPaint.setColor(GameConfig.getElementColor(d.element));
                auraPaint.setAlpha((int) (60 + Math.sin(animTime * 2f + i) * 30));
                auraPaint.setStrokeWidth(2f * zoom);
                canvas.drawCircle(px, py, auraRadius, auraPaint);
            }
            if (i == selectedDisciple) {
                float pulse = 1f + (float) Math.sin(animTime * 4f) * 0.15f;
                selectionPaint.setAlpha(200);
                canvas.drawCircle(px, py, rad * pulse + 4f, selectionPaint);
            }
            Bitmap dSprite = getDiscipleSprite(d);
            if (dSprite != null && !dSprite.isRecycled() && zoom > 0.25f) {
                float spriteSize = rad * 2.2f;
                float bob = (float) Math.sin(animTime * 4f + i) * (2f * zoom);
                if (d.facing < 0) {
                    canvas.save();
                    canvas.scale(-1f, 1f, px, py);
                    dstRect.set((int) (px - spriteSize * 0.5f), (int) (py - spriteSize + bob), (int) (px + spriteSize * 0.5f), (int) (py + bob));
                    canvas.drawBitmap(dSprite, null, dstRect, atlasIconPaint);
                    canvas.restore();
                } else {
                    dstRect.set((int) (px - spriteSize * 0.5f), (int) (py - spriteSize + bob), (int) (px + spriteSize * 0.5f), (int) (py + bob));
                    canvas.drawBitmap(dSprite, null, dstRect, atlasIconPaint);
                }
            } else if (walkSheetGridValid && zoom > 0.3f) {
                int frame = d.animFrame % WALK_FRAME_COUNT;
                int srcX = frame * walkFrameW;
                float spriteW = walkFrameW * zoom * 0.5f, spriteH = walkFrameH * zoom * 0.5f;
                srcRect.set(srcX, 0, srcX + walkFrameW, walkFrameH);
                if (d.facing < 0) {
                    canvas.save();
                    canvas.scale(-1f, 1f, px, py);
                    dstRect.set((int) (px - spriteW * 0.5f), (int) (py - spriteH), (int) (px + spriteW * 0.5f), (int) py);
                    canvas.drawBitmap(walkSheet, srcRect, dstRect, tilePaint);
                    canvas.restore();
                } else {
                    dstRect.set((int) (px - spriteW * 0.5f), (int) (py - spriteH), (int) (px + spriteW * 0.5f), (int) py);
                    canvas.drawBitmap(walkSheet, srcRect, dstRect, tilePaint);
                }
            } else {
                disciplePaint.setColor(d.colorTint != 0 ? d.colorTint : GameConfig.getElementColor(d.element));
                canvas.drawCircle(px, py, rad, disciplePaint);
                if (Math.abs(d.facing) > 0) {
                    disciplePaint.setColor(0xFFFF0000);
                    canvas.drawCircle(px + rad * 0.3f * d.facing, py - rad * 0.2f, rad * 0.2f, disciplePaint);
                }
            }
            namePaint.setColor(0xFFFFFFFF);
            canvas.drawText(d.name != null ? d.name : "?", px, py - rad - 8f * zoom, namePaint);
            if (d.currentTask != GameConfig.TASK_NONE && zoom > 0.6f) {
                int taskIdx = GameConfig.clamp(d.currentTask, 0, GameConfig.TASK_COLORS.length - 1);
                disciplePaint.setColor(GameConfig.TASK_COLORS[taskIdx]);
                canvas.drawCircle(px + rad, py - rad, 4f * zoom, disciplePaint);
            }
            float ratio = Math.max(0f, Math.min(1f, d.maxHp > 0 ? d.hp / (float) d.maxHp : 0f));
            r1.set(px - barW * 0.5f, py + rad + 4f * zoom, px + barW * 0.5f, py + rad + 4f * zoom + barH);
            canvas.drawRect(r1, hpBgPaint);
            hpPaint.setColor(ratio < 0.3f ? 0xFFFF5722 : 0xFF00E676);
            r2.set(r1.left, r1.top, r1.left + barW * ratio, r1.bottom);
            canvas.drawRect(r2, hpPaint);
            if (zoom > 0.7f && d.maxMp > 0) {
                float mpRatio = Math.max(0f, Math.min(1f, d.mp / (float) d.maxMp));
                r1.set(px - barW * 0.5f, r1.bottom + 2f, px + barW * 0.5f, r1.bottom + 2f + barH * 0.6f);
                canvas.drawRect(r1, hpBgPaint);
                r2.set(r1.left, r1.top, r1.left + barW * mpRatio, r1.bottom);
                canvas.drawRect(r2, mpPaint);
            }
        }
    }

    private void renderParticles(Canvas canvas, CameraSystem cam) {
        for (int i = 0; i < activeParticles; i++) {
            float px = cam.worldToScreenX(ptX[i]), py = cam.worldToScreenY(ptY[i]);
            float lifeRatio = ptLife[i] / ptMaxLife[i];
            particlePaint.setColor(ptColor[i]);
            particlePaint.setAlpha((int) (255 * lifeRatio));
            canvas.drawCircle(px, py, ptSize[i] * cam.zoom * lifeRatio, particlePaint);
        }
        particlePaint.setAlpha(255);
    }

    private void renderFloatingTexts(Canvas canvas, CameraSystem cam) {
        for (int i = 0; i < activeFloating; i++) {
            float px = cam.worldToScreenX(ftX[i]), py = cam.worldToScreenY(ftY[i]);
            float lifeRatio = ftLife[i] / ftMaxLife[i];
            floatingTextPaint.setColor(ftColor[i]);
            floatingTextPaint.setAlpha((int) (255 * lifeRatio));
            floatingTextPaint.setTextSize(16f * cam.zoom);
            canvas.drawText(ftText[i], px, py, floatingTextPaint);
        }
        floatingTextPaint.setAlpha(255);
    }

    private void renderLighting(Canvas canvas, CameraSystem cam, SectData data, int W, int H) {
        lightPaint.setColor(0xFF0A0A15); lightPaint.setAlpha(120);
        canvas.drawRect(0, 0, W, H, lightPaint);
        lightPaint.setAlpha(255);

        if (lightBakedBitmap == null || lightBakedBitmap.isRecycled()) return;

        float radius = 180f * frameZoom;
        for (int i = 0; i < buildingCount; i++) {
            Building b = data.buildings.get(i);
            if (b == null || !b.isBuilt) continue;
            float px = cam.worldToScreenX(bldgX[i]), py = cam.worldToScreenY(bldgY[i]);
            if (px < -radius || px > W + radius || py < -radius || py > H + radius) continue;
            drawLightGlow(canvas, px, py, radius, getBuildingLightColor(b.type), 100);
        }
        if (selectedDisciple >= 0 && selectedDisciple < discipleCount) {
            Disciple d = data.disciples.get(selectedDisciple);
            if (d != null) {
                float px = cam.worldToScreenX(d.position.x), py = cam.worldToScreenY(d.position.y);
                drawLightGlow(canvas, px, py, 120f * frameZoom, 0xFFFFD700, 150);
            }
        }
    }

    private int getBuildingLightColor(int type) {
        switch (type) {
            case GameConfig.BUILD_ALCHEMY: return 0xFFFF5722;
            case GameConfig.BUILD_FORGE: return 0xFFFFA726;
            case GameConfig.BUILD_SPIRIT_POOL: return 0xFF00E5FF;
            case GameConfig.BUILD_LIBRARY: return 0xFF448AFF;
            case GameConfig.BUILD_GARDEN: return 0xFF66BB6A;
            default: return 0xFFFFD700;
        }
    }

    private void renderUI(Canvas canvas, SectData data, int W, int H) {
        uiPaint.setColor(0xDD1A0F2E);
        canvas.drawRect(0, 0, W, UI_TOP_H, uiPaint);
        canvas.drawRect(0, 0, W, UI_TOP_H, topBarGradientPaint);
        uiPaint.setColor(0xFFFFD700); uiPaint.setStrokeWidth(2f);
        canvas.drawLine(0, UI_TOP_H, W, UI_TOP_H, uiPaint);

        uiTextPaint.setTextAlign(Paint.Align.LEFT); uiTextPaint.setTextSize(15f);

        sb1.setLength(0); sb1.append("SS: ").append(NumberFormatter.format(data.spiritStones, sb2));
        uiTextPaint.setColor(0xFFFFD700);
        canvas.drawText(sb1, 0, sb1.length(), 16, 30, uiTextPaint);

        sb1.setLength(0); sb1.append("Jade: ").append(NumberFormatter.format(data.jade, sb2));
        uiTextPaint.setColor(0xFF00E5FF);
        canvas.drawText(sb1, 0, sb1.length(), 16, 56, uiTextPaint);

        sb1.setLength(0); sb1.append("Essence: ").append(NumberFormatter.format(data.essence, sb2));
        uiTextPaint.setColor(0xFFB388FF);
        canvas.drawText(sb1, 0, sb1.length(), 190, 30, uiTextPaint);

        sb2.setLength(0);
        sb2.append(data.sectName != null ? data.sectName : "Unknown Sect");
        sb2.append(" | ").append(GameConfig.getRealmName(data.sectRealm));
        uiTextPaint.setColor(0xFFFFFFFF);
        canvas.drawText(sb2, 0, sb2.length(), 190, 56, uiTextPaint);

        uiTextPaint.setColor(0xFFAAAAAA); uiTextPaint.setTextAlign(Paint.Align.RIGHT);
        String timeDisplay = data.time != null ? data.time.getDisplay() : "Y1 M1 D1";
        canvas.drawText(timeDisplay, W - 16, 34, uiTextPaint);

        uiPaint.setColor(0xDD1A0F2E);
        canvas.drawRect(0, H - UI_BOT_H, W, H, uiPaint);
        uiPaint.setColor(0xFFFFD700);
        canvas.drawLine(0, H - UI_BOT_H, W, H - UI_BOT_H, uiPaint);

        uiTextPaint.setColor(0xFFFFFFFF); uiTextPaint.setTextSize(13f); uiTextPaint.setTextAlign(Paint.Align.LEFT);
        sb1.setLength(0);
        sb1.append("Disciples: ").append(data.disciples.size());
        sb1.append("  |  Power: ").append(data.sectPower);
        sb1.append("  |  Income: ").append(NumberFormatter.format(data.netProfit, sb2)).append("/day");
        canvas.drawText(sb1, 0, sb1.length(), 16, H - 24, uiTextPaint);

        // Quick Bottom Action Buttons
        renderBottomButton(canvas, W - 430, H - 54, W - 360, H - 8, "⚔️ War", 0xFFFF5252);
        renderBottomButton(canvas, W - 355, H - 54, W - 285, H - 8, "🏆 Arena", 0xFFFFD700);
        renderBottomButton(canvas, W - 280, H - 54, W - 215, H - 8, "💥 Battle", 0xFFE53935);
        renderBottomButton(canvas, W - 210, H - 54, W - 145, H - 8, "👥 Recruit", 0xFF43A047);
        renderBottomButton(canvas, W - 140, H - 54, W - 75, H - 8, "🏛️ Market", 0xFF1E88E5);
        renderBottomButton(canvas, W - 70, H - 54, W - 5, H - 8, "⚙️ Menu", 0xFF8E24AA);

        if (GameConfig.DEBUG) {
            uiTextPaint.setColor(0xFFFFD700); uiTextPaint.setTextSize(11f);
            String[] q = {"LOW", "MED", "HIGH", "ULT"};
            int qIdx = GameConfig.clamp(GameConfig.currentQuality, 0, q.length - 1);
            canvas.drawText("Q:" + q[qIdx], 16, H - 6, uiTextPaint);
        }
    }

    private void renderBottomButton(Canvas canvas, float left, float top, float right, float bottom, String text, int color) {
        r1.set(left, top, right, bottom);
        uiPaint.setColor(0xEE1E1435);
        canvas.drawRoundRect(r1, 6f, 6f, uiPaint);
        uiPaint.setColor(color);
        uiPaint.setStyle(Paint.Style.STROKE);
        uiPaint.setStrokeWidth(1.5f);
        canvas.drawRoundRect(r1, 6f, 6f, uiPaint);
        uiPaint.setStyle(Paint.Style.FILL);

        uiTextPaint.setColor(0xFFFFFFFF);
        uiTextPaint.setTextSize(10f);
        uiTextPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(text, (left + right) * 0.5f, top + 28f, uiTextPaint);
        uiTextPaint.setTextAlign(Paint.Align.LEFT);
    }

    private void renderMinimap(Canvas canvas, SectData data, int W, int H) {
        float mmX = W - MINIMAP_SIZE - 10f, mmY = MINIMAP_Y;
        minimapPaint.setColor(0xDD0A0A15);
        r1.set(mmX, mmY, mmX + MINIMAP_SIZE, mmY + MINIMAP_SIZE);
        canvas.drawRoundRect(r1, 8f, 8f, minimapPaint);
        minimapPaint.setColor(0xFFFFD700);
        minimapPaint.setStyle(Paint.Style.STROKE); minimapPaint.setStrokeWidth(2f);
        canvas.drawRoundRect(r1, 8f, 8f, minimapPaint);
        minimapPaint.setStyle(Paint.Style.FILL);

        float scale = MINIMAP_SIZE / MINIMAP_WORLD_SIZE;
        float worldOffsetX = MAP_CENTER_X - MINIMAP_WORLD_SIZE * 0.5f;
        float worldOffsetY = MAP_CENTER_Y - MINIMAP_WORLD_SIZE * 0.5f;

        if (camera != null) {
            float viewX = mmX + (camera.pos.x - camera.viewportW / camera.zoom / 2f - worldOffsetX) * scale;
            float viewY = mmY + (camera.pos.y - camera.viewportH / camera.zoom / 2f - worldOffsetY) * scale;
            float viewW = (camera.viewportW / camera.zoom) * scale;
            float viewH = (camera.viewportH / camera.zoom) * scale;
            minimapPaint.setColor(0x44FFFFFF);
            r2.set(viewX, viewY, viewX + viewW, viewY + viewH);
            canvas.drawRect(r2, minimapPaint);
        }

        minimapDotPaint.setColor(0xFFFFD700);
        for (int i = 0; i < buildingCount; i++) {
            Building b = data.buildings.get(i);
            if (b == null || !b.isBuilt) continue;
            canvas.drawCircle(mmX + (bldgX[i] - worldOffsetX) * scale, mmY + (bldgY[i] - worldOffsetY) * scale, 2f, minimapDotPaint);
        }
        for (int i = 0; i < discipleCount; i++) {
            Disciple d = data.disciples.get(i); if (d == null) continue;
            minimapDotPaint.setColor(d.colorTint != 0 ? d.colorTint : GameConfig.getElementColor(d.element));
            canvas.drawCircle(mmX + (d.position.x - worldOffsetX) * scale, mmY + (d.position.y - worldOffsetY) * scale, 1.5f, minimapDotPaint);
        }
        minimapDotPaint.setColor(0xFFFFFFFF);
        canvas.drawCircle(mmX + (MAP_CENTER_X - worldOffsetX) * scale, mmY + (MAP_CENTER_Y - worldOffsetY) * scale, 2f, minimapDotPaint);
    }

    public void onTouchDown(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchDown(x, y);
            return;
        }

        SectData data = SectData.getInstance();
        if (data == null || camera == null) {
            Log.d(TAG, "onTouchDown: data or camera NULL, abort.");
            return;
        }

        int W = camera.viewportW > 0 ? camera.viewportW : 720;
        int H = camera.viewportH > 0 ? camera.viewportH : 1280;

        // Check Bottom Action Bar Buttons
        if (y >= H - UI_BOT_H) {
            if (x >= W - 430 && x <= W - 360) {
                if (selectionListener != null) selectionListener.onWarRequested();
                return;
            } else if (x >= W - 355 && x <= W - 285) {
                if (selectionListener != null) selectionListener.onTournamentRequested();
                return;
            } else if (x >= W - 280 && x <= W - 215) {
                if (selectionListener != null) selectionListener.onBattleRequested();
                return;
            } else if (x >= W - 210 && x <= W - 145) {
                if (selectionListener != null) selectionListener.onRecruitRequested();
                return;
            } else if (x >= W - 140 && x <= W - 75) {
                if (selectionListener != null) selectionListener.onMarketRequested();
                return;
            } else if (x >= W - 70 && x <= W - 5) {
                if (selectionListener != null) selectionListener.onMenuRequested();
                return;
            }
        }

        lastTapWorldX = camera.screenToWorldX(x);
        lastTapWorldY = camera.screenToWorldY(y);
        tapMarkerLife = 1f;
        Log.d(TAG, "onTouchDown screen=(" + x + "," + y + ") world=(" + lastTapWorldX + "," + lastTapWorldY + ")");

        for (int i = 0, n = data.disciples.size(); i < n; i++) {
            Disciple d = data.disciples.get(i); if (d == null) continue;
            float px = camera.worldToScreenX(d.position.x), py = camera.worldToScreenY(d.position.y);
            float rad = DISC_R * camera.zoom * 2f;
            if (x >= px - rad && x <= px + rad && y >= py - rad && y <= py + rad) {
                selectedDiscipleId = d.id;
                selectedDisciple = i;
                selectedBuilding = -1;
                spawnExplosion(d.position.x, d.position.y, 0xFFFFD700, 8);
                Log.d(TAG, "Disciple tapped: " + d.name);
                if (selectionListener != null) selectionListener.onDiscipleSelected(d);
                return;
            }
        }
        for (int i = 0, n = Math.min(data.buildings.size(), bldgX.length); i < n; i++) {
            Building b = data.buildings.get(i); if (b == null) continue;
            float px = camera.worldToScreenX(bldgX[i]), py = camera.worldToScreenY(bldgY[i]);
            float size = BLDG_S * camera.zoom;
            if (x >= px - size && x <= px + size && y >= py - size && y <= py + size) {
                selectedBuilding = i;
                selectedDiscipleId = null; selectedDisciple = -1;
                Log.d(TAG, "Building tapped: " + b.name + " isBuilt=" + b.isBuilt);
                if (selectionListener != null) selectionListener.onBuildingTapped(b, b.isBuilt);
                return;
            }
        }
        if (enableParticles) {
            spawnParticle(lastTapWorldX, lastTapWorldY, RNG.nextFloat(-5, 5), RNG.nextFloat(-10, -2), 3f, 0xFFFFFFFF, 1f);
        }
    }

    public void onTouchMove(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchMove(x, y);
        }
    }

    public void onTouchUp(float x, float y) {
        if (slashMiniGame != null && slashMiniGame.isActive()) {
            slashMiniGame.onTouchUp();
        }
    }

    public void setSelectedDisciple(int index) {
        SectData data = SectData.getInstance();
        if (data != null && data.disciples != null && index >= 0 && index < data.disciples.size()) {
            Disciple d = data.disciples.get(index);
            selectedDiscipleId = d != null ? d.id : null;
            selectedDisciple = d != null ? index : -1;
        } else {
            selectedDiscipleId = null;
            selectedDisciple = -1;
        }
    }
    public void setSelectedBuilding(int index) { selectedBuilding = index; }
    public int getSelectedDisciple() { return selectedDisciple; }
    public void resetSelection() {
        selectedDiscipleId = null;
        selectedDisciple = -1;
        selectedBuilding = -1;
    }
    public void spawnLevelUpEffect(float x, float y) {
        showFloatingText("LEVEL UP!", x, y - 30f, 0xFFFFD700);
        spawnExplosion(x, y, 0xFFFFD700, 15);
    }

    public void destroy() {
        if (lightBakedBitmap != null && !lightBakedBitmap.isRecycled()) {
            lightBakedBitmap.recycle();
        }
        lightBakedBitmap = null;
        for (int i = 0; i < buildingIconCache.length; i++) {
            if (buildingIconCache[i] != null && !buildingIconCache[i].isRecycled()) {
                buildingIconCache[i].recycle();
            }
            buildingIconCache[i] = null;
        }
    }
}
