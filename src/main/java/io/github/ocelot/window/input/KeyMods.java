package io.github.ocelot.window.input;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Boolean representations of each key modifier passed from GLFW.
 *
 * @param mods The raw modifiers bitfield
 * @author Ocelot
 */
public record KeyMods(int mods,
                      boolean shift,
                      boolean control,
                      boolean alt,
                      boolean superKey,
                      boolean caps,
                      boolean numLock) {

   public KeyMods(int mods) {
      this(mods, (mods & GLFW_MOD_SHIFT) > 0, (mods & GLFW_MOD_CONTROL) > 0, (mods & GLFW_MOD_ALT) > 0, (mods & GLFW_MOD_SUPER) > 0, (mods & GLFW_MOD_CAPS_LOCK) > 0, (mods & GLFW_MOD_NUM_LOCK) > 0);
   }
}
