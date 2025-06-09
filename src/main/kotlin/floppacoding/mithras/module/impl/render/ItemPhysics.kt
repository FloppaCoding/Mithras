package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import net.minecraft.block.Blocks
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.ItemEntityRenderer
import net.minecraft.client.render.entity.state.ItemEntityRenderState
import net.minecraft.client.render.item.ItemRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
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
        itemEntity: ItemEntity?,
        partialTicks: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider?,
        i: Int,
        itemRenderer: ItemRenderer,
        random: Random,
        getRenderedAmount: (ItemStack) -> Int
    ): Boolean {
          if (!this.enabled || itemEntity == null) return false
//        matrixStack.push()
//        val itemStack: ItemStack = itemEntity.stack
//        val seed = if (itemStack.isEmpty) 187 else Item.getRawId(itemStack.item) + itemStack.damage
//        random.setSeed(seed.toLong())
//        val bakedModel = itemRenderer.getModel(itemStack, itemEntity.world, null as LivingEntity?, itemEntity.id)
//        val hasDepth = bakedModel.hasDepth()
//        val renderedAmount: Int = getRenderedAmount(itemStack)
//
//        val o = bakedModel.transformation.ground.scale.x()
//        val p = bakedModel.transformation.ground.scale.y()
//        val q = bakedModel.transformation.ground.scale.z()
//        var s: Float
//        var t: Float
//
//        // Make the item flush with the ground.
//        matrixStack.translate(0f,  0.125f * 0.25f * p, 0f)
//
//        if (!itemEntity.isOnGround) {
//            // Makes the item spin while in the air.
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((itemEntity.itemAge.toFloat() + partialTicks) *(itemEntity.uniqueOffset - 3.1415927f) * 0.2f ))
//
//        }else {
//            // Prevent items from sinking in snow layers / soul sand.
//            if (isItemSunken(itemEntity)) {
//                matrixStack.translate(0.0f, 0.125f, 0.0f)
//            }
//        }
//        // Give the item an item specific random rotation. Instead of the yaw itemEntity.uniqueOffset can also be used.
//        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(itemEntity.yaw))
//
//        // Make the items lie on the side.
//        if (!hasDepth) {
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(-Math.PI.toFloat() / 2))
//        }else {
//            matrixStack.translate(0f, 0.125f, 0f)
//            matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(-Math.PI.toFloat() / 2))
//            matrixStack.translate(0f, -0.125f, 0f)
//        }
//
//        for (u in 0 until renderedAmount) {
//            matrixStack.push()
//            if (u > 0) {
//                if (hasDepth) {
//                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
//                    t = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
//                    val v: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.7f // *0.7f to slightly reduce how high the items stack in y direction.
//                    matrixStack.translate(s, t, v)
//                } else {
//                    s = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
//                    t = (random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f
//                    matrixStack.translate(s, t, 0.0f)
//                }
//            }
//            itemRenderer.renderItem(itemStack, ModelTransformationMode.GROUND, false, matrixStack, vertexConsumerProvider, i, OverlayTexture.DEFAULT_UV, bakedModel)
//            matrixStack.pop()
//            if (!hasDepth) {
//                matrixStack.translate(0.0f * o, 0.0f * p, 0.05f * q) // original value is 0.09375f. With 0.5f the items lie right ontop of each other.
//            }
//        }
//        matrixStack.pop()
          return true
    }

    fun render(
        itemEntityRenderState: ItemEntityRenderState?,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider?,
        i: Int,
        box: Box,
        random: Random
    ): Boolean {
        if (!this.enabled || itemEntityRenderState?.itemRenderState?.isEmpty != false) return false

        matrixStack.push()
        val f = -(box.minY.toFloat()) + 0.0625f
        val g = MathHelper.sin(itemEntityRenderState.age / 10.0f + itemEntityRenderState.uniqueOffset) * 0.1f + 0.1f
        matrixStack.translate(0.0f, g + f, 0.0f)
        val h = ItemEntity.getRotation(itemEntityRenderState.age, itemEntityRenderState.uniqueOffset)
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(h))
        ItemEntityRenderer.renderStack(
            matrixStack, vertexConsumerProvider, i, itemEntityRenderState,
            random, box
        )
        matrixStack.pop()

        return true
    }

    private fun isItemSunken(itemEntity: ItemEntity) : Boolean {
        val block = itemEntity.world.getBlockState(itemEntity.blockPos).block
        return block == Blocks.SNOW || block == Blocks.SOUL_SAND
    }
}