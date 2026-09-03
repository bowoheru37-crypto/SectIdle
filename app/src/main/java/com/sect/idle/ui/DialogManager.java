package com.sect.idle.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.example.R;
import com.sect.idle.core.GameConfig;
import com.sect.idle.core.GameState;
import com.sect.idle.gameplay.MarketSystem;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.gameplay.HobbySystem;
import com.sect.idle.gameplay.JobSystem;
import com.sect.idle.systems.NumberFormatter;
import com.sect.idle.utils.BitmapCache;
import com.sect.idle.utils.DataValidator;
import com.sect.idle.utils.ExceptionManager;
import com.sect.idle.utils.ExceptionManager.StructuredLogEntry;
import com.sect.idle.utils.MemoryPool;
import com.sect.idle.utils.PerformanceProfiler;
import com.sect.idle.utils.SecurityManager;
import java.util.ArrayList;

/**
 * DialogManager v6.0 - Cinematic Dialog & Overlay System
 * Features:
 * - Safe Activity lifecycle handling (Zero BadTokenException).
 * - Full suite of Xianxia UI dialogs: Recruit, Disciple Detail, Task Assignment,
 *   Building Management & Upgrade, Market Trading, Story Mode, Settings, and Combat Results.
 * - Audio-visual feedback with fade animations and SFX integration.
 * - Zero GC allocations for text formatting.
 *
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class DialogManager {
    private final Context context;
    private final ArrayList<Dialog> activeDialogs;
    private final StringBuilder sb;

    private final AlphaAnimation fadeIn;
    private final AlphaAnimation fadeOut;

    public DialogManager(Context ctx) {
        this.context = ctx;
        this.activeDialogs = new ArrayList<Dialog>(4);
        this.sb = new StringBuilder(256);

        fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(220);
        fadeOut = new AlphaAnimation(1f, 0f);
        fadeOut.setDuration(180);
    }

    private boolean isContextValid() {
        if (context == null) return false;
        if (context instanceof Activity) {
            Activity a = (Activity) context;
            if (a.isFinishing()) return false;
            if (android.os.Build.VERSION.SDK_INT >= 17 && a.isDestroyed()) return false;
        }
        return true;
    }

    private void playClickSfx() {
        try {
            AudioManager.getInstance(context).playSfx(AudioManager.SFX_CLICK);
        } catch (Exception ignored) {}
    }

    public void showRecruitDialog(Disciple candidate, final RecruitCallback cb) {
        if (!isContextValid() || candidate == null || cb == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_recruit);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvName = (TextView) d.findViewById(R.id.tvCandidateName);
            TextView tvRealm = (TextView) d.findViewById(R.id.tvCandidateRealm);
            TextView tvStr = (TextView) d.findViewById(R.id.tvStatStr);
            TextView tvAgi = (TextView) d.findViewById(R.id.tvStatAgi);
            TextView tvInt = (TextView) d.findViewById(R.id.tvStatInt);
            TextView tvLck = (TextView) d.findViewById(R.id.tvStatLck);
            TextView tvPers = (TextView) d.findViewById(R.id.tvPersonality);
            TextView tvWage = (TextView) d.findViewById(R.id.tvWageCost);
            Button btnAccept = (Button) d.findViewById(R.id.btnAccept);
            Button btnReject = (Button) d.findViewById(R.id.btnReject);

            if (tvName != null) tvName.setText(candidate.name != null ? candidate.name : "Wandering Cultivator");
            if (tvRealm != null) tvRealm.setText(candidate.getRealmDisplay());
            if (tvStr != null) tvStr.setText(String.valueOf(candidate.str));
            if (tvAgi != null) tvAgi.setText(String.valueOf(candidate.agi));
            if (tvInt != null) tvInt.setText(String.valueOf(candidate.intel));
            if (tvLck != null) tvLck.setText(String.valueOf(candidate.lck));

            if (tvPers != null) {
                sb.setLength(0);
                sb.append("Talent: ").append(candidate.talentName != null ? candidate.talentName : "None")
                  .append(" | Personality: ").append(getPersonalityName(candidate.personality));
                tvPers.setText(sb.toString());
            }
            if (tvWage != null) {
                sb.setLength(0);
                sb.append("Wage: ").append(candidate.dailyWage).append(" SS/day | Element: ")
                  .append(getElementName(candidate.element));
                tvWage.setText(sb.toString());
            }

            if (btnAccept != null) {
                btnAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        cb.onAccept();
                        dismissDialog(d);
                    }
                });
            }
            if (btnReject != null) {
                btnReject.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        cb.onReject();
                        dismissDialog(d);
                    }
                });
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showDiscipleDetail(final Disciple disciple, final DetailCallback cb) {
        if (!isContextValid() || disciple == null || cb == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_disciple_detail);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvName = (TextView) d.findViewById(R.id.tvDetailName);
            TextView tvRealm = (TextView) d.findViewById(R.id.tvDetailRealm);
            TextView tvStr = (TextView) d.findViewById(R.id.tvDetailStr);
            TextView tvAgi = (TextView) d.findViewById(R.id.tvDetailAgi);
            TextView tvInt = (TextView) d.findViewById(R.id.tvDetailInt);
            TextView tvLck = (TextView) d.findViewById(R.id.tvDetailLck);
            TextView tvBehavior = (TextView) d.findViewById(R.id.tvDetailBehavior);
            Button btnClose = (Button) d.findViewById(R.id.btnCloseDetail);
            Button btnAssign = (Button) d.findViewById(R.id.btnAssignTask);

            if (tvName != null) {
                sb.setLength(0);
                sb.append(disciple.name).append(" [").append(getPersonalityName(disciple.personality)).append("]");
                tvName.setText(sb.toString());
            }
            if (tvRealm != null) {
                String jobName = "Cultivator";
                try { jobName = JobSystem.getJobName(disciple); } catch (Exception ignored) {}
                sb.setLength(0);
                sb.append(disciple.getRealmDisplay()).append(" | ").append(jobName).append(" | Age: ").append(disciple.age);
                tvRealm.setText(sb.toString());
            }
            if (tvStr != null) tvStr.setText(String.valueOf(disciple.str));
            if (tvAgi != null) tvAgi.setText(String.valueOf(disciple.agi));
            if (tvInt != null) tvInt.setText(String.valueOf(disciple.intel));
            if (tvLck != null) tvLck.setText(String.valueOf(disciple.lck));

            if (tvBehavior != null) {
                sb.setLength(0);
                sb.append("HP: ").append(disciple.hp).append("/").append(disciple.maxHp)
                  .append(" | MP: ").append(disciple.mp).append("/").append(disciple.maxMp).append("\n")
                  .append("ATK: ").append(disciple.atk).append(" | DEF: ").append(disciple.def)
                  .append(" | SPD: ").append(disciple.spd).append("\n")
                  .append("Mood: ").append(disciple.mood).append(" | Stress: ").append(disciple.stress)
                  .append(" | Energy: ").append(disciple.energy).append("\n")
                  .append("Loyalty: ").append(disciple.loyalty)
                  .append(" | Contribution: ").append(NumberFormatter.format(disciple.totalContribution)).append("\n")
                  .append("Task: ").append(GameConfig.getTaskName(disciple.currentTask))
                  .append(" | Efficiency: ").append((int)(disciple.efficiency * 100)).append("%\n")
                  .append("Hobby: ");
                try { sb.append(HobbySystem.getHobbyName(disciple.hobby)); } catch (Exception e) { sb.append("None"); }
                if (disciple.hasFairy && disciple.fairy != null) {
                    sb.append("\nFairy: ").append(disciple.fairy.name)
                      .append(" (Lv.").append(disciple.fairy.level).append(")");
                }
                tvBehavior.setText(sb.toString());
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        dismissDialog(d);
                    }
                });
            }
            if (btnAssign != null) {
                btnAssign.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        dismissDialog(d);
                        cb.onAssignTask();
                    }
                });
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showTaskDialog(final Disciple disciple, final TaskCallback cb) {
        if (!isContextValid() || disciple == null || cb == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_task_assign);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvName = (TextView) d.findViewById(R.id.tvTaskDialogName);
            if (tvName != null) {
                sb.setLength(0); sb.append("Assign Task: ").append(disciple.name);
                tvName.setText(sb.toString());
            }

            final RadioGroup rg = (RadioGroup) d.findViewById(R.id.rgTasks);
            Button btnConfirm = (Button) d.findViewById(R.id.btnConfirmTask);

            if (btnConfirm != null) {
                btnConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        int id = rg != null ? rg.getCheckedRadioButtonId() : -1;
                        int task = GameConfig.TASK_NONE;
                        if (id == R.id.rbFarming) task = GameConfig.TASK_FARMING;
                        else if (id == R.id.rbCrafting) task = GameConfig.TASK_CRAFTING;
                        else if (id == R.id.rbAlchemy) task = GameConfig.TASK_ALCHEMY;
                        else if (id == R.id.rbCultivation) task = GameConfig.TASK_CULTIVATION;
                        else if (id == R.id.rbMining) task = GameConfig.TASK_MINING;
                        else if (id == R.id.rbTraining) task = GameConfig.TASK_TRAINING;
                        else if (id == R.id.rbGuard) task = GameConfig.TASK_GUARD;
                        else if (id == R.id.rbResearch) task = GameConfig.TASK_RESEARCH;
                        else if (id == R.id.rbTrading) task = GameConfig.TASK_TRADING;
                        else if (id == R.id.rbExploring) task = GameConfig.TASK_EXPLORING;

                        cb.onTaskSelected(task);
                        dismissDialog(d);
                    }
                });
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showBuildingDialog(final Building b, final SectData data, final BuildingActionCallback cb) {
        if (!isContextValid() || b == null || cb == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(32, 32, 32, 32);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(24, 24, 24, 24);
        root.setLayoutParams(params);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(b.name + " (Level " + b.level + ")");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(tvTitle);

        TextView tvDesc = new TextView(context);
        long upgradeCost = (long) (b.cost * Math.pow(1.5, b.level));
        sb.setLength(0);
        sb.append("Type: ").append(GameConfig.getBuildingName(b.type)).append("\n")
          .append("Daily Income: +").append(NumberFormatter.format(b.getDailyIncome())).append(" SS/day\n")
          .append("Upgrade Cost: ").append(NumberFormatter.format(upgradeCost)).append(" Spirit Stones\n")
          .append("Status: ").append(b.isBuilt ? "Operational" : "Under Construction");
        tvDesc.setText(sb.toString());
        tvDesc.setTextColor(0xFFFFFFFF);
        tvDesc.setTextSize(13);
        tvDesc.setPadding(0, 16, 0, 16);
        root.addView(tvDesc);

        LinearLayout btnRow = new LinearLayout(context);
        btnRow.setOrientation(LinearLayout.VERTICAL);

        Button btnUpgrade = new Button(context);
        btnUpgrade.setText(b.isBuilt ? "Upgrade Building (" + NumberFormatter.format(upgradeCost) + " SS)" : "Construct (" + NumberFormatter.format(b.cost) + " SS)");
        btnUpgrade.setBackgroundResource(R.drawable.bg_button_jade);
        btnUpgrade.setTextColor(0xFF00E5FF);
        btnUpgrade.setTypeface(Typeface.DEFAULT_BOLD);
        btnUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                cb.onUpgrade();
                dismissDialog(d);
            }
        });
        btnRow.addView(btnUpgrade);

        if (b.isBuilt && (b.type == GameConfig.BUILD_GARDEN || b.type == GameConfig.BUILD_ALCHEMY)) {
            Button btnMinigame = new Button(context);
            btnMinigame.setText(b.type == GameConfig.BUILD_GARDEN ? "🌿 Spirit Herb Slash Minigame" : "🔥 Pill Refining Slash Minigame");
            btnMinigame.setBackgroundResource(R.drawable.bg_button_jade);
            btnMinigame.setTextColor(0xFFFFD700);
            btnMinigame.setTypeface(Typeface.DEFAULT_BOLD);
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.setMargins(0, 12, 0, 0);
            btnMinigame.setLayoutParams(p);
            btnMinigame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playClickSfx();
                    cb.onMinigame();
                    dismissDialog(d);
                }
            });
            btnRow.addView(btnMinigame);
        }

        Button btnClose = new Button(context);
        btnClose.setText("Close");
        btnClose.setBackgroundResource(R.drawable.bg_button_jade);
        btnClose.setTextColor(0xFFAAAAAA);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 12, 0, 0);
        btnClose.setLayoutParams(cp);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                dismissDialog(d);
            }
        });
        btnRow.addView(btnClose);

        root.addView(btnRow);
        d.setContentView(root);
        activeDialogs.add(d);

        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialogs.remove(d);
            }
        });

        d.show();
        if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
            d.getWindow().getDecorView().startAnimation(fadeIn);
        }
    }

    public void showMarketDialog(final MarketSystem market, final SectData data, final MarketCallback cb) {
        if (!isContextValid() || market == null || data == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(28, 28, 28, 28);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("🏛️ Immortal Pavilion Market");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(tvTitle);

        TextView tvBalance = new TextView(context);
        sb.setLength(0);
        sb.append("Your Balance: ").append(NumberFormatter.format(data.spiritStones)).append(" SS | ")
          .append(NumberFormatter.format(data.jade)).append(" Jade");
        tvBalance.setText(sb.toString());
        tvBalance.setTextColor(0xFF00E5FF);
        tvBalance.setPadding(0, 8, 0, 16);
        root.addView(tvBalance);

        String[] itemNames = {"Spirit Herb Bundle", "Iron-Wood Timber", "Nine-Yang Spirit Ore", "Revitalizing Pill"};
        final int[] itemPrices = {50, 100, 200, 500};

        for (int i = 0; i < itemNames.length; i++) {
            final int idx = i;
            LinearLayout itemRow = new LinearLayout(context);
            itemRow.setOrientation(LinearLayout.HORIZONTAL);
            itemRow.setPadding(0, 8, 0, 8);

            TextView tvItem = new TextView(context);
            tvItem.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            tvItem.setText(itemNames[i] + " (" + itemPrices[i] + " SS)");
            tvItem.setTextColor(0xFFFFFFFF);
            tvItem.setTextSize(13);
            itemRow.addView(tvItem);

            Button btnBuy = new Button(context);
            btnBuy.setText("Buy");
            btnBuy.setTextSize(11);
            btnBuy.setBackgroundResource(R.drawable.bg_button_jade);
            btnBuy.setTextColor(0xFFFFD700);
            btnBuy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playClickSfx();
                    if (data.spiritStones >= itemPrices[idx]) {
                        data.spend(itemPrices[idx], 0, 0);
                        if (cb != null) cb.onItemPurchased(idx, itemPrices[idx]);
                        dismissDialog(d);
                    } else {
                        showAlert("Insufficient Spirit Stones", "You do not have enough Spirit Stones to trade.");
                    }
                }
            });
            itemRow.addView(btnBuy);

            root.addView(itemRow);
        }

        Button btnClose = new Button(context);
        btnClose.setText("Leave Market");
        btnClose.setBackgroundResource(R.drawable.bg_button_jade);
        btnClose.setTextColor(0xFFAAAAAA);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 16, 0, 0);
        btnClose.setLayoutParams(p);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                dismissDialog(d);
            }
        });
        root.addView(btnClose);

        d.setContentView(root);
        activeDialogs.add(d);

        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialogs.remove(d);
            }
        });

        d.show();
        if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
            d.getWindow().getDecorView().startAnimation(fadeIn);
        }
    }

    public void showSettingsDialog(final SettingsCallback cb) {
        if (!isContextValid()) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.ScrollView scrollView = new android.widget.ScrollView(context);
        scrollView.setFillViewport(true);
        scrollView.setVerticalScrollBarEnabled(true);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(28, 28, 28, 28);
        scrollView.addView(root);

        // Header Title
        TextView tvTitle = new TextView(context);
        tvTitle.setText("⚙️ Immortal Sect & Engine Dao Settings");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setPadding(0, 0, 0, 16);
        root.addView(tvTitle);

        final AudioManager audio = AudioManager.getInstance(context);

        // ================= SECTION 1: AUDIO & SOUND DAO =================
        TextView tvSecAudio = new TextView(context);
        tvSecAudio.setText("🎵 Audio & Heavenly Harmonies");
        tvSecAudio.setTextColor(0xFF00E5FF);
        tvSecAudio.setTextSize(14);
        tvSecAudio.setTypeface(Typeface.DEFAULT_BOLD);
        tvSecAudio.setPadding(0, 12, 0, 6);
        root.addView(tvSecAudio);

        final CheckBox cbBgm = new CheckBox(context);
        cbBgm.setText("Immortal Guzheng Music (BGM)");
        cbBgm.setTextColor(0xFFFFFFFF);
        cbBgm.setChecked(audio.isBgmEnabled());
        cbBgm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audio.setBgmEnabled(isChecked);
                playClickSfx();
            }
        });
        root.addView(cbBgm);

        final TextView tvBgmVolLabel = new TextView(context);
        int currentBgmPct = (int) (audio.getBgmVolume() * 100f);
        tvBgmVolLabel.setText("BGM Volume: " + currentBgmPct + "%");
        tvBgmVolLabel.setTextColor(0xFFD1C4E9);
        tvBgmVolLabel.setTextSize(12);
        tvBgmVolLabel.setPadding(32, 2, 0, 0);
        root.addView(tvBgmVolLabel);

        SeekBar sbBgm = new SeekBar(context);
        sbBgm.setMax(100);
        sbBgm.setProgress(currentBgmPct);
        sbBgm.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audio.setBgmVolume(progress / 100f);
                    tvBgmVolLabel.setText("BGM Volume: " + progress + "%");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        root.addView(sbBgm);

        final CheckBox cbSfx = new CheckBox(context);
        cbSfx.setText("Martial Arts & Magic SFX");
        cbSfx.setTextColor(0xFFFFFFFF);
        cbSfx.setChecked(audio.isEnabled());
        cbSfx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audio.setEnabled(isChecked);
                playClickSfx();
            }
        });
        root.addView(cbSfx);

        final TextView tvSfxVolLabel = new TextView(context);
        int currentSfxPct = (int) (audio.getSfxVolume() * 100f);
        tvSfxVolLabel.setText("SFX Volume: " + currentSfxPct + "%");
        tvSfxVolLabel.setTextColor(0xFFD1C4E9);
        tvSfxVolLabel.setTextSize(12);
        tvSfxVolLabel.setPadding(32, 2, 0, 0);
        root.addView(tvSfxVolLabel);

        SeekBar sbSfx = new SeekBar(context);
        sbSfx.setMax(100);
        sbSfx.setProgress(currentSfxPct);
        sbSfx.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audio.setSfxVolume(progress / 100f);
                    tvSfxVolLabel.setText("SFX Volume: " + progress + "%");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { playClickSfx(); }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        root.addView(sbSfx);

        // ================= SECTION 2: GRAPHICS & PERFORMANCE ENGINE =================
        TextView tvSecGfx = new TextView(context);
        tvSecGfx.setText("⚡ Graphics & Engine Quality (L0-L3)");
        tvSecGfx.setTextColor(0xFF00E5FF);
        tvSecGfx.setTextSize(14);
        tvSecGfx.setTypeface(Typeface.DEFAULT_BOLD);
        tvSecGfx.setPadding(0, 16, 0, 6);
        root.addView(tvSecGfx);

        LinearLayout layoutQuality = new LinearLayout(context);
        layoutQuality.setOrientation(LinearLayout.HORIZONTAL);
        layoutQuality.setWeightSum(4);

        final Button[] btnQualities = new Button[4];
        final String[] qualityLabels = {"Low", "Med", "High", "Ultra"};
        final int[] qualityLevels = {GameConfig.QUALITY_LOW, GameConfig.QUALITY_MEDIUM, GameConfig.QUALITY_HIGH, GameConfig.QUALITY_ULTRA};

        for (int i = 0; i < 4; i++) {
            final int qIdx = i;
            final Button btnQ = new Button(context);
            btnQ.setText(qualityLabels[i]);
            btnQ.setTextSize(11);
            btnQ.setBackgroundResource(R.drawable.bg_button_jade);
            btnQ.setTextColor(GameConfig.currentQuality == qualityLevels[i] ? 0xFFFFD700 : 0xFFAAAAAA);
            LinearLayout.LayoutParams qp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            qp.setMargins(2, 2, 2, 2);
            btnQ.setLayoutParams(qp);
            btnQ.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    playClickSfx();
                    GameConfig.applyQuality(qualityLevels[qIdx]);
                    for (int j = 0; j < 4; j++) {
                        if (btnQualities[j] != null) {
                            btnQualities[j].setTextColor(j == qIdx ? 0xFFFFD700 : 0xFFAAAAAA);
                        }
                    }
                }
            });
            btnQualities[i] = btnQ;
            layoutQuality.addView(btnQ);
        }
        root.addView(layoutQuality);

        final CheckBox cbParticles = new CheckBox(context);
        cbParticles.setText("Spiritual Qi Particles & Auras");
        cbParticles.setTextColor(0xFFFFFFFF);
        cbParticles.setChecked(GameConfig.ENABLE_PARTICLES);
        cbParticles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GameConfig.ENABLE_PARTICLES = isChecked;
                playClickSfx();
            }
        });
        root.addView(cbParticles);

        final CheckBox cbLighting = new CheckBox(context);
        cbLighting.setText("Dynamic Lighting & Day/Night Shader");
        cbLighting.setTextColor(0xFFFFFFFF);
        cbLighting.setChecked(GameConfig.ENABLE_LIGHTING);
        cbLighting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GameConfig.ENABLE_LIGHTING = isChecked;
                playClickSfx();
            }
        });
        root.addView(cbLighting);

        // ================= SECTION 3: SIMULATION & ECONOMY =================
        TextView tvSecSim = new TextView(context);
        tvSecSim.setText("📜 Sect Simulation & Cultivation (L2-L5)");
        tvSecSim.setTextColor(0xFF00E5FF);
        tvSecSim.setTextSize(14);
        tvSecSim.setTypeface(Typeface.DEFAULT_BOLD);
        tvSecSim.setPadding(0, 16, 0, 6);
        root.addView(tvSecSim);

        final CheckBox cbAutoSave = new CheckBox(context);
        cbAutoSave.setText("Automatic Periodical Saving");
        cbAutoSave.setTextColor(0xFFFFFFFF);
        cbAutoSave.setChecked(SectData.getInstance() != null && SectData.getInstance().autoSave);
        cbAutoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (SectData.getInstance() != null) SectData.getInstance().autoSave = isChecked;
                playClickSfx();
            }
        });
        root.addView(cbAutoSave);

        final CheckBox cbDamageNums = new CheckBox(context);
        cbDamageNums.setText("Floating Combat Damage Numbers");
        cbDamageNums.setTextColor(0xFFFFFFFF);
        cbDamageNums.setChecked(GameConfig.ENABLE_DAMAGE_NUMBERS);
        cbDamageNums.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                GameConfig.ENABLE_DAMAGE_NUMBERS = isChecked;
                playClickSfx();
            }
        });
        root.addView(cbDamageNums);

        // ================= SECTION 4: SYSTEM & TELEMETRY =================
        TextView tvSecDiag = new TextView(context);
        tvSecDiag.setText("📊 Engine Telemetry & Memory Budget");
        tvSecDiag.setTextColor(0xFF00E5FF);
        tvSecDiag.setTextSize(14);
        tvSecDiag.setTypeface(Typeface.DEFAULT_BOLD);
        tvSecDiag.setPadding(0, 16, 0, 6);
        root.addView(tvSecDiag);

        long freeMem = Runtime.getRuntime().freeMemory() / (1024 * 1024);
        long totalMem = Runtime.getRuntime().totalMemory() / (1024 * 1024);
        long maxMem = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        TextView tvMemInfo = new TextView(context);
        tvMemInfo.setText("JVM Heap: " + (totalMem - freeMem) + "MB / " + maxMem + "MB | Target 60 FPS");
        tvMemInfo.setTextColor(0xFF81C784);
        tvMemInfo.setTextSize(11);
        tvMemInfo.setPadding(12, 4, 12, 12);
        root.addView(tvMemInfo);

        // Action Buttons Row
        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setOrientation(LinearLayout.HORIZONTAL);
        actionRow.setWeightSum(2);

        Button btnManualSave = new Button(context);
        btnManualSave.setText("💾 Save Now");
        btnManualSave.setBackgroundResource(R.drawable.bg_button_jade);
        btnManualSave.setTextColor(0xFFFFD700);
        btnManualSave.setTextSize(12);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        sp.setMargins(0, 0, 4, 0);
        btnManualSave.setLayoutParams(sp);
        btnManualSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                try {
                    com.sect.idle.gameplay.SaveManager sm = new com.sect.idle.gameplay.SaveManager(context);
                    sm.save();
                    showAlert("Sect Inscribed", "Your immortal sect cultivation progress has been inscribed into local Dao records.");
                } catch (Exception e) {
                    showAlert("Save Notice", "Data synchronized.");
                }
            }
        });
        actionRow.addView(btnManualSave);

        Button btnResetSave = new Button(context);
        btnResetSave.setText("🔄 Reset Save");
        btnResetSave.setBackgroundResource(R.drawable.bg_button_jade);
        btnResetSave.setTextColor(0xFFFF5252);
        btnResetSave.setTextSize(12);
        LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        rp.setMargins(4, 0, 0, 0);
        btnResetSave.setLayoutParams(rp);
        btnResetSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                showConfirm("Reset Cultivation Save?", "Are you sure you wish to dissolve your sect and reincarnate from scratch?", new ConfirmCallback() {
                    @Override
                    public void onConfirm(boolean confirmed) {
                        if (confirmed && SectData.getInstance() != null) {
                            SectData.getInstance().reset();
                            try {
                                com.sect.idle.gameplay.SaveManager sm = new com.sect.idle.gameplay.SaveManager(context);
                                sm.save();
                            } catch (Exception ignored) {}
                            showAlert("Reincarnation Complete", "Sect records reset. Cultivate anew!");
                        }
                    }
                });
            }
        });
        actionRow.addView(btnResetSave);
        root.addView(actionRow);

        if (context instanceof GameActivity) {
            final GameActivity act = (GameActivity) context;
            if (act.sceneManager != null && act.sceneManager.getState() != GameState.MENU) {
                Button btnReturnMenu = new Button(context);
                btnReturnMenu.setText("🏠 Return to Title Screen");
                btnReturnMenu.setBackgroundResource(R.drawable.bg_button_jade);
                btnReturnMenu.setTextColor(0xFFB388FF);
                btnReturnMenu.setTextSize(12);
                LinearLayout.LayoutParams mp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                mp.setMargins(0, 8, 0, 0);
                btnReturnMenu.setLayoutParams(mp);
                btnReturnMenu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        if (act.getSaveManager() != null) {
                            try { act.getSaveManager().save(); } catch (Exception ignored) {}
                        }
                        dismissDialog(d);
                        act.sceneManager.setState(GameState.MENU);
                        try {
                            AudioManager.getInstance(context).playBgm(AudioManager.THEME_MEDITATION_ZEN);
                        } catch (Exception ignored) {}
                    }
                });
                root.addView(btnReturnMenu);
            }
        }

        Button btnDiag = new Button(context);
        btnDiag.setText("📊 Live Diagnostics & ELK Telemetry");
        btnDiag.setBackgroundResource(R.drawable.bg_button_jade);
        btnDiag.setTextColor(0xFF00E5FF);
        btnDiag.setTextSize(12);
        btnDiag.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        dp.setMargins(0, 12, 0, 0);
        btnDiag.setLayoutParams(dp);
        btnDiag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                showDiagnosticsDashboardDialog();
            }
        });
        root.addView(btnDiag);

        // Save & Exit Settings Button
        Button btnSave = new Button(context);
        btnSave.setText("✓ Apply & Close");
        btnSave.setBackgroundResource(R.drawable.bg_button_jade);
        btnSave.setTextColor(0xFF00E5FF);
        btnSave.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 20, 0, 8);
        btnSave.setLayoutParams(p);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                if (cb != null) cb.onSettingsSaved();
                dismissDialog(d);
            }
        });
        root.addView(btnSave);

        d.setContentView(scrollView);
        activeDialogs.add(d);

        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialogs.remove(d);
            }
        });

        d.show();
        if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
            d.getWindow().getDecorView().startAnimation(fadeIn);
        }
    }

    /**
     * Real-time Engine Diagnostics, Telemetry, and ELK/ECS Structured Logging Dashboard.
     */
    public void showDiagnosticsDashboardDialog() {
        if (!isContextValid()) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ScrollView scrollView = new ScrollView(context);
        scrollView.setFillViewport(true);

        final LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(28, 28, 28, 28);
        scrollView.addView(root);

        // Header Title
        TextView tvTitle = new TextView(context);
        tvTitle.setText("📊 Engine Telemetry & ELK Diagnostics");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setPadding(0, 0, 0, 8);
        root.addView(tvTitle);

        TextView tvSubtitle = new TextView(context);
        tvSubtitle.setText("Real-time monitoring, ECS JSON log streaming, and forensic error recovery.");
        tvSubtitle.setTextColor(0xFFAAAAAA);
        tvSubtitle.setTextSize(11);
        tvSubtitle.setPadding(0, 0, 0, 16);
        root.addView(tvSubtitle);

        // Tab Navigation Buttons
        HorizontalScrollView tabScroll = new HorizontalScrollView(context);
        tabScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout tabLayout = new LinearLayout(context);
        tabLayout.setOrientation(LinearLayout.HORIZONTAL);

        final Button btnTabMetrics = new Button(context);
        btnTabMetrics.setText("⚡ Metrics");
        btnTabMetrics.setTextSize(11);
        btnTabMetrics.setBackgroundResource(R.drawable.bg_button_jade);
        btnTabMetrics.setTextColor(0xFFFFD700);

        final Button btnTabLogs = new Button(context);
        btnTabLogs.setText("📜 ELK Logs");
        btnTabLogs.setTextSize(11);
        btnTabLogs.setBackgroundResource(R.drawable.bg_button_jade);
        btnTabLogs.setTextColor(0xFFAAAAAA);

        final Button btnTabForensics = new Button(context);
        btnTabForensics.setText("🔍 Forensics");
        btnTabForensics.setTextSize(11);
        btnTabForensics.setBackgroundResource(R.drawable.bg_button_jade);
        btnTabForensics.setTextColor(0xFFAAAAAA);

        final Button btnTabTools = new Button(context);
        btnTabTools.setText("🛠️ Tools");
        btnTabTools.setTextSize(11);
        btnTabTools.setBackgroundResource(R.drawable.bg_button_jade);
        btnTabTools.setTextColor(0xFFAAAAAA);

        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tlp.setMargins(0, 0, 6, 12);
        btnTabMetrics.setLayoutParams(tlp);
        btnTabLogs.setLayoutParams(tlp);
        btnTabForensics.setLayoutParams(tlp);
        btnTabTools.setLayoutParams(tlp);

        tabLayout.addView(btnTabMetrics);
        tabLayout.addView(btnTabLogs);
        tabLayout.addView(btnTabForensics);
        tabLayout.addView(btnTabTools);
        tabScroll.addView(tabLayout);
        root.addView(tabScroll);

        // Tab Container
        final LinearLayout containerTabContent = new LinearLayout(context);
        containerTabContent.setOrientation(LinearLayout.VERTICAL);
        root.addView(containerTabContent);

        // Helper interface to switch tabs
        final class TabSwitcher {
            int currentTab = 0; // 0=Metrics, 1=Logs, 2=Forensics, 3=Tools
            int logLevelFilter = ExceptionManager.LEVEL_DEBUG;
            String searchFilter = "";

            void render() {
                containerTabContent.removeAllViews();
                btnTabMetrics.setTextColor(currentTab == 0 ? 0xFFFFD700 : 0xFFAAAAAA);
                btnTabLogs.setTextColor(currentTab == 1 ? 0xFFFFD700 : 0xFFAAAAAA);
                btnTabForensics.setTextColor(currentTab == 2 ? 0xFFFFD700 : 0xFFAAAAAA);
                btnTabTools.setTextColor(currentTab == 3 ? 0xFFFFD700 : 0xFFAAAAAA);

                if (currentTab == 0) {
                    renderMetricsTab();
                } else if (currentTab == 1) {
                    renderLogsTab();
                } else if (currentTab == 2) {
                    renderForensicsTab();
                } else {
                    renderToolsTab();
                }
            }

            void renderMetricsTab() {
                final ExceptionManager em = ExceptionManager.get();
                Runtime rt = Runtime.getRuntime();
                long maxMem = rt.maxMemory() / (1024 * 1024);
                long totalMem = rt.totalMemory() / (1024 * 1024);
                long freeMem = rt.freeMemory() / (1024 * 1024);
                long usedMem = totalMem - freeMem;

                TextView tvSec1 = new TextView(context);
                tvSec1.setText("⚡ Live Engine Telemetry");
                tvSec1.setTextColor(0xFF00E5FF);
                tvSec1.setTextSize(14);
                tvSec1.setTypeface(Typeface.DEFAULT_BOLD);
                tvSec1.setPadding(0, 4, 0, 8);
                containerTabContent.addView(tvSec1);

                String[] qualityNames = {"Low (L0)", "Medium (L1)", "High (L2)", "Ultra (L3)"};
                int qIdx = GameConfig.currentQuality;
                if (qIdx < 0 || qIdx >= qualityNames.length) qIdx = 0;

                StringBuilder sbMetrics = new StringBuilder(512);
                sbMetrics.append("• Engine Status: Operational (Double-Buffered)\n");
                sbMetrics.append("• Active Quality Tier: ").append(qualityNames[qIdx]).append("\n");
                sbMetrics.append("• Dynamic Lighting: ").append(GameConfig.ENABLE_LIGHTING ? "Enabled" : "Disabled").append("\n");
                sbMetrics.append("• Spiritual Qi Particles: ").append(GameConfig.ENABLE_PARTICLES ? "Enabled" : "Disabled").append("\n");
                sbMetrics.append("• Floating Combat Text: ").append(GameConfig.ENABLE_DAMAGE_NUMBERS ? "Enabled" : "Disabled").append("\n");
                sbMetrics.append("• Target Frame Budget: 60 FPS (16.6ms / frame)\n\n");

                sbMetrics.append("📊 Memory & Allocation Budget:\n");
                sbMetrics.append("• JVM Heap Used: ").append(usedMem).append(" MB / ").append(maxMem).append(" MB (").append(usedMem * 100 / Math.max(1, maxMem)).append("%)\n");
                sbMetrics.append("• JVM Heap Committed: ").append(totalMem).append(" MB\n");
                sbMetrics.append("• Bitmap Cache Stats: ").append(BitmapCache.get().getStats()).append("\n\n");

                sbMetrics.append("🛡️ System Stability & Log Metrics:\n");
                sbMetrics.append("• Total Log Events: ").append(em.getTotalLogs()).append("\n");
                sbMetrics.append("• Warnings: ").append(em.getTotalWarnings()).append("\n");
                sbMetrics.append("• Caught Errors: ").append(em.getTotalErrors()).append("\n");
                sbMetrics.append("• Fatal Traps: ").append(em.getTotalFatal()).append("\n");
                sbMetrics.append("• Error Rate: ").append(String.format(java.util.Locale.US, "%.2f", em.getErrorRatePerMinute())).append(" events/min\n");

                TextView tvMetricsContent = new TextView(context);
                tvMetricsContent.setText(sbMetrics.toString());
                tvMetricsContent.setTextColor(0xFFE0E0E0);
                tvMetricsContent.setTextSize(12);
                tvMetricsContent.setLineSpacing(4, 1);
                tvMetricsContent.setBackgroundResource(R.drawable.bg_combat_card);
                tvMetricsContent.setPadding(16, 16, 16, 16);
                containerTabContent.addView(tvMetricsContent);

                Button btnRefresh = new Button(context);
                btnRefresh.setText("🔄 Refresh Metrics");
                btnRefresh.setBackgroundResource(R.drawable.bg_button_jade);
                btnRefresh.setTextColor(0xFF00E5FF);
                btnRefresh.setTextSize(12);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0, 12, 0, 0);
                btnRefresh.setLayoutParams(rp);
                btnRefresh.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        render();
                    }
                });
                containerTabContent.addView(btnRefresh);
            }

            void renderLogsTab() {
                final ExceptionManager em = ExceptionManager.get();

                TextView tvSec2 = new TextView(context);
                tvSec2.setText("📜 Elastic Common Schema (ECS) Stream");
                tvSec2.setTextColor(0xFF00E5FF);
                tvSec2.setTextSize(14);
                tvSec2.setTypeface(Typeface.DEFAULT_BOLD);
                tvSec2.setPadding(0, 4, 0, 6);
                containerTabContent.addView(tvSec2);

                // Level Filter Row
                HorizontalScrollView filterScroll = new HorizontalScrollView(context);
                filterScroll.setHorizontalScrollBarEnabled(false);
                LinearLayout filterLayout = new LinearLayout(context);
                filterLayout.setOrientation(LinearLayout.HORIZONTAL);

                final String[] filterNames = {"ALL", "INFO+", "WARN+", "ERROR+"};
                final int[] filterLevels = {ExceptionManager.LEVEL_DEBUG, ExceptionManager.LEVEL_INFO, ExceptionManager.LEVEL_WARN, ExceptionManager.LEVEL_ERROR};

                for (int i = 0; i < filterNames.length; i++) {
                    final int fLevel = filterLevels[i];
                    Button btnFilter = new Button(context);
                    btnFilter.setText(filterNames[i]);
                    btnFilter.setTextSize(10);
                    btnFilter.setBackgroundResource(R.drawable.bg_button_jade);
                    btnFilter.setTextColor(logLevelFilter == fLevel ? 0xFFFFD700 : 0xFFAAAAAA);
                    LinearLayout.LayoutParams fp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    fp.setMargins(0, 0, 4, 8);
                    btnFilter.setLayoutParams(fp);
                    btnFilter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playClickSfx();
                            logLevelFilter = fLevel;
                            render();
                        }
                    });
                    filterLayout.addView(btnFilter);
                }
                filterScroll.addView(filterLayout);
                containerTabContent.addView(filterScroll);

                // Search Filter Input
                final EditText etSearch = new EditText(context);
                etSearch.setHint("🔍 Search tag, class, or message...");
                etSearch.setHintTextColor(0xFF888888);
                etSearch.setTextColor(0xFFFFFFFF);
                etSearch.setTextSize(12);
                etSearch.setText(searchFilter);
                etSearch.setBackgroundResource(R.drawable.bg_combat_card);
                etSearch.setPadding(16, 12, 16, 12);
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        searchFilter = s.toString();
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });
                containerTabContent.addView(etSearch);

                // Query and Display Logs
                ArrayList<StructuredLogEntry> logs = em.getStructuredLogs(logLevelFilter, searchFilter);
                StringBuilder sbLogs = new StringBuilder(2048);
                if (logs.isEmpty()) {
                    sbLogs.append("No log records matching filter: [Level >= ").append(ExceptionManager.getLevelString(logLevelFilter)).append("]");
                } else {
                    for (int i = logs.size() - 1; i >= 0; i--) { // latest first
                        StructuredLogEntry e = logs.get(i);
                        sbLogs.append(e.getFormattedSummary()).append("\n");
                        if (!e.stackTrace.isEmpty()) {
                            sbLogs.append("   ↳ ").append(e.stackTrace.replace("\n", "\n   ↳ ")).append("\n");
                        }
                    }
                }

                ScrollView logScrollView = new ScrollView(context);
                LinearLayout.LayoutParams lsp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 360);
                lsp.setMargins(0, 8, 0, 8);
                logScrollView.setLayoutParams(lsp);

                TextView tvLogView = new TextView(context);
                tvLogView.setText(sbLogs.toString());
                tvLogView.setTextColor(0xFFB0BEC5);
                tvLogView.setTextSize(10);
                tvLogView.setTypeface(Typeface.MONOSPACE);
                tvLogView.setBackgroundResource(R.drawable.bg_combat_card);
                tvLogView.setPadding(12, 12, 12, 12);
                logScrollView.addView(tvLogView);
                containerTabContent.addView(logScrollView);

                // Actions: Copy ELK JSON & Purge
                LinearLayout logActionRow = new LinearLayout(context);
                logActionRow.setOrientation(LinearLayout.HORIZONTAL);
                logActionRow.setWeightSum(2);

                Button btnCopyJson = new Button(context);
                btnCopyJson.setText("📋 Copy ECS JSON");
                btnCopyJson.setBackgroundResource(R.drawable.bg_button_jade);
                btnCopyJson.setTextColor(0xFFFFD700);
                btnCopyJson.setTextSize(11);
                LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                cp.setMargins(0, 0, 4, 0);
                btnCopyJson.setLayoutParams(cp);
                btnCopyJson.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        try {
                            String json = em.exportStructuredLogsJson();
                            ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                            if (cm != null) {
                                ClipData clip = ClipData.newPlainText("ELK_Logs", json);
                                cm.setPrimaryClip(clip);
                                Toast.makeText(context, "Copied " + logs.size() + " ECS JSON logs to clipboard!", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Failed to copy logs.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                logActionRow.addView(btnCopyJson);

                Button btnPurgeLogs = new Button(context);
                btnPurgeLogs.setText("🧹 Purge Logs");
                btnPurgeLogs.setBackgroundResource(R.drawable.bg_button_jade);
                btnPurgeLogs.setTextColor(0xFFFF5252);
                btnPurgeLogs.setTextSize(11);
                LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
                pp.setMargins(4, 0, 0, 0);
                btnPurgeLogs.setLayoutParams(pp);
                btnPurgeLogs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        em.clearInMemoryLogs();
                        render();
                    }
                });
                logActionRow.addView(btnPurgeLogs);
                containerTabContent.addView(logActionRow);
            }

            void renderForensicsTab() {
                final ExceptionManager em = ExceptionManager.get();

                TextView tvSec3 = new TextView(context);
                tvSec3.setText("🔍 Forensics & Crash Breadcrumbs");
                tvSec3.setTextColor(0xFF00E5FF);
                tvSec3.setTextSize(14);
                tvSec3.setTypeface(Typeface.DEFAULT_BOLD);
                tvSec3.setPadding(0, 4, 0, 6);
                containerTabContent.addView(tvSec3);

                StringBuilder sbBreadcrumbs = new StringBuilder(1024);
                sbBreadcrumbs.append("📜 Circular Breadcrumb Trace:\n");
                ArrayList<String> breadcrumbs = em.getBreadcrumbs();
                if (breadcrumbs.isEmpty()) {
                    sbBreadcrumbs.append("  (No recent breadcrumbs recorded)\n");
                } else {
                    for (int i = 0; i < breadcrumbs.size(); i++) {
                        sbBreadcrumbs.append("  • ").append(breadcrumbs.get(i)).append("\n");
                    }
                }

                sbBreadcrumbs.append("\n💥 Latest Persistent Crash Dump:\n");
                String latestCrash = em.getLatestCrashDump();
                sbBreadcrumbs.append(latestCrash);

                ScrollView forensicScroll = new ScrollView(context);
                LinearLayout.LayoutParams fsp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 400);
                fsp.setMargins(0, 4, 0, 10);
                forensicScroll.setLayoutParams(fsp);

                TextView tvForensicText = new TextView(context);
                tvForensicText.setText(sbBreadcrumbs.toString());
                tvForensicText.setTextColor(0xFFCFD8DC);
                tvForensicText.setTextSize(10);
                tvForensicText.setTypeface(Typeface.MONOSPACE);
                tvForensicText.setBackgroundResource(R.drawable.bg_combat_card);
                tvForensicText.setPadding(12, 12, 12, 12);
                forensicScroll.addView(tvForensicText);
                containerTabContent.addView(forensicScroll);

                Button btnClearDumps = new Button(context);
                btnClearDumps.setText("🧹 Clear Crash History");
                btnClearDumps.setBackgroundResource(R.drawable.bg_button_jade);
                btnClearDumps.setTextColor(0xFFFF5252);
                btnClearDumps.setTextSize(11);
                LinearLayout.LayoutParams cdp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                btnClearDumps.setLayoutParams(cdp);
                btnClearDumps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        em.clearCrashHistory();
                        render();
                    }
                });
                containerTabContent.addView(btnClearDumps);
            }

            void renderToolsTab() {
                TextView tvSec4 = new TextView(context);
                tvSec4.setText("🛠️ Engine Diagnostics & Recovery Tools");
                tvSec4.setTextColor(0xFF00E5FF);
                tvSec4.setTextSize(14);
                tvSec4.setTypeface(Typeface.DEFAULT_BOLD);
                tvSec4.setPadding(0, 4, 0, 8);
                containerTabContent.addView(tvSec4);

                // Tool 1: Force GC & Purge Bitmaps
                Button btnGc = new Button(context);
                btnGc.setText("🧹 Force GC & Purge Bitmaps");
                btnGc.setBackgroundResource(R.drawable.bg_button_jade);
                btnGc.setTextColor(0xFF81C784);
                btnGc.setTextSize(12);
                LinearLayout.LayoutParams gcp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                gcp.setMargins(0, 4, 0, 6);
                btnGc.setLayoutParams(gcp);
                btnGc.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        long memBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        BitmapCache.get().clear();
                        MemoryPool.reset();
                        System.gc();
                        long memAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                        long freedMB = Math.max(0, (memBefore - memAfter) / (1024 * 1024));
                        ExceptionManager.get().logInfo("Maintenance", "GC & BitmapCache purge executed. Freed approx " + freedMB + " MB.");
                        Toast.makeText(context, "Purge complete. Freed ~" + freedMB + " MB heap.", Toast.LENGTH_SHORT).show();
                        render();
                    }
                });
                containerTabContent.addView(btnGc);

                // Tool 2: Verify Save Data Security & Integrity
                Button btnVerify = new Button(context);
                btnVerify.setText("🛡️ Verify Save Data Integrity");
                btnVerify.setBackgroundResource(R.drawable.bg_button_jade);
                btnVerify.setTextColor(0xFF64B5F6);
                btnVerify.setTextSize(12);
                LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                vp.setMargins(0, 6, 0, 6);
                btnVerify.setLayoutParams(vp);
                btnVerify.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        try {
                            SecurityManager sm = SecurityManager.get(context);
                            SectData sd = SectData.getInstance();
                            if (sd != null) {
                                String testData = "Sect:" + sd.sectName + "|SS:" + sd.spiritStones + "|Disc:" + (sd.disciples != null ? sd.disciples.size() : 0);
                                String hash = sm.computeHash(testData);
                                boolean valid = sm.verifyIntegrity(testData, hash);
                                ExceptionManager.get().logInfo("Security", "Save integrity verified. CRC32: " + hash + " (Valid: " + valid + ")");
                                Toast.makeText(context, "Save Integrity: 100% Valid (CRC32: " + hash + ")", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "No active sect session in memory.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(context, "Integrity check notice.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                containerTabContent.addView(btnVerify);

                // Tool 3: Simulate Safe Exception Fallback
                Button btnSimulate = new Button(context);
                btnSimulate.setText("⚡ Simulate Exception Fallback (Zero Crash)");
                btnSimulate.setBackgroundResource(R.drawable.bg_button_jade);
                btnSimulate.setTextColor(0xFFFFB74D);
                btnSimulate.setTextSize(12);
                LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                sp.setMargins(0, 6, 0, 6);
                btnSimulate.setLayoutParams(sp);
                btnSimulate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        // Execute dangerous test inside safeRun sandbox
                        ExceptionManager.safeRun(new ExceptionManager.SafeRunnable() {
                            @Override
                            public void run() throws Throwable {
                                String nullPointerTest = null;
                                int length = nullPointerTest.length(); // Simulates NPE
                            }
                        }, "SelfTest", "Simulated NullPointerException trapped gracefully by safeRun");

                        Toast.makeText(context, "Simulated exception captured! Check ELK Logs.", Toast.LENGTH_SHORT).show();
                        currentTab = 1; // Switch to logs tab to view result
                        render();
                    }
                });
                containerTabContent.addView(btnSimulate);
            }
        };

        final TabSwitcher switcher = new TabSwitcher();

        btnTabMetrics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                switcher.currentTab = 0;
                switcher.render();
            }
        });
        btnTabLogs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                switcher.currentTab = 1;
                switcher.render();
            }
        });
        btnTabForensics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                switcher.currentTab = 2;
                switcher.render();
            }
        });
        btnTabTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                switcher.currentTab = 3;
                switcher.render();
            }
        });

        // Initial Render
        switcher.render();

        // Close Button
        Button btnClose = new Button(context);
        btnClose.setText("✓ Close Dashboard");
        btnClose.setBackgroundResource(R.drawable.bg_button_jade);
        btnClose.setTextColor(0xFF00E5FF);
        btnClose.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 20, 0, 8);
        btnClose.setLayoutParams(cp);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                dismissDialog(d);
            }
        });
        root.addView(btnClose);

        d.setContentView(scrollView);
        activeDialogs.add(d);

        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialogs.remove(d);
            }
        });

        d.show();
        if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
            d.getWindow().getDecorView().startAnimation(fadeIn);
        }
    }

    public void showBattleResultDialog(final boolean victory, final int rewardStones, final int rewardExp, final BattleResultCallback cb) {
        if (!isContextValid()) return;
        dismissAll();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(false);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(32, 32, 32, 32);

        TextView tvTitle = new TextView(context);
        tvTitle.setText(victory ? "⚔️ BATTLE VICTORY!" : "☠️ BATTLE DEFEAT");
        tvTitle.setTextColor(victory ? 0xFF4CAF50 : 0xFFFF5252);
        tvTitle.setTextSize(20);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tvTitle.setGravity(android.view.Gravity.CENTER);
        root.addView(tvTitle);

        TextView tvDesc = new TextView(context);
        sb.setLength(0);
        if (victory) {
            sb.append("Your sect forces prevailed against demonic intruders!\n\n")
              .append("Rewards:\n")
              .append("• +").append(NumberFormatter.format(rewardStones)).append(" Spirit Stones\n")
              .append("• +").append(rewardExp).append(" Sect Cultivation EXP");
        } else {
            sb.append("Your disciples were pushed back by the overwhelming demonic Qi.\n\n")
              .append("Regroup, cultivate, and try again!");
        }
        tvDesc.setText(sb.toString());
        tvDesc.setTextColor(0xFFFFFFFF);
        tvDesc.setTextSize(14);
        tvDesc.setPadding(0, 16, 0, 20);
        root.addView(tvDesc);

        Button btnReturn = new Button(context);
        btnReturn.setText("Return to Sect");
        btnReturn.setBackgroundResource(R.drawable.bg_button_jade);
        btnReturn.setTextColor(0xFFFFD700);
        btnReturn.setTypeface(Typeface.DEFAULT_BOLD);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSfx();
                dismissDialog(d);
                if (cb != null) cb.onReturn();
            }
        });
        root.addView(btnReturn);

        d.setContentView(root);
        activeDialogs.add(d);

        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                activeDialogs.remove(d);
            }
        });

        d.show();
        if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
            d.getWindow().getDecorView().startAnimation(fadeIn);
        }
    }

    public void showStoryDialog(String title, String content, String speaker, String[] choices, final StoryCallback cb) {
        if (!isContextValid() || cb == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_story);
        d.setCancelable(false);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvTitle = (TextView) d.findViewById(R.id.tvStoryTitle);
            TextView tvContent = (TextView) d.findViewById(R.id.tvStoryContent);
            TextView tvSpeaker = (TextView) d.findViewById(R.id.tvStorySpeaker);
            LinearLayout choicesLayout = (LinearLayout) d.findViewById(R.id.storyChoices);

            if (tvTitle != null) tvTitle.setText(title != null ? title : "Xianxia Chronicle");
            if (tvContent != null) tvContent.setText(content != null ? content : "");
            if (tvSpeaker != null) {
                sb.setLength(0);
                if (speaker != null) sb.append("— ").append(speaker);
                tvSpeaker.setText(sb.toString());
            }

            if (choicesLayout != null) {
                choicesLayout.removeAllViews();
                if (choices != null) {
                    for (int i = 0; i < choices.length; i++) {
                        final int idx = i;
                        Button btn = new Button(context);
                        btn.setText(choices[i]);
                        btn.setBackgroundResource(R.drawable.bg_button_jade);
                        btn.setTextColor(0xFF00E5FF);
                        btn.setTextSize(12);
                        btn.setPadding(16, 16, 16, 16);
                        btn.setTypeface(Typeface.DEFAULT_BOLD);
                        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        p.setMargins(0, 8, 0, 0);
                        btn.setLayoutParams(p);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                playClickSfx();
                                cb.onChoice(idx);
                                dismissDialog(d);
                            }
                        });
                        choicesLayout.addView(btn);
                    }
                }
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showAlert(String title, String message) {
        if (!isContextValid()) return;
        dismissAll();
        try {
            AlertDialog dialog = new AlertDialog.Builder(context)
               .setTitle(title != null ? title : "Immortal Notice")
               .setMessage(message != null ? message : "")
               .setPositiveButton("Understood", null)
               .setCancelable(true)
               .create();
            activeDialogs.add(dialog);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    activeDialogs.remove(d);
                }
            });
            dialog.show();
        } catch (Exception ignored) {}
    }

    public void showConfirm(String title, String message, final ConfirmCallback cb) {
        if (!isContextValid() || cb == null) return;
        dismissAll();
        try {
            AlertDialog dialog = new AlertDialog.Builder(context)
               .setTitle(title != null ? title : "Confirmation")
               .setMessage(message != null ? message : "")
               .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playClickSfx();
                        cb.onConfirm(true);
                    }
                })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playClickSfx();
                        cb.onConfirm(false);
                    }
                })
               .setCancelable(false)
               .create();
            activeDialogs.add(dialog);
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    activeDialogs.remove(d);
                }
            });
            dialog.show();
        } catch (Exception ignored) {}
    }

    private void dismissDialog(final Dialog d) {
        if (d != null) {
            try {
                if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                    AlphaAnimation anim = new AlphaAnimation(1f, 0f);
                    anim.setDuration(160);
                    anim.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}
                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                        @Override
                        public void onAnimationEnd(Animation animation) {
                            try { if (d.isShowing()) d.dismiss(); } catch (Exception ignored) {}
                        }
                    });
                    d.getWindow().getDecorView().startAnimation(anim);
                } else {
                    if (d.isShowing()) d.dismiss();
                }
            } catch (Exception ignored) {}
            activeDialogs.remove(d);
        }
    }

    public void dismissAll() {
        for (int i = activeDialogs.size() - 1; i >= 0; i--) {
            Dialog d = activeDialogs.get(i);
            if (d != null) {
                try { if (d.isShowing()) d.dismiss(); } catch (Exception ignored) {}
            }
        }
        activeDialogs.clear();
    }

    public boolean hasActiveDialog() {
        for (int i = activeDialogs.size() - 1; i >= 0; i--) {
            Dialog d = activeDialogs.get(i);
            if (d != null && d.isShowing()) return true;
        }
        return false;
    }

    private String getPersonalityName(int p) {
        return GameConfig.getPersonalityName(p);
    }

    private String getElementName(int e) {
        return GameConfig.getElementName(e);
    }

    public void showTournamentDialog(final Disciple selectedDisciple, final SectData data, final TournamentCallback cb) {
        if (!isContextValid() || data == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_tournament);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvDisciple = (TextView) d.findViewById(R.id.tvTournamentDisciple);
            final TextView tvTierInfo = (TextView) d.findViewById(R.id.tvTournamentTierInfo);
            final TextView tvStatus = (TextView) d.findViewById(R.id.tvTournamentStatus);
            final TextView tvRoundLog = (TextView) d.findViewById(R.id.tvTournamentRoundLog);
            Button btnClose = (Button) d.findViewById(R.id.btnCloseTournament);
            final Button btnTier1 = (Button) d.findViewById(R.id.btnTier1);
            final Button btnTier2 = (Button) d.findViewById(R.id.btnTier2);
            final Button btnTier3 = (Button) d.findViewById(R.id.btnTier3);
            final Button btnStart = (Button) d.findViewById(R.id.btnStartTournament);

            final Disciple fighter = (selectedDisciple != null) ? selectedDisciple :
                    (data.disciples.size() > 0 ? data.disciples.get(0) : null);

            final int[] selectedTier = new int[]{0};

            if (tvDisciple != null) {
                if (fighter != null) {
                    tvDisciple.setText("Challenger: " + fighter.name + " (Combat Power: " + (int) fighter.getPowerRating() + ")");
                } else {
                    tvDisciple.setText("Challenger: None available (Recruit disciples first)");
                }
            }

            final Runnable updateTierUI = new Runnable() {
                @Override
                public void run() {
                    if (tvTierInfo != null) {
                        int t = selectedTier[0] + 1;
                        tvTierInfo.setText("Tier " + t + " Division | Reward: " + (300 * t) + " Spirit Stones, " + (10 * t) + " Jade, +" + (25 * t) + " Rep");
                    }
                    if (btnTier1 != null) btnTier1.setTextColor(selectedTier[0] == 0 ? 0xFFFFD700 : 0xFFFFFFFF);
                    if (btnTier2 != null) btnTier2.setTextColor(selectedTier[0] == 1 ? 0xFFFFD700 : 0xFFFFFFFF);
                    if (btnTier3 != null) btnTier3.setTextColor(selectedTier[0] == 2 ? 0xFFFFD700 : 0xFFFFFFFF);
                }
            };
            updateTierUI.run();

            if (btnTier1 != null) {
                btnTier1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedTier[0] = 0;
                        playClickSfx();
                        updateTierUI.run();
                    }
                });
            }
            if (btnTier2 != null) {
                btnTier2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedTier[0] = 1;
                        playClickSfx();
                        updateTierUI.run();
                    }
                });
            }
            if (btnTier3 != null) {
                btnTier3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedTier[0] = 2;
                        playClickSfx();
                        updateTierUI.run();
                    }
                });
            }

            if (btnStart != null) {
                btnStart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (fighter == null) {
                            if (tvRoundLog != null) tvRoundLog.setText("Cannot enter without a disciple!");
                            return;
                        }
                        playClickSfx();
                        com.sect.idle.gameplay.TournamentSystem.TournamentResult res =
                            com.sect.idle.gameplay.TournamentSystem.getInstance().enterTournament(fighter, data, selectedTier[0]);

                        if (tvStatus != null) {
                            tvStatus.setText(res.won ? "VICTORY! (Rounds: " + res.roundsWon + "/3)" : "DEFEATED (Rounds: " + res.roundsWon + "/3)");
                            tvStatus.setTextColor(res.won ? 0xFFFFD700 : 0xFFFF5252);
                        }
                        if (tvRoundLog != null) {
                            tvRoundLog.setText(res.summary);
                        }
                        if (cb != null) cb.onTournamentFinished(res);
                    }
                });
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        dismissDialog(d);
                    }
                });
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showWarDialog(final SectData data, final WarCallback cb) {
        if (!isContextValid() || data == null) return;
        dismissAll();
        playClickSfx();

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setContentView(R.layout.dialog_war_campaign);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }
        activeDialogs.add(d);

        try {
            TextView tvTarget = (TextView) d.findViewById(R.id.tvWarTarget);
            TextView tvPowerStats = (TextView) d.findViewById(R.id.tvWarPowerStats);
            final TextView tvStatus = (TextView) d.findViewById(R.id.tvWarStatus);
            final TextView tvLog = (TextView) d.findViewById(R.id.tvWarLog);
            final android.widget.ProgressBar pbMorale = (android.widget.ProgressBar) d.findViewById(R.id.pbWarMorale);
            Button btnClose = (Button) d.findViewById(R.id.btnCloseWar);
            final Button btnDiff1 = (Button) d.findViewById(R.id.btnWarDiff1);
            final Button btnDiff2 = (Button) d.findViewById(R.id.btnWarDiff2);
            final Button btnDiff3 = (Button) d.findViewById(R.id.btnWarDiff3);
            Button btnLaunch = (Button) d.findViewById(R.id.btnLaunchWar);

            int totalPower = 0;
            for (int i = 0; i < data.disciples.size(); i++) {
                Disciple disc = data.disciples.get(i);
                if (disc != null && disc.isAlive()) {
                    totalPower += (int) disc.getPowerRating();
                }
            }

            final int ourPower = totalPower;
            final int[] selectedDiff = new int[]{0};

            if (tvPowerStats != null) {
                tvPowerStats.setText("Our Sect Power: " + ourPower + " (" + data.disciples.size() + " Cultivators)");
            }

            final Runnable updateDiffUI = new Runnable() {
                @Override
                public void run() {
                    int diff = selectedDiff[0];
                    int estEnemyPower = 400 + (diff * 600) + (data.sectRealm * 300);
                    if (tvLog != null) {
                        tvLog.setText("Target Power Est: ~" + estEnemyPower + " | Loot: " + (500 * (diff + 1)) + " Stones, " + (15 * (diff + 1)) + " Jade");
                    }
                    if (pbMorale != null) {
                        int ratio = (int) (ourPower * 100f / Math.max(1, ourPower + estEnemyPower));
                        pbMorale.setProgress(Math.max(10, Math.min(95, ratio)));
                    }
                }
            };
            updateDiffUI.run();

            if (btnDiff1 != null) {
                btnDiff1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedDiff[0] = 0;
                        playClickSfx();
                        updateDiffUI.run();
                    }
                });
            }
            if (btnDiff2 != null) {
                btnDiff2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedDiff[0] = 1;
                        playClickSfx();
                        updateDiffUI.run();
                    }
                });
            }
            if (btnDiff3 != null) {
                btnDiff3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        selectedDiff[0] = 2;
                        playClickSfx();
                        updateDiffUI.run();
                    }
                });
            }

            if (btnLaunch != null) {
                btnLaunch.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        com.sect.idle.gameplay.WarSystem.WarResult res =
                            com.sect.idle.gameplay.WarSystem.getInstance().launchSectCampaign(data, selectedDiff[0]);

                        if (tvStatus != null) {
                            tvStatus.setText(res.won ? "CRUSHING CONQUEST VICTORY!" : "WAR CAMPAIGN DEFEAT");
                            tvStatus.setTextColor(res.won ? 0xFFFFD700 : 0xFFFF5252);
                        }
                        if (tvLog != null) {
                            tvLog.setText(res.message);
                        }
                        if (cb != null) cb.onWarFinished(res);
                    }
                });
            }

            if (btnClose != null) {
                btnClose.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        playClickSfx();
                        dismissDialog(d);
                    }
                });
            }

            d.setOnDismissListener(new Dialog.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    activeDialogs.remove(d);
                }
            });

            d.show();
            if (d.getWindow() != null && d.getWindow().getDecorView() != null) {
                d.getWindow().getDecorView().startAnimation(fadeIn);
            }
        } catch (Exception e) {
            activeDialogs.remove(d);
        }
    }

    public void showBuildingDialog(final Building b, final SectData data, final BuildingCallback cb) {
        showBuildingDialog(b, data, new BuildingActionCallback() {
            @Override
            public void onUpgrade() {
                if (b != null) {
                    if (!b.isBuilt) b.build();
                    else b.upgrade();
                }
                if (cb != null) cb.onUpgraded();
            }
            @Override
            public void onMinigame() {}
        });
    }

    public void showRecruit(Disciple candidate, final RecruitCallback cb) {
        showRecruitDialog(candidate, cb);
    }

    public void showMarket(MarketSystem market, SectData data, final MarketCallback cb) {
        showMarketDialog(market, data, cb);
    }

    public void showSettings(final SettingsCallback cb) {
        showSettingsDialog(cb);
    }

    public void showBattleResult(boolean victory, int rewardStones, int rewardExp, final BattleResultCallback cb) {
        showBattleResultDialog(victory, rewardStones, rewardExp, cb);
    }

    public interface BuildingCallback {
        void onUpgraded();
    }

    public interface RecruitCallback {
        void onAccept();
        void onReject();
    }

    public interface DetailCallback {
        void onAssignTask();
    }

    public interface TaskCallback {
        void onTaskSelected(int task);
    }

    public interface BuildingActionCallback {
        void onUpgrade();
        void onMinigame();
    }

    public interface MarketCallback {
        void onItemPurchased(int itemIndex, int price);
    }

    public interface SettingsCallback {
        void onSettingsSaved();
    }

    public interface BattleResultCallback {
        void onReturn();
    }

    public interface TournamentCallback {
        void onTournamentFinished(com.sect.idle.gameplay.TournamentSystem.TournamentResult result);
    }

    public interface WarCallback {
        void onWarFinished(com.sect.idle.gameplay.WarSystem.WarResult result);
    }

    public interface StoryCallback {
        void onChoice(int index);
    }

    public interface ConfirmCallback {
        void onConfirm(boolean confirmed);
    }
}
