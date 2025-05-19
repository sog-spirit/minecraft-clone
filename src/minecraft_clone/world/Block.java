package minecraft_clone.world;

import org.joml.Matrix4f;

import minecraft_clone.engine.RawModel;
import minecraft_clone.render.TextureAtlas;

public class Block {
    private final RawModel model;

    public static final float[] CUBE_VERTICES = {
         // Front face
            -0.5f, -0.5f,  0.5f,
             0.5f, -0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f,
            // Back face
            -0.5f, -0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            // Left face
            -0.5f,  0.5f,  0.5f,
            -0.5f,  0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f, -0.5f,
            -0.5f, -0.5f,  0.5f,
            -0.5f,  0.5f,  0.5f,
            // Right face
             0.5f,  0.5f,  0.5f,
             0.5f, -0.5f,  0.5f,
             0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
             0.5f,  0.5f,  0.5f,
            // Top face
            -0.5f,  0.5f,  0.5f,
             0.5f,  0.5f,  0.5f,
             0.5f,  0.5f, -0.5f,
             0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f, -0.5f,
            -0.5f,  0.5f,  0.5f,
            // Bottom face
            -0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
             0.5f, -0.5f, -0.5f,
             0.5f, -0.5f,  0.5f,
            -0.5f, -0.5f,  0.5f
    };

    public Block(RawModel model) {
        this.model = model;
    }

    public static float[] getCube(TextureAtlas atlas) {
        float[] topUVs    = atlas.getUVCoords(0, 0); // Grass top
        float[] sideUVs   = atlas.getUVCoords(1, 0); // Grass side
        float[] bottomUVs = atlas.getUVCoords(2, 0); // Dirt

        return new float[]{
                // FRONT
                -0.5f, -0.5f,  0.5f, sideUVs[0], sideUVs[1],
                 0.5f, -0.5f,  0.5f, sideUVs[2], sideUVs[3],
                 0.5f,  0.5f,  0.5f, sideUVs[4], sideUVs[5],
                -0.5f,  0.5f,  0.5f, sideUVs[6], sideUVs[7],

                // BACK
                -0.5f, -0.5f, -0.5f, sideUVs[0], sideUVs[1],
                -0.5f,  0.5f, -0.5f, sideUVs[6], sideUVs[7],
                 0.5f,  0.5f, -0.5f, sideUVs[4], sideUVs[5],
                 0.5f, -0.5f, -0.5f, sideUVs[2], sideUVs[3],

                // LEFT
                -0.5f, -0.5f, -0.5f, sideUVs[0], sideUVs[1],
                -0.5f, -0.5f,  0.5f, sideUVs[2], sideUVs[3],
                -0.5f,  0.5f,  0.5f, sideUVs[4], sideUVs[5],
                -0.5f,  0.5f, -0.5f, sideUVs[6], sideUVs[7],

                // RIGHT
                 0.5f, -0.5f, -0.5f, sideUVs[0], sideUVs[1],
                 0.5f,  0.5f, -0.5f, sideUVs[6], sideUVs[7],
                 0.5f,  0.5f,  0.5f, sideUVs[4], sideUVs[5],
                 0.5f, -0.5f,  0.5f, sideUVs[2], sideUVs[3],

                // TOP
                -0.5f,  0.5f,  0.5f, topUVs[0], topUVs[1],
                 0.5f,  0.5f,  0.5f, topUVs[2], topUVs[3],
                 0.5f,  0.5f, -0.5f, topUVs[4], topUVs[5],
                -0.5f,  0.5f, -0.5f, topUVs[6], topUVs[7],

                // BOTTOM
                -0.5f, -0.5f,  0.5f, bottomUVs[0], bottomUVs[1],
                -0.5f, -0.5f, -0.5f, bottomUVs[6], bottomUVs[7],
                 0.5f, -0.5f, -0.5f, bottomUVs[4], bottomUVs[5],
                 0.5f, -0.5f,  0.5f, bottomUVs[2], bottomUVs[3],
            };
    }

    public static int[] getIndices() {
        return new int[]{
             0,  1,  2, 2,  3,  0,  // Front
             4,  5,  6, 6,  7,  4,  // Back
             8,  9, 10,10, 11,  8,  // Left
            12, 13, 14,14, 15, 12,  // Right
            16, 17, 18,18, 19, 16,  // Top
            20, 21, 22,22, 23, 20   // Bottom
        };
    }

    public RawModel getModel() {
        return model;
    }

    public Matrix4f getModelMatrix() {
        return new Matrix4f().identity();
    }
}
