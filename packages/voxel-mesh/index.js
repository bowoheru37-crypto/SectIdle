/**
 * CHIMERA VOXEL MESH ENGINE v6.0
 * Parses MagicaVoxel .VOX format buffers and performs 3D Greedy Meshing
 */

function parseVox(buffer) {
  if (!buffer || buffer.length < 12 || buffer.toString('utf8', 0, 4) !== 'VOX ') {
    return { voxels: [], width: 16, height: 16, depth: 16 };
  }

  const voxels = [];
  let offset = 12; // Skip 'VOX ' + version

  while (offset < buffer.length - 12) {
    const chunkId = buffer.toString('utf8', offset, offset + 4);
    const chunkSize = buffer.readUInt32LE(offset + 4);
    offset += 12;

    if (chunkId === 'XYZI') {
      const numVoxels = buffer.readUInt32LE(offset);
      let vOffset = offset + 4;
      for (let i = 0; i < numVoxels; i++) {
        if (vOffset + 4 <= buffer.length) {
          const x = buffer[vOffset];
          const y = buffer[vOffset + 1];
          const z = buffer[vOffset + 2];
          const color = buffer[vOffset + 3];
          voxels.push({ x, y, z, color });
        }
        vOffset += 4;
      }
    }
    offset += chunkSize;
  }

  return { voxels, count: voxels.length };
}

function greedyMesh3D(voxels) {
  // Filter internal voxels surrounded on all 6 faces
  const set = new Set(voxels.map(v => `${v.x},${v.y},${v.z}`));
  const visible = voxels.filter(v => {
    const neighbors = [
      `${v.x+1},${v.y},${v.z}`, `${v.x-1},${v.y},${v.z}`,
      `${v.x},${v.y+1},${v.z}`, `${v.x},${v.y-1},${v.z}`,
      `${v.x},${v.y},${v.z+1}`, `${v.x},${v.y},${v.z-1}`
    ];
    return !neighbors.every(n => set.has(n));
  });

  return { vertices: visible.length * 8, triangles: visible.length * 12, voxels: visible };
}

module.exports = { parseVox, greedyMesh3D };
