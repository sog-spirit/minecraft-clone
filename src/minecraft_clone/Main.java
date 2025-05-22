package minecraft_clone;

import org.lwjgl.system.Configuration;

import minecraft_clone.engine.GameLoop;

public class Main {
    public static void main(String[] args) {
        Configuration.DEBUG.set(true);
        Configuration.DEBUG_MEMORY_ALLOCATOR.set(true);

        DefaultGame game = new Minecraft();
        game.init();

        GameLoop gameLoop = new GameLoop(game, 1.0f / 64.0f);
        gameLoop.run();

        game.cleanup();
    }
}
