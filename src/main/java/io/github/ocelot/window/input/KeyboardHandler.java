package io.github.ocelot.window.input;

import io.github.ocelot.window.Window;
import io.github.ocelot.window.WindowEventListener;
import org.lwjgl.glfw.GLFW;

import java.util.BitSet;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_UNKNOWN;

/**
 * Tracks pressed keys using keycode and scancode.
 *
 * @author Ocelot
 */
public class KeyboardHandler implements WindowEventListener {

    private final BitSet keys;
    private final BitSet scanKeys;

    public KeyboardHandler() {
        this.keys = new BitSet(GLFW_KEY_LAST + 1);
        this.scanKeys = new BitSet();
    }

    @Override
    public void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
        if (key == GLFW_KEY_UNKNOWN) {
            this.scanKeys.set(scanCode, true);
        } else {
            this.keys.set(key, true);
        }
    }

    @Override
    public void keyReleased(Window window, int key, int scanCode, KeyMods mods) {
        if (key == GLFW_KEY_UNKNOWN) {
            this.scanKeys.set(scanCode, false);
        } else {
            this.keys.set(key, false);
        }
    }

    /**
     * Checks to see if the specified key or scan code is pressed.
     *
     * @param keyCode  The id of the key or {@link GLFW#GLFW_KEY_UNKNOWN} to defer to the scan code
     * @param scanCode The device-specific scan code
     * @return Whether that key is pressed
     */
    public boolean isKeyPressed(int keyCode, int scanCode) {
        return keyCode == GLFW_KEY_UNKNOWN ? this.scanKeys.get(scanCode) : this.keys.get(keyCode);
    }

    /**
     * Checks to see if the specified key is pressed. {@link #isKeyPressed(int, int)} should be used if scanCode is available to support device-specific keys.
     *
     * @param keyCode The id of the key
     * @return Whether that key is pressed
     */
    public boolean isKeyPressed(int keyCode) {
        return keyCode != GLFW_KEY_UNKNOWN && this.keys.get(keyCode);
    }
}
