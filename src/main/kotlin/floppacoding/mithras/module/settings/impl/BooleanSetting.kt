package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility

/**
 * A boolean Setting for modules.
 *
 * Can be used to toggle aspects of a Module.
 * Represented by a toggle button in the GUI.
 * @author Aton
 */
class BooleanSetting (
    name: String,
    override val default: Boolean = false,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
): Setting<Boolean>(name, visibility, description) {

    override var value: Boolean = default
        set(value) {
            field = processInput(value)

        }

    override fun updateFromConfigSetting(configSetting: Setting<*>) {
        this.enabled = (configSetting as BooleanSetting).enabled
    }

    var enabled: Boolean by this::value

    fun toggle() {
        enabled = !enabled
    }
}