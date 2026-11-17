// Xianxia Web Audio Synthesizer - Unified & Battery/Lifecycle-Aware

class SoundManager {
  private ctx: AudioContext | null = null;
  public enabled: boolean = true;
  public bgmEnabled: boolean = false;
  private bgmInterval: number | null = null;
  private listenersAttached: boolean = false;

  private ensureVisibilityLifecycle() {
    if (this.listenersAttached || typeof document === 'undefined') return;
    this.listenersAttached = true;

    document.addEventListener('visibilitychange', () => {
      if (document.hidden) {
        if (this.bgmInterval) {
          clearInterval(this.bgmInterval);
          this.bgmInterval = null;
        }
        if (this.ctx && this.ctx.state === 'running') {
          this.ctx.suspend().catch(() => {});
        }
      } else if (this.bgmEnabled && this.enabled) {
        this.startBgmInterval();
      }
    });
  }

  private initCtx(): AudioContext | null {
    if (typeof window === 'undefined' || !this.enabled) return null;
    if (typeof document !== 'undefined' && document.hidden) return null;
    this.ensureVisibilityLifecycle();

    if (!this.ctx) {
      const AudioCtx =
        window.AudioContext ||
        (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (AudioCtx) {
        this.ctx = new AudioCtx();
      }
    }
    if (this.ctx && this.ctx.state === 'suspended') {
      this.ctx.resume().catch(() => {});
    }
    return this.ctx;
  }

  public playGuzhengNote(noteIndex = 0) {
    const ctx = this.initCtx();
    if (!ctx) return;

    const scale = [261.63, 293.66, 329.63, 392.00, 440.00, 523.25, 587.33, 659.25, 783.99, 880.00];
    const freq = scale[Math.abs(noteIndex) % scale.length];

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'triangle';
    osc.frequency.setValueAtTime(freq, ctx.currentTime);

    gain.gain.setValueAtTime(0.001, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.18, ctx.currentTime + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + 1.2);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 1.3);
  }

  public playSwordStrike() {
    const ctx = this.initCtx();
    if (!ctx) return;

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();

    osc.type = 'sawtooth';
    osc.frequency.setValueAtTime(1200, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(150, ctx.currentTime + 0.15);

    gain.gain.setValueAtTime(0.2, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.2);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.22);
  }

  public playCriticalStrike() {
    const ctx = this.initCtx();
    if (!ctx) return;

    const osc1 = ctx.createOscillator();
    const gain1 = ctx.createGain();
    osc1.type = 'sine';
    osc1.frequency.setValueAtTime(2400, ctx.currentTime);
    osc1.frequency.exponentialRampToValueAtTime(400, ctx.currentTime + 0.25);
    gain1.gain.setValueAtTime(0.3, ctx.currentTime);
    gain1.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
    osc1.connect(gain1);
    gain1.connect(ctx.destination);

    const osc2 = ctx.createOscillator();
    const gain2 = ctx.createGain();
    osc2.type = 'triangle';
    osc2.frequency.setValueAtTime(160, ctx.currentTime);
    osc2.frequency.exponentialRampToValueAtTime(45, ctx.currentTime + 0.35);
    gain2.gain.setValueAtTime(0.35, ctx.currentTime);
    gain2.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.4);
    osc2.connect(gain2);
    gain2.connect(ctx.destination);

    osc1.start();
    osc2.start();
    osc1.stop(ctx.currentTime + 0.45);
    osc2.stop(ctx.currentTime + 0.45);
  }

  public playBreakthrough() {
    const ctx = this.initCtx();
    if (!ctx) return;

    const chords = [523.25, 659.25, 783.99, 1046.50];
    chords.forEach((f, idx) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = 'sine';
      osc.frequency.setValueAtTime(f, ctx.currentTime + idx * 0.08);

      gain.gain.setValueAtTime(0.001, ctx.currentTime + idx * 0.08);
      gain.gain.linearRampToValueAtTime(0.18, ctx.currentTime + idx * 0.08 + 0.03);
      gain.gain.exponentialRampToValueAtTime(0.0001, ctx.currentTime + idx * 0.08 + 1.5);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(ctx.currentTime + idx * 0.08);
      osc.stop(ctx.currentTime + idx * 0.08 + 1.6);
    });
  }

  public playLightningZap() {
    const ctx = this.initCtx();
    if (!ctx) return;

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'square';
    osc.frequency.setValueAtTime(800 + Math.random() * 400, ctx.currentTime);
    osc.frequency.exponentialRampToValueAtTime(200, ctx.currentTime + 0.12);

    gain.gain.setValueAtTime(0.18, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.15);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.16);
  }

  public playClick() {
    const ctx = this.initCtx();
    if (!ctx) return;

    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = 'sine';
    osc.frequency.setValueAtTime(880, ctx.currentTime);
    gain.gain.setValueAtTime(0.06, ctx.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.06);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start();
    osc.stop(ctx.currentTime + 0.07);
  }

  private startBgmInterval() {
    if (this.bgmInterval) {
      clearInterval(this.bgmInterval);
      this.bgmInterval = null;
    }
    if (typeof window === 'undefined' || !this.bgmEnabled || !this.enabled) return;

    this.bgmInterval = window.setInterval(() => {
      if (!this.bgmEnabled || !this.enabled || (typeof document !== 'undefined' && document.hidden)) return;
      const note = Math.floor(Math.random() * 8);
      this.playGuzhengNote(note);
    }, 3500);
  }

  public setSoundEnabled(enabled: boolean) {
    this.enabled = enabled;
    if (!enabled) {
      if (this.bgmInterval) {
        clearInterval(this.bgmInterval);
        this.bgmInterval = null;
      }
      if (this.ctx && this.ctx.state === 'running') {
        this.ctx.suspend().catch(() => {});
      }
    } else if (this.bgmEnabled) {
      this.startBgmInterval();
    }
  }

  public toggleBgm(state?: boolean) {
    this.bgmEnabled = state !== undefined ? state : !this.bgmEnabled;
    if (this.bgmEnabled && this.enabled) {
      this.startBgmInterval();
    } else if (this.bgmInterval) {
      clearInterval(this.bgmInterval);
      this.bgmInterval = null;
    }
  }
}

export const soundManager = new SoundManager();
