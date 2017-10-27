package me.libraryaddict.core.messaging.commands;

import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.messaging.MessageManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.mysql.operations.MysqlFetchUUID;
import me.libraryaddict.network.PlayerData;

public class CommandIgnore extends SimpleCommand
{
    private MessageManager _messageManager;

    public CommandIgnore(MessageManager messageManager)
    {
        super(new String[]
            {
                    "ignore", "unignore"
            }, Rank.ALL);

        _messageManager = messageManager;
    }

    private void onIgnore(Player player, UUID toIgnore, String ignore)
    {
        PlayerData playerData = _messageManager.getData(player);

        if (playerData == null)
            return;

        if (player.getUniqueId() == toIgnore)
        {
            player.sendMessage(C.Red + "That would be weird");
            return;
        }

        if (playerData.getIgnoring().remove(toIgnore))
        {
            player.sendMessage(C.Blue + "No longer ignoring " + ignore + "!");
        }
        else
        {
            playerData.getIgnoring().add(toIgnore);

            player.sendMessage(C.Blue + "Now ignoring " + ignore + "!");
        }
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
            player.sendMessage(C.Red + "/" + alias + " <Player>");
            return;
        }

        Player toIgnore = Bukkit.getPlayer(args[0]);

        if (toIgnore == null)
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    MysqlFetchUUID fetchUUID = new MysqlFetchUUID(args[0]);

                    if (!fetchUUID.isSuccess())
                    {
                        UtilPlayer.sendMessage(player, UtilError.format("Error while performing database lookup!"));
                        return;
                    }

                    if (fetchUUID.getUUID() == null)
                    {
                        UtilPlayer.sendMessage(player, C.Red + "Cannot find the player '" + args[0] + "'");
                        return;
                    }
                    new BukkitRunnable()
                    {
                        public void run()
                        {
                            onIgnore(player, fetchUUID.getUUID(), fetchUUID.getName());
                        }
                    }.runTask(_messageManager.getPlugin());
                }
            }.runTaskAsynchronously(_messageManager.getPlugin());
        }
        else
        {
            onIgnore(player, toIgnore.getUniqueId(), toIgnore.getName());
        }
    }

}
