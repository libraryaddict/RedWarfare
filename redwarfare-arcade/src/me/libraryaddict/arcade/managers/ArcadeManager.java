package me.libraryaddict.arcade.managers;

import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.commands.CommandClearInventory;
import me.libraryaddict.arcade.commands.CommandForceKit;
import me.libraryaddict.arcade.commands.CommandGame;
import me.libraryaddict.arcade.commands.CommandGiveItem;
import me.libraryaddict.arcade.commands.CommandGoTo;
import me.libraryaddict.arcade.commands.CommandKit;
import me.libraryaddict.arcade.commands.CommandPickForcedKits;
import me.libraryaddict.arcade.commands.CommandRefundKit;
import me.libraryaddict.arcade.commands.CommandSaveKit;
import me.libraryaddict.arcade.commands.CommandSetMap;
import me.libraryaddict.arcade.commands.CommandSuicide;
import me.libraryaddict.arcade.commands.CommandTeam;
import me.libraryaddict.arcade.commands.CommandTeamChat;
import me.libraryaddict.arcade.commands.CommandTime;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.command.commands.CommandTeleport;

/**
 * Where all the stuff is created and kept track of
 */
public class ArcadeManager extends CentralManager {
    private DeathManager _deathManager;
    private EventManager _eventManager;
    private GameManager _gameManager;
    private GameStatsManager _gameStatsManager;
    private LobbyManager _lobbyManager;
    private LootManager _lootManager;
    private WinManager _winManager;
    private WorldManager _worldManager;

    public ArcadeManager(Arcade arcade) {
        super(arcade);

        _deathManager = new DeathManager(arcade, this);
        _gameManager = new GameManager(arcade, this);
        _lobbyManager = new LobbyManager(arcade, this);
        _winManager = new WinManager(arcade, this);
        _worldManager = new WorldManager(arcade, this);
        _eventManager = new EventManager(arcade, this);
        _gameStatsManager = new GameStatsManager(arcade, this);
        _lootManager = new LootManager(arcade, this);

        getCommand().unregisterCommand(getCommand().getCommand(CommandTeleport.class));

        getCommand().registerCommand(new CommandGame(this));
        getCommand().registerCommand(new CommandSuicide(getGameManager(), getDamage()));
        getCommand().registerCommand(new CommandKit(getGameManager()));
        getCommand().registerCommand(new CommandSaveKit(getGameManager()));
        getCommand().registerCommand(new CommandTeamChat());
        getCommand().registerCommand(new CommandTeam(getGameManager()));
        getCommand().registerCommand(new CommandGoTo(getGameManager()));
        getCommand().registerCommand(new CommandTime(getGameManager()));
        getCommand().registerCommand(new CommandRefundKit(getGameManager()));
        getCommand().registerCommand(new CommandSetMap(this));
        getCommand().registerCommand(new CommandForceKit(getGameManager()));
        getCommand().unregisterCommand(getCommand().getCommand("give"));
        getCommand().registerCommand(new CommandGiveItem(this));
        getCommand().unregisterCommand(getCommand().getCommand("ci"));
        getCommand().registerCommand(new CommandClearInventory(this));
        getCommand().registerCommand(new CommandPickForcedKits(getLobby()));

        new BukkitRunnable() {
            public void run() {
                getWorld().loadLobby();

                getGameManager().createGame();
            }
        }.runTask(getPlugin());
    }

    public DeathManager getDeath() {
        return _deathManager;
    }

    public EventManager getEvent() {
        return _eventManager;
    }

    public Game getGame() {
        return _gameManager.getGame();
    }

    public GameManager getGameManager() {
        return _gameManager;
    }

    public GameStatsManager getGameStats() {
        return _gameStatsManager;
    }

    public LobbyManager getLobby() {
        return _lobbyManager;
    }

    public LootManager getLoot() {
        return _lootManager;
    }

    public WinManager getWin() {
        return _winManager;
    }

    public WorldManager getWorld() {
        return _worldManager;
    }
}
