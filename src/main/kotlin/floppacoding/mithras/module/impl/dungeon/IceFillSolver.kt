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
    private val lineColor by ColorSetting("Line Color", Color(255,200,0), false, description = "The color with which the solution will be drawn.")
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

    private val renderPathBuffer = Array<BlockPos>(3*3 + 5*5 +7*7 + 5) {BlockPos(0,0,0)}

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
        var length = 0
        var increment = threeLayer!!.getPath(renderPathBuffer, length)
        if (increment > 0){
            length += increment
            renderPathBuffer[length] = threeLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign)
            renderPathBuffer[length + 1] = threeLayer!!.endPos.add(-direction!!.first.sign*2,1,-direction!!.second.sign*2)
            length += 2
        }
        increment = fiveLayer!!.getPath(renderPathBuffer, length)
        if (increment > 0){
            length += increment
            renderPathBuffer[length] = fiveLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign)
            renderPathBuffer[length + 1] = fiveLayer!!.endPos.add(-direction!!.first.sign*2,1,-direction!!.second.sign*2)
            length += 2
        }
        increment = sevenLayer!!.getPath(renderPathBuffer, length)
        if (increment > 0){
            length += increment
            renderPathBuffer[length] = sevenLayer!!.endPos.add(-direction!!.first.sign,0,-direction!!.second.sign)
            length += 1
        }
        var last = Vec3d.of(renderPathBuffer[0]).add(0.5, 0.0, 0.5)
        var next: Vec3d
        for (ii in 1..<length) {
            next = Vec3d.of(renderPathBuffer[ii]).add(0.5, 0.0, 0.5)
//            Renderer3D.drawLine(event.context, last, next, lineColor, 4f, phase)
            // TODO readd rendering
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
        private val center: BlockPos,
        /**
         * Offset of the 3x3 layer from the rooms center.
         * This is used to determine  the direction of the stand and end point relative to the layer center.
         */
        val direction: Pair<Int,Int>
    ){
        private val halfSize = size / 2
        private val posMap: MutableMap<Int, BlockPos> = mutableMapOf()
        private val adjacencyList: MutableMap<Int,Array<Int>> = mutableMapOf()
        private val startPos: BlockPos
        val endPos: BlockPos

        private var pathFound = false
        private val path: Array<Int>
        private val blockPath: Array<BlockPos>
        private val stepsBuffer : Array<Array<Int?>>

        init {
            var row : Int
            var column: Int
            var pos : BlockPos
                for (ii in 0..<size * size) {
                row = ii / size
                column = ii % size
                pos = center.add(row - halfSize, 1, column - halfSize)
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
                adjacencyList[key] = adjacent.toTypedArray()
            }

            path = Array<Int>(adjacencyList.size) {-1}
            val default = BlockPos(0,0,0)
            blockPath = Array<BlockPos>(adjacencyList.size) {default}
            stepsBuffer = Array(adjacencyList.size){Array(4){null} }

            startPos = center.add(halfSize * direction.first.sign, 1 ,halfSize * direction.second.sign)
            val start = posMap.firstNotNullOfOrNull{ if(it.value == startPos) it.key else null }
            endPos = center.add(-halfSize * direction.first.sign, 1 ,-halfSize * direction.second.sign)
            val end = posMap.firstNotNullOfOrNull{ if(it.value == endPos) it.key else null }

            if (start != null && end != null) {
                Mithras.scope.launch {
                    val startTime = System.currentTimeMillis()
                    findPath(start, end)
                    val endTime = System.currentTimeMillis()
                    if (message) mc.send {
                        ChatUtils.modMessage("Ice Fill: ${size}x$size-layer took ${(endTime-startTime)/1000.0}s to solve, success: $pathFound.")
                    }
                }
            }else {
                if (message) ChatUtils.modMessage("Ice Fill: Error! start: $start, end: $end.")
            }
        }

        /**
         * Copies the found path into [destination] starting at the index [offset].
         *
         * @return The number of elements copied
         */
        fun getPath(destination: Array<BlockPos>, offset: Int): Int{
            if (pathFound) {
                blockPath.copyInto(destination, offset)
                return blockPath.size
            }
            return 0
        }

        private fun findPath(start: Int, end: Int) {
            // The index of the block in the center.
            val centerIndex = (size * size) / 2
            // This is a hypothetical index which the starting stair outside the grid would have.
            // It is just used to te determine the directions of the first move.
            val stair = start + (start - centerIndex) / (size/2)
            path[0] = start
            pathFound = iteratePath(stair,0, end)
            if (pathFound) {
                for (ii in path.indices) {
                    blockPath[ii] = posMap[path[ii]]!!
                }
            }
        }


        private fun iteratePath(parent: Int, depth: Int, target: Int): Boolean {
            val key = path[depth]
            if (depth == adjacencyList.size-2) {
                // last step
                if (adjacencyList[key]!!.contains(target)) {
                    path[depth+1] = target
                    return true
                }
                return false
            }
            adjacencyList[key]!!.copyInto(stepsBuffer[depth])
            var step: Int
            for (ii in 0..< 4) {
                 step = stepsBuffer[depth][ii] ?: continue
                if (path.contains(step) || step == target) stepsBuffer[depth][ii] = null
            }
            val straight = key + key - parent
            // First try going straight
            if (stepsBuffer[depth].contains(straight)) {
                path[depth + 1] = straight
                if (iteratePath(key, depth+1, target)) {
                    return true
                }
                path[depth + 1] = -1
            }

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
            // next try going left
            if (stepsBuffer[depth].contains(leftKey)) {
                path[depth + 1] = leftKey
                if (iteratePath(key, depth+1, target)) {
                    return true
                }
                path[depth + 1] = -1
            }
            // last try going right
            if (stepsBuffer[depth].contains(rightKey)) {
                path[depth + 1] = rightKey
                if (iteratePath(key, depth+1, target)) {
                    return true
                }
                path[depth + 1] = -1
            }
            return false
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