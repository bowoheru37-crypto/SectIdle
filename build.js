#!/usr/bin/env node

/**
 * CHIMERA BUILD ENGINE v6.0 - ENTRY POINT ALIAS (ESM)
 * Directs to build_v6.js 1-Click Multiplatform Pipeline
 */

import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { main } from './build_v6.js';

const __filename = fileURLToPath(import.meta.url);
const isEntrypoint = process.argv[1] && path.resolve(process.argv[1]) === __filename;

if (isEntrypoint) {
  try {
    main();
  } catch (err) {
    console.error('[!] Chimera Build Pipeline Failed:', err.message);
    process.exit(1);
  }
}
