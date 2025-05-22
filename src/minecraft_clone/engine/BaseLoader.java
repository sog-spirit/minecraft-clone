package minecraft_clone.engine;

public interface BaseLoader {
    RawModel loadToVertexArrayObject(float[] vertices, int[] indices, int vertexSize);
    void cleanup();
}
