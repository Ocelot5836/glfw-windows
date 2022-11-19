package io.github.ocelot.window;

import io.github.ocelot.window.input.KeyMods;

import java.nio.file.Path;

/**
 * Listens to events on a {@link Window}.
 *
 * @author Ocelot
 */
public interface WindowEventListener {

    default void windowClosed(Window window) {
    }

    default void windowMoved(Window window, int x, int y) {
    }

    default void windowResized(Window window, int width, int height) {
    }

    default void framebufferResized(Window window, int width, int height) {
    }

    default void focusChanged(Window window, boolean focused) {
    }

    default void filesDropped(Window window, Path... files) {
    }

    default void charTyped(Window window, int codePoint, KeyMods mods) {
    }

    default void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
    }

    default void keyReleased(Window window, int key, int scanCode, KeyMods mods) {
    }

    default void keyRepeated(Window window, int key, int scanCode, KeyMods mods) {
    }

    default void mouseMoved(Window window, double x, double y) {
    }

    default void cursorEntered(Window window, boolean entered) {
    }

    default void mousePressed(Window window, int button, KeyMods mods) {
    }

    default void mouseReleased(Window window, int button, KeyMods mods) {
    }

    /**
     * @param dx The motion in the x. This is for mice that can scroll horizontal as well as vertical
     * @param dy The motion in the y. This is the standard "mouse wheel" motion
     */
    default void mouseScrolled(Window window, double dx, double dy) {
    }
}
