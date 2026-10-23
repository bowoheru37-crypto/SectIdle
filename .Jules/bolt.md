## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-02-18 - Replace DecimalFormat with Zero-Allocation Direct Formatting in Render/HUD Loops

**Learning:** `java.text.DecimalFormat` contains internal synchronization locks and allocates temporary string and digit buffer objects on every `format()` call. When called 60+ times per frame across active HUD headers and floating text elements, this causes severe lock contention and frequent GC pressure on low-spec single-core/low-RAM Android devices.

**Action:** Replace `DecimalFormat` in high-frequency number formatting utilities with primitive arithmetic, direct digit/suffix string building, and `ThreadLocal<StringBuilder>` buffers to achieve lock-free, zero-allocation number formatting.
