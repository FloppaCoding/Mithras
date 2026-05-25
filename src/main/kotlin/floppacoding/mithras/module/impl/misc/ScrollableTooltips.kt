package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.GuiMouseScrollEvent
import floppacoding.mithras.mixin.gui.HandledScreenAccessor
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.NumberSetting
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.item.ItemStack
import org.joml.Matrix3x2fStack

/**
 * A module to reposition and scale item tooltips.
 * @author Aton
 */
object ScrollableTooltips : Module(
    "Scrollable Tooltips",
    Category.MISC,
    "Allows you to scroll item tooltips and set a custom scale for them."
) {
    private val scale by NumberSetting("Scale", 1.0f, 0.5, 2.0, 0.01, description = "Item tooltips will be scaled by this number relative to their default size.")

    /**
     * Hook to reposition the item tooltip.
     * @see floppacoding.mithras.mixin.gui.DrawContextMixin.positionTooltip
     */
    fun scaleTooltip(matrices: Matrix3x2fStack, xOffs: Int, yOffs: Int) {
        val stack = (mc.currentScreen as? HandledScreenAccessor)?.focussedSlot?.stack
        if (stack !== lastStack) {
            lastStack = stack
            scrollY = 0f
        }
        matrices.translate((1- scale) * xOffs.toFloat(), (1- scale) * yOffs.toFloat() + scrollY)
        matrices.scale(scale, scale)
    }

    @EventHandler
    fun onScroll(event : GuiMouseScrollEvent) {
        if (event.screen !is HandledScreen<*>) return
        scrollY += event.verticalAmount.toFloat() * 10f
    }


    private var scrollY : Float = 0f
    private var lastStack : ItemStack? = null
}