package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ChatReceivedEvent
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.ConfigRoom
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.Dungeon
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.utils.ChatUtils.modMessage
import floppacoding.mithras.utils.Extensions
import floppacoding.mithras.utils.Extensions.containsOneOf
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.render.Renderer3D
import kotlinx.coroutines.runBlocking
import meteordevelopment.orbit.EventHandler
import meteordevelopment.orbit.EventPriority
import net.minecraft.block.Blocks
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color

/**
 * Module to solve the three weirdos puzzle.
 * @author Aton
 */
object ThreeWeirdosSolver : Module(
    "Three Weirdos Solver",
    category = Category.DUNGEON,
    description = "Solves the three weirdos puzzle and highlights the correct solution."
){
    private val solutionColor by ColorSetting("Solution Color", Color(20,180,70,150), description = "Color with which the correct chest will be highlighted.")

    private val solutions = listOf(
        "The reward isn't in any of our chests.",
        "The reward is not in my chest!",
        "My chest doesn't have the reward. We are all telling the truth.",
        "My chest has the reward and I'm telling the truth!",
        "Both of them are telling the truth.",
        "At least one of them is lying, and the reward is not in"
    )
    private var correctBozo: String? = null
    private var correctChest: BlockPos? = null

    /**
     * Used to check incoming chat messages for solutions to the three weirdos puzzle.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    fun onChat(event: ChatReceivedEvent) {
        if (!inDungeons || event.type != ChatReceivedEvent.Type.GAME_MESSAGE) return
        if (!RoomUtils.isInRoom(ConfigRoom.THREE_WEIRDOS)) return
        val message = Formatting.strip(event.text.string) ?: return
        if (message.startsWith("[NPC]")) {
            val npcName = message.substring(message.indexOf("]") + 2, message.indexOf(":"))
            if (message.containsOneOf(solutions)) {
                modMessage("§c§l$npcName §2has the blessing.")
                correctBozo = npcName
            }
        }
    }

    /**
     * Finds chest
     */
    @EventHandler
    fun onTick(event: ClientTickEvent) = runBlocking {
        if (event.phase != ClientTickEvent.Phase.START || correctChest != null || correctBozo == null) return@runBlocking
        val room = Dungeon.currentRoom ?: return@runBlocking
        if (!RoomUtils.isInRoom(ConfigRoom.THREE_WEIRDOS)) return@runBlocking

        val bozoSearchBox = Box(room.x-11.0,  68.0, room.z -11.0, room.x + 11.0, 75.0, room.z + 11.0)
        val bozo = mc.world?.getEntitiesByClass(ArmorStandEntity::class.java, bozoSearchBox) { entity ->
            Formatting.strip(entity.customName?.string)?.contains(correctBozo!!) == true
        }?.firstOrNull()?: return@runBlocking


        // Find chest
        for (direction in Extensions.HORIZONTALS) {
            val potentialPos = bozo.blockPos.offset(direction)
            if (mc.world?.getBlockState(potentialPos)?.block === Blocks.CHEST) {
                correctChest = potentialPos
                break
            }
        }
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (correctChest == null) return
        Renderer3D.drawBlockBoundingBox(event.context, correctChest!!, fillColor = solutionColor)
    }

    /**
     * Resets the values when changing world.
     */
    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        correctBozo = null
        correctChest = null
    }
}