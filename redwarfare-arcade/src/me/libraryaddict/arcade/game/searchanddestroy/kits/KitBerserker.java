package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.BerserkerAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitBerserker extends SnDKit
{

    public KitBerserker()
    {
        super("Berserker", new String[]
            {
                    "An offensive kit with a powerful axe and low armor. Its damage increases by half a heart every kill as it works up a bloodlust."
            }, new BerserkerAbility());

        setPrice(400);
        setItems(new ItemBuilder(Material.IRON_AXE).addEnchantment(Enchantment.DAMAGE_ALL, 2).build(),
                new ItemStack(Material.COOKED_BEEF, 2));
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, null, Material.IRON_CHESTPLATE, Material.IRON_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.DIAMOND_AXE;
    }

}
