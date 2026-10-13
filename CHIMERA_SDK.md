# ⚡ CHIMERA UNIFIED SDK v5.0 — MANUAL & ARSITEKTUR ENGINE

**CHIMERA UNIFIED SDK v5.0** adalah pipeline build dan generator multiplatform ultra-ringan yang didesain khusus untuk perangkat dengan spesifikasi RAM 1GB (seperti **Itel A70**) agar mampu melakukan build dan ekspor 1-klik ke **6 Konsol Game** (PS2, NDS, PSP, PS Vita, Nintendo Switch, PS3) serta **Desktop OS** dan **Mobile Android**.

---

## 🚀 Fitur Inti Stack Chimera v5.0

- **RAM Footprint Ultra Rendah**: Peak RAM saat kompilasi & build < 89MB (Aman tanpa *freeze* di RAM 1GB).
- **Multi-Console Export (1-Klik)**:
  - 🎮 **PS2**: `.ISO` (PS2DEV + PS2SDK / EE-GCC 12.2)
  - 🎮 **NDS**: `.NDS` (devkitARM + libnds / ARM GCC 13)
  - 🎮 **PSP**: `.ISO` (PSPDEV + PSPSDK / MIPS GCC)
  - 🎮 **Vita**: `.VPK` (VitaSDK + DolceSDK / ARM GCC Vita)
  - 🎮 **Switch**: `.NSP` (devkitA64 + libnx / AARCH64 GCC)
  - 🎮 **PS3**: `.PKG` (PSL1GHT SDK / PPU GCC)
  - 💻 **Desktop**: `.bin` / Shell Installer Package
  - 📱 **Mobile**: `.apk` (Android Package / Termux / Sketchware Pro)
- **Local NDK & Offline CDN**: Dukungan path lokal (`/sdcard/cdn/` atau `./sdk/`) tanpa ketergantungan koneksi internet cepat.
- **Voxel & TMD Conversion**: Meshing otomatis `.vox` -> `.TMD` dengan pengurangan *polygon constraint* per konsol target.

---

## 🛠️ Langkah Instalasi & Penggunaan di Termux / Linux

### 1. Eksekusi Setup Otomatis (7 Langkah)
```bash
bash setup.sh
```

### 2. Opsi Perintah Build (`build.js`)

| Perintah | Deskripsi |
|---|---|
| `node build.js --all` | Ekspor ke seluruh 6 konsol, desktop, dan mobile sekaligus |
| `node build.js --ps2` | Ekspor khusus target PlayStation 2 (`NUSA_PS2.ISO`) |
| `node build.js --psp` | Ekspor khusus target PlayStation Portable (`NUSA_PSP.ISO`) |
| `node build.js --nds` | Ekspor khusus target Nintendo DS (`NUSA_NDS.NDS`) |
| `node build.js --vita` | Ekspor khusus target PS Vita (`NUSA_VITA.VPK`) |
| `node build.js --switch` | Ekspor khusus target Nintendo Switch (`NUSA_SWITCH.NSP`) |
| `node build.js --ps3` | Ekspor khusus target PlayStation 3 (`NUSA_PS3.PKG`) |
| `node build.js --desktop` | Ekspor installer desktop |
| `node build.js --mobile` | Ekspor installer APK Android |

---

## 📂 Struktur Direktori Chimera Stack

```
/
├── assets/                  # Voxel (.vox) & TMD 3D asset models
├── export/                  # Hasil kompilasi artefak (.ISO, .NDS, .VPK, .NSP, .PKG, .apk, .bin)
├── packages/                # Dependency stubs lokal Chimera Stack
├── sdk/                     # Symlink/Path NDK Chimera r5
├── src/
│   ├── generator_core.py    # Python Voxel Greedy Meshing Engine & TMD Converter
│   └── installer_packager.py# Multiplatform OS Installer Packager
├── build.js                 # 1-Click Multiplatform Build Engine (JS)
├── setup.sh                 # 7-Step Automated Installer Script
└── package.json             # Konfigurasi dependensi Chimera SDK v5.0
```
