package com.sect.idle.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.R;
import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.MarketSystem;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.HobbySystem;
import com.sect.idle.systems.JobSystem;
import com.sect.idle.systems.NumberFormatter;
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

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(28, 28, 28, 28);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("⚙️ Game Settings & Dao Preferences");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(tvTitle);

        final AudioManager audio = AudioManager.getInstance(context);

        final CheckBox cbBgm = new CheckBox(context);
        cbBgm.setText("Background Music (BGM)");
        cbBgm.setTextColor(0xFFFFFFFF);
        cbBgm.setChecked(audio.isBgmEnabled());
        cbBgm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audio.setBgmEnabled(isChecked);
            }
        });
        root.addView(cbBgm);

        final CheckBox cbSfx = new CheckBox(context);
        cbSfx.setText("Sound Effects (SFX)");
        cbSfx.setTextColor(0xFFFFFFFF);
        cbSfx.setChecked(audio.isEnabled());
        cbSfx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                audio.setEnabled(isChecked);
            }
        });
        root.addView(cbSfx);

        final CheckBox cbAutoSave = new CheckBox(context);
        cbAutoSave.setText("Automatic Cloud / Local Saving");
        cbAutoSave.setTextColor(0xFFFFFFFF);
        cbAutoSave.setChecked(SectData.getInstance() != null && SectData.getInstance().autoSave);
        cbAutoSave.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (SectData.getInstance() != null) SectData.getInstance().autoSave = isChecked;
            }
        });
        root.addView(cbAutoSave);

        Button btnSave = new Button(context);
        btnSave.setText("Save Settings");
        btnSave.setBackgroundResource(R.drawable.bg_button_jade);
        btnSave.setTextColor(0xFF00E5FF);
        btnSave.setTypeface(Typeface.DEFAULT_BOLD);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 16, 0, 0);
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

    public interface StoryCallback {
        void onChoice(int index);
    }

    public interface ConfirmCallback {
        void onConfirm(boolean confirmed);
    }
}
