import io.github.ocelot.window.Window;
import io.github.ocelot.window.WindowEventListener;
import io.github.ocelot.window.WindowManager;
import io.github.ocelot.window.input.KeyMods;
import io.github.ocelot.window.input.MouseHandler;
import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.GL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.lwjgl.glfw.GLFW.*;

public class WindowTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WindowTest.class);

    @Test
    public void window() {
        try (WindowManager windowManager = new WindowManager()) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);

            Window test = windowManager.create("Test", 800, 600, false);
            test.addListener(new DefaultListener());

            while (!test.isClosed()) {
                windowManager.update();
            }

            LOGGER.info("Closing");
        }
    }

    @Test
    public void fullscreen() {
        try (WindowManager windowManager = new WindowManager()) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

            Window test = windowManager.create("Test", 800, 600, true);
            GL.createCapabilities();

            test.addListener(new DefaultListener());

            while (!test.isClosed()) {
                windowManager.update();
            }

            LOGGER.info("Closing");
        }
    }

    @Test
    public void mouse() {
        try (WindowManager windowManager = new WindowManager()) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);

            Window test = windowManager.create("Test", 800, 600, false);

            MouseHandler mouseHandler = test.createMouseHandler();
            test.addListener(new WindowEventListener() {
                @Override
                public void mousePressed(Window window, int button, KeyMods mods) {
                    if (button == 1) {
                        if (mouseHandler.isMouseGrabbed()) {
                            mouseHandler.releaseMouse();
                        } else {
                            mouseHandler.grabMouse();
                        }
                    }
                }
            });
            test.addListener(new DefaultListener());

            while (!test.isClosed()) {
                windowManager.update();

                double dx = mouseHandler.getAccumulatedDX();
                double dy = mouseHandler.getAccumulatedDY();
                if (dx != 0 || dy != 0)
                    LOGGER.info(dx + ", " + dy);
            }

            LOGGER.info("Closing");
        }
    }

    @Test
    public void share() {
        try (WindowManager windowManager = new WindowManager()) {
            glfwDefaultWindowHints();
            glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
            glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
            glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

            Window test1 = windowManager.create("Test 1", 800, 600, false);
            GL.createCapabilities();

            Window test2 = windowManager.create("Test 2", 800, 600, false, test1.getHandle());
            GL.createCapabilities();

            glfwMakeContextCurrent(test1.getHandle());

            test1.addListener(new DefaultListener());
            test2.addListener(new DefaultListener());

            while (!test1.isClosed()) {
                windowManager.update();
                if (test1.isClosed())
                    test1.free();
                if (test2.isClosed())
                    test2.free();
            }

            LOGGER.info("Closing");
        }
    }

    private static class DefaultListener implements WindowEventListener {
        @Override
        public void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
            if (key == GLFW_KEY_T)
                window.center();
            if (key == GLFW_KEY_ESCAPE)
                window.setClosing(true);
        }

        @Override
        public void windowClosed(Window window) {
            LOGGER.info(window + " requested close");
        }
    }
}
