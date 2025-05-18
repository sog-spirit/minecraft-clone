package minecraft_clone.engine;

import minecraft_clone.world.Block;

import static org.lwjgl.opengl.GL30.*;

public class Renderer {
    public void render(Block block, ShaderProgram shader) {
        RawModel model = block.getModel();
        glBindVertexArray(model.getVertexArrayObjectID());
        glEnableVertexAttribArray(0);
        shader.loadModelMatrix(block.getModelMatrix());
        glDrawArrays(GL_TRIANGLES, 0, model.getVertexCount());
        glDisableVertexAttribArray(0);
        glBindVertexArray(0);
    }
}
