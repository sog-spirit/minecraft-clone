package minecraft_clone;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.engine.BaseShader;
import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.InputManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.Shader;
import minecraft_clone.entity.Camera;
import minecraft_clone.hud.Crosshair;
import minecraft_clone.render.Texture;
import minecraft_clone.render.TextureAtlas;
import minecraft_clone.world.ChunkManager;

public class Minecraft implements BaseGame {
    private DisplayManager displayManager;
    private InputManager inputManager;
    private BaseLoader loader;
    private BaseShader chunkShader;
    private BaseShader crosshairShader;
    private Renderer renderer;
    private Camera camera;
    private TextureAtlas atlas;
    private Texture terrainTexture;
    private Texture iconsTexture;
    private ChunkManager chunkManager;
    private Crosshair crosshair;

    private long lastChunkUpdate = 0;
    private static final long CHUNK_UPDATE_INTERVAL = 100;

    public Minecraft() {
        displayManager = new DisplayManager();
        inputManager = new InputManager();
    }

    @Override
    public void init() {
        displayManager.createDisplay("Minecraft clone", 800, 600);
        inputManager.setupCallbacks(displayManager.getWindow());
        camera = new Camera();
        renderer = new Renderer(camera, displayManager);

        loader = new Loader();
        chunkShader = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
        crosshairShader = new Shader("shaders/crosshair_vertex.glsl", "shaders/crosshair_fragment.glsl");
        atlas = new TextureAtlas(256, 16);
        terrainTexture = new Texture("textures/terrain.png");
        iconsTexture = new Texture("textures/icons.png");
        chunkManager = new ChunkManager(loader, atlas);

        crosshair = new Crosshair(loader, atlas);
        crosshair.generateMesh(displayManager);

        chunkManager.preloadInitialChunks(camera.getPosition());
    }

    @Override
    public void update(float deltaTime) {
        camera.update(deltaTime, inputManager);
        chunkManager.cullChunks(camera, displayManager.getWidth(), displayManager.getHeight());

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastChunkUpdate > CHUNK_UPDATE_INTERVAL) {
            chunkManager.updateChunks(camera.getPosition());
            lastChunkUpdate = currentTime;
            // Debug output
            System.out.println("Chunk Status: " + chunkManager.getLoadingStats());
            System.out.println("Frustum culling status: " + chunkManager.getCullingStats());
        }
    }

    @Override
    public void render() {
        displayManager.clearDisplay();

        renderer.renderChunks(chunkManager, chunkShader, terrainTexture);

        renderer.renderCrosshair(crosshair, crosshairShader, iconsTexture);

        displayManager.updateDisplay();
    }

    @Override
    public void cleanup() {
        chunkManager.cleanup();
        chunkShader.cleanup();
        loader.cleanup();
        inputManager.freeInputCallbacks();
        displayManager.closeDisplay();
    }

    @Override
    public long getWindow() {
        return displayManager.getWindow();
    }
}
