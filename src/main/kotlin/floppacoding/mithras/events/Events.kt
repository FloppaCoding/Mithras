package floppacoding.mithras.events

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

