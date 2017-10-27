package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.TeamBomb;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.core.C;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.hologram.Hologram.HologramTarget;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilTime;

public class SupplyCrateKillstreak extends StreakBase
{
    private HashMap<Block, Entry<String, GameTeam>> _crateOwner = new HashMap<Block, Entry<String, GameTeam>>();

    private HashMap<Block, Long> _crateTimes = new HashMap<Block, Long>();
    private HashMap<Block, Hologram[]> _holograms = new HashMap<Block, Hologram[]>();

    public SupplyCrateKillstreak(SearchAndDestroy manager)
    {
        super(manager, "supply crate");
    }

    private void fillInfo(Block block)
    {
        long placed = _crateTimes.get(block);
        Hologram[] holograms = _holograms.get(block);

        String friends = C.DGreen + C.Bold + "Lootable";

        if (!UtilTime.elasped(placed, 30000))
        {
            friends = C.Red + C.Bold + "Lootable in " + (int) Math.ceil(((placed + 30000) - System.currentTimeMillis()) / 1000D);
        }

        String enem = C.DGreen + C.Bold + "Lootable";

        if (!UtilTime.elasped(placed, 60000))
        {
            enem = C.Red + C.Bold + "Lootable in " + (int) Math.ceil(((placed + 60000) - System.currentTimeMillis()) / 1000D);
        }

        Entry<String, GameTeam> owner = _crateOwner.get(block);

        String ownerCrate = owner.getValue().getColoring() + C.Bold + owner.getKey() + "'s crate";

        holograms[1].setText(ownerCrate, friends);

        holograms[2].setText(ownerCrate, enem);
    }

    @Override
    public Material getFallingMaterial()
    {
        return Material.WOOD;
    }

    @Override
    public ItemStack getItem()
    {
        ItemBuilder builder = new ItemBuilder(Material.REDSTONE);
        builder.setTitle(C.Red + "Supply Crate");
        builder.addLore(C.Gray + "Use this in a clear area to receive supplies!");

        return builder.build();
    }

    @Override
    public int getKillsRequired()
    {
        return 4;
    }

    @Override
    public boolean isUsable(Player player, Block block)
    {
        for (Block b : _crateOwner.keySet())
        {
            if (Math.abs(b.getX() - block.getX()) + Math.abs(b.getZ() - block.getZ()) <= 1)
            {
                player.sendMessage(C.Red + "Too close to another supply crate!");
                return false;
            }
        }

        for (Entity b : _fallingBlocks.keySet())
        {
            if (Math.abs(b.getLocation().getBlockX() - block.getX()) + Math.abs(b.getLocation().getBlockZ() - block.getZ()) <= 1)
            {
                player.sendMessage(C.Red + "Too close to another supply crate!");
                return false;
            }
        }

        for (TeamBomb bomb : _manager.getBombs())
        {
            if (bomb.getBlock().getX() == block.getX() && bomb.getBlock().getZ() == block.getZ())
            {
                player.sendMessage(C.Red + "Cannot activate supplycrate on top of a bomb!");
                return false;
            }
        }

        return super.isUsable(player, block);
    }

    @EventHandler
    public void onGameStateChange(GameStateEvent event)
    {
        for (Hologram[] holograms : _holograms.values())
        {
            for (Hologram hologram : holograms)
            {
                hologram.stop();
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!_manager.isAlive(player))
        {
            return;
        }

        Block block = event.getClickedBlock();

        if (block == null || event.getAction() != Action.RIGHT_CLICK_BLOCK)
        {
            return;
        }

        if (!_holograms.containsKey(block))
        {
            return;
        }

        long landed = _crateTimes.containsKey(block) ? _crateTimes.get(block) : 0;

        if (landed > 0 && !_crateOwner.get(block).getKey().equals(player.getName()))
        {
            GameTeam team = _crateOwner.get(block).getValue();

            if (!(UtilTime.elasped(landed, 30000) && _manager.getTeam(player) == team))
            {
                player.sendMessage(C.Red + "This is not your supply crate!");
                return;
            }
        }

        ArrayList<ItemStack> loot = new ArrayList<ItemStack>();

        loot.add(new ItemStack(Material.COOKED_BEEF, UtilMath.r(3) + 1));

        if (!_manager.isEndGame())
            loot.add(new ItemStack(Material.ENDER_PEARL, UtilMath.r(3) + 1));

        if (UtilMath.nextBoolean() && !UtilInv.contains(player, Material.DIAMOND_CHESTPLATE))
        {
            loot.add(new ItemBuilder(Material.DIAMOND_CHESTPLATE).setUnbreakable(true).build());
        }

        CrateLootEvent crateEvent = new CrateLootEvent(player, loot);

        Bukkit.getPluginManager().callEvent(crateEvent);

        if (!crateEvent.isCancelled())
        {
            for (ItemStack item : loot)
            {
                if (item.getType() == Material.MUSHROOM_SOUP)
                {
                    int slot = player.getInventory().first(Material.MUSHROOM_SOUP);

                    if (slot >= 0)
                    {
                        ItemStack itemstack = player.getInventory().getItem(slot);
                        int amount = itemstack.getAmount() + item.getAmount();

                        itemstack.setAmount(Math.min(64, amount));

                        amount -= itemstack.getAmount();

                        if (amount <= 0)
                        {
                            continue;
                        }
                        else
                        {
                            item.setAmount(amount);
                        }
                    }
                }

                UtilInv.addItem(player, item);
            }

            player.updateInventory();

            player.sendMessage(C.Gold + "You've looted "
                    + (_crateOwner.get(block).getKey().equals(player.getName()) ? "your"
                            : _crateOwner.get(block).getValue().getColoring() + _crateOwner.get(block).getKey() + "'s")
                    + C.Gold + " Supply Crate!");

            block.setType(Material.AIR);

            for (Hologram hologram : _holograms.get(block))
            {
                hologram.stop();
            }

            _holograms.remove(block);
            _crateTimes.remove(block);
            _crateOwner.remove(block);
        }
    }

    @Override
    public void onLanded(Player player, Block block)
    {
        GameTeam team = _manager.getTeam(player);

        block.setType(Material.CHEST);
        block.setData((byte) UtilMath.r(4));

        _crateTimes.put(block, System.currentTimeMillis());

        _crateOwner.put(block, new HashMap.SimpleEntry(player.getName(), team));

        Hologram[] holograms = new Hologram[3];

        for (int i = 0; i < 3; i++)
        {
            Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5));
            holograms[i] = hologram;
        }

        holograms[0].setText(team.getColoring() + C.Bold + player.getName() + "'s crate", C.DGreen + C.Bold + "Lootable");
        holograms[0].setHologramTarget(HologramTarget.WHITELIST);
        holograms[0].addPlayer(player);

        holograms[1].setHologramTarget(HologramTarget.WHITELIST);

        for (Player p : team.getPlayers())
        {
            if (p != player)
            {
                holograms[1].addPlayer(p);
            }

            holograms[2].addPlayer(p);
        }

        _holograms.put(block, holograms);

        fillInfo(block);

        for (Hologram hologram : holograms)
        {
            hologram.start();
        }

        player.sendMessage(C.Blue + "Supply crate has landed!");
    }

    @EventHandler
    public void onUpdate(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
        {
            return;
        }

        Iterator<Entry<Block, Long>> itel = _crateTimes.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<Block, Long> entry = itel.next();

            fillInfo(entry.getKey());

            if (UtilTime.elasped(entry.getValue(), 60000))
            {
                itel.remove();
            }
        }
    }

}
