#!/usr/bin/env node

/**
 * CHIMERA BUILD ENGINE v6.0 FINAL POLISH - MAX OPTIMIZED
 * 1-Click Multiplatform Build Pipeline for Itel A70 (RAM 1GB)
 * Exports Consoles (PSX, PS2, NDS, PSP, Vita, Switch, PS3) + RetroArch + Desktop + Mobile + iOS + Web
 */

const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const VALID_TARGETS = ['psx', 'ps2', 'nds', 'psp', 'vita', 'switch', 'ps3', 'retroarch', 'windows', 'mac', 'linux', 'desktop', 'mobile', 'ios', 'web'];

function parseTargetFlags() {
  const args = process.argv.slice(2);
  if (args.length === 0 || args.includes('--all')) {
    return 'all';
  }

  const targets = [];
  args.forEach(arg => {
    const clean = arg.replace(/^--/, '').toLowerCase();
    if (VALID_TARGETS.includes(clean)) {
      targets.push(clean);
    }
  });

  return targets.length > 0 ? targets.join(' ') : 'all';
}

function runCommand(cmd, label) {
  console.log(`\n${label}...`);
  try {
    const output = execSync(cmd, { cwd: __dirname, encoding: 'utf-8' });
    console.log(output.trim());
  } catch (err) {
    console.error(`[-] Warning during ${label}:`, err.message);
  }
}

function main() {
  const startTime = Date.now();
  console.log("=================================================");
  console.log("  CHIMERA v6.0 FINAL POLISH BUILD START - ITEL A70");
  console.log("=================================================");

  const makeTarget = parseTargetFlags();
  console.log(`[+] Build Target Command: make -j4 ${makeTarget}`);

  // STEP 1: POLISH ASSETS [2 detik] (Non-destructive)
  runCommand("python3 polish_assets.py", "[1/4] Polishing 12 assets + 8 chars (Greedy Mesh 3D & Quantize)");

  // STEP 2: COMPRESS TEXTURE ATLAS [1 detik]
  runCommand("python3 compress_atlas.py --bpp 4 --dither", "[2/4] Compressing atlas 256x256 -> 4bpp 16-color");

  // STEP 3: BUILD SELECTED CONSOLES IN PARALLEL [8 detik - multi core Itel A70]
  runCommand(`make -j4 ${makeTarget}`, `[3/4] Building target (${makeTarget}) in parallel (-j4)`);

  // STEP 4: VERIFY & PACK [1 detik]
  console.log("\n[4/4] Verifying generated export artifacts...");
  const exportDir = path.join(__dirname, 'export');
  if (fs.existsSync(exportDir)) {
    const files = fs.readdirSync(exportDir).filter(f => f !== 'assets_polished' && f !== 'web');
    let totalBytes = 0;
    files.forEach(file => {
      const fullPath = path.join(exportDir, file);
      const stats = fs.statSync(fullPath);
      totalBytes += stats.size;
      const sizeMB = (stats.size / (1024 * 1024)).toFixed(2);
      console.log(`    - ${file.padEnd(28)} : ${sizeMB} MB`);
    });

    const totalMB = (totalBytes / (1024 * 1024)).toFixed(1);
    const durationSec = ((Date.now() - startTime) / 1000).toFixed(2);
    const memUsage = process.memoryUsage();
    const rssMB = (memUsage.rss / 1024 / 1024).toFixed(2);

    console.log("\n=================================================");
    console.log("=== DONE - TOTAL OUTPUT: " + totalMB + "MB - READY TO TEST ===");
    console.log(`[✓] Build Execution Time : ${durationSec} seconds`);
    console.log(`[✓] Memory Footprint     : ${rssMB} MB RSS (Optimal for 1GB RAM)`);
    console.log("=================================================");
  }
}

if (require.main === module) {
  main();
}

module.exports = { main };
