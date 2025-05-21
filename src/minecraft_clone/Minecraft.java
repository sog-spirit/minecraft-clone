package minecraft_clone;

import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.InputManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.Shader;
import minecraft_clone.entity.Camera;
import minecraft_clone.render.Texture;
import minecraft_clone.render.TextureAtlas;
import minecraft_clone.world.Chunk;
import minecraft_clone.world.ChunkManager;

import static org.lwjgl.opengl.GL13.*;

import java.util.ArrayList;
import java.util.List;

public class Minecraft {
    private DisplayManager displayManager;
    private InputManager inputManager;
    private Loader loader;
    private Shader shader;
    private Renderer renderer;
    private Camera camera;
    private TextureAtlas atlas;
    private Texture texture;
    private List<Chunk> chunks = new ArrayList<>();
    private ChunkManager chunkManager;

    public Minecraft() {
        displayManager = new DisplayManager();
        inputManager = new InputManager();
    }

    public void init() {
        displayManager.createDisplay("Minecraft clone", 800, 600);
        inputManager.setupCallbacks(displayManager.getWindow());

        loader = new Loader();
        shader = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
        renderer = new Renderer();
        camera = new Camera();
        atlas = new TextureAtlas(256, 16);
        texture = new Texture("textures/terrain.png");
        chunkManager = new ChunkManager(loader, atlas);

        chunkManager.generateChunks(1);
        chunks.addAll(chunkManager.getChunks().values());
    }

    public void update(float deltaTime) {
        inputManager.updateCamera(camera, deltaTime);
    }

    public void render() {
        displayManager.clearDisplay();

        shader.start();
        shader.loadViewMatrix(camera.getViewMatrix());
        shader.loadProjectionMatrix(camera.getProjectionMatrix(displayManager.getWidth(), displayManager.getHeight()));
        shader.loadTextureSampler();
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        for (Chunk chunk : chunks) {
            renderer.renderChunk(chunk, shader);
        }
        shader.stop();

        displayManager.updateDisplay();
    }

    public void cleanup() {
        shader.cleanup();
        loader.cleanup();
        inputManager.freeInputCallbacks();
        displayManager.closeDisplay();
    }

    public long getWindow() {
        return displayManager.getWindow();
    }
}
