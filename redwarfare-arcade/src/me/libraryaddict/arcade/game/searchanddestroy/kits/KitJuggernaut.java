package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.JuggernautAbility;

public class KitJuggernaut extends SnDKit
{

    public KitJuggernaut(JavaPlugin plugin)
    {
        super("Juggernaut", new String[]
            {
                    "A defensive kit that will only take a fraction of the normal melee and ranged damage it would normally take with an extra 5 hearts!",
                    "Juggernauts however will still suffer from the damage it would normally take from other sources, such as fall.",
                    "Additionally, due to its heavy armor, it's hard to knock a juggernaut back.",
                    "This comes at great cost to its mobility, as it cannot sprint."
            }, new JuggernautAbility(plugin));

        setPrice(350);
        setItems(new ItemStack(Material.STONE_SWORD));
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.IRON_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.DIAMOND_CHESTPLATE;
    }

}
