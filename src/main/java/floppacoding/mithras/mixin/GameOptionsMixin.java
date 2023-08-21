package floppacoding.mithras.mixin;

import floppacoding.mithras.module.impl.render.Zoom;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Inject(method = "getFov", at = @At("HEAD"), cancellable = true)
    private void onGetFov(CallbackInfoReturnable<SimpleOption<Integer>> cir) {
        SimpleOption<Integer> fovOverride = Zoom.INSTANCE.fovOverride();
        if (fovOverride != null) {
            cir.setReturnValue(fovOverride);
            cir.cancel();
        }
    }
}
