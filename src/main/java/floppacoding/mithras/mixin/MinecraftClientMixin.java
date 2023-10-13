package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GuiOpenEvent;
import floppacoding.mithras.events.WorldChangeEvent;
import floppacoding.mithras.module.impl.render.Camera;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {

    @Inject(at = @At("HEAD"), method = "setWorld")
    private void mithras$onSetWorld(ClientWorld world, CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new WorldChangeEvent(world));
    }

    /**
     * Allows for skipping the front view perspective.
     */
    @ModifyArg(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;setPerspective(Lnet/minecraft/client/option/Perspective;)V"))
    private Perspective mithras$modifyPerspective(Perspective perspective) {
        if(Camera.INSTANCE.shouldSkipPerspective(perspective))
            return perspective.next();
        return perspective;
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void onSetScreen(Screen screen, CallbackInfo ci) {
        if (screen != null) {
            Mithras.EVENT_BUS.post(new GuiOpenEvent(screen));
        }
    }
}
