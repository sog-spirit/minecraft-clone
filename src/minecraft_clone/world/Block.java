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

    public int[] getLeftTextureId() {
        return properties.textureLeft;
    }

    public int[] getRightTextureId() {
        return properties.textureRight;
    }

    public int[] getFrontTextureId() {
        return properties.textureFront;
    }

    public int[] getBackTextureId() {
        return properties.textureBack;
    }

    public int[] getBottomTextureId() {
        return properties.textureBottom;
    }

    // Optionally: returns the texture ID for a given face
    public int[] getTextureId(Face face) {
        return switch (face) {
            case TOP -> getTopTextureId();
            case BOTTOM -> getBottomTextureId();
            case LEFT -> getLeftTextureId();
            case RIGHT -> getRightTextureId();
            case FRONT -> getFrontTextureId();
            case BACK -> getBackTextureId();
        };
    }

    public enum Face {
        FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
    }
}
