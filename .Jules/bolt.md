## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-02-19 - Replace DecimalFormat with Fast Arithmetic String Formatting in UI Loops

**Learning:** `java.text.DecimalFormat` instances synchronize internally on formatting calls and allocate internal formatting buffers on every invocation. When formatting high-frequency UI status displays (such as resource counters, currency, or timers rendered every tick), `DecimalFormat` introduces lock contention across threads and high GC allocation pressure. Custom arithmetic integer/decimal splitting using `Math.round(...)` and `StringBuilder` avoids synchronization locks and memory churn entirely.

**Action:** For performance-critical numeric string formatting in game/UI tick loops, use fast custom integer division and `StringBuilder` appending instead of `DecimalFormat`.
