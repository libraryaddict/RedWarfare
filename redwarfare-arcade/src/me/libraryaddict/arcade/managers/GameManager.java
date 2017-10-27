package me.libraryaddict.arcade.managers;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.injector.PacketConstructor;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.*;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.kits.KitLayoutSelectInventory;
import me.libraryaddict.arcade.misc.SpectatorInventory;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.redis.operations.RedisJoinServerType;
import net.minecraft.server.v1_12_R1.EnumItemSlot;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.PlayerList;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Where the game is started, ended, etc.
 */
public class GameManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;
    private AttackType _border = new AttackType("Border", "%Killed% was caught exploring the border").setIgnoreArmor()
            .setNoKnockback();
    private ItemStack _compass = new ItemBuilder(Material.COMPASS).setTitle(C.Gray + "Spectator Compass")
            .addLore(C.Blue + C.Bold + "LEFT-CLICK " + C.Blue + "to teleport to nearby player",
                    C.Blue + C.Bold + "RIGHT-CLICK " + C.Blue + "to open a menu")
            .build();
    private long _emptySince;
    private PacketListener _equipListener;
    private Game _game;
    private ItemStack _joinNextGame = new ItemBuilder(Material.QUARTZ).setTitle(C.Blue + "Join next game").build();
    private ItemStack _kitLayout = new ItemBuilder(Material.NETHER_STAR).setTitle(C.DGreen + "Change kit layout")
            .addLore(C.Green + "Right click with this to change the kit layouts!").build();
    private ServerType _nextGame;

    public GameManager(JavaPlugin plugin, ArcadeManager manager)
    {
        super(plugin, "Game Manager");

        _arcadeManager = manager;

        _equipListener = new PacketAdapter(plugin, ListenerPriority.LOW, PacketType.Play.Server.ENTITY_EQUIPMENT,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN)
        {
            PacketConstructor constructor = ProtocolLibrary.getProtocolManager().createPacketConstructor(
                    PacketType.Play.Server.ENTITY_EQUIPMENT, 0, EnumItemSlot.HEAD, new ItemStack(Material.STONE));

            @Override
            public void onPacketSending(PacketEvent event)
            {
                try
                {
                    if (getGame().getState().isPreGame())
                        return;

                    PacketContainer packet = event.getPacket();

                    Player player = UtilPlayer.getPlayer(packet.getIntegers().read(0));

                    if (player == null)
                        return;

                    if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN)
                    {
                        ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();

                        for (EquipmentSlot slot : EquipmentSlot.values())
                        {
                            EquipmentEvent equipEvent = new EquipmentEvent(player, event.getPlayer(), slot,
                                    new ItemStack(Material.AIR));

                            Bukkit.getPluginManager().callEvent(equipEvent);

                            if (equipEvent.isCancelled() || !equipEvent.isModified())
                                continue;

                            PacketContainer newPacket = constructor.createPacket(player.getEntityId(),
                                    ReflectionManager.createEnumItemSlot(slot), equipEvent.getItem());

                            packets.add(newPacket);
                        }

                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                UtilPlayer.sendPacket(event.getPlayer(), packets);
                            }
                        }.runTask(getPlugin());

                    }
                    else
                    {
                        EquipmentEvent hatEvent = new EquipmentEvent(player, event.getPlayer(),
                                ReflectionManager.createEquipmentSlot(packet.getModifier().read(1)),
                                packet.getItemModifier().read(0));

                        Bukkit.getPluginManager().callEvent(hatEvent);

                        if (hatEvent.isCancelled() || !hatEvent.isModified())
                            return;

                        event.setPacket(packet = packet.shallowClone());

                        packet.getItemModifier().write(0, hatEvent.getItem());
                    }
                }
                catch (Exception ex)
                {
                    UtilError.handle(ex);
                }
            }
        };
    }

    private GameTeam assignTeam(Player player)
    {
        GameTeam current = getGame().getTeam(player);
        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        if (current != null)
        {
            current.removeFromTeam(player);
            board.getTeam(current.getName()).removePlayer(player.getName());
        }

        GameTeam lowest = getSmallestTeam();

        lowest.addToTeam(player);

        board.getTeam(lowest.getName()).addPlayer(player.getName());

        return lowest;
    }

    private void balanceTeams()
    {
        int playersPerTeam = Math.floorDiv(UtilPlayer.getPlayers().size(), getGame().getTeams().size());

        ArrayList<Pair<GameTeam, Integer>> teamBalance = new ArrayList<Pair<GameTeam, Integer>>();
        ArrayList<Player> specialKits = new ArrayList<Player>();

        for (Player player : UtilPlayer.getPlayers())
        {
            if (getGame().getTeam(player) != null)
                continue;

            Kit kit = getGame().getKit(player);

            if (!kit.isBalancedTeams())
                continue;

            specialKits.add(player);
        }

        for (GameTeam team : getGame().getTeams())
        {
            int amount = 0;

            for (Player player : team.getPlayers())
            {
                Kit kit = getGame().getKit(player);

                if (!kit.isBalancedTeams())
                    continue;

                amount++;
            }

            teamBalance.add(Pair.of(team, amount));
        }

        while (!specialKits.isEmpty() && !teamBalance.isEmpty())
        {
            Collections.sort(teamBalance, new Comparator<Pair<GameTeam, Integer>>()
            {
                @Override
                public int compare(Pair<GameTeam, Integer> o1, Pair<GameTeam, Integer> o2)
                {
                    return Integer.compare(o1.getValue(), o2.getValue());
                }
            });

            Iterator<Pair<GameTeam, Integer>> itel = teamBalance.iterator();

            while (itel.hasNext())
            {
                Pair<GameTeam, Integer> pair = itel.next();

                if (pair.getKey().getPlayers().size() >= playersPerTeam)
                {
                    itel.remove();
                    continue;
                }

                pair.getKey().addToTeam(specialKits.remove(0));
                pair.setValue(pair.getValue() + 1);
                break;
            }
        }

        for (Player player : UtilPlayer.getPlayers())
        {
            if (getGame().getTeam(player) != null)
                continue;

            assignTeam(player);
        }

        while (true)
        {
            System.out.println("Balancing teams..");

            GameTeam biggest = getBiggestTeam();
            GameTeam smallest = getSmallestTeam();

            if (Math.abs(biggest.getPlayed().size() - smallest.getPlayed().size()) <= 1)
                break;

            Player player = biggest.getLastToJoin();

            assignTeam(player);
        }
    }

    public void createGame()
    {
        if (getGame() != null && getGame().getState() != GameState.Dead)
        {
            endGame();
        }

        ServerType toMake = _nextGame == null ? getManager().getServer().getGameType() : _nextGame;

        try
        {
            Field field = PlayerList.class.getDeclaredField("maxPlayers");
            field.setAccessible(true);
            field.set(MinecraftServer.getServer().getPlayerList(), toMake.getMaxPlayers());
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        _nextGame = null;

        _game = GameSettings.getGameType(toMake).createInstance(getManager());
        _game.setState(GameState.PreMap);

        if (getGame().getOption(GameOption.SERVER_HANDLES_WORLDS))
        {
            getManager().getWorld().loadMapChoices();
        }
    }

    public void endGame()
    {
        getGame().setState(GameState.End);
    }

    private GameTeam getBiggestTeam()
    {
        GameTeam biggest = null;

        for (GameTeam team : getGame().getTeams())
        {
            if (biggest != null && team.getPlayed().size() < biggest.getPlayed().size())
            {
                continue;
            }

            if (biggest != null && biggest.getPlayed().size() == team.getPlayed().size() && UtilMath.nextBoolean())
                continue;

            biggest = team;
        }

        return biggest;
    }

    public ItemStack getCompass()
    {
        return _compass;
    }

    public Game getGame()
    {
        return _game;
    }

    public ServerType getGameType()
    {
        return getGame().getGameType();
    }

    public ItemStack getKitLayout()
    {
        return _kitLayout;
    }

    public ArcadeManager getManager()
    {
        return _arcadeManager;
    }

    public ItemStack getNextGame()
    {
        return _joinNextGame;
    }

    private GameTeam getSmallestTeam()
    {
        GameTeam smallest = null;

        for (GameTeam team : getGame().getTeams())
        {
            if (smallest != null && team.getPlayed().size() > smallest.getPlayed().size())
            {
                continue;
            }

            if (smallest != null && smallest.getPlayed().size() == team.getPlayed().size() && UtilMath.nextBoolean())
                continue;

            smallest = team;
        }

        return smallest;
    }

    @EventHandler
    public void onChat(ChatEvent event)
    {
        Player player = event.getPlayer();

        boolean specialChat = event.getMessage()[0].startsWith("@");

        if (specialChat)
            event.removeFirstLetter();

        if (!(getGame() instanceof TeamGame))
        {
            if (specialChat)
            {
                event.setCancelled(true);
                player.sendMessage(C.Red + "This is not a team game!");
            }

            return;
        }

        boolean alwaysTalkTeamChat = Preference.getPreference(event.getPlayer(), getGame().getTeamChat());

        if (!specialChat && !alwaysTalkTeamChat)
        {
            return;
        }

        if (!getGame().isLive() && (!getGame().isPreGame() || getManager().getLobby().getCountdown() > 10))
        {
            if (!alwaysTalkTeamChat)
            {
                event.setCancelled(true);

                if (getGame().isPreGame())
                    player.sendMessage(C.Red + "The game hasn't started");
                else
                    player.sendMessage(C.Red + "The game has ended!");
            }

            return;
        }

        if (specialChat == alwaysTalkTeamChat)
            return;

        if (!getGame().isAlive(player))
        {
            player.sendMessage(C.Red + "You cannot talk in team chat if you are dead!");
            event.setCancelled(true);

            return;
        }

        GameTeam team = getGame().getTeam(player);

        if (team == null)
        {
            event.setCancelled(true);
            player.sendMessage(C.Red + "You're not in a team");

            return;
        }

        event.setPrefix(team.getColoring() + C.Bold + "TEAM " + C.Reset + event.getPrefix());

        Iterator<Player> itel = event.getRecipients().iterator();

        while (itel.hasNext())
        {
            Player p = itel.next();

            if (team.isInTeam(p))
                continue;

            itel.remove();
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCheckGameState(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        getGame().checkGameState();
    }

    @EventHandler
    public void onCompassInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (getGame().isAlive(player))
            return;

        if (!UtilInv.isItem(event.getItem(), Material.COMPASS))
            return;

        if (event.getAction().name().contains("LEFT"))
        {
            ArrayList<Player> players = new ArrayList<Player>();

            for (Player p : UtilPlayer.getPlayers())
            {
                if (!getGame().isAlive(p))
                    continue;

                if (getGame() instanceof TeamGame)
                {
                    if (getGame().getTeam(p) != null && !getGame().sameTeam(p, player) && !getGame().getKit(p).canTeleportTo())
                        continue;
                }
                else if (!getGame().getKit(p).canTeleportTo())
                    continue;

                players.add(p);
            }

            Player toTele = UtilMath.r(players);

            if (toTele == null)
            {
                player.sendMessage(C.Red + "Can't find someone to teleport to");
                return;
            }

            UtilPlayer.tele(player, toTele);
        }
        else if (event.getAction().name().contains("RIGHT"))
        {
            new SpectatorInventory(player, getGame()).openInventory();
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(DeathEvent event)
    {
        if (event.getAttackType() != AttackType.SUICIDE && event.getAttackType() != AttackType.QUIT)
            return;

        Player player = event.getPlayer();

        GameTeam team = getGame().getTeam(player);

        if (team == null || !team.isAlive(player))
            return;

        if (!getGame().isLive())
        {
            return;
        }

        team.addNoRewards(player);
    }

    // @EventHandler
    public void onEmptyCheck(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        if (getManager().getServer().isTestServer())
            return;

        boolean empty = Bukkit.getOnlinePlayers().isEmpty();

        if (!empty)
        {
            _emptySince = 0;
        }
        else if (_emptySince == 0)
        {
            _emptySince = System.currentTimeMillis();
        }

        if (!empty || !UtilTime.elasped(_emptySince, 90000))
            return;

        System.out.println("Shutting down as the server is empty");

        getManager().getServer().shutdown("Server is empty");
    }

    @EventHandler
    public void onGameEnd(GameStateEvent event)
    {
        if (event.getState() != GameState.End)
            return;

        getManager().getServer().checkUpdate();

        for (Player player : getGame().getPlayers(true))
        {
            player.setAllowFlight(true);
            player.setFlying(true);

            UtilEnt.velocity(player, new Vector(0, 0.1, 0), false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGamePrepare(GameStateEvent event)
    {
        if (event.getState() != GameState.PrepareGame)
        {
            return;
        }

        balanceTeams();

        int tick = 0;

        for (Player player : UtilPlayer.getPlayers())
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    GameTeam team = getGame().getTeam(player);

                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setFallDistance(0);

                    if (getGame().getOption(GameOption.LOCK_TO_SPAWN))
                        UtilPlayer.tele(player, team.getSpawn());
                }
            }.runTaskLater(getPlugin(), tick++ / 3);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameState(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        World world = getManager().getWorld().getGameWorld();

        Pair<Sound, Float> sounds = getGame().getOption(GameOption.GAME_START_SOUND);

        if (sounds != null)
        {
            world.playSound(world.getSpawnLocation(), sounds.getKey(), 99999, sounds.getValue());
        }

        getGame().Announce(C.Gold + "The game has begun!");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onGameStateDead(GameStateEvent event)
    {
        if (event.getState() != GameState.Dead)
            return;

        if (getGame().getOption(GameOption.HATS))
        {
            ProtocolLibrary.getProtocolManager().removePacketListener(_equipListener);
        }

        for (Kit kit : getGame().getKits())
        {
            kit.unregisterAbilities();
        }

        getGame().unregisterListeners();

        for (Player player : UtilPlayer.getPlayers())
        {
            UtilPlayer.resetState(player);
        }

        createGame();

        World world = getManager().getWorld().getGameWorld();

        for (Player player : world.getPlayers())
        {
            UtilPlayer.tele(player, getManager().getWorld().getRandomHubSpawn());

            UtilPlayer.showToAll(player);
        }

        if (getGame().getOption(GameOption.SERVER_HANDLES_WORLDS))
        {
            getManager().getWorld().unloadWorld();
        }
    }

    @EventHandler
    public void onGameStateEnd(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (getGame().getState() != GameState.End)
            return;

        if (!UtilTime.elasped(getGame().getStateChanged(), 15000))
            return;

        if (getManager().getServer().isMerging())
            return;

        getGame().setState(GameState.Dead);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onGameStateLive(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        for (Player player : UtilPlayer.getPlayers())
        {
            UtilPlayer.resetState(player);
        }

        balanceTeams();

        if (getGame().getForceKit() != null)
        {
            for (GameTeam team : getGame().getTeams())
            {
                ArrayList<Kit> kits = getGame().getForceKit().getKits(team);

                Collections.shuffle(kits);

                Iterator<Kit> itel = kits.iterator();

                for (Player player : team.getPlayers())
                {
                    getGame().chooseKit(player, itel.next(), true);
                }
            }
        }

        try
        {
            getGame().setupScoreboards();
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }

        for (Kit kit : getGame().getKits())
        {
            try
            {
                kit.registerAbilities();
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }
        }

        for (GameTeam team : getGame().getTeams())
        {
            for (Player player : team.getPlayers())
            {
                if (getGame().getOption(GameOption.TEAM_HOTBAR))
                {
                    ItemBuilder builder = new ItemBuilder(Material.LEATHER_CHESTPLATE);

                    builder.setTitle(team.getColoring() + C.Bold + team.getName());
                    builder.addLore(team.getColoring() + "You are on " + team.getName());
                    builder.setColor(team.getColor());

                    player.getInventory().setItem(8, builder.build());
                }

                try
                {
                    getGame().getKit(player).applyKit(player);
                }
                catch (Exception ex)
                {
                    UtilError.handle(ex);
                }
            }
        }
    }

    @EventHandler
    public void onHatsStart(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        ProtocolLibrary.getProtocolManager().addPacketListener(_equipListener);

        PacketConstructor constructor = ProtocolLibrary.getProtocolManager().createPacketConstructor(
                PacketType.Play.Server.ENTITY_EQUIPMENT, 0, EnumItemSlot.HEAD, new ItemStack(Material.STONE));

        for (GameTeam team : getGame().getTeams())
        {
            for (Player receivePacket : team.getPlayers())
            {
                for (Player sendPlayer : UtilPlayer.getPlayers())
                {
                    if (receivePacket == sendPlayer)
                        continue;

                    for (EquipmentSlot slot : EquipmentSlot.values())
                    {
                        try
                        {
                            ItemStack item = UtilInv.getItem(sendPlayer, slot);

                            if (item == null)
                                item = new ItemStack(Material.AIR);

                            ProtocolLibrary.getProtocolManager().sendServerPacket(receivePacket, constructor
                                    .createPacket(sendPlayer.getEntityId(), ReflectionManager.createEnumItemSlot(slot), item));
                        }
                        catch (InvocationTargetException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInteractJoinGame(PlayerInteractEvent event)
    {
        if (!UtilInv.isSimilar(event.getItem(), getNextGame()))
            return;

        Player player = event.getPlayer();

        if (!Recharge.canUse(player, "SwitchServer"))
            return;

        Recharge.use(player, "SwitchServer", 700);

        UUID uuid = player.getUniqueId();
        String server = getGame().getGameType().getName();

        new BukkitRunnable()
        {
            public void run()
            {
                new RedisJoinServerType(uuid, server);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    @EventHandler
    public void onInteractKitLayout(PlayerInteractEvent event)
    {
        if (!UtilInv.isSimilar(event.getItem(), getKitLayout()))
            return;

        Player player = event.getPlayer();

        new KitLayoutSelectInventory(player, getManager()).openInventory();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if (getGame().isPreGame())
        {
            if (!(getGame().getState() == GameState.MapLoaded || getGame().getState() == GameState.PrepareGame)
                    || getManager().getLobby().isFrozen())
            {
                return;
            }

            if (getGame().getOption(GameOption.FLYING_PREGAME))
            {
                event.getPlayer().setAllowFlight(true);
            }

            return;
        }

        Player player = event.getPlayer();

        if (getGame().isAlive(player))
            return;

        UtilPlayer.setSpectator(player);

        player.getInventory().addItem(getCompass());

        if (getGame().getKits().length > 1)
        {
            player.getInventory().addItem(getKitLayout());
        }

        player.getInventory().setItem(8, getNextGame());

        for (Player p : getGame().getPlayers(false))
        {
            player.hidePlayer(p);
        }

        if (getGame().getTeam(player) != null)
            return;

        FakeScoreboard specs = getManager().getScoreboard().getScoreboard("Spectators");

        FakeTeam team = specs.getTeam("Spectators");
        team.addPlayer(player);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onMapLoadState(GameStateEvent event)
    {
        if (event.getState() != GameState.MapLoaded)
        {
            return;
        }

        if (getGame().getOption(GameOption.SERVER_HANDLES_WORLDS))
        {
            getManager().getWorld().loadWorld();
        }

        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        for (GameTeam team : getGame().getTeams())
        {
            FakeTeam fakeTeam = board.createTeam(team.getName());

            fakeTeam.setSeeInvisiblePlayers(!getGame().getOption(GameOption.ATTACK_TEAM));
            fakeTeam.setPrefix(team.getColoring());

            for (Player player : team.getPlayers())
            {
                fakeTeam.addPlayer(player.getName());
            }
        }

        if (!getManager().getLobby().isExplore())
            return;

        int tick = 0;

        for (Player player : UtilPlayer.getPlayers())
        {
            new BukkitRunnable()
            {
                public void run()
                {
                    UtilPlayer.tele(player, getGame().getRandomSpectatorSpawn());

                    if (getGame().getOption(GameOption.FLYING_PREGAME))
                    {
                        player.setAllowFlight(true);
                    }
                }
            }.runTaskLater(getPlugin(), tick++);
        }
    }

    @EventHandler
    public void onPlayerSpawn(PlayerSpawnLocationEvent event)
    {
        Player player = event.getPlayer();

        if (getGame().getState() == GameState.PreMap)
        {
            event.setSpawnLocation(getManager().getWorld().getRandomHubSpawn());
        }
        else
        {
            if (getManager().getLobby().isFrozen())
            {
                GameTeam team = getGame().getTeam(player);

                if (team == null)
                {
                    team = assignTeam(player);
                }

                event.setSpawnLocation(team.getSpawn());
            }
            else
            {
                event.setSpawnLocation(getGame().getRandomSpectatorSpawn());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event)
    {
        Player player = event.getPlayer();

        GameTeam team = getGame().getTeam(player);

        if (team == null || !team.isAlive(player))
            return;

        getManager().getDamage().newDamage(event.getPlayer(), AttackType.QUIT, 0);

        if (!getGame().getState().isPreGame())
        {
            return;
        }

        team.removeFromTeam(player);
    }

    @EventHandler
    public void onSec(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        GameState state = getGame().getState();

        if (state.isEnd() || state == GameState.PreMap)
            return;

        WorldData data = getGame().getData();

        for (Player player : UtilPlayer.getPlayers())
        {
            Location loc = player.getLocation();

            if (player.getWorld() != getManager().getWorld().getGameWorld())
                continue;

            if (getGame().isInsideBorder(loc))
                continue;

            boolean teleport = !state.isLive() || !getGame().isAlive(player);

            Location toTele = data.getInsideBorder(loc);

            if (teleport)
            {
                UtilPlayer.tele(player, toTele);
            }
            else
            {
                getManager().getDamage().newDamage(player, _border, 2);
            }

            if (Recharge.canUse(player, "Border Message"))
            {
                player.sendMessage(C.Red + "You cannot explore the border!");

                Recharge.use(player, "Border Message", 4000);
            }
        }
    }

    public void setNextGame(ServerType gameDisplay)
    {
        _nextGame = gameDisplay;
    }

    public void startGame()
    {
        getGame().setState(GameState.Live);
    }
}
