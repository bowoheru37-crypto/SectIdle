package com.sect.idle.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.example.R;
import com.sect.idle.core.GameConfig;
import com.sect.idle.gameplay.MonetizationManager;
import com.sect.idle.gameplay.SectData;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.NumberFormatter;
import java.util.List;

/**
 * ShopDialog - Intuitive Shop, VIP Pass, Rewarded Boosts & Gacha Draw Modal.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class ShopDialog {
    private final Context context;

    public ShopDialog(Context ctx) {
        this.context = ctx;
    }

    public void showShopModal() {
        if (context == null) return;

        final Dialog d = new Dialog(context, android.R.style.Theme_Translucent_NoTitleBar);
        d.setCancelable(true);
        if (d.getWindow() != null) {
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundResource(R.drawable.bg_combat_card);
        root.setPadding(32, 32, 32, 32);
        scroll.addView(root);

        TextView tvTitle = new TextView(context);
        tvTitle.setText("💎 Immortal Treasure Shop & Banner");
        tvTitle.setTextColor(0xFFFFD700);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(tvTitle);

        final SectData data = SectData.getInstance();
        final MonetizationManager mm = MonetizationManager.getInstance();

        final TextView tvBalance = new TextView(context);
        tvBalance.setText("Jade: " + NumberFormatter.format(data.jade) + " | VIP: " + (mm.isVipActive() ? "Active" : "Inactive") + " | Pity: " + mm.gachaPityCounter + "/50");
        tvBalance.setTextColor(0xFF00E5FF);
        tvBalance.setPadding(0, 8, 0, 16);
        root.addView(tvBalance);

        // Section 1: Gacha Recruitment Banner
        TextView tvGachaHeader = new TextView(context);
        tvGachaHeader.setText("✨ Disciple Recruitment Banner (Gacha)");
        tvGachaHeader.setTextColor(0xFF00E5FF);
        tvGachaHeader.setTypeface(Typeface.DEFAULT_BOLD);
        root.addView(tvGachaHeader);

        LinearLayout gachaRow = new LinearLayout(context);
        gachaRow.setOrientation(LinearLayout.HORIZONTAL);
        gachaRow.setWeightSum(2);

        Button btnPull1 = new Button(context);
        btnPull1.setText("1x Draw (100 Jade)");
        btnPull1.setBackgroundResource(R.drawable.bg_button_jade);
        btnPull1.setTextColor(0xFFFFD700);
        btnPull1.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnPull1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disciple dDisc = mm.performSingleGachaPull(false);
                if (dDisc != null) {
                    try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_BREAKTHROUGH); } catch (Exception ignored) {}
                    tvBalance.setText("Jade: " + NumberFormatter.format(data.jade) + " | Recruited: " + dDisc.name);
                }
            }
        });
        gachaRow.addView(btnPull1);

        Button btnPull10 = new Button(context);
        btnPull10.setText("10x Draw (900 Jade)");
        btnPull10.setBackgroundResource(R.drawable.bg_button_jade);
        btnPull10.setTextColor(0xFFFFD700);
        btnPull10.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
        btnPull10.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Disciple> list = mm.performTenGachaPulls();
                if (list != null && !list.isEmpty()) {
                    try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_BREAKTHROUGH); } catch (Exception ignored) {}
                    tvBalance.setText("Jade: " + NumberFormatter.format(data.jade) + " | Recruited " + list.size() + " Cultivators!");
                }
            }
        });
        gachaRow.addView(btnPull10);
        root.addView(gachaRow);

        // Section 2: Rewarded Ad Boosts
        TextView tvAdHeader = new TextView(context);
        tvAdHeader.setText("🎥 Rewarded Celestial Boosts");
        tvAdHeader.setTextColor(0xFF00E5FF);
        tvAdHeader.setTypeface(Typeface.DEFAULT_BOLD);
        tvAdHeader.setPadding(0, 16, 0, 4);
        root.addView(tvAdHeader);

        Button btnAdSpeed = new Button(context);
        btnAdSpeed.setText("Watch Ad: 2x Game Speed (30 Mins)");
        btnAdSpeed.setBackgroundResource(R.drawable.bg_button_jade);
        btnAdSpeed.setTextColor(0xFF81C784);
        btnAdSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mm.watchAdForSpeedBoost(30);
                try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_UPGRADE); } catch (Exception ignored) {}
                tvBalance.setText("2x Speed Active! Jade: " + NumberFormatter.format(data.jade));
            }
        });
        root.addView(btnAdSpeed);

        Button btnAdRes = new Button(context);
        btnAdRes.setText("Watch Ad: Instant 2-Hour Resources");
        btnAdRes.setBackgroundResource(R.drawable.bg_button_jade);
        btnAdRes.setTextColor(0xFF81C784);
        btnAdRes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mm.watchAdForInstantResources();
                try { AudioManager.getInstance(context).playSfx(AudioManager.SFX_COLLECT); } catch (Exception ignored) {}
                tvBalance.setText("Claimed Instant Yield! SS: " + NumberFormatter.format(data.spiritStones));
            }
        });
        root.addView(btnAdRes);

        // Close
        Button btnClose = new Button(context);
        btnClose.setText("Close Shop");
        btnClose.setBackgroundResource(R.drawable.bg_button_jade);
        btnClose.setTextColor(0xFFAAAAAA);
        LinearLayout.LayoutParams cp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cp.setMargins(0, 20, 0, 0);
        btnClose.setLayoutParams(cp);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (d.isShowing()) d.dismiss();
            }
        });
        root.addView(btnClose);

        d.setContentView(scroll);
        d.show();
    }
}
