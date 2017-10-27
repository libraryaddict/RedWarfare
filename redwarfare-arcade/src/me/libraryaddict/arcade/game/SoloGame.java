package me.libraryaddict.arcade.game;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;

public abstract class SoloGame extends Game
{
    public SoloGame(ArcadeManager arcadeManager, ServerType gameType)
    {
        super(arcadeManager, gameType);
    }

    @Override
    public void checkGameState()
    {
        ArrayList<Player> players = getPlayers(true);

        if (players.size() > 1)
            return;

        if (players.size() == 1)
        {
            getManager().getWin().setWin(players.get(0));
        }
        else
        {
            getManager().getWin().setWin((Player) null);
        }
    }

    @EventHandler
    public void onDeathPrefix(DeathEvent event)
    {
        event.setKilledPrefix(C.Gold);

        if (event.getLastAttacker() == null)
            return;

        event.setKillerPrefix(C.Gold);
    }
}
