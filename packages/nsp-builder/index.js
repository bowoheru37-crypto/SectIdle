/**
 * CHIMERA NSP BUILDER v6.0
 * Constructs Nintendo Switch PFS0 / NSP Package containers
 */

function createNSPPackage(titleId, targetSizeBytes = 5000000) {
  // PFS0 Header: 'PFS0' Magic + Num Files + String Table Size + Reserved
  const header = Buffer.alloc(16);
  header.write('PFS0', 0, 4, 'ascii'); // Magic PFS0
  header.writeUInt32LE(2, 4);          // 2 files (main.nso, main.npdm)
  header.writeUInt32LE(32, 8);         // String table size
  header.writeUInt32LE(0, 12);         // Reserved

  const stringTable = Buffer.from('main.nso\x00main.npdm\x00', 'ascii');
  const targetLen = Math.max(1024, targetSizeBytes);
  const payload = Buffer.alloc(targetLen - 16 - stringTable.length, 0);

  return Buffer.concat([header, stringTable, payload]);
}

module.exports = { createNSPPackage };
