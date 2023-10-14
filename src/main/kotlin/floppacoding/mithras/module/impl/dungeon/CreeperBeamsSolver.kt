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
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.utils.Extensions.withAlpha
import floppacoding.mithras.utils.GeometryHelper
import floppacoding.mithras.utils.LocationManager.inDungeons
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.client.world.ClientWorld
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * Creeper beams solver.
 *
 * @author Aton
 */
object CreeperBeamsSolver : Module(
    "Creeper Beams Solver",
    category = Category.DUNGEON,
    description = "Renders the solution for the creeper beams puzzle."
){
    private val showLines by BooleanSetting("Show lines", false, description = "Renders lines connecting solution pairs.")
    private val opacity by NumberSetting("Opacity", 150, 0, 255, 1, description = "Opacity of the block highlight.")

    private val SOLUTION_COLOURS = listOf(Color(0x50EF39), Color(0xC51111), Color(0x132ED1), Color(0x117F2D), Color(0xED54BA), Color(0xEF7D0D), Color(0xF5F557), Color(0xD6E0F0), Color(0x6B2FBB), Color(0x39FEDC))

    private var solutionPairs: MutableList<Pair<BlockPos, BlockPos>> = ArrayList()
    private var inCreeperRoom = false

    private val BOUNDING_BOX = Box(-0.0, 74.0, 0.0, 1.0, 78.0, 1.0)


    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (event.phase != ClientTickEvent.Phase.START || !inDungeons) return
        inCreeperRoom = RoomUtils.isInRoom(ConfigRoom.CREEPER_BEAMS)
        if (!inCreeperRoom) return
        val room = Dungeon.currentRoom ?: return
        val roomCenter = BlockPos(room.x, 0, room.z)
        val world: ClientWorld = mc.world ?: return
        if (solutionPairs.isNotEmpty()) return


        // Search for nearby sea lanterns
        val point1 = BlockPos(room.x - 14, 69,  room.z - 14)
        val point2 = BlockPos(room.x + 14, 85, room.z + 14)
        // DO NOT USE THE FOLLOWING!!! IT DOES NOT WORK, because the iterator passes the same object every time and only mutates its value.
        // BlockPos.iterate(point1, point2).filter { world.getBlockState(it).block === Blocks.SEA_LANTERN }
        val targets = mutableListOf<BlockPos>()
        BlockPos.iterate(point1, point2).forEach {
            if (world.getBlockState(it).block === Blocks.SEA_LANTERN)
                // A new instance has to be created here, because the BlockPos object is the same in every iteration
                // only its coordinates get mutated.
                targets.add(BlockPos(it))
        }
        targets.sortBy { -it.y } // This puts the block at the top which connects to the block below the creeper at the front of the list.

        // Build all possible pairs from the found blocks and check whether the line connecting the blocks intersects
        // the puzzles hitbox.
        val possibleSolutions: MutableList<Pair<BlockPos, BlockPos>> = ArrayList()
        for (ii in 0 until targets.size - 1 ) {
            val start = targets[ii]
            for (jj in ii + 1 until targets.size) {
                val end = targets[jj]
                val hitPoint = BOUNDING_BOX.offset(roomCenter).raycast(start.toCenterPos(), end.toCenterPos())
                if (hitPoint.isPresent) {
                    possibleSolutions.add(start to end)
                }
            }
        }

        // Sort the solutions by distance to the center-line of the room.
        possibleSolutions.sortBy {
            GeometryHelper.distanceBetweenLines(Vec3d.ofCenter(it.first), Vec3d.ofCenter(it.second),
                roomCenter.toCenterPos(), roomCenter.toCenterPos().add(0.0, 1.0, 0.0)
            )
        }

        // Remove duplicates
        val usedBlocks = mutableSetOf<BlockPos>()
        val iterator = possibleSolutions.iterator()
        while (iterator.hasNext()) {
            val solution = iterator.next()
            if (usedBlocks.contains(solution.first) || usedBlocks.contains(solution.second)) {
                iterator.remove()
            }else {
                usedBlocks.add(solution.first)
                usedBlocks.add(solution.second)
            }
        }
        possibleSolutions.mapTo(solutionPairs) {it.first to  it.second}
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!inCreeperRoom || solutionPairs.isEmpty()) return
        solutionPairs.withIndex().forEach {
            val color = SOLUTION_COLOURS.getOrNull(it.index) ?: return@forEach
            Renderer3D.drawBlockBoundingBox(event.context,it.value.first, fillColor = color.withAlpha(opacity))
            Renderer3D.drawBlockBoundingBox(event.context,it.value.second, fillColor = color.withAlpha(opacity))
            if (showLines) {
                Renderer3D.drawLine(event.context, it.value.first.toCenterPos(), it.value.second.toCenterPos(), color, lineWidth = 2f, phase = true)
            }
        }
    }



    /**
     * reset on warp
     */
    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        solutionPairs.clear()
    }

}