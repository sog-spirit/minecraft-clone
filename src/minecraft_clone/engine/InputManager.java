package minecraft_clone.engine;

import static org.lwjgl.glfw.GLFW.*;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import minecraft_clone.entity.Camera;

public class InputManager {
    private static double lastX = 0;
    private static double lastY = 0;
    private static boolean firstMouse = true;
    public static float deltaX = 0;
    public static float deltaY = 0;
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

    public void updateCamera(Camera camera, float deltaTime) {
        float cappedDeltaTime = Math.min(deltaTime, 0.05f);

        smoothedDeltaX = smoothedDeltaX * SMOOTHING_FACTOR + deltaX * (1.0f - SMOOTHING_FACTOR);
        smoothedDeltaY = smoothedDeltaY * SMOOTHING_FACTOR + deltaY * (1.0f - SMOOTHING_FACTOR);
        // Calculate movement vector based on keyboard input
        Vector3f movement = new Vector3f();
        if (isKeyPressed(GLFW.GLFW_KEY_W)) {
            movement.add(camera.getForward()); // Move forward
        }
        if (isKeyPressed(GLFW.GLFW_KEY_S)) {
            movement.add(camera.getForward().mul(-1)); // Move backward
        }
        if (isKeyPressed(GLFW.GLFW_KEY_A)) {
            movement.add(camera.getRight()); // Move left
        }
        if (isKeyPressed(GLFW.GLFW_KEY_D)) {
            movement.add(camera.getRight().mul(-1)); // Move right
        }

        // If there’s any movement, normalize and scale by speed and deltaTime
        if (movement.lengthSquared() > 0) {
            movement.normalize().mul(speed * deltaTime);
            camera.move(movement);
        }

        // Calculate rotation based on mouse movement
        float rotationSpeed = sensitivity * 60.0f; // Normalize to 60 FPS base
        camera.rotate(smoothedDeltaX * rotationSpeed * cappedDeltaTime, smoothedDeltaY * rotationSpeed * cappedDeltaTime);
        resetMouseDelta(); // Reset deltas after applying rotation
    }

    public static boolean isKeyPressed(int key) {
        return glfwGetKey(window, key) == GLFW_PRESS;
    }

    public void resetMouseDelta() {
        deltaX = 0;
        deltaY = 0;
    }

    public void freeInputCallbacks() {
        cursorCallback.free();
        keyCallback.free();
    }
}
