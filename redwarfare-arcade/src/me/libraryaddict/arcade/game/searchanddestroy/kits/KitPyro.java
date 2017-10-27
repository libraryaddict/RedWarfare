package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitPyro extends SnDKit
{

    public KitPyro()
    {
        super("Pyro", new String[]
            {
                    "A master of fire, this kit is given a flaming bow and sword, and fire resistant leather armor."
            });

        setPrice(200);
        setItems(
                new ItemBuilder(Material.IRON_SWORD).addEnchantment(Enchantment.FIRE_ASPECT, 2).setTitle(C.Red + "Molten Sword")
                        .build(),
                new ItemBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_FIRE, 2).setTitle(C.Red + "Charred Bow")
                        .addEnchantment(Enchantment.ARROW_INFINITE, 1).build(),
                new ItemStack(Material.ARROW));
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.CHAINMAIL_BOOTS).addEnchantment(Enchantment.PROTECTION_FIRE, 1)
                            .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 2)
                            .addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1).build(),
                    new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.RED).addEnchantment(Enchantment.PROTECTION_FIRE, 1)
                            .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 2)
                            .addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1).build(),
                    new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.RED)
                            .addEnchantment(Enchantment.PROTECTION_FIRE, 1).addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 2)
                            .addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1).build(),
                    new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.RED).addEnchantment(Enchantment.PROTECTION_FIRE, 1)
                            .addEnchantment(Enchantment.PROTECTION_EXPLOSIONS, 2)
                            .addEnchantment(Enchantment.PROTECTION_PROJECTILE, 1).build()
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
        return Material.FLINT_AND_STEEL;
    }
}
