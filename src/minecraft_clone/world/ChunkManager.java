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

import org.joml.Matrix4f;
import org.joml.Vector3f;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.entity.Camera;
import minecraft_clone.render.Frustum;
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
    private boolean isInitialLoad = true;

    private final Frustum frustum;
    private final Vector3f chunkMin = new Vector3f();
    private final Vector3f chunkMax = new Vector3f();
    private final Vector3f chunkCenter = new Vector3f();

    private int totalChunks = 0;
    private int culledChunks = 0;
    private int renderedChunks = 0;

    public ChunkManager(BaseLoader loader, TextureAtlas atlas) {
        this.loadedChunks = new ConcurrentHashMap<>();
        this.pendingChunks = new ConcurrentHashMap<>();
        this.loader = loader;
        this.atlas = atlas;
        this.noise = new PerlinNoise(12345);
        this.chunkGenerationExecutor = Executors.newFixedThreadPool(4);
        this.frustum = new Frustum();
    }

    public void cullChunks(Camera camera, int screenWidth, int screenHeight) {
        Matrix4f projectionMatrix = camera.getProjectionMatrix(screenWidth, screenHeight);
        Matrix4f viewMatrix = camera.getViewMatrix();
        Matrix4f projectionViewMatrix = new Matrix4f(projectionMatrix).mul(viewMatrix);

        frustum.extractPlanes(projectionViewMatrix);

        totalChunks = loadedChunks.size();
        culledChunks = 0;
        renderedChunks = 0;

        for (Chunk chunk : loadedChunks.values()) {
            if (isChunkVisible(chunk, camera.getPosition())) {
                renderedChunks++;
            } else {
                culledChunks++;
            }
        }
    }

    private boolean isChunkVisible(Chunk chunk, Vector3f cameraPos) {
        Vector3f chunkPos = chunk.getPosition();
        
        // Calculate chunk bounding box
        chunkMin.set(chunkPos.x, chunkPos.y, chunkPos.z);
        chunkMax.set(chunkPos.x + Chunk.CHUNK_SIZE, chunkPos.y + Chunk.CHUNK_SIZE, chunkPos.z + Chunk.CHUNK_SIZE);
        
        // Early distance check - skip chunks that are very far away
        chunkCenter.set(chunkPos.x + Chunk.CHUNK_SIZE * 0.5f, 
                       chunkPos.y + Chunk.CHUNK_SIZE * 0.5f, 
                       chunkPos.z + Chunk.CHUNK_SIZE * 0.5f);
        
        float distanceSquared = cameraPos.distanceSquared(chunkCenter);
        float maxRenderDistanceSquared = (16 * Chunk.CHUNK_SIZE) * (16 * Chunk.CHUNK_SIZE); // 16 chunks max distance
        
        if (distanceSquared > maxRenderDistanceSquared) {
            return false;
        }
        
        // Skip empty chunks
        if (chunk.isEmpty()) {
            return false;
        }
        
        // Skip chunks without generated meshes
        if (!chunk.isMeshGenerated()) {
            return false;
        }
        
        // Frustum culling test
        return frustum.isAABBInside(chunkMin, chunkMax);
    }

    public void preloadInitialChunks(Vector3f playerPosition) {
        int playerChunkX = (int) Math.floor(playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(playerPosition.z / Chunk.CHUNK_SIZE);
        
        // Load a smaller area synchronously for immediate gameplay
        int initialLoadRadius = Math.min(3, renderDistance); // Load 3x3 area around player
        
        System.out.println("Preloading initial chunks...");
        long startTime = System.currentTimeMillis();
        
        for (int x = playerChunkX - initialLoadRadius; x <= playerChunkX + initialLoadRadius; x++) {
            for (int z = playerChunkZ - initialLoadRadius; z <= playerChunkZ + initialLoadRadius; z++) {
                String chunkKey = getChunkKey(x, z);
                if (!loadedChunks.containsKey(chunkKey)) {
                    // Create chunk synchronously
                    Vector3f position = new Vector3f(x * Chunk.CHUNK_SIZE, 0, z * Chunk.CHUNK_SIZE);
                    Chunk chunk = new Chunk(position, loader, atlas, noise);
                    loadedChunks.put(chunkKey, chunk);
                }
            }
        }
        
        // Update neighbors and generate meshes for initial chunks
        updateChunkNeighbors();
        
        long endTime = System.currentTimeMillis();
        System.out.println("Initial chunks loaded in " + (endTime - startTime) + "ms");
        
        // Now start loading the rest asynchronously
        isInitialLoad = false;
        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkZ = playerChunkZ;
        lastPlayerPosition.set(playerPosition);
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

        if (isInitialLoad) {
            loadInitialChunksSync(chunksToLoad, playerChunkX, playerChunkZ);
        } else {
            loadNewChunks(chunksToLoad);
        }

        unloadDistantChunks(playerChunkX, playerChunkZ);

        updateChunkNeighbors();
    }

    private void loadInitialChunksSync(Set<String> chunksToLoad, int playerChunkX, int playerChunkZ) {
        // Sort chunks by distance from player for priority loading
        List<String> sortedChunks = new ArrayList<>(chunksToLoad);
        sortedChunks.sort((a, b) -> {
            String[] coordsA = a.split(",");
            String[] coordsB = b.split(",");
            int chunkXA = Integer.parseInt(coordsA[0]);
            int chunkZA = Integer.parseInt(coordsA[1]);
            int chunkXB = Integer.parseInt(coordsB[0]);
            int chunkZB = Integer.parseInt(coordsB[1]);
            
            int distA = Math.max(Math.abs(chunkXA - playerChunkX), Math.abs(chunkZA - playerChunkZ));
            int distB = Math.max(Math.abs(chunkXB - playerChunkX), Math.abs(chunkZB - playerChunkZ));
            
            return Integer.compare(distA, distB);
        });

        // Load closest chunks synchronously, distant ones asynchronously
        int syncLoadRadius = 2; // Load chunks within 2 blocks synchronously
        
        for (String chunkKey : sortedChunks) {
            if (!loadedChunks.containsKey(chunkKey) && !pendingChunks.containsKey(chunkKey)) {
                String[] coords = chunkKey.split(",");
                int chunkX = Integer.parseInt(coords[0]);
                int chunkZ = Integer.parseInt(coords[1]);
                int distance = Math.max(Math.abs(chunkX - playerChunkX), Math.abs(chunkZ - playerChunkZ));
                
                if (distance <= syncLoadRadius) {
                    // Load synchronously for immediate availability
                    Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, 0, chunkZ * Chunk.CHUNK_SIZE);
                    Chunk chunk = new Chunk(position, loader, atlas, noise);
                    loadedChunks.put(chunkKey, chunk);
                } else {
                    // Load asynchronously for distant chunks
                    Future<Chunk> future = chunkGenerationExecutor.submit(() -> {
                        Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, 0, chunkZ * Chunk.CHUNK_SIZE);
                        return new Chunk(position, loader, atlas, noise);
                    });
                    pendingChunks.put(chunkKey, future);
                }
            }
        }
        
        // Check completed async chunks
        checkCompletedChunks();
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

        checkCompletedChunks();
    }

    private void checkCompletedChunks() {
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

    public String getCullingStats() {
        return String.format("Chunks - Total: %d, Rendered: %d, Culled: %d (%.1f%%)", 
                           totalChunks, renderedChunks, culledChunks, 
                           totalChunks > 0 ? (culledChunks * 100.0f / totalChunks) : 0.0f);
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
