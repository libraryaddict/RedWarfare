package me.libraryaddict.core.utils;

import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class UtilBlock
{
    /**
     * A block that's perfectly square
     */
    private static boolean[] _fullSolid = new boolean[Material.values().length];

    /**
     * Blocks that players are allowed to spawn on top of
     */
    private static boolean[] _groundBlocks = new boolean[Material.values().length];
    /**
     * Blocks that can be interacted with
     */
    private static boolean[] _interactable = new boolean[Material.values().length];
    /**
     * A block that cannot be interacted with physically
     */
    private static boolean[] _nonSolid = new boolean[Material.values().length];
    /**
     * Blocks that have some part of them solid
     */
    private static boolean[] _partiallySolid = new boolean[Material.values().length];
    /**
     * Blocks that players are allowed to spawn on top of
     */
    private static boolean[] _standable = new boolean[Material.values().length];

    static
    {
        setSolid(Material.BEDROCK, true);
        setSolid(Material.STONE, true);
        setSolid(Material.GRASS, true);
        setSolid(Material.DIRT, true);
        setSolid(Material.COBBLESTONE, true);
        setSolid(Material.WOOD, true);
        setSolid(Material.SAND, true);
        setSolid(Material.GRAVEL, true);
        setSolid(Material.GOLD_ORE, true);
        setSolid(Material.IRON_ORE, true);
        setSolid(Material.COAL_ORE, true);
        setSolid(Material.LOG, true);
        setSolid(Material.LEAVES, false);
        setSolid(Material.SPONGE, true);
        setSolid(Material.GLASS, false);
        setSolid(Material.LAPIS_ORE, true);
        setSolid(Material.LAPIS_BLOCK, true);
        setSolid(Material.DISPENSER, true);
        setSolid(Material.SANDSTONE, true);
        setSolid(Material.NOTE_BLOCK, true);
        setSolid(Material.PISTON_STICKY_BASE, true);
        setSolid(Material.PISTON_BASE, false);
        setSolid(Material.WOOL, true);
        setSolid(Material.GOLD_BLOCK, true);
        setSolid(Material.IRON_BLOCK, true);
        setSolid(Material.DOUBLE_STEP, true);
        setSolid(Material.BRICK, true);
        setSolid(Material.TNT, false);
        setSolid(Material.BOOKSHELF, true);
        setSolid(Material.MOSSY_COBBLESTONE, true);
        setSolid(Material.OBSIDIAN, true);
        setSolid(Material.MOB_SPAWNER, false);
        setSolid(Material.DIAMOND_ORE, true);
        setSolid(Material.DIAMOND_BLOCK, true);
        setSolid(Material.WORKBENCH, true);
        setSolid(Material.FURNACE, true);
        setSolid(Material.BURNING_FURNACE, true);
        setSolid(Material.REDSTONE_ORE, true);
        setSolid(Material.GLOWING_REDSTONE_ORE, true);
        setSolid(Material.ICE, true);
        setSolid(Material.CLAY, true);
        setSolid(Material.SNOW_BLOCK, true);
        setSolid(Material.JUKEBOX, true);
        setSolid(Material.PUMPKIN, true);
        setSolid(Material.NETHERRACK, true);
        setSolid(Material.GLOWSTONE, false);
        setSolid(Material.JACK_O_LANTERN, true);
        setSolid(Material.STAINED_GLASS, false);
        setSolid(Material.MONSTER_EGGS, true);
        setSolid(Material.SMOOTH_BRICK, true);
        setSolid(Material.HUGE_MUSHROOM_1, true);
        setSolid(Material.HUGE_MUSHROOM_2, true);
        setSolid(Material.MELON_BLOCK, true);
        setSolid(Material.MYCEL, true);
        setSolid(Material.NETHER_BRICK, true);
        setSolid(Material.ENDER_STONE, true);
        setSolid(Material.REDSTONE_LAMP_OFF, true);
        setSolid(Material.REDSTONE_LAMP_ON, true);
        setSolid(Material.WOOD_DOUBLE_STEP, true);
        setSolid(Material.EMERALD_ORE, true);
        setSolid(Material.EMERALD_BLOCK, true);
        setSolid(Material.COMMAND, true);
        setSolid(Material.BEACON, true);
        setSolid(Material.REDSTONE_BLOCK, true);
        setSolid(Material.QUARTZ_ORE, true);
        setSolid(Material.QUARTZ_BLOCK, true);
        setSolid(Material.DROPPER, true);
        setSolid(Material.STAINED_CLAY, true);
        setSolid(Material.LEAVES_2, false);
        setSolid(Material.LOG_2, true);
        setSolid(Material.SLIME_BLOCK, true);
        setSolid(Material.BARRIER, false);
        setSolid(Material.PRISMARINE, true);
        setSolid(Material.SEA_LANTERN, true);
        setSolid(Material.HAY_BLOCK, true);
        setSolid(Material.HARD_CLAY, true);
        setSolid(Material.COAL_BLOCK, true);
        setSolid(Material.PACKED_ICE, true);
        setSolid(Material.RED_SANDSTONE, true);
        setSolid(Material.DOUBLE_STONE_SLAB2, true);
        setSolid(Material.CHORUS_FLOWER, false);
        setSolid(Material.PURPUR_BLOCK, true);
        setSolid(Material.PURPUR_PILLAR, true);
        setSolid(Material.PURPUR_DOUBLE_SLAB, true);
        setSolid(Material.END_BRICKS, true);
        setSolid(Material.COMMAND_REPEATING, true);
        setSolid(Material.COMMAND_CHAIN, true);
        setSolid(Material.FROSTED_ICE, true);
        setSolid(Material.MAGMA, false);
        setSolid(Material.NETHER_WART_BLOCK, false);
        setSolid(Material.RED_NETHER_BRICK, true);
        setSolid(Material.BONE_BLOCK, false);
        setSolid(Material.STRUCTURE_BLOCK, false);

        setPartiallySolid(Material.WATER, false);
        setPartiallySolid(Material.STATIONARY_WATER, false);
        setPartiallySolid(Material.LAVA, false);
        setPartiallySolid(Material.STATIONARY_LAVA, false);
        setPartiallySolid(Material.BED_BLOCK, true);
        setPartiallySolid(Material.WEB, false);
        setPartiallySolid(Material.PISTON_EXTENSION, false);
        setPartiallySolid(Material.PISTON_MOVING_PIECE, false);
        setPartiallySolid(Material.STEP, true);
        setPartiallySolid(Material.WOOD_STAIRS, true);
        setPartiallySolid(Material.CHEST, true);
        setPartiallySolid(Material.SOIL, true);
        setPartiallySolid(Material.WOODEN_DOOR, false);
        setPartiallySolid(Material.LADDER, false);
        setPartiallySolid(Material.COBBLESTONE_STAIRS, true);
        setPartiallySolid(Material.IRON_DOOR_BLOCK, false);
        setPartiallySolid(Material.SNOW, true);
        setPartiallySolid(Material.CACTUS, true);
        setPartiallySolid(Material.FENCE, true);
        setPartiallySolid(Material.SOUL_SAND, true);
        setPartiallySolid(Material.CAKE_BLOCK, false);
        setPartiallySolid(Material.DIODE_BLOCK_OFF, true);
        setPartiallySolid(Material.DIODE_BLOCK_ON, true);
        setPartiallySolid(Material.TRAP_DOOR, false);
        setPartiallySolid(Material.IRON_FENCE, false);
        setPartiallySolid(Material.THIN_GLASS, false);
        setPartiallySolid(Material.VINE, false);
        setPartiallySolid(Material.FENCE_GATE, false);
        setPartiallySolid(Material.BRICK_STAIRS, true);
        setPartiallySolid(Material.SMOOTH_STAIRS, true);
        setPartiallySolid(Material.WATER_LILY, true);
        setPartiallySolid(Material.NETHER_FENCE, true);
        setPartiallySolid(Material.NETHER_BRICK_STAIRS, true);
        setPartiallySolid(Material.ENCHANTMENT_TABLE, true);
        setPartiallySolid(Material.BREWING_STAND, true);
        setPartiallySolid(Material.CAULDRON, true);
        setPartiallySolid(Material.ENDER_PORTAL_FRAME, true);
        setPartiallySolid(Material.DRAGON_EGG, true);
        setPartiallySolid(Material.WOOD_STEP, true);
        setPartiallySolid(Material.COCOA, false);
        setPartiallySolid(Material.SANDSTONE_STAIRS, true);
        setPartiallySolid(Material.ENDER_CHEST, true);
        setPartiallySolid(Material.SPRUCE_WOOD_STAIRS, true);
        setPartiallySolid(Material.BIRCH_WOOD_STAIRS, true);
        setPartiallySolid(Material.JUNGLE_WOOD_STAIRS, true);
        setPartiallySolid(Material.COBBLE_WALL, true);
        setPartiallySolid(Material.FLOWER_POT, false);
        setPartiallySolid(Material.SKULL, false);
        setPartiallySolid(Material.ANVIL, true);
        setPartiallySolid(Material.TRAPPED_CHEST, true);
        setPartiallySolid(Material.REDSTONE_COMPARATOR_OFF, true);
        setPartiallySolid(Material.REDSTONE_COMPARATOR_ON, true);
        setPartiallySolid(Material.DAYLIGHT_DETECTOR, true);
        setPartiallySolid(Material.HOPPER, true);
        setPartiallySolid(Material.QUARTZ_STAIRS, true);
        setPartiallySolid(Material.STAINED_GLASS_PANE, false);
        setPartiallySolid(Material.ACACIA_STAIRS, true);
        setPartiallySolid(Material.DARK_OAK_STAIRS, true);
        setPartiallySolid(Material.IRON_TRAPDOOR, false);
        setPartiallySolid(Material.CARPET, true);
        setPartiallySolid(Material.DAYLIGHT_DETECTOR_INVERTED, true);
        setPartiallySolid(Material.RED_SANDSTONE_STAIRS, true);
        setPartiallySolid(Material.STONE_SLAB2, true);
        setPartiallySolid(Material.SPRUCE_FENCE_GATE, false);
        setPartiallySolid(Material.BIRCH_FENCE_GATE, false);
        setPartiallySolid(Material.JUNGLE_FENCE_GATE, false);
        setPartiallySolid(Material.ACACIA_FENCE_GATE, false);
        setPartiallySolid(Material.DARK_OAK_FENCE_GATE, false);
        setPartiallySolid(Material.SPRUCE_FENCE, true);
        setPartiallySolid(Material.BIRCH_FENCE, true);
        setPartiallySolid(Material.JUNGLE_FENCE, true);
        setPartiallySolid(Material.DARK_OAK_FENCE, true);
        setPartiallySolid(Material.ACACIA_FENCE, true);
        setPartiallySolid(Material.SPRUCE_DOOR, false);
        setPartiallySolid(Material.BIRCH_DOOR, false);
        setPartiallySolid(Material.JUNGLE_DOOR, false);
        setPartiallySolid(Material.ACACIA_DOOR, false);
        setPartiallySolid(Material.DARK_OAK_DOOR, false);
        setPartiallySolid(Material.END_ROD, false);
        setPartiallySolid(Material.CHORUS_PLANT, true);
        setPartiallySolid(Material.PURPUR_STAIRS, true);
        setPartiallySolid(Material.PURPUR_SLAB, true);
        setPartiallySolid(Material.GRASS_PATH, true);

        setNonSolid(Material.AIR);
        setNonSolid(Material.SAPLING);
        setNonSolid(Material.POWERED_RAIL);
        setNonSolid(Material.DETECTOR_RAIL);
        setNonSolid(Material.LONG_GRASS);
        setNonSolid(Material.DEAD_BUSH);
        setNonSolid(Material.YELLOW_FLOWER);
        setNonSolid(Material.RED_ROSE);
        setNonSolid(Material.BROWN_MUSHROOM);
        setNonSolid(Material.RED_MUSHROOM);
        setNonSolid(Material.TORCH);
        setNonSolid(Material.FIRE);
        setNonSolid(Material.REDSTONE_WIRE);
        setNonSolid(Material.CROPS);
        setNonSolid(Material.SIGN_POST);
        setNonSolid(Material.RAILS);
        setNonSolid(Material.WALL_SIGN);
        setNonSolid(Material.WOOD_PLATE);
        setNonSolid(Material.LEVER);
        setNonSolid(Material.STONE_PLATE);
        setNonSolid(Material.REDSTONE_TORCH_OFF);
        setNonSolid(Material.REDSTONE_TORCH_ON);
        setNonSolid(Material.STONE_BUTTON);
        setNonSolid(Material.SUGAR_CANE_BLOCK);
        setNonSolid(Material.PORTAL);
        setNonSolid(Material.PUMPKIN_STEM);
        setNonSolid(Material.MELON_STEM);
        setNonSolid(Material.NETHER_WARTS);
        setNonSolid(Material.ENDER_PORTAL);
        setNonSolid(Material.TRIPWIRE_HOOK);
        setNonSolid(Material.TRIPWIRE);
        setNonSolid(Material.CARROT);
        setNonSolid(Material.POTATO);
        setNonSolid(Material.WOOD_BUTTON);
        setNonSolid(Material.GOLD_PLATE);
        setNonSolid(Material.IRON_PLATE);
        setNonSolid(Material.ACTIVATOR_RAIL);
        setNonSolid(Material.DOUBLE_PLANT);
        setNonSolid(Material.STANDING_BANNER);
        setNonSolid(Material.WALL_BANNER);
        setNonSolid(Material.BEETROOT_BLOCK);
        setNonSolid(Material.END_GATEWAY);
        setNonSolid(Material.STRUCTURE_VOID);

        setInteractable(Material.DISPENSER);
        setInteractable(Material.NOTE_BLOCK);
        setInteractable(Material.BED_BLOCK);
        setInteractable(Material.CHEST);
        setInteractable(Material.FURNACE);
        setInteractable(Material.BURNING_FURNACE);
        setInteractable(Material.WOODEN_DOOR);
        setInteractable(Material.LEVER);
        setInteractable(Material.STONE_PLATE);
        setInteractable(Material.WOOD_PLATE);
        setInteractable(Material.STONE_BUTTON);
        setInteractable(Material.JUKEBOX);
        setInteractable(Material.CAKE_BLOCK);
        setInteractable(Material.DIODE_BLOCK_OFF);
        setInteractable(Material.DIODE_BLOCK_ON);
        setInteractable(Material.TRAP_DOOR);
        setInteractable(Material.FENCE_GATE);
        setInteractable(Material.ENCHANTMENT_TABLE);
        setInteractable(Material.BREWING_STAND);
        setInteractable(Material.CAULDRON);
        setInteractable(Material.DRAGON_EGG);
        setInteractable(Material.ENDER_CHEST);
        setInteractable(Material.TRIPWIRE_HOOK);
        setInteractable(Material.COMMAND);
        setInteractable(Material.BEACON);
        setInteractable(Material.FLOWER_POT);
        setInteractable(Material.WOOD_BUTTON);
        setInteractable(Material.ANVIL);
        setInteractable(Material.TRAPPED_CHEST);
        setInteractable(Material.GOLD_PLATE);
        setInteractable(Material.IRON_PLATE);
        setInteractable(Material.REDSTONE_COMPARATOR_OFF);
        setInteractable(Material.REDSTONE_COMPARATOR_ON);
        setInteractable(Material.DROPPER);
        setInteractable(Material.SPRUCE_FENCE_GATE);
        setInteractable(Material.BIRCH_FENCE_GATE);
        setInteractable(Material.JUNGLE_FENCE_GATE);
        setInteractable(Material.ACACIA_FENCE_GATE);
        setInteractable(Material.DARK_OAK_FENCE_GATE);
        setInteractable(Material.SPRUCE_DOOR);
        setInteractable(Material.BIRCH_DOOR);
        setInteractable(Material.JUNGLE_DOOR);
        setInteractable(Material.ACACIA_DOOR);
        setInteractable(Material.DARK_OAK_DOOR);
        setInteractable(Material.COMMAND_REPEATING);
        setInteractable(Material.COMMAND_CHAIN);

        for (Material mat : Material.values())
        {
            if (!mat.isBlock())
                continue;

            if ((solid(mat) || partiallySolid(mat)) != nonSolid(mat) && (solid(mat) || nonSolid(mat) || partiallySolid(mat)))
                continue;

            System.out.println("The block " + mat + " isn't listed properly in UtilBlock");
        }
    }

    public static boolean exposed(Block block)
    {
        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (Math.abs(x) + Math.abs(y) + Math.abs(z) != 1)
                        continue;

                    Block b = block.getRelative(x, y, z);

                    if (UtilBlock.solid(b))
                        continue;

                    return true;
                }
            }
        }

        return false;
    }

    public static ArrayList<Block> getBlocks(Block loc1, Block loc2)
    {
        return getBlocks(loc1.getLocation(), loc2.getLocation());
    }

    public static ArrayList<Block> getBlocks(Location center, double sphereSize)
    {
        ArrayList<Block> blocks = new ArrayList<Block>();

        for (double x = Math.floor(-sphereSize); x <= Math.ceil(sphereSize); x++)
        {
            for (double y = Math.floor(-sphereSize); y <= Math.ceil(sphereSize); y++)
            {
                for (double z = Math.floor(-sphereSize); z <= Math.ceil(sphereSize); z++)
                {
                    if (new Vector(x, y, z).lengthSquared() > sphereSize * sphereSize)
                        continue;

                    if (center.getY() + y < 0 || center.getY() + y > 255)
                        continue;

                    Block block = center.clone().add(x, y, z).getBlock();

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public static ArrayList<Block> getBlocks(Location center, double sphereWidth, double height)
    {
        ArrayList<Block> blocks = new ArrayList<Block>();

        for (double x = Math.floor(-sphereWidth); x <= Math.ceil(sphereWidth); x += 1)
        {
            for (double y = Math.floor(-height); y <= Math.ceil(height); y += 1)
            {
                for (double z = Math.floor(-sphereWidth); z <= Math.ceil(sphereWidth); z += 1)
                {
                    if (new Vector(x, 0, z).lengthSquared() > sphereWidth * sphereWidth || y * y > height * height)
                        continue;

                    if (center.getY() + y < 0 || center.getY() + y > 255)
                        continue;

                    Block block = center.clone().add(x, y, z).getBlock();

                    blocks.add(block);
                }
            }
        }

        return blocks;
    }

    public static ArrayList<Block> getBlocks(Location loc1, Location loc2)
    {
        ArrayList<Block> list = new ArrayList<Block>();

        Vector min = new Vector(Math.min(loc1.getX(), loc2.getX()), Math.min(loc1.getY(), loc2.getY()),
                Math.min(loc1.getZ(), loc2.getZ()));

        Vector max = new Vector(Math.max(loc1.getX(), loc2.getX()), Math.max(loc1.getY(), loc2.getY()),
                Math.max(loc1.getZ(), loc2.getZ()));

        for (int x = min.getBlockX(); x <= max.getBlockX(); x++)
        {
            for (int y = min.getBlockY(); y <= max.getBlockY(); y++)
            {
                for (int z = min.getBlockZ(); z <= max.getBlockZ(); z++)
                {
                    list.add(loc1.getWorld().getBlockAt(x, y, z));
                }
            }
        }

        return list;
    }

    public static Block getHighestBlock(World world, int x, int z)
    {
        Block block = world.getBlockAt(x, 256, z);

        while (nonSolid()[block.getTypeId()] && block.getY() > 0)
        {
            block = block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    public static Block getHighestNonAirBlock(World world, int x, int z)
    {
        Block block = world.getBlockAt(x, 256, z);

        while (block.getType() == Material.AIR && block.getY() > 0)
        {
            block = block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    public static Block getHighestBlock(World world, int x, int z, boolean[] toCheckAgainst)
    {
        Block block = world.getBlockAt(x, 256, z);

        while (toCheckAgainst[block.getTypeId()] && block.getY() > 0)
        {
            block = block.getRelative(BlockFace.DOWN);
        }

        return block;
    }

    public static boolean interactable(Block block)
    {
        return interactable(block.getType());
    }

    public static boolean interactable(Material material)
    {
        return _interactable[material.ordinal()];
    }

    public static boolean[] nonSolid()
    {
        return _nonSolid;
    }

    public static boolean nonSolid(Block block)
    {
        return nonSolid(block.getType());
    }

    public static boolean nonSolid(Material material)
    {
        return _nonSolid[material.ordinal()];
    }

    public static boolean nonSolid(int material)
    {
        return _nonSolid[material];
    }

    public static boolean[] partiallySolid()
    {
        return _partiallySolid;
    }

    public static boolean partiallySolid(Block block)
    {
        return partiallySolid(block.getType());
    }

    public static boolean partiallySolid(Material material)
    {
        return _partiallySolid[material.ordinal()];
    }

    public static boolean partiallySolid(int material)
    {
        return _partiallySolid[material];
    }

    private static void setInteractable(Material mat)
    {
        _interactable[mat.ordinal()] = true;
    }

    private static void setNonSolid(Material mat)
    {
        _nonSolid[mat.ordinal()] = true;
    }

    private static void setPartiallySolid(Material mat, boolean standable)
    {
        _partiallySolid[mat.ordinal()] = true;
        _standable[mat.ordinal()] = standable;
    }

    private static void setSolid(Material mat, boolean ground)
    {
        _fullSolid[mat.ordinal()] = true;
        _groundBlocks[mat.ordinal()] = ground;
        _standable[mat.ordinal()] = true;
    }

    public static boolean solid(Block block)
    {
        return solid(block.getType());
    }

    public static boolean solid(Material material)
    {
        return _fullSolid[material.ordinal()];
    }

    public static boolean solid(int material)
    {
        return _fullSolid[material];
    }

    public static boolean spawnable(Block block)
    {
        return spawnable(block.getType());
    }

    public static boolean spawnable(Material mat)
    {
        return solid(mat) && _groundBlocks[mat.ordinal()];
    }

    public static boolean standable(Block block)
    {
        return standable(block.getType());
    }

    public static boolean standable(Material mat)
    {
        return _standable[mat.ordinal()];
    }

    public static boolean isWater(Block block)
    {
        return isWater(block.getType());
    }

    public static boolean isWater(Material material)
    {
        return material == Material.WATER || material == Material.STATIONARY_WATER;
    }
}
