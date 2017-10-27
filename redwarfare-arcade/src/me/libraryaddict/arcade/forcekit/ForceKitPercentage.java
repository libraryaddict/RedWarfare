package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilNumber;

public class ForceKitPercentage implements ForceKit
{
    private Game _game;
    private HashMap<Kit, Integer> _kits = new HashMap<Kit, Integer>();

    public ForceKitPercentage(Game game)
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
            {
                for (Entry<Kit, Integer> entry : _kits.entrySet())
                {
                    for (int a = 0; a < entry.getValue(); a++)
                    {
                        selection.add(entry.getKey());
                    }
                }
            }

            Kit kit = UtilMath.r(selection);

            selection.remove(kit);
            kits.add(kit);
        }

        return kits;
    }

    public String parse(String string)
    {
        if (string.trim().isEmpty())
            return "No kits given";

        String[] split = string.split(",");

        for (String s : split)
        {
            String[] split2 = s.split("%");

            if (split2.length != 2)
                return "Cannot parse " + s + " to percentage";

            String kitName;
            int per;

            if (UtilNumber.isParsableInt(split2[0]))
            {
                kitName = split2[1];
                per = Integer.parseInt(split2[0]);
            }
            else if (UtilNumber.isParsableInt(split2[1]))
            {
                kitName = split2[0];
                per = Integer.parseInt(split2[1]);
            }
            else
            {
                return "Cannot parse " + s + " to percentage";
            }

            if (per <= 0)
                return s + " has a weird number";

            Kit kit = _game.getKit(kitName);

            if (kit == null)
                return "Cannot find a kit for " + kitName;

            _kits.put(kit, per);
        }

        return null;
    }
}
