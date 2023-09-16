package floppacoding.mithras.module.impl.debug

import net.minecraft.block.BellBlock
import kotlin.reflect.full.functions


fun main() {

    val clazz = B::class
    val funs = clazz.functions
    BellBlock::class.java.getDeclaredMethod("onUse")

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