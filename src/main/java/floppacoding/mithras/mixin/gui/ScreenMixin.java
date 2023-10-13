package floppacoding.mithras.mixin.gui;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.events.GuiBackgroundDrawnEvent;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void onRenderBackground(DrawContext context, CallbackInfo ci) {
        Mithras.EVENT_BUS.post(new GuiBackgroundDrawnEvent((Screen) (Object) this, context));
    }
}
