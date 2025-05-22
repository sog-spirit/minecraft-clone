package minecraft_clone;

public interface DefaultGame {
    void init();
    void update(float deltaTime);
    void render();
    void cleanup();
    long getWindow();
}
