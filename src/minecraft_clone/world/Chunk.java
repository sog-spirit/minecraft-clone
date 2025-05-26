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
    private boolean needsMeshUpdate = true;
    private long lastAccessTime;
    private int chunkX, chunkZ;

    public Chunk(Vector3f position, BaseLoader loader, TextureAtlas atlas, PerlinNoise noise) {
        this.position = position;
        this.loader = loader;
        this.atlas = atlas;
        this.blocks = new BlockType[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.neighbors = new Chunk[4];
        this.noise = noise;
        this.lastAccessTime = System.currentTimeMillis();
        this.chunkX = (int) (position.x / CHUNK_SIZE);
        this.chunkZ = (int) (position.z / CHUNK_SIZE);
        generateTerrain();
    }

    public void setNeighbor(int direction, Chunk neighbor) {
        Chunk oldNeighbor = neighbors[direction];
        neighbors[direction] = neighbor;
        
        // If neighbor changed, we might need mesh update
        if (oldNeighbor != neighbor) {
            needsMeshUpdate = true;
            // Also notify the neighbor that it might need an update
            if (neighbor != null) {
                neighbor.markForMeshUpdate();
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
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // Calculate world coordinates
                float worldX = position.x + x;
                float worldZ = position.z + z;
                
                // Generate height using multiple octaves of Perlin noise for more realistic terrain
                float height = 0;
                float amplitude = 1;
                float frequency = 0.01f;
                
                // Multiple octaves for more detailed terrain
                for (int i = 0; i < 4; i++) {
                    height += noise.noise(worldX * frequency, worldZ * frequency) * amplitude;
                    amplitude *= 0.5f;
                    frequency *= 2.0f;
                }
                
                // Scale height to fit within chunk (0 to CHUNK_SIZE)
                int terrainHeight = (int) ((height + 1) * 0.3f * CHUNK_SIZE) + CHUNK_SIZE / 4;
                terrainHeight = Math.max(1, Math.min(CHUNK_SIZE - 1, terrainHeight));

                for (int y = 0; y < CHUNK_SIZE; y++) {
                    if (y < terrainHeight - 3) {
                        blocks[x][y][z] = BlockType.STONE;
                    } else if (y < terrainHeight - 1) {
                        blocks[x][y][z] = BlockType.DIRT;
                    } else if (y == terrainHeight - 1) {
                        // Occasionally place glass blocks for testing transparency
                        if (noise.noise(worldX * 0.1f, worldZ * 0.1f) > 0.7f) {
                            blocks[x][y][z] = BlockType.GLASS;
                        } else {
                            blocks[x][y][z] = BlockType.GRASS;
                        }
                    } else {
                        blocks[x][y][z] = null; // Air
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
        }

        meshGenerated = true;
        needsMeshUpdate = false;
    }

    private void addVisibleFaces(int x, int y, int z, BlockType type, List<Float> vertices, List<Integer> indices) {
        float[] cubeVertices = CubeModel.getCube(atlas, type);
        BlockProperties props = BlockRegistry.get(type);
        
        // For transparent blocks, we need to render faces that are adjacent to air or other transparent blocks
        // For opaque blocks, we only render faces adjacent to air or transparent blocks
        
        if (shouldRenderFace(x, y + 1, z, type)) {
            float r = (type == BlockType.GRASS) ? 0.4863f : 1.0f;
            float g = (type == BlockType.GRASS) ? 0.7412f : 1.0f;
            float b = (type == BlockType.GRASS) ? 0.2706f : 1.0f;
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 16, 20, x, y, z, r, g, b, alpha); // Top
        }
        if (shouldRenderFace(x, y - 1, z, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 20, 24, x, y, z, 1.0f, 1.0f, 1.0f, alpha); // Bottom
        }
        if (shouldRenderFace(x, y, z + 1, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 0, 4, x, y, z, 1.0f, 1.0f, 1.0f, alpha);   // Front
        }
        if (shouldRenderFace(x, y, z - 1, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 4, 8, x, y, z, 1.0f, 1.0f, 1.0f, alpha);   // Back
        }
        if (shouldRenderFace(x - 1, y, z, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 8, 12, x, y, z, 1.0f, 1.0f, 1.0f, alpha);  // Left
        }
        if (shouldRenderFace(x + 1, y, z, type)) {
            float alpha = props.isTransparent ? 0.8f : 1.0f;
            addFace(vertices, indices, cubeVertices, 12, 16, x, y, z, 1.0f, 1.0f, 1.0f, alpha); // Right
        }
    }

    private boolean shouldRenderFace(int x, int y, int z, BlockType currentType) {
        BlockProperties currentProps = BlockRegistry.get(currentType);
        
        // Check if the position is within the current chunk
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            BlockType adjacent = blocks[x][y][z];
            if (adjacent == null) {
                return true; // Adjacent to air, always render
            }
            
            BlockProperties adjacentProps = BlockRegistry.get(adjacent);
            
            // If current block is opaque, only render face if adjacent is air or transparent
            if (!currentProps.isTransparent) {
                return adjacent == BlockType.AIR || adjacentProps.isTransparent;
            }
            
            // For transparent blocks, be more selective to reduce z-fighting
            if (currentProps.isTransparent) {
                // Don't render transparent faces adjacent to the same transparent block type
                if (adjacent == currentType) {
                    return false;
                }
                // Don't render transparent faces directly adjacent to opaque blocks
                // (this reduces z-fighting but might affect visual quality)
                if (!adjacentProps.isTransparent && adjacent != BlockType.AIR) {
                    return false; // Comment this line if you want transparent faces against solid blocks
                }
                // Render if adjacent to air or different block types
                return adjacent == BlockType.AIR || adjacent != currentType;
            }
            
        } else {
            // Handle chunk boundaries (similar logic as before)
            int chunkX = (x < 0) ? -1 : (x >= CHUNK_SIZE ? 1 : 0);
            int chunkZ = (z < 0) ? -1 : (z >= CHUNK_SIZE ? 1 : 0);
            int blockX = (x + CHUNK_SIZE) % CHUNK_SIZE;
            int blockY = y;
            int blockZ = (z + CHUNK_SIZE) % CHUNK_SIZE;

            Chunk neighbor = getNeighbor(chunkX, chunkZ);
            if (neighbor != null) {
                BlockType adjacent = neighbor.getBlock(blockX, blockY, blockZ);
                if (adjacent == null) {
                    return true;
                }
                
                BlockProperties adjacentProps = BlockRegistry.get(adjacent);
                
                if (!currentProps.isTransparent) {
                    return adjacent == BlockType.AIR || adjacentProps.isTransparent;
                }
                
                // Same transparent block logic for chunk boundaries
                if (adjacent == currentType) {
                    return false;
                }
                if (!adjacentProps.isTransparent && adjacent != BlockType.AIR) {
                    return false; // Reduce z-fighting
                }
                return adjacent == BlockType.AIR || adjacent != currentType;
            } else {
                return true;
            }
        }
        
        return false;
    }

    private Chunk getNeighbor(int chunkX, int chunkZ) {
        if (chunkX == 1 && chunkZ == 0) return neighbors[0]; // +x
        if (chunkX == -1 && chunkZ == 0) return neighbors[1]; // -x
        if (chunkX == 0 && chunkZ == 1) return neighbors[2]; // +z
        if (chunkX == 0 && chunkZ == -1) return neighbors[3]; // -z
        return null; // No diagonal neighbors for simplicity
    }

    private BlockType getBlock(int x, int y, int z) {
        if (y < 0 || y >= CHUNK_SIZE) return null; // Out of vertical bounds
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

    public int getDistanceFrom(int otherChunkX, int otherChunkZ) {
        return Math.max(Math.abs(chunkX - otherChunkX), Math.abs(chunkZ - otherChunkZ));
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
