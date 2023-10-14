package floppacoding.mithras.module.impl.render

import floppacoding.mithras.module.Category
import floppacoding.mithras.module.Module
import floppacoding.mithras.module.settings.impl.BooleanSetting
import net.minecraft.client.particle.ExplosionLargeParticle
import net.minecraft.client.particle.Particle

object Particles : Module(
    "Particle reducer",
    category = Category.RENDER,
    description = "Offers a variety of option to disable certain particles from rendering."
) {
    private val hideBlockBreakParticles by BooleanSetting("Hide Block Break", true, description = "Hides block break particles.")
    private val hideExplosion by BooleanSetting("Hide Explosion", true, description = "Hides explosion particles.")

    fun shouldPreventBreakParticles(): Boolean = this.enabled && hideBlockBreakParticles

    fun shouldHideParticle(particle: Particle): Boolean {
        if (!this.enabled) return false
        return when {
            hideExplosion && particle is ExplosionLargeParticle -> true
            else -> false
        }
    }
}