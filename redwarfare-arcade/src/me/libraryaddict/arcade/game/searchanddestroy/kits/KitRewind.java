package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.RewinderAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitRewind extends SnDKit
{

    public KitRewind()
    {
        super("Rewind", new String[]
            {
                    "This kit is a master of hitting and running, as it has a magical clock that will teleport it where it was 30 seconds in the past. While health is unchanged, if the user was on fire, it will be put out like it never existed."
            }, new RewinderAbility());

        setItems(new ItemBuilder(Material.IRON_SWORD).build(),
                new ItemBuilder(Material.WATCH).setTitle(C.Gold + "Time Machine").build());
    }

    @Override
    public ItemStack[] getArmor()
    {
        return new ItemStack[]
            {
                    new ItemStack(Material.IRON_BOOTS), new ItemStack(Material.IRON_LEGGINGS),
                    new ItemBuilder(Material.IRON_CHESTPLATE).addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1).build(),
                    new ItemStack(Material.CHAINMAIL_HELMET)
            };
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.IRON_CHESTPLATE, Material.CHAINMAIL_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.WATCH;
    }
}
