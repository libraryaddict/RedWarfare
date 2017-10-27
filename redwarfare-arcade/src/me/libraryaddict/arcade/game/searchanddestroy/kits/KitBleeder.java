package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.BleederAbility;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitBleeder extends SnDKit
{
    public KitBleeder()
    {
        super("Bleeder", new String[]
            {
                    "One of the most feared kits, the bleeder can permanently destroy the health of his foes with every blow"
            }, new BleederAbility());

        setPrice(400);

        setItems(new ItemBuilder(Material.WOOD_SWORD).setTitle(C.DRed + "The Bleeder").build());
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.CHAINMAIL_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE,
                    Material.CHAINMAIL_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.WOOD_SWORD;
    }

}
