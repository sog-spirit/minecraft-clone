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

    private int horizontalRenderDistance = 8;
    private int verticalRenderDistance = 4;
    private int maxChunkY = 16; 
    private int horizontalUnloadDistance = 10;
    private int verticalUnloadDistance = 6;
    private Vector3f lastPlayerPosition = new Vector3f();
    private boolean forceUpdate = true;
    private int lastPlayerChunkX = Integer.MAX_VALUE;
    private int lastPlayerChunkY = Integer.MAX_VALUE;
    private int lastPlayerChunkZ = Integer.MAX_VALUE;
    private boolean isInitialLoad = true;

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

    public void preloadInitialChunks(Vector3f playerPosition, Camera camera) {
        int playerChunkX = (int) Math.floor(playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkY = (int) Math.floor(playerPosition.y / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(playerPosition.z / Chunk.CHUNK_SIZE);

        float worldX = playerPosition.x;
        float worldZ = playerPosition.z;
        float height = 0;
        float amplitude = 1;
        float frequency = 0.01f;

        for (int i = 0; i < 4; i++) {
            height += noise.noise(worldX * frequency, worldZ * frequency) * amplitude;
            amplitude *= 0.5f;
            frequency *= 2.0f;
        }

        int terrainHeight = (int) ((height + 1) * 0.5f * 64.0f);
        terrainHeight = Math.max(1, Math.min(Chunk.CHUNK_SIZE - 1, terrainHeight));

        playerPosition.y = terrainHeight + 2.0f;
        camera.setPosition(playerPosition);

        int initialHorizontalRadius = Math.min(3, horizontalRenderDistance);
        int initialVerticalRadius = Math.min(2, verticalRenderDistance);

        System.out.println("Preloading initial chunks...");
        long startTime = System.currentTimeMillis();

        for (int x = playerChunkX - initialHorizontalRadius; x <= playerChunkX + initialHorizontalRadius; x++) {
            for (int y = Math.max(0, playerChunkY - initialVerticalRadius); y <= Math.min(maxChunkY - 1, playerChunkY + initialVerticalRadius); y++) {
                for (int z = playerChunkZ - initialHorizontalRadius; z <= playerChunkZ + initialHorizontalRadius; z++) {
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
        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkY = playerChunkY;
        lastPlayerChunkZ = playerChunkZ;
        lastPlayerPosition.set(playerPosition);
    }

    public void updateChunks(Vector3f playerPosition) {
        int playerChunkX = (int) Math.floor(playerPosition.x / Chunk.CHUNK_SIZE);
        int playerChunkY = (int) Math.floor(playerPosition.y / Chunk.CHUNK_SIZE);
        int playerChunkZ = (int) Math.floor(playerPosition.z / Chunk.CHUNK_SIZE);

        if (!forceUpdate && playerChunkX == lastPlayerChunkX && playerChunkY == lastPlayerChunkY && playerChunkZ == lastPlayerChunkZ) {
            return;
        }

        lastPlayerChunkX = playerChunkX;
        lastPlayerChunkY = playerChunkY;
        lastPlayerChunkZ = playerChunkZ;
        forceUpdate = false;
        lastPlayerPosition.set(playerPosition);

        Set<String> chunksToLoad = getChunksInRange(playerChunkX, playerChunkY, playerChunkZ);

        if (isInitialLoad) {
            loadInitialChunksSync(chunksToLoad, playerChunkX, playerChunkY, playerChunkZ);
        } else {
            loadNewChunks(chunksToLoad);
        }

        unloadDistantChunks(playerChunkX, playerChunkY, playerChunkZ);

        updateChunkNeighbors();
    }

    private void loadInitialChunksSync(Set<String> chunksToLoad, int playerChunkX, int playerChunkY, int playerChunkZ) {
        List<String> sortedChunks = new ArrayList<>(chunksToLoad);
        sortedChunks.sort((a, b) -> {
            String[] coordsA = a.split(",");
            String[] coordsB = b.split(",");
            int chunkXA = Integer.parseInt(coordsA[0]);
            int chunkYA = Integer.parseInt(coordsA[1]);
            int chunkZA = Integer.parseInt(coordsA[2]);
            int chunkXB = Integer.parseInt(coordsB[0]);
            int chunkYB = Integer.parseInt(coordsB[1]);
            int chunkZB = Integer.parseInt(coordsB[2]);
            int distA = Math.max(Math.max(Math.abs(chunkXA - playerChunkX), Math.abs(chunkYA - playerChunkY)), Math.abs(chunkZA - playerChunkZ));
            int distB = Math.max(Math.max(Math.abs(chunkXB - playerChunkX), Math.abs(chunkYB - playerChunkY)), Math.abs(chunkZB - playerChunkZ));
            return Integer.compare(distA, distB);
        });

        int syncHorizontalRadius = 2;
        int syncVerticalRadius = 1;

        for (String chunkKey : sortedChunks) {
            if (!loadedChunks.containsKey(chunkKey) && !pendingChunks.containsKey(chunkKey)) {
                String[] coords = chunkKey.split(",");
                int chunkX = Integer.parseInt(coords[0]);
                int chunkY = Integer.parseInt(coords[1]);
                int chunkZ = Integer.parseInt(coords[2]);
                int distX = Math.abs(chunkX - playerChunkX);
                int distY = Math.abs(chunkY - playerChunkY);
                int distZ = Math.abs(chunkZ - playerChunkZ);
                if (distX <= syncHorizontalRadius && distY <= syncVerticalRadius && distZ <= syncHorizontalRadius) {
                    Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, chunkY * Chunk.CHUNK_SIZE, chunkZ * Chunk.CHUNK_SIZE);
                    Chunk chunk = new Chunk(position, loader, atlas, noise);
                    loadedChunks.put(chunkKey, chunk);
                } else {
                    Future<Chunk> future = chunkGenerationExecutor.submit(() -> {
                        Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, chunkY * Chunk.CHUNK_SIZE, chunkZ * Chunk.CHUNK_SIZE);
                        return new Chunk(position, loader, atlas, noise);
                    });
                    pendingChunks.put(chunkKey, future);
                }
            }
        }
        checkCompletedChunks();
    }

    private Set<String> getChunksInRange(int centerX, int centerY, int centerZ) {
        Set<String> chunks = new HashSet<>();
        for (int x = centerX - horizontalRenderDistance; x <= centerX + horizontalRenderDistance; x++) {
            for (int y = Math.max(0, centerY - verticalRenderDistance); y <= Math.min(maxChunkY - 1, centerY + verticalRenderDistance); y++) {
                for (int z = centerZ - horizontalRenderDistance; z <= centerZ + horizontalRenderDistance; z++) {
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
                    String[] coords = chunkKey.split(",");
                    int chunkX = Integer.parseInt(coords[0]);
                    int chunkY = Integer.parseInt(coords[1]);
                    int chunkZ = Integer.parseInt(coords[2]);
                    Vector3f position = new Vector3f(chunkX * Chunk.CHUNK_SIZE, chunkY * Chunk.CHUNK_SIZE, chunkZ * Chunk.CHUNK_SIZE);
                    return new Chunk(position, loader, atlas, noise);
                });
                pendingChunks.put(chunkKey, future);
            }
        }
        checkCompletedChunks();
    }

    private void checkCompletedChunks() {
        List<String> completedChunks = new ArrayList<>();
        for (Map.Entry<String, Future<Chunk>> entry : pendingChunks.entrySet()) {
            if (entry.getValue().isDone()) {
                try {
                    Chunk chunk = entry.getValue().get();
                    loadedChunks.put(entry.getKey(), chunk);
                    completedChunks.add(entry.getKey());
                } catch (Exception e) {
                    System.err.println("Error generating chunk " + entry.getKey() + ": " + e.getMessage());
                    completedChunks.add(entry.getKey());
                }
            }
        }
        for (String key : completedChunks) {
            pendingChunks.remove(key);
        }
    }

    private void unloadDistantChunks(int playerChunkX, int playerChunkY, int playerChunkZ) {
        List<String> chunksToUnload = new ArrayList<>();
        for (String chunkKey : loadedChunks.keySet()) {
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkY = Integer.parseInt(coords[1]);
            int chunkZ = Integer.parseInt(coords[2]);
            if (Math.abs(chunkX - playerChunkX) > horizontalUnloadDistance ||
                Math.abs(chunkY - playerChunkY) > verticalUnloadDistance ||
                Math.abs(chunkZ - playerChunkZ) > horizontalUnloadDistance) {
                chunksToUnload.add(chunkKey);
            }
        }
        for (String chunkKey : chunksToUnload) {
            Chunk chunk = loadedChunks.remove(chunkKey);
            if (chunk != null) cleanupChunk(chunk);
        }

        List<String> pendingToCancel = new ArrayList<>();
        for (String chunkKey : pendingChunks.keySet()) {
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkY = Integer.parseInt(coords[1]);
            int chunkZ = Integer.parseInt(coords[2]);
            if (Math.abs(chunkX - playerChunkX) > horizontalUnloadDistance ||
                Math.abs(chunkY - playerChunkY) > verticalUnloadDistance ||
                Math.abs(chunkZ - playerChunkZ) > horizontalUnloadDistance) {
                pendingToCancel.add(chunkKey);
            }
        }
        for (String chunkKey : pendingToCancel) {
            Future<Chunk> future = pendingChunks.remove(chunkKey);
            if (future != null) future.cancel(true);
        }
    }

    private void updateChunkNeighbors() {
        for (Map.Entry<String, Chunk> entry : loadedChunks.entrySet()) {
            String chunkKey = entry.getKey();
            Chunk chunk = entry.getValue();
            String[] coords = chunkKey.split(",");
            int chunkX = Integer.parseInt(coords[0]);
            int chunkY = Integer.parseInt(coords[1]);
            int chunkZ = Integer.parseInt(coords[2]);
            chunk.setNeighbor(0, loadedChunks.get(getChunkKey(chunkX + 1, chunkY, chunkZ))); // +x
            chunk.setNeighbor(1, loadedChunks.get(getChunkKey(chunkX - 1, chunkY, chunkZ))); // -x
            chunk.setNeighbor(2, loadedChunks.get(getChunkKey(chunkX, chunkY, chunkZ + 1))); // +z
            chunk.setNeighbor(3, loadedChunks.get(getChunkKey(chunkX, chunkY, chunkZ - 1))); // -z
            chunk.setNeighbor(4, loadedChunks.get(getChunkKey(chunkX, chunkY + 1, chunkZ))); // +y
            chunk.setNeighbor(5, loadedChunks.get(getChunkKey(chunkX, chunkY - 1, chunkZ))); // -y
        }
        for (Chunk chunk : loadedChunks.values()) {
            if (chunk.needsMeshUpdate()) chunk.generateMesh();
        }
    }

    private String getChunkKey(int chunkX, int chunkY, int chunkZ) {
        return chunkX + "," + chunkY + "," + chunkZ;
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

    public void setRenderDistance(int horizontalDistance, int verticalDistance) {
        this.horizontalRenderDistance = horizontalDistance;
        this.verticalRenderDistance = verticalDistance;
        this.horizontalUnloadDistance = horizontalDistance + 2;
        this.verticalUnloadDistance = verticalDistance + 2;
        forceUpdate();
    }

    public int getHorizontalRenderDistance() {
        return horizontalRenderDistance;
    }

    public int getVerticalRenderDistance() {
        return verticalRenderDistance;
    }

    public Chunk getChunkAt(Vector3f worldPosition) {
        int chunkX = (int) Math.floor(worldPosition.x / Chunk.CHUNK_SIZE);
        int chunkY = (int) Math.floor(worldPosition.y / Chunk.CHUNK_SIZE);
        int chunkZ = (int) Math.floor(worldPosition.z / Chunk.CHUNK_SIZE);
        return loadedChunks.get(getChunkKey(chunkX, chunkY, chunkZ));
    }

    public boolean isChunkLoaded(int chunkX, int chunkY, int chunkZ) {
        return loadedChunks.containsKey(getChunkKey(chunkX, chunkY, chunkZ));
    }

    public String getLoadingStats() {
        return String.format("Loaded: %d, Pending: %d, H Render Distance: %d, V Render Distance: %d",
                loadedChunks.size(), pendingChunks.size(), horizontalRenderDistance, verticalRenderDistance);
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
