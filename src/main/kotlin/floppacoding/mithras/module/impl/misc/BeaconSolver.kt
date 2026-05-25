package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.*
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.RegisterHudElement
import floppacoding.mithras.module.settings.Setting.Companion.withInputTransform
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.ui.hud.HudElement
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.SkyblockArea
import floppacoding.mithras.utils.inventory.ItemUtils.lore
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.sound.SoundCategory
import net.minecraft.util.DyeColor
import java.awt.Color

object BeaconSolver : Module(
    name = "Beacon Solver",
    category = Category.MISC,
    description = "Displays the number of needed clicks to tune the Beacon in Galatea."
) {

    private var showCooldownHUD by BooleanSetting("Show Cooldown HUD", true, description = "renders a HUD element showing the remaining cooldown.").
        withInputTransform { newValue, setting ->
            if (newValue && !setting.enabled && this.enabled && LocationManager.inArea(SkyblockArea.GALATEA)) Mithras.EVENT_BUS.subscribe(BeaconTimer)
            if (!newValue) Mithras.EVENT_BUS.unsubscribe(BeaconTimer)
            return@withInputTransform newValue
        }

    private var inBeacon: Boolean = false
    private var targettColor: DyeColor? = null
    private val positions = mutableListOf<Pair<Int,Long>>()
    private var selectedPitch: BeaconPitch? = null
    private var targetPitch: BeaconPitch? = null
    private var targetSpeed: BeaconSpeed? = null
    private var nextAvaliable = 0L

    override fun onEnable() {
        super.onEnable()
        if (!showCooldownHUD || !LocationManager.inArea(SkyblockArea.GALATEA)) Mithras.EVENT_BUS.unsubscribe(BeaconTimer)
    }

    @EventHandler
    private fun onTick(event: ClientTickEvent) {
        if (!inBeacon || !LocationManager.inArea(SkyblockArea.GALATEA) || event.phase != ClientTickEvent.Phase.START) return
        val screen = mc.currentScreen
        if (screen is GenericContainerScreen) {
            val inventory = screen.screenHandler.inventory

            // Slots 10 - 16 contain target
            run getTarget@{
                val targetPanes = (10..16).map { inventory.getStack(it) }
                var color: DyeColor? = null
                var index: Int? = null
                for (pane in targetPanes.withIndex()) {
                    color = ((pane.value.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color ?: continue
                    if (color != DyeColor.GRAY && color != DyeColor.LIGHT_GRAY) {
                        index = pane.index
                        break
                    }
                }
                if (color == null || index == null) return@getTarget
                targettColor = color
                if (index != positions.lastOrNull()?.first) {
                    val delay = Mithras.totalTicks - (positions.lastOrNull()?.second ?: Mithras.totalTicks)
                    positions.add(Pair(index, Mithras.totalTicks))
                    when (delay) {
                        in 10..19 -> { targetSpeed = BeaconSpeed.FIVE}
                        in 20..29 -> { targetSpeed = BeaconSpeed.FOUR}
                        in 30..39 -> { targetSpeed = BeaconSpeed.THREE}
                        in 40..49 -> { targetSpeed = BeaconSpeed.TWO}
                        in 40..59 -> { targetSpeed = BeaconSpeed.ONE}
                    }
                }
            }

            run getSelectedSpeed@ {
                val speedIndicator = inventory.getStack(50)
                val pitchString = speedIndicator.lore.find { it.startsWith("Current pitch: ") }?.substringAfter("Current pitch: ")
                when (pitchString) {
                    "Low" -> selectedPitch = BeaconPitch.LOW
                    "Normal" -> selectedPitch = BeaconPitch.NORMAL
                    "High" -> selectedPitch = BeaconPitch.HIGH
                }
            }


        }else{
            reset()
        }
    }

    private fun reset() {
        inBeacon = false
        targettColor = null
        positions.clear()
        targetPitch = null
        selectedPitch = null
        targetSpeed = null
    }

    /**
     * Determine wether in beacon gui.
     */
    @EventHandler
    private fun onGuiOpen(event: GuiOpenEvent) {
        if (event.newScreen !is GenericContainerScreen || inBeacon || !LocationManager.inArea(SkyblockArea.GALATEA)) return
        val chestName = event.newScreen.title.string
        if (chestName == "Tune Frequency")  {
            inBeacon = true
        }

    }

    /**
     * Detect pitch.
     */
    @EventHandler
    private fun onSound(event: PlaySoundEvent) {
        if(!inBeacon) return
        if (event.sound.category != SoundCategory.BLOCKS || event.sound.id.toString() != "minecraft:block.note_block.bass") return
        val pitch = event.sound.pitch
        val pitchType = when {
            (pitch < 0.5f) -> BeaconPitch.LOW
            (pitch < 1) -> BeaconPitch.NORMAL
            else -> BeaconPitch.HIGH
        }
        if (pitchType != selectedPitch) targetPitch = pitchType
    }

    @EventHandler
    private fun onSlotDraw(event: DrawSlotEvent<*>) {
        if (!inBeacon || event.handledScreen !is GenericContainerScreen ) return

        val clicks = when (event.slot.index) {
            46 -> getClosestColorClicks(event.slot) ?: return
            48 -> getClosestSpeedClicks(event.slot) ?: return
            50 -> getClosestPitchClicks() ?: return
            else -> return
        }
        if (clicks == 0) return
        drawClicks(clicks, event)
    }

    /**
     * Set cooldown.
     */
    @EventHandler
    private fun onChat(event: ChatReceivedEvent) {
        if (!LocationManager.inArea(SkyblockArea.GALATEA)) return
        if (event.text.string != "You adjusted the frequency of the Beacon!") return
        nextAvaliable = Mithras.totalTicks + 12_000
    }

    /**
     * Enable / disable the hud when (not) in Galetea.
     */
    @EventHandler
    private fun onAreaChange(event: AreaChangeEvent) {
        if (event.newArea != SkyblockArea.GALATEA) {
            Mithras.EVENT_BUS.unsubscribe(BeaconTimer)
            reset()
        }
        else if (showCooldownHUD) Mithras.EVENT_BUS.subscribe(BeaconTimer)
    }

    private fun drawClicks(clicks: Int, event: DrawSlotEvent<*>) {
        val textWidth = mc.textRenderer.getWidth("$clicks")
        val offs = (16 - textWidth) / 2
        event.context.matrices.pushMatrix()
        event.context.matrices.translate(0.0f, 0.0f)
        event.context.fill(event.slot.x+offs, event.slot.y+4, event.slot.x+offs+ textWidth, event.slot.y+4 + mc.textRenderer.fontHeight, Color(0,0,0,150).rgb)
        event.context.drawText(mc.textRenderer, "$clicks", event.slot.x + offs, event.slot.y + 4, -1, false)
        event.context.matrices.popMatrix()
    }

    private fun getClosestColorClicks(slot: Slot): Int? {
        val color = COLORS.find { it.second == slot.stack.item }?.first ?: return null
        val current = COLORS.indexOfFirst { it.first == color }
        val target = COLORS.indexOfFirst { it.first == targettColor }
        if (current == -1 || target == -1) return null

        val difference = target - current
        val total = COLORS.size
        return if (difference > total / 2) difference - total
        else if (difference < - total / 2) difference + total
        else difference
    }

    private fun getClosestSpeedClicks(slot: Slot): Int? {
        val target = targetSpeed ?: return null
        val speedString = slot.stack.lore.find { it.startsWith("Current speed: ") }?.substringAfter("Current speed: ") ?: return null
        val currentSpeed = when (speedString) {
            "1" -> BeaconSpeed.ONE
            "2" -> BeaconSpeed.TWO
            "3" -> BeaconSpeed.THREE
            "4" -> BeaconSpeed.FOUR
            "5" -> BeaconSpeed.FIVE
            else -> return null
        }
        val difference = target.ordinal - currentSpeed.ordinal
        val total = 5
        return if (difference > total / 2) difference - total
        else if (difference < - total / 2) difference + total
        else difference
    }

    private fun getClosestPitchClicks(): Int? {
        val target = targetPitch ?: return null
        val current = selectedPitch ?: return null


        val difference = target.ordinal - current.ordinal
        val total = 3
        return if (difference > total / 2) difference - total
        else if (difference < - total / 2) difference + total
        else difference
    }

    // Cyan, Purple, Blue(lapis), Brown(cocoa), Green, Red, White(bonemeal), oragne, magenta, light blue, yellow, lime, pink,
    private val COLORS = listOf(
        DyeColor.CYAN       to Items.CYAN_DYE,
        DyeColor.PURPLE     to Items.PURPLE_DYE,
        DyeColor.BLUE       to Items.LAPIS_LAZULI,
        DyeColor.BROWN      to Items.COCOA_BEANS,
        DyeColor.GREEN      to Items.GREEN_DYE,
        DyeColor.RED        to Items.RED_DYE,
        DyeColor.WHITE      to Items.BONE_MEAL,
        DyeColor.ORANGE     to Items.ORANGE_DYE,
        DyeColor.MAGENTA    to Items.MAGENTA_DYE,
        DyeColor.LIGHT_BLUE to Items.LIGHT_BLUE_DYE,
        DyeColor.YELLOW     to Items.YELLOW_DYE,
        DyeColor.LIME       to Items.LIME_DYE,
        DyeColor.PINK       to Items.PINK_DYE,
    )

    private enum class BeaconPitch {
        LOW, NORMAL, HIGH
    }

    private enum class BeaconSpeed {
        ONE, TWO, THREE, FOUR, FIVE
    }

    @RegisterHudElement
    object BeaconTimer: HudElement (
        BeaconSolver, 0f, 200f,
        DEFAULT_RENDERER.textWidth("Beacon Cooldown: 5:23"),
        DEFAULT_RENDERER.defaultFontHeight,
    ) {
        override fun renderHud(context: DrawContext) {
            val remaining = (nextAvaliable - Mithras.totalTicks).coerceAtLeast(0L)
            val minutes = remaining / 1200
            val seconds = (remaining % 1200) / 20
            val secondsString = if(seconds < 10L) "0$seconds" else "$seconds"
            val bounds = renderer.textBounds("Beacon Cooldown: $minutes:$secondsString")
            renderer.rect(-2f, -2f, bounds.width() + 4f, bounds.height() + 4f, Color(0,0,0,150).rgb)
            renderer.text("Beacon Cooldown: $minutes:$secondsString", 0f, 0f, -1)
        }

    }
}