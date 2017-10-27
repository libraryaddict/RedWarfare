package me.libraryaddict.core.antiafk;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.C;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;

public class AntiAfkManager extends MiniPlugin {
    private HashMap<UUID, Long> _lastMoved = new HashMap<UUID, Long>();
    private RankManager _rankManager;

    public AntiAfkManager(JavaPlugin plugin, RankManager rank) {
        super(plugin, "Anti Afk Manager");

        _rankManager = rank;
    }

    // @EventHandler
    public void onAfkCheck(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        for (Player player : UtilPlayer.getPlayers()) {
            if (_rankManager.getRank(player).hasRank(Rank.MOD))
                continue;

            long lastMoved = _lastMoved.get(player.getUniqueId());

            if (!UtilTime.elasped(lastMoved, 3 * UtilTime.MINUTE * 1000)) {
                continue;
            }

            if (UtilTime.elasped(lastMoved, 6 * UtilTime.MINUTE * 1000)) {
                player.kickPlayer(C.Blue + "Kicked for being afk");
                continue;
            }

            if (!Recharge.canUse(player, "AfkMessage"))
                continue;

            Recharge.use(player, "AfkMessage", 10000);

            player.sendMessage(
                    C.Blue + UtilNumber.getTime(((6 * UtilTime.MINUTE * 1000) + lastMoved - System.currentTimeMillis()),
                            TimeUnit.MILLISECONDS) + " until you are kicked for being afk");
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        synchronized (_lastMoved) {
            _lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        synchronized (_lastMoved) {
            _lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if ((int) Math.floor(event.getTo().getYaw() / 3) == (int) Math.floor(event.getFrom().getYaw() / 3)
                && (int) Math.floor(event.getTo().getPitch() / 3) == (int) Math.floor(event.getFrom().getPitch() / 3))
            return;

        synchronized (_lastMoved) {
            _lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        synchronized (_lastMoved) {
            _lastMoved.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onSneakToggle(PlayerToggleSneakEvent event) {
        synchronized (_lastMoved) {
            _lastMoved.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

}
