package minecraft_clone.engine;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Loader {
    private List<Integer> vertexArrayObjects = new ArrayList<>();
    private List<Integer> vertexBufferObjects = new ArrayList<>();

    public RawModel loadToVertexArrayObject(float[] vertices, int[] indices, int vertexSize) {
        int vertexArrayObjectID = createVertexArrayObject();
        IntBuffer intBuffer = bindIndicesBuffer(indices);
        FloatBuffer positionFloatBuffer = storeDataInAttributeList(0, 3, vertexSize, vertices); // position (x, y, z)
        FloatBuffer uvMappingFloatBuffer = storeDataInAttributeList(1, 2, vertexSize, vertices); // uv (u, v)
        FloatBuffer colorFloatBuffer = storeDataInAttributeList(2, 3, vertexSize, vertices);
        unbindVertexArrayObject();
        memFree(intBuffer);
        memFree(positionFloatBuffer);
        memFree(uvMappingFloatBuffer);
        memFree(colorFloatBuffer);
        return new RawModel(vertexArrayObjectID, indices.length);
    }

    private int createVertexArrayObject() {
        int vertexArrayObjectID = glGenVertexArrays();
        vertexArrayObjects.add(vertexArrayObjectID);
        glBindVertexArray(vertexArrayObjectID);
        return vertexArrayObjectID;
    }

    private IntBuffer bindIndicesBuffer(int[] indices) {
        int vertexBufferObjectID = glGenBuffers();
        vertexBufferObjects.add(vertexBufferObjectID);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vertexBufferObjectID);
        IntBuffer intBuffer = storeDataInIntBuffer(indices);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, intBuffer, GL_STATIC_DRAW);
        return intBuffer;
    }

    private IntBuffer storeDataInIntBuffer(int[] data) {
        IntBuffer intBuffer = memAllocInt(data.length);
        intBuffer.put(data).flip();
        return intBuffer;
    }

    private FloatBuffer storeDataInAttributeList(int attributeNumber, int size, int vertexSize, float[] data) {
        int vertexBufferObjectID = glGenBuffers();
        vertexBufferObjects.add(vertexBufferObjectID);
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectID);

        FloatBuffer buffer = storeDataInFloatBuffer(data);
        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

        switch (attributeNumber) {
        case 0 -> {
            glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, vertexSize * Float.BYTES, 0); // position
            glEnableVertexAttribArray(attributeNumber);
        }
        case 1 -> {
            glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, vertexSize * Float.BYTES, 3 * Float.BYTES); // UV
            glEnableVertexAttribArray(attributeNumber);
        }
        case 2 -> {
            glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, vertexSize * Float.BYTES, 5 * Float.BYTES); // Color
            glEnableVertexAttribArray(attributeNumber);
        }
        }

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        return buffer;
    }

    private FloatBuffer storeDataInFloatBuffer(float[] data) {
        FloatBuffer floatBuffer = memAllocFloat(data.length);
        floatBuffer.put(data).flip();
        return floatBuffer;
    }

    public void cleanup() {
        for (int vertexArrayObject : this.vertexArrayObjects) {
            glDeleteVertexArrays(vertexArrayObject);
        }
        for (int vertexBufferObject : this.vertexBufferObjects) {
            glDeleteBuffers(vertexBufferObject);
        }
    }

    private static void unbindVertexArrayObject() {
        glBindVertexArray(0);
    }
}
