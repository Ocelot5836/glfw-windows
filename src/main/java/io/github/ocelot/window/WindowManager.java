package io.github.ocelot.window;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWMonitorCallback;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.NativeResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Manages monitors and all windows created.
 *
 * @author Ocelot
 */
public class WindowManager implements NativeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowManager.class);

    private final Map<Long, Monitor> monitors;
    private final Set<Window> windows;

    public WindowManager() {
        this.monitors = new HashMap<>();
        this.windows = new HashSet<>();

        // Initialize GLFW
        String preError = getGLFWError();
        if (preError != null) {
            throw new IllegalStateException("GLFW error before init: " + preError);
        }
        GLFWErrorCallback old = glfwSetErrorCallback((error, description) -> LOGGER.error(String.format("GLFW error during init: [0x%X]%s", error, MemoryUtil.memUTF8(description))));
        if (!glfwInit()) {
            glfwTerminate();
            throw new RuntimeException("Failed to initialize GLFW.");
        }
        GLFWErrorCallback o = glfwSetErrorCallback(old);
        if (o != null) {
            o.free();
        }

        GLFWMonitorCallback callback = glfwSetMonitorCallback((monitor, event) -> {
            if (event == GLFW_CONNECTED) {
                Monitor m = new Monitor(monitor);
                this.monitors.put(monitor, m);
                LOGGER.debug("Monitor {} connected", m);
            } else if (event == GLFW_DISCONNECTED) {
                Monitor m = this.monitors.remove(monitor);
                LOGGER.debug("Monitor {} disconnected", m);
            }
        });
        if (callback != null) {
            callback.free();
        }

        PointerBuffer monitors = glfwGetMonitors();
        if (monitors != null) {
            for (int i = 0; i < monitors.limit(); ++i) {
                long handle = monitors.get(i);
                this.monitors.put(handle, new Monitor(handle));
            }
        }
    }

    /**
     * @return The current GLFW error or <code>null</code> if there currently isn't one
     */
    public static @Nullable String getGLFWError() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pointer = stack.mallocPointer(1);
            int error = glfwGetError(pointer);
            if (error == GLFW_NO_ERROR) {
                return null;
            }
            return "[0x%X] %s".formatted(error, MemoryUtil.memUTF8(pointer.get(0)));
        }
    }

    /**
     * Polls window events and updates all windows.
     */
    public void update() {
        glfwPollEvents();
        this.windows.forEach(Window::swapBuffers);
        glfwPollEvents();
    }

    /**
     * Creates a new window without initializing it. {@link Window#create(CharSequence)} must be called for it to be valid.
     *
     * @param width      The width of the window
     * @param height     The height of the window
     * @param fullscreen Whether to initialize in full screen or not
     * @return The window created
     */
    public Window create(int width, int height, boolean fullscreen) {
        Window window = new Window(this, width, height, fullscreen);
        LOGGER.debug("Created {}", window);
        this.windows.add(window);
        return window;
    }

    /**
     * Creates a new window and initializes it.
     *
     * @param title      The title of the window
     * @param width      The width of the window
     * @param height     The height of the window
     * @param fullscreen Whether to initialize in fullscreen or not
     * @return The window created
     */
    public Window create(CharSequence title, int width, int height, boolean fullscreen) {
        return this.create(width, height, fullscreen).create(title);
    }

    /**
     * Creates a new window and initializes it.
     *
     * @param title      The title of the window
     * @param width      The width of the window
     * @param height     The height of the window
     * @param fullscreen Whether to initialize in fullscreen or not
     * @param share      The id of the window to share context with
     * @return The window created
     * @see <a href=https://www.glfw.org/docs/3.3/context_guide.html>GLFW Context Guide</a>
     */
    public Window create(CharSequence title, int width, int height, boolean fullscreen, long share) {
        return this.create(width, height, fullscreen).create(title, share);
    }

    @ApiStatus.Internal
    void removeWindow(Window window) {
        this.windows.remove(window);
    }

    /**
     * Retrieves the monitor with the specified id.
     *
     * @param handle The handle of the monitor
     * @return The monitor with that id or <code>null</code> if no monitor could be found
     */
    public @Nullable Monitor getMonitor(long handle) {
        return this.monitors.get(handle);
    }

    /**
     * Finds the best monitor to use for fullscreen based on how much each monitor covers the window.
     *
     * @param window The window to test
     * @return The monitor the window best fits on or <code>null</code> if no monitor could be found
     */
    public @Nullable Monitor findBestMonitor(Window window) {
        long windowMonitor = window.getHandle() != 0L ? glfwGetWindowMonitor(window.getHandle()) : 0L;
        if (windowMonitor != 0L) {
            return this.getMonitor(windowMonitor);
        }

        int windowMinX = window.getX();
        int windowMaxX = windowMinX + window.getWindowWidth();
        int windowMinY = window.getY();
        int windowMaxY = windowMinY + window.getWindowHeight();
        int bestArea = -1;
        Monitor bestMonitor = null;
        long primaryMonitor = glfwGetPrimaryMonitor();

        for (Monitor monitor : this.monitors.values()) {
            int minX = monitor.getX();
            int maxX = minX + monitor.getCurrentMode().width();
            int minY = monitor.getY();
            int maxY = minY + monitor.getCurrentMode().height();
            int t = clamp(windowMinX, minX, maxX);
            int u = clamp(windowMaxX, minX, maxX);
            int v = clamp(windowMinY, minY, maxY);
            int w = clamp(windowMaxY, minY, maxY);
            int xArea = Math.max(0, u - t);
            int yArea = Math.max(0, w - v);
            int area = xArea * yArea;

            // If the new monitor has the most window area, then try to use that one
            if (area > bestArea) {
                bestMonitor = monitor;
                bestArea = area;
            } else if (area == bestArea && primaryMonitor == monitor.getHandle()) {
                bestMonitor = monitor;
            }
        }

        return bestMonitor;
    }

    private static int clamp(int value, int min, int max) {
        return value < min ? min : Math.min(value, max);
    }

    @Override
    public void free() {
        GLFWMonitorCallback callback = glfwSetMonitorCallback(null);
        if (callback != null) {
            callback.free();
        }
        Set.copyOf(this.windows).forEach(Window::free);
        glfwTerminate();
    }
}
