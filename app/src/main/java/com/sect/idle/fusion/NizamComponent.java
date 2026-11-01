package com.sect.idle.fusion;

public abstract class NizamComponent {
    private NizamNode owner;
    private boolean active = true;

    public NizamNode getOwner() { return owner; }
    public void setOwner(NizamNode owner) { this.owner = owner; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public abstract String getTypeName();
}
