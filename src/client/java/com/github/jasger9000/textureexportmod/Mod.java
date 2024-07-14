package com.github.jasger9000.textureexportmod;

import net.minecraft.util.Identifier;

import java.util.ArrayList;

public final class Mod {
    private final String id;
    private final String displayName;
    private final ArrayList<Identifier> items;
    private boolean export;

    public Mod(String id, String displayName, ArrayList<Identifier> items, boolean export) {
        this.id = id;
        this.displayName = displayName;
        this.items = items;
        this.export = export;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public ArrayList<Identifier> items() {
        return items;
    }

    public boolean export() {
        return export;
    }

    public void export(boolean export) {
        this.export = export;
    }
}
