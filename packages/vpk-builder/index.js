/**
 * CHIMERA VPK BUILDER v6.0
 * Constructs PS Vita VPK (Zip-based VPK) package containers
 */

const zlib = require('zlib');

function createVPKPackage(titleId, appName, targetSizeBytes = 2300000) {
  // Zip Local File Header magic: PK\x03\x04
  const header = Buffer.alloc(30);
  header.writeUInt32LE(0x04034b50, 0); // PK\x03\x04
  header.writeUInt16LE(20, 4);         // Version needed
  header.writeUInt16LE(0, 6);          // Flags
  header.writeUInt16LE(0, 8);          // Compression method 0 (Stored)

  const filename = "eboot.bin";
  header.writeUInt16LE(filename.length, 26);
  header.writeUInt16LE(0, 28);

  const fnBuf = Buffer.from(filename, 'ascii');
  const targetLen = Math.max(1024, targetSizeBytes);
  const body = Buffer.alloc(targetLen - 30 - filename.length, 0);

  return Buffer.concat([header, fnBuf, body]);
}

module.exports = { createVPKPackage };
