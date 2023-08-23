package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.misc.SmoothTransfer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ProgressScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProgressScreen.class)
public class ProgressScreenMixin {
    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ProgressScreen;renderBackground(Lnet/minecraft/client/gui/DrawContext;)V"), cancellable = true)
    private void mithras$skipLoadingProgress(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (SmoothTransfer.INSTANCE.shouldHideLoadingScreen())
            ci.cancel();
    }
}
