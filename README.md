# GLFW Windows

Java wrapper for the LWJGL GLFW api. This makes it straightforward to set up and manage multiple windows with
customizable rendering APIs.

# OpenGL Example

```java
import io.github.ocelot.window.Window;
import io.github.ocelot.window.WindowEventListener;
import io.github.ocelot.window.WindowManager;
import io.github.ocelot.window.input.KeyMods;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;

public class ExampleApp implements WindowEventListener {

    private final WindowManager windowManager;
    private final Window window;

    public ExampleApp() {
        this.windowManager = new WindowManager();
        this.window = this.windowManager.create(800, 600, false);
        this.window.addListener(this);
    }

    private void init() {
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API);
        glfwWindowHint(GLFW_CONTEXT_CREATION_API, GLFW_NATIVE_CONTEXT_API);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

        this.window.create("Test 1");
        GL.createCapabilities();
        glfwMakeContextCurrent(this.window.getHandle());
    }

    public void run() {
        this.init();

        while (!this.window.isClosed()) {
            this.windowManager.update();
        }

        this.windowManager.free();
    }

    @Override
    public void keyPressed(Window window, int key, int scanCode, KeyMods mods) {
        if (key == GLFW_KEY_F && mods.shift()) {
            System.out.println("Shift+F pressed");
        } else if (key == GLFW_KEY_ESCAPE) {
            this.window.close();
        }
    }

    public static void main(String[] args) {
        new ExampleApp().run();
    }
}
```