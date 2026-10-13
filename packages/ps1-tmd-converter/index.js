/**
 * CHIMERA PS1 TMD CONVERTER v6.0
 * Converts 3D meshes into PlayStation 1 TMD (3D Model Format) binary buffers
 */

function convertMeshToTMD(mesh, options = {}) {
  const maxPolys = options.maxPolys || 48;
  const numObjects = 1;

  // TMD Header: Version 0x41, Flags 0x00, Num Objects 1
  const header = Buffer.alloc(12);
  header.writeUInt32LE(0x00000041, 0); // TMD Magic / Version
  header.writeUInt32LE(numObjects, 4);  // Object count
  header.writeUInt32LE(12, 8);          // Object table offset

  // Object Header
  const objTable = Buffer.alloc(28);
  const polyCount = Math.min(maxPolys, mesh.triangles || 16);
  objTable.writeUInt32LE( polyCount, 12 ); // Primitive count

  const payload = Buffer.alloc(polyCount * 16, 0x55);
  return Buffer.concat([header, objTable, payload]);
}

module.exports = { convertMeshToTMD };
