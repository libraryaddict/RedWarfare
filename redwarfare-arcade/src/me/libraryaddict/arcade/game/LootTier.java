package me.libraryaddict.arcade.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.utils.RandomItem;
import me.libraryaddict.core.utils.UtilMath;

public class LootTier implements Listener
{
    private ArrayList<RandomItem> _randomItems = new ArrayList<RandomItem>();
    private HashMap<Integer, ArrayList<RandomItem>> _slotItems = new HashMap<Integer, ArrayList<RandomItem>>();

    public void addLoot(ItemStack itemstack, double chance)
    {
        addLoot(new RandomItem(itemstack, chance));
    }

    public void addLoot(ItemStack itemstack, int minAmount, int maxAmount, double chance)
    {
        addLoot(new RandomItem(itemstack, minAmount, maxAmount, chance));
    }

    public void addLoot(Material material, double chance)
    {
        addLoot(new RandomItem(material, chance));
    }

    public void addLoot(Material material, int minAmount, int maxAmount, double chance)
    {
        addLoot(new RandomItem(material, minAmount, maxAmount, chance));
    }

    public void addLoot(RandomItem... items)
    {
        _randomItems.addAll(Arrays.asList(items));
    }

    public void addSlotLoot(int slot, RandomItem item)
    {
        if (!_slotItems.containsKey(slot))
            _slotItems.put(slot, new ArrayList<RandomItem>());

        _slotItems.get(slot).add(item);
    }

    public LootTier clone()
    {
        LootTier lootManager = new LootTier();

        for (RandomItem randomItem : _randomItems)
        {
            lootManager.addLoot(randomItem);
        }

        return lootManager;
    }

    public Pair<Integer, ItemStack> getFurnaceLoot()
    {
        if (UtilMath.r(3) == 0)
            return null;

        int ran = UtilMath.r(3);

        if (ran == 0)
            ran = 1;
        else if (ran < 4)
            ran = 0;
        else
            ran = 2;

        if (!_slotItems.containsKey(ran))
            return null;

        RandomItem item = UtilMath.r(_slotItems.get(ran));

        return Pair.of(ran, item.getItem());
    }

    public ArrayList<ItemStack> getLoot(int minAmount, int maxAmount)
    {
        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();
        int amount = UtilMath.r(minAmount, maxAmount);

        while (loot.size() < amount)
        {
            RandomItem item = UtilMath.r(_randomItems);

            if (!item.hasChance())
                continue;

            loot.add(item.getItem());
        }

        return loot;
    }

    public boolean hasSlotLoot()
    {
        return !_slotItems.isEmpty();
    }
}
