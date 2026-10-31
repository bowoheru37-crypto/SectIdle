## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-02-18 - Pre-fetch Chunk Grids and Pre-compute Shaded Color Tables in 3D Isometric Map Engine

**Learning:** In chunk-based 3D isometric rendering loops, querying map chunks via `chunkManager.getOrGenerateChunk(cx, cy)` inside the innermost tile rendering loops causes thousands of hash map lookups and `Long` object allocations per frame. Additionally, calculating face shading on the fly via bit shifts and float multiplies for thousands of tiles degrades frame rates.

**Action:** Pre-fetch visible chunk references into a 2D array grid once per frame before entering depth sorting loops, and pre-compute shaded face color lookup tables during initialization.
