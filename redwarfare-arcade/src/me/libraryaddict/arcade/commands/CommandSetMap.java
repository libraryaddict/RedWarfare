package me.libraryaddict.arcade.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.arcade.map.MapInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilString;

public class CommandSetMap extends SimpleCommand
{
    private ArcadeManager _arcadeManager;

    public CommandSetMap(ArcadeManager arcadeManager)
    {
        super(new String[]
            {
                    "setmap", "forcemap", "fmap"
            }, Rank.ADMIN);

        _arcadeManager = arcadeManager;
    }

    public MapInfo loadMapInfo(File file)
    {
        WorldData data = new WorldData(file);
        data.loadData();

        MapInfo info = new MapInfo(data);

        return info;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length > 0)
            return;

        for (File file : UtilFile.getMaps(_arcadeManager.getGame().getGameType()))
        {
            MapInfo mapInfo = null;

            try
            {
                mapInfo = loadMapInfo(file);
            }
            catch (Exception ex)
            {
                System.err.println("Error while loading the map " + file.getName());
                ex.printStackTrace();
            }

            if (mapInfo == null)
                continue;

            if (mapInfo.getData().getName().toLowerCase().startsWith(token.toLowerCase()))
            {
                completions.add(mapInfo.getData().getName());
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (_arcadeManager.getGame().getState() != GameState.PreMap)
        {
            player.sendMessage(C.Red + "Cannot pick the map after the maps were loaded!");
            return;
        }

        for (File file : UtilFile.getMaps(_arcadeManager.getGame().getGameType()))
        {
            MapInfo mapInfo = null;

            try
            {
                mapInfo = loadMapInfo(file);
            }
            catch (Exception ex)
            {
                System.err.println("Error while loading the map " + file.getName());
                ex.printStackTrace();
            }

            if (mapInfo == null)
                continue;

            if (!mapInfo.getData().getName().equalsIgnoreCase(UtilString.join(args, " ")))
            {
                continue;
            }

            ArrayList<MapInfo> mapVoting = _arcadeManager.getWorld().getMapVoting();

            mapVoting.clear();

            for (int i = 0; i < 3; i++)
            {
                mapVoting.add(mapInfo);
            }

            player.sendMessage(C.Blue + mapInfo.getData().getName() + " will now load!");
            return;
        }

        player.sendMessage(C.Red + "Couldn't find '" + UtilString.join(args, " ") + "'!");
    }

}
