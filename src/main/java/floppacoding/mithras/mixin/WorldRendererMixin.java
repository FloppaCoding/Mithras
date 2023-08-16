package floppacoding.mithras.mixin;

import net.minecraft.client.render.DimensionEffects;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    /**
     * Prevents the sunrise and sunset overriding the fog.
     * That sunset / sunrise fog gets bugged by using nanovg rendering.
     * This is a pseudo fix to that problem for now.
     */
    @Redirect(method = "renderSky(Lnet/minecraft/client/util/math/MatrixStack;Lorg/joml/Matrix4f;FLnet/minecraft/client/render/Camera;ZLjava/lang/Runnable;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/DimensionEffects;getFogColorOverride(FF)[F"))
    private float[] preventFogOverride(DimensionEffects instance, float skyAngle, float tickDelta) {
        return null;
    }
}
