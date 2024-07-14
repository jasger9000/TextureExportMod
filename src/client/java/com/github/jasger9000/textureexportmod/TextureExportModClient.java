package com.github.jasger9000.textureexportmod;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.nio.file.Path;
import java.util.Stack;

import static com.github.jasger9000.textureexportmod.TextureExportMod.LOGGER;
import static com.github.jasger9000.textureexportmod.TextureExportMod.MOD_ID;
import static com.github.jasger9000.textureexportmod.Util.createDirectory;

public class TextureExportModClient implements ClientModInitializer {
	public static final MinecraftClient client = MinecraftClient.getInstance();
	public static final Path EXPORT_DIRECTORY = Path.of(System.getProperty("user.home"), "Pictures", MOD_ID);
	public static boolean SHOULD_EXPORT = false;

	public final Stack<Identifier> items = new Stack<>();
	private Framebuffer framebuffer;

	@Override
	public void onInitializeClient() {
		createDirectory(EXPORT_DIRECTORY);

		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			Profiler profiler = client.getProfiler();

			// make new framebuffer to store our textures in
			profiler.push("create frame buffer");
			framebuffer = new SimpleFramebuffer(256, 256, true, MinecraftClient.IS_SYSTEM_MAC);
			profiler.pop();

		});

		ClientCommandRegistrationCallback.EVENT.register(this::onCommandRegistration);
		HudRenderCallback.EVENT.register((context, tickDeltaManager) -> HudRenderEvent.onHudRender(items, framebuffer, context));
	}

	private void onCommandRegistration(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("buildItemStack").executes((context) -> {
			LOGGER.info("Creating Item Stack");
			context.getSource().sendFeedback(Text.literal("Building Stack of all registered Items"));
			items.clear();
			int length = 0;

			for (Identifier id : Registries.ITEM.getIds()) {
				LOGGER.debug("Adding item {}", id);
				items.push(id);
				++length;
			}

			context.getSource().sendFeedback(Text.literal("Finished Building Stack with " + length + " elements"));
			LOGGER.info("Finished Building Stack");

			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("startExport").executes(context -> {
			LOGGER.info("Starting to export");
			context.getSource().sendFeedback(Text.literal("Starting to export Textures"));
			SHOULD_EXPORT = true;
			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("stopExport").executes(context -> {
			LOGGER.info("Stopping export");
			context.getSource().sendFeedback(Text.literal("Stopping export"));
			EXPORT = false;
			return 0;
		}));
	}
}
