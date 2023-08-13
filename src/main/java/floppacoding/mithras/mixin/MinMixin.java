package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GameStartEvent;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public abstract class MinMixin {

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;finishInitialization()V", shift = At.Shift.AFTER))
    private static void onGameStart(String[] args, CallbackInfo ci) {
        Mithras.INSTANCE.getEVENT_BUS().post(new GameStartEvent());
    }
}
