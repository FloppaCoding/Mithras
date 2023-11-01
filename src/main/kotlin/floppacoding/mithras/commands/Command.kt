package floppacoding.mithras.commands

import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.arguments.StringArgumentType.StringType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.command.argument.BlockPosArgumentType
import net.minecraft.command.argument.PosArgument
import net.minecraft.command.argument.Vec3ArgumentType
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

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
 * ### Java Implementations
 * This class is designed for efficient use with Kotlin, but it is also possible ot use it from Java.
 * The syntax just gets more complex. The main differences are:
 * + The Kotlin extension functions defined here take the instance they are applied to as an additional parameter in Java.
 * + To match the Kotlin return type Unit lambdas in java have to return null (or Unit.INSTANCE).
 *
 * The following example shows how to create a command from java.
 *
 *      public class TestCommand extends Command {
 *          @NotNull
 *          @Override
 *          public LiteralArgumentBuilder<FabricClientCommandSource> getBuilder() {
 *              return command("java_command", a -> {
 *                  execute(a, context -> {
 *                      ChatUtils.modMessage("no extra arguments.");
 *                      return null;
 *                  });
 *                  literal(a, "literal", b -> {
 *                      execute(b, context -> {
 *                          ChatUtils.modMessage("1 extra argument: literal");
 *                          return null;
 *                      });
 *                      integer(b, "number", c -> {
 *                          execute(c, context -> {
 *                              int number = getInteger(context, "number");
 *                              ChatUtils.modMessage("2 extra arguments: literal and integer " + number);
 *                              return null;
 *                          });
 *                          return null;
 *                      });
 *                      return null;
 *                  });
 *                  return null;
 *              });
 *          }
 *      }
 *
 * @sample floppacoding.mithras.commands.impl.MainCommand.builder
 * @author Aton
 */
@Suppress("unused")
abstract class Command {

    /**
     * The starting point of a command.
     */
    abstract val builder: LiteralArgumentBuilder<CmdSource>



    /**
     * Use this to start constructing your command.
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

    @JvmOverloads
    fun ArgBuilder.integer(name: String, min: Int = Integer.MIN_VALUE, max: Int = Integer.MAX_VALUE, tasks: Task) =
        argument(name, IntegerArgumentType.integer(min, max), tasks)

    fun CommandContext<*>.getInteger(name: String): Int {
        return IntegerArgumentType.getInteger(this, name)
    }

    @JvmOverloads
    fun ArgBuilder.string(name: String, type: StringType = StringType.QUOTABLE_PHRASE, tasks: Task) = argument(
        name,
        when (type) {
            StringType.QUOTABLE_PHRASE -> StringArgumentType.string()
            StringType.SINGLE_WORD -> StringArgumentType.word()
            StringType.GREEDY_PHRASE -> StringArgumentType.greedyString()
        },
        tasks
    )

    fun CommandContext<*>.getString(name: String): String {
        return StringArgumentType.getString(this, name)
    }

    @JvmOverloads
    fun ArgBuilder.double(name: String, min: Double = Double.MIN_VALUE, max: Double = Double.MAX_VALUE, tasks: Task) =
        argument(name, DoubleArgumentType.doubleArg(min, max), tasks)

    fun CommandContext<*>.getDouble(name: String): Double {
        return DoubleArgumentType.getDouble(this, name)
    }

    fun ArgBuilder.bool(name: String, tasks: Task) = argument(name, BoolArgumentType.bool(), tasks)

    fun CommandContext<*>.getBool(name: String): Boolean {
        return BoolArgumentType.getBool(this, name)
    }

    @JvmOverloads
    fun ArgBuilder.float(name: String, min: Float = Float.MIN_VALUE, max: Float = Float.MAX_VALUE, tasks: Task) =
        argument(name, FloatArgumentType.floatArg(min, max), tasks)

    fun CommandContext<*>.getFloat(name: String): Float {
        return FloatArgumentType.getFloat(this, name)
    }

    @JvmOverloads
    fun ArgBuilder.long(name: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, tasks: Task) =
        argument(name, LongArgumentType.longArg(min, max), tasks)

    fun CommandContext<*>.getLong(name: String): Long {
        return LongArgumentType.getLong(this, name)
    }

    fun ArgBuilder.blockPos(name: String, tasks: Task) = argument(name, BlockPosArgumentType.blockPos(), tasks)

    fun CommandContext<*>.getBlockPos(name: String): BlockPos {
        return (this.getArgument(name, PosArgument::class.java) as PosArgument)
            .toAbsoluteBlockPos(MinecraftClient.getInstance().player!!.commandSource)
    }

    @JvmOverloads
    fun ArgBuilder.vec3(name: String, centerIntegers: Boolean = true, tasks: Task) =
        argument(name, Vec3ArgumentType.vec3(centerIntegers), tasks)

    fun CommandContext<*>.getVec3(name: String): Vec3d {
            return (this.getArgument(name, PosArgument::class.java) as PosArgument)
                .toAbsolutePos(MinecraftClient.getInstance().player!!.commandSource)
        }

}

// Type aliases for more readable code
typealias CmdSource = FabricClientCommandSource
typealias ArgBuilder = ArgumentBuilder<CmdSource, *>
typealias Task = ArgumentBuilder<CmdSource, *>.() -> Unit