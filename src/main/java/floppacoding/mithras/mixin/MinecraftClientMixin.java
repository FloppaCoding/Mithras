package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.ClientTickEvent;
import floppacoding.mithras.module.impl.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "tick")
    private void onStartTick(CallbackInfo info) {
        Mithras.EVENT_BUS.post(new ClientTickEvent(ClientTickEvent.Phase.START));
    }

    @Inject(at = @At("RETURN"), method = "tick")
    private void onEndTick(CallbackInfo info) {
        Mithras.EVENT_BUS.post(new ClientTickEvent(ClientTickEvent.Phase.END));
    }

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
