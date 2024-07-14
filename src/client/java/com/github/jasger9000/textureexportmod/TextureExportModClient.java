package com.github.jasger9000.textureexportmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import static com.github.jasger9000.textureexportmod.TextureExportMod.LOGGER;
import static com.github.jasger9000.textureexportmod.TextureExportMod.MOD_ID;
import static com.github.jasger9000.textureexportmod.Util.createDirectory;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;

public class TextureExportModClient implements ClientModInitializer {
	public static final MinecraftClient client = MinecraftClient.getInstance();
	public static final Path EXPORT_DIRECTORY = Path.of(System.getProperty("user.home"), "Pictures", MOD_ID);
	public static final HashMap<String, Mod> MODS = new HashMap<>();
	public static boolean SHOULD_EXPORT = false;
	public static boolean STACK_DIRTY = true;

	public final Stack<Identifier> items = new Stack<>();
	private Framebuffer framebuffer;

	@Override
	public void onInitializeClient() {
		createDirectory(EXPORT_DIRECTORY);

		LOGGER.info("Building Mod Hashmap");

		for (Identifier id : Registries.ITEM.getIds()) {
			String namespace = id.getNamespace();
			LOGGER.debug("Adding item {}:{}", namespace, id.getPath());

			String displayName = FabricLoader.getInstance()
					.getModContainer(namespace)
					.map(modContainer -> modContainer.getMetadata().getName())
					.orElse(namespace);

			if (MODS.containsKey(namespace)) {
				MODS.get(namespace).items().add(id);
			} else {
				MODS.put(namespace, new Mod(namespace, displayName, new ArrayList<>(), true));
			}
		}
		LOGGER.info("Finished building hashmap with {} mods", MODS.size());

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
			context.getSource().sendFeedback(Text.literal("Building Item stack with all enabled mods"));
			int mods = buildItemStack();
			context.getSource().sendFeedback(Text.literal("Finished Building Stack with " + items.size() + " elements from " + mods + " mods"));

			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("startExport").executes(context -> {
			if (items.isEmpty()) {
				context.getSource().sendError(Text.literal("The item stack is empty, you need to build it first"));
				return 1;
			}
			if (STACK_DIRTY) {
				context.getSource().sendError(Text.literal("You are starting the export with a dirty item stack, you should probably rebuild it").withColor(Colors.YELLOW));
			}
			LOGGER.info("Starting to export");
			context.getSource().sendFeedback(Text.literal("Starting to export textures"));
			SHOULD_EXPORT = true;
			STACK_DIRTY = true;
			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("stopExport").executes(context -> {
			LOGGER.info("Stopping export");
			context.getSource().sendFeedback(Text.literal("Stopping export"));
			SHOULD_EXPORT = false;
			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("shouldExportMod")
				.then(argument("mod", new ModArgumentType())
						.then(argument("shouldExport", BoolArgumentType.bool())
								.executes((context) -> {
									boolean shouldExport = BoolArgumentType.getBool(context, "shouldExport");
									Mod mod = ModArgumentType.getMod(context, "mod");

									mod.export(shouldExport);
									context.getSource().sendFeedback(Text.literal(mod.id() + " will " + (shouldExport ? "now" : "no longer") + " be export"));
									STACK_DIRTY = true;
									return 0;
								})
						)
				)
		);
	}


	public int buildItemStack() {
		LOGGER.info("Creating Item Stack");

		items.clear();
		int mods = 0;
		for (Mod mod : MODS.values()) {
			if (mod.export()) {
				for (Identifier item : mod.items()) {
					LOGGER.debug("Adding item {}", items);
					items.push(item);
				}
				++mods;
			}
		}

		STACK_DIRTY = false;
		LOGGER.info("Finished Building Stack");

		return mods;
	}
}
