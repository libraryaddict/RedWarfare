package me.libraryaddict.core.recharge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilTime;

public class Recharge extends MiniPlugin
{
    private static Recharge _recharge;

    public static boolean canUse(Player player, ItemStack item)
    {
        UUID uuid = player.getUniqueId();

        if (!_recharge._recharges.containsKey(uuid))
            return true;

        for (RechargeInfo info : _recharge._recharges.get(uuid))
        {
            if (Objects.equals(info.getBoundTo(), item == null ? Material.AIR : item.getType()))
            {
                return false;
            }
        }

        return true;
    }

    public static boolean canUse(Player player, String name)
    {
        UUID uuid = player.getUniqueId();

        if (!_recharge._recharges.containsKey(uuid))
            return true;

        for (RechargeInfo info : _recharge._recharges.get(uuid))
        {
            if (Objects.equals(info.getName(), name))
            {
                return false;
            }
        }

        return true;
    }

    public static void clear(Player player)
    {
        _recharge._recharges.remove(player.getUniqueId());
    }

    public static long getTimeLeft(Player player, ItemStack item)
    {
        UUID uuid = player.getUniqueId();

        if (!_recharge._recharges.containsKey(uuid))
            return -1;

        for (RechargeInfo info : _recharge._recharges.get(uuid))
        {
            if ((item == null ? Material.AIR : item.getType()) == info.getBoundTo())
            {
                return info.getRemaining();
            }
        }

        return -1;
    }

    public static long getTimeLeft(Player player, String name)
    {
        UUID uuid = player.getUniqueId();

        if (!_recharge._recharges.containsKey(uuid))
            return -1;

        for (RechargeInfo info : _recharge._recharges.get(uuid))
        {
            if (Objects.equals(info.getName(), name))
            {
                return info.getRemaining();
            }
        }

        return -1;
    }

    public static void use(Player player, ItemStack item, String message, long expires)
    {
        RechargeInfo info = new RechargeInfo(item, message, expires);

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    public static void use(Player player, ItemStack item, String message, long expires, boolean displayExp)
    {
        RechargeInfo info = new RechargeInfo(item, message, expires);
        info.setDisplayExp();

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    public static void use(Player player, String name, long expires)
    {
        RechargeInfo info = new RechargeInfo(name, expires);

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    public static void use(Player player, String name, long expires, boolean displayExp)
    {
        RechargeInfo info = new RechargeInfo(name, expires);
        info.setDisplayExp();

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    public static void use(Player player, String name, String message, long expires)
    {
        RechargeInfo info = new RechargeInfo(name, message, expires);

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    public static void use(Player player, String name, String message, long expires, boolean displayExp)
    {
        RechargeInfo info = new RechargeInfo(name, message, expires);
        info.setDisplayExp();

        ArrayList<RechargeInfo> list;

        if (!_recharge._recharges.containsKey(player.getUniqueId()))
        {
            _recharge._recharges.put(player.getUniqueId(), new ArrayList<RechargeInfo>());
        }

        list = _recharge._recharges.get(player.getUniqueId());

        list.remove(info);
        list.add(info);
    }

    private HashMap<UUID, ArrayList<RechargeInfo>> _recharges = new HashMap<UUID, ArrayList<RechargeInfo>>();

    public Recharge(JavaPlugin plugin)
    {
        super(plugin, "Recharge Manager");

        _recharge = this;
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        Iterator<Entry<UUID, ArrayList<RechargeInfo>>> itel = _recharge._recharges.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<UUID, ArrayList<RechargeInfo>> entry = itel.next();

            Iterator<RechargeInfo> itel2 = entry.getValue().iterator();

            while (itel2.hasNext())
            {
                RechargeInfo info = itel2.next();

                boolean remove = UtilTime.elasped(info.getExpires());

                if (info.isDisplayExp())
                {
                    Player player = Bukkit.getPlayer(entry.getKey());

                    if (player == null)
                        continue;

                    if (info.isBoundToItem() && !UtilInv.isHolding(player, info.getBoundTo()))
                        continue;

                    player.setExp(remove ? 1F : (float) info.getPercentDone());
                }

                if (remove)
                {
                    itel2.remove();

                    if (info.isMessage())
                    {
                        Player player = Bukkit.getPlayer(entry.getKey());

                        if (player != null)
                        {
                            player.sendMessage(info.getMessage());
                        }
                    }

                    continue;
                }

            }

            if (!entry.getValue().isEmpty())
                continue;

            itel.remove();
        }
    }
}
