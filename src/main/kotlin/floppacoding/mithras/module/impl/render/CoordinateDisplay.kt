package floppacoding.mithras.module.impl.render

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.RegisterHudElement
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.hud.HudElement
import floppacoding.mithras.ui.nanovg.NVGR
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.util.hit.HitResult
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * Render a coordinate hud on your screen
 * @author Aton
 */
object CoordinateDisplay : Module(
    "Coordinate HUD",
    category = Category.RENDER,
    description = "Renders your coordinates on your screen."
) {
    private val showLookingAt by BooleanSetting("Looking At", false, description = "Displays the coordinates of the block you are looking at in a second line.")

    @RegisterHudElement
    object CoordinateHUD : HudElement(this, 0f, 150f,
        NVGR.textWidth("123 / 12 / 123 (12.3 / 12.3)"),
        NVGR.DEFAULT_FONT_HEIGHT,
    ) {
        override fun renderHud(context: DrawContext) {

            val player: ClientPlayerEntity = mc.player ?: return

            var xDir = ((player.yaw % 360 + 360) % 360).toDouble()
            if (xDir > 180) xDir -= 360.0
            xDir = (xDir * 10.0).roundToInt().toDouble() / 10.0
            val yDir = (player.pitch * 10.0).roundToInt().toDouble() / 10.0

            val coordText =
                "${floor(player.x).toInt()} / ${floor(player.y).toInt()} / ${floor(player.z).toInt()} ($xDir / $yDir)"

            NVGR.text(coordText, 0f, 0f, -1)

            // handle looking at
            if (showLookingAt) {
                val la = mc.crosshairTarget

                if (la != null && la.type == HitResult.Type.BLOCK) {
                    val laText = "Looking at: ${la.pos.x.format(2)} / ${la.pos.y.format(2)} / ${la.pos.z.format(2)}"
                    NVGR.text(laText, 0f, NVGR.DEFAULT_FONT_HEIGHT + 1, -1)
                }
            }

            this.width = NVGR.textWidth(coordText)
        }
        private fun Double.format(digits: Int) = "%.${digits}f".format(this)
    }
}