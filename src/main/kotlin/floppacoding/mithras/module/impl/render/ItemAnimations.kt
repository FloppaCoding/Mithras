package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Arm
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RotationAxis
import kotlin.math.exp

/**
 * Module to change the appearance of held items.
 *
 * @author Aton
 */
object ItemAnimations : Module(
    "Animations",
    category = Category.RENDER,
    description = "Changes the appearance of held items."
) {

    private val size : Float by NumberSetting("Size", 0.0f, -1.5f, 1.5f, 0.05f, description = "Scales the size of your currently held item. Default: 0")
    private val scaleSwing: Boolean by BooleanSetting("Scale Swing", true, description = "Also scale the size of the swing animation.")
    private val oldSwing: Boolean by BooleanSetting("1.8 Swing", true, description = "Uses the 1.8.9 swing animation.")
    private val disableEquip: Boolean by BooleanSetting("Disable Equip", false, description = "Disables the Item Equip animation.")
    private val x: Double by NumberSetting("X", 0.0, -3.0, 3.0, 0.05, description = "Moves the held item. Default: 0")
    private val y: Double by NumberSetting("Y", 0.0, -2.0, 2.0, 0.05, description = "Moves the held item. Default: 0")
    private val z: Double by NumberSetting("Z", 0.0, -0.5, 3.0, 0.05, description = "Moves the held item. Default: 0")
    private val yaw   : Float by NumberSetting("Yaw", 0.0f, -180.0f, 180.0f, 5.0f, description = "Rotates your held item. Default: 0")
    private val pitch : Float by NumberSetting("Pitch", 0.0f, -180.0f, 180.0f, 5.0f, description = "Rotates your held item. Default: 0")
    private val roll  : Float by NumberSetting("Roll", 0.0f, -180.0f, 180.0f, 5.0f, description = "Rotates your held item. Default: 0")

    /**
     * Modifies the position, angle and scale of the held item.
     */
    fun itemTransformHook(matrices: MatrixStack, arm: Arm, swingProgress: Float) {
        if (!this.enabled) return
        val scale = exp(size)
        if (this.scaleSwing) {
            val bl3 = arm == Arm.RIGHT
            val i = if (bl3) 1 else -1
            val f = -0.4f * MathHelper.sin(MathHelper.sqrt(swingProgress) * 3.1415927f) * (scale - 1)
            val g = 0.2f * MathHelper.sin(MathHelper.sqrt(swingProgress) * 6.2831855f) * (scale - 1)
            val h = -0.2f * MathHelper.sin(swingProgress * 3.1415927f) * (scale - 1)
            matrices.translate(i.toFloat() * f, g, h)
        }
        if (arm == Arm.RIGHT) {
            matrices.translate(x* 0.56, y*0.52, z* -0.72)
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(yaw))
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(pitch))
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(roll))
        }
        matrices.scale(scale, scale, scale)
    }

    /**
     * Scales the item equip animation, or disables it.
     * This animation is part of the 1.9+ item swing.
     */
    fun equipProgressTransform(matrices: MatrixStack, equipProgress: Float) {
        if (!this.enabled) return
        if (disableEquip) {
            matrices.translate(0f, - equipProgress * -0.6f, 0f)
        }else if (scaleSwing) {
            val scale = exp(size)
            matrices.translate(0f, -(1 - scale) * equipProgress * -0.6f, 0f)
        }
    }

    /**
     * Returns whether the 1.8 swing animation should be used.
     * @see floppacoding.mithras.mixin.render.HeldItemRendererMixin.tweakSwing
     */
    fun doOldSwing(): Boolean {
        return this.enabled && oldSwing
    }
}