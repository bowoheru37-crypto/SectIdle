package com.sect.idle.fusion;

public class NizamHitbox {
    public float x;
    public float y;
    public float width;
    public float height;
    public boolean isAttack;

    public NizamHitbox() {
        this(0, 0, 50, 50, true);
    }

    public NizamHitbox(float x, float y, float width, float height, boolean isAttack) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isAttack = isAttack;
    }

    public boolean overlaps(NizamHitbox other) {
        if (other == null) return false;
        return this.x < other.x + other.width &&
               this.x + this.width > other.x &&
               this.y < other.y + other.height &&
               this.y + this.height > other.y;
    }
}
