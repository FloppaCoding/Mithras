package floppacoding.mithras.mixin.gui;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.HudRenderEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {
    @Inject(method = "render", at = @At(value = "HEAD"))
    public void onRenderHUD(DrawContext context, float tickDelta, CallbackInfo ci) {
        // This point should not be reached when the hud is hidden, if it does uncomment this.
        //        if (this.client.options.hudHidden) return;

        Mithras.EVENT_BUS.post(new HudRenderEvent( tickDelta));
    }

    /**
     * Prevent the vignetting. It messes with the stuff rendered during the {@link HudRenderEvent}, which has to come
     * before to prevent issues. The vignette is also pointless so it's safe to just always disable it.
     */
    @Inject(method = "renderVignetteOverlay", at = @At("HEAD"), cancellable = true)
    private void noVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        ci.cancel();
    }
}
