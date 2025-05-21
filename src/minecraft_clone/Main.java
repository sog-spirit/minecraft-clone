package minecraft_clone;

import static org.lwjgl.glfw.GLFW.*;

import org.lwjgl.system.Configuration;

public class Main {
    public static void main(String[] args) {
        Configuration.DEBUG.set(true);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);

        Minecraft minecraft = new Minecraft();
        minecraft.init();

        float lastFrameTime = (float) glfwGetTime();
        float accumulatedTime = 0.0f;
        float fixedTimeStep = 1.0f / 64.0f;

        while (!glfwWindowShouldClose(minecraft.getWindow())) {
            float currentFrameTime = (float) glfwGetTime();
            float deltaTime = currentFrameTime - lastFrameTime;
            lastFrameTime = currentFrameTime;

            accumulatedTime += deltaTime;

            while (accumulatedTime >= fixedTimeStep) {
                glfwPollEvents();
                minecraft.update(fixedTimeStep);
                accumulatedTime -= fixedTimeStep;
            }

            minecraft.render();

            Thread.yield();
        }

        minecraft.cleanup();
    }
}
