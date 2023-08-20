package floppacoding.mithras.module.impl.player

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module

/**
 * Prevents scrolling in the hotbar.
 * @author Aton
 */
object DisableHotbarScroll : Module(
    "No Hotbar Scroll",
    category = Category.PLAYER,
    description = "Disables scrolling in the hotbar."
) {
    /**
     * Returns whether hotbar scrolling should be prevented.
     * @see floppacoding.mithras.mixin.PlayerInventoryMixin.onHotbarScroll
     */
    fun shouldDisableHotbarScroll(): Boolean = this.enabled
}