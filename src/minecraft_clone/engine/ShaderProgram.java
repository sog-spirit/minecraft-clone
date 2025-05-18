package minecraft_clone.engine;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
    private final int programID;

    public ShaderProgram(String vertexPath, String fragmentPath) {
        String vertexCode = readFile(vertexPath);
        String fragmentCode = readFile(fragmentPath);
        int vertexID = compileShader(vertexCode, GL_VERTEX_SHADER);
        int fragmentID = compileShader(fragmentCode, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);
        glLinkProgram(programID);
        glValidateProgram(programID);

        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    private int compileShader(String source, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, source);
        glCompileShader(id);
        return id;
    }

    private String readFile(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load shader: " + path, e);
        }
    }

    public void start() {
        glUseProgram(programID);
    }

    public void stop() {
        glUseProgram(0);
    }

    public void cleanup() {
        glDeleteProgram(programID);
    }

    public void loadModelMatrix(Matrix4f matrix) {
        loadMatrix("model", matrix);
    }

    public void loadViewMatrix(Matrix4f matrix) {
        loadMatrix("view", matrix);
    }

    public void loadProjectionMatrix(Matrix4f matrix) {
        loadMatrix("projection", matrix);
    }

    private void loadMatrix(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = stack.mallocFloat(16);
            matrix.get(floatBuffer);
            glUniformMatrix4fv(glGetUniformLocation(programID, name), false, floatBuffer);
        }
    }
}
