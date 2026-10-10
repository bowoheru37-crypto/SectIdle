package com.sect.idle.gameplay;

import com.sect.idle.models.BattleUnit;
import com.sect.idle.models.Disciple;
import com.sect.idle.systems.RNG;
import java.util.ArrayList;

/**
 * BattleEngine - Turn-based ATB battle simulation.
 * Pure Java 7 & Sketchware Pro v7.0.0 Compatible.
 */
public final class BattleEngine {
    public boolean isRunning = false;
    public boolean playerWon = false;
    public int round = 1;
    public int turn = 0;
    public int battleSpeed = 1; // 1x, 2x, 4x speed support

    private final ArrayList<BattleUnit> units;
    private final ArrayList<String> log;
    private static final int MAX_LOG_SIZE = 50;

    public BattleEngine() {
        this.units = new ArrayList<BattleUnit>();
        this.log = new ArrayList<String>();
    }

    public void startBattle(ArrayList<Disciple> playerTeam, ArrayList<Disciple> enemyTeam) {
        units.clear();
        log.clear();
        round = 1;
        turn = 0;
        isRunning = true;
        playerWon = false;

        if (playerTeam != null) {
            for (int i = 0; i < playerTeam.size(); i++) {
                Disciple d = playerTeam.get(i);
                if (d != null && d.isAlive()) {
                    units.add(new BattleUnit(d, 0));
                }
            }
        }

        if (enemyTeam != null) {
            for (int i = 0; i < enemyTeam.size(); i++) {
                Disciple d = enemyTeam.get(i);
                if (d != null && d.isAlive()) {
                    units.add(new BattleUnit(d, 1));
                }
            }
        }

        addLog("Battle begins! Round " + round);
        checkElementalSynergy();
    }

    private void checkElementalSynergy() {
        // Count elements on player team
        int[] elemCount = new int[12];
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.team == 0) {
                int elem = u.element;
                if (elem >= 0 && elem < elemCount.length) {
                    elemCount[elem]++;
                }
            }
        }

        // Apply +15% stats if 2+ disciples share same element
        for (int e = 0; e < elemCount.length; e++) {
            if (elemCount[e] >= 2) {
                addLog("Elemental Synergy Triggered! (+15% Stats Boost)");
                for (int i = 0; i < units.size(); i++) {
                    BattleUnit u = units.get(i);
                    if (u != null && u.team == 0 && u.element == e) {
                        u.atk = (int) (u.atk * 1.15f);
                        u.def = (int) (u.def * 1.15f);
                    }
                }
            }
        }
    }

    public void setBattleSpeed(int speed) {
        if (speed == 1 || speed == 2 || speed == 4) {
            this.battleSpeed = speed;
        }
    }

    public void tick() {
        if (!isRunning) return;

        for (int s = 0; s < battleSpeed; s++) {
            if (!isRunning) break;
            stepTick();
        }
    }

    private void stepTick() {
        if (!isRunning) return;

        turn++;
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u == null || !u.isAlive) continue;

            u.tickAction();
            u.tickCooldowns();

            if (u.canAct()) {
                performTurn(u);
                u.resetAction();
                checkBattleEnd();
                if (!isRunning) break;
            }
        }
    }

    private void performTurn(BattleUnit actor) {
        BattleUnit target = findTarget(actor.team == 0 ? 1 : 0);
        if (target == null) return;

        String aName = (actor.name != null) ? actor.name : (actor.source != null && actor.source.name != null ? actor.source.name : "Unit");
        String tName = (target.name != null) ? target.name : (target.source != null && target.source.name != null ? target.source.name : "Unit");

        // 1. Martial Arts Combo Chain & Stance Modifier (Tekken / Soul Edge)
        float comboMultiplier = 1.0f;
        boolean isJuggle = RNG.chance(25);
        if (isJuggle) {
            comboMultiplier = 1.6f;
            addLog("🥊 " + aName + " triggered 10-Hit Juggle Combo on " + tName + "!");
        }

        // 2. Desperation Super Combo / Qi Clash (DBGT Final Bout / KoF)
        if (actor.hp < actor.maxHp * 0.25f && RNG.chance(40)) {
            comboMultiplier = 2.5f;
            addLog("⚡ DESPERATION SUPER COMBO! " + aName + " unleashes Heavenly Qi Clash!");
        }

        int dmg = actor.calcDamage(target, comboMultiplier);
        if (dmg <= 0) {
            addLog(aName + " attacked " + tName + ", but missed!");
        } else {
            target.takeDamage(dmg);
            if (dmg > actor.atk * 1.8f) {
                addLog(aName + " CRITICAL FINISHER! Dealt " + dmg + " dmg to " + tName + "!");
            } else {
                addLog(aName + " struck " + tName + " for " + dmg + " dmg.");
            }

            // 3. Mortal Kombat Style Heavenly Execution Finisher
            if (!target.isAlive) {
                actor.kills++;
                addLog("💀 HEAVENLY EXECUTION! " + aName + " utterly vanquished " + tName + "!");
            }
        }
    }

    private BattleUnit findTarget(int team) {
        ArrayList<BattleUnit> candidates = new ArrayList<BattleUnit>();
        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.isAlive && u.team == team) {
                candidates.add(u);
            }
        }
        if (candidates.isEmpty()) return null;
        return candidates.get(RNG.nextInt(candidates.size()));
    }

    private void checkBattleEnd() {
        boolean playerAlive = false;
        boolean enemyAlive = false;

        for (int i = 0; i < units.size(); i++) {
            BattleUnit u = units.get(i);
            if (u != null && u.isAlive) {
                if (u.team == 0) playerAlive = true;
                else enemyAlive = true;
            }
        }

        if (!playerAlive || !enemyAlive) {
            isRunning = false;
            playerWon = playerAlive;
            if (playerWon) {
                addLog("VICTORY! The disciples triumphed!");
            } else {
                addLog("DEFEAT! The sect fell back.");
            }
        }
    }

    public void addLog(String msg) {
        if (msg == null) return;
        log.add(msg);
        if (log.size() > MAX_LOG_SIZE) {
            log.remove(0);
        }
    }

    public ArrayList<BattleUnit> getUnits() { return units; }
    public ArrayList<String> getLog() { return log; }
}
