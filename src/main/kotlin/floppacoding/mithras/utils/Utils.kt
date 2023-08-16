package floppacoding.mithras.utils

import floppacoding.mithras.Mithras.mc
import net.minecraft.client.gui.screen.Screen

object Utils {

    /**
     * Used for executors
     */
    inline val Int.seconds: Long
        get() = (this * 1_000_000_000).toLong()

    fun setScreen(screen: Screen) {
        mc.send {
            mc.setScreen(screen)
        }
    }

}