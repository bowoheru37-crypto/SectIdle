## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-05-18 - High-Water Mark Bounding for Particle Systems

**Learning:** Fixed-size particle array pools (e.g. 512 entries) in `ParticleEngineV2` that iterate over all `MAX_PARTICLES` in `update()` and `render()` waste up to 95% of per-frame CPU cycles when active particle counts are low. Furthermore, calling standard `Math.sin()` during particle position updates incurs native conversion overhead on low-end Android hardware.

**Action:** Track active particle high-water mark (`activeCount`) to bound `update()`/`render()` loops and substitute `Math.sin()` with `MathUtils.sin()` lookup table.
