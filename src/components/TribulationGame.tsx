import React, { useState, useEffect, useRef } from 'react';
import { Zap, ChevronLeft, ChevronRight, Award } from 'lucide-react';
import { soundManager } from '../utils/audio';
import { ResolvedPerformanceTier, triggerCelebration } from '../core/sectEngine';
import { ModalShell } from './ModalShell';

interface TribulationGameProps {
  discipleName: string;
  fromRealm: string;
  toRealm: string;
  perfTier?: ResolvedPerformanceTier;
  onSuccess: () => void;
  onCancel: () => void;
}

function normalizeAngle(a: number): number {
  while (a < -Math.PI) a += Math.PI * 2;
  while (a > Math.PI) a -= Math.PI * 2;
  return a;
}

export const TribulationGame: React.FC<TribulationGameProps> = ({
  discipleName,
  fromRealm,
  toRealm,
  perfTier = 'balanced',
  onSuccess,
  onCancel,
}) => {
  const [shieldAngle, setShieldAngle] = useState<number>(0);
  const [strikesDeflected, setStrikesDeflected] = useState<number>(0);
  const [currentStrikeAngle, setCurrentStrikeAngle] = useState<number>(Math.PI * 0.5);
  const [isStriking, setIsStriking] = useState<boolean>(false);
  const [strikeResult, setStrikeResult] = useState<string>('');
  const [isCompleted, setIsCompleted] = useState<boolean>(false);

  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const shieldAngleRef = useRef<number>(0);
  const perfTierRef = useRef<ResolvedPerformanceTier>(perfTier);
  const strikeTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  perfTierRef.current = perfTier;
  const TOTAL_STRIKES = 9;

  const rotateShield = (delta: number) => {
    soundManager.playClick();
    setShieldAngle(prev => {
      const next = prev + delta;
      shieldAngleRef.current = next;
      return next;
    });
  };

  useEffect(() => {
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'ArrowLeft' || e.key === 'a' || e.key === 'A') rotateShield(-0.55);
      if (e.key === 'ArrowRight' || e.key === 'd' || e.key === 'D') rotateShield(0.55);
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  // Trigger lightning strikes periodically (shieldAngleRef avoids timer reset on rotation)
  useEffect(() => {
    if (isCompleted) return;

    const timer = setInterval(() => {
      if (typeof document !== 'undefined' && document.hidden) return;

      const targetAngle = Math.random() * Math.PI * 2;
      setCurrentStrikeAngle(targetAngle);
      setIsStriking(true);

      if (strikeTimeoutRef.current) clearTimeout(strikeTimeoutRef.current);
      strikeTimeoutRef.current = setTimeout(() => {
        const angleDiff = Math.abs(normalizeAngle(shieldAngleRef.current - targetAngle));
        if (angleDiff < 0.75 || angleDiff > Math.PI * 2 - 0.75) {
          soundManager.playLightningZap();
          setStrikesDeflected(prev => {
            const next = prev + 1;
            if (next >= TOTAL_STRIKES) {
              setIsCompleted(true);
              soundManager.playBreakthrough();
              triggerCelebration(perfTierRef.current, { particleCount: 110, spread: 80, origin: { y: 0.6 } });
            }
            return next;
          });
          setStrikeResult('⚡ DEFLECTED! Heavenly lightning dissipated into dantian Qi!');
        } else {
          soundManager.playSwordStrike();
          setStrikeResult('💥 SHIELD MISSED! Qi recoil shook the meridians!');
        }
        setIsStriking(false);
      }, 700);
    }, 2400);

    return () => {
      clearInterval(timer);
      if (strikeTimeoutRef.current) clearTimeout(strikeTimeoutRef.current);
    };
  }, [isCompleted]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const w = canvas.width;
    const h = canvas.height;
    const cx = w * 0.5;
    const cy = h * 0.5;
    const useGlow = perfTier !== 'eco';

    ctx.clearRect(0, 0, w, h);

    const gradient = ctx.createRadialGradient(cx, cy, 20, cx, cy, 140);
    gradient.addColorStop(0, 'rgba(56, 189, 248, 0.15)');
    gradient.addColorStop(1, 'rgba(15, 23, 42, 0)');
    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.arc(cx, cy, 140, 0, Math.PI * 2);
    ctx.fill();

    ctx.beginPath();
    ctx.arc(cx, cy, 80, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(148, 163, 184, 0.25)';
    ctx.lineWidth = 2;
    ctx.setLineDash([4, 6]);
    ctx.stroke();
    ctx.setLineDash([]);

    ctx.beginPath();
    ctx.arc(cx, cy, 18, 0, Math.PI * 2);
    ctx.fillStyle = '#f59e0b';
    if (useGlow) {
      ctx.shadowColor = '#fbbf24';
      ctx.shadowBlur = 16;
    }
    ctx.fill();
    ctx.shadowBlur = 0;

    ctx.fillStyle = '#0f172a';
    ctx.font = '16px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('🧘', cx, cy);

    const shieldR = 80;
    const shieldX = cx + Math.cos(shieldAngle) * shieldR;
    const shieldY = cy + Math.sin(shieldAngle) * shieldR;

    ctx.beginPath();
    ctx.arc(shieldX, shieldY, 20, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(251, 191, 36, 0.35)';
    ctx.strokeStyle = '#f59e0b';
    ctx.lineWidth = 3;
    if (useGlow) {
      ctx.shadowColor = '#f59e0b';
      ctx.shadowBlur = 12;
    }
    ctx.fill();
    ctx.stroke();
    ctx.shadowBlur = 0;

    ctx.fillStyle = '#fef08a';
    ctx.font = '14px sans-serif';
    ctx.fillText('🛡️', shieldX, shieldY);

    if (isStriking) {
      const strikeStartX = cx + Math.cos(currentStrikeAngle) * 160;
      const strikeStartY = cy + Math.sin(currentStrikeAngle) * 160;

      ctx.beginPath();
      ctx.moveTo(strikeStartX, strikeStartY);
      const steps = 5;
      for (let s = 1; s <= steps; s++) {
        const t = s / steps;
        const px = strikeStartX + (cx - strikeStartX) * t + (Math.random() - 0.5) * 20;
        const py = strikeStartY + (cy - strikeStartY) * t + (Math.random() - 0.5) * 20;
        ctx.lineTo(px, py);
      }
      ctx.strokeStyle = '#38bdf8';
      ctx.lineWidth = 4;
      if (useGlow) {
        ctx.shadowColor = '#00e5ff';
        ctx.shadowBlur = 18;
      }
      ctx.stroke();
      ctx.shadowBlur = 0;
    }
  }, [shieldAngle, isStriking, currentStrikeAngle, perfTier]);

  return (
    <ModalShell
      isOpen={true}
      onClose={onCancel}
      maxWidth="lg"
      borderClassName="border-cyan-500/30"
      bodyClassName="p-6 flex flex-col items-center space-y-3 overflow-y-auto max-h-[82vh]"
      icon={
        <div className="p-2 bg-cyan-950/60 border border-cyan-500/40 rounded-xl">
          <Zap className="w-5 h-5 text-cyan-400" />
        </div>
      }
      title={`${discipleName} • Nine Heavens Tribulation`}
      subtitle={`Ascending from ${fromRealm} to ${toRealm}`}
    >
      <div className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 flex items-center justify-between">
        <div className="text-xs text-slate-400">Lightning Deflection:</div>
        <div className="flex items-center gap-1.5 font-mono text-sm font-bold text-amber-400">
          <span>{strikesDeflected}</span> / <span>{TOTAL_STRIKES}</span>
          <div className="flex gap-1 ml-2">
            {Array.from({ length: TOTAL_STRIKES }).map((_, i) => (
              <div
                key={i}
                className={`w-2 h-4 rounded-xs transition-colors ${
                  i < strikesDeflected ? 'bg-amber-400 shadow-xs shadow-amber-400' : 'bg-slate-800'
                }`}
              />
            ))}
          </div>
        </div>
      </div>

      <div className="relative bg-slate-950/80 border border-slate-800 rounded-2xl p-2 w-full flex items-center justify-center">
        <canvas ref={canvasRef} width={340} height={320} className="w-full max-w-[340px] h-[320px]" />

        <div className="absolute bottom-3 left-4 right-4 flex justify-between gap-3">
          <button
            type="button"
            onClick={() => rotateShield(-0.55)}
            className="flex-1 py-2.5 px-4 bg-slate-800/90 hover:bg-slate-700 text-cyan-300 font-bold text-sm rounded-xl border border-slate-700 flex items-center justify-center gap-1.5 active:scale-95 transition"
          >
            <ChevronLeft className="w-4 h-4" /> Shift Left
          </button>
          <button
            type="button"
            onClick={() => rotateShield(0.55)}
            className="flex-1 py-2.5 px-4 bg-slate-800/90 hover:bg-slate-700 text-cyan-300 font-bold text-sm rounded-xl border border-slate-700 flex items-center justify-center gap-1.5 active:scale-95 transition"
          >
            Shift Right <ChevronRight className="w-4 h-4" />
          </button>
        </div>
      </div>

      <div className="min-h-6 flex items-center justify-center text-xs font-semibold text-center text-cyan-300">
        {strikeResult || 'Align the golden Aegis shield (or press ← / →) to intercept heavenly bolts.'}
      </div>

      {isCompleted ? (
        <div className="w-full flex flex-col items-center">
          <div className="text-emerald-400 font-bold text-center mb-3 text-sm flex items-center gap-1">
            <Award className="w-4 h-4" />
            Heavenly Dao Acknowledged! Tribulation Overcome!
          </div>
          <button
            type="button"
            onClick={onSuccess}
            className="w-full py-3 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold rounded-xl shadow-lg transition"
          >
            Finalize Realm Breakthrough
          </button>
        </div>
      ) : (
        <button
          type="button"
          onClick={onCancel}
          className="text-xs text-slate-400 hover:text-slate-200 transition"
        >
          Retreat to meditate & prepare
        </button>
      )}
    </ModalShell>
  );
};
