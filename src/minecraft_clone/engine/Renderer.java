package minecraft_clone.engine;

import minecraft_clone.world.Chunk;

import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

public class Renderer {
    public void renderChunk(Chunk chunk, Shader shader) {
        RawModel model = chunk.getModel();
        glBindVertexArray(model.getVertexArrayObjectID());
        glEnableVertexAttribArray(0); // Position
        glEnableVertexAttribArray(1); // Texture coords
        shader.loadModelMatrix(new Matrix4f().translate(chunk.getPosition()));
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
