package com.github.jasger9000.textureexportmod.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.random.Random;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static com.github.jasger9000.textureexportmod.TextureExportMod.LOGGER;
import static com.github.jasger9000.textureexportmod.TextureExportModClient.client;

public class Util {
    public static void drawItem(DrawContext context, BakedModel bakedModel, ItemStack stack, Framebuffer mainBuffer) {
        if (stack.isEmpty()) {
            return;
        }

        MatrixStack matrices = context.getMatrices();

        matrices.push();

        float size = context.getScaledWindowHeight(); // I don't know what black magic this does, but it works, DON'T TOUCH IT
        float aspectRatio = (float) mainBuffer.textureWidth / mainBuffer.textureHeight;

        // to make the item quadratic we need to multiply by the aspect ratio of the main framebuffer
        float width = size * aspectRatio;

        try {
            matrices.translate(width / 2, size / 2, 0.0F);
            matrices.scale(width, -size, size);

            boolean bl = !bakedModel.isSideLit();
            if (bl) {
                DiffuseLighting.disableGuiDepthLighting();
            }

            client
                    .getItemRenderer()
                    .renderItem(stack, ModelTransformationMode.GUI, false, matrices, context.getVertexConsumers(), 15728880, OverlayTexture.DEFAULT_UV, bakedModel);
            context.draw();

            if (bl) {
                DiffuseLighting.enableGuiDepthLighting();
            }
        } catch (Throwable var12) {
            CrashReport crashReport = CrashReport.create(var12, "Rendering item");
            CrashReportSection crashReportSection = crashReport.addElement("Item being rendered");
            crashReportSection.add("Item Type", () -> String.valueOf(stack.getItem()));
            crashReportSection.add("Item Components", () -> String.valueOf(stack.getComponents()));
            crashReportSection.add("Item Foil", () -> String.valueOf(stack.hasGlint()));
            throw new CrashException(crashReport);
        }

        matrices.pop();
    }


    public static int getFrameCount(BakedModel model) {
        List<BakedQuad> quads = model.getQuads(null, null, Random.create());

        int frameCount = 1;
        for (BakedQuad quad : quads) {
            int frames = quad.getSprite().getContents().getFrameCount();

            if (frames > frameCount) {
                frameCount = frames;
            }
        }

        return frameCount;
    }

    public static NativeImage createNativeImage(Framebuffer framebuffer) {
        int i = framebuffer.textureWidth;
        int j = framebuffer.textureHeight;
        NativeImage nativeImage = new NativeImage(i, j, false);
        RenderSystem.bindTexture(framebuffer.getColorAttachment());
        nativeImage.loadFromTextureImage(0, false);
        nativeImage.mirrorVertically();
        return nativeImage;
    }

    public static void createDirectory(Path path) {
        File pathFile = path.toFile();

        if (!pathFile.isDirectory()) {
            try {
                if (!pathFile.mkdir()) {
                    LOGGER.error("Failed to create directory: {}", pathFile.getAbsolutePath());
                }
            } catch (SecurityException e) {
                LOGGER.error("Failed to create directory to export textures", e);
            }
        }
    }
}
