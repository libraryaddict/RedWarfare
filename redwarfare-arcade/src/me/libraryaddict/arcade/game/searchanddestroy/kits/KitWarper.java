package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.WarperAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitWarper extends SnDKit
{

    public KitWarper()
    {
        super("Warper", new String[]
            {
                    "A supportive kit, it has the ability to warp a group of enemies to where it was standing 10 seconds ago, making it useful against an otherwise strong defense."
            }, new WarperAbility());

        setPrice(250);
        setItems(new ItemBuilder(Material.IRON_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 1).build(),
                new ItemBuilder(Material.BLAZE_ROD).setTitle(C.Gold + "Warper's Wand").build());
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
        return Material.BLAZE_ROD;
    }
}
