package minecraft_clone.engine;

public class RawModel {
    private final int vertexArrayObjectID;
    private final int vertexCount;

    public RawModel(int vertexArrayObjectID, int vertexCount) {
        this.vertexArrayObjectID = vertexArrayObjectID;
        this.vertexCount = vertexCount;
    }

    public int getVertexArrayObjectID() {
        return vertexArrayObjectID;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
