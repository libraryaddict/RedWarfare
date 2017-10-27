package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.ExplosiveAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitExplosive extends SnDKit
{

    public KitExplosive()
    {
        super("Explosive", new String[]
            {
                    "An offensive kit equipped with RPGs and grenades. When launched, RPGs will soar through the air until they make contact, then explode. Beware, because explosives can be damaged by their own RPGs. Grenades will explode a short time after being thrown. Though they can't be thrown as far or deal as much damage, you will get more over time and can throw multiple at once."
            }, new ExplosiveAbility());

        setPrice(150);

        setItems(new ItemBuilder(Material.STONE_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1).build(),

        new ItemBuilder(Material.EGG).setTitle(C.Yellow + "RPG").setAmount(5).build(),

        new ItemBuilder(Material.FIREWORK_CHARGE).setTitle(C.Yellow + "Grenade").setAmount(10)
                .setColor(TeamSettings.RED.getBukkitColor()).build());
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.DIAMOND_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.DIAMOND_HELMET
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemBuilder(Material.FIREWORK_CHARGE).setColor(Color.RED).build();
    }

    @Override
    public ItemStack[] getItems(Player player)
    {
        return new ItemStack[]
            {
                    buildFuse(null, 0),

                    new ItemBuilder(Material.STONE_SWORD).addEnchantment(Enchantment.KNOCKBACK, 1).build(),

                    new ItemBuilder(Material.EGG).setTitle(C.Yellow + "RPG").setAmount(5).build(),

                    new ItemBuilder(Material.FIREWORK_CHARGE).setTitle(C.Yellow + "Grenade").setAmount(10)
                            .setColor(getManager().getGame().getTeam(player).getColor()).build()
            };
    }

    @Override
    public Material getMaterial()
    {
        return null;
    }

}
