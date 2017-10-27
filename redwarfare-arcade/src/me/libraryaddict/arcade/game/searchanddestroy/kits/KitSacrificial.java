package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.SacrificialAbility;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitSacrificial extends SnDKit
{

    public KitSacrificial()
    {
        super("Sacrificial", KitAvailibility.Staff, new String[]
            {
                    "This kit will lets the user sacrifice their health in place of one of their allies. When being sacrificed for, the ally will transfer all damage taken to the sacrificial, who must consume their every replenishing steak to survive. Be cautious, for the sacrificial will take 5x more damage when directly attacked!"
            }, new SacrificialAbility());

        setPrice(500);

        setItems(new ItemBuilder(Material.WOOD_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1).build(),
                new ItemBuilder(Material.INK_SACK, 1, (short) 1).setTitle(C.Red + "Sacrificial Magic").build(),
                new ItemStack(Material.COOKED_BEEF, 10),
                new ItemBuilder(Material.SHEARS, 2).setTitle(C.Red + "Destroy Sacrificial Bonds").build());
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.IRON_HELMET
            };
    }

    public Kit getHiddenKit()
    {
        return getManager().getGame().getDefaultKit();
    }

    @Override
    public Material getMaterial()
    {
        return Material.COOKED_BEEF;
    }

}
