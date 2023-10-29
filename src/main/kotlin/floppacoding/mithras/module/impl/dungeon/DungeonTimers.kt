package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras
import floppacoding.mithras.events.BlockStateChangeEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.RegisterHudElement
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.RunInformation
import floppacoding.mithras.module.settings.Setting.Companion.withInputTransform
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.ui.hud.HudElement
import floppacoding.mithras.utils.Extensions.format
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.client.gui.DrawContext
import net.minecraft.util.math.BlockPos
import java.awt.Color

object DungeonTimers : Module(
    "Dungeon Timers",
    Category.DUNGEON,
    "This Module provides timers for several events in dungeons."
) {
     private val spiritBearTimer by BooleanSetting("Spirit Bear", true, description = "Shows the time until the spirit bear is about to spawn on the hud.")
         .withInputTransform { state ->
             if (state) {
                 Mithras.EVENT_BUS.subscribe(SpiritBearTimerHUD)
             }else {
                 Mithras.EVENT_BUS.unsubscribe(SpiritBearTimerHUD)
             }

             state
         }
    private val spiritBearTimerColor by ColorSetting("Bear Timer Color", Color(20,80,240), description = "The text color for the spirit bear spawn timer.")

    private val lastThornLightPos = BlockPos(7,77,34)
    private const val SPIRIT_BEAR_SPAWN_DELAY = 3400

    override fun onEnable() {
        Mithras.EVENT_BUS.subscribe(this)
        if (spiritBearTimer) Mithras.EVENT_BUS.subscribe(SpiritBearTimerHUD)
    }

    @EventHandler
    fun onBlockChange(event: BlockStateChangeEvent) {
        if (RunInformation.isInFloor(4) && Dungeon.inBoss) {
            if (event.pos == lastThornLightPos && event.newState.block === Blocks.SEA_LANTERN && event.oldState.block === Blocks.COAL_BLOCK) {
                SpiritBearTimerHUD.arrivalTime = System.currentTimeMillis() + SPIRIT_BEAR_SPAWN_DELAY
            }
        }
    }

    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        SpiritBearTimerHUD.arrivalTime = 0
    }


    @RegisterHudElement
    object SpiritBearTimerHUD : HudElement(
        DungeonTimers, 700f, 500f,
        DEFAULT_RENDERER.textWidth("Spirit Bear: 1.00s"),
        DEFAULT_RENDERER.defaultFontHeight,
    ) {
        var arrivalTime: Long = 0L

        override fun renderHud(context: DrawContext) {
            if (arrivalTime == 0L) return
            val remainig = arrivalTime - System.currentTimeMillis()
            if (remainig <0 ) {
                arrivalTime = 0L
                return
            }

            val text = "Spirit Bear: ${(remainig/1000.0).format(2)}"

            renderer.text(text, 0f, 0f, spiritBearTimerColor.rgb)
        }
    }
}