package floppacoding.mithras.commands.reflectcommands

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

object StringParser : Parser<String> {
    override fun parse(args: String): String {
        return args
    }
}

object IntParser : Parser<Int> {
    override fun parse(args: String): Int? {
        return args.toIntOrNull()
    }
}

object FloatParser : Parser<Float> {
    override fun parse(args: String): Float? {
        return args.toFloatOrNull()
    }
}

/**
 * Interface to create parsers for [CommandFunction].
 *
 *
 * @see CommandFunction
 * @see IntParser
 * @see GreedyStringParser
 * @param E the class the parser parses to
 */
interface Parser<E> {
    /**
     * Function on how to take a string and convert it into corresponding class
     */
    fun parse(args: String): E?

    /**
     * If the parser takes all the remaining arguments or not
     *
     * @see GreedyStringParser
     * @see CommandFunction.parseArgs
     */
    fun consumesAll(): Boolean = false
}
