package floppacoding.mithras.module.impl.dungeon

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.events.TeleportEvent
import floppacoding.mithras.events.WorldChangeEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.impl.dungeon.dungeonmap.dungeon.ConfigRoom
import floppacoding.mithras.module.impl.dungeon.dungeonmap.utils.RoomUtils
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.utils.GeometryHelper
import floppacoding.mithras.utils.LocationManager.inDungeons
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.BlockState
import net.minecraft.block.Blocks
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockStateRaycastContext
import net.minecraft.world.BlockView
import java.awt.Color
// TODO readd rendering
/**
 * A solver for the Dungeons Teleport Maze puzzle.
 *
 * This solver uses a detailed ray cast to accurately solve the puzzle within the least amount of steps possible.
 * It often can solve the puzzle on the first teleport.
 */
object TeleportSolver : Module (
    "Teleport Solver",
    Category.DUNGEON,
    "Assists you with solving the teleport maze puzzle."
){
    private val visitedColor by ColorSetting("Visited Color", Color(255,50,10,150), description = "Color with which the visited pads will be highlighted")
    private val solutionColor by ColorSetting("Solution Color", Color(20,255,40,150), description = "Color with which solution will be highlighted.")
    private val uncertainColor by ColorSetting("Uncertain Color", Color(255,255,10,150), description = "Color with which uncertain solutions will be highlighted before sufficient data is available.")
    private val phase by BooleanSetting("Phase Solutions", false, description = "Shows the solution highlight through walls.")

    private val visitedPads: MutableSet<BlockPos> = mutableSetOf()
    private var possibleSolutions: MutableSet<BlockPos> = mutableSetOf()
    private var inTpMaze = false

    private val OFFSETS = listOf(-1 to -1, 1 to -1, 1 to 1, -1 to 1)

    @EventHandler
    fun onTeleport(event: TeleportEvent) {
        if (!inDungeons || !inTpMaze) return
        val startPad = mc.player!!.blockPos
        if(mc.world!!.getBlockState(startPad).block !== Blocks.END_PORTAL_FRAME) return
        val target = Vec3d(event.packet.change.position.x, event.packet.change.position.y, event.packet.change.position.z)
        val targetPos = BlockPos.ofFloored(target)

        var endPad: BlockPos? = null
        for (offset in OFFSETS) {
            val possibleEndPad = targetPos.add(offset.first, 0, offset.second)
            if(mc.world!!.getBlockState(possibleEndPad).block === Blocks.END_PORTAL_FRAME) {
                endPad = possibleEndPad
                break
            }
        }
        if (endPad == null) return
        visitedPads.add(startPad)
        visitedPads.add(endPad)

        // If already certain about the solution don't search for it
        if (possibleSolutions.size == 1) return
        val foundTargets = raycastPads(target, event.packet.change.yaw)

        if (possibleSolutions.isEmpty()) {
            possibleSolutions.addAll(foundTargets)
        }else if (foundTargets.isNotEmpty()){
            possibleSolutions.removeIf { !foundTargets.contains(it) }
        }
    }

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (!inDungeons) return
        inTpMaze = RoomUtils.isInRoom(ConfigRoom.TELEPORT_MAZE)
    }

    /**
     * Render the highlights.
     */
    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!inTpMaze) return
        try {
            visitedPads.forEach {
//                Renderer3D.drawBlockBoundingBox(event.context, it, fillColor = visitedColor)
            }
            if (possibleSolutions.size > 1) {
                possibleSolutions.forEach {
//                    Renderer3D.drawBlockBoundingBox(event.context, it, fillColor = uncertainColor)
                }
            }
            else if (possibleSolutions.isNotEmpty()){
//                Renderer3D.drawBlockBoundingBox(event.context, possibleSolutions.first(), fillColor =  solutionColor, phase = phase)
            }
        }catch (_: ConcurrentModificationException) {}
    }

    /**
     * Searches all possible solution teleport pads along the horizontal line defined by [start] and [yaw].
     *
     * Searches smooth stone slabs and then check whether there is am end portal frame at one of the corners.
     *
     * Returns all end portals found this way.
     */
    private fun raycastPads(start: Vec3d, yaw: Float): Set<BlockPos> {
        val direction = GeometryHelper.getDirection(0f, yaw)
        val end = start.add(direction.multiply(30.0))
        val startPos = BlockPos.ofFloored(start)

        val foundPads = mutableSetOf<BlockPos>()

        // Raycast shape for the Slabs hitbox.
        val hitboxShape = VoxelShapes.cuboid(0.45, 0.0, 0.45, 0.55, 1.0, 0.55)

        // Raycast context defining start, end and block state predicate.
        val context = BlockStateRaycastContext(start, end) {
            it.block === Blocks.SMOOTH_STONE_SLAB
        }

        // Scan all blocks along the line from start to end and check whether they are smooth stone slab and the line
        // intersects the custom hitbox. If so look for end portals at the corner and add those to the list of possible
        // solutions.
        BlockView.raycast<BlockHitResult?, BlockStateRaycastContext>(context.start, context.end, context,
            hitFactory@{ innerContext: BlockStateRaycastContext, pos: BlockPos ->
                if (pos == startPos) return@hitFactory null
                val blockState: BlockState = mc.world!!.getBlockState(pos)
                // See context for the predicate.
                if (innerContext.statePredicate.test(blockState)) {
                    val hitResult = hitboxShape.raycast(innerContext.start, innerContext.end, pos)
                    if(hitResult != null) {
                        for (offset in OFFSETS) {
                            val possibleEndPad = pos.add(offset.first, 0, offset.second)
                            if(mc.world!!.getBlockState(possibleEndPad).block === Blocks.END_PORTAL_FRAME) {
                                foundPads.add(possibleEndPad)
                                break
                            }
                        }
                    }
                }
                // Retuning null will keep the raycast going. This is done since we do not want just one hit result,
                // but rather all possible targets on the line.
                return@hitFactory null
            }
        ) {
            null
        }
        return foundPads
    }

    /**
     * Reset.
     */
    @EventHandler
    fun onWorldCanage(event: WorldChangeEvent) {
        visitedPads.clear()
        possibleSolutions.clear()
        inTpMaze = false
    }
}