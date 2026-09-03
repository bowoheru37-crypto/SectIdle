package com.sect.idle.gameplay;

import com.sect.idle.core.GameConfig;
import com.sect.idle.models.Building;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.AudioManager;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * TrainingSystem - Manages disciple martial training, body refining,
 * meditation, and breakthrough cultivation.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class TrainingSystem {
    private static volatile TrainingSystem instance;

    public static TrainingSystem getInstance() {
        if (instance == null) {
            synchronized (TrainingSystem.class) {
                if (instance == null) {
                    instance = new TrainingSystem();
                }
            }
        }
        return instance;
    }

    public static final int TYPE_MEDITATION = 0;
    public static final int TYPE_BODY_TEMPERING = 1;
    public static final int TYPE_TECHNIQUE_SPAR = 2;
    public static final int TYPE_DAO_COMPREHENSION = 3;

    public void processDiscipleTraining(Disciple d, SectData s, int trainingType) {
        if (d == null || s == null || !d.isAlive()) return;

        float efficiencyBonus = 1.0f + (d.taskEfficiency / 100f);
        
        // Find building bonus if Arena or Library is built
        for (int i = 0; i < s.buildings.size(); i++) {
            Building b = s.buildings.get(i);
            if (b != null && b.isBuilt) {
                if (b.type == GameConfig.BUILD_ARENA && trainingType == TYPE_TECHNIQUE_SPAR) {
                    efficiencyBonus += b.level * 0.15f;
                } else if (b.type == GameConfig.BUILD_LIBRARY && (trainingType == TYPE_MEDITATION || trainingType == TYPE_DAO_COMPREHENSION)) {
                    efficiencyBonus += b.level * 0.15f;
                }
            }
        }

        switch (trainingType) {
            case TYPE_MEDITATION:
                int expGain = (int)((20 + d.wis * 2 + d.intel * 2) * efficiencyBonus);
                d.realmExp += expGain;
                d.addExperience(expGain / 2);
                break;

            case TYPE_BODY_TEMPERING:
                d.str += RNG.nextInt(1, 3);
                d.vit += RNG.nextInt(1, 3);
                d.bodyRefiningStage++;
                d.recalculateStats();
                break;

            case TYPE_TECHNIQUE_SPAR:
                d.agi += RNG.nextInt(1, 3);
                d.accuracy += 1;
                d.recalculateStats();
                break;

            case TYPE_DAO_COMPREHENSION:
                d.wis += RNG.nextInt(1, 3);
                d.intel += RNG.nextInt(1, 3);
                d.recalculateStats();
                break;
        }

        // Check Realm Breakthrough
        int reqExp = (d.realm + 1) * 200;
        if (d.realmExp >= reqExp && d.realm < GameConfig.REALM_MAX - 1) {
            d.realmExp -= reqExp;
            d.realm++;
            d.realmTier = 1;
            d.recalculateStats();
            
            // Update sect highest realm record
            if (d.realm > s.highestRealm) {
                s.highestRealm = d.realm;
            }
            if (s.highestRealm > s.sectRealm) {
                s.sectRealm = s.highestRealm;
            }
            s.markEconomyDirty();

            GameplayFeedbackDispatcher.getInstance().onDiscipleBreakthrough(d, GameConfig.getRealmName(d.realm));
        }
    }

    public void processAllTrainingDisciples(SectData s) {
        if (s == null || s.disciples == null) return;
        for (int i = 0; i < s.disciples.size(); i++) {
            Disciple d = s.disciples.get(i);
            if (d != null && d.currentTask == GameConfig.TASK_TRAINING) {
                processDiscipleTraining(d, s, TYPE_TECHNIQUE_SPAR);
            } else if (d != null && d.currentTask == GameConfig.TASK_CULTIVATION) {
                processDiscipleTraining(d, s, TYPE_MEDITATION);
            }
        }
    }
}
