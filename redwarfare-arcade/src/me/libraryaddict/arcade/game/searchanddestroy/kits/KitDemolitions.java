package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import me.libraryaddict.arcade.game.searchanddestroy.FuseType;
import me.libraryaddict.arcade.game.searchanddestroy.abilities.DemolitionsAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitDemolitions extends SnDKit
{

    public KitDemolitions()
    {
        super("Demolitions", new String[]
            {
                    "A kit equipped with a flaming sword and two landmines which it can set. When triggered by an enemy, they will explode, dealing severe damage! Additionally, it can fuse and defuse bombs twice as fast!"
            }, new DemolitionsAbility());

        setPrice(300);
        setItems(
                buildFuse(FuseType.BOMB_SPEED, 5), new ItemBuilder(Material.IRON_SWORD).setTitle(C.Red + "Flaming Sword")
                        .addEnchantment(Enchantment.FIRE_ASPECT, 2).build(),
                new ItemBuilder(Material.STONE_PLATE, 2).setTitle(C.Yellow + "Landmine").build());
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.GOLD_BOOTS, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_HELMET
            };
    }

    @Override
    public Material getMaterial()
    {
        return Material.STONE_PLATE;
    }

}
