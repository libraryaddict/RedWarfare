package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.FrostAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitFrost extends SnDKit
{
    public KitFrost()
    {
        super("Frost", new String[]
            {
                    "Master of ice and frost, this special guy can freeze the hands of your enemies for 10 seconds!",
                    "When frozen, your enemies cannot switch items or use their offhand!"
            }, new FrostAbility());

        setPrice(150);

        setItems(new ItemStack(Material.IRON_SWORD));
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.AQUA).build(),
                    new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.AQUA).build(),
                    new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.AQUA).build(),
                    new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.AQUA).build()
            };
    }

    @Override
    public Material[] getArmorMats()
    {
        return null;
    }

    @Override
    public Material getMaterial()
    {
        return Material.ICE;
    }

}
