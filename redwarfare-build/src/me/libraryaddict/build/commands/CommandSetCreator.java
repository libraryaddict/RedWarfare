package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.UUID;

public class CommandSetCreator extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandSetCreator(WorldManager worldManager) {
        super("SetCreator", Rank.ADMIN);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        if (args.length > 0)
            return;

        completions.addAll(getPlayers(token));
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length != 1) {
            player.sendMessage(C.Red + "/" + alias + " <Player>");
            return;
        }

        WorldInfo info;

        if ((info = _worldManager.getWorld(player.getWorld())) == null) {
            player.sendMessage(C.Red + "Join a map first");
            return;
        }

        String message = args[0];

        if (message.length() == 0 || message.length() > 16) {
            player.sendMessage(C.Red + "That is not a player's name!");

            return;
        }

        Player toSet = Bukkit.getPlayerExact(message);

        if (toSet != null) {
            setCreator(info.getData(), player, toSet.getUniqueId(), toSet.getName());

            return;
        } else if (info.getData().getCreatorName().equalsIgnoreCase(message)) {
            player.sendMessage(C.Red + info.getData().getCreatorName() + " is already the creator!");

            return;
        }

        new BukkitRunnable() {
            public void run() {
                MysqlFetchUUID fetchInfo = new MysqlFetchUUID(message);

                new BukkitRunnable() {
                    public void run() {
                        if (!fetchInfo.isSuccess()) {
                            player.sendMessage(UtilError.format("Database error"));

                            return;
                        }

                        if (fetchInfo.getUUID() == null) {
                            player.sendMessage(C.Red + "Cannot find the player '" + message + "'");

                            return;
                        }

                        setCreator(info.getData(), player, fetchInfo.getUUID(), fetchInfo.getName());
                    }
                }.runTask(_worldManager.getPlugin());
            }
        }.runTaskAsynchronously(_worldManager.getPlugin());
    }

    private void setCreator(MapInfo mapInfo, Player player, UUID uuid, String name) {
        mapInfo.setCreator(Pair.of(uuid, name));

        Player toSet = Bukkit.getPlayer(uuid);

        if (toSet != null) {
            toSet.sendMessage(
                    C.Red + "You were set as creator to the map " + mapInfo.getName() + " by " + player.getName());
        }

        for (Player p : player.getWorld().getPlayers()) {
            p.sendMessage(C.Gold + player.getName() + " set " + name + " as the creator");
        }

        mapInfo.save();
    }
}
