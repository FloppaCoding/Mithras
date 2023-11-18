package floppacoding.mithras.commands.parsers.impl

import floppacoding.mithras.commands.parsers.Parser

/**
 * @see GreedyStringParser
 */
data class GreedyString(val string: String)

/**
 * Takes remaining arguments and combines them
 * For example: this is used to be able to write sentences in commands
 *
 * @see CommandFunction.parseArgs
 */
object GreedyStringParser : Parser<GreedyString> {
    override fun parse(args: String): GreedyString {
        return GreedyString(args)
    }

    override fun consumesAll(): Boolean = true
}
