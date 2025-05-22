package minecraft_clone.engine;

import org.joml.Matrix4f;

public interface BaseShader {
    void start();
    void stop();
    void cleanup();
    void loadUniformMatrix4f(String name, Matrix4f value);
    void loadUniformInt(String name, int value);
}
