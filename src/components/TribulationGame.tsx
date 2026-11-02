import React, { useState, useEffect, useRef } from 'react';
import { Shield, Zap, ChevronLeft, ChevronRight, Award } from 'lucide-react';
import { soundManager } from '../utils/audio';
import confetti from 'canvas-confetti';

interface TribulationGameProps {
  discipleName: string;
  fromRealm: string;
  toRealm: string;
  onSuccess: () => void;
  onCancel: () => void;
}

export const TribulationGame: React.FC<TribulationGameProps> = ({
  discipleName,
  fromRealm,
  toRealm,
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

  const TOTAL_STRIKES = 9;

  // Trigger lightning strikes periodically
  useEffect(() => {
    if (isCompleted) return;

    const timer = setInterval(() => {
      // Pick random lightning descent angle (between 0 and 2*PI)
      const targetAngle = Math.random() * Math.PI * 2;
      setCurrentStrikeAngle(targetAngle);
      setIsStriking(true);

      setTimeout(() => {
        // Evaluate deflection
        const angleDiff = Math.abs(normalizeAngle(shieldAngle - targetAngle));
        // Shield covers approx +/- 0.65 radians
        if (angleDiff < 0.75 || angleDiff > Math.PI * 2 - 0.75) {
          soundManager.playLightningZap();
          setStrikesDeflected(prev => {
            const next = prev + 1;
            if (next >= TOTAL_STRIKES) {
              setIsCompleted(true);
              soundManager.playBreakthrough();
              confetti({ particleCount: 120, spread: 80, origin: { y: 0.6 } });
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

    return () => clearInterval(timer);
  }, [shieldAngle, isCompleted]);

  const normalizeAngle = (a: number) => {
    while (a < -Math.PI) a += Math.PI * 2;
    while (a > Math.PI) a -= Math.PI * 2;
    return a;
  };

  const rotateShield = (delta: number) => {
    soundManager.playClick();
    setShieldAngle(prev => prev + delta);
  };

  // Draw tribulation scene
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const w = canvas.width;
    const h = canvas.height;
    const cx = w * 0.5;
    const cy = h * 0.5;

    ctx.clearRect(0, 0, w, h);

    // Cosmic background aura
    const gradient = ctx.createRadialGradient(cx, cy, 20, cx, cy, 140);
    gradient.addColorStop(0, 'rgba(56, 189, 248, 0.15)');
    gradient.addColorStop(1, 'rgba(15, 23, 42, 0)');
    ctx.fillStyle = gradient;
    ctx.beginPath();
    ctx.arc(cx, cy, 140, 0, Math.PI * 2);
    ctx.fill();

    // Protective boundary ring
    ctx.beginPath();
    ctx.arc(cx, cy, 80, 0, Math.PI * 2);
    ctx.strokeStyle = 'rgba(148, 163, 184, 0.25)';
    ctx.lineWidth = 2;
    ctx.setLineDash([4, 6]);
    ctx.stroke();
    ctx.setLineDash([]);

    // Central Cultivator
    ctx.beginPath();
    ctx.arc(cx, cy, 18, 0, Math.PI * 2);
    ctx.fillStyle = '#f59e0b';
    ctx.shadowColor = '#fbbf24';
    ctx.shadowBlur = 18;
    ctx.fill();
    ctx.shadowBlur = 0;

    // Cultivator icon
    ctx.fillStyle = '#0f172a';
    ctx.font = '16px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    ctx.fillText('🧘', cx, cy);

    // Aegis Golden Shield Barrier
    const shieldR = 80;
    const shieldX = cx + Math.cos(shieldAngle) * shieldR;
    const shieldY = cy + Math.sin(shieldAngle) * shieldR;

    ctx.beginPath();
    ctx.arc(shieldX, shieldY, 20, 0, Math.PI * 2);
    ctx.fillStyle = 'rgba(251, 191, 36, 0.35)';
    ctx.strokeStyle = '#f59e0b';
    ctx.lineWidth = 3;
    ctx.shadowColor = '#f59e0b';
    ctx.shadowBlur = 15;
    ctx.fill();
    ctx.stroke();
    ctx.shadowBlur = 0;

    ctx.fillStyle = '#fef08a';
    ctx.font = '14px sans-serif';
    ctx.fillText('🛡️', shieldX, shieldY);

    // Heavenly Lightning Strike
    if (isStriking) {
      const strikeStartX = cx + Math.cos(currentStrikeAngle) * 160;
      const strikeStartY = cy + Math.sin(currentStrikeAngle) * 160;

      ctx.beginPath();
      ctx.moveTo(strikeStartX, strikeStartY);

      // Jagged lightning path
      const steps = 5;
      for (let s = 1; s <= steps; s++) {
        const t = s / steps;
        const px = strikeStartX + (cx - strikeStartX) * t + (Math.random() - 0.5) * 20;
        const py = strikeStartY + (cy - strikeStartY) * t + (Math.random() - 0.5) * 20;
        ctx.lineTo(px, py);
      }
      ctx.strokeStyle = '#38bdf8';
      ctx.lineWidth = 4;
      ctx.shadowColor = '#00e5ff';
      ctx.shadowBlur = 20;
      ctx.stroke();
      ctx.shadowBlur = 0;
    }
  }, [shieldAngle, isStriking, currentStrikeAngle]);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-lg bg-slate-900 border border-cyan-500/30 rounded-2xl p-6 shadow-2xl flex flex-col items-center">
        {/* Title */}
        <div className="text-center mb-3">
          <div className="inline-flex items-center gap-2 px-3 py-1 bg-cyan-950/60 border border-cyan-500/40 rounded-full text-cyan-300 text-xs font-mono mb-1">
            <Zap className="w-3.5 h-3.5 text-cyan-400 animate-pulse" />
            NINE HEAVENS TRIBULATION
          </div>
          <h3 className="font-serif-title font-bold text-slate-100 text-xl">
            {discipleName} Breakthrough
          </h3>
          <p className="text-xs text-slate-400">
            Ascending from <span className="text-slate-300 font-semibold">{fromRealm}</span> to{' '}
            <span className="text-amber-400 font-bold">{toRealm}</span>
          </p>
        </div>

        {/* Counter */}
        <div className="w-full bg-slate-950 border border-slate-800 rounded-xl p-3 mb-4 flex items-center justify-between">
          <div className="text-xs text-slate-400">
            Lightning Deflection:
          </div>
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

        {/* Visual Arena */}
        <div className="relative bg-slate-950/80 border border-slate-800 rounded-2xl p-2 w-full flex items-center justify-center">
          <canvas ref={canvasRef} width={340} height={320} className="w-full max-w-[340px] h-[320px]" />
          
          {/* Controls */}
          <div className="absolute bottom-3 left-4 right-4 flex justify-between gap-3">
            <button
              onClick={() => rotateShield(-0.55)}
              className="flex-1 py-2.5 px-4 bg-slate-800/90 hover:bg-slate-700 text-cyan-300 font-bold text-sm rounded-xl border border-slate-700 flex items-center justify-center gap-1.5 active:scale-95 transition"
            >
              <ChevronLeft className="w-4 h-4" /> Shift Left
            </button>
            <button
              onClick={() => rotateShield(0.55)}
              className="flex-1 py-2.5 px-4 bg-slate-800/90 hover:bg-slate-700 text-cyan-300 font-bold text-sm rounded-xl border border-slate-700 flex items-center justify-center gap-1.5 active:scale-95 transition"
            >
              Shift Right <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Result Message */}
        <div className="h-8 flex items-center justify-center text-xs font-semibold text-center mt-3 text-cyan-300">
          {strikeResult}
        </div>

        {/* Finish Dialog */}
        {isCompleted ? (
          <div className="mt-2 w-full flex flex-col items-center">
            <div className="text-emerald-400 font-bold text-center mb-3 text-sm flex items-center gap-1">
              <Award className="w-4 h-4" />
              Heavenly Dao Acknowledged! Tribulation Overcome!
            </div>
            <button
              onClick={onSuccess}
              className="w-full py-3 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold rounded-xl shadow-lg transition"
            >
              Finalize Realm Breakthrough
            </button>
          </div>
        ) : (
          <button
            onClick={onCancel}
            className="mt-2 text-xs text-slate-500 hover:text-slate-300 transition"
          >
            Retreat to meditate & prepare
          </button>
        )}
      </div>
    </div>
  );
};
