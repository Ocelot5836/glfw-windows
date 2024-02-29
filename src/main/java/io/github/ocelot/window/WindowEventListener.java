package io.github.ocelot.window;

import io.github.ocelot.window.input.KeyMods;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

/**
 * Listens to events on a {@link Window}.
 *
 * @author Ocelot
 */
public interface WindowEventListener {

    /**
     * Called when the window is closed.
     *
     * @param window The window that fired the event
     */
    default void windowClosed(Window window) {
    }

    /**
     * Called when the window is moved.
     *
     * @param window The window that fired the event
     * @param x      The new x position of the window
     * @param y      The new y position of the window
     */
    default void windowMoved(Window window, int x, int y) {
    }

    /**
     * Called when the window is resized.
     * <br>
     * <strong>Note: {@link #framebufferResized(Window, int, int)} should be used to resize rendering.</strong>
     *
     * @param window The window that fired the event
     * @param width  The new x size of the window
     * @param height The new y size of the window
     */
    default void windowResized(Window window, int width, int height) {
    }

    /**
     * Called when the window framebuffer is resized.
     *
     * @param window The window that fired the event
     * @param width  The new x size of the framebuffer
     * @param height The new y size of the framebuffer
     */
    default void framebufferResized(Window window, int width, int height) {
    }

    /**
     * Called when the window enters or exits focus.
     *
     * @param window  The window that fired the event
     * @param focused Whether the window is now focused
     */
    default void focusChanged(Window window, boolean focused) {
    }

    /**
     * Called when files are dropped onto the window.
     *
     * @param window The window that fired the event
     * @param files  The paths to each file dropped
     */
    default void filesDropped(Window window, Path... files) {
    }

    /**
     * Called when a character is typed.
     *
     * @param window    The window that fired the event
     * @param codePoint The code point typed. To get actual chars use {@link Character#toChars(int)}
     * @param mods      The keyboard modifier flags set when the key was typed
     */
    default void charTyped(Window window, int codePoint, KeyMods mods) {
    }

    /**
     * Called when a key on the keyboard is pressed.
     *
     * @param window   The window that fired the event
     * @param key      The universal key code
     * @param scanCode The machine-specific scan code
     * @param mods     The keyboard modifier flags set when the key was pressed
     */
    default void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
    }

    /**
     * Called when a key on the keyboard is released.
     *
     * @param window   The window that fired the event
     * @param key      The universal key code
     * @param scanCode The machine-specific scan code
     * @param mods     The keyboard modifier flags set when the key was released
     */
    default void keyReleased(Window window, int key, int scanCode, KeyMods mods) {
    }

    /**
     * Called when a key on the keyboard is held util it repeats.
     *
     * @param window   The window that fired the event
     * @param key      The universal key code
     * @param scanCode The machine-specific scan code
     * @param mods     The keyboard modifier flags set when the key was repeated
     */
    default void keyRepeated(Window window, int key, int scanCode, KeyMods mods) {
    }

    /**
     * Called when the mouse moves.
     *
     * @param window The window that fired the event
     * @param x      The amount the mouse moved in the x
     * @param y      The amount the mouse moved in the y
     */
    default void mouseMoved(Window window, double x, double y) {
    }

    /**
     * Called when the mouse enters or exists the window.
     *
     * @param window  The window that fired the event
     * @param entered Whether the cursor entered or left the window
     */
    default void cursorEntered(Window window, boolean entered) {
    }

    /**
     * Called when a mouse button is pressed.
     *
     * @param window The window that fired the event
     * @param button The button that was pressed.
     *               Usually one of:
     *               <br>
     *               <table>
     *               <caption>Standard Mouse Buttons</caption>
     *                   <tr>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_LEFT MOUSE_BUTTON_LEFT}</td>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_RIGHT MOUSE_BUTTON_RIGHT}</td>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_MIDDLE MOUSE_BUTTON_MIDDLE}</td>
     *                   </tr>
     *               </table>
     * @param mods   The keyboard modifier flags set when the mouse button was pressed
     */
    default void mousePressed(Window window, int button, KeyMods mods) {
    }

    /**
     * Called when a mouse button is released.
     *
     * @param window The window that fired the event
     * @param button The button that was released.
     *               Usually one of:
     *               <br>
     *               <table>
     *               <caption>Standard Mouse Buttons</caption>
     *                   <tr>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_LEFT MOUSE_BUTTON_LEFT}</td>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_RIGHT MOUSE_BUTTON_RIGHT}</td>
     *                       <td>{@link GLFW#GLFW_MOUSE_BUTTON_MIDDLE MOUSE_BUTTON_MIDDLE}</td>
     *                   </tr>
     *               </table>
     * @param mods   The keyboard modifier flags set when the mouse button was released
     */
    default void mouseReleased(Window window, int button, KeyMods mods) {
    }

    /**
     * Called when the mouse wheel is scrolled.
     *
     * @param window The window that fired the event
     * @param dx     The motion in the x. This is for mice that can scroll horizontal as well as vertical
     * @param dy     The motion in the y. This is the standard "mouse wheel" motion
     */
    default void mouseScrolled(Window window, double dx, double dy) {
    }
}
