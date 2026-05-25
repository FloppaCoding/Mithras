package floppacoding.mithras.module.impl.render

import floppacoding.mithras.Mithras
import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import net.minecraft.block.Blocks
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.command.OrderedRenderCommandQueue
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.render.entity.state.ItemEntityRenderState
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState
import net.minecraft.client.render.item.ItemRenderState
import net.minecraft.client.render.state.CameraRenderState
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.RotationAxis
import net.minecraft.util.math.random.Random

/**
 * This module makes it looks as if dropped items would follow collision physics.
 * @author Aton
 */
object ItemPhysics : Module(
    "Item Physics",
    category = Category.RENDER,
    description = "Makes dropped items look as if they obey collision physics."
) {

    /**
     * Returns whether to prevent item shadows from rendering.
     * @see floppacoding.mithras.mixin.render.EntityRenderDispatcherMixin.onRenderShadows
     */
    fun shouldDisableItemShadows(): Boolean = this.enabled

    /**
     * A replacement of the vanilla code for rendering ItemEntities.
     *
     * Returns true when this method handled the rendering, to signal that the vanilla rendering should bs skipped.
     * @see floppacoding.mithras.mixin.render.ItemEntityRendererMixin.onRender
     */
    fun render(
        itemEntityRenderState: ItemEntityRenderState?,
        matrixStack: MatrixStack,
        queue: OrderedRenderCommandQueue,
        cameraRenderState: CameraRenderState,
        box: Box,
        random: Random
    ): Boolean {
        if (!this.enabled || itemEntityRenderState?.itemRenderState?.isEmpty != false) return false

        matrixStack.push()
        val f = -(box.minY.toFloat()) + 0.0625f

        // Make the item flush with the ground
        matrixStack.translate(0.0f, -0.175f + f, 0.0f)

        if (!isOnGround(itemEntityRenderState)) {
            // Makes the item spin while in the air.
            val partialTicks = Mithras.mc.renderTickCounter.getTickProgress(true)
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((itemEntityRenderState.age + partialTicks) *(itemEntityRenderState.uniqueOffset - 3.1415927f) * 0.2f ))

        }else {
            // Prevent items from sinking in snow layers / soul sand.
            if (isItemSunken(itemEntityRenderState)) {
                matrixStack.translate(0.0f, 0.125f, 0.0f)
            }
        }

        // Give the item an item specific random rotation. Instead of the yaw itemEntity.uniqueOffset can also be used.
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(itemEntityRenderState.uniqueOffset))

        val hasDepth = box.lengthZ > 0.0625f
        // Make the items lie on the side.
        if (!hasDepth) {
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(-Math.PI.toFloat() / 2))
        }else {
            matrixStack.translate(0f, 0.125f, 0f)
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(-Math.PI.toFloat() / 2))
            matrixStack.translate(0f, -0.125f, 0f)
        }

        ItemEntityRenderer.render(
            matrixStack, queue, itemEntityRenderState.light, itemEntityRenderState,
            random, box
        )
        matrixStack.pop()

        return true
    }

    fun renderStack(matrices: MatrixStack, queue: OrderedRenderCommandQueue, light: Int, state: ItemStackEntityRenderState, random: Random, box: Box) : Boolean{
        if (!this.enabled || state.itemRenderState?.isEmpty != false) return false
        val i: Int = state.renderedAmount
        if (i != 0) {
            random.setSeed(state.seed.toLong())
            val itemRenderState: ItemRenderState = state.itemRenderState
            val f: Float = box.lengthZ.toFloat()
            if (f > 0.0625f) {
                itemRenderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor)

                for (j in 1..<i) {
                    matrices.push()
                    val g: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val h: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
                    val k: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.7f // *0.7f to slightly reduce how high the items stack in y direction.
                    matrices.translate(g, h, k)
                    itemRenderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor)
                    matrices.pop()
                }
            } else {
                val l = f * 1.0f // original value is 1.5f. With 0.5f the items lie right ontop of each other.
                //matrices.translate(0.0f, 0.0f, -(l * (i - 1) / 2.0f)) // this would make the items sink into the ground
                itemRenderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor)
                matrices.translate(0.0f, 0.0f, l)

                for (m in 1..<i) {
                    matrices.push()
                    val h: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    val k: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
                    matrices.translate(h, k, 0.0f)
                    itemRenderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, state.outlineColor)
                    matrices.pop()
                    matrices.translate(0.0f, 0.0f, l)
                }
            }
        }
        return true
    }

    private fun isOnGround(renderState: ItemEntityRenderState): Boolean {
        val pos = BlockPos.ofFloored(renderState.x, renderState.y-0.1, renderState.z)
        return Mithras.mc.world?.getBlockState(pos)?.isSolid ?: true
    }

    private fun isItemSunken(renderState: ItemEntityRenderState) : Boolean {
        val pos = BlockPos.ofFloored(renderState.x, renderState.y, renderState.z)
        val block = Mithras.mc.world?.getBlockState(pos)?.block
        return block == Blocks.SNOW || block == Blocks.SOUL_SAND
    }
}