package floppacoding.mithras.utils;


import floppacoding.mithras.events.InputEvent;
import net.minecraft.client.util.InputUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This class has to exist because Mixin classes don't allow public static members.
 * But such a member is required to effectively unpress all buttons on GUI open.
 * @author Aton
 */
public abstract class KeyStateTracker {
    /**
     * Used to keep track of key states. Only when a state is changed an {@link InputEvent} will be posted.
     */
    public static final HashMap<InputUtil.Key, Integer> keyStates = new HashMap<>();

    /**
     * Sets the previous state of all keys to unpressed.
     * This can be used when a gui is opened so that the game will not falsely assume that some keys are still pressed.
     */
    public static void unpressAllKeys() {
        for (Map.Entry<InputUtil.Key, Integer> keyIntegerEntry : keyStates.entrySet()) {
            keyIntegerEntry.setValue(0);
        }
    }
}
