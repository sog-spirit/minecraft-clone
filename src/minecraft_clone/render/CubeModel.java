package minecraft_clone.render;

import minecraft_clone.world.BlockProperties;
import minecraft_clone.world.BlockRegistry;
import minecraft_clone.world.BlockType;

public class CubeModel {
    public static float[] getCube(TextureAtlas atlas, BlockType type) {
        BlockProperties blockProperties = BlockRegistry.get(type);
        float[] topUVs    = atlas.getUVCoords(blockProperties.textureTop[0], blockProperties.textureTop[1]);
        float[] frontUVs  = atlas.getUVCoords(blockProperties.textureFront[0], blockProperties.textureFront[1]);
        float[] backUVs   = atlas.getUVCoords(blockProperties.textureBack[0], blockProperties.textureBack[1]);
        float[] leftUVs   = atlas.getUVCoords(blockProperties.textureLeft[0], blockProperties.textureLeft[1]);
        float[] rightUVs  = atlas.getUVCoords(blockProperties.textureRight[0], blockProperties.textureRight[1]);
        float[] bottomUVs = atlas.getUVCoords(blockProperties.textureBottom[0], blockProperties.textureBottom[1]);

        return new float[]{
                // FRONT
                -0.5f, -0.5f,  0.5f, frontUVs[0], frontUVs[1],
                 0.5f, -0.5f,  0.5f, frontUVs[2], frontUVs[3],
                 0.5f,  0.5f,  0.5f, frontUVs[4], frontUVs[5],
                -0.5f,  0.5f,  0.5f, frontUVs[6], frontUVs[7],

                // BACK
                -0.5f, -0.5f, -0.5f, backUVs[0], backUVs[1],
                -0.5f,  0.5f, -0.5f, backUVs[6], backUVs[7],
                 0.5f,  0.5f, -0.5f, backUVs[4], backUVs[5],
                 0.5f, -0.5f, -0.5f, backUVs[2], backUVs[3],

                // LEFT
                -0.5f, -0.5f, -0.5f, leftUVs[0], leftUVs[1],
                -0.5f, -0.5f,  0.5f, leftUVs[2], leftUVs[3],
                -0.5f,  0.5f,  0.5f, leftUVs[4], leftUVs[5],
                -0.5f,  0.5f, -0.5f, leftUVs[6], leftUVs[7],

                // RIGHT
                 0.5f, -0.5f, -0.5f, rightUVs[0], rightUVs[1],
                 0.5f,  0.5f, -0.5f, rightUVs[6], rightUVs[7],
                 0.5f,  0.5f,  0.5f, rightUVs[4], rightUVs[5],
                 0.5f, -0.5f,  0.5f, rightUVs[2], rightUVs[3],

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
}
