package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.DwarfAbility;

public class KitDwarf extends SnDKit
{

    public KitDwarf()
    {
        super("Dwarf", new String[]
            {
                    "A defensive kit that gains power by sneaking. As it levels up, it gets stronger and stronger, but there is a cost. The more powerful a dwarf is, the slower it gets. When not sneaking, it will lose power at the same rate as it gained, with a much higher cost for sprinting."
            }, new DwarfAbility());

        setPrice(500);
        setItems(new ItemStack(Material.IRON_SWORD));
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
        return Material.EXP_BOTTLE;
    }

}
