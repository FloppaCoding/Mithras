package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras
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
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.utils.ChatUtils
import floppacoding.mithras.utils.Extensions.HORIZONTALS
import floppacoding.mithras.utils.Extensions.identicalToOneOf
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.render.Renderer3D
import kotlinx.coroutines.launch
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.abs
import kotlin.math.sign

/**
 * A solver for the Ice Fill Puzzle.
 *
 * The puzzle is a Hamiltonian Path problem.
 *
 */
object IceFillSolver : Module(
    "Ice Fill Solver",
    Category.DUNGEON,
    "Shows you the a solution for the ice fill puzzle."
) {
    private val lineColor by ColorSetting("Line Color", Color(69,13,152), false, description = "The color with which the solution will be drawn.")
    private val phase by BooleanSetting("Phase", false, description = "Show the solution through blocks.")
    private val message by BooleanSetting("Message", false, description = "Shows in chat how long the layers took to solve.")


    private var inIceFill = false

    private var threeLayer : Layer? = null
    private var fiveLayer  : Layer? = null
    private var sevenLayer : Layer? = null
    private val OFFSETS = listOf(
        7 to 0,
        0 to 7,
        -7 to 0,
        0 to -7
    )
    private var direction: Pair<Int,Int>? = null

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (!LocationManager.inDungeons) return
        inIceFill = RoomUtils.isInRoom(ConfigRoom.ICE_FILL)
        if (!inIceFill) return
        if (threeLayer != null) return
        val room = Dungeon.currentRoom ?: return
        val center = BlockPos(room.x, 69, room.z)

        // find rotation
        for(offset in OFFSETS) {
            val block = mc.world!!.getBlockState(BlockPos(room.x+ offset.first, 69, room.z + offset.second)).block
            if (block.identicalToOneOf(Blocks.ICE, Blocks.PACKED_ICE)) {
                direction = offset
                break
            }
        }
        if (direction == null) return

        threeLayer = Layer(3, center.add(direction!!.first, 0, direction!!.second), direction!!)
        fiveLayer  = Layer(5, center.add(direction!!.first.sign, 1, direction!!.second.sign), direction!!)
        sevenLayer = Layer(7, center.add(-direction!!.first, 2, -direction!!.second), direction!!)
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!inIceFill || threeLayer == null) return
        val path = mutableListOf<BlockPos>()
        if (threeLayer!!.getPath(path)){
            path.add(threeLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign))
            path.add(threeLayer!!.endPos.add(-direction!!.first.sign*2,1,-direction!!.second.sign*2))
        }
        if (fiveLayer!!.getPath(path)){
            path.add(fiveLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign))
            path.add(fiveLayer!!.endPos.add(-direction!!.first.sign*2,1,-direction!!.second.sign*2))
        }
        if (sevenLayer!!.getPath(path)){
            path.add(sevenLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign))
        }
        var last = Vec3d.of(path[0]).add(0.5, 0.0, 0.5)
        var next: Vec3d
        for (ii in 1..<path.size) {
            next = Vec3d.of(path[ii]).add(0.5, 0.0, 0.5)
            Renderer3D.drawLine(event.context, last, next, lineColor, 4f, phase)
            last = next
        }
    }

    @EventHandler
    fun onWorldChange(event: WorldChangeEvent) {
        inIceFill = false
        threeLayer = null
        fiveLayer  = null
        sevenLayer = null
        direction = null
    }

    /**
     * A Layer in the ice fill puzzle.
     * The layer has the area [size]x[size] and is centered around the ice block at [center].
     */
    class Layer(
        /**
         * Side length of the layer.
         */
        val size: Int,
        /**
         * Center ice block of the layer.
         */
        val center: BlockPos,
        /**
         * Offset of the 3x3 layer from the rooms center.
         * This is used to determine  the direction of the stand and end point relative to the layer center.
         */
        val direction: Pair<Int,Int>
    ){
        private val halfSize = size / 2
        private val posMap: MutableMap<Int, BlockPos> = mutableMapOf()
        private val adjacencyList: MutableMap<Int,Set<Int>> = mutableMapOf()
        val startPos: BlockPos
        val endPos: BlockPos
        private val start: Int?
        private val end: Int?
        private var path: List<Int>? = null

        init {
            for (ii in 0..<size * size) {
                val row = ii / size
                val column = ii % size
                val pos = center.add(row - halfSize, 1, column - halfSize)
                if (mc.world!!.getBlockState(pos).isAir) {
                    posMap[ii] = pos
                }
            }
            posMap.forEach{ (key, pos) ->
                val adjacent = mutableSetOf<Int>()
                HORIZONTALS.forEach { direction ->
                    val neighbor = pos.offset(direction)
                    posMap.firstNotNullOfOrNull { if(it.value == neighbor) it.key else null }?.let { adjacent.add(it) }
                }
                adjacencyList[key] = adjacent
            }

            startPos = center.add(halfSize * direction.first.sign, 1 ,halfSize * direction.second.sign)
            start = posMap.firstNotNullOfOrNull{ if(it.value == startPos) it.key else null }
            endPos = center.add(-halfSize * direction.first.sign, 1 ,-halfSize * direction.second.sign)
            end = posMap.firstNotNullOfOrNull{ if(it.value == endPos) it.key else null }

            if (start != null && end != null) {
                Mithras.scope.launch {
                    val startTime = System.currentTimeMillis()
                    path = findPath(adjacencyList, start, end, size)
                    val endTime = System.currentTimeMillis()
                    if (message) mc.send {
                        ChatUtils.modMessage("Ice Fill: ${size}x$size-layer took ${(endTime-startTime)/1000.0}s to solve.")
                    }
                }
            }
        }

        /**
         * Appends a list of the blocks in the order they have to be stepped on the complete the layer to [description].
         *
         * @return true when a solution has been appended.
         */
        fun getPath(destination: MutableList<BlockPos>): Boolean{
            return path?.mapTo(destination){ posMap[it]!! } != null
        }

        private fun findPath(adjecencyList: Map<Int,Set<Int>>, start: Int, end: Int, size: Int): List<Int> {
            // The blocks in the layer are indexed from 0 to [size]x[size] - 1.
            val root = Node(start)
            // The index of the block in the center.
            val centerIndex = (size * size) / 2
            // This is a hypothetical index which the starting stair outside the grid would have.
            // It is just used to te determine the directions of the first move.
            val stair = start + (start - centerIndex) / (size/2)

            for (ii in 2 until adjecencyList.size) {
                root.nextLayer(stair, mutableSetOf(end), adjecencyList, size)
            }
            root.nextLayer(stair, mutableSetOf(), adjecencyList, size)

            val bestPath = mutableListOf<Int>()
            root.getPath(bestPath)

            return bestPath
        }

        class Node(
            var key: Int,
            var left: Node? = null,
            var middle: Node? = null,
            var right: Node? = null
        ) {
            fun getPath(out: MutableList<Int>) {
                out.add(key)
                middle?.getPath(out) ?: right?.getPath(out) ?: left?.getPath(out)
            }

            /**
             * Adds a layer to the tree of possible paths.
             *
             * @return whether any of the children are not null after the layer was added. If they are all null this node
             * can be deleted.
             */
            fun nextLayer(parent: Int, visited: MutableSet<Int>, adjacencyList: Map<Int,Set<Int>>, size: Int): Boolean {
                // If any of the children is not null, do not try to create them again, but instead go to the next layer.
                if (left != null || middle != null || right != null){
                    visited.add(key)
                    if (left?.  nextLayer(key, visited, adjacencyList, size) == false) left   = null
                    if (middle?.nextLayer(key, visited, adjacencyList, size) == false) middle = null
                    if (right?. nextLayer(key, visited, adjacencyList, size) == false) right  = null
                    visited.remove(key)
                    return left != null || middle != null || right != null
                }

                val steps = adjacencyList[key]!!.minus(visited)
                val straight = key + key - parent
                val diff = key - parent
                val leftKey: Int
                val rightKey: Int
                if (abs(diff) > 1) {
                    rightKey = key + diff.sign
                    leftKey = key - diff.sign
                }else {
                    rightKey = key - diff.sign * size
                    leftKey = key + diff.sign * size
                }

                left = if (steps.contains(leftKey)) Node(leftKey) else null
                middle = if (steps.contains(straight)) Node(straight) else null
                right = if (steps.contains(rightKey)) Node(rightKey) else null

                return left != null || middle != null || right != null
            }
        }
    }








    /*
    * PUZZLE INFO:
    * There are three layers:
    * 3x3 at with ice blocks at y = 69 (player on top at y =70), offset by 7 blocks from center
    * 5x5 at with ice blocks at y = 70 (player on top at y =71), offset by 1 block from center
    * 7x7 at with ice blocks at y = 71 (player on top at y =72), offset by -7 blocks from room center
    *
    * The center of the 5x5 layer is offset by 1 block towards the door from the center of the room.
    * There is one extra ice block additional to the grid at the end in direction of the next layer and then a star block
    * upwards. The ice is completely surrounded by polished andersite blocks, except for the stair block at the start
    * of teach layer.
    * The rotation can be determined by the lowest layer. its center is offset by 7 blocks from the room center.
    * The fields are by default ice, and if filled in packed ice. The obstruction on top of the ice are polished andersite.
    *
    * */
}