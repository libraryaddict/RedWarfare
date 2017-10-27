package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;

public class WarperAbility extends Ability
{
    private HashMap<UUID, ArrayList<Pair<Location, Long>>> _locs = new HashMap<UUID, ArrayList<Pair<Location, Long>>>();

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        _locs.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!isLive())
            return;

        if (!UtilInv.isHolding(player, event.getHand(), Material.BLAZE_ROD))
            return;

        if (event.getHand() == EquipmentSlot.OFF_HAND)
        {
            PlayerInteractEvent newEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                    UtilInv.getHolding(player, Material.BLAZE_ROD), null, null);

            Bukkit.getPluginManager().callEvent(newEvent);

            if (newEvent.useItemInHand() == Result.DENY)
                return;
        }

        if (!Recharge.canUse(player, "Warper"))
        {
            player.sendMessage(C.Red + "Your wand has not recharged yet!");
            return;
        }

        if (!_locs.containsKey(player.getUniqueId()) || _locs.get(player.getUniqueId()).isEmpty())
        {
            player.sendMessage(C.Red + "No landing spots found in the last 10 seconds!");
            return;
        }

        ArrayList<LivingEntity> warps = new ArrayList<LivingEntity>();
        GameTeam team = getGame().getTeam(player);

        HashSet<Entity> toCheck = new HashSet<Entity>(event.getRightClicked().getNearbyEntities(3, 3, 3));
        toCheck.add(event.getRightClicked());

        for (Entity entity : toCheck)
        {
            if (!(entity instanceof LivingEntity))
                continue;

            if (team.isInTeam(entity))
            {
                continue;
            }

            if (!isAlive(entity))
                continue;

            warps.add((LivingEntity) entity);
        }

        if (warps.isEmpty())
            return;

        player.sendMessage(C.DAqua + "The energy drains out of your wand");
        Recharge.use(player, "Warper", 30000, true);

        Location loc = _locs.get(player.getUniqueId()).get(0).getKey();
        _locs.get(player.getUniqueId()).clear();

        for (LivingEntity entity : warps)
        {
            entity.eject();

            if (entity instanceof Player)
            {
                entity.sendMessage(C.DAqua + "Warped by " + team.getColoring() + player.getName());
            }

            entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
            entity.getWorld().playEffect(entity.getLocation(), Effect.ENDER_SIGNAL, 9);

            UtilPlayer.tele(entity, loc);

            entity.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
            entity.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 9);

            ConditionManager.addFall(entity, player);
        }
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getPlayers(true))
        {
            ArrayList<Pair<Location, Long>> list;

            if (!_locs.containsKey(player.getUniqueId()))
            {
                _locs.put(player.getUniqueId(), new ArrayList<Pair<Location, Long>>());
            }

            list = _locs.get(player.getUniqueId());

            while (list.size() > 1 && UtilTime.elasped(list.get(1).getValue(), 10000)
                    && (!UtilLoc.isSafeTeleport(list.get(0).getKey()) || UtilLoc.isSafeTeleport(list.get(1).getKey())))
            {
                list.remove(0);
            }

            Location loc = player.getLocation();

            if (!UtilLoc.isSafeTeleport(loc))
                continue;

            list.add(list.size(), Pair.of(loc, System.currentTimeMillis()));
        }
    }

    @Override
    public void registerAbility()
    {
        for (Player player : getPlayers())
        {
            Recharge.use(player, "Warper", 10000, true);
        }
    }

}
