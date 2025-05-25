package minecraft_clone.world;

import java.util.EnumMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<BlockType, BlockProperties> registry = new EnumMap<>(BlockType.class);

    static {
        register(BlockType.AIR,    new BlockProperties("Air", false, true,   new int[] {-1, -1}, new int[] {-1, -1}, new int[] {-1, -1}));
        register(BlockType.STONE,  new BlockProperties("Stone", true, false, new int[] {1, 0}, new int[] {1, 0}, new int[] {1, 0}));
        register(BlockType.GRASS,  new BlockProperties("Grass", true, false, new int[] {0, 0}, new int[] {3, 0}, new int[] {2, 0}));
        register(BlockType.DIRT,   new BlockProperties("Dirt", true, false,  new int[] {2, 0}, new int[] {2, 0}, new int[] {2, 0}));
        register(BlockType.COBBLESTONE,   new BlockProperties("Cobblestone", true, false,  new int[] {0, 1}, new int[] {0, 1}, new int[] {0, 1}));
        register(BlockType.OAK_PLANKS,   new BlockProperties("Oak planks", true, false,  new int[] {4, 0}, new int[] {4, 0}, new int[] {4, 0}));
        register(BlockType.BEDROCK,   new BlockProperties("Bedrock", true, false,  new int[] {1, 1}, new int[] {1, 1}, new int[] {1, 1}));
        register(BlockType.SAND,   new BlockProperties("Sand", true, false,  new int[] {2, 1}, new int[] {2, 1}, new int[] {2, 1}));
        register(BlockType.GRAVEL,   new BlockProperties("Gravel", true, false,  new int[] {3, 1}, new int[] {3, 1}, new int[] {3, 1}));
        register(BlockType.GOLD_ORE,   new BlockProperties("Gold ore", true, false,  new int[] {0, 2}, new int[] {0, 2}, new int[] {0, 2}));
        register(BlockType.IRON_ORE,   new BlockProperties("Iron ore", true, false,  new int[] {1, 2}, new int[] {1, 2}, new int[] {1, 2}));
        register(BlockType.COAL_ORE,   new BlockProperties("Coal ore", true, false,  new int[] {2, 2}, new int[] {2, 2}, new int[] {2, 2}));
        register(BlockType.OAK_LOG,   new BlockProperties("Oak log", true, false,  new int[] {5, 1}, new int[] {4, 1}, new int[] {5, 1}));
        register(BlockType.SPRUCE_LOG,   new BlockProperties("Spruce log", true, false,  new int[] {5, 1}, new int[] {4, 7}, new int[] {5, 1}));
        register(BlockType.BIRCH_LOG,   new BlockProperties("Birch log", true, false,  new int[] {5, 1}, new int[] {5, 7}, new int[] {5, 1}));
        register(BlockType.OAK_LEAVES,   new BlockProperties("Oak leaves", true, false,  new int[] {5, 3}, new int[] {5, 3}, new int[] {5, 3}));
        register(BlockType.SPRUCE_LEAVES,   new BlockProperties("Spurce leaves", true, false,  new int[] {5, 8}, new int[] {5, 8}, new int[] {5, 8}));
        register(BlockType.BIRCH_LEAVES,   new BlockProperties("Birch leaves", true, false,  new int[] {5, 8}, new int[] {5, 8}, new int[] {5, 8}));
        register(BlockType.SPONGE,   new BlockProperties("Sponge", true, false,  new int[] {0, 3}, new int[] {0, 3}, new int[] {0, 3}));
        register(BlockType.GLASS,   new BlockProperties("Glass", true, true,  new int[] {1, 3}, new int[] {1, 3}, new int[] {1, 3}));
        register(BlockType.LAPIS_LAZULI_ORE,   new BlockProperties("Lapis lazuli ore", true, false,  new int[] {0, 10}, new int[] {0, 10}, new int[] {0, 10}));
        register(BlockType.LAPIS_LAZULI,   new BlockProperties("Lapis lazuli", true, false,  new int[] {0, 9}, new int[] {0, 9}, new int[] {0, 9}));
        register(BlockType.DISPENSER,   new BlockProperties("Dispenser", true, false,  new int[] {14, 3}, new int[] {14, 2}, new int[] {13, 2}, new int[] {13, 2}, new int[] {13, 2}, new int[] {14, 3}));
        register(BlockType.SANDSTONE,   new BlockProperties("Sandstone", true, false,  new int[] {0, 11}, new int[] {0, 12}, new int[] {0, 13}));
        register(BlockType.NOTE_BLOCK,   new BlockProperties("Note block", true, false,  new int[] {11, 4}, new int[] {10, 4}, new int[] {10, 4}));
        register(BlockType.STICKY_PISTON,   new BlockProperties("Sticky piston", true, false,  new int[] {10, 6}, new int[] {12, 6}, new int[] {13, 6}));
        register(BlockType.PISTON,   new BlockProperties("Piston", true, false,  new int[] {11, 6}, new int[] {12, 6}, new int[] {13, 6}));
        register(BlockType.WHITE_WOOL,   new BlockProperties("White wool", true, false,  new int[] {0, 4}, new int[] {0, 4}, new int[] {0, 4}));
        register(BlockType.ORANGE_WOOL,   new BlockProperties("Orange wool", true, false,  new int[] {2, 13}, new int[] {2, 13}, new int[] {2, 13}));
        register(BlockType.MAGENTA_WOOL,   new BlockProperties("Magenta wool", true, false,  new int[] {2, 12}, new int[] {2, 12}, new int[] {2, 12}));
        register(BlockType.LIGHT_BLUE_WOOL,   new BlockProperties("Light blue wool", true, false,  new int[] {2, 11}, new int[] {2, 11}, new int[] {2, 11}));
        register(BlockType.YELLOW_WOOL,   new BlockProperties("Yellow wool", true, false,  new int[] {2, 10}, new int[] {2, 10}, new int[] {2, 10}));
        register(BlockType.LIME_WOOL,   new BlockProperties("Lime wool", true, false,  new int[] {2, 9}, new int[] {2, 9}, new int[] {2, 9}));
        register(BlockType.PINK_WOOL,   new BlockProperties("Pink wool", true, false,  new int[] {2, 8}, new int[] {2, 8}, new int[] {2, 8}));
        register(BlockType.GRAY_WOOL,   new BlockProperties("Gray wool", true, false,  new int[] {2, 7}, new int[] {2, 7}, new int[] {2, 7}));
        register(BlockType.LIGHT_GRAY_WOOL,   new BlockProperties("Light gray wool", true, false,  new int[] {1, 14}, new int[] {1, 14}, new int[] {1, 14}));
        register(BlockType.CYAN_WOOL,   new BlockProperties("Cyan wool", true, false,  new int[] {1, 13}, new int[] {1, 13}, new int[] {1, 13}));
        register(BlockType.PURPLE_WOOL,   new BlockProperties("Purple wool", true, false,  new int[] {1, 12}, new int[] {1, 12}, new int[] {1, 12}));
        register(BlockType.BLUE_WOOL,   new BlockProperties("Blue wool", true, false,  new int[] {1, 11}, new int[] {1, 11}, new int[] {1, 11}));
        register(BlockType.BROWN_WOOL,   new BlockProperties("Brown wool", true, false,  new int[] {1, 10}, new int[] {1, 10}, new int[] {1, 10}));
        register(BlockType.GREEN_WOOL,   new BlockProperties("Green wool", true, false,  new int[] {1, 9}, new int[] {1, 9}, new int[] {1, 9}));
        register(BlockType.RED_WOOL,   new BlockProperties("Red wool", true, false,  new int[] {1, 8}, new int[] {1, 8}, new int[] {1, 8}));
        register(BlockType.BLACK_WOOL,   new BlockProperties("Black wool", true, false,  new int[] {1, 7}, new int[] {1, 7}, new int[] {1, 7}));
        register(BlockType.GOLD,   new BlockProperties("Gold", true, false,  new int[] {7, 1}, new int[] {7, 1}, new int[] {7, 1}));
        register(BlockType.IRON,   new BlockProperties("Iron", true, false,  new int[] {6, 1}, new int[] {6, 1}, new int[] {6, 1}));
        register(BlockType.BRICK,   new BlockProperties("Brick", true, false,  new int[] {7, 0}, new int[] {7, 0}, new int[] {7, 0}));
        register(BlockType.TNT,   new BlockProperties("Tnt", true, false,  new int[] {9, 0}, new int[] {8, 0}, new int[] {10, 0}));
        register(BlockType.BOOKSHELF,   new BlockProperties("Bookshelf", true, false,  new int[] {4, 0}, new int[] {3, 2}, new int[] {4, 0}));
        register(BlockType.MOSSY_COBBLESTONE,   new BlockProperties("Mossy cobblestone", true, false,  new int[] {4, 2}, new int[] {4, 2}, new int[] {4, 2}));
        register(BlockType.OBSIDIAN,   new BlockProperties("Obsidian", true, false,  new int[] {5, 2}, new int[] {5, 2}, new int[] {5, 2}));
//        register(BlockType.CHEST,   new BlockProperties("Chest", true, false,  new int[] {5, 2}, new int[] {5, 2}, new int[] {5, 2}));
        register(BlockType.DIAMOND_ORE,   new BlockProperties("Diamond ore", true, false,  new int[] {2, 3}, new int[] {2, 3}, new int[] {2, 3}));
        register(BlockType.DIAMOND,   new BlockProperties("Diamond", true, false,  new int[] {8, 1}, new int[] {8, 1}, new int[] {8, 1}));
        register(BlockType.CRAFTING_TABLE,   new BlockProperties("Crafting table", true, false, new int[] {11, 2}, new int[] {11, 3}, new int[] {12, 3}, new int[] {4, 0}));
        register(BlockType.FARMLAND,   new BlockProperties("Farmland", true, false,  new int[] {7, 5}, new int[] {2, 0}, new int[] {2, 0}));
        register(BlockType.FURNACE,   new BlockProperties("Furnace", true, false,  new int[] {14, 3}, new int[] {12, 2}, new int[] {13, 2}, new int[] {13, 2}, new int[] {13, 2}, new int[] {14, 3}));
        register(BlockType.REDSTONE_ORE,   new BlockProperties("Redstone ore", true, false,  new int[] {3, 3}, new int[] {3, 3}, new int[] {3, 3}));
        register(BlockType.ICE,   new BlockProperties("Ice", true, false,  new int[] {3, 4}, new int[] {3, 4}, new int[] {3, 4}));
        register(BlockType.SNOW,   new BlockProperties("Snow", true, false,  new int[] {2, 4}, new int[] {2, 4}, new int[] {2, 4}));
        register(BlockType.CACTUS,   new BlockProperties("Cactus", true, false,  new int[] {7, 4}, new int[] {6, 4}, new int[] {5, 4}));
        register(BlockType.CLAY,   new BlockProperties("Clay", true, false,  new int[] {8, 4}, new int[] {8, 4}, new int[] {8, 4}));
        register(BlockType.JUKEBOX,   new BlockProperties("Jukebox", true, false,  new int[] {11, 4}, new int[] {10, 4}, new int[] {10, 4}));
        register(BlockType.PUMPKIN,   new BlockProperties("Pumpkin", true, false,  new int[] {6, 6}, new int[] {7, 7}, new int[] {6, 7}, new int[] {6, 7}, new int[] {6, 7}, new int[] {6, 7}));
        register(BlockType.NETHERRACK,   new BlockProperties("Netherack", true, false,  new int[] {7, 6}, new int[] {7, 6}, new int[] {7, 6}));
        register(BlockType.SOUL_SAND,   new BlockProperties("Soul sand", true, false,  new int[] {8, 6}, new int[] {8, 6}, new int[] {8, 6}));
        register(BlockType.GLOWSTONE,   new BlockProperties("Glowstone", true, false,  new int[] {9, 6}, new int[] {9, 6}, new int[] {9, 6}));
        register(BlockType.JACK_O_LANTERN,   new BlockProperties("Jack o lantern", true, false,  new int[] {6, 6}, new int[] {8, 7}, new int[] {6, 7}, new int[] {6, 7}, new int[] {6, 7}, new int[] {6, 7}));
    }

    private static void register(BlockType type, BlockProperties props) {
        registry.put(type, props);
    }

    public static BlockProperties get(BlockType type) {
        return registry.get(type);
    }
}
