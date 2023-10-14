package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen

object KeepMousePosition : Module (
    "Keep Mouse Position",
    category = Category.MISC,
    description = "Keeps the mouse position when changing the inventory."
) {
    fun shouldKeepMousePosition(): Boolean = this.enabled && mc.currentScreen is GenericContainerScreen
}