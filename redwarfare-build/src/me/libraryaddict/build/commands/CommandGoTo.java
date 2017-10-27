package me.libraryaddict.build.commands;

import com.mojang.authlib.GameProfile;
import me.libraryaddict.build.database.RedisJoinMap;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.command.commands.CommandTeleport;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.redis.operations.RedisSwitchServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class CommandGoTo extends CommandTeleport {
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public CommandGoTo(WorldManager worldManager, RankManager rankManager) {
        _worldManager = worldManager;
        _rankManager = rankManager;

        setRanks(Rank.ALL);
    }

    public Pair<Location, Pair<String, UUID>> getBuildDestination(Player player, PlayerRank rank, String alias,
            String[] args) throws Exception {
        if (canUse(player, rank, TeleportType.TO_PLAYER, args)) {
            Pair<String, UUID> pair = getToPlayerUUID(player, args);

            if (pair == null)
                return null;

            return Pair.of(null, pair);
        } else if (canUse(player, rank, TeleportType.PLAYER_TO_PLAYER, args)) {
            Pair<String, UUID> pair = getPlayerToPlayerUUID(player, args);

            if (pair == null)
                return null;

            return Pair.of(null, pair);
        } else if (canUse(player, rank, TeleportType.TO_LOCATION, args)) {
            Location pair = getToLocation(player, args);

            if (pair == null)
                return null;

            return Pair.of(pair, null);
        } else if (canUse(player, rank, TeleportType.PLAYER_TO_LOCATION, args)) {
            Location pair = getPlayerToLocation(player, args);

            if (pair == null)
                return null;

            return Pair.of(pair, null);
        }

        throw new Exception();
    }

    private Pair<MapInfo, Pair<String, UUID>> getPlayer(String name) {
        HashMap<UUID, HashMap<UUID, GameProfile>> fakePlayers = _worldManager.getFakePlayers();

        for (Entry<UUID, HashMap<UUID, GameProfile>> entry : fakePlayers.entrySet()) {
            for (GameProfile profile : entry.getValue().values()) {
                if (profile.getName().toLowerCase().startsWith(name.toLowerCase())) {
                    return Pair.of(_worldManager.getMap(entry.getKey()), Pair.of(profile.getName(), profile.getId()));
                }
            }
        }

        return null;
    }

    public ArrayList<String> getPlayers(String token) {
        ArrayList<String> players = new ArrayList<String>();

        for (HashMap<UUID, GameProfile> entry : _worldManager.getFakePlayers().values()) {
            for (GameProfile player : entry.values()) {
                if (player.getName().toLowerCase().startsWith(token.toLowerCase())) {
                    players.add(player.getName());
                }
            }
        }

        return players;
    }

    public Pair<String, UUID> getPlayerToPlayerUUID(Player player, String[] args) {
        Pair<MapInfo, Pair<String, UUID>> teleTo = getPlayer(args[1]);

        if (teleTo == null) {
            player.sendMessage(C.Red + "Player '" + args[1] + "' not found");
            return null;
        }

        MapInfo info = teleTo.getKey();

        if (!info.isAllowVisitors() && !info.hasRank(player, MapRank.VISITOR) && !_rankManager.getRank(player)
                .hasRank(Rank.BUILDER)) {
            player.sendMessage(C.Red + teleTo.getValue().getKey() + " is in a map you do not have permission to join");
            return null;
        }

        return Pair.of(info.getLoadedServer(), teleTo.getValue().getValue());
    }

    public Pair<String, UUID> getToPlayerUUID(Player player, String[] args) {
        Pair<MapInfo, Pair<String, UUID>> teleTo = getPlayer(args[0]);

        if (teleTo == null) {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
            return null;
        }

        MapInfo info = teleTo.getKey();

        if (!info.isAllowVisitors() && !info.hasRank(player, MapRank.VISITOR) && !_rankManager.getRank(player)
                .hasRank(Rank.BUILDER)) {
            player.sendMessage(C.Red + teleTo.getValue().getKey() + " is in a map you do not have permission to join");
            return null;
        }

        return Pair.of(info.getLoadedServer(), teleTo.getValue().getValue());
    }

    @Override
    public boolean hasPermission(Player player, PlayerRank rank, TeleportType teleportType) {
        if (super.hasPermission(player, rank, teleportType))
            return true;

        return teleportType == TeleportType.TO_PLAYER || teleportType == TeleportType.TO_LOCATION;
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        try {
            Player teleporter = getTeleporter(player, rank, alias, args);

            if (teleporter == null)
                return;

            Pair<Location, Pair<String, UUID>> destination = getBuildDestination(player, rank, alias, args);

            if (destination == null)
                return;

            if (player != teleporter) {
                teleporter.sendMessage(C.Blue + player.getName() + " teleported you");
            }

            if (destination.getKey() == null) {
                Player p = Bukkit.getPlayer(destination.getValue().getValue());

                if (p != null) {
                    destination.setKey(p.getLocation());
                }
            }

            if (destination.getKey() != null) {
                UtilPlayer.tele(player, destination.getKey());
            } else {
                new BukkitRunnable() {
                    public void run() {
                        new RedisJoinMap(teleporter.getUniqueId(), destination.getValue().getValue());
                        new RedisSwitchServer(teleporter.getUniqueId(), destination.getValue().getKey());
                    }
                }.runTaskAsynchronously(_worldManager.getPlugin());
            }
        }
        catch (Exception ex) {
            sendInfo(player, rank, alias);
        }
    }
}