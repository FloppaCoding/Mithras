package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.math.Direction
import java.awt.Color
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * A collection of generally useful extension functions.
 *
 * @author Aton
 * @author Stivais
 */
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

    /**
     * Checks whether this class is the same or a subclass of one of [other].
     *
     * Use the spread operator * to pass arrays.
     *
     *      clazz.isSubclassOfOneOf(*arrayOf(A::class, B::class))
     *
     * @see isSubclassOf
     */
    fun KClass<*>.isSubclassOfOneOf(vararg other: KClass<*>): Boolean {
        return other.any {
            this.isSubclassOf(it)
        }
    }

    /**
     * Test whether the String contains one of the stings in the list.
     */
    fun String.containsOneOf(vararg options: String, ignoreCase: Boolean = false): Boolean {
        return this.containsOneOf(options.toSet(),ignoreCase)
    }

    /**
     * Test whether the String contains one of the stings in the list.
     */
    fun String.containsOneOf(options: Collection<String>, ignoreCase: Boolean = false): Boolean {
        options.forEach{
            if (this.contains(it, ignoreCase)) return true
        }
        return false
    }

    fun <K, V> MutableMap<K, V>.removeIf(filter: (Map.Entry<K, V>) -> Boolean) : Boolean {
        Objects.requireNonNull(filter)
        var removed = false
        val each: MutableIterator<Map.Entry<K, V>> = this.iterator()
        while (each.hasNext()) {
            if (filter(each.next())) {
                each.remove()
                removed = true
            }
        }
        return removed
    }


    fun Color.withAlpha(alpha: Int) = Color(this.red, this.green, this.blue, alpha)

    // TODO move this to a fitting class.
    @JvmStatic
    val HORIZONTALS: List<Direction> = listOf(Direction.SOUTH, Direction.WEST, Direction.NORTH, Direction.EAST)


    // TODO rename this
    /**
     * Used for executors
     */
    inline val Int.seconds: Long
        get() = (this * 1_000_000_000).toLong()

    // TODO move this to a fitting class and maybe rename it?.
    fun setScreen(screen: Screen) {
        mc.send {
            mc.setScreen(screen)
        }
    }

}