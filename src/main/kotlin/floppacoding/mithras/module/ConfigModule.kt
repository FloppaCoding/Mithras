package floppacoding.mithras.module

import floppacoding.mithras.module.settings.Setting

/**
 * A dummy implementation of the [Module] class.
 *
 * This class is made to be used as a dummy class for gson to use when reading the module config.
 * @author Aton
 */
class ConfigModule(
    name: String,
    category: Category = Category.MISC,
    description: String = "",
    keyCode: Int = 0,
    toggled: Boolean = false,
    settings: ArrayList<Setting<*>> = ArrayList(),
) : Module(name, category, description, keyCode, toggled, settings)