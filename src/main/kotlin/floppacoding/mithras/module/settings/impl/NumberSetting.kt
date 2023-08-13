package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility
import net.minecraft.util.math.MathHelper
import kotlin.math.round

/**
 * A Double Setting for Modules.
 *
 * Represented in the GUI by a slider.
 * To use a different type adjust the [increment] and use the toInt() etc. methods.
 */
class NumberSetting(
    name: String,
    override val default: Double = 1.0,
    val min: Double = -10000.0,
    val max: Double = 10000.0,
    val increment: Double = 1.0,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
) : Setting<Double>(name, visibility, description) {

    override var value: Double = default
        set(newVal) {
            field = MathHelper.clamp(roundToIncrement(processInput(newVal)), min, max)
        }

    private fun roundToIncrement(x: Double): Double {
        return round(x / increment) * increment
    }
}