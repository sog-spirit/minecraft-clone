package minecraft_clone.world;

public class BlockProperties {
    public final String name;
    public final boolean isSolid;
    public final boolean isTransparent;
    public final int[] textureTop;
    public final int[] textureSide;
    public final int[] textureBottom;

    public BlockProperties(String name, boolean solid, boolean transparent, int[] top, int[] side, int[] bottom) {
        this.name = name;
        this.isSolid = solid;
        this.isTransparent = transparent;
        this.textureTop = top;
        this.textureSide = side;
        this.textureBottom = bottom;
    }
}
