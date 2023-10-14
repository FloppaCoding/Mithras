package floppacoding.mithras.module.settings.impl

import floppacoding.mithras.module.settings.Setting
import floppacoding.mithras.module.settings.Visibility

/**
 * Provides a Setting which stores a String.
 *
 * This Setting is represented by a text field in the gui.
 * @author Aton
 */
class StringSetting(
    name: String,
    override val default: String = "",
    val length: Int = 30,
    visibility: Visibility = Visibility.VISIBLE,
    description: String? = null,
) : Setting<String>(name, visibility, description) {

    override var value: String = default
        set(newStr) {
            val tempStr = processInput(newStr)
            field = if (tempStr.length > length) {
                tempStr.substring(0, length - 1)
            }else
                tempStr
        }

    override fun updateFromConfigSetting(configSetting: Setting<*>) {
        this.text = (configSetting as StringSetting).text
    }

    var text: String by this::value
}