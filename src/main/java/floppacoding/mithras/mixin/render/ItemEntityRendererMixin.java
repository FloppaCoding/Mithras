package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.render.ItemPhysics;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntityRenderer.class)
public abstract class ItemEntityRendererMixin extends EntityRenderer<ItemEntity, ItemEntityRenderState> {
    @Shadow @Final private Random random;


    @Shadow
    private static Box getBoundingBox(ItemRenderState state) {
        Box.Builder builder = new Box.Builder();
        state.load(builder::encompass);
        return builder.build();
    }

    protected ItemEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    /**
     * Allows for overriding the item rendering.
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("HEAD"), cancellable = true)
    private void onRender(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
        if (ItemPhysics.INSTANCE.render(itemEntityRenderState, matrixStack, vertexConsumerProvider, i, getBoundingBox(itemEntityRenderState.itemRenderState), this.random)) {
            super.render(itemEntityRenderState, matrixStack, vertexConsumerProvider, i);
            ci.cancel();
        }

    }

    @Inject(method = "renderStack", at = @At("HEAD"), cancellable = true)
    private static void onRenderStack(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, ItemStackEntityRenderState state, Random random, Box box, CallbackInfo ci) {
        if (ItemPhysics.INSTANCE.renderStack(matrices, vertexConsumers, light, state, random, box)) {
            ci.cancel();
        }
    }
}
