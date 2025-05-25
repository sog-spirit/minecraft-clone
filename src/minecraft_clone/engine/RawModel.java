package minecraft_clone.engine;

import java.util.List;

public class RawModel {
    private final int vertexArrayObjectID;
    private final List<Integer> vertexBufferObjectIDs;
    private final int vertexCount;

    public RawModel(int vertexArrayObjectID, List<Integer> vertexBufferObjectIDs, int vertexCount) {
        this.vertexArrayObjectID = vertexArrayObjectID;
        this.vertexBufferObjectIDs = vertexBufferObjectIDs;
        this.vertexCount = vertexCount;
    }

    public int getVertexArrayObjectID() {
        return vertexArrayObjectID;
    }

    public List<Integer> getVertexBufferObjectIDs() {
        return vertexBufferObjectIDs;
    }

    public int getVertexCount() {
        return vertexCount;
    }
}
