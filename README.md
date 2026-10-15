# 📚 DOKUMENTASI LENGKAP & SPESIFIKASI SYSTEM — SECT IDLE & CHIMERA SDK v6.0

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://github.com/bowoheru37-crypto/SectIdle/actions/workflows/build-release.yml/badge.svg)](https.github.com/bowoheru37-crypto/SectIdle/actions)

## 📌 RINGKASAN EKSEKUTIF

**Sect Idle (Nusa Legends)** adalah permainan simulasi bisnis & idle RPG berbasis Xianxia yang terintegrasi dengan **Chimera Build Engine v6.0**, **Game Visual UI Designer**, dan pipeline build multiplatform ultra-ringan.

---

## ⚡ TEKNOLOGI & CHIMERA BUILD ENGINE v6.0

- **Target Spesifikasi Rendah**: Ditargetkan untuk HP Entry-Class (RAM 1GB, contoh **Itel A70**) dengan *peak RAM footprint* < 89MB (~46MB RSS).
- **Sketchware Pro v7.0.0 Compatible**: Source code Java murni (100% tanpa lambda, stream API, atau method reference) di direktori `sketchware_pro_v7/` dan `app/`.
- **Game Visual UI Designer**: Drag-and-drop canvas layout editor (`GameVisualUiDesigner.java`) untuk merancang HUD HP bar, tombol skill, frame dialog, dan panel status.
- **1-Click Multiplatform Export**:
  - 🕹️ **PS1 / PSX**: `NUSA_PSX.BIN` & `NUSA_PSX.CUE`
  - 🎮 **PS2**: `NUSA_PS2.ISO`
  - 🎮 **NDS**: `NUSA_NDS.NDS`
  - 🎮 **PSP**: `NUSA_PSP.ISO`
  - 🎮 **Vita**: `NUSA_VITA.VPK`
  - 🎮 **Switch**: `NUSA_SWITCH.NSP`
  - 🎮 **PS3**: `NUSA_PS3.PKG`
  - 🌀 **RetroArch**: `NUSA_RETROARCH.so` Libretro Core
  - 🪟 **Windows**: `NUSA_WINDOWS.exe`
  - 🍎 **macOS**: `NUSA_MAC.dmg`
  - 🐧 **Linux**: `NUSA_LINUX.AppImage`
  - 📱 **Mobile**: `NUSA_MOBILE.apk`

---

## 🛠️ CARA BUILD & EKSEKUSI PIPELINE

### 1. Build Multiplatform (1-Click)
```bash
bash setup.sh          # Setup otomatis 7-langkah
node build_v6.js --all  # Build seluruh konsol & multiplatform dalam 12s
python3 auto_test.py   # Uji integritas header & simulated boot sequence
```

### 2. Build Mobile Android Release APK
```bash
./gradlew assembleRelease
```

---

## ⚖️ LISENSI & LEGAL

Proyek ini terlisensi di bawah **[MIT License](LICENSE)**.
Detail kebijakan privasi dan ketentuan layanan dapat dibaca di:
- 🔒 [Privacy Policy](PRIVACY_POLICY.md)
- 📜 [Terms of Service](TERMS_OF_SERVICE.md)
- 🤝 [Contributing Guidelines](CONTRIBUTING.md)
- 🛡️ [Security Policy](SECURITY.md)
