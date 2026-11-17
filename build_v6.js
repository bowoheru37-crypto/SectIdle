#!/usr/bin/env node

/**
 * CHIMERA BUILD ENGINE v6.0 FINAL POLISH - MAX OPTIMIZED (CROSS-PLATFORM ESM)
 * 1-Click Multiplatform Build Pipeline for Itel A70 (RAM 1GB) & Desktop/Console CI
 * Exports Consoles (PSX, PS2, NDS, PSP, Vita, Switch, PS3) + RetroArch + Desktop + Mobile + iOS + Web + Nizam
 */

import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import { fileURLToPath } from 'node:url';
import { execSync, spawnSync } from 'node:child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const VALID_TARGETS = [
  'psx', 'ps2', 'nds', 'psp', 'vita', 'switch', 'ps3',
  'retroarch', 'windows', 'mac', 'linux', 'desktop',
  'mobile', 'ios', 'web', 'nizam', 'sketchware'
];

const ALL_GENERATOR_TARGETS = [
  'psx', 'ps2', 'nds', 'psp', 'vita', 'switch', 'ps3',
  'retroarch', 'windows', 'mac', 'linux', 'mobile', 'ios', 'web'
];

function detectPythonBin() {
  const candidates = process.platform === 'win32'
    ? ['python', 'python3', 'py -3']
    : ['python3', 'python'];
  for (const cmd of candidates) {
    const parts = cmd.split(' ');
    const res = spawnSync(parts[0], [...parts.slice(1), '--version'], {
      stdio: 'ignore',
      shell: process.platform === 'win32'
    });
    if (res.status === 0) return cmd;
  }
  throw new Error('Python 3 executable not found in PATH.');
}

function hasMake() {
  const res = spawnSync('make', ['--version'], { stdio: 'ignore' });
  return res.status === 0;
}

function parseTargetFlags() {
  const args = process.argv.slice(2);
  if (args.length === 0 || args.includes('--all')) {
    return ['all'];
  }

  const targets = [];
  args.forEach(arg => {
    const clean = arg.replace(/^--/, '').toLowerCase();
    if (VALID_TARGETS.includes(clean)) {
      targets.push(clean);
    }
  });

  return targets.length > 0 ? targets : ['all'];
}

function runCommand(cmd, label) {
  console.log(`\n${label}...`);
  try {
    const output = execSync(cmd, { cwd: __dirname, encoding: 'utf-8', stdio: ['ignore', 'pipe', 'pipe'] });
    if (output && output.trim()) {
      console.log(output.trim());
    }
  } catch (err) {
    console.error(`[-] Error during ${label}:`, err.stderr || err.message);
    throw err;
  }
}

function buildNizamBundle(pythonBin) {
  const pyCode = [
    'import zipfile, os',
    "os.makedirs('export', exist_ok=True)",
    "z = zipfile.ZipFile('export/NIZAM_ENGINE_V120.zip', 'w', zipfile.ZIP_DEFLATED)",
    "z.write('NizamEngine/build.zig', 'build.zig')",
    "z.write('NizamEngine/src/main.zig', 'src/main.zig')",
    "z.write('NizamEngine/src/wizard.zig', 'src/wizard.zig')",
    "z.write('NizamEngine/README.md', 'README.md')",
    'z.close()',
    "print('    [✓] Artifact created: NIZAM_ENGINE_V120.zip')"
  ].join('; ');
  runCommand(`${pythonBin} -c "${pyCode}"`, '[3b/4] Packaging Nizam Engine v1.2.0 bundle');
}

function buildTargetsDirectly(pythonBin, requestedTargets) {
  let expanded = [];
  if (requestedTargets.includes('all')) {
    expanded = [...ALL_GENERATOR_TARGETS, 'nizam'];
  } else {
    for (const t of requestedTargets) {
      if (t === 'desktop') {
        expanded.push('windows', 'mac', 'linux');
      } else {
        expanded.push(t);
      }
    }
  }
  expanded = [...new Set(expanded)];

  for (const t of expanded) {
    if (t === 'nizam') {
      buildNizamBundle(pythonBin);
    } else {
      runCommand(`${pythonBin} src/generator_core.py --target ${t}`, `[3/4] Generating ${t.toUpperCase()} target via Chimera Core`);
    }
  }
}

export function main() {
  const startTime = Date.now();
  console.log('=================================================');
  console.log('  CHIMERA v6.0 FINAL POLISH BUILD START - ITEL A70');
  console.log('=================================================');

  const pythonBin = detectPythonBin();
  const requestedTargets = parseTargetFlags();
  const makeTarget = requestedTargets.join(' ');
  const jobs = Math.max(2, Math.min(8, os.cpus().length || 4));

  // STEP 1: POLISH ASSETS (Non-destructive)
  runCommand(`${pythonBin} polish_assets.py`, '[1/4] Polishing 12 assets + 8 chars (Greedy Mesh 3D & Quantize)');

  // STEP 2: COMPRESS TEXTURE ATLAS
  runCommand(`${pythonBin} compress_atlas.py --bpp 4 --dither`, '[2/4] Compressing atlas 256x256 -> 4bpp 16-color');

  // STEP 3: BUILD SELECTED CONSOLES / OS / KERNEL TARGETS
  if (hasMake() && process.platform !== 'win32') {
    console.log(`[+] Build Target Command: make -j${jobs} ${makeTarget}`);
    runCommand(`make -j${jobs} ${makeTarget}`, `[3/4] Building target (${makeTarget}) in parallel (-j${jobs})`);
  } else {
    console.log(`[+] GNU Make unavailable or Windows host detected — using direct Python Chimera Core pipeline`);
    buildTargetsDirectly(pythonBin, requestedTargets);
  }

  // STEP 4: VERIFY & PACK
  console.log('\n[4/4] Verifying generated export artifacts...');
  const exportDir = path.join(__dirname, 'export');
  if (!fs.existsSync(exportDir)) {
    throw new Error('Export directory was not created.');
  }

  const files = fs.readdirSync(exportDir).filter(f => f !== 'assets_polished' && f !== 'web');
  let totalBytes = 0;
  files.forEach(file => {
    const fullPath = path.join(exportDir, file);
    const stats = fs.statSync(fullPath);
    if (stats.isFile()) {
      totalBytes += stats.size;
      const sizeMB = (stats.size / (1024 * 1024)).toFixed(2);
      console.log(`    - ${file.padEnd(28)} : ${sizeMB} MB`);
    }
  });

  const totalMB = (totalBytes / (1024 * 1024)).toFixed(1);
  const durationSec = ((Date.now() - startTime) / 1000).toFixed(2);
  const memUsage = process.memoryUsage();
  const rssMB = (memUsage.rss / 1024 / 1024).toFixed(2);

  console.log('\n=================================================');
  console.log(`=== DONE - TOTAL OUTPUT: ${totalMB}MB - READY TO TEST ===`);
  console.log(`[✓] Build Execution Time : ${durationSec} seconds`);
  console.log(`[✓] Memory Footprint     : ${rssMB} MB RSS (Optimal for 1GB RAM)`);
  console.log('=================================================');
}

const isEntrypoint = process.argv[1] && path.resolve(process.argv[1]) === __filename;
if (isEntrypoint) {
  try {
    main();
  } catch (err) {
    console.error('[!] Chimera Build Pipeline Failed:', err.message);
    process.exit(1);
  }
}

export default { main };
