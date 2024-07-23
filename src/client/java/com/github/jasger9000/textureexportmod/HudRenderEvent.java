package com.github.jasger9000.textureexportmod;

import com.github.jasger9000.textureexportmod.gui.ExportScreen;
import com.github.jasger9000.textureexportmod.util.AnimatedItemContext;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.nio.file.Path;

import static com.github.jasger9000.textureexportmod.TextureExportMod.LOGGER;
import static com.github.jasger9000.textureexportmod.TextureExportModClient.*;
import static com.github.jasger9000.textureexportmod.util.Util.*;

public class HudRenderEvent {

    public static void onHudRender(DrawContext context, RenderTickCounter tickDeltaManager) {
        if (SHOULD_EXPORT && ITEMS.isEmpty()) {
            LOGGER.info("Finished exporting textures");
            if (client.player != null && !(client.currentScreen instanceof ExportScreen)) {
                client.player.sendMessage(Text.translatable("commands.textureexport.finish").withColor(Colors.GREEN));
            }

            stopExport();
            return;
        } else if (!SHOULD_EXPORT) {
            return;
        }

        FRAMEBUFFER.clear(MinecraftClient.IS_SYSTEM_MAC);

        // Make shaders not affect our output
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        client.getFramebuffer().endWrite();
        FRAMEBUFFER.beginWrite(false);

        Identifier id = ITEMS.peek();

        LOGGER.debug("Drawing item {}", id.getPath());

        Path path;
        BakedModel model;
        ItemStack stack = Registries.ITEM.get(id).getDefaultStack();

        if (ANIMATED_ITEM_CONTEXT != null) {
            path = EXPORT_DIRECTORY.resolve(Path.of(id.getNamespace(), id.getPath(), ANIMATED_ITEM_CONTEXT.getFrame() + ".png"));
            model = ANIMATED_ITEM_CONTEXT.model;

        } else {
            path = EXPORT_DIRECTORY.resolve(Path.of(id.getNamespace(), id.getPath() + ".png"));
            model = client.getItemRenderer().getModel(stack, null, null, 0);
            ITEMS.pop();
        }

        drawItem(context, model, stack, client.getFramebuffer());

        FRAMEBUFFER.endWrite();
        client.getFramebuffer().beginWrite(true);

        try (NativeImage screenshot = createNativeImage(FRAMEBUFFER)) {
            createDirectory(path.getParent());

            screenshot.writeTo(path);
        } catch (IOException e) {
            if (client.player != null) {
                client.player.sendMessage(Text.translatable("screenshot.failure", e.getMessage()).withColor(Colors.LIGHT_RED));
            }
            LOGGER.error("Failed to write screenshot.", e);
        }

        if (ANIMATED_ITEM_CONTEXT != null) {
            if (ANIMATED_ITEM_CONTEXT.getFrame() >= ANIMATED_ITEM_CONTEXT.frames - 1 && ANIMATED_ITEM_CONTEXT.wasSpriteUploaded()) {
                ANIMATED_ITEM_CONTEXT = null;
                ITEMS.pop();
                ++EXPORTED_TEXTURES;
            } else if (ANIMATED_ITEM_CONTEXT.wasSpriteUploaded()) {
                ANIMATED_ITEM_CONTEXT.nextFrame();
            }
        } else {
            ++EXPORTED_TEXTURES;
        }


        // peek if next item is animated and create AnimatedItemContext if it is
        if (!ITEMS.isEmpty() && ANIMATED_ITEM_CONTEXT == null) {
            id = ITEMS.peek();
            stack = Registries.ITEM.get(id).getDefaultStack();
            model = client.getItemRenderer().getModel(stack, null, null, 0);
            int frames = getFrameCount(model);

            if (frames > 1) {
                ANIMATED_ITEM_CONTEXT = new AnimatedItemContext(frames, model);
            }
        }
    }
}
