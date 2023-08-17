package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.render.ItemAnimations;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {

    /**
     * Allows for custom position, rotation and scale of the held item.
     */
    @Inject(method = "renderFirstPersonItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/item/HeldItemRenderer;applySwingOffset(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/util/Arm;F)V"))
    public void transformItemPosition(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        ItemAnimations.INSTANCE.itemTransformHook(matrices, hand, swingProgress);
    }

    /**
     * For modifying the item equip animation. This also affects the item swing animation.
     */
    @Inject(method = "applyEquipOffset", at = @At("HEAD"))
    public void tweakEquipProgress(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        ItemAnimations.INSTANCE.equipProgressTransform(matrices, equipProgress);
    }

    /**
     * Used to do the 1.8 swing animation instead of the 1.9+ animation.
     */
    @Redirect(method = "updateHeldItems", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getAttackCooldownProgress(F)F"))
    public float tweakSwing(ClientPlayerEntity instance, float v) {
        if(ItemAnimations.INSTANCE.doOldSwing()) {
            return 1f;
        }else {
            return instance.getAttackCooldownProgress(v);
        }
    }
}
