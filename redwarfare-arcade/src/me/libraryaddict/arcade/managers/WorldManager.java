package me.libraryaddict.arcade.managers;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.map.MapInfo;
import me.libraryaddict.arcade.map.MapInventory;
import me.libraryaddict.core.C;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilFile;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilPlayer;
import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class WorldManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;
    private WorldData _data;
    private World _gameWorld;
    private ArrayList<MapInventory> _inventories = new ArrayList<MapInventory>();
    private ArrayList<String> _lastMap = new ArrayList<String>();
    private World _lobbyWorld;
    private ArrayList<MapInfo> _mapVoting = new ArrayList<MapInfo>();

    public WorldManager(Arcade arcade, ArcadeManager arcadeManager)
    {
        super(arcade, "World Manager");

        _arcadeManager = arcadeManager;

        copyHub();
    }

    public void addVoting(MapInventory voting)
    {
        _inventories.add(voting);
    }

    private void copyHub()
    {
        File hub = new File(UtilFile.getUpdateFolder(), "Hubs/" + getManager().getServer().getGameType().getName() + ".zip");

        if (!hub.exists())
        {
            hub = new File(UtilFile.getUpdateFolder(), "Hubs/Default.zip");
        }

        if (hub.exists())
            UtilFile.extractZip(hub, new File("world"));
    }

    public WorldData getData()
    {
        return _data;
    }

    public World getGameWorld()
    {
        return _gameWorld;
    }

    public World getLobby()
    {
        return _lobbyWorld;
    }

    public ArcadeManager getManager()
    {
        return _arcadeManager;
    }

    public ArrayList<MapInfo> getMapVoting()
    {
        return _mapVoting;
    }

    public Location getRandomHubSpawn()
    {
        Location spec = getLobby().getSpawnLocation();
        Location loc = spec;

        loop:

        for (int i = 0; i < 40; i++)
        {
            Location l = spec.clone().add(UtilMath.rr(-10, 10), 0, UtilMath.rr(-10, 10));

            if (!UtilLoc.isSpawnableHere(l))
                continue;

            if (!UtilLoc.hasSight(l, spec))
                continue loop;

            loc = l;
            break;
        }

        return loc;
    }

    private MapInfo getVotedFor(Player player)
    {
        for (MapInfo mapInfo : getMapVoting())
        {
            if (mapInfo.hasVotedFor(player))
            {
                return mapInfo;
            }
        }

        return null;
    }

    public void loadLobby()
    {
        _lobbyWorld = Bukkit.getWorld("world");
        _lobbyWorld.setGameRuleValue("doDaylightCycle", "false");
        _lobbyWorld.setFullTime(0);
        _lobbyWorld.setStorm(false);
        _lobbyWorld.setThundering(false);

        UtilFile.delete(new File("world/playerdata"));
    }

    public void loadMapChoices()
    {
        _mapVoting.clear();

        for (File file : UtilFile.getMaps(getManager().getGame().getGameType()))
        {
            MapInfo mapInfo = null;

            try
            {
                mapInfo = new MapInfo(loadMapInfo(file));
            }
            catch (Exception ex)
            {
                System.err.println("Error while loading the map " + file.getName());
                ex.printStackTrace();
                continue;
            }

            _mapVoting.add(mapInfo);
            System.out.print("Loaded " + mapInfo.getData().getName());
        }

        Collections.shuffle(_mapVoting);

        int remove = (int) Math.floor(_mapVoting.size() / 3D);

        Iterator<MapInfo> itel = _mapVoting.iterator();

        while (itel.hasNext())
        {
            MapInfo info = itel.next();

            String name = info.getData().getName();

            for (int i = 0; i < Math.min(_lastMap.size(), remove); i++)
            {
                if (i >= _lastMap.size())
                {
                    break;
                }

                if (_lastMap.get(i).equals(name))
                {
                    System.out.println("Remove selection " + name);
                    itel.remove();
                    break;
                }
            }
        }

        while (_mapVoting.size() > 3)
        {
            MapInfo info = _mapVoting.remove(0);
            System.out.println("Can't vote for " + info.getData().getName());
        }

        for (MapInfo data : _mapVoting)
        {
            _lastMap.add(data.getData().getName());

            if (_lastMap.size() > 5)
            {
                _lastMap.remove(0);
            }
        }

    }

    public WorldData loadMapInfo(File file)
    {
        WorldData data = new WorldData(file);
        data.loadData();

        return data;
    }

    public void loadWorld()
    {
        _inventories.clear();

        Collections.sort(_mapVoting, new Comparator<MapInfo>()
        {

            @Override
            public int compare(MapInfo o1, MapInfo o2)
            {
                return Integer.compare(o2.getVotes(), o1.getVotes());
            }
        });

        MapInfo winner = _mapVoting.get(0);
        int players = UtilPlayer.getPlayers().size();

        if (players < 20 && winner.getData().getTeams() > 2)
        {
            winner = _mapVoting.stream().filter((vote) -> vote.getData().getTeams() <= 2).findFirst().orElse(winner);
        }

        Bukkit.broadcastMessage(C.Gold + "Map loaded: " + C.Yellow + winner.getData().getName());

        setGameWorld(loadWorld(winner.getData()), winner.getData());
    }

    public World loadWorld(WorldData data)
    {
        data.setupWorld();

        WorldCreator creator = new WorldCreator(data.getWorldFolder().getName());

        World world = Bukkit.createWorld(creator);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setTime(13000);
        world.setStorm(false);
        // Make arrows despawn after 20 seconds
        ((CraftWorld) world).getHandle().spigotConfig.arrowDespawnRate = 20 * 20;

        data.loadData(world);

        setupTeams(data);

        return world;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        if (getManager().getGame().getState() != GameState.PreMap)
            return;

        for (MapInfo mapInfo : getMapVoting())
        {
            mapInfo.removeVote(event.getPlayer());
        }
    }

    public void setGameWorld(World world, WorldData data)
    {
        _gameWorld = world;
        _data = data;
    }

    public void setLobby(World world)
    {
        _lobbyWorld = world;
    }

    public void setupTeams(WorldData data)
    {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(data.getWorldFolder(), "config.yml"));

        ArrayList<GameTeam> teams = new ArrayList<GameTeam>();

        for (String key : config.getConfigurationSection("Teams").getKeys(false))
        {
            ConfigurationSection teamConfig = config.getConfigurationSection("Teams").getConfigurationSection(key);

            TeamSettings settings = TeamSettings.valueOf(key);

            GameTeam team = new GameTeam(getManager().getGameManager().getGame(), settings);

            ArrayList<Location> spawns = new ArrayList<Location>();

            for (String s : teamConfig.getStringList("Spawns"))
            {
                spawns.add(data.parseLoc(s).add(0.5, 0, 0.5));
            }

            team.setSpawns(spawns);

            teams.add(team);
        }

        getManager().getGame().setTeams(teams);
    }

    public void unloadWorld()
    {
        unloadWorld(getGameWorld());

        _gameWorld = null;
    }

    public void unloadWorld(World world)
    {
        for (Player player : world.getPlayers())
        {
            player.kickPlayer(C.Red + "Forced out of the server by bad programming");
        }

        Bukkit.unloadWorld(world, false);

        File file = world.getWorldFolder();

        System.out.println("Unloading world " + file.getAbsolutePath());

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                UtilFile.delete(file);
            }
        }.runTaskLater(getPlugin(), 10);
    }

    public void voteFor(Player player, MapInfo info)
    {
        if (getVotedFor(player) == info)
        {
            return;
        }

        for (MapInfo mapInfo : getMapVoting())
        {
            mapInfo.removeVote(player);
        }

        info.voteFor(player);

        player.sendMessage(C.Gold + "Voted for the map " + info.getData().getName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);

        Iterator<MapInventory> itel = _inventories.iterator();

        while (itel.hasNext())
        {
            MapInventory voting = itel.next();

            if (!voting.isInUse())
            {
                itel.remove();
                continue;
            }

            voting.updateChoices(this);
        }
    }

}
