package minecraft_clone;

import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.Shader;
import minecraft_clone.entity.Camera;
import minecraft_clone.input.InputManager;
import minecraft_clone.render.Texture;
import minecraft_clone.render.TextureAtlas;
import minecraft_clone.world.Block;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL13.*;

public class Main {
    public static void main(String[] args) {
        DisplayManager.createDisplay("Minecraft clone", 800, 600);

        InputManager.setupCallbacks(DisplayManager.getWindow());
        Loader loader = new Loader();
        Shader shader = new Shader("shaders/vertex.glsl", "shaders/fragment.glsl");
        Renderer renderer = new Renderer();
        Camera camera = new Camera();
        TextureAtlas atlas = new TextureAtlas(256, 16);
        Texture texture = new Texture("textures/terrain.png");

        float[] vertices = Block.getCube(atlas);
        int[] indices = Block.getIndices();

//        RawModel cubeModel = loader.loadToVertexArrayObject(Block.CUBE_VERTICES);
        RawModel cubeModel = loader.loadToVertexArrayObject(vertices, indices, 5);
        Block block = new Block(cubeModel);

        float lastFrameTime = (float) glfwGetTime();

        while (!glfwWindowShouldClose(DisplayManager.getWindow())) {
            float currentFrameTime = (float) glfwGetTime();
            float deltaTime = currentFrameTime - lastFrameTime;
            lastFrameTime = currentFrameTime;

            DisplayManager.clearDisplay();

            float dx = InputManager.deltaX;
            float dy = InputManager.deltaY;

            camera.update(deltaTime, dx, dy);
            InputManager.resetDeltas();

            shader.start();
            shader.loadViewMatrix(camera.getViewMatrix());
            shader.loadProjectionMatrix(camera.getProjectionMatrix(800, 600));
            shader.loadTextureSampler();
            glActiveTexture(GL_TEXTURE0);
            texture.bind();
            renderer.render(block, shader);
            shader.stop();

            DisplayManager.updateDisplay();
        }

        shader.cleanup();
        loader.cleanup();
        DisplayManager.closeDisplay();
    }
}
