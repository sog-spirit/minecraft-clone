package minecraft_clone;

public interface BaseGame {
    void init();
    void update(float deltaTime);
    void render();
    void cleanup();
    long getWindow();
}
