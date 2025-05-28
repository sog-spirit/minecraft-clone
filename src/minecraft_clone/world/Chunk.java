package minecraft_clone.world;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.render.CubeModel;
import minecraft_clone.render.TextureAtlas;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    private BlockType[][][] blocks;
    private RawModel opaqueModel;
    private RawModel transparentModel;
    private Vector3f position;
    private BaseLoader loader;
    private TextureAtlas atlas;
    private Chunk[] neighbors; // 0: +x, 1: -x, 2: +z, 3: -z
    private PerlinNoise noise;

    private boolean meshGenerated = false;
    private boolean needsMeshUpdate = false;
    private long lastAccessTime;
    private int chunkX, chunkY, chunkZ;

    public Chunk(Vector3f position, BaseLoader loader, TextureAtlas atlas, PerlinNoise noise) {
        this.position = position;
        this.loader = loader;
        this.atlas = atlas;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.neighbors = new Chunk[6];
        this.noise = noise;
        this.lastAccessTime = System.currentTimeMillis();
        this.chunkX = (int) (position.x / CHUNK_SIZE);
        this.chunkY = (int) (position.y / CHUNK_SIZE);
        this.chunkZ = (int) (position.z / CHUNK_SIZE);
        generateTerrain();
    }

    public void setNeighbor(int direction, Chunk neighbor) {
        if (direction >= 0 && direction < 6) {
            Chunk oldNeighbor = neighbors[direction];
            neighbors[direction] = neighbor;
            if (oldNeighbor != neighbor) {
                needsMeshUpdate = true;
                if (neighbor != null) {
                    neighbor.markForMeshUpdate();
                }
            }
        }
    }

    public void markForMeshUpdate() {
        needsMeshUpdate = true;
    }

    public boolean needsMeshUpdate() {
        return needsMeshUpdate;
    }

    private void generateTerrain() {
        float worldHeight = 64.0f;
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                float worldX = position.x + x;
                float worldZ = position.z + z;
                float height = 0;
                float amplitude = 1;
                float frequency = 0.01f;
                for (int i = 0; i < 4; i++) {
                    height += noise.noise(worldX * frequency, worldZ * frequency) * amplitude;
                    amplitude *= 0.5f;
                    frequency *= 2.0f;
                }
                int h_global = (int) ((height + 1) * 0.5f * worldHeight);
                h_global = Math.max(0, Math.min((int)worldHeight, h_global));
                for (int y = 0; y < CHUNK_SIZE; y++) {
                    float globalY = position.y + y;
                    if (globalY < h_global) {
                        int depth = (int)(h_global - globalY);
                        if (depth > 3) {
                            blocks[x][y][z] = BlockType.STONE;
                        } else if (depth > 1) {
                            blocks[x][y][z] = BlockType.DIRT;
                        } else {
                            if (noise.noise(worldX * 0.1f, worldZ * 0.1f) > 0.7f) {
                                blocks[x][y][z] = BlockType.GLASS;
                            } else {
                                blocks[x][y][z] = BlockType.GRASS;
                            }
                        }
                    } else {
                        blocks[x][y][z] = BlockType.AIR;
                    }
                }
            }
        }
    }

    public void generateMesh() {
        lastAccessTime = System.currentTimeMillis();

        List<Float> opaqueVerticesList = new ArrayList<>();
        List<Integer> opaqueIndicesList = new ArrayList<>();
        List<Float> transparentVerticesList = new ArrayList<>();
        List<Integer> transparentIndicesList = new ArrayList<>();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    BlockType block = blocks[x][y][z];
                    if (block != null && block != BlockType.AIR) {
                        BlockProperties props = BlockRegistry.get(block);
                        if (props.isTransparent && block!= BlockType.AIR) {
                            addVisibleFaces(x, y, z, block, transparentVerticesList, transparentIndicesList);
                        } else {
                            addVisibleFaces(x, y, z, block, opaqueVerticesList, opaqueIndicesList);
                        }
                    }
                }
            }
        }
        cleanupModels();

        // Create opaque model
        if (!opaqueVerticesList.isEmpty()) {
            float[] opaqueVertices = new float[opaqueVerticesList.size()];
            for (int i = 0; i < opaqueVerticesList.size(); i++) {
                opaqueVertices[i] = opaqueVerticesList.get(i);
            }
            int[] opaqueIndices = new int[opaqueIndicesList.size()];
            for (int i = 0; i < opaqueIndicesList.size(); i++) {
                opaqueIndices[i] = opaqueIndicesList.get(i);
            }
            opaqueModel = loader.loadToVertexArrayObject(opaqueVertices, opaqueIndices, 9);
            opaqueModel.setCullBackFaces(true);
        }

        // Create transparent model
        if (!transparentVerticesList.isEmpty()) {
            float[] transparentVertices = new float[transparentVerticesList.size()];
            for (int i = 0; i < transparentVerticesList.size(); i++) {
                transparentVertices[i] = transparentVerticesList.get(i);
            }
            int[] transparentIndices = new int[transparentIndicesList.size()];
            for (int i = 0; i < transparentIndicesList.size(); i++) {
                transparentIndices[i] = transparentIndicesList.get(i);
            }
            transparentModel = loader.loadToVertexArrayObject(transparentVertices, transparentIndices, 9);
            transparentModel.setCullBackFaces(false);
        }

        meshGenerated = true;
        needsMeshUpdate = false;
    }

    private void addVisibleFaces(int x, int y, int z, BlockType type, List<Float> vertices, List<Integer> indices) {
        float[] cubeVertices = CubeModel.getCube(atlas, type);
        BlockProperties props = BlockRegistry.get(type);

        // For transparent blocks, we need to render faces that are adjacent to air or other transparent blocks
        // For opaque blocks, we only render faces adjacent to air or transparent blocks

        if (shouldRenderFace(x, y, z, 0, 1, 0, type)) {
            float r = (type == BlockType.GRASS) ? 0.4863f : 1.0f;
            float g = (type == BlockType.GRASS) ? 0.7412f : 1.0f;
            float b = (type == BlockType.GRASS) ? 0.2706f : 1.0f;
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 16, 20, x, y, z, r, g, b, alpha); // Top
        }
        if (shouldRenderFace(x, y, z, 0, -1, 0, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 20, 24, x, y, z, 1.0f, 1.0f, 1.0f, alpha); // Bottom
        }
        if (shouldRenderFace(x, y, z, 0, 0, 1, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 0, 4, x, y, z, 1.0f, 1.0f, 1.0f, alpha);   // Front
        }
        if (shouldRenderFace(x, y, z, 0, 0, -1, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 4, 8, x, y, z, 1.0f, 1.0f, 1.0f, alpha);   // Back
        }
        if (shouldRenderFace(x, y, z, -1, 0, 0, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 8, 12, x, y, z, 1.0f, 1.0f, 1.0f, alpha);  // Left
        }
        if (shouldRenderFace(x, y, z, 1, 0, 0, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 12, 16, x, y, z, 1.0f, 1.0f, 1.0f, alpha); // Right
        }
    }

    private boolean shouldRenderFace(int x, int y, int z, int dx, int dy, int dz, BlockType currentType) {
        int adjX = x + dx;
        int adjY = y + dy;
        int adjZ = z + dz;
        BlockProperties currentProps = BlockRegistry.get(currentType);

        if (adjX >= 0 && adjX < CHUNK_SIZE && adjY >= 0 && adjY < CHUNK_SIZE && adjZ >= 0 && adjZ < CHUNK_SIZE) {
            BlockType adjacent = blocks[adjX][adjY][adjZ];
            if (adjacent == null) return true;
            BlockProperties adjacentProps = BlockRegistry.get(adjacent);
            if (!currentProps.isTransparent) {
                return adjacent == BlockType.AIR || adjacentProps.isTransparent;
            }
            if (currentProps.isTransparent) {
                if (adjacent == currentType) return false;
                if (!adjacentProps.isTransparent && adjacent != BlockType.AIR) return false;
                return adjacent == BlockType.AIR || adjacent != currentType;
            }
        } else {
            int chunkOffsetX = (adjX < 0 ? -1 : (adjX >= CHUNK_SIZE ? 1 : 0));
            int chunkOffsetY = (adjY < 0 ? -1 : (adjY >= CHUNK_SIZE ? 1 : 0));
            int chunkOffsetZ = (adjZ < 0 ? -1 : (adjZ >= CHUNK_SIZE ? 1 : 0));
            if (chunkOffsetX == 0 && chunkOffsetY == 0 && chunkOffsetZ == 0) return false;
            Chunk neighbor = getNeighbor(chunkOffsetX, chunkOffsetY, chunkOffsetZ);
            if (neighbor != null) {
                int blockX = (adjX + CHUNK_SIZE) % CHUNK_SIZE;
                int blockY = (adjY + CHUNK_SIZE) % CHUNK_SIZE;
                int blockZ = (adjZ + CHUNK_SIZE) % CHUNK_SIZE;
                BlockType adjacent = neighbor.getBlock(blockX, blockY, blockZ);
                if (adjacent == null) return true;
                BlockProperties adjacentProps = BlockRegistry.get(adjacent);
                if (!currentProps.isTransparent) {
                    return adjacent == BlockType.AIR || adjacentProps.isTransparent;
                }
                if (currentProps.isTransparent) {
                    if (adjacent == currentType) return false;
                    if (!adjacentProps.isTransparent && adjacent != BlockType.AIR) return false;
                    return adjacent == BlockType.AIR || adjacent != currentType;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private Chunk getNeighbor(int dx, int dy, int dz) {
        if (dx == 1 && dy == 0 && dz == 0) return neighbors[0];  // +x
        if (dx == -1 && dy == 0 && dz == 0) return neighbors[1]; // -x
        if (dx == 0 && dy == 0 && dz == 1) return neighbors[2];  // +z
        if (dx == 0 && dy == 0 && dz == -1) return neighbors[3]; // -z
        if (dx == 0 && dy == 1 && dz == 0) return neighbors[4];  // +y
        if (dx == 0 && dy == -1 && dz == 0) return neighbors[5]; // -y
        return null;
    }

    private BlockType getBlock(int x, int y, int z) {
        if (x < 0 || x >= CHUNK_SIZE || y < 0 || y >= CHUNK_SIZE || z < 0 || z >= CHUNK_SIZE) return null;
        return blocks[x][y][z];
    }

    private void addFace(List<Float> vertices, List<Integer> indices, float[] cubeVertices, int vertexStart, int vertexEnd, int x, int y, int z, float r, float g, float b, float alpha) {
        int startIndex = vertices.size() / 9; // Changed to 9 floats per vertex (added alpha)
        // Add the 4 vertices for this face
        for (int i = vertexStart; i < vertexEnd; i++) {
            float posX = cubeVertices[i * 5] + x;
            float posY = cubeVertices[i * 5 + 1] + y;
            float posZ = cubeVertices[i * 5 + 2] + z;

            vertices.add(posX);                         // x position
            vertices.add(posY);                         // y position
            vertices.add(posZ);                         // z position
            vertices.add(cubeVertices[i * 5 + 3]);      // u coordinate
            vertices.add(cubeVertices[i * 5 + 4]);      // v coordinate
            vertices.add(r);                            // red
            vertices.add(g);                            // green
            vertices.add(b);                            // blue
            vertices.add(alpha);                        // alpha
        }
        // Add indices for two triangles: 0,1,2 and 2,3,0
        indices.add(startIndex + 0);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex + 2);
        indices.add(startIndex + 3);
        indices.add(startIndex + 0);
    }

    private void cleanupModels() {
        if (opaqueModel != null) {
            loader.deleteVertexBufferObjects(opaqueModel.getVertexBufferObjectIDs());
            loader.deleteVertexArrayObject(opaqueModel.getVertexArrayObjectID());
            opaqueModel = null;
        }
        if (transparentModel != null) {
            loader.deleteVertexBufferObjects(transparentModel.getVertexBufferObjectIDs());
            loader.deleteVertexArrayObject(transparentModel.getVertexArrayObjectID());
            transparentModel = null;
        }
    }

    public RawModel getModel() {
        return opaqueModel;
    }

    public RawModel getOpaqueModel() {
        return opaqueModel;
    }

    public RawModel getTransparentModel() {
        return transparentModel;
    }

    public Vector3f getPosition() {
        return position;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void updateAccessTime() {
        lastAccessTime = System.currentTimeMillis();
    }

    public boolean isMeshGenerated() {
        return meshGenerated;
    }

    public int getDistanceFrom(int otherChunkX, int otherChunkY, int otherChunkZ) {
        return Math.max(Math.max(Math.abs(chunkX - otherChunkX), Math.abs(chunkY - otherChunkY)), Math.abs(chunkZ - otherChunkZ));
    }

    public boolean isEmpty() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (blocks[x][y][z] != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void cleanup() {
        cleanupModels();
        // Clear block references
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    blocks[x][y][z] = null;
                }
            }
        }
        // Clear neighbor references
        for (int i = 0; i < neighbors.length; i++) {
            neighbors[i] = null;
        }
    }
}
