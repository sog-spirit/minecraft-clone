package minecraft_clone.world;

import java.util.HashMap;
import java.util.Map;

import org.joml.Vector3f;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.render.TextureAtlas;

public class ChunkManager {
    private Map<String, Chunk> chunks;
    private BaseLoader loader;
    private TextureAtlas atlas;
    private PerlinNoise noise;

    public ChunkManager(BaseLoader loader, TextureAtlas atlas) {
        this.chunks = new HashMap<>();
        this.loader = loader;
        this.atlas = atlas;
        this.noise = new PerlinNoise(12345);
    }

    public void generateChunks(int range) {
     // Generate chunks in a square grid from -range to +range in x and z
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                String key = x + "," + z;
                Vector3f position = new Vector3f(x * Chunk.CHUNK_SIZE, 0, z * Chunk.CHUNK_SIZE);
                Chunk chunk = new Chunk(position, loader, atlas, noise);
                chunks.put(key, chunk);
            }
        }

     // Set neighbors for each chunk
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                String key = x + "," + z;
                Chunk chunk = chunks.get(key);
                if (chunk != null) {
                    // Set neighbors: 0 (+x), 1 (-x), 2 (+z), 3 (-z)
                    chunk.setNeighbor(0, chunks.get((x + 1) + "," + z)); // +x
                    chunk.setNeighbor(1, chunks.get((x - 1) + "," + z)); // -x
                    chunk.setNeighbor(2, chunks.get(x + "," + (z + 1))); // +z
                    chunk.setNeighbor(3, chunks.get(x + "," + (z - 1))); // -z
                }
            }
        }

     // Generate meshes after setting neighbors to ensure correct occlusion culling
        for (Chunk chunk : chunks.values()) {
            chunk.generateMesh();
        }
    }

    public Map<String, Chunk> getChunks() {
        return chunks;
    }
}
