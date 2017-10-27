package me.libraryaddict.core.vote;

import java.util.Arrays;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.vote.commands.CommandVote;

public class VoteManager
{
    private String[] _voteNames = new String[0];
    private String[] _voteSites = new String[0];

    public VoteManager(CommandManager commandManager)
    {
        commandManager.registerCommand(new CommandVote(this));

        /**
         * @http://minecraftservers.org/server/93948
         * @http://minecraft-mp.com/server/132317/vote/
         * @http://topg.org/Minecraft/in-376349
         * @http://minecraft-server-list.com/server/364228/vote/
         * @https://www.minestatus.net/100143-red-warfare
         */
        registerVote("MinecraftServers.org", C.Green + "http://tiger.redwarfare.com");
        registerVote("Minecraft-MP.com", C.Gold + "http://bear.redwarfare.com");
        registerVote("TopG.org", C.Gray + "http://lion.redwarfare.com");
        registerVote("MCSL", C.White + "http://croc.redwarfare.com");
        registerVote("Minestatus", C.Purple + "http://wolf.redwarfare.com");
    }

    public String[] getVoteNames()
    {
        return _voteNames;
    }

    public String[] getVoteSites()
    {
        return _voteSites;
    }

    private void registerVote(String name, String url)
    {
        _voteNames = Arrays.copyOf(_voteNames, _voteNames.length + 1);
        _voteSites = Arrays.copyOf(_voteSites, _voteSites.length + 1);

        _voteNames[_voteNames.length - 1] = name;
        _voteSites[_voteSites.length - 1] = url;
    }
}
