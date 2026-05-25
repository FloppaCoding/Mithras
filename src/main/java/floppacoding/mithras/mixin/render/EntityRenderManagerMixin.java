package floppacoding.mithras.mixin.render;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import floppacoding.mithras.module.impl.render.ItemPhysics;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRenderManager;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;

@Mixin(EntityRenderManager.class)
public abstract class EntityRenderManagerMixin {
    /**
     * Allows for preventing item shadows from rendering.
     */
    @WrapWithCondition(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;submitShadowPieces(Lnet/minecraft/client/util/math/MatrixStack;FLjava/util/List;)V"))
    private static <S extends EntityRenderState> boolean onSubmitShowds(OrderedRenderCommandQueue instance, MatrixStack matrixStack, float v, List<EntityRenderState.ShadowPiece> list, S renderState, CameraRenderState cameraState, double offsetX, double offsetY, double offsetZ, MatrixStack matrices, OrderedRenderCommandQueue queue) {
        return !ItemPhysics.INSTANCE.shouldDisableItemShadows() || renderState.entityType != EntityType.ITEM;
    }
}
