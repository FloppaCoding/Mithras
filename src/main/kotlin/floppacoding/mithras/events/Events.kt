package floppacoding.mithras.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil.Key

//
// A collection of all basic events for this mod.
// Author: Aton

/**
 * Posted when the game starts, after most initialization steps are done.
 * @see floppacoding.mithras.mixin.MainMixin.onGameStart
 */
class GameStartEvent

/**
 * Fired at the start and end of a client tick.
 * @see floppacoding.mithras.mixin.MinecraftClientMixin.onStartTick
 * @see floppacoding.mithras.mixin.MinecraftClientMixin.onEndTick
 */
class ClientTickEvent(val phase: Phase) {
    enum class Phase {
        START, END
    }
}

/**
 * Posted when a key or mouse button is pressed, before the inputs are evaluated.
 * Only posted when not in a GUI.
 * @param action Signals whether the key was pressed or released. 1 = pressed, 0 = released.
 * @see floppacoding.mithras.mixin.MouseMixin.onMouseClick
 * @see floppacoding.mithras.mixin.KeyboardMixin.onKeyPress
 */
class InputEvent(val key: Key, val action: Int) : Cancellable() {
    companion object {
        const val RELEASED = 0
        const val PRESSED = 1
    }
}

/**
 * Posted when the mouse is scrolled, before the input is evaluated by the vanilla methods.
 * Only posted when not in a GUI.
 * @see floppacoding.mithras.mixin.MouseMixin.onScroll
 */
class MouseScrollEvent(val amount: Double) : Cancellable()

/**
 * Posted when the in game hud is being rendered.
 * Posted right after the hotbar is rendered but before everything else is rendered.
 * Does not get posted when the hud is hidden but does get posted in spectator
 * @see floppacoding.mithras.mixin.InGameHudMixin.onRenderCrosshair
 */
class HudRenderEvent(val context: DrawContext, val partialTicks: Float)

