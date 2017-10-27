package me.libraryaddict.build.commands;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.RemoteFileManager;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.Objects;

public class CommandExport extends SimpleCommand {
    private WorldManager _worldManager;

    public CommandExport(WorldManager worldManager) {
        super("export", Rank.ALL);

        _worldManager = worldManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        WorldInfo worldInfo = _worldManager.getWorld(player.getWorld());

        if (worldInfo == null) {
            player.sendMessage(C.Red + "Erm, do this in a map..");
            return;
        }

        if (!worldInfo.isCreator(player) && !rank.hasRank(Rank.ADMIN)) {
            player.sendMessage(C.Red + "Trying to take liberties with a map you don't own?");
            return;
        }

        if (!Recharge.canUse(player, "Export Map")) {
            player.sendMessage(C.Red + "30 seconds between each map export!");
            return;
        }

        if (worldInfo.getData().isFileInUse()) {
            player.sendMessage(C.Red + "The world cannot be exported at this time");
        }

        worldInfo.Announce(C.Gold + "Saving world for export..");

        worldInfo.saveMap(_worldManager.getIP());

        UtilString.log("Saved map for export " + worldInfo.getData().getUUID().toString() + " to " + _worldManager
                .getIP() + " and path" + worldInfo.getData().getZip().getAbsolutePath());

        worldInfo.Announce(C.Gold + "Saved world");

        worldInfo.getData().setFileInUse("Export map");

        Recharge.use(player, "Export Map", 30000);

        String ip = _worldManager.getIP();
        String name = player.getName() + "-" + worldInfo.getData().getName().replaceAll("[^A-Za-z0-9]", "") + ".zip";
        String remoteFile = "/home/files/web/download/" + name;

        new BukkitRunnable() {
            public void run() {
                try {
                    RemoteFileManager fileManager = new RemoteFileManager(ip);

                    if (!Objects.equals(ip, worldInfo.getData().getIPLoc())) {
                        fileManager.copyFileToLocal();
                    }

                    fileManager.exportMapToSite(worldInfo.getData().getFileLoc(), null, null, null, remoteFile);
                }
                catch (Exception e) {
                    UtilPlayer.sendMessage(player, UtilError.format("Error while exporting map!"));
                    e.printStackTrace();
                    return;
                }

                UtilPlayer.sendMessage(player,
                        C.Blue + "Download your map at http://www.redwarfare.com/download/" + name);
                UtilPlayer.sendMessage(player, C.Blue + "The link will expire in 60 minutes");
            }
        }.runTaskAsynchronously(_worldManager.getPlugin());
    }
}
