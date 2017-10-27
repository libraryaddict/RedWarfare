package me.libraryaddict.core.data;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.C;

public enum TeamSettings
{
    BLUE("Blue Team", "Blue", C.Blue, C.DBlue, new ItemStack(Material.DIAMOND_ORE), Color.BLUE, DyeColor.BLUE,
            ParticleColor.BLUE),

    GREEN("Green Team", "Green", C.DGreen, C.DGreen, new ItemStack(Material.LEAVES), Color.GREEN, DyeColor.GREEN,
            ParticleColor.GREEN),

    PLAYER("Players", "Players", "", "", new ItemStack(Material.SKULL_ITEM, 1, (short) 3), Color.WHITE, DyeColor.WHITE,
            ParticleColor.WHITE),

    PURPLE("Purple Team", "Purple", C.DPurple, C.DPurple, new ItemStack(Material.OBSIDIAN), Color.PURPLE, DyeColor.PURPLE,
            ParticleColor.PURPLE),

    RED("Red Team", "Red", C.Red, C.DRed, new ItemStack(Material.TNT), Color.RED, DyeColor.RED, ParticleColor.RED),

    YELLOW("Yellow Team", "Yellow", C.Yellow, C.Gold, new ItemStack(Material.GOLD_BLOCK), Color.YELLOW, DyeColor.YELLOW,
            ParticleColor.YELLOW);

    private Color _bukkitColor;
    private String _color;
    private String _darkColor;
    private DyeColor _dyeColor;
    private ItemStack _hat;
    private String _name;
    private ParticleColor _particle;
    private String _simpleName;

    private TeamSettings(String name, String simpleName, String color, String darkColor, ItemStack hat, Color bukkitColor,
            DyeColor dyeColor, ParticleColor particle)
    {
        _darkColor = darkColor;
        _simpleName = simpleName;
        _name = name;
        _color = color;
        _dyeColor = dyeColor;
        _hat = hat;
        _bukkitColor = bukkitColor;
        _particle = particle;
    }

    public Color getBukkitColor()
    {
        return _bukkitColor;
    }

    public String getColor()
    {
        return _color;
    }

    public DyeColor getDyeColor()
    {
        return _dyeColor;
    }

    public ItemStack getHat()
    {
        return _hat;
    }

    public String getName()
    {
        return _name;
    }

    public ParticleColor getParticleColor()
    {
        return _particle;
    }

    public String getSimpleName()
    {
        return _simpleName;
    }

}
