package io.github.ocelot.window;

import ca.weblite.objc.Client;
import ca.weblite.objc.NSObject;
import com.sun.jna.Pointer;
import io.github.ocelot.window.input.KeyMods;
import io.github.ocelot.window.input.KeyboardHandler;
import io.github.ocelot.window.input.MouseHandler;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWDropCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.NativeResource;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFWNativeCocoa.glfwGetCocoaWindow;

/**
 * <p>Manages and tracks certain key events for windows. Events are passed to all registered listeners.</p>
 * <p>In order to use graphics, create the capabilities for the desired API after calling {@link #create(CharSequence)}.</p>
 * <p>For example, to use LWJGL OpenGL use <code>org.lwjgl.opengl.GL.createCapabilities()</code></p>
 *
 * @author Ocelot
 * @see WindowEventListener
 */
public class Window implements NativeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);
    private static final int NS_FULL_SCREEN_WINDOW_MASK = 16384;

    private long handle;

    private final WindowManager windowManager;
    private final List<WindowEventListener> listeners;
    private CharSequence title;
    private int width;
    private int height;
    private int x;
    private int y;
    private int windowWidth;
    private int windowHeight;
    private int framebufferWidth;
    private int framebufferHeight;
    private boolean fullscreen;
    private int swapInterval;
    private boolean focused;
    private boolean closed;

    Window(WindowManager windowManager, int width, int height, boolean fullscreen) {
        this.windowManager = windowManager;
        this.listeners = new LinkedList<>();
        this.width = this.windowWidth = width;
        this.height = this.windowHeight = height;
        this.fullscreen = fullscreen;
        this.swapInterval = 0;
    }

    /**
     * Adds the specified listener to the event list.
     *
     * @param listener The listener to add
     */
    public void addListener(WindowEventListener listener) {
        this.listeners.add(listener);
        LOGGER.debug("Added listener: {}", listener.getClass().getName());
    }

    /**
     * Removes the specified listener to the event list.
     *
     * @param listener The listener to add
     */
    public void removeListener(WindowEventListener listener) {
        this.listeners.remove(listener);
        LOGGER.debug("Removed listener: {}", listener.getClass().getName());
    }

    /**
     * Creates this window with GLFW.
     *
     * @param title The title of the window
     */
    public Window create(CharSequence title) {
        return this.create(title, 0L);
    }

    /**
     * Creates this window with GLFW.
     *
     * @param title The title of the window
     * @param share The id off the window to share context with or <code>0L</code> to create a new context
     * @see <a href=https://www.glfw.org/docs/3.3/context_guide.html>GLFW Context Guide</a>
     */
    public Window create(CharSequence title, long share) {
        Monitor monitor = null;
        if (this.fullscreen) {
            monitor = this.windowManager.findBestMonitor(this);
            if (monitor != null) {
                VideoMode mode = monitor.getCurrentMode();
                this.windowWidth = mode.width();
                this.windowHeight = mode.height();
            }
        }

        this.title = title;
        this.handle = glfwCreateWindow(this.windowWidth, this.windowHeight, title, monitor != null ? monitor.getHandle() : 0L, share);
        if (this.handle == 0L) {
            throw new IllegalStateException("Failed to create window: " + title + ". " + WindowManager.getGLFWError());
        }

        // Center on the screen
        if (!this.fullscreen) {
            this.center();
        }

        // Focus
        this.focused = true;
        glfwRequestWindowAttention(this.handle);

        // Update framebuffer size
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            glfwGetFramebufferSize(this.handle, w, h);
            this.framebufferWidth = w.get();
            this.framebufferHeight = h.get();
        }

        glfwMakeContextCurrent(this.handle);

        LOGGER.debug("Initialized {}", this);

        glfwSetWindowCloseCallback(this.handle, window -> {
            this.closed = true;
            this.listeners.forEach(listener -> listener.windowClosed(this));
        });
        glfwSetWindowPosCallback(this.handle, (window, x, y) -> {
            this.x = x;
            this.y = y;
            this.listeners.forEach(listener -> listener.windowMoved(this, x, y));
        });
        glfwSetWindowSizeCallback(this.handle, (window, w, h) -> {
            this.width = this.windowWidth = w;
            this.height = this.windowHeight = h;
            this.listeners.forEach(listener -> listener.windowResized(this, w, h));
        });
        glfwSetFramebufferSizeCallback(this.handle, (window, w, h) -> {
            this.framebufferWidth = w;
            this.framebufferHeight = h;
            this.listeners.forEach(listener -> listener.framebufferResized(this, w, h));
        });
        glfwSetWindowFocusCallback(this.handle, (window, f) -> {
            this.focused = f;
            this.listeners.forEach(listener -> listener.focusChanged(this, f));
        });
        glfwSetDropCallback(this.handle, (window, count, names) -> {
            Path[] paths = new Path[count];
            for (int i = 0; i < paths.length; i++) {
                paths[i] = Paths.get(GLFWDropCallback.getName(names, i));
            }
            this.listeners.forEach(listener -> listener.filesDropped(this, paths));
        });
        glfwSetCharModsCallback(this.handle, (window, codepoint, mods) -> this.listeners.forEach(listener -> listener.charTyped(this, codepoint, new KeyMods(mods))));
        glfwSetKeyCallback(this.handle, (window, key, scancode, action, mods) -> {
            KeyMods keyMods = new KeyMods(mods);
            if (action == GLFW_PRESS) {
                this.listeners.forEach(listener -> listener.keyPressed(this, key, scancode, keyMods));
            } else if (action == GLFW_RELEASE) {
                this.listeners.forEach(listener -> listener.keyReleased(this, key, scancode, keyMods));
            } else if (action == GLFW_REPEAT) {
                this.listeners.forEach(listener -> listener.keyRepeated(this, key, scancode, keyMods));
            }
        });
        glfwSetCursorPosCallback(this.handle, (window, xpos, ypos) -> this.listeners.forEach(listener -> listener.mouseMoved(this, xpos, ypos)));
        glfwSetCursorEnterCallback(this.handle, (window, entered) -> this.listeners.forEach(listener -> listener.cursorEntered(this, entered)));
        glfwSetMouseButtonCallback(this.handle, (window, button, action, mods) -> {
            KeyMods keyMods = new KeyMods(mods);
            if (action == GLFW_PRESS) {
                this.listeners.forEach(listener -> listener.mousePressed(this, button, keyMods));
            } else if (action == GLFW_RELEASE) {
                this.listeners.forEach(listener -> listener.mouseReleased(this, button, keyMods));
            }
        });
        glfwSetScrollCallback(this.handle, (window, xoffset, yoffset) -> this.listeners.forEach(listener -> listener.mouseScrolled(this, xoffset, yoffset)));

        return this;
    }

    /**
     * Creates the default implementation of mouse tracking and automatically adds it to the listeners.
     *
     * @return A new mouse handler that tracks events for this window
     */
    public MouseHandler createMouseHandler() {
        MouseHandler mouseHandler = new MouseHandler(this);
        this.addListener(mouseHandler);
        return mouseHandler;
    }

    /**
     * Creates the default implementation of key tracking and automatically adds it to the listeners.
     *
     * @return A new keyboard handler that tracks events for this window
     */
    public KeyboardHandler createKeyboardHandler() {
        KeyboardHandler keyboardHandler = new KeyboardHandler();
        this.addListener(keyboardHandler);
        return keyboardHandler;
    }

    /**
     * Updates the contents of the window. Called by {@link WindowManager#update()} automatically.
     */
    public void swapBuffers() {
        glfwSwapInterval(this.swapInterval);
        glfwSwapBuffers(this.handle);
    }

    /**
     * Toggles the fullscreen flag.
     */
    public void toggleFullscreen() {
        this.setFullscreen(!this.fullscreen);
    }

    @Override
    public void free() {
        if (this.handle != 0) {
            glfwFreeCallbacks(this.handle);
            glfwDestroyWindow(this.handle);
        }
        this.handle = 0;
        this.closed = true;
        this.windowManager.removeWindow(this);
    }

    /**
     * @return The GLFW id of the window
     */
    public long getHandle() {
        return this.handle;
    }

    /**
     * @return The absolute x position of the window
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return The absolute y position of the window
     */
    public int getY() {
        return this.y;
    }

    /**
     * @return The width of the physical window. {@link #getFramebufferWidth()} should be used for drawing logic
     */
    public int getWindowWidth() {
        return this.windowWidth;
    }

    /**
     * @return The width of the physical window. {@link #getFramebufferHeight()} should be used for drawing logic
     */
    public int getWindowHeight() {
        return this.windowHeight;
    }

    /**
     * @return The width of the canvas in the window
     */
    public int getFramebufferWidth() {
        return this.framebufferWidth;
    }

    /**
     * @return The height of the canvas in the window
     */
    public int getFramebufferHeight() {
        return this.framebufferHeight;
    }

    /**
     * @return Whether the window is currently full screen
     */
    public boolean isFullscreen() {
        return this.fullscreen;
    }

    /**
     * @return The number of monitor frames to wait before continuing execution when {@link #swapBuffers()} is called
     */
    public int getSwapInterval() {
        return this.swapInterval;
    }

    /**
     * @return Whether vsync is enabled
     */
    public boolean isVsync() {
        return this.swapInterval > 0;
    }

    /**
     * @return The title of the window or <code>null</code> if the window has not been initialized yet
     */
    public @Nullable CharSequence getTitle() {
        return this.title;
    }

    /**
     * @return If the window is currently focused
     */
    public boolean isFocused() {
        return this.focused;
    }

    /**
     * @return If the window is requesting to close
     */
    public boolean isClosed() {
        return this.closed;
    }

    /**
     * @return The current string on the clipboard or <code>null</code> if there is nothing
     */
    public @Nullable String getClipboard() {
        return glfwGetClipboardString(this.handle);
    }

    /**
     * Sets the window fullscreen or not. Will automatically handle special mac handling.
     *
     * @param fullscreen Whether to enter fullscreen
     */
    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
        if (this.handle == 0L) { // will be updated later
            return;
        }

        try {
            Monitor monitor = this.windowManager.findBestMonitor(this);
            if (monitor == null) {
                this.fullscreen = false;
                return;
            }
            if (Platform.get() == Platform.MACOSX) {
                getNsWindow(this.handle).filter(Window::isInKioskMode).ifPresent(Window::toggleMacFullscreen);
            }

            VideoMode mode = monitor.getCurrentMode();
            if (this.fullscreen) {
                int w = this.width;
                int h = this.height;
                glfwSetWindowMonitor(this.handle, monitor.getHandle(), 0, 0, mode.width(), mode.height(), mode.refreshRate());
                this.width = w;
                this.height = h;
            } else {
                glfwSetWindowMonitor(this.handle, 0L, monitor.getX() + (mode.width() - this.width) / 2, monitor.getY() + (mode.height() - this.height) / 2, this.width, this.height, GLFW_DONT_CARE);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to " + (fullscreen ? "enter" : "exit") + " fullscreen", e);
        }
    }

    /**
     * Sets the number of monitor frames to wait before continuing execution when {@link #swapBuffers()} is called.
     *
     * @param swapInterval The frame count
     */
    public void setSwapInterval(int swapInterval) {
        this.swapInterval = Math.max(0, swapInterval);
    }

    /**
     * Sets the window to use vsync.
     *
     * @param vsync Whether vsync should be enabled
     */
    public void setVsync(boolean vsync) {
        this.swapInterval = vsync ? 1 : 0;
    }

    /**
     * Updates the window title to the specified string.
     *
     * @param title The new window title
     */
    public void setTitle(CharSequence title) {
        if (this.handle != 0L) {
            glfwSetWindowTitle(this.handle, title);
            this.title = title;
        }
    }

    /**
     * Centers this window on the current monitor.
     */
    public void center() {
        Monitor monitor = this.windowManager.findBestMonitor(this);
        if (monitor != null) {
            VideoMode mode = monitor.getCurrentMode();
            this.setPosition(monitor.getX() + (mode.width() - this.windowWidth) / 2, monitor.getY() + (mode.height() - this.windowHeight) / 2);
        }
    }

    /**
     * Sets the absolute position of the window.
     *
     * @param x The new x position
     * @param y The new y position
     */
    public void setPosition(int x, int y) {
        if (this.handle != 0L) {
            glfwSetWindowPos(this.handle, x, y);
        }
    }

    /**
     * Sets the size of the window.
     *
     * @param width  The new x size
     * @param height The new y size
     */
    public void setSize(int width, int height) {
        if (this.handle != 0L) {
            glfwSetWindowSize(this.handle, width, height);
        }
    }

    /**
     * Marks the window as closing or not.
     *
     * @param closing Whether the window should close
     */
    public void setClosing(boolean closing) {
        if (this.handle != 0L) {
            this.closed = closing;
            glfwSetWindowShouldClose(this.handle, closing);
        }
    }

    @Override
    public String toString() {
        return String.format("Window[%s %s,%s %sx%s]", this.title != null ? this.title : this.handle, this.x, this.y, this.windowWidth, this.windowHeight);
    }

    /**
     * Reads the icon from the stream and sends it to the OS to set the mac icon. This should only be called on a mac.
     *
     * @param stream The image file stream
     * @throws IOException If any error occurs reading the image
     */
    public static void setMacIcon(InputStream stream) throws IOException {
        String icon = Base64.getEncoder().encodeToString(stream.readAllBytes());
        Client client = Client.getInstance();
        Object nsData = client.sendProxy("NSData", "alloc").send("initWithBase64Encoding:", icon);
        Object nsImage = client.sendProxy("NSImage", "alloc").send("initWithData:", nsData);
        client.sendProxy("NSApplication", "sharedApplication").send("setApplicationIconImage:", nsImage);
    }

    private static Optional<NSObject> getNsWindow(long handle) {
        long cocoaWindow = glfwGetCocoaWindow(handle);
        return cocoaWindow != 0L ? Optional.of(new NSObject(new Pointer(cocoaWindow))) : Optional.empty();
    }

    private static boolean isInKioskMode(NSObject nSObject) {
        return (nSObject.sendInt("styleMask") & NS_FULL_SCREEN_WINDOW_MASK) > 0;
    }

    private static void toggleMacFullscreen(NSObject nSObject) {
        nSObject.send("toggleFullScreen:", Pointer.NULL);
    }
}
