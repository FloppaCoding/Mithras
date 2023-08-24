package floppacoding.mithras.mixin;

import floppacoding.mithras.module.impl.render.Particles;
import net.minecraft.block.BlockState;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleManager.class)
public class ParticleManagerMixin {

    @Inject(method = "addBlockBreakParticles", at = @At("HEAD"), cancellable = true)
    private void mithras$hideBlockBreakParticles(BlockPos pos, BlockState state, CallbackInfo ci) {
        if(Particles.INSTANCE.shouldPreventBreakParticles())
            ci.cancel();
    }

    @Inject(method = "addParticle(Lnet/minecraft/client/particle/Particle;)V", at = @At("HEAD"), cancellable = true)
    private void mithras$hideParticles(Particle particle, CallbackInfo ci) {
        if(Particles.INSTANCE.shouldHideParticle(particle))
            ci.cancel();
    }
}
