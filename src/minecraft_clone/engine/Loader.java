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

    public RawModel loadToVertexArrayObject(float[] positions) {
        int vertexArrayObjectID = glGenVertexArrays();
        glBindVertexArray(vertexArrayObjectID);

        int vertexBufferObjectID = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vertexBufferObjectID);
        glBufferData(GL_ARRAY_BUFFER, positions, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
        glEnableVertexAttribArray(0);

        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        vertexArrayObjects.add(vertexArrayObjectID);
        vertexBufferObjects.add(vertexBufferObjectID);

        return new RawModel(vertexArrayObjectID, positions.length / 3);
    }

    public RawModel loadToVertexArrayObject(float[] vertices, int[] indices, int vertexSize) {
        int vertexArrayObjectID = createVertexArrayObject();
        IntBuffer intBuffer = bindIndicesBuffer(indices);
        FloatBuffer floatBuffer = storeDataInAttributeList(0, 3, vertexSize, vertices); // position (x, y, z)
        FloatBuffer floatBuffer2 = storeDataInAttributeList(1, 2, vertexSize, vertices); // uv (u, v)
        unbindVertexArrayObject();
        memFree(intBuffer);
        memFree(floatBuffer);
        memFree(floatBuffer2);
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

        glVertexAttribPointer(attributeNumber, size, GL_FLOAT, false, vertexSize * Float.BYTES, attributeNumber == 0 ? 0 : 3 * Float.BYTES);
        glEnableVertexAttribArray(attributeNumber);

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
