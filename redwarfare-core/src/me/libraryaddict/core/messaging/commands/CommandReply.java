package me.libraryaddict.core.messaging.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.messaging.MessageManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandReply extends SimpleCommand
{
    private MessageManager _messageManager;

    public CommandReply(MessageManager messageManager)
    {
        super(new String[]
            {
                    "r", "reply"
            }, Rank.ALL);

        _messageManager = messageManager;
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
            player.sendMessage(C.Red + "/" + alias + " <Message>");
            return;
        }

        _messageManager.sendMessage(player, null, UtilString.join(args, " "));
    }

}
