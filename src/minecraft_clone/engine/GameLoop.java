package minecraft_clone.engine;

import minecraft_clone.DefaultGame;

import static org.lwjgl.glfw.GLFW.*;

public class GameLoop {
    private final DefaultGame game;
    private final float fixedTimeStep;

    public GameLoop(DefaultGame game, float fixedTimeStep) {
        this.game = game;
        this.fixedTimeStep = fixedTimeStep;
    }

    public void run() {
        float lastFrameTime = (float) glfwGetTime();
        float accumulatedTime = 0.0f;

        while (!glfwWindowShouldClose(game.getWindow())) {
            float currentFrameTime = (float) glfwGetTime();
            float deltaTime = currentFrameTime - lastFrameTime;
            lastFrameTime = currentFrameTime;

            accumulatedTime += deltaTime;

            while (accumulatedTime >= fixedTimeStep) {
                glfwPollEvents();
                game.update(fixedTimeStep);
                accumulatedTime -= fixedTimeStep;
            }

            game.render();

            Thread.yield();
        }
    }
}
