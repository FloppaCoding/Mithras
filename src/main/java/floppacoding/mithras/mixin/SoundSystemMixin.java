package floppacoding.mithras.mixin;

import floppacoding.mithras.Mithras;
import floppacoding.mithras.commands.impl.DebugCommand;
import floppacoding.mithras.events.PlaySoundEvent;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundSystem;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundSystem.class)
public abstract class SoundSystemMixin {

    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)Lnet/minecraft/client/sound/SoundSystem$PlayResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/sound/SoundInstance;getSound()Lnet/minecraft/client/sound/Sound;"), cancellable = true)
    private void onPlaySound(SoundInstance sound, CallbackInfoReturnable<SoundSystem.PlayResult> cir) {
        if (DebugCommand.shouldLogShounds()) {
            SoundCategory logCategory = DebugCommand.getLogCategory();
            SoundCategory soundCategory = sound.getCategory();
            if (logCategory == null || soundCategory == logCategory) { try {
                String categoryName = soundCategory == null ? "" : soundCategory.getName();

                Mithras.getLogger().info("Playing sound {} in category {} with pitch {} at volume {} at [{}, {}, {}], repeatable {} with delay {}", sound.getId(), categoryName, sound.getPitch(), sound.getVolume(), sound.isRepeatable(), sound.getX(), sound.getY(), sound.getZ(), sound.getRepeatDelay());
            }catch (Exception e) {
                Mithras.getLogger().info("Playing sound {}", sound);
            } }
        }
        if(Mithras.EVENT_BUS.post(new PlaySoundEvent(sound)).isCancelled()) cir.cancel();
    }
}
