package floppacoding.mithras.commands.reflectcommands

import java.lang.invoke.MethodHandles
import java.lang.reflect.Parameter

fun main() {

    test { x: Float, str: GreedyString ->
        println("$x ${str.string}")
    }.invoke("3 a a a") // prints: 3.0 a a a

    test { x: Float, str: String ->
        println("$x $str")
    }.invoke("3 a a a") // fails to print because arg size exceeds parameter size
}

fun test(function: Function<*>): CommandFunction {
    return CommandFunction(function)
}

// redo this to make it clear that the string/argument inputted is used as a list
/**
 * A class for invoking [functions][Function] with custom arguments using a string,
 * which help provide a sleek way to create commands.
 *
 * It uses reflection to get the parameters you provide,
 * creates [parsers][Parser] that allow converting a string into the arguments needed.
 *
 * Example:
 * ```
 *  // example function, you'd want to use this as a way to invoke and to be the commands
 *  fun command(block: Function<*>): CommandFunction = CommandFunction(block)
 *
 *  // Takes an int and a float
 *  command { param1: Int, param2: Float -> println("int: $param1, float: $param2") }
 *  command.invoke("32 24.0") // prints: `int: 32, float: 24.0`
 *
 *  // takes 2 strings
 *  command { str1: String, str2 -> println("string 1: $str1, string 2: $str2) }
 *  command.invoke("first second") // prints: `string 1: first, string 2: second`
 *
 *  // takes all strings in argument
 *  command { str: GreedyString -> println("greedy string: ${str.string}") }
 *  command.invoke("doesn't matter how much arguments there are because it combines them into one")
 *  // prints: `greedy string: doesn't matter how much arguments there are because it combines them into one`
 * ```
 *
 * @author Stivais
 * @see invoke
 * @see createParsers
 * @see parseArgs
 */
class CommandFunction(function: Function<*>) { // maybe rename

    /** Function used to invoke the method, this has to be used otherwise compiler will error. */
    internal val function: JavaFunction

    /** Functions parameters to create the parsers */
    internal val parameters: Array<Parameter>

    /** Function's parsers */
    internal val parsers: ArrayList<Parser<*>>

    init {
        try {
            val method = function.javaClass.declaredMethods[1]
            if (!method.isAccessible) method.isAccessible = true

            val methodHandle = MethodHandles.lookup().unreflect(method).bindTo(function)
            this.function = JavaFunction { return@JavaFunction methodHandle.invokeWithArguments(*it) }
            this.parameters = method.parameters
            this.parsers = createParsers(method.parameters)
        } catch (e: Exception) {
            throw Throwable("Error while creating command!", e)
        }
    }

    /**
     * Takes the parsers for this class and creates an array of values to be invoked in at [this function][function]
     *
     * @return Array of values or null if it failed to.
     */
    private fun parseArgs(args: MutableList<String>): Array<Any>? {
        val mutableList = mutableListOf<Any>()

        for (i in parsers) {
            val value = if (i.consumesAll()) args.joinToString(" ") else args[0]
            mutableList.add(i.parse(value) ?: return null)
            args.removeAt(0)

            if (i.consumesAll()) {
                break
            }
        }
        return mutableList.toTypedArray()
    }

    /**
     * Takes a string, splits it up into a list to parse at [parseArgs] and invokes it.
     */
    fun invoke(string: String) {
        val args = string.split(" ").toMutableList()

        if (args.size == parsers.size || parsers.any { it.consumesAll() }) {
            val value = parseArgs(args) ?: return println("Args don't match")
            function.apply(value)
        } else {
            println("Arg size don't match")
        }
    }
}

/**
 * Uses a function's parameters to create a list of parsers
 * that handle converting a string into argument's for the function
 *
 * @param parameters List of parameters from a function
 */
fun createParsers(parameters: Array<Parameter>): ArrayList<Parser<*>> {
    val arrayList = arrayListOf<Parser<*>>()

    for (i in parameters) {
        val parser = when (i.type) {
            // add an error if you use greedy as not last since it crashes and also is unintended
            GreedyString::class.java -> GreedyStringParser
            String::class.java -> StringParser
            Float::class.java -> FloatParser
            Int::class.java -> IntParser
            else -> throw Throwable("No parser found") // make proper throwables
        }
        arrayList.add(parser)
    }

    return arrayList
}

private typealias JavaFunction = java.util.function.Function<Array<*>, *>
