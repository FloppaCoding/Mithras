package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

interface FloppaCommand : CommandBuilder {
    val command: Subcommand
}

/**
 * Provides [command] for [Subcommand] and [FloppaCommand].
 */
interface CommandBuilder {
    fun command(name: String, block: Subcommand.() -> Unit): Subcommand {
        return Subcommand(LiteralBuilder.literal(name)).also(block)
    }

    fun literal(name: String, block: Subcommand.() -> Unit): Subcommand = command(name, block)

    operator fun String.invoke(block: Subcommand.() -> Unit): Subcommand = command(this, block)
}

class Subcommand(val builder: LiteralBuilder): CommandBuilder {

    override fun command(name: String, block: Subcommand.() -> Unit): Subcommand {
        return super.command(name, block).also {
            builder.then(it.builder)
        }
    }

    internal fun execute(paramName: String, block: Function<*>) {
        val function = CommandFunction(block)
        val reqBuilder =
            RequiredBuilder.argument<FabricClientCommandSource, String>(paramName, StringArgumentType.greedyString())
        reqBuilder.executes {
            function.invoke(StringArgumentType.getString(it, paramName))
            0
        }
        builder.then(reqBuilder)
    }

    internal fun execute(block: () -> Unit) {
        builder.executes {
            block()
            0
        }
    }
}

typealias RequiredBuilder = RequiredArgumentBuilder<FabricClientCommandSource, *>
typealias LiteralBuilder = LiteralArgumentBuilder<FabricClientCommandSource>
