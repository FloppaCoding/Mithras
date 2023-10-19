package floppacoding.mithras.module.impl.debug

import java.text.NumberFormat
import java.util.*


fun main() {
    val price = 852173387

    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 1
    println(format.format(price))
    val format2 = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.SHORT)
    format2.minimumFractionDigits = 1
    println(format2.format(price))

    val format3 = NumberFormat.getCompactNumberInstance(Locale.US, NumberFormat.Style.LONG)
    format3.minimumFractionDigits = 1
    println(format3.format(price))

}


open class A {
    open fun doStuff() {}
}

class C: B() {
    override fun doStuff() {
        super.doStuff()
    }
}

open class B: A()