package me.libraryaddict.core.messaging.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.messaging.MessageManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandMessage extends SimpleCommand
{
    private MessageManager _messageManager;

    public CommandMessage(MessageManager messageManager)
    {
        super(new String[]
            {
                    "message", "msg", "w", "whisper", "pm"
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
        if (args.length <= 1)
        {
            player.sendMessage(C.Red + "/" + alias + " <Player> <Message>");
            return;
        }
        _messageManager.sendMessage(player, args[0], UtilString.join(1, args, " "));
    }

}
