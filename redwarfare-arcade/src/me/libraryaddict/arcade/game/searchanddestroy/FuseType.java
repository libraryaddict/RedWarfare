package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilNumber;
import net.md_5.bungee.api.ChatColor;

public enum FuseType
{
    BOMB_ARMING("Bomb Arming"), BOMB_DEFUSING("Bomb Defusing"), BOMB_SPEED("Bomb Speed");

    public static FuseType getFuseType(ItemStack item)
    {
        ArrayList<String> lore = new ItemBuilder(item).getLore();

        for (String s : lore)
        {
            for (FuseType type : FuseType.values())
            {
                if (!type.isFuseLine(s))
                    continue;

                return type;
            }
        }

        return null;
    }

    private String _fuseName;

    private FuseType(String fuseName)
    {
        _fuseName = fuseName;
    }

    public int getLevel(ItemStack item)
    {
        ArrayList<String> lore = new ItemBuilder(item).getLore();

        for (String s : lore)
        {
            if (!isFuseLine(s))
                continue;

            s = s.substring(s.lastIndexOf(" ") + 1);

            return UtilNumber.convertFromRoman(s);
        }

        return 0;
    }

    private boolean isFuseLine(String string)
    {
        return ChatColor.stripColor(string).contains(_fuseName);
    }

    public ItemStack setLevel(ItemStack item, int level)
    {
        for (FuseType type : values())
        {
            if (type == this)
                continue;

            int oldLevel = type.getLevel(item);

            if (oldLevel <= 0)
                continue;

            type.setLevel(item, 0);

            if (type == FuseType.BOMB_SPEED)
            {
                if (this == FuseType.BOMB_ARMING)
                {
                    FuseType.BOMB_DEFUSING.setLevel(item, oldLevel);
                }
                else if (this == FuseType.BOMB_DEFUSING)
                {
                    FuseType.BOMB_ARMING.setLevel(item, oldLevel);
                }
            }
        }

        ItemMeta meta = item.getItemMeta();

        ArrayList<String> lore = new ArrayList<String>(meta.getLore());

        int contained = 0;

        for (FuseType fuseType : FuseType.values())
        {
            if (fuseType != this && fuseType.getLevel(item) > 0)
            {
                contained = 1;
            }
        }

        for (int i = 0; i < lore.size(); i++)
        {
            String s = lore.get(i);

            if (!isFuseLine(s))
                continue;

            if (contained == 0)
                contained = 2;

            lore.remove(i);
        }

        if (level > 0)
        {
            meta.addEnchant(UtilInv.getVisual(), 1, true);

            if (contained == 0)
                lore.add("");

            lore.add(C.Gray + _fuseName + " " + UtilNumber.convertToRoman(level));
        }
        else
        {
            if (contained == 2 && !lore.isEmpty() && lore.get(lore.size() - 1).isEmpty())
                lore.remove(lore.size() - 1);

            meta.removeEnchant(UtilInv.getVisual());
        }

        meta.setLore(lore);

        item.setItemMeta(meta);

        return item;
    }
}
