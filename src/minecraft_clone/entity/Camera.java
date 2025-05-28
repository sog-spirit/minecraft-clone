package minecraft_clone.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import minecraft_clone.engine.InputManager;

public class Camera {
    private Vector3f position; // Camera position in 3D space
    private float yaw;         // Rotation around the vertical axis (in degrees)
    private float pitch;       // Rotation around the horizontal axis (in degrees)

    // Constructor initializes the camera at origin with no rotation
    public Camera() {
        position = new Vector3f(0, 0, 0);
        yaw = 270;
        pitch = 0;
    }

    public void update(float deltaTime, InputManager inputManager) {
        float cappedDeltaTime = Math.min(deltaTime, 0.05f);

        inputManager.updateSmoothedDeltas();
        Vector3f movement = new Vector3f();
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_W)) {
            movement.add(getForward()); // Move forward
        }
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_S)) {
            movement.add(getForward().mul(-1)); // Move backward
        }
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_A)) {
            movement.add(getRight()); // Move left
        }
        if (InputManager.isKeyPressed(GLFW.GLFW_KEY_D)) {
            movement.add(getRight().mul(-1)); // Move right
        }

        if (movement.lengthSquared() > 0) {
            movement.normalize().mul(inputManager.getSpeed() * deltaTime);
            move(movement);
        }

        float rotationSpeed = inputManager.getSensitivity() * 60.0f;
        rotate(inputManager.getSmoothedDeltaX() * rotationSpeed * cappedDeltaTime, inputManager.getSmoothedDeltaY() * rotationSpeed * cappedDeltaTime);
        inputManager.resetMouseDelta();
    }

    // Move the camera by adding a displacement vector to its position
    public void move(Vector3f displacement) {
        position.add(displacement);
    }

    // Rotate the camera by adjusting yaw and pitch, with pitch clamped to prevent flipping
    public void rotate(float deltaYaw, float deltaPitch) {
        yaw += deltaYaw;
        pitch += deltaPitch;
        // Clamp pitch to avoid camera flipping (between -89 and 89 degrees)
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;
    }

    // Compute the forward vector based on yaw and pitch
    public Vector3f getForward() {
        float cosPitch = (float) Math.cos(Math.toRadians(pitch));
        float sinPitch = (float) Math.sin(Math.toRadians(pitch));
        float cosYaw = (float) Math.cos(Math.toRadians(yaw));
        float sinYaw = (float) Math.sin(Math.toRadians(yaw));
        return new Vector3f(
            cosYaw * cosPitch,  // x component
            sinPitch,           // y component
            sinYaw * cosPitch   // z component
        ).normalize();
    }

    // Compute the right vector as the cross product of world up and forward
    public Vector3f getRight() {
        Vector3f forward = getForward();
        Vector3f worldUp = new Vector3f(0, 1, 0); // Assuming world up is +Y
        Vector3f right = new Vector3f();
        worldUp.cross(forward, right); // worldUp x forward gives right vector
        return right.normalize();
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Matrix4f getViewMatrix() {
        Vector3f forward = getForward();
        Vector3f target = new Vector3f(position).add(forward);
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Matrix4f getProjectionMatrix(int width, int height) {
        return new Matrix4f().perspective((float) Math.toRadians(70.0f), (float) width / height, .1f, 1000f);
    }
}
