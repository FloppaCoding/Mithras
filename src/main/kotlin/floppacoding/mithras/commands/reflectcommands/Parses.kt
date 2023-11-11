package floppacoding.mithras.commands.reflectcommands

data class GreedyString(val string: String)

object GreedyStringParser : Parser<GreedyString> {
    override fun parse(args: String): GreedyString {
        return GreedyString(args)
    }

    override fun consumesArg(): Boolean = false
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

interface Parser<E> {
    fun parse(args: String): E?
    fun consumesArg(): Boolean = true
}
