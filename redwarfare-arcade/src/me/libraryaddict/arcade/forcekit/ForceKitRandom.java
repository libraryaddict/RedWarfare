package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.utils.UtilMath;

public class ForceKitRandom implements ForceKit
{
    private Game _game;
    private ArrayList<Kit> _kits = new ArrayList<Kit>();

    public ForceKitRandom(Game game)
    {
        _game = game;
    }

    @Override
    public ArrayList<Kit> getKits(GameTeam team)
    {
        ArrayList<Kit> kits = new ArrayList<Kit>();
        ArrayList<Kit> selection = new ArrayList<Kit>();

        for (int i = 0; i < team.getPlayers().size(); i++)
        {
            if (selection.isEmpty())
                selection = (ArrayList<Kit>) _kits.clone();

            Kit kit = UtilMath.r(selection);

            selection.remove(kit);
            kits.add(kit);
        }

        return kits;
    }

    @Override
    public String parse(String string)
    {
        if (string.trim().isEmpty())
            return "No kits given";

        String[] split = string.split(",");

        for (String kitName : split)
        {
            Kit kit = _game.getKit(kitName);

            if (kit == null)
                return "Cannot find a kit for " + kitName;

            _kits.add(kit);
        }

        return null;
    }

}
