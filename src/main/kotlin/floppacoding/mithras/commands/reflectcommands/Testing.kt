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

/**
 * Base of using reflection's for clean dsl code or whaetever make commenets layter
 */
class CommandFunction(function: Function<*>) {

    internal val function: JavaFunction
    internal val parameters: Array<Parameter>
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

    private fun parseArgs(args: MutableList<String>): Array<Any>? {
        val mutableList = mutableListOf<Any>()

        for (i in parsers) {
            val value = if (!i.consumesArg()) args.joinToString(" ") else args[0]
            mutableList.add(i.parse(value) ?: return null)
            args.removeAt(0)
            if (!i.consumesArg()) {
                break
            }
        }
        return mutableList.toTypedArray()
    }

    fun invoke(string: String) {
        val args = string.split(" ").toMutableList()

        if (args.size == parsers.size || parsers.any { !it.consumesArg() }) {
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
