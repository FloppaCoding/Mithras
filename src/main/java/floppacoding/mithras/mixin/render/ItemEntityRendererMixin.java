package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.render.ItemPhysics;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.render.entity.state.ItemStackEntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
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

    protected ItemEntityRendererMixin(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    /**
     * Allows for overriding the item rendering.
     */
    @Inject(method = "render(Lnet/minecraft/client/render/entity/state/ItemEntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/client/render/state/CameraRenderState;)V", at = @At("HEAD"), cancellable = true)
    private void onRender(ItemEntityRenderState itemEntityRenderState, MatrixStack matrixStack, OrderedRenderCommandQueue orderedRenderCommandQueue, CameraRenderState cameraRenderState, CallbackInfo ci) {
        if (ItemPhysics.INSTANCE.render(itemEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState, itemEntityRenderState.itemRenderState.getModelBoundingBox(), this.random)) {
            super.render(itemEntityRenderState, matrixStack, orderedRenderCommandQueue, cameraRenderState);
            ci.cancel();
        }

    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;Lnet/minecraft/util/math/Box;)V", at = @At("HEAD"), cancellable = true)
    private static void onRenderStack(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, ItemStackEntityRenderState state, Random random, Box box, CallbackInfo ci) {
        if (ItemPhysics.INSTANCE.renderStack(matrices, queue, light, state, random, box)) {
            ci.cancel();
        }
    }
}
