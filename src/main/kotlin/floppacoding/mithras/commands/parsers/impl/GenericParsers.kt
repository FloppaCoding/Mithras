package floppacoding.mithras.commands.parsers.impl

import floppacoding.mithras.commands.parsers.Parser

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

object LongParser : Parser<Long> {
    override fun parse(args: String): Long? {
        return args.toLongOrNull()
    }
}

object FloatParser : Parser<Float> {
    override fun parse(args: String): Float? {
        return args.toFloatOrNull()
    }
}

object DoubleParser : Parser<Double> {
    override fun parse(args: String): Double? {
        return args.toDoubleOrNull()
    }
}
