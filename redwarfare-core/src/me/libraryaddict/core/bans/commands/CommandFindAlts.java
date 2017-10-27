package me.libraryaddict.core.bans.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.bans.mysql.MysqlFetchAlts;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.mysql.operations.MysqlFetchPlayerHistory;
import me.libraryaddict.mysql.operations.MysqlFetchRankData;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;

public class CommandFindAlts extends SimpleCommand
{
    public CommandFindAlts()
    {
        super("findalts", Rank.MOD);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length == 0)
        {
            player.sendMessage(C.Red + "/findalts <Player>");
            return;
        }

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlFetchUUID fetchUUID = new MysqlFetchUUID(args[0]);

                if (fetchUUID.getUUID() == null)
                {
                    UtilPlayer.sendMessage(player, C.Red + "Cannot find the player '" + args[0] + "'");
                    return;
                }

                MysqlFetchPlayerHistory fetchHistory = new MysqlFetchPlayerHistory(fetchUUID.getUUID());

                MysqlFetchAlts fetchAlts = new MysqlFetchAlts(fetchHistory.getHistory().getIPs().keySet().toArray(new String[0]));

                HashMap<UUID, HashMap<String, Pair<Long, Long>>> alts = new HashMap<UUID, HashMap<String, Pair<Long, Long>>>();
                HashMap<UUID, String> names = new HashMap<UUID, String>();

                for (Entry<String, HashMap<UUID, Pair<Long, Long>>> sine : fetchAlts.getAlts().entrySet())
                {
                    for (Entry<UUID, Pair<Long, Long>> pine : sine.getValue().entrySet())
                    {
                        UUID uuid = pine.getKey();

                        if (fetchUUID.getUUID().equals(uuid))
                            continue;

                        if (!names.containsKey(uuid))
                        {
                            MysqlFetchPlayerHistory fetchName = new MysqlFetchPlayerHistory(uuid);

                            names.put(uuid, fetchName.getHistory().getName());
                        }

                        if (!alts.containsKey(uuid))
                        {
                            alts.put(uuid, new HashMap<String, Pair<Long, Long>>());
                        }

                        alts.get(uuid).put(sine.getKey(), pine.getValue());
                    }
                }

                if (alts.isEmpty())
                {
                    UtilPlayer.sendMessage(player, C.Red + "No alts to be found!");
                    return;
                }

                if (!rank.hasRank(Rank.OWNER))
                {
                    ArrayList<UUID> uuids = new ArrayList<UUID>(names.keySet());
                    uuids.add(fetchUUID.getUUID());

                    for (UUID uuid : uuids)
                    {
                        MysqlFetchRankData fetchRank = new MysqlFetchRankData(uuid);

                        for (String name : fetchRank.getRanks().keySet())
                        {
                            Rank theirRank = Rank.valueOf(name);

                            if (theirRank.ownsRank(Rank.MOD))
                            {
                                UtilPlayer.sendMessage(player, C.Red
                                        + "A staff member was found by the results and you do not have permission to look into it!");
                                return;
                            }
                        }
                    }
                }

                FancyMessage fancyMessage = new FancyMessage(fetchUUID.getName() + "'s alts: ");

                {
                    ArrayList<String> tooltip = new ArrayList<String>();

                    for (Entry<String, Pair<Long, Long>> ip : fetchHistory.getHistory().getIPs().entrySet())
                    {
                        tooltip.add(C.Blue + "IP: " + C.Aqua + ip.getKey() + C.Blue + ", last used: " + C.Aqua
                                + UtilTime.parse(ip.getValue().getValue()));
                    }

                    fancyMessage.tooltip(tooltip);
                }

                ArrayList<Entry<UUID, HashMap<String, Pair<Long, Long>>>> list = new ArrayList(alts.entrySet());

                Collections.sort(list, new Comparator<Entry<UUID, HashMap<String, Pair<Long, Long>>>>()
                {
                    @Override
                    public int compare(Entry<UUID, HashMap<String, Pair<Long, Long>>> o1,
                            Entry<UUID, HashMap<String, Pair<Long, Long>>> o2)
                    {
                        if (o1.getValue().isEmpty() != o2.getValue().isEmpty())
                            return Boolean.compare(o1.getValue().isEmpty(), o2.getValue().isEmpty());
                        else if (o1.getValue().isEmpty() && o2.getValue().isEmpty())
                            return 0;

                        return Long.compare(getLast(o2.getValue()), getLast(o1.getValue()));
                    }

                    private Long getLast(HashMap<String, Pair<Long, Long>> entry)
                    {
                        long biggest = 0;

                        for (Pair<Long, Long> e : entry.values())
                        {
                            if (e.getValue() < biggest)
                                continue;

                            biggest = e.getValue();
                        }

                        return biggest;
                    }
                });

                Iterator<Entry<UUID, HashMap<String, Pair<Long, Long>>>> itel = alts.entrySet().iterator();

                while (itel.hasNext())
                {
                    Entry<UUID, HashMap<String, Pair<Long, Long>>> entry = itel.next();

                    fancyMessage.then(names.get(entry.getKey()));

                    fancyMessage.suggest(names.get(entry.getKey()));

                    ArrayList<String> tooltip = new ArrayList<String>();

                    for (Entry<String, Pair<Long, Long>> ip : entry.getValue().entrySet())
                    {
                        tooltip.add(C.Blue + "IP: " + C.Aqua + ip.getKey() + C.Blue + ", last used: " + C.Aqua
                                + UtilTime.parse(ip.getValue().getValue()));
                    }

                    fancyMessage.tooltip(tooltip);

                    if (itel.hasNext())
                    {
                        fancyMessage.then(", ");
                    }
                }

                new BukkitRunnable()
                {
                    public void run()
                    {
                        fancyMessage.send(player);
                    }
                }.runTask(getPlugin());
            }
        }.runTaskAsynchronously(getPlugin());
    }
}
