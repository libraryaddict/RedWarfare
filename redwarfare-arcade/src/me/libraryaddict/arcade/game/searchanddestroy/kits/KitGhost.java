package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.GhostAbility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitGhost extends SnDKit
{

    public KitGhost(JavaPlugin plugin)
    {
        super("Ghost", new String[]
            {
                    "This kit is invisible and can sneak around unseen by the enemy, but its invisibility is not perfect. Held items, sprint particles, flames, and arrows will all reveal a ghost's presence. While it has no armor, it is given enderpearls to help travel around and make hasty escapes."
            }, new GhostAbility(plugin));

        setPrice(150);

        setItems(new ItemBuilder(Material.GOLD_SWORD).addEnchantment(Enchantment.DAMAGE_ALL, 2).build(), null,
                new ItemStack(Material.ENDER_PEARL, 4));
    }

    public boolean canTeleportTo()
    {
        return false;
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[4];
    }

    @Override
    public Material getMaterial()
    {
        return Material.GHAST_TEAR;
    }

}
