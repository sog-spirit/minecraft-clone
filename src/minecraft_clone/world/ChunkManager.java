package minecraft_clone.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.joml.Vector3f;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.render.TextureAtlas;

public class ChunkManager {
    private Map<String, Chunk> loadedChunks;
    private Map<String, Future<Chunk>> pendingChunks;
    private BaseLoader loader;
    private TextureAtlas atlas;
    private PerlinNoise noise;
    private ExecutorService chunkGenerationExecutor;

    private int renderDistance = 8; // Chunks to keep loaded around player
    private int unloadDistance = 10; // Distance at which to unload chunks
    private Vector3f lastPlayerPosition = new Vector3f();
    private boolean forceUpdate = true;
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;

    public ChunkManager(BaseLoader loader, TextureAtlas atlas) {
        this.loadedChunks = new ConcurrentHashMap<>();
        this.pendingChunks = new ConcurrentHashMap<>();
        this.loader = loader;
        this.atlas = atlas;
        this.noise = new PerlinNoise(12345);
        this.chunkGenerationExecutor = Executors.newFixedThreadPool(4);
    }

    public void updateChunks(Vector3f playerPosition) {
        int playerChunkX = (int) Math.floor(playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(playerPosition.z / Chunk.CHUNK_SIZE);

        if (!forceUpdate && playerChunkX == lastPlayerChunkX && playerChunkZ == lastPlayerChunkZ) {
            return;
        }

        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;
        forceUpdate = false;
        lastPlayerPosition.set(playerPosition);

        Set<String> chunksToLoad = getChunksInRange(playerChunkX, playerChunkZ, renderDistance);

        loadNewChunks(chunksToLoad);

        unloadDistantChunks(playerChunkX, playerChunkZ);

        updateChunkNeighbors();
    }

    private Set<String> getChunksInRange(int centerX, int centerZ, int range) {
        Set<String> chunks = new HashSet<>();
        for (int x = centerX - range; x <= centerX + range; x++) {
            for (int z = centerZ - range; z <= centerZ + range; z++) {
                chunks.add(getChunkKey(x, z));
            }
        }
        return chunks;
    }

    private void loadNewChunks(Set<String> chunksToLoad) {
        for (String chunkKey : chunksToLoad) {
            if (!loadedChunks.containsKey(chunkKey) && !pendingChunks.containsKey(chunkKey)) {
                // Start async chunk generation
                Future<Chunk> future = chunkGenerationExecutor.submit(() -> {
                    String[] coords = chunkKey.split(",");
                    int chunkX = Integer.parseInt(coords[0]);
                    int chunkZ = Integer.parseInt(coords[1]);
                    Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, 0, chunkZ * Chunk.CHUNK_SIZE);
                    return new Chunk(position, loader, atlas, noise);
                });
                pendingChunks.put(chunkKey, future);
            }
        }
        
        // Check for completed chunk generations
        List<String> completedChunks = new ArrayList<>();
        for (Map.Entry<String, Future<Chunk>> entry : pendingChunks.entrySet()) {
            if (entry.getValue().isDone()) {
                try {
                    Chunk chunk = entry.getValue().get();
                    loadedChunks.put(entry.getKey(), chunk);
                    completedChunks.add(entry.getKey());
                } catch (Exception e) {
                    System.err.println("Error generating chunk " + entry.getKey() + ": " + e.getMessage());
                    completedChunks.add(entry.getKey()); // Remove from pending even if failed
                }
            }
        }
        
        // Remove completed chunks from pending
        for (String key : completedChunks) {
            pendingChunks.remove(key);
        }
    }

    private void unloadDistantChunks(int playerChunkX, int playerChunkZ) {
        List<String> chunksToUnload = new ArrayList<>();
        
        for (String chunkKey : loadedChunks.keySet()) {
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            
            int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
            
            if (distance > unloadDistance) {
                chunksToUnload.add(chunkKey);
            }
        }
        
        // Unload chunks
        for (String chunkKey : chunksToUnload) {
            Chunk chunk = loadedChunks.remove(chunkKey);
            if (chunk != null) {
                // Clean up chunk resources if needed
                cleanupChunk(chunk);
            }
        }
        
        // Also cancel pending chunks that are too far
        List<String> pendingToCancel = new ArrayList<>();
        for (String chunkKey : pendingChunks.keySet()) {
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            
            int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
            
            if (distance > unloadDistance) {
                pendingToCancel.add(chunkKey);
            }
        }
        
        for (String chunkKey : pendingToCancel) {
            Future<Chunk> future = pendingChunks.remove(chunkKey);
            if (future != null) {
                future.cancel(true);
            }
        }
    }

    private void updateChunkNeighbors() {
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            String chunkKey = entry.getKey();
            Chunk chunk = entry.getValue();
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkZ = Integer.parseInt(coords[1]);
            
            // Set neighbors: 0 (+x), 1 (-x), 2 (+z), 3 (-z)
            chunk.setNeighbor(0, loadedChunks.get(getChunkKey(chunkX + 1, chunkZ))); // +x
            chunk.setNeighbor(1, loadedChunks.get(getChunkKey(chunkX - 1, chunkZ))); // -x
            chunk.setNeighbor(2, loadedChunks.get(getChunkKey(chunkX, chunkZ + 1))); // +z
            chunk.setNeighbor(3, loadedChunks.get(getChunkKey(chunkX, chunkZ - 1))); // -z
        }
        
        // Generate meshes for chunks that need updates
        for (Chunk chunk : loadedChunks.values()) {
            if (chunk.needsMeshUpdate()) {
                chunk.generateMesh();
            }
        }
    }

    private String getChunkKey(int chunkX, int chunkZ) {
        return chunkX + "," + chunkZ;
    }

    private void cleanupChunk(Chunk chunk) {
        chunk.cleanup();
    }

    public void forceUpdate() {
        this.forceUpdate = true;
    }

    public Map<String, Chunk> getChunks() {
        return new HashMap<>(loadedChunks);
    }

    public Map<String, Chunk> getLoadedChunks() {
        return loadedChunks;
    }

    public void setRenderDistance(int distance) {
        this.renderDistance = distance;
        this.unloadDistance = distance + 2;
        forceUpdate();
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public Chunk getChunkAt(Vector3f worldPosition) {
        int chunkX = (int) Math.floor(worldPosition.x / Chunk.CHUNK_SIZE);
        int chunkZ = (int) Math.floor(worldPosition.z / Chunk.CHUNK_SIZE);
        return loadedChunks.get(getChunkKey(chunkX, chunkZ));
    }

    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        return loadedChunks.containsKey(getChunkKey(chunkX, chunkZ));
    }

    public String getLoadingStats() {
        return String.format("Loaded: %d, Pending: %d, Render Distance: %d", loadedChunks.size(), pendingChunks.size(), renderDistance);
    }

    public void cleanup() {
        chunkGenerationExecutor.shutdown();
        for (Future<Chunk> future : pendingChunks.values()) {
            future.cancel(true);
        }
        pendingChunks.clear();
        
        for (Chunk chunk : loadedChunks.values()) {
            cleanupChunk(chunk);
        }
        loadedChunks.clear();
    }
}
