package minecraft_clone.engine;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

public class InputManager {
    private static double lastX = 0;
    private static double lastY = 0;
    private static boolean firstMouse = true;
    public static float totalDeltaX = 0;
    public static float totalDeltaY = 0;
    private static float smoothedDeltaX = 0;
    private static float smoothedDeltaY = 0;
    private static final float SMOOTHING_FACTOR = 0.3f;

    private static long window;
    private static GLFWCursorPosCallback cursorCallback;
    private static GLFWKeyCallback keyCallback;
    private float speed = 5.0f;       // Movement speed (units per second)
    private float sensitivity = 0.1f; // Mouse sensitivity (degrees per pixel)

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

                totalDeltaX += (float) (xPos - lastX);
                totalDeltaY += (float) (lastY - yPos);
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

    public static boolean isKeyPressed(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public void resetMouseDelta() {
        totalDeltaX = 0;
        totalDeltaY = 0;
    }

    public void freeInputCallbacks() {
        cursorCallback.free();
        keyCallback.free();
    }

    public void updateSmoothedDeltas() {
        smoothedDeltaX = smoothedDeltaX * SMOOTHING_FACTOR + totalDeltaX * (1.0f - SMOOTHING_FACTOR);
        smoothedDeltaY = smoothedDeltaY * SMOOTHING_FACTOR + totalDeltaY * (1.0f - SMOOTHING_FACTOR);
    }

    public float getSensitivity() {
        return sensitivity;
    }

    public float getSmoothedDeltaX() {
        return smoothedDeltaX;
    }

    public float getSmoothedDeltaY() {
        return smoothedDeltaY;
    }

    public float getSpeed() {
        return speed;
    }
}
