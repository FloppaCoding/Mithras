package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.NumberSetting

/**
 * A simple gamma override module.
 * @author Aton
 */
object Fullbright : Module(
    "Fullbright",
    category = Category.RENDER,
    description = "Increases the games brightness."
) {
    private var gamma by NumberSetting("Gamma", 2f, 0f, 15f, increment = 0.01f, description = "Tha gamma value to use.")

    /**
     * Returns the value that the games gamma option should be overridden to.
     * Returns null if it should not be overridden.
     * @see floppacoding.mithras.mixin.render.LightmapTextureManagerMixin.gammaOverride
     */
    fun gammaOverride(): Float? {
        return if (this.enabled)
            gamma
        else
            null
    }
}