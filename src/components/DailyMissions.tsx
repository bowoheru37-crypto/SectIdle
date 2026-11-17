import React from 'react';
import type { DailyMission } from '../types/game';
import { soundManager } from '../utils/audio';
import { ScrollText, RefreshCw, CheckCircle2, Sparkles, Coins } from 'lucide-react';

interface DailyMissionsProps {
  missions: DailyMission[];
  dayKey: string;
  spiritStones: number;
  onClaimMission: (missionId: string) => void;
  onRerollMissions: () => void;
}

export const DailyMissions: React.FC<DailyMissionsProps> = ({
  missions,
  dayKey,
  spiritStones,
  onClaimMission,
  onRerollMissions,
}) => {
  const claimedCount = missions.filter(m => m.claimed).length;
  const readyCount = missions.filter(m => !m.claimed && m.progress >= m.target).length;
  const rerollCost = 50;
  const canReroll = spiritStones >= rerollCost;

  return (
    <section
      aria-labelledby="daily-missions-heading"
      className="bg-slate-900/75 border border-slate-800 rounded-3xl p-4 sm:p-5 shadow-xl space-y-4"
    >
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="space-y-0.5">
          <div className="flex items-center gap-2">
            <ScrollText className="w-5 h-5 text-amber-400 shrink-0" aria-hidden="true" />
            <h3
              id="daily-missions-heading"
              className="font-serif-title font-bold text-base text-slate-100"
            >
              Daily Missions
            </h3>
            <span className="text-[11px] font-mono tabular-nums text-slate-400">
              {dayKey}
            </span>
          </div>
          <p className="text-xs text-slate-400">
            Complete sect mandates to earn randomized Spirit Stones and Sect EXP rewards.
          </p>
        </div>

        <div className="flex items-center gap-3">
          <div className="text-xs font-mono tabular-nums text-slate-300">
            <span className="text-emerald-400 font-bold">{claimedCount}</span> / {missions.length} Claimed
            {readyCount > 0 && (
              <span className="ml-2 text-amber-300 font-bold">({readyCount} Ready)</span>
            )}
          </div>

          <button
            type="button"
            onClick={() => {
              soundManager.playClick();
              onRerollMissions();
            }}
            disabled={!canReroll}
            title="Roll a fresh set of unclaimed Daily Missions with randomized rewards"
            className={`inline-flex items-center gap-1.5 px-3 min-h-[40px] rounded-xl text-xs font-bold whitespace-nowrap transition border ${
              canReroll
                ? 'bg-slate-800 hover:bg-slate-700 border-slate-700 text-slate-200'
                : 'bg-slate-900 border-slate-800 text-slate-500 cursor-not-allowed'
            }`}
          >
            <RefreshCw className="w-3.5 h-3.5" aria-hidden="true" />
            New Scroll ({rerollCost} SS)
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-3">
        {missions.map(mission => {
          const pct = Math.min(100, Math.round((mission.progress / mission.target) * 100));
          const isReady = !mission.claimed && mission.progress >= mission.target;

          return (
            <div
              key={mission.id}
              className={`rounded-2xl border p-4 flex flex-col justify-between gap-3 transition ${
                isReady
                  ? 'bg-emerald-950/25 border-emerald-500/50 shadow-md shadow-emerald-950/30'
                  : mission.claimed
                  ? 'bg-slate-950/60 border-slate-800/70 opacity-75'
                  : 'bg-slate-950/75 border-slate-800'
              }`}
            >
              <div className="space-y-2">
                <div className="flex items-start justify-between gap-2">
                  <h4 className="font-bold text-sm text-slate-100 leading-snug">
                    {mission.title}
                  </h4>
                  <span
                    className={`inline-flex items-center gap-1 text-xs font-mono font-bold tabular-nums shrink-0 ${
                      mission.rewardType === 'spiritStones'
                        ? 'text-amber-300'
                        : 'text-emerald-300'
                    }`}
                  >
                    {mission.rewardType === 'spiritStones' ? (
                      <Coins className="w-3.5 h-3.5" aria-hidden="true" />
                    ) : (
                      <Sparkles className="w-3.5 h-3.5" aria-hidden="true" />
                    )}
                    +{mission.rewardAmount}{' '}
                    {mission.rewardType === 'spiritStones' ? 'SS' : 'Sect EXP'}
                  </span>
                </div>

                <p className="text-xs text-slate-400 leading-relaxed">
                  {mission.description}
                </p>
              </div>

              <div className="space-y-2.5">
                <div>
                  <div className="flex items-center justify-between text-[11px] font-mono tabular-nums text-slate-400 mb-1">
                    <span>Progress</span>
                    <span className={isReady ? 'text-emerald-300 font-bold' : 'text-slate-300'}>
                      {mission.progress} / {mission.target}
                    </span>
                  </div>
                  <div className="w-full h-1.5 bg-slate-800 rounded-full overflow-hidden">
                    <div
                      className={`h-full transition-all duration-200 ${
                        mission.claimed
                          ? 'bg-slate-500'
                          : isReady
                          ? 'bg-emerald-400'
                          : 'bg-amber-400'
                      }`}
                      style={{ width: `${pct}%` }}
                    />
                  </div>
                </div>

                <button
                  type="button"
                  disabled={!isReady}
                  onClick={() => {
                    soundManager.playBreakthrough();
                    onClaimMission(mission.id);
                  }}
                  className={`w-full min-h-[40px] px-3 rounded-xl font-bold text-xs flex items-center justify-center gap-1.5 whitespace-nowrap transition ${
                    mission.claimed
                      ? 'bg-slate-900 border border-slate-800 text-slate-500 cursor-default'
                      : isReady
                      ? 'bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-400 hover:to-teal-400 text-slate-950 shadow-md'
                      : 'bg-slate-800/80 border border-slate-700/60 text-slate-400 cursor-not-allowed'
                  }`}
                >
                  {mission.claimed ? (
                    <>
                      <CheckCircle2 className="w-3.5 h-3.5" aria-hidden="true" />
                      Claimed
                    </>
                  ) : isReady ? (
                    'Claim Reward'
                  ) : (
                    `In Progress (${mission.progress}/${mission.target})`
                  )}
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </section>
  );
};
