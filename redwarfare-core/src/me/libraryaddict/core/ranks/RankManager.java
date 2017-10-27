package me.libraryaddict.core.ranks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.player.events.PlayerLoadEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.command.CommandRank;
import me.libraryaddict.core.ranks.command.CommandUpdateRank;
import me.libraryaddict.core.ranks.redis.RedisRankListener;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.PlayerData;

public class RankManager extends MiniPlugin
{
    private PlayerDataManager _playerDataManager;
    private HashMap<UUID, PlayerRank> _ranks = new HashMap<UUID, PlayerRank>();

    public RankManager(JavaPlugin plugin, PlayerDataManager playerDataManager, CommandManager commandManager)
    {
        super(plugin, "Rank Manager");

        _playerDataManager = playerDataManager;

        new RedisRankListener(this);

        commandManager.registerCommand(new CommandUpdateRank(plugin));
        commandManager.registerCommand(new CommandRank(this, playerDataManager));
       // commandManager.registerCommand(new CommandRedeemVIP(getPlugin()));
    }

    public Rank getDisplayedRank(Player player)
    {
        return getRank(player).getDisplayedRank();
    }

    public PlayerRank getRank(Player player)
    {
        return getRankInfo(player);
    }

    public PlayerRank getRankInfo(Player player)
    {
        return _ranks.get(player.getUniqueId());
    }

    @EventHandler
    public void onLoadRank(PlayerLoadEvent event)
    {
        ArrayList<RankInfo> ranksInfo = new ArrayList<RankInfo>();

        for (Entry<Integer, Long> entry : event.getData().getOwnedRanks().entrySet())
        {
            RankInfo info = new RankInfo(Rank.valueOf(KeyMappings.getKey(entry.getKey())), entry.getValue(),
                    event.getData().getDisplayedRanks().contains(entry.getKey()));

            ranksInfo.add(info);
        }

        _ranks.put(event.getPlayer().getUniqueId(), new PlayerRank(ranksInfo));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event)
    {
        _ranks.remove(event.getPlayer().getUniqueId());
    }

    public void setRank(Player player, Rank rank, long expires)
    {
        PlayerRank ranks = _ranks.get(player.getUniqueId());

        Iterator<RankInfo> itel = ranks.getInfo().iterator();
        boolean modified = false;

        while (itel.hasNext())
        {
            RankInfo info = itel.next();

            if (info.getRank() != rank)
                continue;

            modified = true;

            if (info.getExpires() == expires)
                continue;

            if (expires == -1)
            {
                itel.remove();
                player.sendMessage(C.Red + "Your rank " + rank.name() + " was removed");
                continue;
            }

            info.setExpires(expires);

            if (expires == 0)
            {
                player.sendMessage(C.Red + "Your rank " + rank.getName() + " has been changed to permanament");
                continue;
            }

            player.sendMessage(C.Red + "Your rank " + rank.getName() + " now expires in "
                    + UtilNumber.getTime(expires - System.currentTimeMillis()));
        }

        PlayerData playerData = _playerDataManager.getData(player);

        Integer id = KeyMappings.getKey(rank.name());

        if (expires == -1)
        {
            playerData.getDisplayedRanks().remove(id);
            playerData.getOwnedRanks().remove(id);
        }
        else
        {
            if (!playerData.getOwnedRanks().containsKey(id))
            {
                playerData.getDisplayedRanks().remove(id);
                playerData.getDisplayedRanks().add(id);
            }

            playerData.getOwnedRanks().put(id, expires);
        }

        playerData.save();

        if (modified || expires == -1)
            return;

        ranks.getInfo().add(new RankInfo(rank, expires, true));

        player.sendMessage(C.Blue + "You were given the rank " + rank.getPrefix() + rank.getName());
    }

    public void setupScoreboard(FakeScoreboard fakeScoreboard)
    {
        for (Rank rank : Rank.values())
        {
            FakeTeam team = fakeScoreboard.createTeam(rank.name());
            team.setPrefix(rank.getPrefix());
        }

        for (Player player : UtilPlayer.getPlayers())
        {
            Rank rank = getRank(player).getDisplayedRank();

            FakeTeam team = fakeScoreboard.getTeam(rank.name());
            team.addPlayer(player.getName());
        }
    }
}
