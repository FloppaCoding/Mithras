package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import floppacoding.mithras.commands.CommandBase.Command
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

/**
 * # Superclass for all Commands in the mod
 *
 * Provides functionality for greatly simplifying the syntax of creating new commands.
 *
 * ## Creating a new command
 *
 * To create a new command you have to create class (or object) that inherits from this class.
 * In there, override [builder] to return your desired functionality.
 * Alternatively use the [Command] constructor for a simpler syntax.
 * Then add an Instance of your command to the [commands][MithrasCommandManager.commands]
 * list in the [MithrasCommandManager].
 *
 * @sample floppacoding.mithras.commands.impl.MainCommand.buildCommand
 * @author Aton
 */
abstract class CommandBase {

    abstract val builder: LiteralArgumentBuilder<FabricClientCommandSource?>
    fun buildCommand(): LiteralArgumentBuilder<FabricClientCommandSource?> {
        return builder
    }

    fun command(
        name: String,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ): LiteralArgumentBuilder<FabricClientCommandSource?> {
        val command = LiteralArgumentBuilder.literal<FabricClientCommandSource>(name)

        (command as ArgumentBuilder<FabricClientCommandSource, *>).tasks()
        return command
    }

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.execute(
        command: (context: CommandContext<FabricClientCommandSource>) -> Unit
    ): T {
        this.executes { command(it); 0 }
        return this
    }

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.literal(
        name: String,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ): T {
        val literal = LiteralArgumentBuilder.literal<FabricClientCommandSource>(name)
                as ArgumentBuilder<FabricClientCommandSource, *>
        literal.tasks()
        this.then(literal)
        return this
    }

    fun <A, T : ArgumentBuilder<FabricClientCommandSource, *>> T.argument(
        name: String,
        type: ArgumentType<A>,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ): T {
        val argument = RequiredArgumentBuilder.argument<FabricClientCommandSource, A>(name, type)
                as ArgumentBuilder<FabricClientCommandSource, *>
        argument.tasks()
        this.then(argument)
        return this
    }

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.integer(
        name: String,
        min: Int = Integer.MIN_VALUE,
        max: Int = Integer.MAX_VALUE,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, IntegerArgumentType.integer(min, max), tasks)

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.string(
        name: String,
        type: StringArgumentType.StringType = StringArgumentType.StringType.QUOTABLE_PHRASE,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, when (type) {
        StringArgumentType.StringType.QUOTABLE_PHRASE -> StringArgumentType.string()
        StringArgumentType.StringType.SINGLE_WORD -> StringArgumentType.word()
        StringArgumentType.StringType.GREEDY_PHRASE -> StringArgumentType.greedyString()
    }, tasks)

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.double(
        name: String,
        min: Double = Double.MIN_VALUE,
        max: Double = Double.MAX_VALUE,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, DoubleArgumentType.doubleArg(min, max), tasks)

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.bool(
        name: String,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, BoolArgumentType.bool(), tasks)

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.float(
        name: String,
        min: Float = Float.MIN_VALUE,
        max: Float = Float.MAX_VALUE,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, FloatArgumentType.floatArg(min, max), tasks)

    fun <T : ArgumentBuilder<FabricClientCommandSource, *>> T.long(
        name: String,
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE,
        tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit
    ) = this.argument(name, LongArgumentType.longArg(min, max), tasks)

open class Command(name: String, tasks: ArgumentBuilder<FabricClientCommandSource, *>.() -> Unit) : CommandBase() {
    override val builder: LiteralArgumentBuilder<FabricClientCommandSource?> = command(name, tasks)
}

// The code below does not quite work so far. It cleans up some things tho and maybe it can be made to work but I cba to
// fix it rn

    /*
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
    }*/


}