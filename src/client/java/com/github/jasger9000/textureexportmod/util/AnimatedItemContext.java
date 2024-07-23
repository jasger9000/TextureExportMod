package com.github.jasger9000.textureexportmod.util;

import net.minecraft.client.render.model.BakedModel;

public class AnimatedItemContext {

    private int frame;
    private boolean wasSpriteUploaded;
    public final int frames;
    public final BakedModel model;

    public AnimatedItemContext(int frames, final BakedModel model) {
        this.frame = 0;
        this.frames = frames;
        this.model = model;
    }

    public void setSpriteUploaded() {
        this.wasSpriteUploaded = true;
    }

    public int getFrame() {
        return frame;
    }

    public boolean wasSpriteUploaded() {
        return wasSpriteUploaded;
    }

    public void nextFrame() {
        ++frame;
        wasSpriteUploaded = false;
    }
}
