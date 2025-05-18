package minecraft_clone.entity;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private final Vector3f position = new Vector3f(0, 0, 3);
    private final float pitch = 20;
    private final float yaw = 0;

    public void update() {
        
    }

    public Matrix4f getViewMatrix() {
        return new Matrix4f().identity()
                .rotate((float) Math.toRadians(pitch), new Vector3f(1, 0, 0))
                .rotate((float) Math.toRadians(yaw), new Vector3f(0, 1, 0))
                .translate(-position.x, -position.y, -position.z);
    }

    public Matrix4f getProjectionMatrix(int width, int height) {
        return new Matrix4f().perspective((float) Math.toRadians(70.0f), (float) width / height, .1f, 1000f);
    }
}
