package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.explosion.CustomExplosion;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilTime;

public class DemolitionsAbility extends Ability
{
    private ArrayList<Landmine> _mines = new ArrayList<Landmine>();
    private ArrayList<Pair<UUID, Long>> _regen = new ArrayList<Pair<UUID, Long>>();
    private AttackType DEMOLITIONS_EXPLODE = new AttackType("Demolitions Landmine", "%Killed% stepped on %Killer%'s landmine")
            .setExplosion();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (event.getBlock().getType() != Material.STONE_PLATE)
            return;

        Iterator<Landmine> itel = _mines.iterator();

        while (itel.hasNext())
        {
            Landmine mine = itel.next();

            if (!mine.getBlock().equals(event.getBlock()))
                continue;

            if (mine.isTriggered())
            {
                mine.getPlayer().sendMessage(C.Red + "This mine is about to explode!");
                continue;
            }

            if (!mine.getPlayer().equals(event.getPlayer()))
                continue;

            itel.remove();
            event.getBlock().setType(Material.AIR);

            _regen.add(Pair.of(mine.getPlayer().getUniqueId(), System.currentTimeMillis()));
            break;
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!isAlive(player))
            return;

        if (event.getBlock().getType() != Material.STONE_PLATE)
            return;

        if (event.getBlockReplacedState().getType() != Material.AIR)
        {
            player.sendMessage(C.Red + "Must place in a valid location!");
            return;
        }

        if (event.getBlock().getRelative(BlockFace.DOWN).getType() == Material.MONSTER_EGGS)
        {
            return;
        }

        event.setCancelled(false);

        _mines.add(new Landmine(player, event.getBlock()));
    }

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        Player player = event.getPlayer();

        if (!hasAbility(event.getPlayer()))
            return;

        Iterator<Landmine> itel = _mines.iterator();

        while (itel.hasNext())
        {
            Landmine mine = itel.next();

            if (!mine.getPlayer().equals(player))
                continue;

            itel.remove();
            mine.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL)
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        for (Landmine mine : _mines)
        {
            if (!mine.getBlock().equals(block))
                continue;

            event.setCancelled(true);

            if (getGame().sameTeam(event.getPlayer(), mine.getPlayer()))
                continue;

            if (!mine.isArmed())
                continue;

            if (mine.isTriggered())
                continue;

            if (!UtilTime.elasped(mine.getLastState(), 100))
                continue;

            mine.setTriggered();

            UtilParticle.playParticle(ParticleType.LARGE_SMOKE, mine.getLocation().add(0, -.5, 0), 0.3F, 0.0F, 0.3F, 3);

            mine.getLocation().getWorld().playSound(mine.getLocation(), Sound.ENTITY_CREEPER_HURT, 1, 0);
            mine.getLocation().getWorld().playSound(mine.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 0);

            mine.getPlayer().sendMessage(C.Gold + "One of your landmines just went off");
        }
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.LEFT_CLICK_BLOCK)
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        Player player = event.getPlayer();

        for (Landmine mine : _mines)
        {
            if (!mine.getBlock().equals(block))
                continue;

            event.setCancelled(true);

            player.sendMessage(C.Gold + "This is " + getGame().getTeam(mine.getPlayer()).getColoring()
                    + mine.getPlayer().getName() + "'s " + C.Gold + "landmine");
        }
    }

    @EventHandler
    public void onMineRegen(TimeEvent event)
    {
        Iterator<Pair<UUID, Long>> itel = _regen.iterator();

        while (itel.hasNext())
        {
            Pair<UUID, Long> pair = itel.next();

            if (!UtilTime.elasped(pair.getValue(), 40000))
                continue;

            itel.remove();

            Player player = Bukkit.getPlayer(pair.getKey());

            if (player == null || !hasAbility(player) || !isAlive(player))
                continue;

            UtilInv.addItem(player, new ItemBuilder(Material.STONE_PLATE).setTitle(C.Yellow + "Landmine").build());
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 5, 1);
        }
    }

    @EventHandler
    public void onRedstone(BlockRedstoneEvent event)
    {
        for (Landmine mine : _mines)
        {
            if (!mine.getBlock().equals(event.getBlock()))
                continue;

            event.setNewCurrent(event.getOldCurrent());
        }
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Landmine mine : new ArrayList<Landmine>(_mines))
        {
            if (mine.isArmed())
            {
                if (!mine.isTriggered() || !UtilTime.elasped(mine.getLastState(), 700))
                    continue;

                mine.getBlock().setType(Material.AIR);

                new CustomExplosion(mine.getLocation(), 6F, DEMOLITIONS_EXPLODE)

                .setDamager(mine.getPlayer())

                .setMaxDamage(30)

                .setDamageBlocks(false)

                .setIgnoreNonLiving(true)

                .explode();

                _mines.remove(mine);

                _regen.add(Pair.of(mine.getPlayer().getUniqueId(), System.currentTimeMillis()));
            }
            else
            {
                if (!UtilTime.elasped(mine.getLastState(), 2000))
                    continue;

                mine.setArmed();

                UtilParticle.playParticle(ParticleType.CRIT, mine.getLocation().add(0, 0.4, 0), 3);

                mine.getLocation().getWorld().playSound(mine.getLocation(), Sound.BLOCK_WOOD_PRESSUREPLATE_CLICK_ON, 1, 0.1F);

                mine.getPlayer().sendMessage(C.Green + "Your landmine is now armed!");
            }
        }
    }

}
