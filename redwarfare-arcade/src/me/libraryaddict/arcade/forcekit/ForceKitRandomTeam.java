package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;

public class ForceKitRandomTeam implements ForceKit
{
    private Game _game;
    private HashMap<GameTeam, ForceKit> _kits = new HashMap<GameTeam, ForceKit>();

    public ForceKitRandomTeam(Game game)
    {
        _game = game;
    }

    @Override
    public ArrayList<Kit> getKits(GameTeam team)
    {
        return _kits.get(team).getKits(team);
    }

    @Override
    public String parse(String string)
    {
        String[] split = string.split(" ");

        if (split.length != _game.getTeams().size())
            return "Invalid amount of teams defined";

        ArrayList<GameTeam> teams = new ArrayList<GameTeam>(_game.getTeams());

        Collections.shuffle(teams);

        Iterator<GameTeam> itel = teams.iterator();

        for (String kits : split)
        {
            ForceKit forceKit;

            if (kits.contains("%"))
                forceKit = new ForceKitPercentage(_game);
            else
                forceKit = new ForceKitRandom(_game);

            String returns = forceKit.parse(kits);

            if (returns != null)
                return returns;

            _kits.put(itel.next(), forceKit);
        }

        return null;
    }

}
