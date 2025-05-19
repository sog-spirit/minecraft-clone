package minecraft_clone.engine;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
    private final int programID;

    public Shader(String vertexPath, String fragmentPath) {
        String vertexCode = loadShader(vertexPath);
        String fragmentCode = loadShader(fragmentPath);
        int vertexID = compileShader(vertexCode, GL_VERTEX_SHADER);
        int fragmentID = compileShader(fragmentCode, GL_FRAGMENT_SHADER);

        programID = glCreateProgram();
        glAttachShader(programID, vertexID);
        glAttachShader(programID, fragmentID);
        glLinkProgram(programID);
        glValidateProgram(programID);
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader link failed:\n" + glGetProgramInfoLog(programID));
        }
        glDeleteShader(vertexID);
        glDeleteShader(fragmentID);
    }

    private int compileShader(String source, int type) {
        int id = glCreateShader(type);
        glShaderSource(id, source);
        glCompileShader(id);
        if (glGetShaderi(id, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Shader compile failed:\n" + glGetShaderInfoLog(id));
        }
        return id;
    }

    private String loadShader(String path) {
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

    public void loadTextureSampler() {
        loadUniform("textureSampler", 0);
    }

    private void loadMatrix(String name, Matrix4f matrix) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer floatBuffer = stack.mallocFloat(16);
            matrix.get(floatBuffer);
            glUniformMatrix4fv(glGetUniformLocation(programID, name), false, floatBuffer);
        }
    }

    private void loadUniform(String name, int value) {
        int location = glGetUniformLocation(programID, name);
        glUniform1i(location, value);
    }
}
