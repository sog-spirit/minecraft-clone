package minecraft_clone.engine;

import minecraft_clone.entity.Camera;
import minecraft_clone.hud.Crosshair;
import minecraft_clone.render.Texture;
import minecraft_clone.world.Chunk;
import minecraft_clone.world.ChunkManager;

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

    public void renderChunks(ChunkManager chunkManager, BaseShader shader, Texture texture) {
        shader.start();
        shader.loadUniformMatrix4f("view", camera.getViewMatrix());
        shader.loadUniformMatrix4f("projection", camera.getProjectionMatrix(displayManager.getWidth(), displayManager.getHeight()));
        shader.loadUniformInt("textureSampler", 0);
        texture.bind();
        glActiveTexture(GL_TEXTURE0);

        // First pass: Render all opaque blocks
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glDisable(GL_BLEND);
        glDepthMask(true);
        for (Chunk chunk : chunkManager.getVisibleOpaqueChunks()) {
            renderChunkOpaque(chunk, shader);
        }

        // Second pass: Render all transparent blocks
        glDisable(GL_CULL_FACE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glDepthMask(false); // Don't write to depth buffer for transparent objects

        for (Chunk chunk : chunkManager.getVisibleTransparentChunks()) {
            renderChunkTransparent(chunk, shader);
        }

        // Restore state
        glDepthMask(true);
        glDisable(GL_BLEND);
        
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

    private void renderChunkOpaque(Chunk chunk, BaseShader shader) {
        RawModel opaqueModel = chunk.getOpaqueModel();
        if (opaqueModel != null && opaqueModel.getVertexCount() > 0) {
            shader.loadUniformMatrix4f("model", new Matrix4f().translate(chunk.getPosition()));

            glBindVertexArray(opaqueModel.getVertexArrayObjectID());
            glEnableVertexAttribArray(0); // Position
            glEnableVertexAttribArray(1); // Texture coords
            glEnableVertexAttribArray(2); // Color
            glDrawElements(GL_TRIANGLES, opaqueModel.getVertexCount(), GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        }
    }

    private void renderChunkTransparent(Chunk chunk, BaseShader shader) {
        RawModel transparentModel = chunk.getTransparentModel();
        if (transparentModel != null && transparentModel.getVertexCount() > 0) {
            shader.loadUniformMatrix4f("model", new Matrix4f().translate(chunk.getPosition()));

            glBindVertexArray(transparentModel.getVertexArrayObjectID());
            glEnableVertexAttribArray(0); // Position
            glEnableVertexAttribArray(1); // Texture coords
            glEnableVertexAttribArray(2); // Color
            glDrawElements(GL_TRIANGLES, transparentModel.getVertexCount(), GL_UNSIGNED_INT, 0);
            glDisableVertexAttribArray(2);
            glDisableVertexAttribArray(1);
            glDisableVertexAttribArray(0);
            glBindVertexArray(0);
        }
    }
}
