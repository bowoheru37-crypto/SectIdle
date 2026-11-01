package com.sect.idle.fusion;

public class NizamMoveData {
    public String moveName = "Punch";
    public int startupFrames = 5;
    public int activeFrames = 8;
    public int recoveryFrames = 12;
    public float damage = 15f;
    public String effect = "BatikGlow";
    public NizamHitbox activeHitbox = new NizamHitbox(100, 100, 60, 60, true);

    public NizamMoveData() {}

    public NizamMoveData(String name, int startup, int active, int recovery, float dmg) {
        this.moveName = name;
        this.startupFrames = startup;
        this.activeFrames = active;
        this.recoveryFrames = recovery;
        this.damage = dmg;
    }

    public int getTotalFrames() {
        return startupFrames + activeFrames + recoveryFrames;
    }

    public boolean isHitboxActive(int currentFrame) {
        return currentFrame >= startupFrames && currentFrame < (startupFrames + activeFrames);
    }
}
