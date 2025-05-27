package minecraft_clone.world;

import java.util.ArrayList;
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
import org.joml.Vector3i;

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

    private int renderDistance = 8; // Chunks to keep loaded around player (horizontal)
    private int verticalRenderDistance = 4; // Chunks to keep loaded above/below player
    private int unloadDistance = 10; // Distance at which to unload chunks
    private int verticalUnloadDistance = 6; // Vertical distance at which to unload chunks

    private Vector3f lastPlayerPosition = new Vector3f();
    private boolean forceUpdate = true;
    private Vector3i lastPlayerChunk = new Vector3i(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private boolean isInitialLoad = true;

    private static final int MIN_WORLD_Y = -64;
    private static final int MAX_WORLD_Y = 320;
    private static final int MIN_CHUNK_Y = MIN_WORLD_Y / Chunk.CHUNK_SIZE;
    private static final int MAX_CHUNK_Y = MAX_WORLD_Y / Chunk.CHUNK_SIZE;

    private final Frustum frustum;
    private final Vector3f chunkMin = new Vector3f();
    private final Vector3f chunkMax = new Vector3f();
    private final Vector3f chunkCenter = new Vector3f();

    private List<Chunk> visibleOpaqueChunks = new ArrayList<>();
    private List<Chunk> visibleTransparentChunks = new ArrayList<>();

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

        visibleOpaqueChunks.clear();
        visibleTransparentChunks.clear();

        totalChunks = loadedChunks.size();
        culledChunks = 0;
        renderedChunks = 0;

        for (Chunk chunk : loadedChunks.values()) {
            if (isChunkVisible(chunk, camera.getPosition())) {
                if (chunk.isMeshGenerated()) {
                    if (chunk.getOpaqueModel() != null) {
                        visibleOpaqueChunks.add(chunk);
                    }
                    if (chunk.getTransparentModel() != null) {
                        visibleTransparentChunks.add(chunk);
                    }
                }
                renderedChunks++;
            } else {
                culledChunks++;
            }
        }

        if (!visibleTransparentChunks.isEmpty()) {
            final Vector3f finalCameraPos = new Vector3f(camera.getPosition());
            visibleTransparentChunks.sort((a, b) -> {
                float distA = a.getPosition().distanceSquared(finalCameraPos);
                float distB = b.getPosition().distanceSquared(finalCameraPos);
                return Float.compare(distB, distA); // Reverse order (farthest first)
            });
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
        Vector3i playerChunk = getChunkCoordinates(playerPosition);

        // Load a smaller area synchronously for immediate gameplay
        int initialLoadRadius = Math.min(2, renderDistance);
        int initialVerticalRadius = Math.min(2, verticalRenderDistance);
        
        System.out.println("Preloading initial chunks...");
        long startTime = System.currentTimeMillis();
        
        for (int x = playerChunk.x - initialLoadRadius; x <= playerChunk.x + initialLoadRadius; x++) {
            for (int z = playerChunk.z - initialLoadRadius; z <= playerChunk.z + initialLoadRadius; z++) {
                for (int y = Math.max(MIN_CHUNK_Y, playerChunk.y - initialVerticalRadius); 
                     y <= Math.min(MAX_CHUNK_Y, playerChunk.y + initialVerticalRadius); y++) {
                    String chunkKey = getChunkKey(x, y, z);
                    if (!loadedChunks.containsKey(chunkKey)) {
                        Vector3f position = new Vector3f(x * Chunk.CHUNK_SIZE, y * Chunk.CHUNK_SIZE, z * Chunk.CHUNK_SIZE);
                        Chunk chunk = new Chunk(position, loader, atlas, noise);
                        loadedChunks.put(chunkKey, chunk);
                    }
                }
            }
        }

        // Update neighbors and generate meshes for initial chunks
        updateChunkNeighbors();

        long endTime = System.currentTimeMillis();
        System.out.println("Initial chunks loaded in " + (endTime - startTime) + "ms");
        
        // Now start loading the rest asynchronously
        isInitialLoad = false;
        lastPlayerChunk.set(playerChunk);
        lastPlayerPosition.set(playerPosition);
    }

    public void updateChunks(Vector3f playerPosition) {
        Vector3i playerChunk = getChunkCoordinates(playerPosition);

        if (!forceUpdate && playerChunk.equals(lastPlayerChunk)) {
            return;
        }

        lastPlayerChunk.set(playerChunk);
        forceUpdate = false;
        lastPlayerPosition.set(playerPosition);

        Set<String> chunksToLoad = getChunksInRange(playerChunk.x, playerChunk.y, playerChunk.z, renderDistance, verticalRenderDistance);

        if (isInitialLoad) {
            loadInitialChunksSync(chunksToLoad, playerChunk);
        } else {
            loadNewChunks(chunksToLoad);
        }

        unloadDistantChunks(playerChunk);

        updateChunkNeighbors();
    }

    private Vector3i getChunkCoordinates(Vector3f worldPosition) {
        return new Vector3i(
            (int) Math.floor(worldPosition.x / Chunk.CHUNK_SIZE),
            (int) Math.floor(worldPosition.y / Chunk.CHUNK_SIZE),
            (int) Math.floor(worldPosition.z / Chunk.CHUNK_SIZE)
        );
    }

    private void loadInitialChunksSync(Set<String> chunksToLoad, Vector3i playerChunk) {
        // Sort chunks by distance from player for priority loading
        List<String> sortedChunks = new ArrayList<>(chunksToLoad);
        sortedChunks.sort((a, b) -> {
            Vector3i coordsA = parseChunkKey(a);
            Vector3i coordsB = parseChunkKey(b);

            int distA = Math.max(Math.max(Math.abs(coordsA.x - playerChunk.x), Math.abs(coordsA.z - playerChunk.z)), Math.abs(coordsA.y - playerChunk.y));
            int distB = Math.max(Math.max(Math.abs(coordsB.x - playerChunk.x), Math.abs(coordsB.z - playerChunk.z)), Math.abs(coordsB.y - playerChunk.y));

            return Integer.compare(distA, distB);
        });

        // Load closest chunks synchronously, distant ones asynchronously
        int syncLoadRadius = 1; // Load chunks within 2 blocks synchronously

        for (String chunkKey : sortedChunks) {
            if (!loadedChunks.containsKey(chunkKey) && !pendingChunks.containsKey(chunkKey)) {
                Vector3i coords = parseChunkKey(chunkKey);
                int distance = Math.max(Math.max(Math.abs(coords.x - playerChunk.x), Math.abs(coords.z - playerChunk.z)), Math.abs(coords.y - playerChunk.y));
                
                if (distance <= syncLoadRadius) {
                    Vector3f position = new Vector3f(coords.x * Chunk.CHUNK_SIZE, coords.y * Chunk.CHUNK_SIZE, coords.z * Chunk.CHUNK_SIZE);
                    Chunk chunk = new Chunk(position, loader, atlas, noise);
                    loadedChunks.put(chunkKey, chunk);
                } else {
                    Future<Chunk> future = chunkGenerationExecutor.submit(() -> {
                        Vector3f position = new Vector3f(coords.x * Chunk.CHUNK_SIZE, coords.y * Chunk.CHUNK_SIZE, coords.z * Chunk.CHUNK_SIZE);
                        return new Chunk(position, loader, atlas, noise);
                    });
                    pendingChunks.put(chunkKey, future);
                }
            }
        }

        // Check completed async chunks
        checkCompletedChunks();
    }

    private Set<String> getChunksInRange(int centerX, int centerY, int centerZ, int horizontalRange, int verticalRange) {
        Set<String> chunks = new HashSet<>();
        for (int x = centerX - horizontalRange; x <= centerX + horizontalRange; x++) {
            for (int z = centerZ - horizontalRange; z <= centerZ + horizontalRange; z++) {
                for (int y = Math.max(MIN_CHUNK_Y, centerY - verticalRange); 
                     y <= Math.min(MAX_CHUNK_Y, centerY + verticalRange); y++) {
                    chunks.add(getChunkKey(x, y, z));
                }
            }
        }
        return chunks;
    }

    private void loadNewChunks(Set<String> chunksToLoad) {
        for (String chunkKey : chunksToLoad) {
            if (!loadedChunks.containsKey(chunkKey) && !pendingChunks.containsKey(chunkKey)) {
                Future<Chunk> future = chunkGenerationExecutor.submit(() -> {
                    Vector3i coords = parseChunkKey(chunkKey);
                    Vector3f position = new Vector3f(coords.x * Chunk.CHUNK_SIZE, coords.y * Chunk.CHUNK_SIZE, coords.z * Chunk.CHUNK_SIZE);
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

    private void unloadDistantChunks(Vector3i playerChunk) {
        List<String> chunksToUnload = new ArrayList<>();

        for (String chunkKey : loadedChunks.keySet()) {
            Vector3i coords = parseChunkKey(chunkKey);

            int horizontalDistance = Math.max(Math.abs(coords.x - playerChunk.x), Math.abs(coords.z - playerChunk.z));
            int verticalDistance = Math.abs(coords.y - playerChunk.y);

            if (horizontalDistance > unloadDistance || verticalDistance > verticalUnloadDistance) {
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
            Vector3i coords = parseChunkKey(chunkKey);

            int horizontalDistance = Math.max(Math.abs(coords.x - playerChunk.x), Math.abs(coords.z - playerChunk.z));
            int verticalDistance = Math.abs(coords.y - playerChunk.y);

            if (horizontalDistance > unloadDistance || verticalDistance > verticalUnloadDistance) {
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
            Vector3i coords = parseChunkKey(chunkKey);

            // Set neighbors: 0 (+x), 1 (-x), 2 (+z), 3 (-z)
            chunk.setNeighbor(0, loadedChunks.get(getChunkKey(coords.x + 1, coords.y, coords.z))); // +x
            chunk.setNeighbor(1, loadedChunks.get(getChunkKey(coords.x - 1, coords.y, coords.z))); // -x
            chunk.setNeighbor(2, loadedChunks.get(getChunkKey(coords.x, coords.y + 1, coords.z))); // +y
            chunk.setNeighbor(3, loadedChunks.get(getChunkKey(coords.x, coords.y - 1, coords.z))); // -y
            chunk.setNeighbor(4, loadedChunks.get(getChunkKey(coords.x, coords.y, coords.z + 1))); // +z
            chunk.setNeighbor(5, loadedChunks.get(getChunkKey(coords.x, coords.y, coords.z - 1))); // -z
        }

        // Generate meshes for chunks that need updates
        for (Chunk chunk : loadedChunks.values()) {
            if (chunk.needsMeshUpdate()) {
                chunk.generateMesh();
            }
        }
    }

    private String getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return chunkX + "," + chunkY + "," + chunkZ;
    }

    private Vector3i parseChunkKey(String chunkKey) {
        String[] coords = chunkKey.split(",");
        return new Vector3i(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]));
    }

    private void cleanupChunk(Chunk chunk) {
        chunk.cleanup();
    }

    public void forceUpdate() {
        this.forceUpdate = true;
    }

    public List<Chunk> getVisibleOpaqueChunks() {
        return visibleOpaqueChunks;
    }

    public List<Chunk> getVisibleTransparentChunks() {
        return visibleTransparentChunks;
    }

    public void setRenderDistance(int distance) {
        this.renderDistance = distance;
        this.unloadDistance = distance + 2;
        forceUpdate();
    }

    public void setVerticalRenderDistance(int distance) {
        this.verticalRenderDistance = Math.max(1, Math.min(distance, (MAX_CHUNK_Y - MIN_CHUNK_Y) / 2));
        this.verticalUnloadDistance = verticalRenderDistance + 2;
        forceUpdate();
    }

    public int getRenderDistance() {
        return renderDistance;
    }

    public int getVerticalRenderDistance() {
        return verticalRenderDistance;
    }

    public Chunk getChunkAt(Vector3f worldPosition) {
        Vector3i chunkCoords = getChunkCoordinates(worldPosition);
        return loadedChunks.get(getChunkKey(chunkCoords.x, chunkCoords.y, chunkCoords.z));
    }

    public boolean isChunkLoaded(int chunkX, int chunkY, int chunkZ) {
        return loadedChunks.containsKey(getChunkKey(chunkX, chunkY, chunkZ));
    }

    public String getLoadingStats() {
        return String.format("Loaded: %d, Pending: %d, Render Distance: %d", loadedChunks.size(), pendingChunks.size(), renderDistance);
    }

    public String getCullingStats() {
        return String.format("Chunks - Total: %d, Rendered: %d, Culled: %d (%.1f%%)", 
                           totalChunks, renderedChunks, culledChunks, 
                           totalChunks > 0 ? (culledChunks * 100.0f / totalChunks) : 0.0f);
    }

    public static int getMinWorldY() {
        return MIN_WORLD_Y;
    }

    public static int getMaxWorldY() {
        return MAX_WORLD_Y;
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
