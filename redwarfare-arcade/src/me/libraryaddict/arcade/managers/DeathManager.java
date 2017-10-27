package me.libraryaddict.arcade.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDeathEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilEnt;

public class DeathManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;

    public DeathManager(Arcade arcade, ArcadeManager arcadeManager)
    {
        super(arcade, "Death Manager");

        _arcadeManager = arcadeManager;
    }

    public void handleDeath(CustomDeathEvent event)
    {
        Game game = _arcadeManager.getGame();

        Player player = event.getPlayer();

        DeathEvent deathEvent = new DeathEvent(player, event.getCombatLog());

        Bukkit.getPluginManager().callEvent(deathEvent);

        if (deathEvent.isCancelled())
            return;

        if (game.getOption(GameOption.DEATH_MESSAGES))
        {
            CustomDamageEvent damageEvent = deathEvent.getDamageEvent();
            String killed = UtilEnt.getName(deathEvent.getPlayer());
            String killer = UtilEnt.getName(deathEvent.getLastAttacker());
            String cause = UtilEnt.getName(deathEvent.getDamageEvent().getDamager());

            Bukkit.broadcastMessage(damageEvent.getAttackType().getDeathMessage(C.Yellow, deathEvent.getKilledPrefix() + killed,
                    deathEvent.getKillerPrefix() + killer, cause));
        }

        GameTeam team = game.getTeam(player);

        team.setDead(player);

        game.checkGameState();
    }

    @EventHandler
    public void onDeath(CustomDeathEvent event)
    {
        event.setCancelled(true);

        handleDeath(event);
    }

}
