package io.github.ocelot.window;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;

/**
 * A monitor detected by GLFW that can be used in window creation.
 *
 * @author Ocelot
 */
public class Monitor {

    private final long handle;
    private final List<VideoMode> videoModes;
    private final List<VideoMode> videoModesView;
    private VideoMode currentMode;
    private int x;
    private int y;

    public Monitor(long handle) {
        this.handle = handle;
        this.videoModes = new ArrayList<>();
        this.videoModesView = Collections.unmodifiableList(this.videoModes);
        this.refreshVideoModes();
    }

    /**
     * Queries GLFW for the position and valid video modes of this monitor.
     */
    public void refreshVideoModes() {
        this.videoModes.clear();
        GLFWVidMode.Buffer buffer = glfwGetVideoModes(this.handle);

        if (buffer != null) {
            for (int i = buffer.limit() - 1; i >= 0; i--) {
                buffer.position(i);
                VideoMode videoMode = new VideoMode(buffer);
                if (videoMode.redBits() >= 8 && videoMode.greenBits() >= 8 && videoMode.blueBits() >= 8) {
                    this.videoModes.add(videoMode);
                }
            }
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetMonitorPos(this.handle, x, y);
            this.x = x.get(0);
            this.y = y.get(0);
        }

        this.currentMode = new VideoMode(Objects.requireNonNull(glfwGetVideoMode(this.handle)));
    }

    /**
     * @return The current video mode of the monitor
     */
    public VideoMode getCurrentMode() {
        return this.currentMode;
    }

    /**
     * @return The absolute x position of this monitor
     */
    public int getX() {
        return this.x;
    }

    /**
     * @return The absolute y position of this monitor
     */
    public int getY() {
        return this.y;
    }

    /**
     * @return A view of all possible video modes for this monitor
     */
    public List<VideoMode> getVideoModes() {
        return this.videoModesView;
    }

    /**
     * @return The GLFW id for this monitor
     */
    public long getHandle() {
        return this.handle;
    }

    @Override
    public String toString() {
        return String.format("Monitor[%s %s,%s %s]", this.handle, this.x, this.y, this.currentMode);
    }
}
