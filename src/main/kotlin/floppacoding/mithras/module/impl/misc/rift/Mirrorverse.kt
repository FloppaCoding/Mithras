package floppacoding.mithras.module.impl.misc.rift

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.utils.Extensions.Box
import floppacoding.mithras.utils.Extensions.withAlpha
import floppacoding.mithras.utils.LocationManager.currentRegionPair
import floppacoding.mithras.utils.ScoreboardUtils.cleanSB
import floppacoding.mithras.utils.ScoreboardUtils.sidebarLines
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext
import net.minecraft.block.Blocks
import net.minecraft.block.LeverBlock.POWERED
import net.minecraft.entity.mob.CaveSpiderEntity
import net.minecraft.entity.mob.SlimeEntity
import net.minecraft.entity.mob.ZombieEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color

object Mirrorverse : Module(
    "Mirrorverse",
    category = Category.MISC,
) {
    private val renderBoundingBox: Boolean by BooleanSetting("Debug", true)

    private var currentArea: RiftPuzzle? = null

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        currentArea = RiftPuzzle.entries.firstOrNull {
            it.check()
        }
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        currentArea?.let {
            if (renderBoundingBox) {
                Renderer3D.drawBox(event.context, it.area, outlineColor = Color.RED)
            }
            when (it) {
                RiftPuzzle.FOUR_LEVERS -> {
                    renderLever(event.context, BlockPos(-91, 65, -119), Vec3d(-80.5, 65.25, -118.75))
                    renderLever(event.context, BlockPos(-91, 67, -119), Vec3d(-80.5, 67.25, -118.75))
                    renderLever(event.context, BlockPos(-91, 67, -116), Vec3d(-80.5, 67.25, -115.75))
                    renderLever(event.context, BlockPos(-91, 65, -116), Vec3d(-80.5, 65.25, -115.75))
                }

                RiftPuzzle.LAVA_MAZE -> {
                    for (i in blocks) {
                        Renderer3D.drawBox(event.context, Box(i, i.add(1, 1, 1)), fillColor = Color.RED.withAlpha(100))
                    }
                    renderLever(event.context, BlockPos(-105, 52, -99), Vec3d(-104.75, 52.0, -112.75))
                }

                RiftPuzzle.CRAFTING_ROOM -> {
                    for (i in mc.world!!.entities) {
                        val mirror = Box(-108, 52, -127, -117, 58, -117)
                        if (!mirror.contains(i.pos) || !i.isAlive) continue

                        val color = when (i) {
                            is SlimeEntity -> Color.GREEN
                            is ZombieEntity -> Color(147, 117, 76)
                            is CaveSpiderEntity -> Color(200, 200, 200)
                            else -> return
                        }
                        val pos = i.boundingBox.offset(.0, .0, (i.pos.z + 117.0) * -2 + 1)
                        Renderer3D.drawBox(event.context, pos, color, color.withAlpha(50))
                    }
                }
            }
        }
    }

    private fun renderLever(ctx: WorldRenderContext, mirrorLever: BlockPos, realLever: Vec3d) {
        if (mc.world!!.getBlockState(mirrorLever).block != Blocks.LEVER) return
        val color = if (mc.world!!.getBlockState(mirrorLever).get(POWERED)) Color.GREEN else Color.RED
        Renderer3D.drawBox(ctx, Box(realLever, realLever.add(.5, .5, .5)), fillColor = color)
    }

    private fun inMirrorverse(): Boolean {
        return currentRegionPair?.first?.data?.name == "The Rift" && cleanSB(sidebarLines[4]) == "  Mirrorverse"
    }

    enum class RiftPuzzle(val area: Box) {
        FOUR_LEVERS(Box(-80, 64, -121, -85, 69, -113)),
        LAVA_MAZE(Box(-81, 52, -117, -105, 61, -106)),
        CRAFTING_ROOM(Box(-108, 52, -116, -117, 58, -106)),
        //LAVA_PARKOUR(Box()),
        //TURBULATOR(Box())
        ;

        fun check(): Boolean {
            if (mc.player == null) return false
            return area.contains(mc.player!!.pos)
        }
    }

    // make this a json
    // and get lava parkour coordinates and the turbulator ones
    private val blocks = arrayOf(
        BlockPos(-82, 51, -111), BlockPos(-82, 51, -112), BlockPos(-82, 51, -113), BlockPos(-83, 51, -111),
        BlockPos(-83, 51, -112), BlockPos(-83, 51, -113), BlockPos(-84, 51, -107), BlockPos(-84, 51, -108),
        BlockPos(-84, 51, -109), BlockPos(-84, 51, -111), BlockPos(-84, 51, -112), BlockPos(-84, 51, -113),
        BlockPos(-85, 51, -107), BlockPos(-85, 51, -109), BlockPos(-85, 51, -112), BlockPos(-86, 51, -107),
        BlockPos(-86, 51, -109), BlockPos(-86, 51, -110), BlockPos(-86, 51, -112), BlockPos(-86, 51, -113),
        BlockPos(-86, 51, -114), BlockPos(-86, 51, -115), BlockPos(-87, 51, -107), BlockPos(-87, 51, -110),
        BlockPos(-87, 51, -115), BlockPos(-88, 51, -107), BlockPos(-88, 51, -110), BlockPos(-88, 51, -111),
        BlockPos(-88, 51, -112), BlockPos(-88, 51, -113), BlockPos(-88, 51, -114), BlockPos(-88, 51, -115),
        BlockPos(-89, 51, -107), BlockPos(-90, 51, -108), BlockPos(-90, 51, -110), BlockPos(-90, 51, -111),
        BlockPos(-90, 51, -112), BlockPos(-90, 51, -114), BlockPos(-90, 51, -115), BlockPos(-90, 51, -116),
        BlockPos(-91, 51, -108), BlockPos(-91, 51, -110), BlockPos(-91, 51, -112), BlockPos(-91, 51, -114),
        BlockPos(-91, 51, -116), BlockPos(-92, 51, -108), BlockPos(-92, 51, -110), BlockPos(-92, 51, -112),
        BlockPos(-92, 51, -113), BlockPos(-92, 51, -114), BlockPos(-92, 51, -116), BlockPos(-93, 51, -108),
        BlockPos(-93, 51, -109), BlockPos(-93, 51, -110), BlockPos(-93, 51, -116), BlockPos(-94, 51, -114),
        BlockPos(-94, 51, -115), BlockPos(-94, 51, -116), BlockPos(-95, 51, -108), BlockPos(-95, 51, -109),
        BlockPos(-95, 51, -110), BlockPos(-95, 51, -111), BlockPos(-95, 51, -114), BlockPos(-96, 51, -108),
        BlockPos(-96, 51, -111), BlockPos(-96, 51, -112), BlockPos(-96, 51, -114), BlockPos(-96, 51, -115),
        BlockPos(-96, 51, -116), BlockPos(-97, 51, -108), BlockPos(-97, 51, -112), BlockPos(-97, 51, -116),
        BlockPos(-98, 51, -108), BlockPos(-98, 51, -109), BlockPos(-98, 51, -110), BlockPos(-98, 51, -112),
        BlockPos(-98, 51, -113), BlockPos(-98, 51, -114), BlockPos(-98, 51, -115), BlockPos(-98, 51, -116),
        BlockPos(-99, 51, -110), BlockPos(-100, 51, -108), BlockPos(-100, 51, -109), BlockPos(-100, 51, -110),
        BlockPos(-100, 51, -112), BlockPos(-100, 51, -113), BlockPos(-100, 51, -114), BlockPos(-100, 51, -115),
        BlockPos(-100, 51, -116), BlockPos(-101, 51, -108), BlockPos(-101, 51, -112), BlockPos(-101, 51, -116),
        BlockPos(-102, 51, -108), BlockPos(-102, 51, -109), BlockPos(-102, 51, -110), BlockPos(-102, 51, -111),
        BlockPos(-102, 51, -112), BlockPos(-102, 51, -114), BlockPos(-102, 51, -115), BlockPos(-102, 51, -116),
        BlockPos(-103, 51, -114), BlockPos(-104, 51, -112), BlockPos(-104, 51, -113), BlockPos(-104, 51, -114),
        BlockPos(-105, 51, -112), BlockPos(-105, 51, -113), BlockPos(-90, 51, -107)
    )
}