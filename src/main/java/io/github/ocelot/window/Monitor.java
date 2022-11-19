package io.github.ocelot.window;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.ArrayList;
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
    private VideoMode currentMode;
    private int x;
    private int y;

    public Monitor(long handle) {
        this.handle = handle;
        this.videoModes = new ArrayList<>();
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
                if (videoMode.redBits() >= 8 && videoMode.greenBits() >= 8 && videoMode.blueBits() >= 8)
                    this.videoModes.add(videoMode);
            }
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer x = stack.mallocInt(1);
            IntBuffer y = stack.mallocInt(1);
            glfwGetMonitorPos(this.handle, x, y);
            this.x = x.get();
            this.y = y.get();
        }

        this.currentMode = new VideoMode(Objects.requireNonNull(glfwGetVideoMode(this.handle)));
    }

    public VideoMode getCurrentMode() {
        return this.currentMode;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    /**
     * @return A list of all possible video modes for this monitor
     */
    public List<VideoMode> getVideoModes() {
        return List.copyOf(this.videoModes);
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
