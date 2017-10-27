package me.libraryaddict.build.inventories;

import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.AnvilInventory;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class SetCreatorInventory extends AnvilInventory {
    private MapInfo _mapInfo;
    private RankManager _rankManager;

    public SetCreatorInventory(Player player, RankManager rankManager, MapInfo info) {
        super(player, info.getCreatorName());

        _mapInfo = info;
        _rankManager = rankManager;
    }

    @Override
    public void onMessage(String message) {
    }

    @Override
    public void onSave(String message) {
        closeInventory();

        if (!_rankManager.getRank(getPlayer()).hasRank(Rank.ADMIN)) {
            getPlayer().sendMessage(C.Red + "You don't have permission to do that!");

            return;
        }

        if (message.length() == 0 || message.length() > 16) {
            openAddBuilder();

            getPlayer().sendMessage(C.Red + "That is not a player's name!");

            return;
        }

        Player player = Bukkit.getPlayerExact(message);

        if (player != null) {
            setCreator(player.getUniqueId(), player.getName());

            openBuilders();
            return;
        } else if (_mapInfo.getCreatorName().equalsIgnoreCase(message)) {
            getPlayer().sendMessage(C.Red + _mapInfo.getCreatorName() + " is already the creator!");

            openBuilders();

            return;
        }

        new BukkitRunnable() {
            public void run() {
                MysqlFetchUUID fetchInfo = new MysqlFetchUUID(message);

                new BukkitRunnable() {
                    public void run() {
                        if (!fetchInfo.isSuccess()) {
                            getPlayer().sendMessage(UtilError.format("Database error"));

                            openAddBuilder();
                            return;
                        }

                        if (fetchInfo.getUUID() == null) {
                            getPlayer().sendMessage(C.Red + "Cannot find the player '" + message + "'");

                            openAddBuilder();
                            return;
                        }

                        setCreator(fetchInfo.getUUID(), fetchInfo.getName());

                        openBuilders();
                    }
                }.runTask(_rankManager.getPlugin());
            }
        }.runTaskAsynchronously(_rankManager.getPlugin());
    }

    private void openAddBuilder() {
        new SetCreatorInventory(getPlayer(), _rankManager, _mapInfo).openInventory();
    }

    private void openBuilders() {
        new BuildersInventory(_rankManager, getPlayer(), _mapInfo).openInventory();
    }

    private void setCreator(UUID uuid, String name) {
        _mapInfo.setCreator(Pair.of(uuid, name));

        Player player = Bukkit.getPlayer(uuid);

        if (player != null) {
            player.sendMessage(C.Red + "You were set as creator to the map " + _mapInfo.getName() + " by " + getPlayer()
                    .getName());
        }

        for (Player p : getPlayer().getWorld().getPlayers()) {
            p.sendMessage(C.Gold + getPlayer().getName() + " set " + name + " as the creator");
        }

        _mapInfo.save();
    }
}
