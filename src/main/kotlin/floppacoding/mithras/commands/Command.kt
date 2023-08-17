package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.arguments.StringArgumentType.StringType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource

/**
 * # Interface for all Commands in the mod
 *
 * Provides functionality for greatly simplifying the syntax of creating new commands.
 *
 * ## Creating a new command
 *
 * To create a new command you have to create class (or object) that inherits from this interface.
 * In there, override [builder] to return your desired functionality.
 * Then add an Instance of your command to the [commands][MithrasCommandManager.commands]
 * list in the [MithrasCommandManager].
 *
 * @sample floppacoding.mithras.commands.impl.MainCommand.builder
 * @author Aton
 */
interface Command {

    /**
     * The starting point of a command.
     */
    val builder: LiteralArgumentBuilder<CmdSource>

    /**
     * Clean syntax to start your command.
     */
    fun command(name: String, tasks: Task): LiteralArgumentBuilder<CmdSource> {
        val command = LiteralArgumentBuilder.literal<CmdSource>(name)
        command.tasks()
        return command
    }

    fun ArgBuilder.execute(command: (context: CommandContext<CmdSource>) -> Unit): ArgBuilder {
        executes {
            command(it)
            0
        }
        return this
    }

    fun ArgBuilder.literal(name: String, tasks: Task): ArgBuilder {
        val literal = LiteralArgumentBuilder.literal<CmdSource>(name)
        literal.tasks()
        then(literal)
        return this
    }

    fun <A> ArgBuilder.argument(name: String, type: ArgumentType<A>, tasks: Task): ArgBuilder {
        val argument = RequiredArgumentBuilder.argument<CmdSource, A>(name, type)
        argument.tasks()
        then(argument)
        return this
    }

    fun ArgBuilder.integer(
        name: String,
        min: Int = Integer.MIN_VALUE,
        max: Int = Integer.MAX_VALUE,
        tasks: Task
    ) = argument(name, IntegerArgumentType.integer(min, max), tasks)

    fun ArgBuilder.string(
        name: String,
        type: StringType = StringType.QUOTABLE_PHRASE,
        tasks: Task
    ) = argument(
        name,
        when (type) {
            StringType.QUOTABLE_PHRASE -> StringArgumentType.string()
            StringType.SINGLE_WORD -> StringArgumentType.word()
            StringType.GREEDY_PHRASE -> StringArgumentType.greedyString()
        },
        tasks
    )

    fun ArgBuilder.double(
        name: String,
        min: Double = Double.MIN_VALUE,
        max: Double = Double.MAX_VALUE,
        tasks: Task
    ) = argument(name, DoubleArgumentType.doubleArg(min, max), tasks)

    fun ArgBuilder.bool(
        name: String,
        tasks: Task
    ) = argument(name, BoolArgumentType.bool(), tasks)

    fun ArgBuilder.float(
        name: String,
        min: Float = Float.MIN_VALUE,
        max: Float = Float.MAX_VALUE,
        tasks: Task
    ) = argument(name, FloatArgumentType.floatArg(min, max), tasks)

    fun ArgBuilder.long(
        name: String,
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE,
        tasks: Task
    ) = argument(name, LongArgumentType.longArg(min, max), tasks)
}

// Type aliases for more readable code
typealias CmdSource = FabricClientCommandSource
typealias ArgBuilder = ArgumentBuilder<CmdSource, *>
typealias Task = ArgumentBuilder<CmdSource, *>.() -> Unit 

// The code below does not quite work so far. It cleans up some things though, and maybe it can be made to work but I cba to
// fix it rn

    /*
    class MithrasCommandBuilder private constructor(name: String) : LiteralArgumentBuilder<CmdSource>(name) {

        fun execute(
            command: (context: CommandContext<CmdSource>) -> Unit
        ) {
            this.executes { command(it); 0 }
        }

        fun literal(
            name: String,
            tasks: LiteralArgumentBuilder<CmdSource>.() -> Unit
        ) {
            val literal = literal<CmdSource>(name)
            literal.tasks()
            this.then(literal)
        }

        fun <A> argument(
            name: String,
            type: ArgumentType<A>,
            tasks: RequiredArgumentBuilder<CmdSource, A>.() -> Unit
        ){
            val argument = RequiredArgumentBuilder.argument<CmdSource, A>(name, type)
            argument.tasks()
            this.then(argument)
        }

        fun integer(
            name: String,
            tasks: RequiredArgumentBuilder<CmdSource, Int>.() -> Unit
        ) = this.argument(name, IntegerArgumentType.integer(), tasks)

        fun string(
            name: String,
            tasks: RequiredArgumentBuilder<CmdSource, String>.() -> Unit
        ) = this.argument(name, StringArgumentType.string(), tasks)

        companion object {
            fun command(
                name: String,
                tasks: MithrasCommandBuilder.() -> Unit
            ): LiteralArgumentBuilder<CmdSource?> {
                val command = MithrasCommandBuilder(name)
                command.tasks()
                return command.`this`
            }
        }
    }*/