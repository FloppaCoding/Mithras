package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource


// The code below does not quite work so far. It cleans up some things tho and maybe it can be made to work but I cba to
// fix it rn

class MithrasCommandBuilder private constructor(name: String) : LiteralArgumentBuilder<FabricClientCommandSource>(name) {

    fun execute(
        command: (context: CommandContext<FabricClientCommandSource>) -> Unit
    ) {
        this.executes { command(it); 0 }
    }

    fun literal(
        name: String,
        tasks: LiteralArgumentBuilder<FabricClientCommandSource>.() -> Unit
    ) {
        val literal = literal<FabricClientCommandSource>(name)
        literal.tasks()
        this.then(literal)
    }

    fun <A> argument(
        name: String,
        type: ArgumentType<A>,
        tasks: RequiredArgumentBuilder<FabricClientCommandSource, A>.() -> Unit
    ){
        val argument = RequiredArgumentBuilder.argument<FabricClientCommandSource, A>(name, type)
        argument.tasks()
        this.then(argument)
    }

    fun integer(
        name: String,
        tasks: RequiredArgumentBuilder<FabricClientCommandSource, Int>.() -> Unit
    ) = this.argument(name, IntegerArgumentType.integer(), tasks)

    fun string(
        name: String,
        tasks: RequiredArgumentBuilder<FabricClientCommandSource, String>.() -> Unit
    ) = this.argument(name, StringArgumentType.string(), tasks)

    companion object {
        fun command(
            name: String,
            tasks: MithrasCommandBuilder.() -> Unit
        ): LiteralArgumentBuilder<FabricClientCommandSource?> {
            val command = MithrasCommandBuilder(name)
            command.tasks()
            return command.`this`
        }
    }
}



