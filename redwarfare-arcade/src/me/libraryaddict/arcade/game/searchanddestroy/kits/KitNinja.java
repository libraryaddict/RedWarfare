package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.NinjaAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitNinja extends SnDKit
{
    public KitNinja()
    {
        super("Ninja", new String[]
            {
                    "The ninja starts with enderpearls and can sprint about the map with a speed boost",
                    "He is known as one who can outrun the stronger warriors", "With fall protection, he's not afraid of heights!"
            }, new NinjaAbility());

        setPrice(200);

        setItems(new ItemStack(Material.IRON_SWORD), new ItemStack(Material.ENDER_PEARL, 6));
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemBuilder(Material.CHAINMAIL_BOOTS).addEnchantment(Enchantment.PROTECTION_FALL, 2).build(),
                    new ItemStack(Material.CHAINMAIL_LEGGINGS), new ItemStack(Material.IRON_CHESTPLATE),
                    new ItemStack(Material.IRON_HELMET)
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
        return Material.ENDER_PEARL;
    }

}
