package me.libraryaddict.arcade.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.command.commands.CommandTeleport;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandGoTo extends CommandTeleport
{
    private GameManager _gameManager;

    public CommandGoTo(GameManager gameManager)
    {
        _gameManager = gameManager;

        setRanks(Rank.ALL);
    }

    @Override
    public Location getToPlayer(Player player, String[] args)
    {
        Player teleTo = Bukkit.getPlayer(args[0]);

        if (teleTo == null)
        {
            player.sendMessage(C.Red + "Player '" + args[0] + "' not found");
            return null;
        }

        Game game = _gameManager.getGame();
        Kit kit;
        GameTeam targetTeam;
        GameTeam teleTeam;

        if (game.isAlive(teleTo) && !(kit = game.getKit(teleTo)).canTeleportTo())
        {
            if ((targetTeam = game.getTeam(teleTo)) != null && (teleTeam = game.getTeam(player)) != null && targetTeam != teleTeam
                    && !game.getManager().getRank().getRank(player).hasRank(Rank.MOD))
            {
                player.sendMessage(C.Red + "You cannot teleport to enemy " + kit.getName() + "s!");
                return null;
            }
        }

        return teleTo.getLocation();
    }

    @Override
    public boolean hasPermission(Player player, PlayerRank rank, TeleportType teleportType)
    {
        if (super.hasPermission(player, rank, teleportType))
            return true;

        if (teleportType != TeleportType.TO_PLAYER)
            return false;

        Game game = _gameManager.getGame();

        return !game.isAlive(player);
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        try
        {
            Player teleporter = getTeleporter(player, rank, alias, args);

            if (teleporter == null)
                return;

            Location destination = getDestination(player, rank, alias, args);

            if (destination == null)
                return;

            if (player != teleporter)
            {
                teleporter.sendMessage(C.Blue + player.getName() + " teleported you");
            }

            UtilPlayer.tele(player, destination);
        }
        catch (Exception ex)
        {
            Game game = _gameManager.getGame();

            if (game.isAlive(player))
            {
                player.sendMessage(C.Red + "You cannot teleport unless you are a spectator!");
                return;
            }

            sendInfo(player, rank, alias);
        }
    }
}
