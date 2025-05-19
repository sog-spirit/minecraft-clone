package minecraft_clone.world;

import org.joml.Vector3f;

import minecraft_clone.engine.RawModel;

public class Block {
    private final Vector3f position;
    private final RawModel model;

    public Block(RawModel model, Vector3f position) {
        this.position = position;
        this.model = model;
    }

    public RawModel getModel() {
        return model;
    }

    public Vector3f getPosition() {
        return position;
    }
}
