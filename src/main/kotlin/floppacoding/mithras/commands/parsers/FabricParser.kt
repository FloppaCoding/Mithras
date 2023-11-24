package floppacoding.mithras.commands.parsers

import com.mojang.brigadier.context.CommandContext
import floppacoding.mithras.commands.RequiredBuilder

interface FabricParser<T> { // alot of bloat idk how to reduce
    /** Name of the argument/command used for [getValue] */
    val name: String
    val builder: RequiredBuilder
    fun getValue(ctx: CommandContext<*>): T // gets value of parser
    fun copy(name: String): FabricParser<T> // best way I thought of to copy from the map

    companion object {
        private val parserMap = mapOf<Class<*>, FabricParser<*>>(
            Int::class.java to IntParser(""),
            String::class.java to StringParser(""),
            GreedyString::class.java to GreedyStringParser("")
        )

        fun get(clazz: Class<*>, name: String): FabricParser<*>? {
            return parserMap[clazz]?.copy(name)
        }
    }
}
