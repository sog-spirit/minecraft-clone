package minecraft_clone.engine;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class InputManager {
    private static double lastX = 400;
    private static double lastY = 300;
    private static boolean firstMouse = true;
    public static float deltaX = 0;
    public static float deltaY = 0;

    private static long window;
    private static GLFWCursorPosCallback cursorCallback;
    private static GLFWKeyCallback keyCallback;

    public void setupCallbacks(long win) {
        window = win;

        cursorCallback = new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xPos, double yPos) {
                if (firstMouse) {
                    lastX = xPos;
                    lastY = yPos;
                    firstMouse = false;
                }

                deltaX = (float) (xPos - lastX);
                deltaY = (float) (lastY - yPos);
                lastX = xPos;
                lastY = yPos;
            }
        };
        glfwSetCursorPosCallback(win, cursorCallback);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);

        keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                    glfwSetWindowShouldClose(window, true);
                }
            }
        };
        glfwSetKeyCallback(win, keyCallback);
    }

    public static boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public void resetDeltas() {
        deltaX = 0;
        deltaY = 0;
    }

    public void freeInputCallbacks() {
        cursorCallback.free();
        keyCallback.free();
    }
}
