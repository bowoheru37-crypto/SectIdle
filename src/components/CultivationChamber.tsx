import React, { useState, useEffect } from 'react';
import { soundManager } from '../utils/audio';
import { Disciple } from '../types/game';
import { REALMS } from '../constants/gameData';
import { Zap, Sparkles, Award } from 'lucide-react';

interface CultivationChamberProps {
  disciples: Disciple[];
  sectRealm: number;
  sectRealmExp: number;
  onGatherQi: () => void;
  onOpenTribulation: (disciple: Disciple) => void;
  onSectRealmBreakthrough: () => void;
}

export const CultivationChamber: React.FC<CultivationChamberProps> = ({
  disciples,
  sectRealm,
  sectRealmExp,
  onGatherQi,
  onOpenTribulation,
  onSectRealmBreakthrough,
}) => {
  const [dantianQi, setDantianQi] = useState<number>(35);
  const [isMeditating, setIsMeditating] = useState<boolean>(true);

  // Passive ambient Qi gathering
  useEffect(() => {
    const timer = setInterval(() => {
      setDantianQi(prev => Math.min(100, prev + 2));
    }, 1800);
    return () => clearInterval(timer);
  }, []);

  const handleManualGather = () => {
    soundManager.playClick();
    soundManager.playGuzhengNote(Math.floor(dantianQi / 10));
    setDantianQi(prev => Math.min(100, prev + 12));
    onGatherQi();
  };

  const sectCap = 1000;
  const sectProgress = Math.min(100, Math.floor((sectRealmExp / sectCap) * 100));
  const canSectBreakthrough = sectRealmExp >= sectCap && sectRealm < REALMS.length - 1;

  return (
    <div className="space-y-5">
      {/* Title */}
      <div className="bg-slate-900/80 border border-slate-800 p-4 rounded-2xl flex items-center justify-between">
        <div>
          <h2 className="font-serif-title font-bold text-slate-100 text-lg flex items-center gap-2">
            <Sparkles className="w-5 h-5 text-cyan-400" />
            Spirit Array Cultivation Chamber
          </h2>
          <p className="text-xs text-slate-400">
            Channel the leyline Qi of Heaven and Earth to refine your core dantian and ascend realms
          </p>
        </div>

        <div className="text-right">
          <span className="text-[10px] text-slate-500 uppercase font-mono block">Sect Realm</span>
          <span className="font-serif-title font-bold text-amber-400 text-sm">
            {REALMS[sectRealm]}
          </span>
        </div>
      </div>

      {/* Interactive Meditation Array Visualizer */}
      <div className="relative w-full h-80 bg-gradient-to-b from-slate-950 via-slate-900 to-indigo-950/60 border border-cyan-500/20 rounded-3xl overflow-hidden p-6 flex flex-col items-center justify-center shadow-2xl">
        {/* Animated Spirit Array Concentric Rings */}
        <div className="absolute w-72 h-72 rounded-full border border-cyan-500/20 animate-spin [animation-duration:30s] pointer-events-none" />
        <div className="absolute w-56 h-56 rounded-full border border-dashed border-amber-500/20 animate-spin [animation-duration:20s] [animation-direction:reverse] pointer-events-none" />
        <div className="absolute w-40 h-40 rounded-full border border-cyan-400/30 animate-pulse-slow pointer-events-none" />

        {/* Central Cultivator Sphere */}
        <div className="relative z-10 w-24 h-24 rounded-full bg-gradient-to-tr from-amber-600 via-yellow-400 to-cyan-400 p-1 shadow-2xl shadow-cyan-500/30 animate-pulse-slow flex items-center justify-center">
          <div className="w-full h-full rounded-full bg-slate-950 flex flex-col items-center justify-center">
            <span className="text-3xl">🧘</span>
            <span className="text-[10px] font-mono font-bold text-cyan-300 mt-1">{dantianQi}%</span>
          </div>
        </div>

        {/* Floating Quick Action */}
        <button
          onClick={handleManualGather}
          className="relative z-10 mt-6 px-6 py-2.5 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 text-slate-950 font-bold text-xs rounded-xl shadow-lg shadow-cyan-500/20 active:scale-95 transition flex items-center gap-2"
        >
          <Sparkles className="w-4 h-4" />
          Absorb Ambient Mountain Qi (+12%)
        </button>
      </div>

      {/* Sect Realm Advancement Progress Bar */}
      <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl space-y-3">
        <div className="flex items-center justify-between text-xs">
          <span className="text-slate-300 font-bold">
            Sect Dao Density Advancement:
          </span>
          <span className="font-mono text-cyan-400 font-bold">
            {sectRealmExp} / {sectCap} Qi ({sectProgress}%)
          </span>
        </div>

        <div className="w-full h-3 bg-slate-950 rounded-full overflow-hidden border border-slate-800">
          <div
            className="h-full bg-gradient-to-r from-cyan-500 via-indigo-500 to-amber-500 transition-all duration-300"
            style={{ width: `${sectProgress}%` }}
          />
        </div>

        {canSectBreakthrough && (
          <button
            onClick={() => {
              soundManager.playBreakthrough();
              onSectRealmBreakthrough();
            }}
            className="w-full py-3 bg-gradient-to-r from-amber-500 via-yellow-400 to-amber-600 text-slate-950 font-bold text-xs rounded-xl shadow-lg animate-pulse"
          >
            Ascend Sect Realm to {REALMS[sectRealm + 1]}!
          </button>
        )}
      </div>

      {/* Ready Disciples Section */}
      <div className="bg-slate-900 border border-slate-800 p-5 rounded-2xl">
        <h3 className="font-serif-title font-bold text-slate-100 text-sm mb-3 flex items-center gap-2">
          <Zap className="w-4 h-4 text-amber-400" />
          Disciples Approaching Heavenly Tribulation
        </h3>

        <div className="space-y-2">
          {disciples.map(d => {
            const expCap = 1000;
            const pct = Math.min(100, Math.floor((d.realmExp / expCap) * 100));
            const ready = d.realmExp >= expCap && d.realm < REALMS.length - 1;
            return (
              <div
                key={d.id}
                className="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between"
              >
                <div>
                  <div className="font-bold text-xs text-slate-200">
                    {d.name} <span className="text-slate-500 font-normal">({REALMS[d.realm]})</span>
                  </div>
                  <div className="text-[11px] text-slate-400 mt-0.5 font-mono">
                    Qi Progress: {d.realmExp}/{expCap} ({pct}%)
                  </div>
                </div>

                {ready ? (
                  <button
                    onClick={() => {
                      soundManager.playClick();
                      onOpenTribulation(d);
                    }}
                    className="px-3.5 py-1.5 bg-gradient-to-r from-amber-500 to-amber-600 hover:from-amber-400 text-slate-950 font-bold text-xs rounded-lg transition animate-pulse"
                  >
                    Trigger Tribulation
                  </button>
                ) : (
                  <button
                    onClick={() => {
                      soundManager.playClick();
                      d.realmExp += 150;
                      soundManager.playGuzhengNote(3);
                    }}
                    className="px-3 py-1 bg-slate-800 hover:bg-slate-700 text-slate-300 text-xs font-semibold rounded-lg transition"
                  >
                    Bestow Qi (+150)
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
};
