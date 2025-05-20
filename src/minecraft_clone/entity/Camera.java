package minecraft_clone.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import minecraft_clone.engine.InputManager;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private final Vector3f position = new Vector3f(0, 0, 3);
    private float pitch = 0;
    private float yaw = -90;
    private final float sensitivity = 0.1f;
    private final float speed = 5.0f;

    private Vector3f front = new Vector3f(0, 0, -1);
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f right = new Vector3f();
    private final Vector3f worldUp = new Vector3f(0, 1, 0);

    public void update(float deltaTime, float dx, float dy) {
        yaw += dx * sensitivity;
        pitch += dy * sensitivity;

        if (pitch > 89.0f) {
            pitch = 89.0f;
        }
        if (pitch < -89.0f) {
            pitch = -89.0f;
        }

        updateVectors();

        float velocity = speed * deltaTime;

        if (InputManager.isKeyDown(GLFW_KEY_W)) {
            position.add(new Vector3f(front).mul(velocity));
        }
        if (InputManager.isKeyDown(GLFW_KEY_S)) {
            position.sub(new Vector3f(front).mul(velocity));
        }
        if (InputManager.isKeyDown(GLFW_KEY_A)) {
            position.sub(new Vector3f(right).mul(velocity));
        }
        if (InputManager.isKeyDown(GLFW_KEY_D)) {
            position.add(new Vector3f(right).mul(velocity));
        }
        if (InputManager.isKeyDown(GLFW_KEY_SPACE)) {
            position.add(new Vector3f(up).mul(velocity));
        }
        if (InputManager.isKeyDown(GLFW_KEY_LEFT_SHIFT)) {
            position.sub(new Vector3f(up).mul(velocity));
        }
    }

    private void updateVectors() {
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();

        right.set(front).cross(worldUp).normalize();
        up.set(right).cross(front).normalize();
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().lookAt(position, new Vector3f(position).add(front), up);
    }

    public Matrix4f getProjectionMatrix(int width, int height) {
        return new Matrix4f().perspective((float) Math.toRadians(70.0f), (float) width / height, .1f, 1000f);
    }
}
