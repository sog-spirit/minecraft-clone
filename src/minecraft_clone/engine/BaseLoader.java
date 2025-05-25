package minecraft_clone.engine;

import java.util.List;

public interface BaseLoader {
    RawModel loadToVertexArrayObject(float[] vertices, int[] indices, int vertexSize);
    RawModel loadTo2DVertexArrayObject(float[] vertices, int[] indices, int vertexSize);
    void deleteVertexArrayObject(int vertexArrayObjectID);
    void deleteVertexBufferObjects(List<Integer> vertexBufferObjectIDs);
    void cleanup();
}
