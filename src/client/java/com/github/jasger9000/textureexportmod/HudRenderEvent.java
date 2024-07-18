package com.github.jasger9000.textureexportmod;

import com.github.jasger9000.textureexportmod.gui.ExportScreen;
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

        Identifier id = ITEMS.pop();

        LOGGER.debug("Drawing item {}", id.getPath());

        ItemStack stack = Registries.ITEM.get(id).getDefaultStack();
        BakedModel model = client.getItemRenderer().getModel(stack, null, null, 0);

        drawItem(context, model, stack, client.getFramebuffer());

        FRAMEBUFFER.endWrite();
        client.getFramebuffer().beginWrite(true);

        try (NativeImage screenshot = createNativeImage(FRAMEBUFFER)) {
            Path path = EXPORT_DIRECTORY.resolve(Path.of(id.getNamespace(), id.getPath() + ".png"));
            createDirectory(path.getParent());

            screenshot.writeTo(path);
        } catch (IOException e) {
            if (client.player != null) {
                client.player.sendMessage(Text.translatable("screenshot.failure", e.getMessage()).withColor(Colors.LIGHT_RED));
            }
            LOGGER.error("Failed to write screenshot.", e);
        }

        ++EXPORTED_TEXTURES;
    }
}
