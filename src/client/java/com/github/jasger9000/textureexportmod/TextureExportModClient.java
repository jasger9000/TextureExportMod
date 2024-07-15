package com.github.jasger9000.textureexportmod;

import com.github.jasger9000.textureexportmod.gui.ExportScreen;
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
	public static int EXPORTED_TEXTURES = 0;
	public static int TOTAL_TEXTURES = 0;
	public static final Stack<Identifier> ITEMS = new Stack<>();

	public static Framebuffer FRAMEBUFFER;

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
			FRAMEBUFFER = new SimpleFramebuffer(256, 256, true, MinecraftClient.IS_SYSTEM_MAC);
			profiler.pop();

		});

		ClientCommandRegistrationCallback.EVENT.register(this::onCommandRegistration);
		HudRenderCallback.EVENT.register(HudRenderEvent::onHudRender);
	}

	private void onCommandRegistration(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(ClientCommandManager.literal("buildItemStack").executes((context) -> {
			context.getSource().sendFeedback(Text.translatable("commands.textureexport.build"));
			int mod_size = buildItemStack();
			context.getSource().sendFeedback(Text.translatable("commands.textureexport.finish_build", ITEMS.size(), mod_size));

			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("startExport").executes(context -> {
			if (ITEMS.isEmpty()) {
				context.getSource().sendError(Text.translatable("commands.textureexport.stack_empty"));
				return 1;
			}
			if (STACK_DIRTY) {
				context.getSource().sendError(Text.translatable("commands.textureexport.stack_dirty").withColor(Colors.YELLOW));
			}
			context.getSource().sendFeedback(Text.translatable("commands.textureexport.start"));
			startExport();
			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("stopExport").executes(context -> {
			context.getSource().sendFeedback(Text.translatable("commands.textureexport.stop"));
			stopExport();
			return 0;
		}));

		dispatcher.register(ClientCommandManager.literal("shouldExportMod")
				.then(argument("mod", new ModArgumentType())
						.then(argument("shouldExport", BoolArgumentType.bool())
								.executes((context) -> {
									boolean shouldExport = BoolArgumentType.getBool(context, "shouldExport");
									Mod mod = ModArgumentType.getMod(context, "mod");

									mod.export(shouldExport);
									context.getSource().sendFeedback(shouldExport ?
											Text.translatable("commands.textureexport.should_export", mod.id()) :
											Text.translatable("commands.textureexport.should_not_export", mod.id())
									);
									STACK_DIRTY = true;
									return 0;
								})
						)
				)
		);

		dispatcher.register(ClientCommandManager.literal("openExportScreen").executes(context -> {
			client.send(() -> client.setScreen(new ExportScreen()));
			return 0;
		}));
	}


	public static void startExport() {
		LOGGER.info("Starting export");
		STACK_DIRTY = true;
		SHOULD_EXPORT = true;
	}

	public static void stopExport() {
		LOGGER.info("Stopping export");
		SHOULD_EXPORT = false;
	}

	public static int buildItemStack() {
		LOGGER.info("Creating Item Stack");

		ITEMS.clear();
		int mods = 0;
		for (Mod mod : MODS.values()) {
			if (mod.export()) {
				for (Identifier item : mod.items()) {
					LOGGER.debug("Adding item {}", ITEMS);
					ITEMS.push(item);
				}
				++mods;
			}
		}

		EXPORTED_TEXTURES = 0;
		TOTAL_TEXTURES = ITEMS.size();
		STACK_DIRTY = false;
		LOGGER.info("Finished Building Stack");

		return mods;
	}
}
