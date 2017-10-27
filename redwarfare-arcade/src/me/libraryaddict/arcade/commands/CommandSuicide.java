package me.libraryaddict.arcade.commands;

import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public class CommandSuicide extends SimpleCommand
{
    private DamageManager _damageManager;
    private GameManager _gameManager;

    public CommandSuicide(GameManager gameManager, DamageManager damageManager)
    {
        super(new String[]
            {
                    "suicide", "kill"
            }, Rank.ALL);

        _damageManager = damageManager;
        _gameManager = gameManager;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String lastToken, Collection<String> completions)
    {
        if (!rank.hasRank(Rank.ADMIN))
            return;

        if (args.length > 0)
            return;

        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (p.getName().toLowerCase().startsWith(lastToken.toLowerCase()))
            {
                completions.add(p.getName());
            }
        }
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        if (args.length > 0 && rank.hasRank(Rank.ADMIN))
        {
            Player victim = Bukkit.getPlayerExact(args[0]);

            if (victim == null)
            {
                player.sendMessage(C.Red + "Can't find the " + C.Strike + "victim" + C.Red + "target!");
                return;
            }
            else if (!_gameManager.getGame().isAlive(victim))
            {
                player.sendMessage(C.Red + "Can't kill a dead man!");
                return;
            }
            else if (!_gameManager.getGame().isLive())
            {
                player.sendMessage(C.Red + "Can't kill when the game isn't running!");
                return;
            }

            String message = UtilString.join(1, args, " ");
            AttackType attackType = AttackType.SUICIDE_ASSISTED;

            if (message.contains("%Killed%"))
            {
                attackType = new AttackType("Custom Suicide", message).setInstantDeath().setNoKnockback();
            }

            _damageManager.newDamage(victim, attackType, 0);
        }
        else
        {
            if (!_gameManager.getGame().isLive() || !_gameManager.getGame().isAlive(player))
            {
                player.sendMessage(C.Red + "You can't do that! Find a dark corner and try again!");
                return;
            }

            _damageManager.newDamage(player, AttackType.SUICIDE, 0);
        }
    }

}
