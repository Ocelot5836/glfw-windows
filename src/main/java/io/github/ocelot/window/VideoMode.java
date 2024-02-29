package io.github.ocelot.window;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.glfw.GLFWVidMode.Buffer;

public record VideoMode(int width, int height, int redBits, int greenBits, int blueBits, int refreshRate) {

    public VideoMode(Buffer buffer) {
        this(buffer.width(), buffer.height(), buffer.redBits(), buffer.greenBits(), buffer.blueBits(), buffer.refreshRate());
    }

    public VideoMode(GLFWVidMode vidMode) {
        this(vidMode.width(), vidMode.height(), vidMode.redBits(), vidMode.greenBits(), vidMode.blueBits(), vidMode.refreshRate());
    }
}
