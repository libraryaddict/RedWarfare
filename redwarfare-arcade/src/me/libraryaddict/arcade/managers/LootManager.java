package me.libraryaddict.arcade.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.LootEvent;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.LootTier;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;

public class LootManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;
    private HashMap<Vector, Hologram> _looted = new HashMap<Vector, Hologram>();
    private HashMap<String, LootTier> _lootManager = new HashMap<String, LootTier>();

    public LootManager(JavaPlugin plugin, ArcadeManager arcadeManager)
    {
        super(plugin, "Loot Manager");

        _arcadeManager = arcadeManager;
    }

    public void fillWithLoot(Block block, LootTier loot)
    {
        ArrayList<Block> blocks = getBlocks(block);

        for (Block b : blocks)
        {
            InventoryHolder holder = (InventoryHolder) b.getState();

            fillWithLoot(holder.getInventory(), loot);

            ((BlockState) holder).update(true);
        }
    }

    public void fillWithLoot(Inventory inv, LootTier loot)
    {
        inv.clear();

        if (inv.getViewers().isEmpty())
        {
            InventoryHolder holder = inv.getHolder();

            if (holder instanceof BlockState)
            {
                Block block = null;

                if (inv.getHolder() instanceof DoubleChest)
                {
                    block = ((BlockState) ((DoubleChest) holder).getLeftSide()).getBlock();
                }
                else
                {
                    block = ((BlockState) holder).getBlock();
                }

                block = getBlock(block);

                Vector cords = getCords(block);

                if (_looted.containsKey(cords))
                {
                    _looted.remove(cords).stop();
                }
            }
        }

        if (loot.hasSlotLoot())
        {
            Pair<Integer, ItemStack> item = loot.getFurnaceLoot();

            if (item == null)
                return;

            if (item.getKey() >= inv.getSize())
                return;

            inv.setItem(item.getKey(), item.getValue());
            return;
        }

        Pair<Integer, Integer> amount = getArcade().getGame().getOption(GameOption.LOOT_AMOUNT);

        ArrayList<ItemStack> items = loot.getLoot(amount.getKey(), amount.getValue());
        ArrayList<Integer> slots = UtilMath.getList(inv.getSize());

        Iterator<ItemStack> itel = items.iterator();

        while (!slots.isEmpty() && itel.hasNext())
        {
            int slot = UtilMath.r(slots);
            slots.remove((Integer) slot);

            inv.setItem(slot, itel.next());
        }
    }

    public ArcadeManager getArcade()
    {
        return _arcadeManager;
    }

    public Block getBlock(Block block)
    {
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
        {
            for (int x = -1; x <= 0; x++)
            {
                for (int z = -1; z <= 0; z++)
                {
                    if (Math.abs(x) + Math.abs(z) != 1)
                        continue;

                    Block b = block.getRelative(x, 0, z);

                    if (b.getType() != block.getType())
                        continue;

                    return b;
                }
            }
        }

        return block;
    }

    public ArrayList<Block> getBlocks(Block block)
    {
        ArrayList<Block> blocks = new ArrayList<Block>();
        blocks.add(block);

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST)
        {
            for (int x = -1; x <= 1; x++)
            {
                for (int z = -1; z <= 1; z++)
                {
                    if (Math.abs(x) + Math.abs(z) != 1)
                        continue;

                    Block b = block.getRelative(x, 0, z);

                    if (b.getType() != block.getType())
                        continue;

                    blocks.add(b);
                }
            }
        }

        return blocks;
    }

    public Vector getCords(Block block)
    {
        return new Vector(block.getX(), block.getY(), block.getZ());
    }

    public LootTier getLoot(String name)
    {
        if (!_lootManager.containsKey(name))
        {
            _lootManager.put(name, new LootTier());
        }

        return _lootManager.get(name);
    }

    public boolean isLooted(Block block)
    {
        return _looted.containsKey(getCords(block));
    }

    @EventHandler
    public void onGameEnd(GameStateEvent event)
    {
        if (event.getState() != GameState.Dead)
        {
            return;
        }

        _looted.clear();
        _lootManager.clear();
    }

    @EventHandler
    public void onOpen(InventoryOpenEvent event)
    {
        if (!getArcade().getGame().getOption(GameOption.CHEST_LOOT))
            return;

        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof BlockState))
            return;

        Block block = null;

        if (event.getInventory().getHolder() instanceof DoubleChest)
        {
            block = ((BlockState) ((DoubleChest) holder).getLeftSide()).getBlock();
        }
        else
        {
            block = ((BlockState) holder).getBlock();
        }

        block = getBlock(block);

        if (isLooted(block))
            return;

        ArrayList<Block> blocks = getBlocks(block);

        ArrayList<Location> locs = new ArrayList<Location>();

        for (Block b : blocks)
        {
            locs.add(b.getLocation().add(0.5, 1.03, 0.5));
        }

        Hologram looted = new Hologram(UtilLoc.getAverage(locs), C.Gold + C.Bold + "Looted");
        looted.setViewDistance(20);
        looted.start();

        for (Block b : blocks)
        {
            _looted.put(getCords(b), looted);
        }

        Bukkit.getPluginManager().callEvent(new LootEvent((Player) event.getPlayer(), block));
    }

    public void setLoot(String name, LootTier lootManager)
    {
        _lootManager.put(name, lootManager);
    }

}
