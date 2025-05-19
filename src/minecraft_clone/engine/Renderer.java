package minecraft_clone.engine;

import minecraft_clone.world.Block;

import static org.lwjgl.opengl.GL30.*;

import org.joml.Matrix4f;

public class Renderer {
    public void render(Block block, Shader shader) {
        RawModel model = block.getModel();
        glBindVertexArray(model.getVertexArrayObjectID());
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        shader.loadModelMatrix(new Matrix4f().translate(block.getPosition()));
        glDrawElements(GL_TRIANGLES, model.getVertexCount(), GL_UNSIGNED_INT, 0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
