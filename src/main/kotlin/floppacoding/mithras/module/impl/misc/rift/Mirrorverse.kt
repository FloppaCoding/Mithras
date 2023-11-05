package floppacoding.mithras.module.impl.misc.rift

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import floppacoding.mithras.Mithras
import floppacoding.mithras.Mithras.RESOURCE_DOMAIN
import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.ClientTickEvent
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.utils.ChatUtils.modMessage
import floppacoding.mithras.utils.Extensions.Box
import floppacoding.mithras.utils.Extensions.offsetAlpha
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
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Vec3d
import java.awt.Color

/**
 * Helps complete some of the puzzles within the Rift's mirrorverse
 *
 * @author Stivais
 */
object Mirrorverse : Module(
    "Mirrorverse", // maybe rename, and add description and maybe make it customizable
    category = Category.MISC,
) {
    private var currentArea: Puzzles? = null

    @EventHandler
    fun onTick(event: ClientTickEvent) {
        if (inMirrorverse()) {
            currentArea = Puzzles.entries.firstOrNull {
                it.check()
            }
        }
    }

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (inMirrorverse()) {
            currentArea?.let {
                for (i in it.blocks) {
                    Renderer3D.drawFilledBox(event.context, i, Color.RED.withAlpha(100))
                }
                it.onRender(event.context)
            }
        }
    }

    @Suppress("UNUSED")
    internal enum class Puzzles(
        private val minX: Int,
        private val minY: Int,
        private val minZ: Int,
        private val maxX: Int,
        private val maxY: Int,
        private val maxZ: Int,
    ) : RiftPuzzle {
        FOUR_LEVERS(-80, 64, -121, -85, 69, -113) {
            override val blocks: Array<Box> = arrayOf()

            override fun onRender(ctx: WorldRenderContext) {
                renderLever(ctx, BlockPos(-91, 65, -119), Vec3d(-80.5, 65.25, -118.75))
                renderLever(ctx, BlockPos(-91, 67, -119), Vec3d(-80.5, 67.25, -118.75))
                renderLever(ctx, BlockPos(-91, 67, -116), Vec3d(-80.5, 67.25, -115.75))
                renderLever(ctx, BlockPos(-91, 65, -116), Vec3d(-80.5, 65.25, -115.75))
            }
        },
        LAVA_MAZE(-81, 52, -117, -105, 61, -106) {
            override val blocks: Array<Box> = getBlocks(file = "lavamaze")

            override fun onRender(ctx: WorldRenderContext) {
                renderLever(ctx, BlockPos(-105, 52, -99), Vec3d(-104.75, 52.0, -112.75))
            }
        },
        CRAFTING_ROOM(-108, 52, -116, -117, 58, -106) {
            override val blocks: Array<Box> = arrayOf(Box(-113, 52, 115, 112, 53, 114))

            override fun onRender(ctx: WorldRenderContext) {
                for (i in mc.world!!.entities) {
                    val mirror = Box(-108, 52, -127, -117, 58, -117)
                    if (!mirror.contains(i.pos) || !i.isAlive) continue

                    val color = when (i) {
                        is SlimeEntity -> Color.GREEN
                        is ZombieEntity -> Color(147, 117, 76)
                        is CaveSpiderEntity -> Color(200, 200, 200)
                        else -> Color(0, 0, 0, 0)
                    }
                    val pos = i.boundingBox.offset(.0, .0, (i.pos.z + 117.0) * -2 + 1)
                    Renderer3D.drawBox(ctx, pos, color, color.offsetAlpha(-200))
                }
            }
        },
        UPSIDE_DOWN_PARKOUR(-116, 35, -125, -224, 46, -91) {
            override val blocks: Array<Box> = getBlocks(file = "upsidedownparkour")
            override fun onRender(ctx: WorldRenderContext) {
                // no-op
            }
        },

        RED_GREEN(-225, 43, -109, -261, 50, -104) {
            override val blocks: Array<Box> = arrayOf()
            private var entered = false
            override fun onRender(ctx: WorldRenderContext) {
                if (!entered) {
                    modMessage("Watch a guide for this one.")
                    entered = true
                }
            }
        },
        TURBULATOR(-298, 1, -112, -309, 62, -101) {
            override val blocks: Array<Box> = getBlocks("tubulator")
            override fun onRender(ctx: WorldRenderContext) {
                // no-op
            }
        },
        REWARD_ROOM(-305, 56, -100, 290, 63, -81) {
            override val blocks: Array<Box> = arrayOf()
            override fun onRender(ctx: WorldRenderContext) {
                Renderer3D.drawBox(
                    ctx,
                    Box(-293.8, 56, -87.8, -293.2, 57.8, -87.2),
                    Color.BLUE,
                    Color.BLUE.withAlpha(125
                    )
                )
            }
        };

        fun check(): Boolean = Box(minX, minY, minZ, maxX, maxY, maxZ).contains(mc.player!!.pos)
    }

    interface RiftPuzzle {
        val blocks: Array<Box>
        fun onRender(ctx: WorldRenderContext)
    }

    /**
     * Gets an array of boxes used for rift puzzles parkour's
     *
     * @param file The file name of the json file in resources/assets/mithras/rift/<FILENAME>.json
     */
    fun getBlocks(file: String): Array<Box> {
        return try {
            val resource = mc.resourceManager.getResource(Identifier(RESOURCE_DOMAIN, "rift/$file.json"))
            val stream = resource.get().inputStream

            Gson().fromJson(stream.bufferedReader(), object : TypeToken<Array<Box>>() {}.type)
        } catch (e: JsonSyntaxException) {
            Mithras.logger.error("Error parsing rift $file data.")
            arrayOf()
        } catch (e: JsonIOException) {
            Mithras.logger.error("Error reading rift $file data.")
            arrayOf()
        }
    }

    /**
     * Checks the lever in mirror's position and renders the lever by color.
     *
     * @param mirrorLever Position of lever in the mirror
     * @param realLever Place where it should render a box to represent a lever.
     */
    private fun renderLever(ctx: WorldRenderContext, mirrorLever: BlockPos, realLever: Vec3d) {
        if (mc.world!!.getBlockState(mirrorLever).block != Blocks.LEVER) return
        val color = if (mc.world!!.getBlockState(mirrorLever).get(POWERED)) Color.GREEN else Color.RED
        Renderer3D.drawBox(ctx, Box(realLever, realLever.add(.5, .5, .5)), color, color.withAlpha(100))
    }

    /**
     * Checks if the player is inside the mirror verse.
     */
    private fun inMirrorverse(): Boolean {
        return currentRegionPair?.first?.data?.name == "The Rift" && cleanSB(sidebarLines[4]) == "  Mirrorverse"
    }
}