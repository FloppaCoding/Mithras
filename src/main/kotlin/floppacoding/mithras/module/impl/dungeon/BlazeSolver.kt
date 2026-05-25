package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
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
import floppacoding.mithras.utils.ChatUtils.stripControlCodes
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.render.Renderer3D
import kotlinx.coroutines.runBlocking
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.entity.decoration.ArmorStandEntity
import net.minecraft.entity.mob.BlazeEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import java.awt.Color

/**
 * This module is made to automatically complete the Blaze puzzle in dungeons.
 *
 * @author Aton
 */
object BlazeSolver : Module(
    "Blaze Solver",
    category = Category.DUNGEON,
    description = "Highlights which blaze to shoot."
){
    private val targetColor by ColorSetting("Target Color", Color(20,255,70), description = "Color for the next blaze to shoot.")
    private val lineColor by ColorSetting("Line Color", Color(20,120,255), description = "Color for the line to the next blaze to shoot.")
    private val secondLineColor by ColorSetting("Next Line Color", Color(180,10,20), description = "Color for line to the blaze after the next blaze to shoot.")

    /**
     * Determines the order in which the blazes have to be sorted.
     */
    var topDown: Boolean? = null
        private set


    private val orderedBlazes = ArrayList<ShootableBlaze>()
    private var impossible = false

    private var inBlazeRoom = false

    fun getOrderedBlazes() = orderedBlazes.toList()

    /**
     * Finds and sorts the blazes to solve the puzzle.
     * Based on Skytils Blaze solver
     */
    @EventHandler
    fun onTick(event: ClientTickEvent) = runBlocking {
        if (event.phase != ClientTickEvent.Phase.START || !inDungeons) return@runBlocking
        val room = Dungeon.currentRoom ?: return@runBlocking
        inBlazeRoom = RoomUtils.isInRoom(ConfigRoom.BLAZE)
        if (!inBlazeRoom) return@runBlocking

        // detect the order if not already done
        if (topDown == null) {
            topDown = when (mc.world!!.getBlockState(BlockPos( room.x, 68, room.z)).block) {
                Blocks.AIR, Blocks.IRON_BARS -> true
                else -> false
            }
        }

        // get sorted blaze list
        orderedBlazes.clear()
        val blazeSerachBox = Box(room.x-8.0,  10.0, room.z -8.0, room.x + 8.0, 150.0, room.z + 8.0)
        mc.world!!.getEntitiesByClass(ArmorStandEntity::class.java, blazeSerachBox) { entity ->
            entity.name.string.contains("Blaze") && entity.name.string.contains("/")
        }.forEach { entity ->
            val blazeName = entity.name.string.stripControlCodes()
            try {
                val health =
                    blazeName.substringAfter("/").filter { it.isDigit() }.toInt()
                val blazeBox = Box(
                    entity.x - 0.5,
                    entity.y - 2,
                    entity.z - 0.5,
                    entity.x + 0.5,
                    entity.y,
                    entity.z + 0.5
                )
                val blazes = mc.world!!.getEntitiesByClass(
                    BlazeEntity::class.java, blazeBox
                ) {true}
                if (blazes.isEmpty()) return@forEach
                orderedBlazes.add(ShootableBlaze(blazes[0], health))
            } catch (ex: NumberFormatException) {
                ex.printStackTrace()
            }
        }
        orderedBlazes.sortWith { blaze1, blaze2 ->
            val compare = blaze1.health.compareTo(blaze2.health)
            if (compare == 0 && !impossible) {
                impossible = true
                modMessage("§cDetected two blazes with the exact same amount of health!")
                val first = blaze1.blaze.health
                val second = blaze2.blaze.health
                if (first.toInt() == second.toInt()) return@sortWith first.compareTo(second)
            }
            return@sortWith compare
        }
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!inBlazeRoom || orderedBlazes.isEmpty()) return
        val highestFirst = topDown ?: return
        val target: ShootableBlaze
        val next: ShootableBlaze?
        val secondNext: ShootableBlaze?
        when(highestFirst){
            true -> {
                target = orderedBlazes.last()
                next = orderedBlazes.getOrNull(orderedBlazes.lastIndex - 1)
                secondNext = orderedBlazes.getOrNull(orderedBlazes.lastIndex - 2)
            }
            false -> {
                target = orderedBlazes.first()
                next = orderedBlazes.getOrNull(1)
                secondNext = orderedBlazes.getOrNull(2)
            }
        }

        Renderer3D.drawEntityBoundingBox(event.context, target.blaze, outlineColor = targetColor, lineWidth = 4f)
        if (next != null && lineColor.alpha != 0) {
            Renderer3D.drawLine(event.context, target.blaze.entityPos.add(0.0,1.0,0.0), next.blaze.entityPos.add(0.0,1.0,0.0), lineColor, 4f)
            if (secondNext != null && secondLineColor.alpha != 0) {
                Renderer3D.drawLine(event.context, next.blaze.entityPos.add(0.0,1.0,0.0), secondNext.blaze.entityPos.add(0.0,1.0,0.0), secondLineColor, 4f)
            }
        }

    }

    data class ShootableBlaze(@JvmField var blaze: BlazeEntity, var health: Int)

    /**
     *  reset on warp
     */
    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        topDown = null
        orderedBlazes.clear()
        impossible = false
        inBlazeRoom = false
    }
}