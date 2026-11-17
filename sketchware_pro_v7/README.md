# Sect Idle — Sketchware Pro V7.0.0 Single-Source Export

Proyek ini terintegrasi dengan arsitektur **Single Source of Truth**. Untuk menghindari duplikasi kode dan divergensi antar versi, seluruh source code Java murni (100% Pure Java 7, tanpa lambda/method reference/stream/`var`) berada di satu lokasi utama:

- `../app/src/main/java/` : Source code Java utama (satu sumber untuk Gradle Android & Sketchware Pro v7.0.0).
- `../app/src/main/res/` : Resource XML, drawable, mipmap, layout, dan raw audio.
- `../app/src/main/assets/` : Asset tambahan game termasuk `Data/sect_core_spec.json` yang tersinkronisasi dari `/shared/sect_core_spec.json`.
- `../app/src/main/AndroidManifest.xml` : Manifest Android resmi.

## Cara Ekspor Paket Sketchware Pro Siap Import:
Jalankan dari root repositori:
```bash
python3 src/generator_core.py --target sketchware
```
atau:
```bash
make sketchware
```
Hasilnya akan dibuat di `export/SECT_IDLE_SKETCHWARE.zip` berisi struktur `src/main/...` terbaru tanpa duplikasi file di repositori.

## Kompatibilitas Perangkat:
- Target Minimum: Android 5.0 (API 21)
- RAM Minimum: 1 GB (Teroptimalisasi dengan `GameConfig` + `MobileThermalGovernor` + `BattleCanvasRenderer`)
