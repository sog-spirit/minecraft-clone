package minecraft_clone.world;

public class BlockProperties {
    public final String name;
    public final boolean isSolid;
    public final boolean isTransparent;
    public final int[] textureTop;
    public final int[] textureFront;
    public final int[] textureBack;
    public final int[] textureLeft;
    public final int[] textureRight;
    public final int[] textureBottom;

    // Constructor for blocks with same texture on all sides
    public BlockProperties(String name, boolean solid, boolean transparent, int[] top, int[] side, int[] bottom) {
        this.name = name;
        this.isSolid = solid;
        this.isTransparent = transparent;
        this.textureTop = top;
        this.textureFront = side;
        this.textureBack = side;
        this.textureLeft = side;
        this.textureRight = side;
        this.textureBottom = bottom;
    }

    // Constructor for blocks with different textures on each side
    public BlockProperties(String name, boolean solid, boolean transparent, int[] top, int[] front, int[] back, int[] left, int[] right, int[] bottom) {
        this.name = name;
        this.isSolid = solid;
        this.isTransparent = transparent;
        this.textureTop = top;
        this.textureFront = front;
        this.textureBack = back;
        this.textureLeft = left;
        this.textureRight = right;
        this.textureBottom = bottom;
    }

    // Constructor for blocks with separate front/back and left/right textures
    public BlockProperties(String name, boolean solid, boolean transparent, int[] top, int[] frontBack, int[] leftRight, int[] bottom) {
        this.name = name;
        this.isSolid = solid;
        this.isTransparent = transparent;
        this.textureTop = top;
        this.textureFront = frontBack;
        this.textureBack = frontBack;
        this.textureLeft = leftRight;
        this.textureRight = leftRight;
        this.textureBottom = bottom;
    }
}
