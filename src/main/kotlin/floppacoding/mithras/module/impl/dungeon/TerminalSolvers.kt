package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.*
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.RunInformation
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.LocationManager.inDungeons
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.StainedGlassPaneBlock
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen
import net.minecraft.item.BlockItem
import net.minecraft.item.Items
import net.minecraft.screen.slot.Slot
import net.minecraft.util.DyeColor
import java.awt.Color
import kotlin.math.abs

/**
 * Module for Terminal features such as automatically completing the terminals.
 *
 *
 * @author Aton
 */
object TerminalSolvers : Module(
    "Terminal Solvers",
    category = Category.DUNGEON,
    description = "Shows the solution for terminals."
){
    private val highlightColor by ColorSetting("Highlight Color", Color(0,255,255), description = "Color with which all valid next clicks will be highlighted.")
    private val nextColor by ColorSetting("Next Color", Color(255,255,0), description = "Color with which the next click in the numbers terminal will be highlighted.")
    private val hideOthers by BooleanSetting("Hide Wrong Slots", false, description = "Prevents slots which do not have to be clicked from being rendered.")

    private var currentTerminal = TerminalType.NONE

    private var solutionOrder = arrayListOf<Slot>()

    private var closestColorIndex = -1
    private val colorOrder = listOf(14, 1, 4, 13, 11)
    private var lastRowClicked = 0

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (!inDungeons || event.phase != ClientTickEvent.Phase.START || mc.currentScreen is GenericContainerScreen) return
        if (currentTerminal != TerminalType.NONE) {
            reset()
        }
    }

    private fun reset() {
        currentTerminal = TerminalType.NONE
        closestColorIndex = -1
        lastRowClicked = 0
    }

    @EventHandler
    fun onGuiOpen(event: GuiOpenEvent) {
        if (event.screen !is GenericContainerScreen || currentTerminal != TerminalType.NONE || !RunInformation.inF7Boss()) return
        val chestName = event.screen.title.string
        currentTerminal = when {
            chestName == "Click in order!" -> TerminalType.NUMBERS
            chestName == "Correct all the panes!" -> TerminalType.CORRECT_ALL
            chestName.startsWith("What starts with: '") -> TerminalType.LETTER
            chestName.startsWith("Select all the") -> TerminalType.COLOR
            chestName == "Click the button on time!" -> TerminalType.TIMING
            chestName == "Change all to same color!" -> TerminalType.SAME_COLOR
            else -> TerminalType.NONE
        }
    }

    @EventHandler
    fun onGuiDraw(event: GuiBackgroundDrawnEvent) {
        if (currentTerminal == TerminalType.NONE || event.screen !is GenericContainerScreen) return
        val updateClicks = if (currentTerminal == TerminalType.NUMBERS) {
            event.screen.screenHandler.cursorStack.isEmpty
        }else true
        if (updateClicks) {
            solutionOrder.clear()
            solutionOrder.addAll(getClicks(event.screen))
        }
    }

    @EventHandler
    fun onSlotDraw(event: DrawSlotEvent<*>) {
        if (event.handledScreen !is GenericContainerScreen || currentTerminal == TerminalType.NONE || !RunInformation.inF7Boss()) return
        when(currentTerminal) {
            TerminalType.NUMBERS -> {
                if (solutionOrder.contains(event.slot)) {
                    val x: Int = event.slot.x
                    val y: Int = event.slot.y
                    val color = if (solutionOrder.indexOf(event.slot) == 0)
                        highlightColor
                    else if (solutionOrder.indexOf(event.slot) == 1)
                        nextColor
                    else return
                    event.context.fill(x, y, x + 16, y + 16, color.rgb)
                }
            }
            TerminalType.LETTER, TerminalType.COLOR -> {
                if (solutionOrder.contains(event.slot)) {
                    val x: Int = event.slot.x
                    val y: Int = event.slot.y
                    event.context.fill(x, y, x + 16, y + 16, highlightColor.rgb)
                }else if(hideOthers) event.cancel()
            }
            TerminalType.SAME_COLOR -> {
                val colorId = getColorId(event.slot)
                if (!colorOrder.contains(colorId)) {
                    if (hideOthers) event.cancel()
                    return
                }
                val clicks = mapColorClicks( closestColorIndex - colorOrder.indexOf(colorId))
                if (clicks == 0) return
                val offs = (16 - mc.textRenderer.getWidth("$clicks")) / 2
                event.context.matrices.push()
                event.context.matrices.translate(0.0f, 0.0f, 1000.0f)
                event.context.drawText(mc.textRenderer, "$clicks", event.slot.x + offs, event.slot.y + 4, -1, false)
                event.context.matrices.pop()
            }
            TerminalType.NONE, TerminalType.TIMING, TerminalType.CORRECT_ALL -> {}
        }
    }

    @EventHandler
    fun onTooltip(event: DrawItemTooltopEvent) {
        if (currentTerminal == TerminalType.NUMBERS) return
    }

    private fun getClicks(screen: GenericContainerScreen): MutableList<Slot> {
        val chestSlots = screen.screenHandler.slots.filterNot { it.inventory === mc.player?.inventory }
        val clicks = mutableListOf<Slot>()
        when (currentTerminal) {
            TerminalType.NUMBERS -> {
                val panes = chestSlots.filter { (it.stack?.item as? BlockItem)?.block is StainedGlassPaneBlock }
                val min = panes.filter { getColorId(it) == 5 }.maxOfOrNull { it.stack.count } ?: 0
                clicks.addAll(panes.filter { getColorId(it) == 14 && it.stack.count < 15 }
                    .sortedBy { it.stack.count })
                if (clicks.size + min != 14) clicks.clear()
            }
            TerminalType.CORRECT_ALL -> {
//                clicks.addAll(chestSlots.filter { (it.stack?.item as? BlockItem)?.block is StainedGlassPaneBlock && it.stack?.damage == 14 })
                clicks.addAll(chestSlots.filter { it.stack?.item === Items.RED_STAINED_GLASS_PANE })
            }
            TerminalType.LETTER -> {
                val chestName = screen.title.string
                if (chestName.length > chestName.indexOf("'") + 1) {
                    val letterNeeded = chestName[chestName.indexOf("'") + 1]
                    clicks.addAll(chestSlots.filter {
                        it.stack?.hasEnchantments() == false && (it.stack?.name?.string?.stripControlCodes()?.get(0)
                            ?: "") == letterNeeded && with(
                            it.id
                        ) {
                            this in 10..43 && this % 9 in 1..7
                        }
                    })
                }
            }
            TerminalType.COLOR -> {
                val colorNeeded = DyeColor.entries.find {
                    screen.title.string.contains(
                        it.id.replace("_", " ").uppercase()
                    )
                }?.name ?: return clicks
                clicks.addAll(chestSlots.filter {
                    val translationKey = it.stack.item.translationKey
                    it.stack?.hasEnchantments() == false &&
                            (translationKey.contains(colorNeeded) || colorNeeded == DyeColor.BLACK.name && translationKey == "item.minecraft.ink_sac" )
                            && with(it.id) {
                        this in 10..43 && this % 9 in 1..7
                    }
                })
            }
            TerminalType.SAME_COLOR -> {
                val panes = chestSlots.filter {
                    (it.stack?.item as? BlockItem)?.block is StainedGlassPaneBlock && colorOrder.contains(getColorId(it))
                }
                if (closestColorIndex == -1) {
                    closestColorIndex = colorOrder.indexOf(getClosestColor(panes))
                }
                if (closestColorIndex == -1) return clicks
                val extraClick = mutableListOf<Slot>()
                panes.forEach {
                    val colorIndex = colorOrder.indexOf(getColorId(it))
                    if (closestColorIndex != colorIndex) {
                        clicks.add(it)
                    }
                }
                clicks.addAll(extraClick)
            }
            else -> {}
        }
        return clicks
    }

    private fun getClosestColor(panes: List<Slot>): Int? {
        var minClicks = IndexedValue(-1, 9999)
        for (color in colorOrder.withIndex()) {
            var clicks = 0
            panes.forEach {
                clicks += abs(mapColorClicks( color.index - colorOrder.indexOf(getColorId(it))))
            }
            if (minClicks.value > clicks) minClicks = IndexedValue(color.index, clicks)
        }
        return if (minClicks.value == 0) null else colorOrder.getOrNull(minClicks.index)
    }

    private fun mapColorClicks(diff: Int): Int {
        return when(diff) {
            1,-1,2,-2 -> diff
            3 -> -2
            -3 -> 2
            4 -> -1
            -4 -> 1
            0 -> 0
            else -> {0}
        }
    }

    fun getColorId(slot: Slot): Int? = ((slot.stack?.item as? BlockItem)?.block as? StainedGlassPaneBlock)?.color?.index

    enum class TerminalType {
        NUMBERS, CORRECT_ALL, LETTER, COLOR, TIMING, SAME_COLOR, NONE
    }
}