package floppacoding.mithras.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import floppacoding.mithras.commands.parsers.FabricParser
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import java.lang.invoke.MethodHandles
import java.lang.reflect.Parameter

interface FloppaCommand : CommandBuilder {
    val command: Subcommand
}

interface CommandBuilder {
    fun command(name: String, block: Subcommand.() -> Unit = {}): Subcommand {
        return Subcommand(LiteralBuilder.literal(name)).also(block)
    }
}

class Subcommand(val builder: LiteralBuilder): CommandBuilder {

    override fun command(name: String, block: Subcommand.() -> Unit): Subcommand {
        return super.command(name, block).also {
            builder.then(it.builder)
        }
    }

    internal fun execute(vararg paramNames: String, block: Function<*>) {
        val function = AdvancedExecutable(*paramNames, function = block)
        function.setup(builder)
    }

    internal fun execute(block: () -> Unit) {
        builder.executes {
            block()
            0
        }
    }
}



abstract class BaseCommand(function: Function<*>) {
    internal val function: java.util.function.Function<Array<*>, *>
    internal val parameters: Array<Parameter>

    init {
        try {
            val method = function.javaClass.declaredMethods[1]
            if (!method.isAccessible) method.isAccessible = true

            val methodHandle = MethodHandles.lookup().unreflect(method).bindTo(function)
            this.function = java.util.function.Function { return@Function methodHandle.invokeWithArguments(*it) }
            this.parameters = method.parameters
        } catch (e: Exception) {
            throw Throwable("Error creating command. ${e.message}")
        }
    }
}

class AdvancedExecutable(private vararg val paramNames: String, function: Function<*>) : BaseCommand(function) {

    private val parsers = mutableListOf<FabricParser<*>>()

    fun setup(builder: LiteralBuilder) {
        if (parameters.size != paramNames.size) throw Throwable("Please use correct names amount")

        val builders = MutableList(parameters.size) { // creates a list of parsers (keep in mind it is reversed)
            val i = parameters.size - 1 - it
            FabricParser.get(parameters[i].type, paramNames[i]) ?: throw Throwable("Error creating builder's")
        }
        parsers.addAll(builders.reversed()) // re-reverse it so

        var previous: RequiredBuilder? = null

        while (builders.size != 0) {
            if (previous != null) {
                builders[0].builder.then(previous)
            } else {
                builders[0].builder.executes {
                    function.apply(getValues(it))
                    0
                }
            }

            previous = builders.getOrNull(0)?.builder
            builders.removeAt(0)
        }
        builder.then(parsers[0].builder)
    }

    private fun getValues(ctx: CommandContext<*>): Array<Any> { // gets actual values to invoke into function
        return Array(parsers.size) { index ->
            parsers[index].getValue(ctx) ?: throw Throwable("This shouldn't be possible")
        }
    }
}

typealias RequiredBuilder = RequiredArgumentBuilder<FabricClientCommandSource, *>
typealias LiteralBuilder = LiteralArgumentBuilder<FabricClientCommandSource>
