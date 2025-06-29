package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.AreaChangeEvent
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.RegisterHudElement
import floppacoding.mithras.module.settings.Visibility
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.ui.hud.HudElement
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.SkyblockArea
import meteordevelopment.orbit.EventHandler
import net.minecraft.client.gui.DrawContext
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.text.Text
import net.minecraft.util.math.Box
import java.awt.Color

/**
 * A module to render a small hud element showing your tree gift contribution.
 * That way you do not have to look down.
 *
 * @author Aton
 */
object TreeGiftHud: Module(
    "Tree Gift Hud",
    category = Category.MISC,
    description = "Renders a HUD element showing your contribution percentage for nearby tree gifts."
) {
    private val range by NumberSetting<Double>("Range", 10.0, 3.0, 20.0, 1.0, description = "Detection range for the tree gift.")
    private val backgroundColor by ColorSetting("BackgroundColor", Color(0, 0, 0, 150), true, description = "Color background for the text.", visibility = Visibility.ADVANCED_ONLY)

    private val contributions: MutableList<Text> = mutableListOf()

    @EventHandler
    private fun onTick(event: ClientTickEvent) {
        if (!LocationManager.inArea(SkyblockArea.GALATEA)) return
        val player = mc.player ?: return
        val box = player.boundingBox?.expand(range) ?: return

        contributions.clear()
        mc.world?.getEntitiesByClass(ArmorStandEntity::class.java, box) { entity ->
            entity.customName?.string?.matches(Regex("(?:by|and) (?:[\\w\\[\\]+]* )?${player.name.string}")) == true
        }?.forEach { entity ->


            val searchBox = Box(
                entity.x - 0.5,
                entity.y,
                entity.z - 0.5,
                entity.x + 0.5,
                entity.y+1,
                entity.z + 0.5
            )
            val stands = mc.world!!.getEntitiesByClass(
                ArmorStandEntity::class.java, searchBox
            ) {entity.hasCustomName()}
            if (stands.isEmpty()) return@forEach

            contributions.add(stands[0].name)
        }
    }

    @EventHandler
    private fun onAreaChange(event: AreaChangeEvent) {
        if (event.newArea != SkyblockArea.GALATEA) {
            Mithras.EVENT_BUS.unsubscribe(TreeGiftHudElement)
            contributions.clear()
        }
        else Mithras.EVENT_BUS.subscribe(TreeGiftHudElement)
    }

    @RegisterHudElement
    object TreeGiftHudElement : HudElement(
        TreeGiftHud, 0f, 150f,
        DEFAULT_RENDERER.textWidth("FIG TREE 23%"),
        DEFAULT_RENDERER.defaultFontHeight,
    ) {
        override fun renderHud(context: DrawContext) {
            if (contributions.isEmpty()) return
            var longest = 0f
            contributions.forEach { text ->
                val textWidth = renderer.textBounds(text.string).width()
                if (textWidth > longest) longest = textWidth
            }
            renderer.rect(-2f, -2f, longest + 4f, contributions.size * (renderer.defaultFontHeight + 1f) + 3f, backgroundColor.rgb)

            contributions.withIndex().forEach { (index, text) ->
                renderer.text(text, 0f, index*(renderer.defaultFontHeight + 1f), -1)
            }
        }
    }
}