package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.VampireAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitVampire extends SnDKit
{

    public KitVampire()
    {
        super("Vampire", new String[]
            {
                    "A kit that must pick its early kills carefully, as it only starts with 8 hearts. However, every kill grants it a vital reward of 2 hearts, with a maximum of 20."
            }, new VampireAbility());

        setPrice(300);
        setItems(new ItemBuilder(Material.IRON_SWORD).setTitle(C.Red + "Vampiric Sword").addEnchantment(Enchantment.DAMAGE_ALL, 1).build());
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
        return new ItemBuilder(Material.INK_SACK, 1, (short) 1).build();
    }

    @Override
    public Material getMaterial()
    {
        return null;
    }

}
