package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module

object SmoothTransfer : Module(
    "Smooth Warp",
    category = Category.MISC,
    description = "Hides the loading screen when changing world."
) {
    fun shouldHideLoadingScreen(): Boolean = this.enabled
}