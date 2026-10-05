package com.sect.idle.models;

import com.sect.idle.core.Vector2;
import java.util.ArrayList;

public class BattleUnit {
    public String name;
    public int hp, maxHp;
    public int mp, maxMp;
    public int atk, def, spd;
    public int critRate, critDmg;
    public int dodge, accuracy;
    public int element;
    public int team; // 0=player, 1=enemy
    public Vector2 pos;
    public boolean isAlive;
    public int actionBar;
    public int maxActionBar;
    public Disciple source;

    public int kills = 0;
    private ArrayList<Integer> skillCooldowns;

    public BattleUnit(Disciple d, int team) {
        this.source = d;
        this.team = team;
        this.name = d.name;
        this.hp = d.hp;
        this.maxHp = d.maxHp;
        this.mp = d.mp;
        this.maxMp = d.maxMp;
        this.atk = d.atk;
        this.def = d.def;
        this.spd = d.spd;
        this.critRate = d.critRate;
        this.critDmg = d.critDmg;
        this.dodge = d.dodge;
        this.accuracy = d.accuracy;
        this.element = d.element;
        this.pos = new Vector2();
        this.isAlive = true;
        this.actionBar = 0;
        this.maxActionBar = Math.max(100, 200 - spd);

        this.skillCooldowns = new ArrayList<Integer>();
        if (d.skills != null) {
            for (int i = 0; i < d.skills.size(); i++) skillCooldowns.add(0);
        }
    }

    public void tickAction() {
        actionBar += Math.max(1, spd / 5);
        if (actionBar >= maxActionBar) actionBar = maxActionBar;
    }

    public boolean canAct() { return actionBar >= maxActionBar && isAlive; }
    public void resetAction() { actionBar = 0; }

    public int calcDamage(BattleUnit target, float skillPower) {
        float dmg = atk * skillPower * (1 + (float)source.realm / 10);
        dmg = dmg * (100f / (100f + target.def));
        if (Math.random() * 100 < critRate) dmg *= critDmg / 100f;
        float acc = Math.min(100, accuracy - target.dodge + 80);
        if (Math.random() * 100 > acc) return 0; // miss
        return Math.max(1, (int)dmg);
    }

    public void takeDamage(int dmg) {
        hp -= dmg;
        if (hp <= 0) { hp = 0; isAlive = false; }
    }

    public void heal(int amt) {
        hp = Math.min(maxHp, hp + amt);
    }

    public void tickCooldowns() {
        for (int i = 0; i < skillCooldowns.size(); i++) {
            int cd = skillCooldowns.get(i);
            if (cd > 0) skillCooldowns.set(i, cd - 1);
        }
    }

    public boolean isSkillReady(int skillIndex) {
        if (skillIndex < 0 || skillIndex >= skillCooldowns.size()) return false;
        return skillCooldowns.get(skillIndex) <= 0;
    }

    public void setSkillCooldown(int skillIndex, int cd) {
        if (skillIndex >= 0 && skillIndex < skillCooldowns.size()) {
            skillCooldowns.set(skillIndex, cd);
        }
    }
}
