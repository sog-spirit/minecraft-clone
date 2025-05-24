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
import minecraft_clone.world.Chunk;
import minecraft_clone.world.ChunkManager;

import java.util.ArrayList;
import java.util.List;

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
    private List<Chunk> chunks = new ArrayList<>();
    private ChunkManager chunkManager;
    private Crosshair crosshair;

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
        chunkManager.generateChunks(1);
        chunks.addAll(chunkManager.getChunks().values());

        crosshair = new Crosshair(loader, atlas);
        crosshair.generateMesh(displayManager);
    }

    @Override
    public void update(float deltaTime) {
        camera.update(deltaTime, inputManager);
    }

    @Override
    public void render() {
        displayManager.clearDisplay();

        for (Chunk chunk : chunks) {
            renderer.renderChunk(chunk, chunkShader, terrainTexture);
        }

        renderer.renderCrosshair(crosshair, crosshairShader, iconsTexture);

        displayManager.updateDisplay();
    }

    @Override
    public void cleanup() {
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
