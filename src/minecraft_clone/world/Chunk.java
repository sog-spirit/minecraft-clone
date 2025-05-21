package minecraft_clone.world;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3f;

import minecraft_clone.engine.Loader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.render.CubeModel;
import minecraft_clone.render.TextureAtlas;

public class Chunk {
    public static final int CHUNK_SIZE = 16;
    private Block[][][] blocks;
    private RawModel model;
    private Vector3f position;
    private Loader loader;
    private TextureAtlas atlas;
    private Chunk[] neighbors; // 0: +x, 1: -x, 2: +z, 3: -z
    private PerlinNoise noise;

    public Chunk(Vector3f position, Loader loader, TextureAtlas atlas, PerlinNoise noise) {
        this.position = position;
        this.loader = loader;
        this.atlas = atlas;
        this.blocks = new Block[CHUNK_SIZE][CHUNK_SIZE][CHUNK_SIZE];
        this.neighbors = new Chunk[4];
        this.noise = noise;
        generateTerrain();
    }

    public void setNeighbor(int direction, Chunk neighbor) {
        neighbors[direction] = neighbor;
    }

    private void generateTerrain() {
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                // Calculate world coordinates
                float worldX = position.x + x;
                float worldZ = position.z + z;
                // Generate height using Perlin noise
                float height = noise.noise(worldX * 0.1f, worldZ * 0.1f);
                // Scale height to fit within chunk (0 to CHUNK_SIZE)
                int terrainHeight = (int) ((height + 1) * 0.5f * CHUNK_SIZE);
                terrainHeight = Math.max(1, Math.min(CHUNK_SIZE - 1, terrainHeight));

                for (int y = 0; y < CHUNK_SIZE; y++) {
                    if (y < terrainHeight - 2) {
                        blocks[x][y][z] = new Block(new Vector3f(x, y, z), BlockType.STONE);
                    } else if (y < terrainHeight - 1) {
                        blocks[x][y][z] = new Block(new Vector3f(x, y, z), BlockType.DIRT);
                    } else if (y == terrainHeight - 1) {
                        blocks[x][y][z] = new Block(new Vector3f(x, y, z), BlockType.GRASS);
                    } else {
                        blocks[x][y][z] = null; // Air
                    }
                }
            }
        }
    }

    public void generateMesh() {
        List<Float> verticesList = new ArrayList<>();
        List<Integer> indicesList = new ArrayList<>();

        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    Block block = blocks[x][y][z];
                    if (block != null && block.getType() != BlockType.AIR) {
                        addVisibleFaces(x, y, z, block.getType(), verticesList, indicesList);
                    }
                }
            }
        }

        // Convert to arrays
        float[] vertices = new float[verticesList.size()];
        for (int i = 0; i < verticesList.size(); i++) {
            vertices[i] = verticesList.get(i);
        }
        int[] indices = new int[indicesList.size()];
        for (int i = 0; i < indicesList.size(); i++) {
            indices[i] = indicesList.get(i);
        }

        // Load into VAO
        model = loader.loadToVertexArrayObject(vertices, indices, 5); // 5 floats per vertex
    }

    private void addVisibleFaces(int x, int y, int z, BlockType type, List<Float> vertices, List<Integer> indices) {
        float[] cubeVertices = CubeModel.getCube(atlas, type);
        if (isFaceVisible(x, y + 1, z)) addFace(vertices, indices, cubeVertices, 16, 20, x, y, z); // Top
        if (isFaceVisible(x, y - 1, z)) addFace(vertices, indices, cubeVertices, 20, 24, x, y, z); // Bottom
        if (isFaceVisible(x, y, z + 1)) addFace(vertices, indices, cubeVertices, 0, 4, x, y, z);   // Front
        if (isFaceVisible(x, y, z - 1)) addFace(vertices, indices, cubeVertices, 4, 8, x, y, z);   // Back
        if (isFaceVisible(x - 1, y, z)) addFace(vertices, indices, cubeVertices, 8, 12, x, y, z);  // Left
        if (isFaceVisible(x + 1, y, z)) addFace(vertices, indices, cubeVertices, 12, 16, x, y, z); // Right
    }

    private boolean isFaceVisible(int x, int y, int z) {
     // Check if the position is within the current chunk
        if (x >= 0 && x < CHUNK_SIZE && y >= 0 && y < CHUNK_SIZE && z >= 0 && z < CHUNK_SIZE) {
            Block adjacent = blocks[x][y][z];
            return adjacent == null || adjacent.getType() == BlockType.AIR;
        } else {
            // Handle out-of-bounds cases by checking adjacent chunks
            int chunkX = (x < 0) ? -1 : (x >= CHUNK_SIZE ? 1 : 0);
            int chunkZ = (z < 0) ? -1 : (z >= CHUNK_SIZE ? 1 : 0);
            int blockX = (x + CHUNK_SIZE) % CHUNK_SIZE;
            int blockY = y; // Assuming y stays within bounds for simplicity
            int blockZ = (z + CHUNK_SIZE) % CHUNK_SIZE;

            // Get the neighboring chunk
            Chunk neighbor = getNeighbor(chunkX, chunkZ);
            if (neighbor != null) {
                Block adjacent = neighbor.getBlock(blockX, blockY, blockZ);
                return adjacent == null || adjacent.getType() == BlockType.AIR;
            } else {
                return true; // No neighbor available, assume visible
            }
        }
    }

    private Chunk getNeighbor(int chunkX, int chunkZ) {
        if (chunkX == 1 && chunkZ == 0) return neighbors[0]; // +x
        if (chunkX == -1 && chunkZ == 0) return neighbors[1]; // -x
        if (chunkX == 0 && chunkZ == 1) return neighbors[2]; // +z
        if (chunkX == 0 && chunkZ == -1) return neighbors[3]; // -z
        return null; // No diagonal neighbors for simplicity
    }

    private Block getBlock(int x, int y, int z) {
        if (y < 0 || y >= CHUNK_SIZE) return null; // Out of vertical bounds
        return blocks[x][y][z];
    }

    private void addFace(List<Float> vertices, List<Integer> indices, float[] cubeVertices, int vertexStart, int vertexEnd, int x, int y, int z) {
        int startIndex = vertices.size() / 5; // Current vertex count before adding this face
        // Add the 4 vertices for this face
        for (int i = vertexStart; i < vertexEnd; i++) {
            vertices.add(cubeVertices[i * 5] + x);      // x position
            vertices.add(cubeVertices[i * 5 + 1] + y);  // y position
            vertices.add(cubeVertices[i * 5 + 2] + z);  // z position
            vertices.add(cubeVertices[i * 5 + 3]);      // u coordinate
            vertices.add(cubeVertices[i * 5 + 4]);      // v coordinate
        }
        // Add indices for two triangles: 0,1,2 and 2,3,0
        indices.add(startIndex + 0);
        indices.add(startIndex + 1);
        indices.add(startIndex + 2);
        indices.add(startIndex + 2);
        indices.add(startIndex + 3);
        indices.add(startIndex + 0);
    }

    public RawModel getModel() {
        return model;
    }

    public Vector3f getPosition() {
        return position;
    }
}
