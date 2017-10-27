package me.libraryaddict.build.commands;

import me.libraryaddict.build.inventories.BuildersInventory;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.UUID;

public class CommandBuilder extends SimpleCommand
// Add, Remove, Promote, Demote
{
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public CommandBuilder(WorldManager worldManager, RankManager rankManager) {
        super(new String[]{"builder", "builders", "promote"}, Rank.ALL);

        _worldManager = worldManager;
        _rankManager = rankManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        if (args.length == 0) {
            for (String s : new String[]{"add", "remove", "setadmin", "setbuilder", "admin", "builder", "addbuilder",
                    "addadmin", "fire", "hire"}) {
                if (s.startsWith(token.toLowerCase())) {
                    completions.add(s);
                }
            }
        } else {
            completions.addAll(getPlayers(token));
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (args.length == 0) {
            WorldInfo worldInfo = _worldManager.getWorld(player.getWorld());

            if (worldInfo == null) {
                player.sendMessage(C.Red + "You are not in a map");
                return;
            }

            if (!worldInfo.isAdmin(player)) {
                player.sendMessage(C.Red + "You are not an admin in this world");
                return;
            }

            new BuildersInventory(_rankManager, player, worldInfo.getData()).openInventory();
            return;
        }

        if (args.length == 2) {
            WorldInfo worldInfo = _worldManager.getWorld(player.getWorld());

            if (worldInfo == null) {
                player.sendMessage(C.Red + "You are not in a map");
                return;
            }

            if (!worldInfo.isAdmin(player)) {
                player.sendMessage(C.Red + "You do not have permission to modify the builders of this map");
                return;
            }

            MapInfo info = worldInfo.getData();
            Player toAdd = Bukkit.getPlayerExact(args[1]);
            Pair<UUID, String> toRemove = worldInfo.getData().getPlayer(args[1]);
            String toDo = args[0].toLowerCase();

            boolean force = toDo.equals("builder") || toDo.equals("addbuilder") || toDo.equals("setbuilder") || toDo
                    .equals("admin") || toDo.equals("addadmin") || toDo.equals("setadmin");

            if (toDo.equals("add") || toDo.equals("hire") || toDo.equals("builder") || toDo.equals("addbuilder") || toDo
                    .equals("setbuilder")) {
                if (!force && toRemove != null && info.getRank(toRemove) == MapRank.BUILDER) {
                    player.sendMessage(C.Red + args[1] + " is already a builder");
                    return;
                }

                if (toAdd == null && toRemove == null) {
                    player.sendMessage(C.Red + args[1] + " is not in the server");
                    return;
                }

                if (toRemove == null) {
                    toRemove = Pair.of(toAdd.getUniqueId(), toAdd.getName());
                }

                info.addBuilder(toRemove, MapRank.BUILDER);

                worldInfo.Announce(
                        C.Blue + player.getName() + " added " + toRemove.getValue() + " to the map as a builder");

                if (toAdd != null) {
                    toAdd.sendMessage(C.Blue + "You were made a builder by " + player.getName() + " for the map " + info
                            .getName());
                }

                return;
            }

            if (toDo.equals("admin") || toDo.equals("addadmin") || toDo.equals("setadmin")) {
                if (toRemove != null && info.hasRank(toRemove, MapRank.ADMIN)) {
                    player.sendMessage(C.Red + args[1] + " is already an admin");
                    return;
                }

                if (toAdd == null && toRemove == null) {
                    player.sendMessage(C.Red + args[1] + " is not in the server");
                    return;
                }

                if (toRemove == null) {
                    toRemove = Pair.of(toAdd.getUniqueId(), toAdd.getName());
                }

                info.addBuilder(toRemove, MapRank.ADMIN);

                worldInfo.Announce(
                        C.Blue + player.getName() + " added " + toRemove.getValue() + " to the map as an admin");

                if (toAdd != null) {
                    toAdd.sendMessage(C.Blue + "You were made an admin by " + player.getName() + " for the map " + info
                            .getName());
                }

                return;
            }

            if (toDo.equals("remove") || toDo.equals("fire")) {
                if (toRemove == null) {
                    player.sendMessage(C.Red + args[1] + " is not a builder or admin for the map " + info.getName());
                    return;
                }

                if (toRemove.getKey().equals(info.getCreatorUUID())) {
                    player.sendMessage(C.Red + "The map's creator cannot be demoted");
                    return;
                }

                info.removeBuilder(toRemove);

                worldInfo.Announce(C.Blue + player.getName() + " removed " + toRemove
                        .getValue() + " from the builders for the map");

                if (toAdd != null) {
                    toAdd.sendMessage(
                            C.Blue + "You were removed as a builder by " + player.getName() + " for the map " + info
                                    .getName());
                }

                return;
            }
        }

        player.sendMessage(C.Blue + "/" + alias + " hire <Name> - Grants the player permission to build in this map");
        player.sendMessage(C.Blue + "/" + alias + " fire <Name> - Removes the player from permitted builders");
        player.sendMessage(
                C.Blue + "/" + alias + " ceo <Name> - Allows the player to delete the map, and manage the map " +
                        "employees");
    }
}
