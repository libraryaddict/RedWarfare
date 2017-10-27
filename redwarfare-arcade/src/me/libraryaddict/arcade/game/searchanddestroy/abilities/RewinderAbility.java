package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;

public class RewinderAbility extends Ability
{
    private HashMap<UUID, ArrayList<Pair<Location, Long>>> _locs = new HashMap<UUID, ArrayList<Pair<Location, Long>>>();

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        _locs.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (event.useItemInHand() == Result.DENY)
            return;

        if (!hasAbility(player))
            return;

        if (!isLive())
            return;

        if (!UtilInv.isHolding(player, event.getHand(), Material.WATCH))
            return;

        if (!Recharge.canUse(player, "Rewinder"))
        {
            player.sendMessage(C.Red + "Your timestream is too unstable! Attempting to use it now would kill you!");
            return;
        }

        if (!_locs.containsKey(player.getUniqueId()) || _locs.get(player.getUniqueId()).isEmpty())
        {
            player.sendMessage(C.Red + "No landing spots found in the last 10 seconds!");
            return;
        }

        player.sendMessage(C.DAqua + "You step sideways into the river of time..");

        Recharge.use(player, "Rewinder", 30000, true);

        Location loc = _locs.get(player.getUniqueId()).get(0).getKey();
        _locs.get(player.getUniqueId()).clear();

        player.eject();

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
        player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 9);

        UtilPlayer.tele(player, loc);

        player.getWorld().playSound(loc, Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
        player.getWorld().playEffect(loc, Effect.ENDER_SIGNAL, 9);

        player.setFallDistance(0);
        player.setFireTicks(0);

        UtilEnt.velocity(player, new Vector(), false);

        ConditionManager.removeCondition(player, "Fire");
        ConditionManager.removeCondition(player, "Fall");
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

            while (list.size() > 1 && UtilTime.elasped(list.get(1).getValue(), 30000)
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
            Recharge.use(player, "Rewinder", 30000, true);
        }
    }

}
