package floppacoding.mithras.commands.impl

import com.mojang.brigadier.arguments.StringArgumentType.getString
import com.mojang.brigadier.arguments.StringArgumentType.greedyString
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import floppacoding.mithras.commands.CommandFunction
import floppacoding.mithras.utils.ChatUtils.modMessage
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

object TestCommand : FloppaCommand {
    override val command: Subcommand =
        "mtest" {
            execute(paramName = "x, y, z") { x: Float, y: Float, z: Float ->
                modMessage("x: $x, y: $y, z: $z")
            }
            "literal" {
                execute {
                    modMessage("literal 1")
                }
                "literaltwo" {
                    execute {
                        modMessage("literal 2")
                    }
                }
            }
        }
}

// Add good comment
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

// Add good comment
class Subcommand(val builder: LiteralBuilder): CommandBuilder {

    override fun command(name: String, block: Subcommand.() -> Unit): Subcommand {
        return super.command(name, block).also {
            builder.then(it.builder)
        }
    }

    internal fun execute(paramName: String, block: Function<*>) {
        val function = CommandFunction(block)
        val reqBuilder = RequiredBuilder.argument<FabricClientCommandSource, String>(paramName, greedyString())
        reqBuilder.executes {
            function.invoke(getString(it, paramName))
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
