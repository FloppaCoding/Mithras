package floppacoding.mithras.mixin.render;

import floppacoding.mithras.module.impl.render.Camera;
import floppacoding.mithras.utils.ScreenMixinDuck;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow protected abstract void bobView(MatrixStack matrices, float tickDelta);

    @Shadow @Final private MinecraftClient client;

    /**
     * Disables the hurt tilt.
     */
    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void preventHurtTilt(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (Camera.INSTANCE.shouldDisableHurtTilt()) ci.cancel();
    }

    /**
     * Disables the screen beat.
     */
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;bobView(Lnet/minecraft/client/util/math/MatrixStack;F)V"))
    private void onBobView(GameRenderer instance, MatrixStack matrices, float tickDelta) {
        if (Camera.INSTANCE.shouldAllowViewBobbing()) this.bobView(matrices, tickDelta);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/render/GuiRenderer;incrementFrame()V", shift = At.Shift.AFTER))
    private void renderCustomScreenElements(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci) {
        if (this.client.currentScreen == null) return;
        ((ScreenMixinDuck) this.client.currentScreen).mithras_finishFrame();
    }
}
