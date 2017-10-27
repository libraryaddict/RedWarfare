package me.libraryaddict.arcade.game.searchanddestroy.killstreak;

import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks.IronGolemKillstreak;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks.WolvesKillstreak;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public abstract class StreakBase implements Listener
{
    protected HashMap<Entity, String> _fallingBlocks = new HashMap<Entity, String>();
    private String _killstreakName;
    protected SearchAndDestroy _manager;

    public StreakBase(SearchAndDestroy manager, String killstreakName)
    {
        _killstreakName = killstreakName;
        _manager = manager;
    }

    public void dropKillstreak(Player player, Location loc)
    {
        FallingBlock block = loc.getWorld().spawnFallingBlock(loc, getFallingMaterial(), (byte) 0);

        block.setDropItem(false);

        _fallingBlocks.put(block, player.getName());
    }

    public abstract Material getFallingMaterial();

    public abstract ItemStack getItem();

    public abstract int getKillsRequired();

    public boolean getProperBlock()
    {
        return true;
    }

    public void giveKillstreak(Player player, int kills)
    {
        player.sendMessage(C.Blue + "Killstreak of " + kills + "! Given " + _killstreakName + "!");

        Kit kit = _manager.getKit(player);

        if (kit instanceof KitGhost || kit instanceof KitWraith)
        {
            boolean foundEmpty = false;

            for (int slot = 0; slot < player.getInventory().getSize(); slot++)
            {
                ItemStack item = player.getInventory().getItem(slot);

                if (item != null && item.getType() != Material.AIR)
                    continue;

                if (foundEmpty)
                {
                    player.getInventory().setItem(slot, getItem());
                    break;
                }
                else
                {
                    foundEmpty = true;
                }
            }
        }
        else
        {
            UtilInv.addItem(player, getItem());
        }
    }

    private void handleLanding(Entity entity)
    {
        String playerName = _fallingBlocks.remove(entity);

        Player player = Bukkit.getPlayer(playerName);

        if (player == null || !_manager.isAlive(player))
        {
            return;
        }

        if (entity.getLocation().getY() < 0)
        {
            return;
        }

        Block block = entity.getLocation().getBlock();

        if (getProperBlock())
        {
            for (int i = 0; i < 10; i++)
            {
                if (!UtilBlock.nonSolid(block))
                {
                    block = block.getRelative(BlockFace.UP);
                }
                else if (UtilBlock.nonSolid(block.getRelative(BlockFace.DOWN)))
                {
                    block = block.getRelative(BlockFace.DOWN);
                }
            }
        }

        block.getWorld().playSound(block.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 7);

        onLanded(player, block);
    }

    public boolean hasKillstreak(int kills)
    {
        return kills % getKillsRequired() == 0;
    }

    public boolean isUsable()
    {
        return true;
    }

    public boolean isUsable(Player player, Block block)
    {
        if (block.getType() != Material.AIR || !UtilBlock.solid(block.getRelative(BlockFace.DOWN)))
        {
            return false;
        }

        return true;
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent event)
    {
        if (!_fallingBlocks.containsKey(event.getEntity()))
        {
            return;
        }

        event.setCancelled(true);
        event.getEntity().remove();

        handleLanding(event.getEntity());
    }

    @EventHandler
    public void onFallingUpdate(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
        {
            return;
        }

        ArrayList<Entity> entities = new ArrayList<Entity>();

        for (Entity entity : _fallingBlocks.keySet())
        {
            if (!entity.isValid())
            {
                entities.add(entity);
            }
        }

        for (Entity entity : entities)
        {
            handleLanding(entity);
        }
    }

    @EventHandler
    public void onItemUse(PlayerInteractEvent event)
    {
        if (!isUsable())
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Player player = event.getPlayer();

        if (!_manager.isAlive(player))
            return;

        ItemStack item = event.getItem();

        if (item == null || item.getType() != getItem().getType())
            return;

        net.minecraft.server.v1_12_R1.ItemStack nmsItem = CraftItemStack.asNMSCopy(getItem());
        net.minecraft.server.v1_12_R1.ItemStack nmsPlayerItem = CraftItemStack.asNMSCopy(item);

        boolean tag = item.getDurability() == getItem().getDurability() || (nmsItem.hasTag() && nmsPlayerItem.hasTag()
                && nmsItem.getTag().hasKey("EntityTag") && nmsPlayerItem.getTag().hasKey("EntityTag")
                && Objects.equals(nmsItem.getTag().getCompound("EntityTag").getString("id"),
                        nmsPlayerItem.getTag().getCompound("EntityTag").getString("id")));

        boolean display = item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && Objects.equals(item.getItemMeta().getDisplayName(), getItem().getItemMeta().getDisplayName());
        boolean type = item.getType() == getItem().getType() && (item.getType() != Material.MONSTER_EGG
                || ((this instanceof WolvesKillstreak && item.getDurability() == EntityType.WOLF.getTypeId())
                        || (this instanceof IronGolemKillstreak && item.getDurability() == EntityType.IRON_GOLEM.getTypeId())));

        if (!display && !type && !tag)
            return;

        if (event.getClickedBlock() == null)
            return;

        event.setCancelled(true);

        Block block = event.getClickedBlock().getRelative(event.getBlockFace());

        if (!isUsable(player, block))
        {
            player.updateInventory();
            return;
        }

        UtilInv.remove(player, getItem().getType(), 1);

        Location loc = block.getWorld().getHighestBlockAt(block.getLocation()).getLocation().add(0.5, 0, 0.5);

        loc.add(0, Math.max(100 - (loc.getY() - block.getY()), 50), 0);

        dropKillstreak(player, loc);
    }

    public abstract void onLanded(Player player, Block block);
}
