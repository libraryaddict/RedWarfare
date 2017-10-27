package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitLongbow extends SnDKit
{

    public KitLongbow()
    {
        super("Longbow", new String[]
            {
                    "A supporting archer, this kit receives a bow that will send enemies flying."
            });

        setItems(new ItemStack(Material.STONE_SWORD), new ItemBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE, 1)
                .addEnchantment(Enchantment.ARROW_KNOCKBACK, 3).build(), new ItemStack(Material.ARROW));
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.CHAINMAIL_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.CHAINMAIL_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.BOW;
    }

}
