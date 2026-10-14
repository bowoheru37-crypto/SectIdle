## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.
