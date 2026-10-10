package com.sect.idle.render;

import com.sect.idle.systems.RNG;
import java.util.HashMap;
import java.util.Map;

/**
 * ChunkManager - Open World MMORPG Chunk-Based World Engine for 3D Isometric Map Design.
 * Handles dynamic chunk loading, heightmap terrain generation, biomes, fog of war, and object placement.
 * Pure Java standard implementation without lambdas.
 */
public final class ChunkManager {
    public static final int CHUNK_SIZE = 16; // 16x16 tiles per chunk
    public static final int TILE_SIZE = 64;  // Base tile width in world units

    // Biome Definitions
    public static final int BIOME_SECT_GROUNDS = 0;
    public static final int BIOME_SPIRIT_FOREST = 1;
    public static final int BIOME_MARKET_CITY   = 2;
    public static final int BIOME_SECRET_REALM  = 3;
    public static final int BIOME_RIVAL_SECT    = 4;

    // Tile Type Definitions
    public static final int TILE_VOID         = 0;
    public static final int TILE_GRASS        = 1;
    public static final int TILE_DIRT_PATH    = 2;
    public static final int TILE_STONE_PAVER  = 3;
    public static final int TILE_SPIRIT_WATER = 4;
    public static final int TILE_JADE_FLOOR   = 5;
    public static final int TILE_MOUNTAIN     = 6;
    public static final int TILE_ANCIENT_RUIN = 7;

    public static final class Chunk {
        public final int chunkX, chunkY;
        public final int[][] tiles;
        public final float[][] heights;
        public final boolean[][] fogOfWar;
        public final int biomeType;
        public boolean isExplored;

        public Chunk(int cx, int cy, int biome) {
            this.chunkX = cx;
            this.chunkY = cy;
            this.biomeType = biome;
            this.tiles = new int[CHUNK_SIZE][CHUNK_SIZE];
            this.heights = new float[CHUNK_SIZE][CHUNK_SIZE];
            this.fogOfWar = new boolean[CHUNK_SIZE][CHUNK_SIZE];
            this.isExplored = false;
        }
    }

    private final Map<Long, Chunk> loadedChunks;
    private final int maxCachedChunks = 64; // Memory safe for 1GB RAM

    public ChunkManager() {
        this.loadedChunks = new HashMap<Long, Chunk>();
    }

    public static long getChunkKey(int cx, int cy) {
        return (((long) cx) << 32) | (cy & 0xFFFFFFFFL);
    }

    public int getBiomeForChunk(int cx, int cy) {
        int distFromCenter = (int) Math.sqrt(cx * cx + cy * cy);
        if (distFromCenter <= 1) return BIOME_SECT_GROUNDS;
        if (cx > 1 && cy >= 0) return BIOME_SPIRIT_FOREST;
        if (cx < -1 && cy >= 0) return BIOME_MARKET_CITY;
        if (cy < -1) return BIOME_SECRET_REALM;
        return BIOME_RIVAL_SECT;
    }

    public Chunk getOrGenerateChunk(int cx, int cy) {
        long key = getChunkKey(cx, cy);
        Chunk chunk = loadedChunks.get(key);
        if (chunk == null) {
            if (loadedChunks.size() >= maxCachedChunks) {
                // Remove an old chunk key if cache limit reached
                Long firstKey = loadedChunks.keySet().iterator().next();
                loadedChunks.remove(firstKey);
            }
            int biome = getBiomeForChunk(cx, cy);
            chunk = generateChunk(cx, cy, biome);
            loadedChunks.put(key, chunk);
        }
        return chunk;
    }

    private Chunk generateChunk(int cx, int cy, int biome) {
        Chunk chunk = new Chunk(cx, cy, biome);
        long seed = (cx * 73856093L) ^ (cy * 19349663L);

        for (int tx = 0; tx < CHUNK_SIZE; tx++) {
            for (int ty = 0; ty < CHUNK_SIZE; ty++) {
                int worldTileX = cx * CHUNK_SIZE + tx;
                int worldTileY = cy * CHUNK_SIZE + ty;

                // Procedural elevation height map using sine combination
                float height = (float) (Math.sin(worldTileX * 0.15) * Math.cos(worldTileY * 0.15) * 24.0
                        + Math.sin(worldTileX * 0.05 + worldTileY * 0.05) * 40.0);
                if (height < 0f) height = 0f;
                chunk.heights[tx][ty] = height;

                // Biome-specific tile selection logic
                int tileType = TILE_GRASS;
                switch (biome) {
                    case BIOME_SECT_GROUNDS:
                        if ((worldTileX % 4 == 0) || (worldTileY % 4 == 0)) {
                            tileType = TILE_STONE_PAVER;
                        } else if (Math.abs(worldTileX) <= 2 && Math.abs(worldTileY) <= 2) {
                            tileType = TILE_JADE_FLOOR;
                        } else {
                            tileType = TILE_GRASS;
                        }
                        break;
                    case BIOME_SPIRIT_FOREST:
                        if (height > 35f) {
                            tileType = TILE_MOUNTAIN;
                        } else if (height < 5f) {
                            tileType = TILE_SPIRIT_WATER;
                        } else if ((tx + ty) % 5 == 0) {
                            tileType = TILE_DIRT_PATH;
                        } else {
                            tileType = TILE_GRASS;
                        }
                        break;
                    case BIOME_MARKET_CITY:
                        if ((tx + ty) % 2 == 0) {
                            tileType = TILE_STONE_PAVER;
                        } else {
                            tileType = TILE_DIRT_PATH;
                        }
                        break;
                    case BIOME_SECRET_REALM:
                        if (height > 45f) {
                            tileType = TILE_ANCIENT_RUIN;
                        } else if (height < 8f) {
                            tileType = TILE_SPIRIT_WATER;
                        } else {
                            tileType = TILE_JADE_FLOOR;
                        }
                        break;
                    case BIOME_RIVAL_SECT:
                        if (height > 30f) {
                            tileType = TILE_MOUNTAIN;
                        } else {
                            tileType = TILE_DIRT_PATH;
                        }
                        break;
                }
                chunk.tiles[tx][ty] = tileType;
                chunk.fogOfWar[tx][ty] = false; // Default visible for initial area
            }
        }
        return chunk;
    }

    public void revealArea(float worldX, float worldY, float radius) {
        int centerTileX = (int) (worldX / TILE_SIZE);
        int centerTileY = (int) (worldY / TILE_SIZE);
        int radiusTiles = (int) (radius / TILE_SIZE);

        for (int dx = -radiusTiles; dx <= radiusTiles; dx++) {
            for (int dy = -radiusTiles; dy <= radiusTiles; dy++) {
                if (dx * dx + dy * dy <= radiusTiles * radiusTiles) {
                    int tx = centerTileX + dx;
                    int ty = centerTileY + dy;
                    int cx = (tx >= 0) ? (tx / CHUNK_SIZE) : ((tx - CHUNK_SIZE + 1) / CHUNK_SIZE);
                    int cy = (ty >= 0) ? (ty / CHUNK_SIZE) : ((ty - CHUNK_SIZE + 1) / CHUNK_SIZE);

                    Chunk chunk = getOrGenerateChunk(cx, cy);
                    chunk.isExplored = true;
                    int localX = tx - cx * CHUNK_SIZE;
                    int localY = ty - cy * CHUNK_SIZE;
                    if (localX >= 0 && localX < CHUNK_SIZE && localY >= 0 && localY < CHUNK_SIZE) {
                        chunk.fogOfWar[localX][localY] = true;
                    }
                }
            }
        }
    }
}
