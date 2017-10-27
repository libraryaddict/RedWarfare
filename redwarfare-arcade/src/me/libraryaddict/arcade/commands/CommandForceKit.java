package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.entity.Player;

import me.libraryaddict.arcade.forcekit.ForceKit;
import me.libraryaddict.arcade.forcekit.ForceKitMaster;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.core.AllowForcedKits;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandForceKit extends SimpleCommand {
    private GameManager _gameManager;

    public CommandForceKit(GameManager gameManager) {
        super(new String[] {
                "forcekit", "forcekits", "fkit"
        }, AllowForcedKits.donators != null ? Rank.ALL : Rank.ADMIN);

        _gameManager = gameManager;
    }
    // color:kit,kit 90%kit,40%kit

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions) {
        for (Kit kit : _gameManager.getGame().getKits()) {
            if (!kit.getName().toLowerCase().startsWith(token.toLowerCase()))
                continue;

            completions.add(kit.getName());
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args) {
        if (!_gameManager.getGame().isPreGame() || _gameManager.getGame().getManager().getLobby().getCountdown() <= 10) {
            player.sendMessage(C.Red + "A bit late for that?");
            return;
        }

        if (args.length == 0) {
            player.sendMessage(C.Red + "Forcing a kit, how to");
            player.sendMessage(C.Red + "Use /forcekit <Kit> and everyone will be using that kit");
            player.sendMessage(C.Red
                    + "Use /forcekit <Kit> <Kit> with a space in between, if there is two teams then this will assign one kit to each team. Otherwise it will error");
            player.sendMessage(C.Red + "Use /forcekit Red:<Kit> Blue:<Kit> to assign a kit to those teams");
            player.sendMessage(C.Red + "Use /forcekit <Kit>,<Kit> and everyone has a random chance to get one of the two kits");
            player.sendMessage(C.Red
                    + "Use /forcekit <Kit>%2,<Kit>%1 and two players will get the first kit, the third player the second kit");
            player.sendMessage(C.Red
                    + "You can also force the kit 'None' which is no items at all, however you cannot give items to anyone.");
            return;
        }
        /* if (args.length == 0) {
            if (_gameManager.getGame().getForceKit() != null) {
                _gameManager.getGame().setForceKit(null);
                _gameManager.getGame().Announce(C.DGreen + "No longer forcing kits");
                return;
            }
        
            player.sendMessage(C.Red + "/forcekit <Kits>");
            player.sendMessage(C.Red + "Though given you don't know how to use this, you really should not be touching this..");
            return;
        }*/

        ForceKit forceKit = new ForceKitMaster(_gameManager.getGame());
        String returns = forceKit.parse(UtilString.join(args, " "));

        if (returns != null) {
            player.sendMessage(C.Red + returns);
            return;
        }

        _gameManager.getGame().getManager().getLobby().addCanidate(player, forceKit, UtilString.join(args, " "));

        // _gameManager.getGame().setForceKit(forceKit);

        // _gameManager.getGame().Announce(C.DGreen + "Now forcing kits!");
    }

}
