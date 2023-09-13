package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent

/**
 * ## Utilities for playing client side sounds.
 *
 * This class is not strictly needed, but it makes should make playing sounds a little bit easier than using the vanilla
 * methods. The extra layer of abstraction might also with making things easier.
 *
 * @author Aton
 */
object SoundManager {
    /**
     * Plays a sound for the player.
     * @param soundEvent The sound to play. See [SoundEvents][net.minecraft.sound.SoundEvents].
     * @param volume In the range of 0f and 1f.
     * @param pitch In the range of 0f and 2f.
     * @param loud Determines whether the category volume setting should be bypassed. Useful for warning sounds which
     * should play even when the player has muted the corresponding category.
     * @param category The category for which the sound should be played. The corresponding category-volume will affect
     * the volume of the sound played.
     */
    fun playSound(soundEvent: SoundEvent, volume: Float, pitch: Float, loud: Boolean = false, category: SoundCategory? = null) {
        val adjustedCategory = if (loud) SoundCategory.MASTER else category
        if (adjustedCategory != null) {
            mc.player?.playSound(soundEvent, adjustedCategory, volume, pitch)
        }else {
            mc.player?.playSound(soundEvent, volume, pitch)
        }
    }
}