package minecraft_clone;

import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.Loader;
import minecraft_clone.engine.RawModel;
import minecraft_clone.engine.Renderer;
import minecraft_clone.engine.ShaderProgram;
import minecraft_clone.entity.Camera;
import minecraft_clone.world.Block;

import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;

public class Main {
    public static void main(String[] args) {
        DisplayManager.createDisplay("Minecraft clone", 800, 600);

        Loader loader = new Loader();
        ShaderProgram shader = new ShaderProgram("shaders/vertex.glsl", "shaders/fragment.glsl");
        Renderer renderer = new Renderer();
        Camera camera = new Camera();

        RawModel cubeModel = loader.loadToVertexArrayObject(Block.CUBE_VERTICES);
        Block block = new Block(cubeModel);

        while (!glfwWindowShouldClose(DisplayManager.getWindow())) {
            DisplayManager.clearDisplay();
            camera.update();
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
