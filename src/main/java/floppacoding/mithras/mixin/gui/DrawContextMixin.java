package floppacoding.mithras.mixin.gui;

import com.llamalad7.mixinextras.sugar.Local;
import floppacoding.mithras.module.impl.misc.ScrollableTooltips;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(DrawContext.class)
public abstract class DrawContextMixin {
    @Shadow @Final private Matrix3x2fStack matrices;

    @Inject(
            method = "drawTooltipImmediately",
            at = @At(value = "INVOKE", target = "Lorg/joml/Matrix3x2fStack;pushMatrix()Lorg/joml/Matrix3x2fStack;", shift = At.Shift.AFTER)
    )
    private void positionTooltip(TextRenderer textRenderer, List<TooltipComponent> components, int x, int y, TooltipPositioner positioner, @Nullable Identifier texture, CallbackInfo ci, @Local(ordinal = 6) int n, @Local(ordinal = 7) int o) {
        if (!ScrollableTooltips.INSTANCE.getEnabled()) return;
        ScrollableTooltips.INSTANCE.scaleTooltip(this.matrices, n, o);
    }
}
