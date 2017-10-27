package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.EqualityAbility;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitEquality extends SnDKit
{
    public KitEquality()
    {
        super("Equality", KitAvailibility.Locked, new String[]
            {
                    "You will always take 3 damage, you will always deal 3 damage. You were born equal and you will die equal."
            }, new EqualityAbility());

        setPrice(350);
        
        setItems(new ItemStack(Material.IRON_SWORD));
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.WHITE).build(),
                    new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.BLACK).build(),
                    new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.WHITE).build(),
                    new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.BLACK).build()
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
        return Material.STEP;
    }

}
