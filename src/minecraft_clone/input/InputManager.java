package minecraft_clone.input;

import static org.lwjgl.glfw.GLFW.*;

public class InputManager {
    private static double lastX = 400;
    private static double lastY = 300;
    private static boolean firstMouse = true;
    public static float deltaX = 0;
    public static float deltaY = 0;

    private static long window;

    public static void setupCallbacks(long win) {
        window = win;

        glfwSetCursorPosCallback(window, (w, xPos, yPos) -> {
            if (firstMouse) {
                lastX = xPos;
                lastY = yPos;
                firstMouse = false;
            }

            deltaX = (float) (xPos - lastX);
            deltaY = (float) (lastY - yPos);
            lastX = xPos;
            lastY = yPos;
        });

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public static boolean isKeyDown(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public static void resetDeltas() {
        deltaX = 0;
        deltaY = 0;
    }
}
