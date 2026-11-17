import React, { useEffect, useRef, useState } from 'react';
import { Zap, Award, AlertTriangle } from 'lucide-react';
import { soundManager } from '../utils/audio';
import { ResolvedPerformanceTier } from '../core/sectEngine';
import { ModalShell } from './ModalShell';

interface SlashItem {
  id: number;
  x: number;
  y: number;
  vx: number;
  vy: number;
  radius: number;
  isBomb: boolean;
  color: string;
  name: string;
  isSliced: boolean;
}

interface Particle {
  x: number;
  y: number;
  vx: number;
  vy: number;
  color: string;
  life: number;
  maxLife: number;
}

interface SlashMiniGameModalProps {
  isOpen: boolean;
  onClose: () => void;
  buildingType: 'garden' | 'alchemy';
  perfTier?: ResolvedPerformanceTier;
  onFinished: (score: number, maxCombo: number) => void;
}

export const SlashMiniGameModal: React.FC<SlashMiniGameModalProps> = ({
  isOpen,
  onClose,
  buildingType,
  perfTier = 'balanced',
  onFinished,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const onFinishedRef = useRef(onFinished);
  const perfTierRef = useRef(perfTier);
  onFinishedRef.current = onFinished;
  perfTierRef.current = perfTier;

  const [timeLeft, setTimeLeft] = useState<number>(15);
  const [combo, setCombo] = useState<number>(0);
  const [score, setScore] = useState<number>(0);
  const [isGameOver, setIsGameOver] = useState<boolean>(false);
  const [summaryMsg, setSummaryMsg] = useState<string>('');

  const stateRef = useRef({
    items: [] as SlashItem[],
    particles: [] as Particle[],
    trails: [] as { x: number; y: number; life: number }[],
    isMouseDown: false,
    lastX: 0,
    lastY: 0,
    score: 0,
    combo: 0,
    maxCombo: 0,
    timeLeft: 15,
    lastSpawn: 0,
    isOver: false,
    buildingType,
  });

  stateRef.current.buildingType = buildingType;

  useEffect(() => {
    if (!isOpen) return;

    stateRef.current = {
      items: [],
      particles: [],
      trails: [],
      isMouseDown: false,
      lastX: 0,
      lastY: 0,
      score: 0,
      combo: 0,
      maxCombo: 0,
      timeLeft: 15,
      lastSpawn: performance.now(),
      isOver: false,
      buildingType,
    };

    setScore(0);
    setCombo(0);
    setTimeLeft(15);
    setIsGameOver(false);
    setSummaryMsg('');

    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    let animId = 0;
    let lastTime = performance.now();

    const spawnItem = (w: number, h: number) => {
      const isBomb = Math.random() < 0.16;
      const herbColors = ['#22c55e', '#3b82f6', '#eab308', '#ec4899', '#a855f7'];
      const color = isBomb ? '#ef4444' : herbColors[Math.floor(Math.random() * herbColors.length)];
      const names =
        buildingType === 'garden'
          ? ['Nine-Leaf Ginseng', 'Dragon Root', 'Spirit Orchid', 'Moonlight Petal']
          : ['Refining Essence', 'Mercury Dew', 'Cinnabar Pellet', 'Jade Core'];

      stateRef.current.items.push({
        id: Math.random(),
        x: w * 0.15 + Math.random() * (w * 0.7),
        y: h + 30,
        vx: (Math.random() - 0.5) * 120,
        vy: -(h * 0.95 + Math.random() * 250),
        radius: 28 + Math.random() * 12,
        isBomb,
        color,
        name: isBomb ? 'Demonic Qi Bomb' : names[Math.floor(Math.random() * names.length)],
        isSliced: false,
      });
    };

    const loop = (now: number) => {
      if (typeof document !== 'undefined' && document.hidden) {
        lastTime = now;
        animId = requestAnimationFrame(loop);
        return;
      }

      const dt = Math.min(0.06, (now - lastTime) / 1000);
      lastTime = now;

      if (!stateRef.current.isOver) {
        stateRef.current.timeLeft -= dt;
        setTimeLeft(Math.max(0, Math.ceil(stateRef.current.timeLeft)));

        if (stateRef.current.timeLeft <= 0) {
          stateRef.current.isOver = true;
          setIsGameOver(true);
          const finalScore = stateRef.current.score;
          const finalMaxCombo = stateRef.current.maxCombo;
          const msg =
            buildingType === 'garden'
              ? `Herb harvest complete! Harvested +${finalScore * 8} Spirit Herbs with ${finalMaxCombo}x Max Combo!`
              : `Pill concoction finalized! +${finalScore * 12} Alchemy Points with ${finalMaxCombo}x Max Combo!`;
          setSummaryMsg(msg);
          soundManager.playBreakthrough();
          onFinishedRef.current(finalScore, finalMaxCombo);
        }

        if (now - stateRef.current.lastSpawn > 650) {
          spawnItem(canvas.width, canvas.height);
          stateRef.current.lastSpawn = now;
        }
      }

      const gravity = 700;
      for (const it of stateRef.current.items) {
        it.x += it.vx * dt;
        it.y += it.vy * dt;
        it.vy += gravity * dt;
      }
      stateRef.current.items = stateRef.current.items.filter(it => it.y < canvas.height + 60);

      for (const p of stateRef.current.particles) {
        p.x += p.vx * dt;
        p.y += p.vy * dt;
        p.vy += 350 * dt;
        p.life -= dt;
      }
      stateRef.current.particles = stateRef.current.particles.filter(p => p.life > 0);

      for (const t of stateRef.current.trails) {
        t.life -= dt * 3.5;
      }
      stateRef.current.trails = stateRef.current.trails.filter(t => t.life > 0);

      ctx.clearRect(0, 0, canvas.width, canvas.height);

      if (stateRef.current.trails.length > 1) {
        ctx.beginPath();
        ctx.moveTo(stateRef.current.trails[0].x, stateRef.current.trails[0].y);
        for (let i = 1; i < stateRef.current.trails.length; i++) {
          const t = stateRef.current.trails[i];
          ctx.lineTo(t.x, t.y);
        }
        ctx.strokeStyle = '#38bdf8';
        ctx.lineWidth = 5;
        ctx.lineCap = 'round';
        ctx.stroke();
      }

      const useGlow = perfTierRef.current !== 'eco';
      for (const it of stateRef.current.items) {
        if (it.isSliced) continue;
        ctx.save();
        ctx.beginPath();
        ctx.arc(it.x, it.y, it.radius, 0, Math.PI * 2);
        ctx.fillStyle = it.color;
        if (useGlow) {
          ctx.shadowColor = it.color;
          ctx.shadowBlur = 12;
        }
        ctx.fill();
        ctx.shadowBlur = 0;

        ctx.strokeStyle = '#ffffff';
        ctx.lineWidth = 2;
        ctx.stroke();

        ctx.fillStyle = '#ffffff';
        ctx.font = 'bold 12px sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(it.isBomb ? '💣' : buildingType === 'garden' ? '🌿' : '💊', it.x, it.y);
        ctx.restore();
      }

      for (const p of stateRef.current.particles) {
        ctx.save();
        ctx.globalAlpha = Math.max(0, p.life / p.maxLife);
        ctx.beginPath();
        ctx.arc(p.x, p.y, 4, 0, Math.PI * 2);
        ctx.fillStyle = p.color;
        ctx.fill();
        ctx.restore();
      }

      animId = requestAnimationFrame(loop);
    };

    animId = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(animId);
  }, [isOpen, buildingType]);

  const toCanvasCoords = (e: React.PointerEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return null;
    const rect = canvas.getBoundingClientRect();
    const scaleX = canvas.width / Math.max(1, rect.width);
    const scaleY = canvas.height / Math.max(1, rect.height);
    return {
      x: (e.clientX - rect.left) * scaleX,
      y: (e.clientY - rect.top) * scaleY,
    };
  };

  const handlePointerDown = (e: React.PointerEvent<HTMLCanvasElement>) => {
    const pt = toCanvasCoords(e);
    if (!pt) return;
    stateRef.current.isMouseDown = true;
    stateRef.current.lastX = pt.x;
    stateRef.current.lastY = pt.y;
    stateRef.current.trails.push({ x: pt.x, y: pt.y, life: 1 });
  };

  const handlePointerMove = (e: React.PointerEvent<HTMLCanvasElement>) => {
    if (!stateRef.current.isMouseDown || stateRef.current.isOver) return;
    const pt = toCanvasCoords(e);
    if (!pt) return;
    const { x, y } = pt;

    stateRef.current.trails.push({ x, y, life: 1 });

    const particleBurstCount = perfTierRef.current === 'eco' ? 6 : 12;

    for (const it of stateRef.current.items) {
      if (it.isSliced) continue;
      const dx = it.x - x;
      const dy = it.y - y;
      if (Math.hypot(dx, dy) < it.radius + 18) {
        it.isSliced = true;

        for (let i = 0; i < particleBurstCount; i++) {
          const angle = Math.random() * Math.PI * 2;
          const speed = 70 + Math.random() * 180;
          stateRef.current.particles.push({
            x: it.x,
            y: it.y,
            vx: Math.cos(angle) * speed,
            vy: Math.sin(angle) * speed,
            color: it.color,
            life: 0.45,
            maxLife: 0.45,
          });
        }

        if (it.isBomb) {
          soundManager.playLightningZap();
          stateRef.current.combo = 0;
          stateRef.current.score = Math.max(0, stateRef.current.score - 5);
          setCombo(0);
          setScore(stateRef.current.score);
        } else {
          soundManager.playSwordStrike();
          stateRef.current.combo += 1;
          if (stateRef.current.combo > stateRef.current.maxCombo) {
            stateRef.current.maxCombo = stateRef.current.combo;
          }
          stateRef.current.score += 1 + Math.floor(stateRef.current.combo / 3);
          setCombo(stateRef.current.combo);
          setScore(stateRef.current.score);
        }
      }
    }

    stateRef.current.lastX = x;
    stateRef.current.lastY = y;
  };

  const handlePointerUp = () => {
    stateRef.current.isMouseDown = false;
  };

  return (
    <ModalShell
      isOpen={isOpen}
      onClose={onClose}
      maxWidth="lg"
      borderClassName="border-amber-500/30"
      bodyClassName="flex flex-col overflow-hidden"
      icon={<Zap className="w-5 h-5 text-amber-400 animate-pulse" />}
      title={buildingType === 'garden' ? 'Spirit Herb Sword-Slash' : 'Alchemy Cauldron Slice'}
      footer={
        <div className="px-4 py-2 bg-slate-950 text-xs text-slate-400 text-center border-t border-slate-800 flex items-center justify-center gap-1.5">
          <AlertTriangle className="w-3.5 h-3.5 text-amber-400 shrink-0" />
          <span>Swipe fast to slice spiritual ingredients! Avoid demonic bombs (💣).</span>
        </div>
      }
    >
      <div className="grid grid-cols-3 gap-2 px-5 py-2.5 bg-slate-900/90 text-sm border-b border-slate-800">
        <div className="text-center">
          <span className="text-slate-400 block text-xs">TIME</span>
          <span className={`font-mono font-bold ${timeLeft <= 3 ? 'text-red-400 animate-pulse' : 'text-cyan-400'}`}>
            {timeLeft}s
          </span>
        </div>
        <div className="text-center">
          <span className="text-slate-400 block text-xs">COMBO</span>
          <span className="font-mono font-bold text-amber-400">{combo}x</span>
        </div>
        <div className="text-center">
          <span className="text-slate-400 block text-xs">HARVEST SCORE</span>
          <span className="font-mono font-bold text-emerald-400">{score}</span>
        </div>
      </div>

      <div className="relative bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950 flex items-center justify-center overflow-hidden">
        <canvas
          ref={canvasRef}
          width={480}
          height={360}
          onPointerDown={handlePointerDown}
          onPointerMove={handlePointerMove}
          onPointerUp={handlePointerUp}
          onPointerLeave={handlePointerUp}
          className="touch-none cursor-crosshair w-full h-[360px]"
        />

        {isGameOver && (
          <div className="absolute inset-0 bg-black/85 flex flex-col items-center justify-center p-6 text-center animate-fade-in">
            <Award className="w-12 h-12 text-amber-400 mb-2 animate-bounce" />
            <h4 className="font-serif-title text-xl font-bold text-amber-300 mb-1">Session Complete!</h4>
            <p className="text-sm text-slate-200 max-w-sm mb-4 leading-relaxed">{summaryMsg}</p>
            <button
              type="button"
              onClick={onClose}
              className="px-6 py-2 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 hover:to-amber-500 text-slate-950 font-bold rounded-xl transition shadow-lg"
            >
              Claim Harvest & Return
            </button>
          </div>
        )}
      </div>
    </ModalShell>
  );
};
