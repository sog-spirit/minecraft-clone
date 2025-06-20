package minecraft_clone.render;

public class TextureAtlas {
    private final int tileCount;
    private final float tileSize;

    public TextureAtlas(int atlasSize, int tileSizePx) {
        this.tileCount = atlasSize / tileSizePx;
        this.tileSize = 1.0f / tileCount;
    }

    // Returns UVs for a tile (e.g., tileX = 1, tileY = 3)
    public float[] getUVCoords(int tileX, int tileY) {
        float u = tileX * tileSize;
        float v = 1.0f - (tileY + 1) * tileSize;

        return new float[]{
            u, v,
            u + tileSize, v,
            u + tileSize, v + tileSize,
            u, v + tileSize,
        };
    }
}
