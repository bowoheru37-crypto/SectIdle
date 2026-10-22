## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-02-18 - Avoid String Allocations in High-Frequency Canvas HUD Drawing Calls

**Learning:** In 60 FPS Canvas rendering routines (such as HUD overlays and action drawers), calling `.toString()` on `StringBuilder` instances or using `+` string concatenation inside `canvas.drawText(...)` instantiates thousands of short-lived `String` objects per minute, triggering GC pauses on low-end devices.

**Action:** Pass `StringBuilder` or `CharSequence` directly to `canvas.drawText(CharSequence text, int start, int end, float x, float y, Paint paint)` and pass reusable `StringBuilder` buffers directly into `NumberFormatter.format(num, sb)` without calling `.toString()`.
