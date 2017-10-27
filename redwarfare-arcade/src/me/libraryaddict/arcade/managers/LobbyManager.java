package me.libraryaddict.arcade.managers;

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.arcade.Arcade;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.forcekit.ForceKit;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.kits.KitInventory;
import me.libraryaddict.arcade.map.MapInventory;
import me.libraryaddict.core.C;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.events.PlayerUnloadEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.network.ServerInfo.ServerState;
import me.libraryaddict.redis.operations.RedisPublishServerInfo;

public class LobbyManager extends MiniPlugin {
    private class ForceKitCanidate implements Comparable<ForceKitCanidate> {
        private ForceKit kit;
        private String owner;
        private ArrayList<String> votes = new ArrayList<String>();
        private String info;

        public ForceKitCanidate(ForceKit kit, String player, String info) {
            owner = player;
            this.kit = kit;
            this.info = info;
        }

        public String getInfo() {
            return info;
        }

        public ForceKit getKit() {
            return kit;
        }

        public String getPlayer() {
            return owner;
        }

        public ArrayList<String> getVotes() {
            return votes;
        }

        @Override
        public int compareTo(ForceKitCanidate o) {
            return -Integer.compare(getVotes().size(), o.getVotes().size());
        }
    }

    private int _countdown = 90;
    private long _gameStarts;
    private ItemStack _kitSelector = new ItemBuilder(Material.FEATHER).setTitle(C.Blue + "Select a kit").build();
    private ArcadeManager _manager;
    private ItemStack _mapVote = new ItemBuilder(Material.EMPTY_MAP).setTitle(C.Gold + "Vote for a map").build();
    private boolean _waitingForTeles;
    private long _waitingSince;
    private ArrayList<ForceKitCanidate> canidates = new ArrayList<ForceKitCanidate>();

    public LobbyManager(Arcade arcade, ArcadeManager manager) {
        super(arcade, "Lobby Manager");

        _manager = manager;
    }

    public void addCanidate(Player player, ForceKit kit, String info) {
        canidates.removeIf((canidate) -> player.getName().equals(canidate.getPlayer()));

        if (canidates.size() > 10) {
            player.sendMessage(C.Gold + "Too many proposals!");
            return;
        }

        canidates.add(new ForceKitCanidate(kit, player.getName(), info));

        player.sendMessage(C.Gold + "Added your proposal!");
    }

    public void drawScoreboard() {
        int size = UtilPlayer.getPlayers().size();
        int minPlayers = getMinPlayers();
        GameState state = getGame().getState();

        FakeScoreboard board = _manager.getScoreboard().getMainScoreboard();

        ArrayList<String> lines = new ArrayList<String>();

        lines.add("");
        lines.add(C.Gold + "Map: " + C.Yellow
                + (state == GameState.PreMap ? "Voting.." : getManager().getWorld().getData().getName()));
        lines.add(C.Gold + "Players: " + C.Yellow + UtilPlayer.getPlayers().size());
        lines.add("");

        if ((state == GameState.PreMap ? size * 2 : size) >= minPlayers || getManager().getServer().isUpdateReady()) {
            if (getManager().getServer().isUpdateReady()) {
                lines.add(C.Gold + "Server updating..");
            }
            else if (state == GameState.PreMap) {
                lines.add(C.Yellow + "Map loads in " + (getCountdown() - 30));
            }
            else {
                lines.add(C.Yellow + "Game starting in " + getCountdown());
            }
        }
        else {
            lines.add(C.Yellow + "Waiting for " + (minPlayers - size) + " player" + (minPlayers - size == 1 ? "" : "s"));
        }

        board.setSidebar(lines);
    }

    public int getCountdown() {
        return _countdown;
    }

    public Game getGame() {
        return getGameManager().getGame();
    }

    public GameManager getGameManager() {
        return getManager().getGameManager();
    }

    public ArcadeManager getManager() {
        return _manager;
    }

    public int getMinPlayers() {
        if (getGame().getGameType().isStaticPlayers())
            return getGame().getGameType().getMinPlayers();

        if (UtilPlayer.getPlayers().isEmpty())
            _waitingSince = System.currentTimeMillis();

        int minPlayers = getGame().getGameType().getMinPlayers();

        long elasped = Math.floorDiv(System.currentTimeMillis() - _waitingSince, 15000);

        if (elasped > 0)
            minPlayers = (int) Math.max(getGame().getGameType().getAbsoluteMinPlayers(), minPlayers - elasped);

        return minPlayers;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        canidates.removeIf((canidate) -> event.getPlayer().getName().equals(canidate.getPlayer()));
    }

    public boolean isExplore() {
        return getGame().getOption(GameOption.EXPLORE_PREGAME);
    }

    public boolean isFrozen() {
        return getGame().getState() == GameState.MapLoaded && getCountdown() <= (isExplore() ? 10 : 30);
    }

    @EventHandler
    public void lobbyStart(GameStateEvent event) {
        if (event.getState() != GameState.PreMap) {
            return;
        }

        getManager().getScoreboard().discardScoreboards();

        FakeScoreboard scoreboard = getManager().getScoreboard().getMainScoreboard();

        getManager().getRank().setupScoreboard(scoreboard);

        scoreboard.setSidebarTitle(C.Red + "Red Warfare");

        drawScoreboard();

        if (getGame().getKits().length > 1) {
            for (Player player : UtilPlayer.getPlayers()) {
                player.getInventory().addItem(_kitSelector);
                player.getInventory().addItem(getManager().getGameManager().getKitLayout());
                player.getInventory().setItem(7, getManager().getPreferences().getIcon());
            }
        }

        if (getGame().getOption(GameOption.MAP_VOTE)) {
            for (Player player : UtilPlayer.getPlayers()) {
                player.getInventory().setItem(8, _mapVote);
            }
        }

        canidates.clear();

        canidates.add(new ForceKitCanidate(null, "Default", "Don't force kits"));

        _countdown = 90;
        _gameStarts = System.currentTimeMillis() + (_countdown * 1000);

        sendInfo();
    }

    @EventHandler
    public void onGameCountdown(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        if (!getGame().getState().isPreGame())
            return;

        if (getCountdown() <= 0)
            return;

        // if (getGame().getState() == GameState.PreMap && getManager().getWorld().getGameWorld() != null)
        // return;

        int size = UtilPlayer.getPlayers().size();
        int minPlayers = getMinPlayers();
        GameState state = getGame().getState();

        if (getManager().getServer().isUpdateReady() || size * 2 < minPlayers && state == GameState.PreMap) {
            _countdown = 90;
            _gameStarts = System.currentTimeMillis() + (_countdown * 1000);
            sendInfo();

            drawScoreboard();
            return;
        }
        else if (state == GameState.MapLoaded && size < minPlayers) {
            _countdown = 30;
            _gameStarts = System.currentTimeMillis() + (_countdown * 1000);
            sendInfo();

            drawScoreboard();
            return;
        }

        WorldManager world = getManager().getWorld();

        if (state == GameState.MapLoaded && _waitingForTeles && world.getLobby() != world.getGameWorld()
                && !world.getLobby().getPlayers().isEmpty()) {
            sendInfo();
            return;
        }

        setTime(_countdown - 1);

        sendInfo();
    }

    @EventHandler
    public void onGameState(GameStateEvent event) {
        if (event.getState() != GameState.PreMap)
            return;

        _waitingSince = System.currentTimeMillis();
    }

    @EventHandler
    public void onInfoSecond(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        if (getGame().getState().isPreGame())
            return;

        sendInfo();
    }

    @EventHandler
    public void onInteractKit(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || !item.isSimilar(_kitSelector))
            return;

        KitInventory kitInv = new KitInventory(event.getPlayer(), getManager());
        kitInv.openInventory();
    }

    @EventHandler
    public void onInteractMap(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (item == null || !item.isSimilar(_mapVote))
            return;

        event.setCancelled(true);

        event.getPlayer().updateInventory();

        MapInventory voting = new MapInventory(getManager().getWorld(), event.getPlayer());
        voting.openInventory();

        getManager().getWorld().addVoting(voting);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendInfo();

        if (!getGame().isPreGame())
            return;

        Player player = event.getPlayer();

        if (getGame().getKits().length > 1) {
            player.getInventory().addItem(_kitSelector);
            player.getInventory().addItem(getManager().getGameManager().getKitLayout());
            player.getInventory().setItem(7, getManager().getPreferences().getIcon());
        }

        if (isFrozen())
            return;

        FakeScoreboard mainBoard = getManager().getScoreboard().getMainScoreboard();

        Rank rank = getManager().getRank().getDisplayedRank(player);

        FakeTeam team = mainBoard.getTeam(rank.name());

        team.addPlayer(player.getName());

        if (getGame().getState() == GameState.PreMap && getGame().getOption(GameOption.MAP_VOTE)) {
            player.getInventory().setItem(8, _mapVote);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onMapLoad(GameStateEvent event) {
        if (event.getState() != GameState.MapLoaded) {
            return;
        }

        for (Player player : UtilPlayer.getPlayers()) {
            UtilInv.remove(player, _mapVote);
        }
    }

    @EventHandler
    public void onQuit(PlayerUnloadEvent event) {
        sendInfo(-1);

        Player player = event.getPlayer();

        FakeScoreboard mainBoard = getManager().getScoreboard().getMainScoreboard();

        Rank rank = getManager().getRank().getDisplayedRank(player);

        FakeTeam team = mainBoard.getTeam(rank.name());

        if (team != null) {
            team.removePlayer(player.getName());
        }
    }

    @EventHandler
    public void onStateChanged(GameStateEvent event) {
        new BukkitRunnable() {
            public void run() {
                sendInfo();
            }
        }.runTask(getPlugin());
    }

    private void sendInfo() {
        sendInfo(0);
    }

    private void sendInfo(int playersMod) {
        if (!getManager().getServer().isPublic())
            return;

        ServerState state;

        if (getGame().isLive()) {
            state = ServerState.IN_PROGRESS;
        }
        else if (getGame().isEnded()) {
            state = ServerState.ENDED;
        }
        else if (getCountdown() < 30) {
            state = ServerState.JOINABLE_NO_MERGE;
        }
        else {
            state = ServerState.JOINABLE;
        }

        ServerInfo serverInfo = new ServerInfo(state, ServerManager.getServerName(), getGame().getGameType(),
                Bukkit.getOnlinePlayers().size(), _gameStarts, getManager().getServer().getIP() + ":" + Bukkit.getPort(),
                getManager().getServer().isUpdateReady());

        new BukkitRunnable() {
            public void run() {
                new RedisPublishServerInfo(serverInfo);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public void setTime(int newTime) {
        _gameStarts = System.currentTimeMillis() + (_countdown * 1000);

        if (!_waitingForTeles) {
            _countdown = newTime;
        }
        else {
            _waitingForTeles = false;
        }

        GameState state = getGame().getState();

        if (getCountdown() == 30 && state == GameState.PreMap) {
            getGame().setState(GameState.MapLoaded);

            if (!isExplore()) {
                Bukkit.getPluginManager().callEvent(new GameStateEvent(GameState.PrepareGame));
            }

            drawScoreboard();
            _waitingForTeles = true;
            return;
        }
        else if (getCountdown() == 10 && isExplore()) {
            Bukkit.getPluginManager().callEvent(new GameStateEvent(GameState.PrepareGame));
        }
        else if (getCountdown() == 0) {
            Collections.sort(canidates);

            int votes = 0;

            for (ForceKitCanidate can : canidates) {
                votes += can.getVotes().size();
            }

            if (canidates.size() > 1) {
                canidates.removeIf((canidate) -> canidate.getVotes().size() < canidates.get(0).getVotes().size());

                ForceKitCanidate won = UtilMath.r(canidates);

                if (won.getKit() == null) {
                    Bukkit.broadcastMessage(C.Gold + won.getVotes().size() + "/" + votes + " players voted not to force kits!");
                }
                else {
                    Bukkit.broadcastMessage(C.Gold + won.getPlayer() + "'s forced kits won with " + won.getVotes().size() + "/"
                            + votes + " votes!");
                    Bukkit.broadcastMessage(C.Gold + "The kits: " + won.getInfo());
                }

                getGame().setForceKit(won.getKit());
            }

            getGameManager().startGame();
            return;
        }

        if (getCountdown() == 10 && canidates.size() > 1) {
            Bukkit.broadcastMessage(C.Gold + "Someone proposes to force kits! Click on your favorite!");

            for (ForceKitCanidate canidate : canidates) {
                FancyMessage message = new FancyMessage(C.Gold + canidate.getPlayer() + ": " + C.Yellow + canidate.getInfo())
                        .command("/pick " + canidate.getPlayer());

                for (Player p : Bukkit.getOnlinePlayers()) {
                    message.send(p);
                }
            }
        }

        if (getCountdown() <= 5 || getCountdown() == 10 || getCountdown() == 15 || getCountdown() == 30) {
            World world = getManager().getWorld().getGameWorld();

            world.playSound(world.getSpawnLocation(), Sound.ENTITY_CREEPER_DEATH, 99999, 0);

            getGame().Announce(C.Red + "The game will begin in " + UtilNumber.getTime(getCountdown()));
        }

        drawScoreboard();
    }

    public void setWaiting(long waitingSince) {
        _waitingSince = waitingSince;
    }

    public void pickKit(Player player, String string) {
        if (getCountdown() > 10)
            return;

        for (ForceKitCanidate canidate : canidates) {
            canidate.getVotes().remove(player.getName());

            if (!canidate.getPlayer().equals(string)) {
                continue;
            }

            canidate.getVotes().add(player.getName());

            player.sendMessage(C.Yellow + "You have voted for " + string + "'s proposal!");
        }
    }
}
