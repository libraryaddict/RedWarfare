package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.TeleporterAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitTeleporter extends SnDKit
{
    public KitTeleporter()
    {
        super("Teleporter", new String[]
            {
                    "A supportive kit that can create teleporters to transfer their team across the map. Step on one and sneak to use it! Beware, the enemies can easily break the teleporters. "
            }, new TeleporterAbility());

        setItems(new ItemStack(Material.IRON_SWORD),
                new ItemBuilder(Material.QUARTZ).setTitle(C.Blue + "Teleport Creator")
                        .addLore(C.Green + "Right click on ground to create teleporter").build(),
                new ItemBuilder(Material.EMPTY_MAP).setTitle(C.Blue + "Remote Teleporter")
                        .addLore(
                                C.Green + "Right click with this to open a menu where you can select one of your teammates to create a teleporter to",
                                C.Green + "You can also destroy all existing teleporters with this!")
                        .build());
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
        return Material.MOB_SPAWNER;
    }

}
