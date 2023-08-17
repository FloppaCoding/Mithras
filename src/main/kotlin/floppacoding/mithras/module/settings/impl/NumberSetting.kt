package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility
import kotlin.math.round

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
@Suppress("UNCHECKED_CAST")
class NumberSetting<E> @Throws(java.lang.IllegalArgumentException::class) constructor(
    name: String,
    override val default: E = 1.0 as E, // This type cast works in kotlin, but not in java.
    val min: E = -10000.0 as E,
    val max: E = 10000.0 as E,
    val increment: E = 1.0 as E,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
) : Setting<E>(name, visibility, description) where E : Number, E : Comparable<E> {

    override var value: E = default
        set(newVal) {
            field = (roundToIncrement(processInput(newVal)).toValueType()).coerceIn(min, max)
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
        if (default !is Double && default !is Float && default !is Int && default !is Long && default !is Short && default !is Byte)
            throw IllegalArgumentException("Disallowed Number type used for NumberSetting. Allowed: Double, Float, Int, Long, Byte, Short; Used: ${default.javaClass}.")
    }

    @Throws(java.lang.IllegalArgumentException::class)
    private fun Double.toValueType(): E {
        return when (default::class) {
            Double::class -> this
            Float::class -> this.toFloat()
            Long::class -> this.toLong()
            Int::class -> this.toInt()
            Short::class -> this.toInt().toShort()
            Byte::class -> this.toInt().toByte()
            else -> throw IllegalArgumentException("Disallowed Number type used for NumberSetting. Allowed: Double, Float, Int, Long, Byte, Short; Used: ${default.javaClass}.")
        } as E
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