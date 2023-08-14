package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

/**
 * # Superclass for all Commands in the mod
 *
 * Provides functionality for greatly simplifying the syntax of creating new commands.
 *
 * ## Creating a new command
 *
 * To create a new command you have to create class (or object) that inherits from this class.
 * In there, override [buildCommand] to return your desired functionality.
 * Then add an Instance of your command to the [commands][MithrasCommandManager.commands]
 * list in the [MithrasCommandManager].
 *
 * @sample floppacoding.mithras.commands.impl.MainCommand.buildCommand
 * @author Aton
 */
abstract class CommandBase {
    abstract fun buildCommand(): LiteralArgumentBuilder<FabricClientCommandSource?>

    protected fun String.literal(
        vararg nextArgs: ArgumentBuilder<FabricClientCommandSource?, *>,
        command: ((context: CommandContext<FabricClientCommandSource>) -> Unit)? = null
    ) : LiteralArgumentBuilder<FabricClientCommandSource?> {
        val result =  ClientCommandManager.literal(this)
        for (next in nextArgs) {
            result.then(next)
        }
        if (command != null)
            result.executes{ command(it); 0 }
        return  result
    }

    protected fun String.literal() : LiteralArgumentBuilder<FabricClientCommandSource?> {
        return ClientCommandManager.literal(this)
    }

    protected fun <T> String.argument(
        type: ArgumentType<T>,
        vararg nextArgs: ArgumentBuilder<FabricClientCommandSource, *>,
        command: ((context: CommandContext<FabricClientCommandSource>) -> Unit)? = null
    ): RequiredArgumentBuilder<FabricClientCommandSource, T>{
        val result =  ClientCommandManager.argument(this, type)
        for (next in nextArgs) {
            result.then(next)
        }
        if (command != null)
            result.executes{ command(it); 0 }
        return  result
    }
}