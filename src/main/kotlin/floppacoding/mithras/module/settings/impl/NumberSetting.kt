package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility
import kotlin.math.round
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * A Double Setting for Modules.
 *
 * Represented in the GUI by a slider.
 *
 * Note: Only the types [Double], [Float], [Int], [Long], [Short] and [Byte] are allowed for [E].
 * The compiler does not check this, but it will result in a crash during run time.
 *
 * @author Aton, Stivais
 */
class NumberSetting<E> @Throws(java.lang.IllegalArgumentException::class) constructor(
    name: String,
    default: Number = 1.0,
    min: Number = -10000.0,
    max: Number = 10000.0,
    increment: Number = 1.0,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
    private val clazz: KClass<E>
) : Setting<E>(name, visibility, description) where E : Number, E : Comparable<E> {

    override val default: E = default.toValueType()
    val min: E = min.toValueType()
    val max: E = max.toValueType()
    val increment: E = increment.toValueType()

    override var value: E = this.default
        set(newVal) {
            field = roundToIncrement(processInput(newVal)).toValueType().coerceIn(min, max)
        }

    /**
     * Required for the gui and config.
     */
    var doubleValue: Double
        get() = value.toDouble()
        set(newVal) {
            value = newVal.toValueType()
        }
    val minDouble = min.toDouble()
    val maxDouble = max.toDouble()
    val incrementDouble = increment.toDouble()

    init {
        if (clazz != Double::class && clazz != Float::class && clazz != Int::class && clazz != Long::class && clazz != Short::class && clazz != Byte::class)
            throw IllegalArgumentException("Disallowed Number type used for NumberSetting. Allowed: Double, Float, Int, Long, Byte, Short; Used: ${default.javaClass}.")
    }

    @Throws(java.lang.IllegalArgumentException::class)
    private fun Number.toValueType(): E {
        val temp = when (clazz) {
            Double::class -> this.toDouble()
            Float::class -> this.toFloat()
            Long::class -> this.toLong()
            Int::class -> this.toInt()
            Short::class -> this.toInt().toShort()
            Byte::class -> this.toInt().toByte()
            else -> throw IllegalArgumentException("Disallowed Number type used for NumberSetting. Allowed: Double, Float, Int, Long, Byte, Short; Used: ${default.javaClass}.")
        }
        return clazz.cast(temp)
    }

    private fun roundToIncrement(x: E): Double {
        return round((x.toDouble() / increment.toDouble())) * increment.toDouble()
    }

    private fun E.coerceIn(min: E, max: E): E {
        if (this < min) return min
        if (this > max) return max
        return this
    }
}

inline fun <reified E> NumberSetting(
    name: String,
    default: E = E::class.cast(1.0),
    min: Number = -10000.0,
    max: Number = 10000.0,
    increment: Number = 1.0,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
): NumberSetting<E> where E : Number, E : Comparable<E> {
    return NumberSetting(name, default, min, max, increment, visibility, description, E::class)
}