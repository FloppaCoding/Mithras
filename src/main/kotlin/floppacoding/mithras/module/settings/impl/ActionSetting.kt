package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility

/**
 * A clickable setting for Modules that runs code on click.
 *
 * Represented by a button in the GUI which when clicked will invoke [value].
 * @author Aton
 */
class ActionSetting(
    name: String,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
    override val default: () -> Unit = {}
) : Setting<() -> Unit>(name, visibility, description) {

    override var value: () -> Unit = default

    /**
     * Nothing to read here.
     */
    override fun updateFromConfigSetting(configSetting: Setting<*>) {}

    var action: () -> Unit by this::value

    fun doAction() {
        action()
    }
}
