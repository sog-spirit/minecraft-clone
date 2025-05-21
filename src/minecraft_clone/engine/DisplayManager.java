package minecraft_clone.engine;

import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class DisplayManager {
    private static long window;
    private static int width;
    private static int height;

    public void createDisplay(String title, int width, int height) {
        DisplayManager.width = width;
        DisplayManager.height = height;
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        window = glfwCreateWindow(width, height, title, 0, 0);
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        GL.createCapabilities();
        glEnable(GL_DEPTH_TEST);
    }

    public void updateDisplay() {
        glfwSwapBuffers(window);
    }

    public void clearDisplay() {
        glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void closeDisplay() {
        glfwDestroyWindow(window);
        GL.setCapabilities(null);
        glfwTerminate();
    }

    public long getWindow() {
        return window;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
