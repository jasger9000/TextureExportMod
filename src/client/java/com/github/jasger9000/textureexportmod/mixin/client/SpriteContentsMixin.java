package com.github.jasger9000.textureexportmod.mixin.client;

import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.github.jasger9000.textureexportmod.TextureExportModClient.*;

@Mixin(targets = { "net.minecraft.client.texture.SpriteContents$AnimatorImpl" })
public class SpriteContentsMixin {

    @Shadow int frame;
    @Shadow @Final SpriteContents.Animation animation;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    public void tick(int x, int y, CallbackInfo ci) {
        if (SHOULD_EXPORT && ANIMATED_ITEM_CONTEXT != null && ANIMATED_ITEM_CONTEXT.getFrame() < animation.frames.size()) {
            int currentFrame = this.animation.frames.get(this.frame).index;

            this.frame = ANIMATED_ITEM_CONTEXT.getFrame();
            int nextFrame = this.animation.frames.get(this.frame).index;

            if (currentFrame != nextFrame) {
                this.animation.upload(x, y, nextFrame);
            } else {
                ANIMATED_ITEM_CONTEXT.setSpriteUploaded();
            }
            ci.cancel();
        }
    }
}
