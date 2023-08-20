package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.render.Fullbright;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Objects;

/**
 * Enables the gamma override module.
 * @author Aton
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {
    /**
     * Allows for overriding the games brightness / gamma.
     */
    @Redirect(
        method = "update",
        at = @At(value = "INVOKE", target = "Ljava/lang/Double;floatValue()F"),
        slice = @Slice(
            from = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/render/DimensionEffects;shouldBrightenLighting()Z"
            )
        )
    )
    private float gammaOverride(Double instance) {
        return Objects.requireNonNullElseGet(Fullbright.INSTANCE.gammaOverride(), instance::floatValue);
    }
}
