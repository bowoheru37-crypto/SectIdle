## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-05-18 - Replace DecimalFormat with Zero-Allocation Math String Builders

**Learning:** `java.text.DecimalFormat` is slow, thread-unsafe, and creates new `String` and `StringBuffer` objects on every `.format()` call. In high-frequency 60 FPS HUD rendering loops (e.g. `HudManager`), formatting spirit stone values and resource totals using `DecimalFormat` causes frequent GC pauses on low-spec target devices (such as 1GB RAM Itel A70).

**Action:** Implement fast custom integer/double math formatting routines directly writing into reusable `StringBuilder` buffers (`appendFormattedInt`, `append1Dec`, `append2Dec`) to eliminate `DecimalFormat` overhead and heap allocations during frame updates.
