package me.libraryaddict.core.cosmetics.types.disguises;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.cosmetics.types.CosmeticDisguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;

public class CosmeticSheep extends CosmeticDisguise
{
    public CosmeticSheep()
    {
        super("Sheep Disguise");
    }

    @Override
    public String[] getDescription()
    {
        return new String[]
            {
                    "You've always felt a little wooly, maybe a few baa's leaked from your lips as you idly munch on stalks of grass..",
                    "But that doesn't mean anything, right?"
            };
    }

    @Override
    public DisguiseType getDisguise()
    {
        return DisguiseType.SHEEP;
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(Material.WOOL);
    }

}
