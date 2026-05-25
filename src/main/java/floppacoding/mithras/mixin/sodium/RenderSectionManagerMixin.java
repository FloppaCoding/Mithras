package floppacoding.mithras.mixin.sodium;

import floppacoding.mithras.module.impl.render.Camera;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionManager.class)
public abstract class RenderSectionManagerMixin {

    @Shadow(remap = false) protected abstract float getRenderDistance();

    @Inject(method = "getSearchDistance", at = @At("HEAD"), cancellable = true, remap = false)
    private void preventFogOcclusion(FogParameters fogParameters, CallbackInfoReturnable<Float> cir) {
        if (Camera.shouldPreventFogOcclusion()) {
            cir.setReturnValue(this.getRenderDistance());
        }
    }
}
