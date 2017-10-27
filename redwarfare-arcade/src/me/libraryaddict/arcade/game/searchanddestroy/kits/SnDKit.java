package me.libraryaddict.arcade.game.searchanddestroy.kits;

import java.util.ArrayList;
import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.searchanddestroy.FuseType;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.utils.UtilInv;

public abstract class SnDKit extends Kit
{
    public SnDKit(String name, KitAvailibility availibility, String[] desc, Ability... abilities)
    {
        super(name, availibility, desc, abilities);
    }

    public SnDKit(String name, String[] desc, Ability... abilities)
    {
        super(name, desc, abilities);
    }

    public ItemStack buildFuse(FuseType type, int level)
    {
        ItemBuilder builder = new ItemBuilder(Material.BLAZE_POWDER);
        builder.setTitle(C.Gold + "Bomb Fuse");
        builder.addLore(C.Purple + C.Bold + "RIGHT CLICK BOMB TO ARM AND DISARM");

        if (type != null)
        {
            return type.setLevel(builder.build(), level);
        }

        return builder.build();
    }

    @Override
    public void setItems(ItemStack... items)
    {
        if (!UtilInv.isItem(items[0], Material.BLAZE_POWDER))
        {
            ArrayList<ItemStack> item = new ArrayList<ItemStack>(Arrays.asList(items));
            item.add(0, buildFuse(null, 0));
            items = item.toArray(new ItemStack[0]);
        }

        super.setItems(items);
    }
}
