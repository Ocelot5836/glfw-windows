package io.github.ocelot.window.input;

import io.github.ocelot.window.Window;
import io.github.ocelot.window.WindowEventListener;

import java.util.BitSet;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Tracks the mouse position, velocity, and pressed buttons. Also supports tracking the mouse while grabbed.
 *
 * @author Ocelot
 */
public class MouseHandler implements WindowEventListener {

    private final Window window;
    private final BitSet mouseButtons;
    private double mouseX;
    private double mouseY;
    private double mouseDX;
    private double mouseDY;
    private double accumulatedDX;
    private double accumulatedDY;
    private boolean mouseGrabbed;
    private boolean ignoreFirstMovement;

    public MouseHandler(Window window) {
        this.window = window;
        this.mouseButtons = new BitSet(GLFW_MOUSE_BUTTON_LAST + 1);
    }

    @Override
    public void mouseMoved(Window window, double x, double y) {
        this.mouseDX = x - this.mouseX;
        this.mouseDY = y - this.mouseY;
        this.mouseX = x;
        this.mouseY = y;

        // Only add to mouse motion if the mouse is grabbed
        if (this.mouseGrabbed) {
            this.accumulatedDX += this.mouseDX;
            this.accumulatedDY += this.mouseDY;
        }

        if (this.ignoreFirstMovement) {
            this.mouseDX = 0;
            this.mouseDY = 0;
            this.accumulatedDX = 0;
            this.accumulatedDY = 0;
            this.ignoreFirstMovement = false;
        }
    }

    @Override
    public void mousePressed(Window window, int button, KeyMods mods) {
        this.mouseButtons.set(button, true);
    }

    @Override
    public void mouseReleased(Window window, int button, KeyMods mods) {
        this.mouseButtons.set(button, false);
    }

    @Override
    public void cursorEntered(Window window, boolean entered) {
        if (entered) {
            this.ignoreFirstMovement = true;
        }
    }

    /**
     * Grabs the mouse by disabling it and moving it to the center of the window.
     */
    public void grabMouse() {
        if (!this.window.isFocused() || this.mouseGrabbed) {
            return;
        }
        this.mouseGrabbed = true;
        this.mouseX = this.window.getWindowWidth() / 2.0;
        this.mouseY = this.window.getWindowHeight() / 2.0;
        glfwSetCursorPos(this.window.getHandle(), this.mouseX, this.mouseY);
        glfwSetInputMode(this.window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        this.ignoreFirstMovement = true;
        this.accumulatedDX = 0;
        this.accumulatedDY = 0;
    }

    /**
     * Releases the mouse and sets its position to the center of the window.
     */
    public void releaseMouse() {
        if (!this.mouseGrabbed) {
            return;
        }
        this.mouseGrabbed = false;
        this.mouseX = this.window.getWindowWidth() / 2.0;
        this.mouseY = this.window.getWindowHeight() / 2.0;
        glfwSetCursorPos(this.window.getHandle(), this.mouseX, this.mouseY);
        glfwSetInputMode(this.window.getHandle(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    /**
     * Ignores the first movement velocity of the mouse.
     */
    public void ignoreFirstMovement() {
        this.ignoreFirstMovement = true;
    }

    /**
     * Checks if the specified mouse button is pressed.
     *
     * @param button The button to check
     * @return Whether that button is pressed
     */
    public boolean isButtonPressed(int button) {
        return button >= 0 && button <= GLFW_MOUSE_BUTTON_LAST && this.mouseButtons.get(button);
    }

    /**
     * @return The x position of the mouse
     */
    public double getMouseX() {
        return mouseX;
    }

    /**
     * @return The y position of the mouse
     */
    public double getMouseY() {
        return mouseY;
    }

    /**
     * @return The x velocity of the mouse
     */
    public double getMouseDX() {
        return mouseDX;
    }

    /**
     * @return The y velocity of the mouse
     */
    public double getMouseDY() {
        return mouseDY;
    }

    /**
     * @return Whether the mouse is disabled and not currently visible
     */
    public boolean isMouseGrabbed() {
        return mouseGrabbed;
    }

    /**
     * @return The total x motion of the mouse since the last query if grabbed
     */
    public double getAccumulatedDX() {
        double value = this.accumulatedDX;
        this.accumulatedDX = 0;
        return value;
    }

    /**
     * @return The total y motion of the mouse since the last query if grabbed
     */
    public double getAccumulatedDY() {
        double value = this.accumulatedDY;
        this.accumulatedDY = 0;
        return value;
    }
}
