package floppacoding.mithras.module.impl.debug

import com.google.gson.GsonBuilder

fun main() {

    val b = mapOf("U21" to mapOf("C3" to listOf(Pair("L4", 2), Pair("C1", 1)), "C4" to listOf(Pair("L5", 2), Pair("C2", 1))), "A21" to mapOf("B3" to listOf(Pair("C4", 2), Pair("C1", 1)), "B4" to listOf(Pair("C5", 2), Pair("C2", 1))))
    val c = listOf("ASAA", "asdSD", "SDA")
    val gson = GsonBuilder().setPrettyPrinting().create()

    val jsonString = gson.toJson(b)
    val jsonString2 = gson.toJson(c)
    println(jsonString)

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


class Outer() {

    private object Inner: SpecialType() {

    }
}

open class SpecialType() {}