package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.SpyAbility;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitSpy extends SnDKit
{
    public KitSpy()
    {
        super("Spy", new String[]
            {
                    "A sneaky kit that always looks like the team of the view. He looks like a trooper!"
            }, new SpyAbility());

        setPrice(250);
        setItems(new ItemBuilder(Material.IRON_SWORD).build());
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.IRON_BOOTS).build(), new ItemStack(Material.IRON_LEGGINGS),
                    new ItemStack(Material.IRON_CHESTPLATE), new ItemStack(Material.IRON_HELMET)
            };
    }

    @Override
    public Material[] getArmorMats()
    {
        return null;
    }

    public Kit getHiddenKit()
    {
        return getManager().getGame().getDefaultKit();
    }

    @Override
    public Material getMaterial()
    {
        return Material.SKULL_ITEM;
    }

}
