package com.sect.idle.fusion;

import java.util.ArrayList;
import java.util.List;

/**
 * NizamNode - Godot-style Node & Scene Tree structure.
 * Pure Java 7/8 without lambdas for Sketchware Pro v7.0.0.
 */
public class NizamNode {
    private int id;
    private String name;
    private NizamNode parent;
    private final List<NizamNode> children = new ArrayList<NizamNode>();
    private final List<NizamComponent> components = new ArrayList<NizamComponent>();
    private boolean enabled = true;

    public NizamNode(int id, String name) {
        this.id = id;
        this.name = name != null ? name : "Node";
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public NizamNode getParent() { return parent; }

    public void addChild(NizamNode child) {
        if (child != null && child != this && !children.contains(child)) {
            if (child.parent != null) {
                child.parent.removeChild(child);
            }
            child.parent = this;
            children.add(child);
        }
    }

    public void removeChild(NizamNode child) {
        if (child != null && children.remove(child)) {
            child.parent = null;
        }
    }

    public List<NizamNode> getChildren() {
        return children;
    }

    public void addComponent(NizamComponent comp) {
        if (comp != null && !components.contains(comp)) {
            comp.setOwner(this);
            components.add(comp);
        }
    }

    public List<NizamComponent> getComponents() {
        return components;
    }
}
