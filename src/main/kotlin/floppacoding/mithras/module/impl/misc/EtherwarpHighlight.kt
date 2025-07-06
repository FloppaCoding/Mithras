package floppacoding.mithras.module.impl.misc

import floppacoding.mithras.Mithras.mc
import floppacoding.mithras.events.RenderWorldOverlayEvent
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import floppacoding.mithras.module.settings.impl.ColorSetting
import floppacoding.mithras.module.settings.impl.NumberSetting
import floppacoding.mithras.utils.Extensions.isSubclassOfOneOf
import floppacoding.mithras.utils.LocationManager
import floppacoding.mithras.utils.inventory.ItemUtils.extraAttributes
import floppacoding.mithras.utils.render.Renderer3D
import meteordevelopment.orbit.EventHandler
import net.minecraft.block.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.BlockStateRaycastContext
import net.minecraft.world.BlockView
import net.minecraft.world.World
import java.awt.Color

/**
 * A module to highlight in game the block targeted by etherwarp.
 *
 * @author Aton
 */
object EtherwarpHighlight : Module(
    "Etherwarp Highlight",
    category = Category.MISC,
    description = "Highlights the block you are going to etherwarp to. "
) {
    private val validOutlineColor by ColorSetting("Valid Outline", Color(20,255,100), description = "Color of the outline of valid etherwarp targets.")
    private val validFillColor by ColorSetting("Valid Fill", Color(20,255,100,100), description = "Fill color for the sides of valid etherwarp targets.")
    private val invalidOutlineColor by ColorSetting("Invalid Outline", Color(255,55,20), description = "Color of the outline of invalid etherwarp targets.")
    private val invalidFillColor by ColorSetting("Invalid Fill", Color(255,55,20,100), description = "Fill color for the sides of invalid etherwarp targets.")
    private val lineWidth by NumberSetting("Outline Width", 2f, 1f, 4f, description = "Line width of the outline.")
    private val showInvalidTarget by BooleanSetting("Highlight Obstruction", true, description = "Highlights the block which is obstructing the view onto a valid target.")

    @EventHandler
    fun onRender(event: RenderWorldOverlayEvent) {
        if (!LocationManager.inSkyblock || mc.player?.isSneaking != true) return
        // The is holding check is not strictly required since the check for the ethermerge attribute will already cover that
//        if (mc.player?.isHoldingInMainHand(SkyblockItem.AOTV,SkyblockItem.AOTE) != true) return
        val attributes = mc.player?.mainHandStack?.extraAttributes ?: return
        if (!attributes.getBoolean("ethermerge", false)) return
        val distance = 57.0 + attributes.getInt("tuned_transmission", 0)

        val hitResult = raycastEtherwarp(distance) ?: return
        val targetState = mc.world?.getBlockState(hitResult.blockPos) ?: return

        val isValid = if (
            (mc.crosshairTarget as? BlockHitResult)?.blockPos == hitResult.blockPos &&
            doesBlockUsePreventEtherwarp(targetState)
        ) false else isValidTarget(targetState)

        if (!showInvalidTarget && !isValid) return

        val outlineColor: Color
        val fillColor: Color
        if (isValid && canEtherwarpTo(hitResult)) {
            outlineColor = validOutlineColor
            fillColor    = validFillColor
        } else {
            outlineColor = invalidOutlineColor
            fillColor    = invalidFillColor
        }

        Renderer3D.drawBox(event.context, Box(hitResult.blockPos), outlineColor, fillColor, lineWidth)
    }

    private fun canEtherwarpTo(hitResult: BlockHitResult): Boolean {
        val targettedPostion = hitResult.blockPos ?: return false
        for (offset in 1..2) {
            val state = mc.world!!.getBlockState(targettedPostion.up(offset))
            if (isValidTarget(state)) {
                return false
            }
        }
        return true
    }

    private fun isValidTarget(state: BlockState): Boolean {
        if (state.block is LilyPadBlock || state.block is SignBlock) return true
        // i have no clue why adding lilypadblock didn't work to that list. nd it was easier to just do above
//        if (state.block::class.equalsOneOf(nonSolidValidTargets)) return true
        if (state.isSolid) return true
        return false
    }

    /**
     * Determines whether the block can be interacted with by checking whether the class overrides
     * [AbstractBlock.onUse][net.minecraft.block.AbstractBlock.onUse].
     *
     * That method is deprecated, but that is fine in this case.
     */
    private fun doesBlockUsePreventEtherwarp(state: BlockState): Boolean {
        // blocks which have this but should not:
        // stairs, fence, daylight sensor, juke box, not block, tnt
        if (state.block::class.isSubclassOfOneOf(*usableBypass)) return false
        return try {
            state.block::class.java.getMethod(
                "method_9534", // for debugging this has ot be "onUse"
                BlockState::class.java,
                World::class.java,
                BlockPos::class.java,
                PlayerEntity::class.java,
                Hand::class.java,
                BlockHitResult::class.java
            ).declaringClass !== AbstractBlock::class.java
        } catch (_: NoSuchMethodException) {
            false
        }
    }

    private fun raycastEtherwarp(distance: Double) : BlockHitResult? {
        // Crouching eye height is modern versions is 1.27F,
        // this is used in areas that only support modern versions
        // however in servers that continue support for 1.8.9, eye height is the one in older versions, 1.54
        val eyeHeight = if (LocationManager.currentArea?.isIslandModern() == true) 1.27 else 1.54

        val start = mc.player?.pos?.add(0.0,eyeHeight,0.0) ?: return null
        val direction = mc.player?.rotationVector ?: return null
        val end = start.add(direction.multiply(distance))

        val context = BlockStateRaycastContext(start, end) {
            !it.isAir
        }

        return BlockView.raycast<BlockHitResult?, BlockStateRaycastContext>(context.start, context.end, context,
            { innerContext: BlockStateRaycastContext, pos: BlockPos ->
                val blockState: BlockState = mc.world!!.getBlockState(pos)
                if (innerContext.statePredicate.test(blockState)) {
                    val vec3d = innerContext.start.subtract(innerContext.end)
                    BlockHitResult(
                        pos.toCenterPos(),
                        Direction.getFacing(vec3d.x, vec3d.y, vec3d.z),
                        pos,
                        false
                    )
                } else null
            }
        ) {
            null
        }

        // Some raycast info.
        //
        // mc.player?.raycast(distance,event.context.tickDelta(), false) would be a simple option but it does not match
        // the raycast done by hypixel.
        // mc.player.raycast should always return BlockHitResult, even though the signature says it can return any HitResult.
        // It returns the result of mc.world.raycast which is of type BlockHitResult.
        //
        // DO NOT USE BlockView.raycast(BlockStateRaycastContext), it will always return the block at the end!
    }

//    private val nonSolidValidTargets = listOf<KClass<*>>(
//        SignBlock::class,
//    )

    private val usableBypass = arrayOf(
        TntBlock::class,
        AbstractSignBlock::class,
        FenceBlock::class,
        StairsBlock::class,
        DaylightDetectorBlock::class,
        JukeboxBlock::class,
        NoteBlock::class,
    )

    /* ETHERWARP RULES:
    // The etherwarp raycast ignores the shape of the block and treats everything like a full cube.
    //
    // If the cursor is on the block you cannot etherwarp to it.
    //
    // Blocks ignored by the raycast: item frame.
    //
    // When a block you cannot etherwarp to is in the line of sight you cannot etherwarp to anything behind it.
    //
    // You cannot etherwarp to torches, foliage, skulls, tripwire, fluids, fire, ladder, redstone, repeater, comparator.
    // These will block the line of sight, but when on top of a target they will not prevent the etherwarp.
    //
    // You can etherwarp to signs and lily pads.
    //
    // The following blocks can be on top of the target and will not prevent etherwarp:
    // Skull, fire, torch, foliage.
    //
    // The following block will prevent etherwarp if in the space above the target:
    // Solid blocks, sign, banner,
    //
    // If there is fire on top of the block you cannot etherwarp from it when looking at the top side.
    */
}