package floppacoding.mithras.events

import net.minecraft.client.gui.DrawContext
import net.minecraft.client.util.InputUtil.Key

/**
 * Posted when the game starts, after most initialization steps are done.
 * @see floppacoding.mithras.mixin.MainMixin.onGameStart
 */
class GameStartEvent

/**
 * Posted when a key or mouse button is pressed, before the inputs are evaluated.
 * Only when not in an GUI.
 * @see floppacoding.mithras.mixin.MouseMixin.onMouseClick
 * @see floppacoding.mithras.mixin.KeyboardMixin.onKeyPress
 */
class InputEvent(val key: Key)

/**
 * Posted when the in game hud is being rendered.
 * Posted right after the hotbar is rendered but before everything else is rendered.
 * Does not get posted when the hud is hidden but does get posted in spectator
 * @see floppacoding.mithras.mixin.InGameHudMixin.onRenderCrosshair
 */
class HudRenderEvent(val context: DrawContext, val partialTicks: Float)

