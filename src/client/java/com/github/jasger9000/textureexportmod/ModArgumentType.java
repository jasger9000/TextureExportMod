package com.github.jasger9000.textureexportmod;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.text.Text;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

import static com.github.jasger9000.textureexportmod.TextureExportModClient.MODS;

public class ModArgumentType implements ArgumentType<Mod> {
    private static final Collection<String> EXAMPLES = Arrays.asList("minecraft", "create", "computercraft");
    public static final SimpleCommandExceptionType NOT_IN_MODLIST = new SimpleCommandExceptionType(Text.translatable("argument.mod.not_in_list"));

    public static Mod getMod(final CommandContext<?> context, final String name) {
        return context.getArgument(name, Mod.class);
    }

    @Override
    public Mod parse(StringReader reader) throws CommandSyntaxException {
        Mod mod = MODS.get(reader.readString());
        if (mod == null) {
            throw NOT_IN_MODLIST.createWithContext(reader);
        }

        return mod;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        String arg = builder.getRemainingLowerCase();

        if (MODS.containsKey(arg)) {
            builder.suggest(arg);
        } else {
            for (String id : MODS.keySet()) {
                if (id.startsWith(arg)) {
                    builder.suggest(id);
                }
            }
        }

        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}