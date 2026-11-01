# NIZAM ENGINE v1.2.0 - Indie Edition (Wizard Mode)

*Tagline: Satu Klik, Jadi Game. Dari Itel A70 sampai PS5.*

## Fitur Inti
1. **Lapis 0: Kernel** - Arena memory allocator, anti-leak, anti-GC OOM, thermal throttling auto-limit 150MB.
2. **Lapis 1: Render** - 1 Shader PBR Auto-LOD, Sprite & Mesh batching, Batik formation particle system.
3. **Lapis 2: Physics & Logic** - NIZAM Physics Lite (AABB + Circle) & cache-friendly ECS.
4. **Lapis 3: Content Pipeline** - Atlas WWE & Jade Dragon sprite cut & collision mapping.
5. **Lapis 4: Wizard Mode** - 4-Step Interactive Studio (Project Setup, Character Preview, Platform Selection, Build All).

## Cara Build
```bash
# Di PC / Laptop (dengan Zig 0.13.0+)
cd NizamEngine
zig build run

# Target Android (APK ~3.5MB untuk Itel A70):
zig build -Dtarget=aarch64-linux-android

# Target WebGL / WASM:
zig build -Dtarget=wasm32-emscripten
```
