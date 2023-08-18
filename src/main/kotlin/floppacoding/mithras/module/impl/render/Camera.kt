package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import net.minecraft.client.option.Perspective

/**
 * A module to improve how the game is viewed through the camera.
 */
object Camera : Module(
    "Camera",
    category = Category.RENDER,
    description = "A module to give a cleaner gameplay experience."
) {
    private val removeFire      by BooleanSetting("Remove Fire Overlay", true, description = "Prevents the fire overly from rendering.")
    private val skipFrontView   by BooleanSetting("Skip Front View", true, description = "Skips the front view when toggling the perspective.")
    private val noHurtCam       by BooleanSetting("No Hurt Tilt", true, description = "Disables the camera tilt when taking damage.")
    private val disabnleBobbing by BooleanSetting("No Screen Bobbing", true, description = "Disables the screen from bobbing when view bobbing is enables, but does not stop the hand from bobbing.")

    /**
     * Hook to determine whether the perspective that is about to be set should be skipped.
     * @see floppacoding.mithras.mixin.MinecraftClientMixin.modifyPerspective
     */
    fun shouldSkipPerspective(perspective: Perspective) : Boolean {
        return this.enabled && this.skipFrontView && perspective == Perspective.THIRD_PERSON_FRONT
    }

    /**
     * Returns whether the fire overlay should be disabled.
     * @see floppacoding.mithras.mixin.render.InGameOverlayRendererMixin.skipFireOverlay
     */
    fun shouldDisableFireOverlay(): Boolean = this.enabled && removeFire

    /**
     * Returns whether the screen tilt when hurt should be prevented.
     * @see floppacoding.mithras.mixin.render.GameRendererMixin.preventHurtTilt
     */
    fun shouldDisableHurtTilt(): Boolean = this.enabled && noHurtCam

    /**
     * Returns whether view bobbing should happen.
     * @see floppacoding.mithras.mixin.render.GameRendererMixin.onBobView
     */
    fun shouldAllowViewBobbing(): Boolean = !this.enabled || !disabnleBobbing
}