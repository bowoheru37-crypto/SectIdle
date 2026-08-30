package com.sect.idle.models;

public class Quest {
    public String id;
    public String title;
    public String desc;
    public int type; // 0=main, 1=daily, 2=sect, 3=secret
    public int target;
    public int progress;
    public int rewardSS;
    public int rewardJade;
    public int rewardRep;
    public boolean completed;
    public boolean claimed;
    public long deadline;
    
    public Quest(String id, String title, String desc, int type, int target, int ss, int jade, int rep) {
        this.id = id; this.title = title; this.desc = desc; this.type = type;
        this.target = target; this.rewardSS = ss; this.rewardJade = jade;
        this.rewardRep = rep; this.progress = 0;
    }
    
    public void addProgress(int amt) {
        if (!completed) {
            progress += amt;
            if (progress >= target) { progress = target; completed = true; }
        }
    }
    
    public float getPercent() { return Math.min(100f, (progress * 100f) / target); }
}
