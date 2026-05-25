package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.Setting.Companion.onSet
import floppacoding.mithras.module.settings.Setting.Companion.withDependency
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.module.settings.impl.SelectorOptions
import floppacoding.mithras.module.settings.impl.SelectorSetting
import net.minecraft.client.option.Perspective
import net.minecraft.client.render.fog.FogRenderer

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
    private val disableBobbing  by BooleanSetting("No Screen Bobbing", true, description = "Disables the screen from bobbing when view bobbing is enables, but does not stop the hand from bobbing.")

    private val fogMode by SelectorSetting("Fog Mode", FogMode.REDUCED, description = "Modifies fog rendering. Fog can be rendered normally, completely hidden, or reduced in intensity.").onSet {
        mode ->
        // This code looks a little weird because the fog state field is private and only accessible by toggling.
        val fog = FogRenderer.toggleFog()
        if(fog != mode.isFogVisible()) {FogRenderer.toggleFog()}
    }
    private val fogIntensity by NumberSetting<Float>("Fog Intensity", 0.5f, 0f, 1f, 0.05f, description = "The fog intensity when Fog Mode reduced is selected.").withDependency {
        fogMode == FogMode.REDUCED
    }

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
    fun shouldAllowViewBobbing(): Boolean = !this.enabled || !disableBobbing

    /**
     * Returns the modified fog intensity.
     * @see floppacoding.mithras.mixin.render.FogRendererMixin.modifyFogColor
     */
    @JvmStatic
    fun fogIntensity(): Float {
        if (!this.enabled) return 1f
        return when (this.fogMode) {
            FogMode.HIDDEN -> 0f
            FogMode.REDUCED -> this.fogIntensity
            else -> 1f
        }
    }

    /**
     * Prevent sodium from occluding chunks normally hidden by fog.
     * @see floppacoding.mithras.mixin.sodium.RenderSectionManagerMixin
     */
    @JvmStatic
    fun shouldPreventFogOcclusion(): Boolean = this.enabled && fogMode !== FogMode.VISIBLE

    enum class FogMode(override val displayName: String): SelectorOptions{
        VISIBLE("visible"), HIDDEN("hidden"), REDUCED("reduced");

        fun isFogVisible() = this != HIDDEN
    }
}