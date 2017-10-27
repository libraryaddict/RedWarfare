package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;

public class ForceKitMaster implements ForceKit
{
    private ForceKit _child;
    private Game _game;

    public ForceKitMaster(Game game)
    {
        _game = game;
    }

    @Override
    public ArrayList<Kit> getKits(GameTeam team)
    {
        return _child.getKits(team);
    }

    @Override
    public String parse(String kits)
    {
        if (kits.contains(" "))
        {
            if (kits.contains(":"))
            {
                _child = new ForceKitDefinedTeam(_game);
            }
            else
            {
                _child = new ForceKitRandomTeam(_game);
            }
        }
        else
        {
            if (kits.contains("%"))
            {
                _child = new ForceKitPercentage(_game);
            }
            else
            {
                _child = new ForceKitRandom(_game);
            }
        }

        return _child.parse(kits);
    }

}
