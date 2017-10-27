package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitTrooper extends SnDKit
{

    public KitTrooper()
    {
        super("Trooper", new String[]
            {
                    "The kit that sets the standards for all others. It receives a set of iron armor and an iron sword, along with five golden apples with which it can quickly recover from fights."
            }, new Ability[0]);

        setItems(new ItemBuilder(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).build(),
                new ItemStack(Material.GOLDEN_APPLE, 3));
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.IRON_SWORD;
    }

}
