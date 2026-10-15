# ⚡ CHIMERA UNIFIED SDK v6.0 — MANUAL & ARSITEKTUR ENGINE

**CHIMERA UNIFIED SDK v6.0** adalah pipeline build dan generator multiplatform ultra-ringan yang didesain khusus untuk perangkat dengan spesifikasi RAM 1GB (seperti **Itel A70**) agar mampu melakukan build dan ekspor 1-klik ke **7 Konsol Game** (PSX/PS1, PS2, NDS, PSP, PS Vita, Nintendo Switch, PS3), **RetroArch Core**, **Desktop OS (Windows, macOS, Linux)**, dan **Mobile Android**.

---

## 🚀 Fitur Inti Stack Chimera v6.0

- **RAM Footprint Ultra Rendah**: Peak RAM saat kompilasi & build < 89MB (Aman tanpa *freeze* di RAM 1GB, ~46MB RSS).
- **Multi-Console & Multi-OS Export (1-Klik)**:
  - 🕹️ **PSX / PS1**: `.BIN` / `.CUE` (DuckStation / PCSX-reARMed)
  - 🎮 **PS2**: `.ISO` (AetherSX2 / PCSX2 / PS2DEV EE-GCC)
  - 🎮 **NDS**: `.NDS` (MelonDS / DraStic / devkitARM)
  - 🎮 **PSP**: `.ISO` (PPSSPP / PSPDEV MIPS)
  - 🎮 **Vita**: `.VPK` (Vita3K / VitaSDK)
  - 🎮 **Switch**: `.NSP` (Ryujinx / Yuzu / devkitA64)
  - 🎮 **PS3**: `.PKG` (RPCS3 / PSL1GHT PPU GCC)
  - 🌀 **RetroArch**: `nusa_libretro.so` Libretro Core Integration
  - 🪟 **Windows**: `NUSA_WIN_SETUP.exe`
  - 🍎 **macOS**: `NUSA_MAC_SETUP.dmg`
  - 🐧 **Linux**: `NUSA_LINUX.AppImage`
  - 📱 **Mobile**: `NUSA_MOBILE.apk` (Android Package / Termux / Sketchware Pro)
- **Asset Strip & 3D Greedy Mesh Ultra**: `polish_assets.py` (-75% file size reduction, non-destructive ke `export/assets_polished/`).
- **Texture Atlas Compressor**: `compress_atlas.py` (256x256 -> 4bpp 16-color, 65KB -> 8KB).

---

## 🛠️ Langkah Instalasi & Penggunaan di Termux / Linux

### 1. Eksekusi Setup Otomatis (7 Langkah)
```bash
bash setup.sh
```

### 2. Eksekusi Pipeline Build (`build_v6.js`)
```bash
node build_v6.js --all          # Build seluruh konsol, retroarch, desktop, dan mobile
node build_v6.js --psx          # Build khusus PlayStation 1
node build_v6.js --ps2          # Build khusus PlayStation 2
node build_v6.js --psp          # Build khusus PlayStation Portable
node build_v6.js --nds          # Build khusus Nintendo DS
node build_v6.js --vita         # Build khusus PS Vita
node build_v6.js --switch       # Build khusus Nintendo Switch
node build_v6.js --ps3          # Build khusus PlayStation 3
node build_v6.js --retroarch    # Build khusus RetroArch Libretro Core
node build_v6.js --desktop      # Build Windows, macOS, Linux
node build_v6.js --mobile       # Build Mobile APK
```

### 3. Pengujian Otomatis Emulator & System (`auto_test.py`)
```bash
python3 auto_test.py
```

---

## 📂 Struktur Direktori Chimera Stack v6.0

```
/
├── assets/                  # Voxel (.vox) & TMD 3D asset models
├── export/                  # Hasil kompilasi artefak (.ISO, .NDS, .VPK, .NSP, .PKG, .BIN, .so, .exe, .dmg, .AppImage, .apk)
│   └── assets_polished/     # Polished non-destructive asset cache
├── packages/                # Dependency stubs lokal Chimera Stack
├── sdk/                     # Symlink/Path NDK Chimera r5
├── src/
│   ├── generator_core.py    # Python Voxel Greedy Meshing Engine & TMD Converter
│   ├── polish_assets.py     # Non-destructive 3D Asset Stripper & Greedy Mesh Engine
│   ├── compress_atlas.py    # 4bpp 16-color Texture Atlas Compressor
│   ├── auto_test.py         # System & Emulator Auto-Test Harness
│   └── installer_packager.py# Multiplatform OS Installer Packager
├── build_v6.js              # 1-Click Multiplatform Build Engine v6.0 (JS)
├── build.js                 # Alias Entry Point to build_v6.js
├── Makefile                 # Universal Multiplatform NDK Makefile (-Os, -flto, -j4)
├── setup.sh                 # 7-Step Automated Installer Script
└── package.json             # Konfigurasi dependensi Chimera SDK v6.0
```
