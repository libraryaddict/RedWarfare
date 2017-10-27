package me.libraryaddict.core.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.Pair;

public class UtilItem
{
    private static HashMap<String, Pair<Material, Short>> _itemNames = new HashMap<String, Pair<Material, Short>>();
    private static HashMap<Material, HashMap<String, Short>> _matDura = new HashMap<Material, HashMap<String, Short>>();
    private static HashMap<Pair<Material, Short>, String> _matNames = new HashMap<Pair<Material, Short>, String>();

    static
    {
        register(Material.WOOD, 0, "Oak Wood", "Oak Planks");
        register(Material.WOOD, 1, "Spruce Wood", "Spruce Planks");
        register(Material.WOOD, 2, "Birch Wood", "Birch Planks");
        register(Material.WOOD, 3, "Jungle Wood", "Jungle Planks");
        register(Material.WOOD, 4, "Acacia Wood", "Acacia Planks");
        register(Material.WOOD, 5, "Dark Oak Wood", "Dark Oak Planks");

        registerDura(Material.WOOD, 0, "Oak");
        registerDura(Material.WOOD, 1, "Spruce");
        registerDura(Material.WOOD, 2, "Birch");
        registerDura(Material.WOOD, 3, "Jungle");
        registerDura(Material.WOOD, 4, "Acacia");
        registerDura(Material.WOOD, 5, "DarkOak", "DOak");

        register(Material.ARMOR_STAND, "Armorstand", "Armor Stand");

        register(Material.ENDER_PEARL, "Enderpearl", "Ender Pearl");

        register(Material.STONE, 1, "Granite");
        register(Material.STONE, 2, "Polished Granite");
        register(Material.STONE, 3, "Diorite");
        register(Material.STONE, 4, "Polished Dorite");
        register(Material.STONE, 5, "Andesite");
        register(Material.STONE, 6, "Polished Andesite");

        registerDura(Material.STONE, 1, "Granite");
        registerDura(Material.STONE, 2, "PolishedGranite", "PGranite");
        registerDura(Material.STONE, 3, "Diorite");
        registerDura(Material.STONE, 4, "PolishedDorite", "PDorite");
        registerDura(Material.STONE, 5, "Andesite");
        registerDura(Material.STONE, 6, "PolishedAndesite", "PolishedAndesite");

        register(Material.DIRT, 1, "Coarse Dirt");
        register(Material.DIRT, 2, "Podzol");

        registerDura(Material.DIRT, 1, "Coarse", "CoarseDirt");
        registerDura(Material.DIRT, 2, "Podzol");

        register(Material.SAPLING, 0, "Oak Sapling", "Sapling");
        register(Material.SAPLING, 1, "Spruce Sapling");
        register(Material.SAPLING, 2, "Birch Sapling");
        register(Material.SAPLING, 3, "Jungle Sapling");
        register(Material.SAPLING, 4, "Acacia Sapling");
        register(Material.SAPLING, 5, "Dark Oak Sapling");

        registerDura(Material.SAPLING, 0, "Oak");
        registerDura(Material.SAPLING, 1, "Spruce");
        registerDura(Material.SAPLING, 2, "Birch");
        registerDura(Material.SAPLING, 3, "Jungle");
        registerDura(Material.SAPLING, 4, "Acacia");
        registerDura(Material.SAPLING, 5, "DarkOak", "DOak");

        register(Material.SAND, 1, "Red Sand");

        registerDura(Material.SAND, 1, "Red");

        register(Material.LOG, 0, "Oak Log", "Log");
        register(Material.LOG, 1, "Spruce Log");
        register(Material.LOG, 2, "Birch Log");
        register(Material.LOG, 3, "Jungle Log");

        registerDura(Material.LOG, 0, "Oak");
        registerDura(Material.LOG, 1, "Spruce");
        registerDura(Material.LOG, 2, "Birch");
        registerDura(Material.LOG, 3, "Jungle");

        register(Material.LOG_2, 0, "Acacia Log");
        register(Material.LOG_2, 1, "Dark Oak Log");

        registerDura(Material.LOG_2, 0, "Acacia");
        registerDura(Material.LOG_2, 1, "DarkOak", "DOak");

        register(Material.LEAVES, 0, "Oak Leaves", "Leaves");
        register(Material.LEAVES, 1, "Spruce Leaves");
        register(Material.LEAVES, 2, "Birch Leaves");
        register(Material.LEAVES, 3, "Jungle Leaves");

        registerDura(Material.LEAVES, 0, "Oak");
        registerDura(Material.LEAVES, 1, "Spruce");
        registerDura(Material.LEAVES, 2, "Birch");
        registerDura(Material.LEAVES, 3, "Jungle");

        register(Material.LEAVES_2, 0, "Acacia Leaves");
        register(Material.LEAVES_2, 1, "Dark Oak Leaves");

        registerDura(Material.LEAVES_2, 0, "Acacia");
        registerDura(Material.LEAVES_2, 1, "DarkOak", "DOak");

        for (Material mat : new Material[]
            {
                    Material.WOOL, Material.STAINED_CLAY, Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.CARPET
            })
        {
            String[] names = new String[0];

            switch (mat)
            {
            case WOOL:
                names = new String[]
                    {
                            "Wool"
                    };
                break;
            case STAINED_CLAY:
                names = new String[]
                    {
                            "Stained Clay", "Hardened Clay", "Stained Hardened Clay", "Clay"
                    };
                break;
            case STAINED_GLASS:
                names = new String[]
                    {
                            "Stained Glass", "Glass"
                    };
                break;
            case STAINED_GLASS_PANE:
                names = new String[]
                    {
                            "Stained Glass Pane", "Glass Pane", "Pane"
                    };
                break;
            case CARPET:
                names = new String[]
                    {
                            "Carpet"
                    };
                break;
            default:
                break;
            }

            for (int i = 0; i <= 15; i++)
            {
                String[] colors = new String[0];

                switch (i)
                {
                case 0:
                    colors = new String[]
                        {
                                "White"
                        };
                    break;
                case 1:
                    colors = new String[]
                        {
                                "Orange", "Light Purple"
                        };
                    break;
                case 2:
                    colors = new String[]
                        {
                                "Magenta"
                        };
                    break;
                case 3:
                    colors = new String[]
                        {
                                "Light Blue"
                        };
                    break;
                case 4:
                    colors = new String[]
                        {
                                "Yellow"
                        };
                    break;
                case 5:
                    colors = new String[]
                        {
                                "Lime"
                        };
                    break;
                case 6:
                    colors = new String[]
                        {
                                "Pink"
                        };
                    break;
                case 7:
                    colors = new String[]
                        {
                                "Gray"
                        };
                    break;
                case 8:
                    colors = new String[]
                        {
                                "Light Gray"
                        };
                    break;
                case 9:
                    colors = new String[]
                        {
                                "Cyan"
                        };
                    break;
                case 10:
                    colors = new String[]
                        {
                                "Purple"
                        };
                    break;
                case 11:
                    colors = new String[]
                        {
                                "Blue"
                        };
                    break;
                case 12:
                    colors = new String[]
                        {
                                "Brown"
                        };
                    break;
                case 13:
                    colors = new String[]
                        {
                                "Green", "Dark Green"
                        };
                    break;
                case 14:
                    colors = new String[]
                        {
                                "Red"
                        };
                    break;
                case 15:
                    colors = new String[]
                        {
                                "Black"
                        };
                    break;
                }

                ArrayList<String> list = new ArrayList<String>();

                for (String name : names)
                {
                    for (String color : colors)
                    {
                        list.add(color + " " + name);
                    }

                    if (i == 0)
                    {
                        list.add(name);

                        list.remove("Glass");
                        list.remove("Glass Pane");
                        list.remove("Clay");
                    }
                }

                register(mat, i, list.toArray(new String[0]));

                registerDura(mat, i, colors);
            }
        }

        register(Material.REDSTONE_TORCH_ON, 0, "Redstone Torch");
        register(Material.REDSTONE_LAMP_OFF, 0, "Redstone Lamp");
        register(Material.DIODE, 0, "Repeater", "Diode", "Redstone Diode", "Redstone Repeater");
        register(Material.REDSTONE_COMPARATOR, 0, "Redstone Comparator", "Comparator");

        register(Material.DOUBLE_STEP, 0, "Double Stone Slab", "Double Slab");
        register(Material.DOUBLE_STEP, 1, "Double Sandstone Slab");
        register(Material.DOUBLE_STEP, 2, "Double Tough Wooden Slab");
        register(Material.DOUBLE_STEP, 3, "Double Cobblestone Slab");
        register(Material.DOUBLE_STEP, 4, "Double Bricks Slab");
        register(Material.DOUBLE_STEP, 5, "Double Stone Brick Slab");
        register(Material.DOUBLE_STEP, 6, "Double Nether Slab", "Double Nether Brick Slab");
        register(Material.DOUBLE_STEP, 7, "Double Quartz Slab");
        register(Material.DOUBLE_STEP, 8, "Double Smooth Stone Slab", "Double Smooth Slab", "Smooth Double Slab",
                "Smooth Double Stone Slab");
        register(Material.DOUBLE_STEP, 9, "Double Smooth Sandstone Slab", "Smooth Double Sandstone Slab");
        register(Material.DOUBLE_STEP, 15, "Double Tiled Quartz", "Tiled Double Quartz Slab");

        registerDura(Material.DOUBLE_STEP, 0, "Stone");
        registerDura(Material.DOUBLE_STEP, 1, "Sandstone", "Sand");
        registerDura(Material.DOUBLE_STEP, 2, "Wood", "Wooden");
        registerDura(Material.DOUBLE_STEP, 3, "Cobble", "Cobblestone");
        registerDura(Material.DOUBLE_STEP, 4, "Bricks", "Brick");
        registerDura(Material.DOUBLE_STEP, 5, "StoneBrick");
        registerDura(Material.DOUBLE_STEP, 6, "Nether", "NetherBrick");
        registerDura(Material.DOUBLE_STEP, 7, "Quartz");
        registerDura(Material.DOUBLE_STEP, 8, "SmoothStone", "Smooth");
        registerDura(Material.DOUBLE_STEP, 9, "SmoothSand", "SmoothSandstone");
        registerDura(Material.DOUBLE_STEP, 15, "TiledQuartz", "Tiled", "Chiseled");

        register(Material.STEP, 0, "Stone Step", "Stone Slab", "Step", "Slab");
        register(Material.STEP, 1, "Sandstone Slab", "Sandstone Step");
        register(Material.STEP, 2, "Tough Wooden Slab", "Tough Wooden Step");
        register(Material.STEP, 3, "Cobblestone Slab", "Cobblestone Step");
        register(Material.STEP, 4, "Bricks Slab", "Bricks Step");
        register(Material.STEP, 5, "Stone Brick Slab", "Stone Brick Step");
        register(Material.STEP, 6, "Nether Slab", "Nether Step", "Nether Brick Slab", "Nether Brick Step");
        register(Material.STEP, 7, "Quartz Slab", "Quartz Step");

        registerDura(Material.STEP, 0, "Stone");
        registerDura(Material.STEP, 1, "Sand", "Sandstone");
        registerDura(Material.STEP, 2, "Wooden", "Wood");
        registerDura(Material.STEP, 3, "Cobble", "Cobblestone");
        registerDura(Material.STEP, 4, "Brick", "Bricks");
        registerDura(Material.STEP, 5, "StoneBrick", "StoneBricks");
        registerDura(Material.STEP, 6, "Nether", "NetherBrick", "NetherBricks");
        registerDura(Material.STEP, 7, "Quartz");

        register(Material.DOUBLE_STONE_SLAB2, 0, "Double Red Sandstone Slab", "Double Red Sandstone Step");
        register(Material.STONE_SLAB2, 0, "Red Sandstone Slab", "Red Sandstone Step");

        register(Material.WOOD_DOUBLE_STEP, 0, "Double Oak Slab", "Double Oak Wood Slab", "Double Oak Step",
                "Double Oak Wood Step");
        register(Material.WOOD_DOUBLE_STEP, 1, "Double Spruce Slab", "Double Spruce Wood Slab", "Double Spruce Step",
                "Double Spruce Wood Step");
        register(Material.WOOD_DOUBLE_STEP, 2, "Double Birch Slab", "Double Birch Wood Slab", "Double Birch Step",
                "Double Birch Wood Step");
        register(Material.WOOD_DOUBLE_STEP, 3, "Double Jungle Slab", "Double Jungle Wood Slab", "Double Jungle Step",
                "Double Jungle Wood Step");
        register(Material.WOOD_DOUBLE_STEP, 4, "Double Acacia Slab", "Double Acacia Wood Slab", "Double Acacia Step",
                "Double Acacia Wood Step");
        register(Material.WOOD_DOUBLE_STEP, 5, "Double Dark Oak Slab", "Double Dark Oak Wood Slab", "Double Dark Oak Step",
                "Double Dark Oak Wood Step");

        registerDura(Material.WOOD_DOUBLE_STEP, 0, "Oak");
        registerDura(Material.WOOD_DOUBLE_STEP, 1, "Spruce");
        registerDura(Material.WOOD_DOUBLE_STEP, 2, "Birch");
        registerDura(Material.WOOD_DOUBLE_STEP, 3, "Jungle");
        registerDura(Material.WOOD_DOUBLE_STEP, 4, "Acacia");
        registerDura(Material.WOOD_DOUBLE_STEP, 5, "DarkOak", "DOak");

        register(Material.WOOD_STEP, 0, "Oak Step", "Oak Slab");
        register(Material.WOOD_STEP, 1, "Spruce Slab", "Spruce Step");
        register(Material.WOOD_STEP, 2, "Birch Slab", "Birch Step");
        register(Material.WOOD_STEP, 3, "Jungle Slab", "Jungle Step");
        register(Material.WOOD_STEP, 4, "Acacia Slab", "Acacia Step");
        register(Material.WOOD_STEP, 5, "Dark Oak Slab", "Dark Oak Step");

        registerDura(Material.WOOD_STEP, 0, "Oak");
        registerDura(Material.WOOD_STEP, 1, "Spruce");
        registerDura(Material.WOOD_STEP, 2, "Birch");
        registerDura(Material.WOOD_STEP, 3, "Jungle");
        registerDura(Material.WOOD_STEP, 4, "Acacia");
        registerDura(Material.WOOD_STEP, 5, "DarkOak", "DOak");

        register(Material.SANDSTONE, 1, "Chiseled Sandstone");
        register(Material.SANDSTONE, 2, "Smooth Sandstone");

        registerDura(Material.SANDSTONE, 0, "Normal");
        registerDura(Material.SANDSTONE, 1, "Chiseled");
        registerDura(Material.SANDSTONE, 2, "Smooth");

        register(Material.RED_SANDSTONE, 1, "Chiseled Red Sandstone");
        register(Material.RED_SANDSTONE, 2, "Smooth Red Sandstone");

        registerDura(Material.RED_SANDSTONE, 0, "Normal");
        registerDura(Material.RED_SANDSTONE, 1, "Chiseled");
        registerDura(Material.RED_SANDSTONE, 2, "Smooth");

        register(Material.LONG_GRASS, 0, "Dead Bush", "Shrub");
        register(Material.LONG_GRASS, 1, "Long Grass", "Tall Grass");
        register(Material.LONG_GRASS, 2, "Fern");

        registerDura(Material.LONG_GRASS, 0, "DeadBush", "Dead", "Bush", "Shrub");
        registerDura(Material.LONG_GRASS, 1, "Long", "Tall", "Big");
        registerDura(Material.LONG_GRASS, 2, "Fern");

        register(Material.YELLOW_FLOWER, 0, "Dandelion", "Yellow Flower");

        register(Material.RED_ROSE, 0, "Poppy", "Rose", "Red Rose");
        register(Material.RED_ROSE, 1, "Blue Orchid", "Blue Flower");
        register(Material.RED_ROSE, 2, "Allium", "Purple Flower");
        register(Material.RED_ROSE, 3, "Azure Bluet");
        register(Material.RED_ROSE, 4, "Red Tulip", "Tulip");
        register(Material.RED_ROSE, 5, "Orange Tulip", "Orange Flower");
        register(Material.RED_ROSE, 6, "White Tulip", "White Flower");
        register(Material.RED_ROSE, 7, "Pink Tulip", "Pink Flower");
        register(Material.RED_ROSE, 8, "Oxeye Daisy");

        registerDura(Material.RED_ROSE, 0, "Poppy", "Rose", "Red");
        registerDura(Material.RED_ROSE, 1, "Orchid", "Blue");
        registerDura(Material.RED_ROSE, 2, "Allium", "Purple");
        registerDura(Material.RED_ROSE, 3, "Azure", "Bluet");
        registerDura(Material.RED_ROSE, 4, "Tulip");
        registerDura(Material.RED_ROSE, 5, "Orange");
        registerDura(Material.RED_ROSE, 6, "White");
        registerDura(Material.RED_ROSE, 7, "Pink");
        registerDura(Material.RED_ROSE, 8, "Oxeye", "Daisy");

        register(Material.DOUBLE_PLANT, 0, "Sunflower");
        register(Material.DOUBLE_PLANT, 1, "Lilac");
        register(Material.DOUBLE_PLANT, 2, "Double Long Grass", "Double Tall Grass");
        register(Material.DOUBLE_PLANT, 3, "Large Fern");
        register(Material.DOUBLE_PLANT, 4, "Rose Bush");
        register(Material.DOUBLE_PLANT, 5, "Peony");

        registerDura(Material.DOUBLE_PLANT, 0, "Sunflower");
        registerDura(Material.DOUBLE_PLANT, 1, "Lilac");
        registerDura(Material.DOUBLE_PLANT, 2, "Grass");
        registerDura(Material.DOUBLE_PLANT, 3, "Fern");
        registerDura(Material.DOUBLE_PLANT, 4, "Rose");
        registerDura(Material.DOUBLE_PLANT, 5, "Peony");

        register(Material.WOOD_STAIRS, 0, "Oak Stairs", "Oak Wood Stairs", "Stairs");
        register(Material.COBBLESTONE_STAIRS, 0, "Cobblestone Stairs", "Stone Stairs");
        register(Material.BRICK_STAIRS, 0, "Cobblestone Stairs", "Stone Stairs");
        register(Material.SMOOTH_STAIRS, 0, "Smooth Stairs", "Stone Brick Stairs");
        register(Material.SPRUCE_WOOD_STAIRS, 0, "Spruce Stairs", "Spruce Wood Stairs");
        register(Material.BIRCH_WOOD_STAIRS, 0, "Birch Stairs", "Birch Stairs");
        register(Material.JUNGLE_WOOD_STAIRS, 0, "Jungle Stairs", "Jungle Stairs");
        register(Material.ACACIA_STAIRS, 0, "Acacia Stairs", "Acacia Stairs");
        register(Material.DARK_OAK_STAIRS, 0, "Dark Oak Stairs", "Dark Oak Stairs");
        register(Material.RED_SANDSTONE_STAIRS, 0, "Red Sandstone Stairs", "Red Stairs");

        register(Material.SOIL, "Farmland", "Soil", "Tilled Dirt");

        register(Material.SNOW, 0, "Snow Layer");

        register(Material.PUMPKIN, 0, "Pumpkin");
        register(Material.PUMPKIN, 4, "Faceless Pumpkin");

        registerDura(Material.PUMPKIN, 4, "Faceless");

        register(Material.JACK_O_LANTERN, 0, "Jack o'Lantern");
        register(Material.JACK_O_LANTERN, 4, "Faceless Jack o'Lantern");

        registerDura(Material.JACK_O_LANTERN, 4, "Faceless");

        register(Material.MONSTER_EGGS, 0, "Stone Monster Block", "Monster Block", "Stone Monster Egg", "Monster Egg Block");
        register(Material.MONSTER_EGGS, 1, "Cobblestone Monster Block");
        register(Material.MONSTER_EGGS, 2, "Stone Brick Monster Block");
        register(Material.MONSTER_EGGS, 3, "Mossy Stone Brick Monster Block", "Mossy Brick Monster Block", "Mossy Monster Block",
                "Mossy Stone Brick Monster Egg");
        register(Material.MONSTER_EGGS, 4, "Cracked Stone Brick Monster Block", "Cracked Stone Monster Block",
                "Cracked Monster Block");
        register(Material.MONSTER_EGGS, 5, "Chiseled Stone Brick Monster Block", "Chiseled Stone Monster Block",
                "Chiseled Monster Block");

        registerDura(Material.MONSTER_EGGS, 0, "Stone");
        registerDura(Material.MONSTER_EGGS, 1, "Cobblestone", "Cobble");
        registerDura(Material.MONSTER_EGGS, 2, "Brick");
        registerDura(Material.MONSTER_EGGS, 3, "Mossy");
        registerDura(Material.MONSTER_EGGS, 4, "Cracked");
        registerDura(Material.MONSTER_EGGS, 5, "Chiseled");

        register(Material.SMOOTH_BRICK, 0, "Stone Brick", "Smooth Brick");
        register(Material.SMOOTH_BRICK, 1, "Mossy Brick", "Mossy Stone Brick", "Mossy Smooth Brick");
        register(Material.SMOOTH_BRICK, 2, "Cracked Brick", "Cracked Stone Brick", "Cracked Smooth Brick");
        register(Material.SMOOTH_BRICK, 3, "Chiseled Brick", "Chiseled Stone Brick", "Chiseled Smooth Brick");

        registerDura(Material.SMOOTH_BRICK, 0, "Stone");
        registerDura(Material.SMOOTH_BRICK, 1, "Mossy");
        registerDura(Material.SMOOTH_BRICK, 2, "Cracked");
        registerDura(Material.SMOOTH_BRICK, 3, "Chiseled");

        register(Material.PRISMARINE, 0, "Prismarine");
        register(Material.PRISMARINE, 1, "Prismarine Bricks");
        register(Material.PRISMARINE, 2, "Dark Prismarine");

        registerDura(Material.PRISMARINE, 1, "Brick", "Bricks");
        registerDura(Material.PRISMARINE, 2, "Dark");

        register(Material.SPONGE, 1, "Wet Sponge", "Soaked Sponge");

        registerDura(Material.SPONGE, 1, "Wet", "Soaked", "Dark");

        register(Material.HUGE_MUSHROOM_1, 14, "Red Mushroom Cap");
        register(Material.HUGE_MUSHROOM_1, 15, "Red Mushroom Stem");

        registerDura(Material.HUGE_MUSHROOM_1, 14, "Cap", "Red");
        registerDura(Material.HUGE_MUSHROOM_1, 15, "Stem");

        register(Material.HUGE_MUSHROOM_2, 14, "Brown Mushroom Cap");
        register(Material.HUGE_MUSHROOM_2, 15, "Brown Mushroom Stem");

        registerDura(Material.HUGE_MUSHROOM_2, 14, "Cap", "Red");
        registerDura(Material.HUGE_MUSHROOM_2, 15, "Stem");

        register(Material.COBBLE_WALL, 0, "Cobblestone Wall", "Stone Wall", "Cobble Wall");
        register(Material.COBBLE_WALL, 1, "Mossy Wall", "Mossy Stone Wall", "Mossy Cobblestone Wall");

        registerDura(Material.COBBLE_WALL, 0, "Normal");
        registerDura(Material.COBBLE_WALL, 1, "Mossy");

        register(Material.QUARTZ_BLOCK, 0, "Quartz Block");
        register(Material.QUARTZ_BLOCK, 1, "Chiseled Quartz");
        register(Material.QUARTZ_BLOCK, 2, "Quartz Pillar", "Pillar Quartz");

        registerDura(Material.QUARTZ_BLOCK, 0, "Normal");
        registerDura(Material.QUARTZ_BLOCK, 1, "Chiseled");
        registerDura(Material.QUARTZ_BLOCK, 1, "Pillar");

        register(Material.THIN_GLASS, "Glass Pane", "Thin Glass");

        register(Material.COAL, 1, "Charcoal");

        registerDura(Material.COAL, 1, "Charcoal");

        register(Material.INK_SACK, 0, "Ink Sac", "Black Dye", "Dye");
        register(Material.INK_SACK, 1, "Rose Red", "Red Dye");
        register(Material.INK_SACK, 2, "Cactus Green", "Green Dye");
        register(Material.INK_SACK, 3, "Cocoa Beans", "Brown Dye");
        register(Material.INK_SACK, 4, "Lapis Lazuli", "Blue Dye");
        register(Material.INK_SACK, 5, "Purple Dye");
        register(Material.INK_SACK, 6, "Cyan Dye");
        register(Material.INK_SACK, 7, "Light Gray Dye");
        register(Material.INK_SACK, 8, "Gray Dye");
        register(Material.INK_SACK, 9, "Pink Dye");
        register(Material.INK_SACK, 10, "Lime Dye");
        register(Material.INK_SACK, 11, "Dandelion Yellow", "Yellow Dye");
        register(Material.INK_SACK, 12, "Light Blue Dye");
        register(Material.INK_SACK, 13, "Magenta Dye", "Light Purple Dye");
        register(Material.INK_SACK, 14, "Orange Dye");
        register(Material.INK_SACK, 15, "Bonemeal", "Bone Meal", "White Dye");

        registerDura(Material.INK_SACK, 0, "Black");
        registerDura(Material.INK_SACK, 1, "Red");
        registerDura(Material.INK_SACK, 2, "Green");
        registerDura(Material.INK_SACK, 3, "Brown");
        registerDura(Material.INK_SACK, 4, "Blue");
        registerDura(Material.INK_SACK, 5, "Purple");
        registerDura(Material.INK_SACK, 6, "Cyan", "Aqua");
        registerDura(Material.INK_SACK, 7, "LightGray", "LGray");
        registerDura(Material.INK_SACK, 8, "Gray");
        registerDura(Material.INK_SACK, 9, "Pink");
        registerDura(Material.INK_SACK, 10, "Lime");
        registerDura(Material.INK_SACK, 11, "Yellow");
        registerDura(Material.INK_SACK, 12, "LightBlue", "LBlue");
        registerDura(Material.INK_SACK, 13, "Magenta", "LightPurple", "LPurple");
        registerDura(Material.INK_SACK, 14, "Orange");
        registerDura(Material.INK_SACK, 15, "White");

        register(Material.RAW_FISH, 0, "Fish", "Raw Fish");
        register(Material.RAW_FISH, 1, "Raw Salmon");
        register(Material.RAW_FISH, 2, "Clownfish");
        register(Material.RAW_FISH, 3, "Pufferfish");

        registerDura(Material.RAW_FISH, 0, "Fish");
        registerDura(Material.RAW_FISH, 1, "Salmon");
        registerDura(Material.RAW_FISH, 2, "Clown", "Clownfish");
        registerDura(Material.RAW_FISH, 3, "Puffer", "Pufferfish");

        register(Material.COOKED_FISH, 0, "Cooked Fish");
        register(Material.COOKED_FISH, 1, "Cooked Salmon", "Salmon");

        registerDura(Material.COOKED_FISH, 0, "Fish");
        registerDura(Material.COOKED_FISH, 1, "Salmon");

        register(Material.ANVIL, 1, "Slightly Damaged Anvil");
        register(Material.ANVIL, 2, "Very Damaged Anvil", "Damaged Anvil");

        registerDura(Material.ANVIL, 1, "Damaged");
        registerDura(Material.ANVIL, 2, "Broken");

        register(Material.GOLDEN_APPLE, 1, "Enchanted Golden Apple", "Enchanted Apple");

        registerDura(Material.GOLDEN_APPLE, 1, "Enchanted", "Enchant");

        register(Material.STRUCTURE_BLOCK, 0, "Save Structure Block");
        register(Material.STRUCTURE_BLOCK, 1, "Load Structure Block");
        register(Material.STRUCTURE_BLOCK, 2, "Corner Structure Block");
        register(Material.STRUCTURE_BLOCK, 3, "Data Structure Block");

        registerDura(Material.STRUCTURE_BLOCK, 0, "Save");
        registerDura(Material.STRUCTURE_BLOCK, 1, "Load");
        registerDura(Material.STRUCTURE_BLOCK, 2, "Corner");
        registerDura(Material.STRUCTURE_BLOCK, 3, "Data");

        register(Material.PORK, 0, "Raw Pork");
        register(Material.GRILLED_PORK, 0, "Cooked Pork", "Grilled Pork", "Pork");
        register(Material.RABBIT, "Raw Rabbit");
        register(Material.MUTTON, "Raw Mutton");

        register(Material.COOKED_BEEF, 0, "Cooked Beef", "Beef");
        register(Material.COOKED_CHICKEN, "Cooked Chicken", "Chicken");
        register(Material.COOKED_MUTTON, "Cooked Mutton", "Mutton");
        register(Material.COOKED_RABBIT, "Cooked Rabbit", "Rabbit");

        register(Material.MOB_SPAWNER, 0, "Monster Spawner", "Mob Spawner");

        register(Material.GOLD_RECORD, "13", "Record 13", "Music Disk 13");
        register(Material.GREEN_RECORD, "Cat", "Record Cat", "Music Disk Cat");
        register(Material.RECORD_3, "Blocks", "Record Blocks", "Music Disk Blocks");
        register(Material.RECORD_4, "Chirp", "Record Chirp", "Music Disk Chirp");
        register(Material.RECORD_5, "Far", "Record Far", "Music Disk Far");
        register(Material.RECORD_6, "Mall", "Record Mall", "Music Disk Mall");
        register(Material.RECORD_7, "Mellohi", "Record Mellohi", "Music Disk Mellohi");
        register(Material.RECORD_8, "Stal", "Record Stal", "Music Disk Stal");
        register(Material.RECORD_9, "Strad", "Record Strad", "Music Disk Strad");
        register(Material.RECORD_10, "Ward", "Record Ward", "Music Disk Ward");
        register(Material.RECORD_11, "11", "Record 11", "Music Disk 11");
        register(Material.RECORD_12, "Wait", "Record Wait", "Music Disk Wait");

        for (Material mat : Material.values())
        {
            if (_matNames.containsKey(Pair.of(mat, (short) 0)))
                continue;

            register(mat);
        }

        // TODO Something for invalid blocks
    }

    public static ArrayList<String> getCompletions(String name, boolean blockOnly)
    {
        ArrayList<String> names = new ArrayList<String>();

        for (Entry<String, Pair<Material, Short>> entry : _itemNames.entrySet())
        {
            if (!entry.getKey().toLowerCase().startsWith(name.toLowerCase()))
                continue;

            if (blockOnly && !entry.getValue().getKey().isBlock())
                continue;

            names.add(entry.getKey());
        }

        return names;
    }

    public static Short getDura(Material mat, String dura)
    {
        if (!_matDura.containsKey(mat))
            return null;

        return _matDura.get(mat).get(dura.toLowerCase());
    }

    public static Pair<Material, Short> getItem(String name)
    {
        for (Entry<String, Pair<Material, Short>> entry : _itemNames.entrySet())
        {
            if (!entry.getKey().equalsIgnoreCase(name))
                continue;

            return entry.getValue();
        }

        return null;
    }

    public static String getName(ItemStack item)
    {
        return getName(item.getType(), item.getDurability());
    }

    public static String getName(Material mat)
    {
        return getName(mat, 0);
    }

    public static String getName(Material mat, int data)
    {
        Pair<Material, Short> pair = Pair.of(mat, (short) data);

        if (_matNames.containsKey(pair))
            return _matNames.get(pair);

        pair = Pair.of(mat, (short) 0);

        if (!_matNames.containsKey(pair))
            throw new RuntimeException("Cannot find the name for " + mat.name() + ":" + data);

        return _matNames.get(pair);
    }

    private static String getProperName(Material mat)
    {
        String[] split = mat.name().split("_");

        for (int i = 0; i < split.length; i++)
        {
            split[i] = split[i].substring(0, 1) + split[i].substring(1).toLowerCase();
        }

        return UtilString.join(split, " ");
    }

    private static void register(Material... mats)
    {
        for (Material mat : mats)
        {
            register(mat, 0, getProperName(mat));
        }
    }

    private static void register(Material mat, int data, String... names)
    {
        Pair<Material, Short> pair = Pair.of(mat, (short) data);

        if (_matNames.containsKey(pair))
            throw new RuntimeException(mat.name() + ":" + data + " is already registered!");

        _matNames.put(pair, names[0]);

        for (String name : names)
        {
            if (_itemNames.containsKey(name))
                throw new RuntimeException(name + " is already registered!");

            _itemNames.put(name.replaceAll(" ", "_"), pair);
        }
    }

    private static void register(Material mat, String name, String... names)
    {
        names = Arrays.copyOf(names, names.length + 1);
        names[names.length - 1] = name;

        register(mat, 0, names);
    }

    private static void registerDura(Material mat, int data, String... names)
    {
        if (!_matDura.containsKey(mat))
            _matDura.put(mat, new HashMap<String, Short>());

        for (String name : names)
        {
            _matDura.get(mat).put(name.toLowerCase(), (short) data);
        }
    }
}
