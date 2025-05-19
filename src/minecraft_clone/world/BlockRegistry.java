package minecraft_clone.world;

import java.util.EnumMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<BlockType, BlockProperties> registry = new EnumMap<>(BlockType.class);

    static {
        register(BlockType.AIR,    new BlockProperties("Air", false, true,   new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}));
        register(BlockType.GRASS,  new BlockProperties("Grass", true, false, new int[] {0, 0}, new int[] {3, 0}, new int[] {2, 0}));
        register(BlockType.DIRT,   new BlockProperties("Dirt", true, false,  new int[] {2, 0}, new int[] {2, 0}, new int[] {2, 0}));
        register(BlockType.STONE,  new BlockProperties("Stone", true, false, new int[] {1, 0}, new int[] {1, 0}, new int[] {1, 0}));
        register(BlockType.SAND,   new BlockProperties("Sand", true, false,  new int[] {2, 1}, new int[] {2, 1}, new int[] {2, 1}));
    }

    private static void register(BlockType type, BlockProperties props) {
        registry.put(type, props);
    }

    public static BlockProperties get(BlockType type) {
        return registry.get(type);
    }
}
