package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.misc.SmoothTransfer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DownloadingTerrainScreen.class)
public abstract class DownloadingTerrainScreenMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"), cancellable = true)
    private void skipLoadingProgress(DrawContext context, int mouseX, int mouseY, float deltaTicks, CallbackInfo ci) {
        if (SmoothTransfer.INSTANCE.shouldHideLoadingScreen())
            ci.cancel();
    }
}
