package floppacoding.mithras.commands

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.minecraft.command.CommandSource
import java.util.concurrent.CompletableFuture

class StringSelectionArgumentType(
    private val optionProvider: StringSelectionSuggester
): ArgumentType<String> {

    override fun parse(reader: StringReader): String {
        return reader.readString()
    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val list = optionProvider?.invoke(context)
        return if (list == null) Suggestions.empty()
        else CommandSource.suggestMatching(list, builder)
    }

    companion object {
        @JvmStatic
        fun getString(context: CommandContext<*>, name: String?): String {
            return context.getArgument(name, String::class.java)
        }
    }
}

typealias StringSelectionSuggester = ((context: CommandContext<*>) -> List<String>?)?