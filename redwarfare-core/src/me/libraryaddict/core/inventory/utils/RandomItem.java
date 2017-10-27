package me.libraryaddict.core.inventory.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilMath;

public class RandomItem {
    private double _chance;
    private ItemStack _item;
    private int _min, _max;
    private boolean _uniqueItem;

    public RandomItem(ItemStack item, double chance) {
        this(item, 1, 1, chance);
    }

    public RandomItem(ItemStack item, int minAmount, int maxAmount, double chance) {
        _item = item;
        _min = minAmount;
        _max = maxAmount;
        _chance = chance;
    }

    public RandomItem(Material mat, double chance) {
        this(new ItemStack(mat), chance);
    }

    public RandomItem(Material mat, int minAmount, int maxAmount, double chance) {
        this(new ItemStack(mat), minAmount, maxAmount, chance);
    }

    public ItemStack getItem() {
        ItemStack item = _item.clone();

        item.setAmount(UtilMath.r(_min, _max));

        if (_uniqueItem) {
            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();

                meta.setDisplayName(meta.getDisplayName() + UtilInv.getUniqueId());

                item.setItemMeta(meta);
            }
        }

        return item;
    }

    public boolean hasChance() {
        return UtilMath.getDouble() < _chance;
    }

    public RandomItem setUnique() {
        _uniqueItem = true;

        return this;
    }
}
