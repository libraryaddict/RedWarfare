package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitShortbow extends SnDKit
{

    public KitShortbow()
    {
        super("Shortbow", new String[]
            {
                    "A powerful archer, this kit receives a bow with which it deals precise damage."
            });

        setItems(new ItemBuilder(Material.WOOD_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1).build(),
                new ItemBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE, 1)
                        .addEnchantment(Enchantment.ARROW_DAMAGE, 2).build(),
                new ItemStack(Material.ARROW));
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
