package minecraft_clone.engine;

import minecraft_clone.entity.Camera;
import minecraft_clone.hud.Crosshair;
import minecraft_clone.render.Texture;
import minecraft_clone.world.Chunk;

import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Renderer {
    private final Camera camera;
    private final DisplayManager displayManager;

    public Renderer(Camera camera, DisplayManager displayManager) {
        this.camera = camera;
        this.displayManager = displayManager;
    }

    public void renderChunk(Chunk chunk, BaseShader shader, Texture texture) {
        shader.start();
        shader.loadUniformMatrix4f("view", camera.getViewMatrix());
        shader.loadUniformMatrix4f("projection", camera.getProjectionMatrix(displayManager.getWidth(), displayManager.getHeight()));
        shader.loadUniformInt("textureSampler", 0);
        texture.bind();

        glActiveTexture(GL_TEXTURE0);
        RawModel model = chunk.getModel();
        glBindVertexArray(model.getVertexArrayObjectID());
        glEnableVertexAttribArray(0); // Position
        glEnableVertexAttribArray(1); // Texture coords
        shader.loadUniformMatrix4f("model", new Matrix4f().translate(chunk.getPosition()));
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);

        texture.unbind();
        shader.stop();
    }

    public void renderCrosshair(Crosshair crosshair, BaseShader shader, Texture texture) {
        glDisable(GL_DEPTH_TEST); // Draw overlay on top
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glActiveTexture(GL_TEXTURE0);
        RawModel model = crosshair.getModel();
        texture.bind();
        shader.start();
        shader.loadUniformVector2f("screenSize", new Vector2f(displayManager.getWidth(), displayManager.getHeight()));

        glBindVertexArray(model.getVertexArrayObjectID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glBindVertexArray(0);

        shader.stop();
        glDisable(GL_BLEND);
        glEnable(GL_DEPTH_TEST);
    }
}
