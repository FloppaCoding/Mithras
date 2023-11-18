package floppacoding.mithras.commands.parsers

import floppacoding.mithras.commands.parsers.impl.*

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

    companion object {

        /**
         * Map of parsers to use for command creation using command functions
         */
        val parserMap = mutableMapOf<Class<*>, Parser<*>>(
            GreedyString::class.java to GreedyStringParser,
            String::class.java to StringParser,
            Int::class.java to IntParser,
            Long::class.java to LongParser,
            Float::class.java to FloatParser,
            Double::class.java to DoubleParser
        )

        /**
         * So you can register your own parsers if there isn't anything you need
         */
        inline fun <reified T> registerParser(vararg parsers: Parser<T>) {
            for (parser in parsers) {
                parserMap[T::class.java] = parser
            }
        }
    }
}
