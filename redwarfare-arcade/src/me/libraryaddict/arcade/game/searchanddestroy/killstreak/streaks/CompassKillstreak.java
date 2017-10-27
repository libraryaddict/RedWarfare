package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.core.C;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilPlayer;

public class CompassKillstreak extends StreakBase
{
    private double _xMod, _yMod, _zMod;

    public CompassKillstreak(SearchAndDestroy manager)
    {
        super(manager, "a tracking compass");
    }

    @Override
    public Material getFallingMaterial()
    {
        return null;
    }

    @Override
    public ItemStack getItem()
    {
        ItemStack stack = new ItemStack(Material.COMPASS);

        ItemMeta itemMeta = stack.getItemMeta();
        itemMeta.setDisplayName(C.Green + C.Bold + "Tracking Compass");
        stack.setItemMeta(itemMeta);

        return stack;
    }

    @Override
    public int getKillsRequired()
    {
        return 2;
    }

    @Override
    public boolean hasKillstreak(int kills)
    {
        return kills == getKillsRequired();
    }

    @Override
    public boolean isUsable()
    {
        return false;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!_manager.isAlive(player))
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        if (!_manager.isLive())
            return;

        if (!UtilInv.isItem(event.getItem(), Material.COMPASS))
            return;

        Player closest = null;
        double dist = 0;

        for (Player p : UtilPlayer.getPlayers())
        {
            if (!_manager.isAlive(p))
                continue;

            if (_manager.sameTeam(player, p))
                continue;

            double d = UtilLoc.getDistance(player, p);

            if (closest != null && d > dist)
                continue;

            dist = d;
            closest = p;
        }

        if (closest == null)
        {
            player.sendMessage(C.Red + "No targets found!");
            return;
        }

        if (Recharge.canUse(closest, "Compass" + player.getUniqueId()))
        {
            closest.playSound(closest.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 4, 0);

            closest.sendMessage(C.Red + player.getName() + " has found you with their compass");

            Recharge.use(closest, "Compass" + player.getUniqueId(), 30000);
        }

        boolean ghost = _manager.getKit(closest) instanceof KitGhost || _manager.getKit(closest) instanceof KitWraith;

        Location loc = closest.getLocation();

        if (ghost)
        {
            loc.add(_xMod, _yMod, _zMod);
        }

        if (Recharge.canUse(player, "Compass Message"))
        {
            Recharge.use(player, "Compass Message", 2000);

            player.sendMessage(C.Red + "Tracking player " + closest.getName() + ", they're " + (ghost ? "maybe " : "")
                    + (int) UtilLoc.getDistance(player.getLocation(), loc) + " blocks away" + (ghost ? "??" : "."));
        }

        player.setCompassTarget(loc);
    }

    @Override
    public void onLanded(Player player, Block block)
    {
    }

    @EventHandler
    public void onSlowSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        _xMod = UtilMath.clamp(_xMod + UtilMath.r(-1, 1), -8, 8);
        _yMod = UtilMath.clamp(_yMod + UtilMath.r(-1, 1), -8, 8);
        _zMod = UtilMath.clamp(_zMod + UtilMath.r(-1, 1), -8, 8);
    }

}
