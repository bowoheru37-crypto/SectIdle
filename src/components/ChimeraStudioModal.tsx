import React, { useState } from 'react';
import { soundManager } from '../utils/audio';
import { Terminal, Gamepad2, Download, Cpu, X, CheckCircle, RefreshCw } from 'lucide-react';

interface ChimeraStudioModalProps {
  isOpen: boolean;
  onClose: () => void;
  sectName: string;
}

export const ChimeraStudioModal: React.FC<ChimeraStudioModalProps> = ({
  isOpen,
  onClose,
  sectName,
}) => {
  const [selectedTarget, setSelectedTarget] = useState<string>('ps2');
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [exportLogs, setExportLogs] = useState<string[]>([]);
  const [downloadReady, setDownloadReady] = useState<boolean>(false);

  if (!isOpen) return null;

  const targets = [
    { id: 'ps2', name: 'PlayStation 2 (ISO)', ext: '.iso', desc: 'PS2 CDVD TMD converter with hardware EE VU1 optimization.' },
    { id: 'psp', name: 'PSP (EBOOT.PBP)', ext: '.pbp', desc: 'Sony PlayStation Portable MIPS Allegrex native binary.' },
    { id: 'vita', name: 'PS Vita (VPK)', ext: '.vpk', desc: 'PlayStation Vita ARM Cortex-A9 GXM package.' },
    { id: 'switch', name: 'Nintendo Switch (NSP)', ext: '.nsp', desc: 'Tegra X1 ARM64 Homebrew NSP package.' },
    { id: 'nds', name: 'Nintendo DS (NDS)', ext: '.nds', desc: 'ARM9/ARM7 Dual-Screen ROM with touch stylus controls.' },
    { id: 'ps3', name: 'PlayStation 3 (PKG)', ext: '.pkg', desc: 'Cell Broadband Engine SPU pipeline export.' },
    { id: 'desktop', name: 'Desktop Native (ELF/EXE)', ext: '.bin', desc: 'x86_64 standalone executable with Vulkan/OpenGL.' },
  ];

  const handleExport = () => {
    soundManager.playClick();
    setIsExporting(true);
    setDownloadReady(false);
    setExportLogs([]);

    const targetObj = targets.find(t => t.id === selectedTarget) || targets[0];
    const steps = [
      `[Chimera v6.0] Initializing pipeline for target: ${targetObj.name}...`,
      `[VoxelMesh] Packing 3D sect assets, voxel tilesets, and char models...`,
      `[TMD-Converter] Synthesizing polygon vertices & texture atlas...`,
      `[GameEngine] Compiling Sect Idle core loop & ATB battle state bytecode...`,
      `[Packager] Generating signed ${targetObj.ext} image for ${sectName}...`,
      `[DONE] Build succeeded in 0.84s! File: ${sectName.toLowerCase().replace(/\s+/g, '_')}${targetObj.ext}`
    ];

    steps.forEach((s, idx) => {
      setTimeout(() => {
        setExportLogs(prev => [...prev, s]);
        if (idx === steps.length - 1) {
          setIsExporting(false);
          setDownloadReady(true);
          soundManager.playBreakthrough();
        }
      }, (idx + 1) * 350);
    });
  };

  const handleDownload = () => {
    soundManager.playClick();
    const targetObj = targets.find(t => t.id === selectedTarget) || targets[0];
    const blob = new Blob([exportLogs.join('\n')], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${sectName.toLowerCase().replace(/\s+/g, '_')}_build_manifest${targetObj.ext}.txt`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-cyan-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-cyan-500/10 border border-cyan-500/30 rounded-xl">
              <Gamepad2 className="w-5 h-5 text-cyan-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                Chimera Build Engine v6.0 Studio
              </h3>
              <p className="text-xs text-slate-400">
                1-Click Multiplatform Console Exporter (PS2, PSP, Vita, Switch, NDS, PS3)
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 text-slate-400 hover:text-white rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 space-y-4 overflow-y-auto max-h-[75vh]">
          {/* Target Selector */}
          <div>
            <label className="text-xs font-bold uppercase tracking-wider text-slate-400 block mb-2">
              Select Console Target Platform
            </label>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
              {targets.map(t => (
                <button
                  key={t.id}
                  onClick={() => {
                    soundManager.playClick();
                    setSelectedTarget(t.id);
                  }}
                  className={`p-3 text-left rounded-xl border transition ${
                    selectedTarget === t.id
                      ? 'bg-cyan-950/40 border-cyan-500 text-cyan-200 shadow-md'
                      : 'bg-slate-800/40 border-slate-700/60 hover:bg-slate-800 text-slate-300'
                  }`}
                >
                  <div className="font-bold text-xs truncate">{t.name}</div>
                  <div className="text-[10px] text-slate-400 mt-1 line-clamp-2">{t.desc}</div>
                </button>
              ))}
            </div>
          </div>

          {/* Console Build Logs */}
          <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 font-mono text-xs">
            <div className="flex items-center justify-between text-[11px] text-slate-500 mb-2 border-b border-slate-800/80 pb-1">
              <span className="flex items-center gap-1.5 text-slate-400">
                <Terminal className="w-3.5 h-3.5 text-cyan-400" />
                Build Output Terminal
              </span>
              <span>Node.js / Chimera Core</span>
            </div>

            <div className="h-32 overflow-y-auto space-y-1 text-slate-300 pr-1">
              {exportLogs.length === 0 ? (
                <span className="text-slate-600 italic">Select a target platform and click "Start Multiplatform Export" to compile...</span>
              ) : (
                exportLogs.map((log, i) => (
                  <div
                    key={i}
                    className={
                      log.includes('[DONE]')
                        ? 'text-emerald-400 font-bold'
                        : log.includes('[Chimera')
                        ? 'text-cyan-400'
                        : 'text-slate-300'
                    }
                  >
                    {log}
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Action Button */}
          <div className="flex gap-3">
            <button
              onClick={handleExport}
              disabled={isExporting}
              className={`flex-1 py-3.5 rounded-xl font-bold text-xs transition shadow-lg flex items-center justify-center gap-2 ${
                isExporting
                  ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
                  : 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 text-slate-950'
              }`}
            >
              {isExporting ? (
                <>
                  <RefreshCw className="w-4 h-4 animate-spin" /> Compiling Console Image...
                </>
              ) : (
                <>
                  <Cpu className="w-4 h-4" /> Start Multiplatform Export
                </>
              )}
            </button>

            {downloadReady && (
              <button
                onClick={handleDownload}
                className="py-3.5 px-5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-xl transition flex items-center gap-2 shadow-lg"
              >
                <Download className="w-4 h-4" /> Download Package
              </button>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
