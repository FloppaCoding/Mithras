package floppacoding.mithras.commands.parsers

import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import floppacoding.mithras.commands.RequiredBuilder

class IntParser(override val name: String) : FabricParser<Int> {
    override val builder: RequiredBuilder = RequiredBuilder.argument(name, IntegerArgumentType.integer())
    override fun getValue(ctx: CommandContext<*>) = IntegerArgumentType.getInteger(ctx, name)
    override fun copy(name: String): IntParser = IntParser(name)
}

class DoubleParser(override val name: String) : FabricParser<Double> {
    override val builder: RequiredBuilder = RequiredBuilder.argument(name, DoubleArgumentType.doubleArg())
    override fun getValue(ctx: CommandContext<*>) = DoubleArgumentType.getDouble(ctx, name)
    override fun copy(name: String): DoubleParser = DoubleParser(name)
}

class StringParser(override val name: String): FabricParser<String> {
    override val builder: RequiredBuilder = RequiredBuilder.argument(name, StringArgumentType.word())
    override fun getValue(ctx: CommandContext<*>): String = StringArgumentType.getString(ctx, name)
    override fun copy(name: String): StringParser = StringParser(name)
}

data class GreedyString(val string: String)

class GreedyStringParser(override val name: String) : FabricParser<GreedyString> {
    override val builder: RequiredBuilder = RequiredBuilder.argument(name, StringArgumentType.greedyString())
    override fun getValue(ctx: CommandContext<*>): GreedyString = GreedyString(StringArgumentType.getString(ctx, name))
    override fun copy(name: String): GreedyStringParser = GreedyStringParser(name)
}
