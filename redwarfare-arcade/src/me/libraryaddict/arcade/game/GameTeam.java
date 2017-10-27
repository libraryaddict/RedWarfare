package me.libraryaddict.arcade.game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilPlayer;

public class GameTeam
{
    public static Comparator<GameTeam> COMPARE_PLAYERS = new Comparator<GameTeam>()
    {

        @Override
        public int compare(GameTeam o1, GameTeam o2)
        {
            int ret = Integer.compare(o2.getPlayers(true).size(), o1.getPlayers(true).size());

            if (ret != 0)
                return ret;

            return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
        }
    };
    private boolean _dead;
    private HashMap<UUID, Long> _deadAndWhen = new HashMap<UUID, Long>();
    private Game _game;
    private HashMap<UUID, Long> _lastToJoin = new HashMap<UUID, Long>();
    private HashMap<UUID, UUID> _mobs = new HashMap<UUID, UUID>();
    private ArrayList<UUID> _noRewards = new ArrayList<UUID>();
    private HashMap<UUID, Integer> _playersAndTimesDied = new HashMap<UUID, Integer>();
    private ArrayList<Location> _spawns = new ArrayList<Location>();
    private TeamSettings _teamSettings;

    public GameTeam(Game game, TeamSettings settings)
    {
        _game = game;
        _teamSettings = settings;
    }
    
    public void addNoRewards(Player player)
    {
        _noRewards.add(player.getUniqueId());
    }

    public void addToTeam(Entity entity, Player owner)
    {
        _mobs.put(entity.getUniqueId(), owner == null ? null : owner.getUniqueId());
    }

    /*public GameTeam(Game game, String teamName, String teamColor, String teamPrefix, ItemStack teamHat, Color bukkitColor)
    {
    	_game = game;
    	_teamName = teamName;
    	_teamColor = teamColor;
    	_teamPrefix = teamPrefix;
    	_teamHat = teamHat;
    }*/

    public void addToTeam(Player player)
    {
        _lastToJoin.put(player.getUniqueId(), System.currentTimeMillis());
        _playersAndTimesDied.put(player.getUniqueId(), 0);
    }

    public Color getColor()
    {
        return getSettings().getBukkitColor();
    }

    public String getColoring()
    {
        return getSettings().getColor();
    }

    public ItemStack getHat()
    {
        return getSettings().getHat();
    }

    public Player getLastToJoin()
    {
        Player lowest = null;

        for (Player player : UtilPlayer.getPlayers())
        {
            if (!_lastToJoin.containsKey(player.getUniqueId()))
                continue;

            if (lowest != null && _lastToJoin.get(player.getUniqueId()) < _lastToJoin.get(player.getUniqueId()))
            {
                continue;
            }

            lowest = player;
        }

        return lowest;
    }

    public String getName()
    {
        return getSettings().getName();
    }

    public ArrayList<UUID> getNoRewards()
    {
        return _noRewards;
    }

    public UUID getOwner(Entity entity)
    {
        if (_mobs.containsKey(entity.getUniqueId()))
            return _mobs.get(entity.getUniqueId());

        return null;
    }

    public ArrayList<UUID> getPlayed()
    {
        return new ArrayList<UUID>(_playersAndTimesDied.keySet());
    }

    public ArrayList<Player> getPlayers()
    {
        ArrayList<Player> players = new ArrayList<Player>();

        for (Player player : UtilPlayer.getPlayers())
        {
            if (!isInTeam(player))
                continue;

            players.add(player);
        }

        return players;
    }

    public ArrayList<Player> getPlayers(boolean alive)
    {
        ArrayList<Player> players = new ArrayList<Player>();

        for (Player player : UtilPlayer.getPlayers())
        {
            if (!isInTeam(player))
                continue;

            if (isAlive(player) != alive)
                continue;

            players.add(player);
        }

        return players;
    }

    public ArrayList<UUID> getRewardable()
    {
        ArrayList<UUID> played = getPlayed();
        played.removeAll(_noRewards);

        return played;
    }

    public TeamSettings getSettings()
    {
        return _teamSettings;
    }

    public Location getSpawn()
    {
        Location loc = UtilLoc.getFurtherest(_game.getPlayers(true), getSpawns());

        loc.setDirection(UtilLoc.getDirection2d(loc, _game.getSpectatorSpawn()));

        return loc;
    }

    public ArrayList<Location> getSpawns()
    {
        return _spawns;
    }

    public int getTimesDied(Player player)
    {
        return _playersAndTimesDied.get(player.getUniqueId());
    }

    public boolean isAlive()
    {
        return !_dead;
    }

    public boolean isAlive(Player player)
    {
        return !_deadAndWhen.containsKey(player.getUniqueId());
    }

    public boolean isInTeam(Entity entity)
    {
        if (_mobs.containsKey(entity.getUniqueId()))
            return true;

        return _playersAndTimesDied.containsKey(entity.getUniqueId());
    }

    public void removeFromTeam(Player player)
    {
        _lastToJoin.remove(player.getUniqueId());
        _playersAndTimesDied.remove(player.getUniqueId());
        _deadAndWhen.remove(player.getUniqueId());
        _noRewards.remove(player.getUniqueId());
    }

    public void setDead(boolean dead)
    {
        _dead = dead;
    }

    public void setDead(Player player)
    {
        if (!isAlive(player))
            return;

        UtilPlayer.setSpectator(player);

        new BukkitRunnable()
        {
            public void run()
            {
                player.getInventory().addItem(_game.getManager().getGameManager().getCompass());

                if (_game.getKits().length > 1)
                {
                    player.getInventory().addItem(_game.getManager().getGameManager().getKitLayout());
                }

                player.getInventory().setItem(8, _game.getManager().getGameManager().getNextGame());
            }
        }.runTaskLater(_game.getManager().getPlugin(), 20);

        new BukkitRunnable()
        {
            public void run()
            {
                UtilPlayer.tele(player, _game.getRandomSpectatorSpawn());
            }
        }.runTask(_game.getManager().getPlugin());

        _playersAndTimesDied.put(player.getUniqueId(), getTimesDied(player));
        _deadAndWhen.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public void setSpawns(ArrayList<Location> spawns)
    {
        _spawns = spawns;
    }
}
