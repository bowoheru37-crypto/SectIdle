# ==============================================================================
# CHIMERA BUILD ENGINE v6.0 - UNIVERSAL MULTIPLATFORM MAKEFILE (MAX OPTIMIZED)
# Targets: PSX (PS1), PS2, NDS, PSP, Vita, Switch, PS3, RetroArch, Windows, Mac, Linux, Android, iOS, Web
# ==============================================================================

EXPORT_DIR = export

$(shell mkdir -p $(EXPORT_DIR))

.PHONY: all psx ps2 nds psp vita switch ps3 retroarch windows mac linux mobile ios web desktop nizam clean

all: psx ps2 nds psp vita switch ps3 retroarch windows mac linux mobile ios web desktop nizam
	@echo "================================================="
	@echo " CHIMERA v6.0 ALL-PLATFORM BUILD COMPLETE "
	@echo " SUPPORTED: PSX, PS2, NDS, PSP, VITA, SWITCH, PS3 "
	@echo " RETROARCH, WINDOWS, MAC, LINUX, ANDROID, IOS, WEB, NIZAM "
	@echo "================================================="

nizam:
	@echo "[>] Compiling Nizam Engine Bundle (NIZAM_ENGINE_V120.zip)..."
	@python3 -c "import zipfile, os; os.makedirs('$(EXPORT_DIR)', exist_ok=True); z=zipfile.ZipFile('$(EXPORT_DIR)/NIZAM_ENGINE_V120.zip', 'w'); z.write('NizamEngine/build.zig', 'build.zig'); z.write('NizamEngine/src/main.zig', 'src/main.zig'); z.write('NizamEngine/src/wizard.zig', 'src/wizard.zig'); z.write('NizamEngine/README.md', 'README.md'); z.close()"

psx:
	@echo "[>] Compiling PSX / PS1 Target (NUSA_PSX.BIN)..."
	@python3 src/generator_core.py --target psx

ps2:
	@echo "[>] Compiling PS2 Target (NUSA_PS2.ISO)..."
	@python3 src/generator_core.py --target ps2

nds:
	@echo "[>] Compiling NDS Target (NUSA_NDS.NDS)..."
	@python3 src/generator_core.py --target nds

psp:
	@echo "[>] Compiling PSP Target (NUSA_PSP.ISO)..."
	@python3 src/generator_core.py --target psp

vita:
	@echo "[>] Compiling Vita Target (NUSA_VITA.VPK)..."
	@python3 src/generator_core.py --target vita

switch:
	@echo "[>] Compiling Switch Target (NUSA_SWITCH.NSP)..."
	@python3 src/generator_core.py --target switch

ps3:
	@echo "[>] Compiling PS3 Target (NUSA_PS3.PKG)..."
	@python3 src/generator_core.py --target ps3

retroarch:
	@echo "[>] Compiling RetroArch Libretro Core Target (NUSA_RETROARCH.so)..."
	@python3 src/generator_core.py --target retroarch

windows:
	@echo "[>] Compiling Windows Target (NUSA_WINDOWS.exe)..."
	@python3 src/generator_core.py --target windows

mac:
	@echo "[>] Compiling macOS Target (NUSA_MAC.dmg)..."
	@python3 src/generator_core.py --target mac

linux:
	@echo "[>] Compiling Linux Target (NUSA_LINUX.AppImage)..."
	@python3 src/generator_core.py --target linux

mobile:
	@echo "[>] Compiling Mobile Target (NUSA_MOBILE.apk)..."
	@python3 src/generator_core.py --target mobile

ios:
	@echo "[>] Compiling iOS Target (NUSA_IOS_PACKAGE.ipa)..."
	@python3 src/generator_core.py --target ios

web:
	@echo "[>] Compiling Web Game Target (NUSA_WEB_GAME.zip)..."
	@python3 src/generator_core.py --target web

desktop: windows mac linux

clean:
	@rm -rf $(EXPORT_DIR)/*
	@echo "[✓] Cleaned build outputs."
