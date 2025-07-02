package floppacoding.mithras.mixin.gui;

import floppacoding.mithras.module.impl.player.InventoryTweaks;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.StatusEffectsDisplay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StatusEffectsDisplay.class)
public abstract class StatusEffectsDisplayMixin {
    @Inject(method = "drawStatusEffects(Lnet/minecraft/client/gui/DrawContext;II)V", at = @At("HEAD"), cancellable = true)
    private void hideStatusEffects(DrawContext context, int mouseX, int mouseY, CallbackInfo ci) {
        if (InventoryTweaks.shouldHideStatusEffects()) ci.cancel();
    }
}
