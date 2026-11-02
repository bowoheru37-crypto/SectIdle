import React from 'react';
import { MarketItem } from '../types/game';
import { soundManager } from '../utils/audio';
import { ShoppingBag, TrendingUp, TrendingDown, X, PackageCheck } from 'lucide-react';

interface MarketplaceProps {
  isOpen: boolean;
  onClose: () => void;
  marketListings: MarketItem[];
  spiritStones: number;
  onBuyItem: (item: MarketItem) => void;
  onSellResource: (type: 'herbs' | 'ores', amount: number, earnings: number) => void;
  spiritHerbs: number;
  spiritOres: number;
}

export const Marketplace: React.FC<MarketplaceProps> = ({
  isOpen,
  onClose,
  marketListings,
  spiritStones,
  onBuyItem,
  onSellResource,
  spiritHerbs,
  spiritOres,
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/85 backdrop-blur-md p-4">
      <div className="relative w-full max-w-2xl bg-slate-900 border border-amber-500/30 rounded-2xl overflow-hidden shadow-2xl flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-800 bg-slate-950/90">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-amber-500/10 border border-amber-500/30 rounded-xl">
              <ShoppingBag className="w-5 h-5 text-amber-400" />
            </div>
            <div>
              <h3 className="font-serif-title font-bold text-slate-100 text-lg">
                Sect Grand Bazaar
              </h3>
              <p className="text-xs text-slate-400">
                Fluctuating commodities, cultivation artifacts, and spiritual resources
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

        {/* Resources Header */}
        <div className="flex items-center justify-between px-6 py-2.5 bg-slate-950 border-b border-slate-800 text-xs font-mono">
          <div className="flex gap-4">
            <span>Treasury: <strong className="text-amber-400 font-bold">{spiritStones} SS</strong></span>
            <span>Herbs: <strong className="text-emerald-400 font-bold">{spiritHerbs}</strong></span>
            <span>Ores: <strong className="text-cyan-400 font-bold">{spiritOres}</strong></span>
          </div>
          <span className="text-slate-500 text-[11px]">Prices update every sect day</span>
        </div>

        {/* Content */}
        <div className="p-6 space-y-6 overflow-y-auto max-h-[72vh]">
          {/* Quick Sell Section */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Sell Sect Surplus to Merchants
            </h4>
            <div className="grid grid-cols-2 gap-3">
              <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between">
                <div>
                  <div className="text-xs font-bold text-slate-200">Sell 25 Spirit Herbs</div>
                  <div className="text-[11px] text-slate-400 mt-0.5">Value: +300 Spirit Stones</div>
                </div>
                <button
                  onClick={() => {
                    if (spiritHerbs >= 25) {
                      soundManager.playClick();
                      onSellResource('herbs', 25, 300);
                    }
                  }}
                  disabled={spiritHerbs < 25}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                    spiritHerbs >= 25
                      ? 'bg-emerald-600 hover:bg-emerald-500 text-white'
                      : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                  }`}
                >
                  Sell (25)
                </button>
              </div>

              <div className="p-3 bg-slate-950 border border-slate-800 rounded-xl flex items-center justify-between">
                <div>
                  <div className="text-xs font-bold text-slate-200">Sell 15 Spirit Ores</div>
                  <div className="text-[11px] text-slate-400 mt-0.5">Value: +450 Spirit Stones</div>
                </div>
                <button
                  onClick={() => {
                    if (spiritOres >= 15) {
                      soundManager.playClick();
                      onSellResource('ores', 15, 450);
                    }
                  }}
                  disabled={spiritOres < 15}
                  className={`px-3 py-1.5 rounded-lg text-xs font-bold transition ${
                    spiritOres >= 15
                      ? 'bg-cyan-600 hover:bg-cyan-500 text-white'
                      : 'bg-slate-800 text-slate-500 cursor-not-allowed'
                  }`}
                >
                  Sell (15)
                </button>
              </div>
            </div>
          </div>

          {/* Commodities / Market Listings */}
          <div>
            <h4 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-2">
              Merchant Commodities for Purchase
            </h4>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              {marketListings.map(item => {
                const canAfford = spiritStones >= item.price && item.stock > 0;
                const priceTrend = item.price >= item.originalPrice ? 'up' : 'down';
                return (
                  <div
                    key={item.id}
                    className="p-3.5 bg-slate-950 border border-slate-800 hover:border-slate-700 rounded-xl flex flex-col justify-between transition"
                  >
                    <div>
                      <div className="flex items-start justify-between gap-2">
                        <span className="font-bold text-xs text-slate-100">{item.itemName}</span>
                        <div className="flex items-center gap-1 font-mono text-[11px]">
                          {priceTrend === 'up' ? (
                            <span className="text-amber-400 flex items-center">
                              <TrendingUp className="w-3 h-3 mr-0.5" />
                              {item.price} SS
                            </span>
                          ) : (
                            <span className="text-emerald-400 flex items-center">
                              <TrendingDown className="w-3 h-3 mr-0.5" />
                              {item.price} SS
                            </span>
                          )}
                        </div>
                      </div>
                      <div className="flex items-center justify-between text-[11px] text-slate-400 mt-1">
                        <span>Stock: <strong className="text-slate-300 font-mono">{item.stock}/{item.maxStock}</strong></span>
                        <span className="text-slate-500">Demand: {Math.round(item.demand * 100)}%</span>
                      </div>
                    </div>

                    <button
                      onClick={() => {
                        if (canAfford) {
                          soundManager.playClick();
                          onBuyItem(item);
                        }
                      }}
                      disabled={!canAfford}
                      className={`mt-3 w-full py-2 rounded-lg font-bold text-xs transition ${
                        canAfford
                          ? 'bg-amber-500/10 hover:bg-amber-500 border border-amber-500/30 hover:border-amber-500 text-amber-300 hover:text-slate-950'
                          : 'bg-slate-800 text-slate-600 border border-slate-800 cursor-not-allowed'
                      }`}
                    >
                      {item.stock <= 0 ? 'Out of Stock' : canAfford ? `Purchase (${item.price} SS)` : 'Insufficient Stones'}
                    </button>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
