package minecraft_clone.hud;

import minecraft_clone.engine.BaseLoader;
import minecraft_clone.engine.DisplayManager;
import minecraft_clone.engine.RawModel;
import minecraft_clone.render.TextureAtlas;

public class Crosshair {
    private RawModel model;
    private BaseLoader loader;
    TextureAtlas atlas;

    public Crosshair(BaseLoader loader, TextureAtlas atlas) {
        this.loader = loader;
        this.atlas = atlas;
    }

    public void generateMesh(DisplayManager displayManager) {
        float centerX = displayManager.getWidth() / 2.0f;
        float centerY = displayManager.getHeight() / 2.0f;
        // Define a 16x16 pixel quad centered at (centerX, centerY)
        float left = centerX - 8.0f;
        float right = centerX + 8.0f;
        float top = centerY - 8.0f;
        float bottom = centerY + 8.0f;

        float[] uvCoords = atlas.getUVCoords(0, 0);

        float[] vertices = {
                left, top, 0.0f, uvCoords[0], uvCoords[1],         // Top-left
                right, top, 0.0f, uvCoords[2], uvCoords[3],       // Top-right
                right, bottom, 0.0f, uvCoords[4], uvCoords[5],    // Bottom-right
                left, bottom, 0.0f, uvCoords[6], uvCoords[7]      // Bottom-left
        };

        int[] indices = {
            0, 1, 2,
            2, 3, 0
        };
        model = loader.loadTo2DVertexArrayObject(vertices, indices, 5); // (vertexSize=5: x, y, z, u, v)
    }

    public RawModel getModel() {
        return model;
    }
}
