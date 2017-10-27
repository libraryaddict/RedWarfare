package me.libraryaddict.arcade.map;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import me.libraryaddict.core.map.WorldData;

public class MapInfo
{
    private WorldData _data;
    private ArrayList<String> _votes = new ArrayList<String>();

    public MapInfo(WorldData data)
    {
        _data = data;
    }

    public WorldData getData()
    {
        return _data;
    }

    public int getVotes()
    {
        return _votes.size();
    }

    public boolean hasVotedFor(Player player)
    {
        return _votes.contains(player.getName());
    }

    public void removeVote(Player player)
    {
        _votes.remove(player.getName());
    }

    public void voteFor(Player player)
    {
        if (_votes.contains(player.getName()))
            return;

        _votes.add(player.getName());
    }
}
