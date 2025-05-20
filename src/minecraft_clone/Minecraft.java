package minecraft_clone;

import org.joml.Vector3f;

import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.InputManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.Shader;
import minecraft_clone.entity.Camera;
import minecraft_clone.render.CubeModel;
import minecraft_clone.render.Texture;
import minecraft_clone.render.TextureAtlas;
import minecraft_clone.world.Block;
import minecraft_clone.world.BlockType;

import static org.lwjgl.opengl.GL13.*;

public class Minecraft {
    private DisplayManager displayManager;
    private InputManager inputManager;
    private Loader loader;
    private Shader shader;
    private Renderer renderer;
    private Camera camera;
    private TextureAtlas atlas;
    private Texture texture;
    private Block block;
    private RawModel cubeModel;

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
        block = new Block(new Vector3f(0, 0, 0), BlockType.STONE);

        float[] vertices = CubeModel.getCube(atlas, block.getType());
        int[] indices = CubeModel.getIndices();
        cubeModel = loader.loadToVertexArrayObject(vertices, indices, 5);
    }

    public void update(float deltaTime) {
        float dx = InputManager.deltaX;
        float dy = InputManager.deltaY;
        camera.update(deltaTime, dx, dy);
        inputManager.resetDeltas();
    }

    public void render() {
        displayManager.clearDisplay();

        shader.start();
        shader.loadViewMatrix(camera.getViewMatrix());
        shader.loadProjectionMatrix(camera.getProjectionMatrix(displayManager.getWidth(), displayManager.getHeight()));
        shader.loadTextureSampler();
        glActiveTexture(GL_TEXTURE0);
        texture.bind();
        renderer.renderBlock(cubeModel, shader, block);
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
