package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.GhostAbility;
import me.libraryaddict.arcade.game.searchanddestroy.abilities.WraithAbility;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitWraith extends SnDKit
{

    public KitWraith(JavaPlugin plugin)
    {
        super("Wraith", new String[]
            {
                    "A kit that is extremely similar to what Ghost is, however this kit functions differently!", "",
                    "Inferior to a normal ghost, wraith has no enderpearls or sword but instead must make do with a bow with power II!"
            }, new GhostAbility(plugin), new WraithAbility());

        setPrice(200);

        setItems(new ItemBuilder(Material.BOW).addEnchantment(Enchantment.ARROW_INFINITE, 1)
                .addEnchantment(Enchantment.ARROW_DAMAGE, 2).build(), new ItemStack(Material.ARROW));
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
        return Material.TIPPED_ARROW;
    }

}
