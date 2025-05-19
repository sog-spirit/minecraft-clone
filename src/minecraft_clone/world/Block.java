package minecraft_clone.world;

import org.joml.Vector3f;

public class Block {
    private final Vector3f position;
    private final BlockType type;
    private final BlockProperties properties;

    public Block(Vector3f position, BlockType type) {
        this.position = position;
        this.type = type;
        this.properties = BlockRegistry.get(type);
    }

    public Vector3f getPosition() {
        return position;
    }

    public BlockType getType() {
        return type;
    }

    public String getName() {
        return properties.name;
    }

    public boolean isSolid() {
        return properties.isSolid;
    }

    public boolean isTransparent() {
        return properties.isTransparent;
    }

    public int[] getTopTextureId() {
        return properties.textureTop;
    }

    public int[] getSideTextureId() {
        return properties.textureSide;
    }

    public int[] getBottomTextureId() {
        return properties.textureBottom;
    }

    // Optionally: returns the texture ID for a given face
    public int[] getTextureId(Face face) {
        return switch (face) {
            case TOP -> getTopTextureId();
            case BOTTOM -> getBottomTextureId();
            case LEFT, RIGHT, FRONT, BACK -> getSideTextureId();
        };
    }

    public enum Face {
        FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
    }
}
