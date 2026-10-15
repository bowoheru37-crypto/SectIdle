#!/usr/bin/env node

/**
 * CHIMERA BUILD ENGINE v6.0 - ENTRY POINT ALIAS
 * Directs to build_v6.js 1-Click Multiplatform Pipeline
 */

const { main } = require('./build_v6.js');

if (require.main === module) {
  main();
}
