package minecraft_clone;

import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.ShaderProgram;
import minecraft_clone.entity.Camera;
import minecraft_clone.input.InputManager;
import minecraft_clone.world.Block;

import static org.lwjgl.glfw.GLFW.*;

public class Main {
    public static void main(String[] args) {
        DisplayManager.createDisplay("Minecraft clone", 800, 600);

        InputManager.setupCallbacks(DisplayManager.getWindow());
        Loader loader = new Loader();
        ShaderProgram shader = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        Renderer renderer = new Renderer();
        Camera camera = new Camera();

        RawModel cubeModel = loader.loadToVertexArrayObject(Block.CUBE_VERTICES);
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
            renderer.render(block, shader);
            shader.stop();

            DisplayManager.updateDisplay();
        }

        shader.cleanup();
        loader.cleanup();
        DisplayManager.closeDisplay();
    }
}
