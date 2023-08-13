package floppacoding.mithras.events

import net.minecraft.client.util.InputUtil.Key

/**
 * Posted when the game starts, after most initialization steps are done.
 * @see floppacoding.mithras.mixin.MainMixin.onGameStart
 */
class GameStartEvent

/**
 * Posted before mouse inputs are evaluated.
 * Only when not in an GUI.
 * @see floppacoding.mithras.mixin.MouseMixin.onMouseClick
 */
class PreMouseInputEvent(key: Key)

/**
 * Posted before keyboard input is evaluated. Only when not in GUI.
 * @see floppacoding.mithras.mixin.KeyboardMixin.onKeyPress
 */
class PreKeyInputEvent(key: Key)

