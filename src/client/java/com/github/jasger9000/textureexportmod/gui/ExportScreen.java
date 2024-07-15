package com.github.jasger9000.textureexportmod.gui;

import com.github.jasger9000.textureexportmod.gui.widgets.ModListWidget;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;

import static com.github.jasger9000.textureexportmod.TextureExportModClient.*;
import static net.minecraft.client.MinecraftClient.IS_SYSTEM_MAC;

public class ExportScreen extends Screen {
    private static final Text SCREEN_NAME = Text.translatable("gui.textureexportmod.screen_name");
    private static final Text MODS_TO_EXPORT_TEXT = Text.translatable("gui.textureexportmod.mods_to_export");
    private static final Text START_BTN_TEXT = Text.translatable("gui.textureexportmod.start_button");
    private static final Text STOP_BTN_TEXT = Text.translatable("gui.textureexportmod.stop_button");
    private static final Text BUILD_BTN_TEXT = Text.translatable("gui.textureexportmod.build_button");
    private static final Text TEXTURE_SIZE_TEXT = Text.translatable("gui.textureexportmod.texture_size");
    private static final Text APPLY_TEXT = Text.translatable("gui.textureexportmod.apply");

    private final ButtonWidget startBtn;
    private final ButtonWidget stopBtn;
    private final ButtonWidget buildBtn;
    private final ButtonWidget applySizeBtn;
    private TextFieldWidget textureSizeField;

    public ExportScreen() {
        super(SCREEN_NAME);

        startBtn = ButtonWidget.builder(
                START_BTN_TEXT,
                (widget) -> {
                    if (ITEMS.isEmpty()) {
                        buildItemStack();
                    }
                    startExport();
               })
                .position(177, 50)
                .size(60, 20)
                .build();
        stopBtn = ButtonWidget.builder(STOP_BTN_TEXT, (widget) -> stopExport()).position(177, 75).size(60, 20).build();
        buildBtn = ButtonWidget.builder(BUILD_BTN_TEXT, (widget) -> buildItemStack()).position(177, 100).size(60, 20).build();
        applySizeBtn = ButtonWidget.builder(
                APPLY_TEXT,
                (widget) -> {
                    if (SHOULD_EXPORT) {
                        return;
                    }
                    try {
                        int size = Integer.parseInt(textureSizeField.getText());
                        if (size <= 0) {
                            return;
                        }

                        FRAMEBUFFER = new SimpleFramebuffer(size, size, true, IS_SYSTEM_MAC);
                    } catch (NumberFormatException e) {
                        return;
                    }
                })
                .position(250, 140)
                .size(30, 20)
                .build();
    }

    protected void init() {
        this.textureSizeField = new TextFieldWidget(this.textRenderer, 177, 140, 50, 20, TEXTURE_SIZE_TEXT);
        this.textureSizeField.setText(Integer.toString(FRAMEBUFFER.textureHeight));

        this.addDrawableChild(new ModListWidget(client, 25, 40, 126, this.height - 50));

        this.addDrawableChild(startBtn);
        this.addDrawableChild(stopBtn);
        this.addDrawableChild(buildBtn);
        this.addDrawableChild(textureSizeField);
        this.addDrawableChild(applySizeBtn);
    }


    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        assert client != null;
        super.render(context, mouseX, mouseY, delta);

        startBtn.active = !SHOULD_EXPORT && !ITEMS.isEmpty();
        stopBtn.active = SHOULD_EXPORT;
        buildBtn.active = !SHOULD_EXPORT && STACK_DIRTY;
        textureSizeField.active = !SHOULD_EXPORT;
        applySizeBtn.active = !SHOULD_EXPORT;

        context.drawCenteredTextWithShadow(this.textRenderer, MODS_TO_EXPORT_TEXT, 60, 40 - this.textRenderer.fontHeight - 10, Colors.WHITE);
        context.drawTextWithShadow(this.textRenderer, TEXTURE_SIZE_TEXT, textureSizeField.getX(), textureSizeField.getY() - 12, Colors.GRAY);
        context.drawTextWithShadow(this.textRenderer,
                Text.of("px"),
                textureSizeField.getX() + textureSizeField.getWidth() + 3,
                textureSizeField.getY() + textureSizeField.getHeight() - this.textRenderer.fontHeight,
                Colors.WHITE
        );
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.textureexportmod.status_label", SHOULD_EXPORT ?
                        Text.translatable("gui.textureexportmod.status_export") :
                        Text.translatable("gui.textureexportmod.status_idle")),
                250,
                56,
                Colors.WHITE
        );
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.textureexportmod.progress_label",
                        EXPORTED_TEXTURES,
                        TOTAL_TEXTURES,
                        ((float) EXPORTED_TEXTURES/TOTAL_TEXTURES) * 100),
                250,
                56 + textRenderer.fontHeight + 5,
                Colors.WHITE
        );
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.textureexportmod.eta_label", (TOTAL_TEXTURES - EXPORTED_TEXTURES) / 60, 60),
                250,
                56 + (textRenderer.fontHeight + 5) * 2,
                Colors.WHITE
        );
        context.drawTextWithShadow(this.textRenderer,
                Text.translatable("gui.textureexportmod.eta_label", (TOTAL_TEXTURES - EXPORTED_TEXTURES) / client.getCurrentFps(), client.getCurrentFps()),
                250,
                56 + (textRenderer.fontHeight + 5) * 3,
                Colors.WHITE
        );
        if (!ITEMS.isEmpty()) {
            Identifier item = ITEMS.peek();
            context.drawTextWithShadow(this.textRenderer,
                    Text.translatable("gui.textureexportmod.current_label", item.toString()),
                    250,
                    56 + (textRenderer.fontHeight + 5) * 4,
                    Colors.WHITE
            );
        }
    }
}
