package floppacoding.mithras.module.impl.player

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module

/**
 * A simple toggle sprint module.
 */
object AutoSprint : Module(
    "Auto Sprint",
    category = Category.PLAYER,
    description = "Makes you always sprint. Pressing the sprint key will not affect your sprint state. " +
            "But you can bind this Module to the sprint key, for it to toggle sprinting."
) {
    /**
     * Returns whether the player should be sprinting.
     * @see floppacoding.mithras.mixin.ClientPlayerEntityMixin.onCheckSprintState
     */
    @JvmStatic
    fun shouldForceSprint(): Boolean = this.enabled
}