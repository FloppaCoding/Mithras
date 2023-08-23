package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.client.gui.screen.Screen
import java.awt.Color

object Extensions {

    /**
     * Checks whether any of the inputs in [other] fulfils structural equality (==).
     *
     * Important: collections as input may lead to unexpected behaviour. Use the spread operator on arrays:
     *
     *      obj.equalsOneOf(*arrayOf(Obj2, Obj3))
     *
     * @see identicalToOneOf
     */
    fun Any?.equalsOneOf(vararg other: Any): Boolean {
        return other.any {
            this == it
        }
    }

    /**
     * Checks whether any of the inputs in [other] fulfils referential equality (===).
     *
     * Important: collections as input may lead to unexpected behaviour. Use the spread operator on arrays:
     *
     *      obj.identicalToOneOf(*arrayOf(Obj2, Obj3))
     *
     * @see equalsOneOf
     */
    fun Any?.identicalToOneOf(vararg other: Any): Boolean {
        return other.any {
            this === it
        }
    }

    fun Color.withAlpha(alpha: Int) = Color(this.red, this.green, this.blue, alpha)


    // TODO rename this
    /**
     * Used for executors
     */
    inline val Int.seconds: Long
        get() = (this * 1_000_000_000).toLong()

    fun setScreen(screen: Screen) {
        mc.send {
            mc.setScreen(screen)
        }
    }

}