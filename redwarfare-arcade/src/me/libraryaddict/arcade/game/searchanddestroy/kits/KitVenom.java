package me.libraryaddict.arcade.game.searchanddestroy.kits;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionType;

import me.libraryaddict.arcade.game.searchanddestroy.abilities.VenomAbility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;

public class KitVenom extends SnDKit
{

    public KitVenom()
    {
        super("Venom", new String[]
            {
                    "An offensive kit that excels at hit and runs. Its blade is coated in deadly poison and paralyzer, making every hit it lands poison its foe and slow them down!"
            }, new VenomAbility());

        setPrice(400);
        setItems(new ItemBuilder(Material.IRON_SWORD).setTitle(C.DGreen + "Poisoned Sword").addDullEnchant()
                .addLore("", C.Gray + "Venom I").build());
    }

    @Override
    public Material[] getArmorMats()
    {
        return new Material[]
            {
                    Material.IRON_BOOTS, Material.IRON_LEGGINGS, Material.GOLD_CHESTPLATE, Material.CHAINMAIL_HELMET
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemBuilder(Material.POTION).setPotionType(PotionType.POISON).build();
    }

    @Override
    public Material getMaterial()
    {
        return null;
    }

}
