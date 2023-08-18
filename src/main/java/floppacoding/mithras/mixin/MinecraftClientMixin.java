package floppacoding.mithras.mixin;

import floppacoding.mithras.module.impl.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

    /**
     * Allows for skipping the front view perspective.
     */
    @ModifyArg(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setPerspective(Lnet/minecraft/client/option/Perspective;)V"))
    private Perspective modifyPerspective(Perspective perspective) {
        if(Camera.INSTANCE.shouldSkipPerspective(perspective))
            return perspective.next();
        return perspective;
    }
}
