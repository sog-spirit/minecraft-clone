package minecraft_clone.engine;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

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

    public void cleanup() {
        for (int vertexArrayObject : this.vertexArrayObjects) {
            glDeleteVertexArrays(vertexArrayObject);
        }
        for (int vertexBufferObject : this.vertexBufferObjects) {
            glDeleteBuffers(vertexBufferObject);
        }
    }
}
