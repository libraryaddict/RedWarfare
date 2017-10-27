package me.libraryaddict.arcade.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.bukkit.entity.Player;

import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;

public class CommandSaveKit extends SimpleCommand
{
    private GameManager _gameManager;

    public CommandSaveKit(GameManager gameManager)
    {
        super("savekit", Rank.ALL);

        _gameManager = gameManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
        if (args.length > 0)
            return;

        for (Kit kit : _gameManager.getGame().getKits())
        {
            if (kit.getName().toLowerCase().startsWith(token.toLowerCase()))
            {
                completions.add(kit.getName());
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        GameState state = _gameManager.getGame().getState();

        if (args.length == 0)
        {
            ArrayList<String> kits = new ArrayList<String>();

            for (Kit kit : _gameManager.getGame().getKits())
            {
                kits.add(kit.getName());
            }

            if (state.isPreGame())
            {
                player.sendMessage(C.Red + "Use /savekit <Kit>");
                return;
            }

            FancyMessage fancyMessage = new FancyMessage(C.Blue + C.Bold + "Kits: " + C.Aqua);

            Iterator<String> itel = kits.iterator();

            while (itel.hasNext())
            {
                String kit = itel.next();

                fancyMessage.then(kit).command("/savekit " + kit);

                if (itel.hasNext())
                {
                    fancyMessage.then(C.Blue + ", " + C.Aqua);
                }
                else
                {
                    fancyMessage.then(C.Blue + ".");
                }
            }

            fancyMessage.send(player);
            return;
        }

        Kit kit = null;

        for (Kit k : _gameManager.getGame().getKits())
        {
            if (k.getName().equalsIgnoreCase(args[0]))
            {
                kit = k;
                break;
            }
        }

        if (kit == null)
        {
            player.sendMessage(C.Blue + "Kit '" + args[0] + "' not found");
            return;
        }

        if (!kit.ownsKit(player))
        {
            player.sendMessage(C.Red + "You do not own the kit " + kit.getName() + "!");
            return;
        }

        Preference.setPreference(player, _gameManager.getGame().getSaveKit(), kit.getName());

        player.sendMessage(C.Blue + "Saved default kit to " + kit.getName());
    }

}
