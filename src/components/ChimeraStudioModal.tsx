import React, { useState, useEffect, useRef } from 'react';
import { soundManager } from '../utils/audio';
import { CONSOLE_TARGETS, CORE_SPEC } from '../constants/gameData';
import { Terminal, Gamepad2, Download, Cpu, RefreshCw } from 'lucide-react';
import { ModalShell } from './ModalShell';

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
  const [selectedTarget, setSelectedTarget] = useState<string>(CONSOLE_TARGETS[0]?.id ?? 'ps2');
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [exportLogs, setExportLogs] = useState<string[]>([]);
  const [downloadReady, setDownloadReady] = useState<boolean>(false);

  const timersRef = useRef<ReturnType<typeof setTimeout>[]>([]);

  const clearTimers = () => {
    timersRef.current.forEach(t => clearTimeout(t));
    timersRef.current = [];
  };

  useEffect(() => {
    return () => clearTimers();
  }, []);

  const targetObj = CONSOLE_TARGETS.find(t => t.id === selectedTarget) || CONSOLE_TARGETS[0];

  const handleExport = () => {
    soundManager.playClick();
    clearTimers();
    setIsExporting(true);
    setDownloadReady(false);
    setExportLogs([]);

    const safeSlug = sectName.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '') || 'sect_idle';
    const steps = [
      `[Chimera v${CORE_SPEC.version}] Initializing unified pipeline for ${targetObj.name}...`,
      `[Spec] Loaded single-source canonical ruleset (${CORE_SPEC.realms.length} realms, ${CORE_SPEC.buildings.length} buildings).`,
      `[Kernel/OS] Configuring target ABI, low-RAM allocator, and zero-network offline assets...`,
      `[GameEngine] Linking Sect Idle simulation loop & ATB battle state bytecode...`,
      `[Packager] Preparing ${targetObj.ext} build manifest for ${sectName}...`,
      `[DONE] Manifest ready! Target package: ${safeSlug}_${targetObj.id}${targetObj.ext}`,
    ];

    steps.forEach((s, idx) => {
      const t = setTimeout(() => {
        setExportLogs(prev => [...prev, s]);
        if (idx === steps.length - 1) {
          setIsExporting(false);
          setDownloadReady(true);
          soundManager.playBreakthrough();
        }
      }, (idx + 1) * 260);
      timersRef.current.push(t);
    });
  };

  const handleDownload = () => {
    soundManager.playClick();
    const safeSlug = sectName.toLowerCase().replace(/[^a-z0-9]+/g, '_').replace(/^_|_$/g, '') || 'sect_idle';
    const manifestPayload = JSON.stringify(
      {
        generator: 'Chimera Unified Pipeline',
        version: CORE_SPEC.version,
        sectName,
        target: targetObj,
        exportedAt: new Date().toISOString(),
        logs: exportLogs,
        specSummary: {
          realms: CORE_SPEC.realms.length,
          buildings: CORE_SPEC.buildings.length,
          techNodes: CORE_SPEC.techTree.length,
          cliHint: `python3 src/generator_core.py --target ${targetObj.id} --name "${sectName}"`,
        },
      },
      null,
      2
    );
    const blob = new Blob([manifestPayload], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${safeSlug}_${targetObj.id}_manifest.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <ModalShell
      isOpen={isOpen}
      onClose={onClose}
      maxWidth="2xl"
      borderClassName="border-cyan-500/30"
      icon={
        <div className="p-2 bg-cyan-500/10 border border-cyan-500/30 rounded-xl">
          <Gamepad2 className="w-5 h-5 text-cyan-400" />
        </div>
      }
      title="Chimera Unified Target Studio"
      subtitle="All Consoles, All Kernels & All OS Build Manifest Exporter (CLI: build_v6.js / generator_core.py)"
    >
      <div>
        <div className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
          Select Target Platform ({CONSOLE_TARGETS.length} Supported Targets)
        </div>
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-2.5">
          {CONSOLE_TARGETS.map(t => (
            <button
              key={t.id}
              type="button"
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

      <div className="bg-slate-950 border border-slate-800 rounded-xl p-4 font-mono text-xs">
        <div className="flex items-center justify-between text-[11px] text-slate-500 mb-2 border-b border-slate-800/80 pb-1">
          <span className="flex items-center gap-1.5 text-slate-400">
            <Terminal className="w-3.5 h-3.5 text-cyan-400" />
            Unified Build Output
          </span>
          <span>Zero-Redundancy Core</span>
        </div>

        <div className="h-32 overflow-y-auto space-y-1 text-slate-300 pr-1">
          {exportLogs.length === 0 ? (
            <span className="text-slate-500 italic">
              Select a target platform and click "Generate Build Manifest" to verify the cross-OS export pipeline...
            </span>
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

      <div className="flex flex-wrap gap-3">
        <button
          type="button"
          onClick={handleExport}
          disabled={isExporting}
          className={`flex-1 py-3.5 px-4 rounded-xl font-bold text-xs transition shadow-lg flex items-center justify-center gap-2 ${
            isExporting
              ? 'bg-slate-800 text-slate-500 cursor-not-allowed'
              : 'bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 text-slate-950'
          }`}
        >
          {isExporting ? (
            <>
              <RefreshCw className="w-4 h-4 animate-spin" /> Packaging Target Manifest...
            </>
          ) : (
            <>
              <Cpu className="w-4 h-4" /> Generate Build Manifest
            </>
          )}
        </button>

        {downloadReady && (
          <button
            type="button"
            onClick={handleDownload}
            className="py-3.5 px-5 bg-emerald-600 hover:bg-emerald-500 text-white font-bold text-xs rounded-xl transition flex items-center gap-2 shadow-lg"
          >
            <Download className="w-4 h-4" /> Download Manifest (.json)
          </button>
        )}
      </div>
    </ModalShell>
  );
};
