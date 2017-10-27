package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.InertiaAbility;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitInertia extends SnDKit
{
    public KitInertia()
    {
        super("Inertia", KitAvailibility.Locked, new String[]
            {
                    " An unstoppable force that can be as much danger to itself as it is to others.",
                    "Damage increases as you run, sharp turns negate this and running into a wall can be quite painful.."
            }, new InertiaAbility());

        setItems(new ItemStack(Material.WOOD_SWORD), new ItemStack(Material.SHIELD));
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.IRON_BOOTS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                    new ItemBuilder(Material.IRON_LEGGINGS).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                    new ItemBuilder(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build(),
                    new ItemBuilder(Material.IRON_HELMET).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 2).build()
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.QUARTZ;
    }

    @Override
    public Material[] getArmorMats()
    {
        return null;
    }

}
