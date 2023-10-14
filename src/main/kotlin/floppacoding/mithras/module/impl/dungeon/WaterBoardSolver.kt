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
import floppacoding.mithras.utils.Extensions
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.Blocks
import net.minecraft.block.LeverBlock
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import java.awt.Color
import java.util.*

/**
 * A solver for the dungeons water board puzzle.
 *
 * Parts of this are based on the
 * [Skytils](https://github.com/Skytils/SkytilsMod) solver, which in turn is based on
 * [SBA](https://github.com/bowser0000/SkyblockMod).
 *
 * @author Aton
 */
object WaterBoardSolver : Module(
    "Water Board Solver",
    category = Category.DUNGEON,
    description = "Automatically solves the water puzzle. Start by flicking the water flow start lever."
){
    private val flowColor by ColorSetting("Flow Color", Color(255,118,10,60), description = "The predicted water flow will be shown with this color.")

    private var variant = -1
    private var inWater = false
    private var direction: Direction? = null
    private var flow: WaterFlow? = null


    /**
     * Used to detect the water layout.
     */
    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (!LocationManager.inDungeons || event.phase != ClientTickEvent.Phase.START) return
        inWater = RoomUtils.isInRoom(ConfigRoom.WATER_BOARD)
        if (!inWater) return
        val room = Dungeon.currentRoom ?: return

        // find the rotation
        if (direction == null) for (horizontal in Extensions.HORIZONTALS) {
            if (mc.world!!.getBlockState(BlockPos(room.x, 58, room.z).offset(horizontal, 10)).block === Blocks.BLUE_WOOL) {
                this.direction = horizontal
                Lever.entries.forEach { it.updatePosition(room.x, room.z, horizontal) }
                break
            }
        }
        if (direction == null) return

        // Create the flow and calculate
        if (flow == null) {
            flow = WaterFlow(BlockPos(room.x, 80, room.z).offset(direction!!, 11))
        }
        flow!!.calculateFlow()

        /** Update which gates are open once per tick */
        for (gate in Gates.entries){
            gate.setClosed(mc.world!!.getBlockState(
                BlockPos(room.x,55,room.z).offset(direction!!, gate.ordinal)
            ).block === Blocks.PISTON_HEAD)
        }

        /** If the variant has not been determined so far find it */
        if (variant == -1) {
            var foundGold = false
            var foundClay = false
            var foundEmerald = false
            var foundQuartz = false
            var foundDiamond = false

            val waterPos = BlockPos(room.x,78, room.z).offset(direction!!, 11)

            val x = waterPos.x
            val z = waterPos.z


            // Detect first blocks near water stream
            // This detection is based on Skytils and SBA
            for (puzzleBlockPos in BlockPos.iterate(
                BlockPos(x + 1, 78, z + 1),
                BlockPos(x - 1, 77, z - 1)
            )) {
                val block = mc.world!!.getBlockState(puzzleBlockPos).block
                when {
                    block === Blocks.GOLD_BLOCK -> {
                        foundGold = true
                    }
                    block === Blocks.TERRACOTTA -> {
                        foundClay = true
                    }
                    block === Blocks.EMERALD_BLOCK -> {
                        foundEmerald = true
                    }
                    block === Blocks.QUARTZ_BLOCK -> {
                        foundQuartz = true
                    }
                    block === Blocks.DIAMOND_BLOCK -> {
                        foundDiamond = true
                    }
                }
            }
            if (foundGold && foundClay) {
                variant = 0
            } else if (foundEmerald && foundQuartz) {
                variant = 1
            } else if (foundQuartz && foundDiamond) {
                variant = 2
            } else if (foundGold && foundQuartz) {
                variant = 3
            }
        }
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!inWater || flow == null || direction == null) return
        if (flowColor.alpha != 0) {
            flow!!.path.forEach {
                Renderer3D.drawBox(event.context, Box(it.offset(direction!!.opposite)), null, flowColor)
            }
        }
        if (variant != -1) {
            val leverStates = Lever.entries.associateWithTo(EnumMap(Lever::class.java)) {
                it.getLeverToggleState()
            }
            var offset = 0
            for (gate in Gates.entries) {
                if (!gate.getClosed()) continue

                val solution = gate.getLevers(variant) ?: continue
                for ((lever, switched) in leverStates) {
                    if (switched && !solution.contains(lever) || !switched && solution.contains(lever)) {
                        Renderer3D.drawBox(event.context, Box(lever.position.up(offset)), gate.color, lineWidth = 3f)
                    }
                }
                offset++
            }
        }
    }

    /**
     * Resets the variables.
     */
    @EventHandler
    fun onWarp(event: WorldChangeEvent) {
        variant = -1
        flow = null
        inWater = false
        direction = null
        Gates.reset()
    }

    class WaterFlow(private val starPos: BlockPos) {
        val path: MutableSet<BlockPos> = mutableSetOf(starPos)
        private val flowEnds: MutableSet<FlowEnd> = mutableSetOf(FlowEnd(starPos, starPos))

        fun calculateFlow() {
            path.clear()
            path.add(starPos)
            flowEnds.clear()
            flowEnds.add(FlowEnd(starPos, starPos))

            var ii = 0
            var going = true
            while (++ii < 100 && going){
                going = iterateFlow()
            }
        }

        /**
         * Returns true if any flow ends are still active.
         */
        private fun iterateFlow(): Boolean {
            val terminatedOrigins = mutableSetOf<BlockPos>()
            val newFlowEnds = mutableSetOf<FlowEnd>()
            val flowEndIterator = flowEnds.iterator()
            var flowEnd: FlowEnd
            var down: BlockPos
            while (flowEndIterator.hasNext()) {
                flowEnd = flowEndIterator.next()
                if (flowEnd.reachedEnd) continue
                down = flowEnd.position.down()

                if (!mc.world!!.getBlockState(down).isSolid) {// If it can flow down do so.
                    if (flowEnd.direction !== Direction.DOWN) {
                        // check all other flow ends with the same origin and remove if it has not found a way down.
                        terminatedOrigins.add(flowEnd.origin)
                        path.addAll(flowEnd.subPath)
                        flowEnd.direction = Direction.DOWN
                    }
                    path.add(down)
                    flowEnd.origin = down
                    flowEnd.position = down

                }else { // Check horizontal steps.
                    if (flowEnd.direction === Direction.DOWN) {
                        // Water that has been flowing own hits a block.
                        // Remove the down flow end and add new ones for all available directions.
                        flowEndIterator.remove()
                        Extensions.HORIZONTALS.map { it to flowEnd.position.offset(it) }
                            .filter {!mc.world!!.getBlockState(it.second).isSolid}.forEach {
                                newFlowEnds.add(FlowEnd(it.second, flowEnd.position, it.first))
                            }
                    }else {
                        // Water has flow direction. continue flow in that direction if possible.
                        val next = flowEnd.position.offset(flowEnd.direction)
                        if (!mc.world!!.getBlockState(next).isSolid) {
                            flowEnd.subPath.add(next)
                            flowEnd.position = next
                        }else {
                            // If not possible, mark it as an end, so it will not be checked again.
                            flowEnd.reachedEnd = true
                        }
                    }
                }
            }

            flowEnds.addAll(newFlowEnds)

            // The seconds check for >0 is probably not required since all flow ends with horizontal length 0 should have
            // their position as origin.
            flowEnds.removeIf { terminatedOrigins.contains(it.origin)}
            return flowEnds.any { !it.reachedEnd && it.position.y > END_Y }
        }

        private class FlowEnd(
            var position: BlockPos,
            var origin: BlockPos,
            var direction: Direction = Direction.DOWN,
        ) {
            var reachedEnd = false
            val subPath = mutableSetOf<BlockPos>(position)
        }

        companion object {
            private const val END_Y = 59
        }
    }

    /**
     * Provides all the lever related functionality.
     */
    enum class Lever(private val relativePosition: BlockPos) {
        COAL(   BlockPos( 5,61, -5)),
        GOLD(   BlockPos( 5,61,  0)),
        QUARTZ( BlockPos( 5,61,  5)),
        DIAMOND(BlockPos(-5,61,  5)),
        EMERALD(BlockPos(-5,61,  0)),
        CLAY(   BlockPos(-5,61, -5)),
        //WATER(  BlockPos( 0,60,-10))
        ;

        var position: BlockPos = relativePosition

        fun updatePosition(x: Int, z: Int, direction: Direction) {
            position = BlockPos(x, relativePosition.y, z).offset(direction, relativePosition.z).offset(direction.rotateYCounterclockwise(), relativePosition.x)
        }

        fun getLeverToggleState(): Boolean {
            val state = mc.world!!.getBlockState(position)
            return if (state.block !== Blocks.LEVER) false else state.get(LeverBlock.POWERED)
        }
    }

    /**
     * Contains and manages information about the gates.
     */
    enum class Gates(var color: Color) {
        RED(Color(255,0,0)),
        GREEN(Color(0,255,0)),
        BLUE(Color(0,0,255)),
        ORANGE(Color(255,100,0)),
        PURPLE(Color(140,0,255));

        private var closed = false

        /**
         * Sets the extended state for the gate to the given state.
         */
        fun setClosed(state: Boolean) {
            closed = state
        }

        /**
         * Gets the extended state for the specified color.
         */
        fun getClosed(): Boolean {
            return closed
        }

        fun getLevers(variant: Int): Set<Lever>? {
            return SOLUTIONS[variant]?.get(this)
        }

        companion object {

            /**
             * Restes all gates to open
             */
            fun reset() {
                entries.forEach { it.closed = false }
            }

            @JvmStatic
            val SOLUTIONS: Map<Int, Map<Gates, Set<Lever>>> = mapOf(
                0 to mapOf(
                    PURPLE  to setOf(Lever.QUARTZ, Lever.GOLD, Lever.DIAMOND, Lever.CLAY),
                    ORANGE  to setOf(Lever.GOLD, Lever.COAL, Lever.EMERALD),
                    BLUE    to setOf(Lever.QUARTZ, Lever.GOLD, Lever.EMERALD, Lever.CLAY),
                    GREEN   to setOf(Lever.EMERALD),
                    RED     to setOf(),
                ),
                1 to mapOf(
                    PURPLE to setOf(Lever.COAL),
                    ORANGE to setOf(Lever.QUARTZ, Lever.GOLD, Lever.EMERALD, Lever.CLAY),
                    BLUE   to setOf(Lever.QUARTZ, Lever.DIAMOND, Lever.EMERALD),
                    GREEN  to setOf(Lever.QUARTZ, Lever.EMERALD),
                    RED    to setOf(Lever.QUARTZ, Lever.COAL, Lever.EMERALD),
                ),
                2 to mapOf(
                    PURPLE to setOf(Lever.QUARTZ, Lever.GOLD, Lever.DIAMOND),
                    ORANGE to setOf(Lever.EMERALD),
                    BLUE   to setOf(Lever.QUARTZ, Lever.DIAMOND),
                    GREEN  to setOf(),
                    RED    to setOf(Lever.GOLD, Lever.EMERALD),
                ),
                3 to mapOf(
                    PURPLE to setOf(Lever.QUARTZ, Lever.GOLD, Lever.EMERALD, Lever.CLAY),
                    ORANGE to setOf(Lever.GOLD, Lever.COAL),
                    BLUE   to setOf(Lever.QUARTZ, Lever.GOLD, Lever.COAL, Lever.EMERALD, Lever.CLAY),
                    GREEN  to setOf(Lever.GOLD, Lever.EMERALD),
                    RED    to setOf(Lever.GOLD, Lever.DIAMOND, Lever.EMERALD, Lever.CLAY),
                )
            )
        }
    }
    /*
    * WATER BAORD INFO:
    * THe floor in the water board room is at y = 59 (player y pos when on floor).
    * The board is offset by 11 blocks form the room center.
    * The board is 19 blocks wide and 18 blocks high. It is 2 bocks over the floor. (so the lowerst block in the board is at y= 61)
    * water flow would have to be calculated from y = 78 on.
    *
    * */
}
