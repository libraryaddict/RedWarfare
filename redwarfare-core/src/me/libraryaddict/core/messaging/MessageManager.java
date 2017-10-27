package me.libraryaddict.core.messaging;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.C;
import me.libraryaddict.core.censor.CensorManager;
import me.libraryaddict.core.chat.ChatManager;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.messaging.commands.CommandIgnore;
import me.libraryaddict.core.messaging.commands.CommandMessage;
import me.libraryaddict.core.messaging.commands.CommandReply;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.network.PlayerData;
import me.libraryaddict.network.Pref;

public class MessageManager extends MiniPlugin
{
    private CensorManager _censorManager;
    private ChatManager _chatManager;
    private HashMap<String, String> _messagers = new HashMap<String, String>();
    private PlayerDataManager _playerManager;
    private RankManager _rankManager;
    private Pref<Boolean> _receiveMessages = new Pref<Boolean>("Messages.Receive", true);

    public MessageManager(JavaPlugin plugin, CommandManager commandManager, CensorManager censorManager, RankManager rankManager,
            ChatManager chatManager, PlayerDataManager playerManager)
    {
        super(plugin, "Message Manager");

        _censorManager = censorManager;
        _rankManager = rankManager;
        _chatManager = chatManager;
        _playerManager = playerManager;

        commandManager.registerCommand(new CommandMessage(this));
        commandManager.registerCommand(new CommandReply(this));
        commandManager.registerCommand(new CommandIgnore(this));
    }

    public PlayerData getData(Player player)
    {
        return _playerManager.getData(player);
    }

    public Pref<Boolean> getReceiveMessages()
    {
        return _receiveMessages;
    }

    public void sendMessage(Player sender, String target, String message)
    {
        if (getData(sender).isMuted())
        {
            sender.sendMessage(getData(sender).getMuted());
            return;
        }

        if (!Preference.getPreference(sender, getReceiveMessages()) && !_rankManager.getRank(sender).hasRank(Rank.MOD))
        {
            sender.sendMessage(C.Red + "You have private messages disabled");
            return;
        }

        Player toReceive;

        if (target == null && _messagers.containsKey(sender.getName()))
        {
            target = _messagers.get(sender.getName());
            toReceive = Bukkit.getPlayerExact(target);

            if (toReceive == null)
            {
                sender.sendMessage(C.Red + "The player '" + target + "' is no longer online");
                return;
            }
        }
        else if (target == null)
        {
            sender.sendMessage(C.Red + "You are not in a conversation!");
            return;
        }
        else
        {
            toReceive = Bukkit.getPlayer(target);

            if (toReceive == null)
            {
                sender.sendMessage(C.Red + "Cannot find the player '" + target + "'");
                return;
            }
        }

        if (!Preference.getPreference(toReceive, getReceiveMessages()) && !_rankManager.getRank(sender).hasRank(Rank.MOD))
        {
            sender.sendMessage(C.Red + toReceive.getName() + " has private messages disabled");
            return;
        }

        if (getData(toReceive).getIgnoring().contains(sender.getUniqueId()) && !_rankManager.getRank(sender).hasRank(Rank.MOD))
        {
            sender.sendMessage(C.Red + toReceive.getName() + " is ignoring you");
            return;
        }

        if (getData(sender).getIgnoring().contains(toReceive.getUniqueId()) && !_rankManager.getRank(sender).hasRank(Rank.MOD))
        {
            sender.sendMessage(C.Red + "You are ignoring " + toReceive.getName());
            return;
        }

        if (_chatManager.isSpamming(sender, message))
        {
            return;
        }

        _messagers.put(sender.getName(), toReceive.getName());
        _messagers.put(toReceive.getName(), sender.getName());

        String[] messages;

        Rank sRank = _rankManager.getDisplayedRank(sender);
        Rank rRank = _rankManager.getDisplayedRank(toReceive);

        if (!sRank.ownsRank(Rank.MOD) && !rRank.ownsRank(Rank.MOD))
        {
            messages = _censorManager.censorMessage(message);
        }
        else
            messages = new String[]
                {
                        message, message
                };

        sender.sendMessage(C.Gray + "[me -> " + (rRank == Rank.ALL ? "" : rRank.getPrefix()) + toReceive.getName() + C.Gray + "] "
                + C.Reset + messages[0]);
        toReceive.sendMessage(C.Gray + "[" + (sRank == Rank.ALL ? "" : sRank.getPrefix()) + sender.getName() + C.Gray + " -> me] "
                + C.Reset + messages[0]);
    }
}
