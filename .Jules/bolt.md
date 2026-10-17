## 2025-02-18 - Avoid Object Allocation in High-Frequency Canvas Render Passes

**Learning:** In custom 2D Canvas rendering loops (such as particle systems running up to 512 particles at 60 FPS), instantiating `RectF` or other UI helper objects inside `draw` methods (e.g., `canvas.drawOval(new RectF(...), paint)`) causes significant heap object churn and frequent garbage collection (GC) pauses on low-end Android devices.

**Action:** Always pre-allocate reusable `RectF`, `Paint`, or matrix structures as class instance fields and update them via `.set(...)` inside the render pass before issuing Canvas drawing commands.

## 2025-02-18 - Replace Floating-Point Modulo in Trigonometric Lookup Tables with Bitwise Masking

**Learning:** Floating-point modulo operations (`% SIN_TABLE_SIZE` where `SIN_TABLE_SIZE` is 1024) in high-frequency trigonometric lookup functions (`MathUtils.sin`/`cos`) emit floating-point remainder runtime routines (`frem`) and conditional branching logic (`if (idx < 0)`).

**Action:** Because `SIN_TABLE_SIZE` is a power of 2 (1024 = 2^10), cast `(int)(rad * SIN_TABLE_SCALE)` and apply bitwise AND masking `& (SIN_TABLE_SIZE - 1)` (`& 1023`). This maps positive, negative, and wrapped angles to valid table indices in 1 CPU cycle without floating-point division or branches.
