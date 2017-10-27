package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;
import java.util.HashMap;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;

public class ForceKitDefinedTeam implements ForceKit
{
    private Game _game;
    private HashMap<GameTeam, ForceKit> _kits = new HashMap<GameTeam, ForceKit>();

    public ForceKitDefinedTeam(Game game)
    {
        _game = game;
    }

    @Override
    public ArrayList<Kit> getKits(GameTeam team)
    {
        return _kits.get(team).getKits(team);
    }

    @Override
    public String parse(String toParse)
    {
        String[] split = toParse.split(" ");

        if (split.length != _game.getTeams().size())
            return "Not enough teams defined";

        for (String layout : split)
        {
            String[] split2 = layout.split(":");

            if (split2.length != 2)
                return "Bad team defination for " + layout;

            String teamName = split2[0];
            String kits = split2[1];

            GameTeam myTeam = null;

            for (GameTeam team : _game.getTeams())
            {
                if (!team.getName().toLowerCase().startsWith(teamName.toLowerCase()))
                    continue;

                myTeam = team;
                break;
            }

            if (myTeam == null)
                return "Unknown team " + teamName;

            ForceKit forceKit;

            if (kits.contains("%"))
                forceKit = new ForceKitPercentage(_game);
            else
                forceKit = new ForceKitRandom(_game);

            String returns = forceKit.parse(kits);

            if (returns != null)
                return returns;

            _kits.put(myTeam, forceKit);
        }

        return null;
    }

}
