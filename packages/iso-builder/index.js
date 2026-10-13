/**
 * CHIMERA ISO BUILDER v6.0
 * Builds ISO-9660 Disc Images with System Descriptors for PS1, PS2, PSP
 */

function createISO9660Image(volIdentifier, systemId, files = [], targetSizeBytes = 2048000) {
  // ISO-9660 Sector size = 2048 bytes
  const SECTOR_SIZE = 2048;
  const numSectors = Math.max(16, Math.ceil(targetSizeBytes / SECTOR_SIZE));
  const buffer = Buffer.alloc(numSectors * SECTOR_SIZE, 0);

  // Sector 16: Primary Volume Descriptor (PVD)
  const pvdOffset = 16 * SECTOR_SIZE;
  buffer[pvdOffset] = 0x01; // Volume Descriptor Type 1 (Primary)
  buffer.write('CD001', pvdOffset + 1, 5, 'ascii'); // ISO Standard Identifier
  buffer[pvdOffset + 6] = 0x01; // Version 1

  buffer.write(systemId.padEnd(32, ' '), pvdOffset + 8, 32, 'ascii');
  buffer.write(volIdentifier.padEnd(32, ' '), pvdOffset + 40, 32, 'ascii');

  // Total Sector Count
  buffer.writeUInt32LE(numSectors, pvdOffset + 80);
  buffer.writeUInt32BE(numSectors, pvdOffset + 84);

  // Logical Block Size = 2048
  buffer.writeUInt16LE(SECTOR_SIZE, pvdOffset + 128);
  buffer.writeUInt16BE(SECTOR_SIZE, pvdOffset + 130);

  return buffer;
}

module.exports = { createISO9660Image };
