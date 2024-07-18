package com.github.jasger9000.textureexportmod.gui.widgets;

import com.github.jasger9000.textureexportmod.util.Mod;
import com.github.jasger9000.textureexportmod.mixin.client.CheckboxWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

import static com.github.jasger9000.textureexportmod.TextureExportModClient.MODS;
import static com.github.jasger9000.textureexportmod.TextureExportModClient.STACK_DIRTY;

public class ModListWidget extends ElementListWidget<ModListWidget.Entry> {
	private final Entry selectAllEntry;

	public ModListWidget(MinecraftClient client, int x, int y, int width, int height) {
		super(client, width, height, y, 20);
		this.setX(x);
		x += 10; y += 5;

		this.selectAllEntry = new Entry(
				Text.translatable("gui.textureexportmod.select_all"),
				(widget, checked) -> {
					STACK_DIRTY = true;
					for (Entry entry : this.children()) {
						if (checked != entry.checkbox.isChecked()) {
							entry.checkbox.onPress();
						}
					}
				},
                MODS.values().stream().allMatch(Mod::export),
				x - 5,
				y
		);
		this.addEntry(this.selectAllEntry);

		for (Mod mod : MODS.values()) {
			y += 20;

			this.addEntry(new ModListEntry(mod, x, y));
		}
	}

	public class ModListEntry extends ModListWidget.Entry {
		final Mod mod;

		public ModListEntry(Mod mod, int x, int y) {
			super(
					Text.translatable("gui.textureexportmod.mod_entry", mod.displayName(), mod.items().size()),
					(widget, checked) -> {
						mod.export(checked);
						STACK_DIRTY = true;
						if (!checked && selectAllEntry.checkbox.isChecked()) {
							((CheckboxWidgetAccessor) selectAllEntry.checkbox).setChecked(false);
						} else if (checked && MODS.values().stream().allMatch(Mod::export)) {
							((CheckboxWidgetAccessor) selectAllEntry.checkbox).setChecked(true);
						}
					},
					mod.export(),
					x,
					y
			);
			this.mod = mod;
		}


		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
//			context.drawText(client.textRenderer, text, x, y, 0xFFFFFF, true);
			super.render(context, index, y, x, entryWidth, entryHeight, mouseX, mouseY, hovered, tickDelta);
		}
	}

	public class Entry extends ElementListWidget.Entry<ModListWidget.Entry> {
		final CheckboxWidget checkbox;

		public Entry(Text text, CheckboxWidget.Callback callback, boolean checked, int x, int y) {
			this.checkbox = CheckboxWidget
					.builder(text, client.textRenderer)
					.pos(x, y)
					.callback(callback)
					.checked(checked)
					.build();
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			this.checkbox.render(context, mouseX, mouseY, tickDelta);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return List.of(this.checkbox);
		}

		@Override
		public List<? extends Element> children() {
			return List.of(this.checkbox);
		}
	}
}