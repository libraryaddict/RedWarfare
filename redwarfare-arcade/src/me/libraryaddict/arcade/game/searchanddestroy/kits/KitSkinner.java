package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.SkinnerAbility;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitSkinner extends SnDKit
{
    public KitSkinner()
    {
        super("Skinner", new String[]
            {
                    "A stealth fighter kit which gains the nametag and nametag color of any enemy slain nearby.",
                    "Much like an adaptive Spy.", "Every kill they are defenseless for 3 seconds."
            }, new SkinnerAbility());

        setPrice(300);

        setItems(new ItemBuilder(Material.IRON_SWORD).addEnchantment(Enchantment.SILK_TOUCH, 1).build());
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
    public ItemStack getIcon()
    {
        return new ItemBuilder(getMaterial()).setColor(Color.fromRGB(255, 218, 185)).build();
    }

    @Override
    public Material getMaterial()
    {
        return Material.LEATHER_CHESTPLATE;
    }

}
