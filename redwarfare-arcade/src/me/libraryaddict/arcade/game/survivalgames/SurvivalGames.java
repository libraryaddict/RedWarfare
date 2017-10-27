package me.libraryaddict.arcade.game.survivalgames;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.LootEvent;
import me.libraryaddict.arcade.events.WinEvent;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.LootTier;
import me.libraryaddict.arcade.game.SoloGame;
import me.libraryaddict.arcade.game.survivalgames.commands.CommandBounty;
import me.libraryaddict.arcade.game.survivalgames.commands.CommandPoints;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.arcade.managers.LootManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.combat.CombatEvent;
import me.libraryaddict.core.combat.CombatLog;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilFirework;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.core.utils.UtilTime;

public class SurvivalGames extends SoloGame
{
    private CommandBounty _bountyCommand;
    private BountyManager _bountyManager;
    private int _chestRefillTime = 7 * 60;
    private int _deathmatchTime = 14 * 60;
    private int _fireworkSpawn;
    private boolean _frozen;
    private int _gameEndTime = 17 * 60;
    private HashMap<UUID, ArrayList<String>> _hiddenNames = new HashMap<UUID, ArrayList<String>>();
    private int _looted;
    private CommandPoints _pointsCommand;
    private SurvivalGamesItems _survivalGamesItems;
    private HashMap<UUID, ArrayList<UUID>> _teabagged = new HashMap<UUID, ArrayList<UUID>>();

    private AttackType END_OF_GAME = new AttackType("End of Game", "%Killed% was slaughtered by the server").setNoKnockback()
            .setIgnoreArmor().setInstantDeath();

    public SurvivalGames(ArcadeManager arcadeManager)
    {
        super(arcadeManager, ServerType.SurvivalGames);

        setOption(GameOption.BREAK_GRASS, true);
        setOption(GameOption.PLAYER_DROP_ITEM, true);
        setOption(GameOption.REMOVE_SEEDS_DROP, true);
        setOption(GameOption.PICKUP_ITEM, true);
        setOption(GameOption.KILLS_IN_TAB, true);
        setOption(GameOption.HUNGER, true);
        setOption(GameOption.EXPLORE_PREGAME, false);
        setOption(GameOption.GAME_START_SOUND, Pair.of(Sound.ENTITY_MULE_DEATH, 0F));
        setOption(GameOption.DEAD_BODIES, true);
        setOption(GameOption.CHEST_LOOT, true);
        setOption(GameOption.PLAYER_DEATH_SOUND, Pair.of(Sound.ENTITY_LIGHTNING_THUNDER, 2F));
        setOption(GameOption.DEATH_ITEMS, true);
        setOption(GameOption.PLACABLE_BLOCKS, new Material[]
            {
                    Material.CAKE_BLOCK
            });
        setOption(GameOption.UNBREAKABLE, false);
        setOption(GameOption.ALLOW_CRAFTING, true);

        _survivalGamesItems = new SurvivalGamesItems(this);

        registerListener(_survivalGamesItems);

        _bountyManager = new BountyManager(this);
        _bountyCommand = new CommandBounty(this);
        _pointsCommand = new CommandPoints(this);

        getManager().getCommand().registerCommand(_bountyCommand);
        getManager().getCommand().registerCommand(_pointsCommand);
    }

    public void addPoints(Player player, String reason, long newPoints)
    {
        Currency.add(player, CurrencyType.POINT, reason, newPoints);

        displayPoints(player, _bountyManager.getKillworth(player));
    }

    public void displayPoints(Player player, long newPoints)
    {
        getScoreboard().getMainScoreboard().makeScore(DisplaySlot.BELOW_NAME, player.getName(), (int) newPoints);
    }

    public void drawScoreboard()
    {
        FakeScoreboard board = getScoreboard().getMainScoreboard();

        ArrayList<String> lines = new ArrayList<String>();

        lines.add(C.Blue + "Time: " + C.Aqua + UtilNumber.getTimeAbbr(getGameTime()));
        lines.add("");
        lines.add(C.DAqua + "Tributes " + C.Blue + getPlayers(true).size());
        lines.add(C.DGreen + "Specs " + C.Gray + getPlayers(false).size());
        lines.add("");

        long timeTillRefill = _chestRefillTime - getGameTime();
        long timeTillDeathmatch = _deathmatchTime - getGameTime();
        long timeTillEnd = _gameEndTime - getGameTime();

        if (timeTillRefill >= 0 && timeTillRefill <= 180)
        {
            if (timeTillRefill == 0)
            {
                lines.add(C.Gold + "Chests refilled");
            }
            else
            {
                lines.add(C.Gold + "Refill in " + UtilNumber.getTimeAbbr(timeTillRefill));
            }
        }
        else if (timeTillDeathmatch > 60)
        {
            lines.add(C.Gold + "Unlooted: " + _looted);
        }
        else if (timeTillDeathmatch >= 0)
        {
            lines.add(C.DRed + "Deathmatch in " + timeTillDeathmatch);
        }
        else
        {
            lines.add(C.DRed + "Game ends in " + UtilNumber.getTimeAbbr(timeTillEnd));
        }

        board.setSidebar(lines);
    }

    @EventHandler
    public void fillChests(GameStateEvent event)
    {
        if (event.getState() != GameState.MapLoaded)
            return;

        LootManager loot = getManager().getLoot();

        WorldData data = getData();

        LootTier tier1 = loot.getLoot("Tier1");

        for (Block block : data.getCustomBlocks("Chests"))
        {
            if (loot.isLooted(block))
                continue;

            _looted++;
            loot.fillWithLoot(block, tier1);
        }

        LootTier tier2 = loot.getLoot("Tier2");

        for (Block block : data.getCustomBlocks("Special Chests"))
        {
            if (loot.isLooted(block))
                continue;

            _looted++;
            loot.fillWithLoot(block, tier2);
        }

        LootTier furnace = loot.getLoot("Furnace");

        for (Block block : data.getCustomBlocks("Furnaces"))
        {
            if (loot.isLooted(block))
                continue;

            loot.fillWithLoot(block, furnace);
        }
    }

    public BountyManager getBountyManager()
    {
        return _bountyManager;
    }

    @Override
    public int getCreditsKill()
    {
        return 0;
    }

    @Override
    public int getCreditsLose()
    {
        return 1;
    }

    @Override
    public int getCreditsWin()
    {
        return 5;
    }

    public int getDeathmatchTime()
    {
        return _deathmatchTime;
    }

    @EventHandler
    public void onBountyDeath(DeathEvent event)
    {
        Player player = event.getPlayer();

        CombatLog log = event.getCombatLog();

        ArrayList<CombatEvent> logs = log.getEvents();
        Player killer = null;

        for (CombatEvent combatLog : logs)
        {
            if (UtilTime.elasped(combatLog.getWhen(), 15000))
            {
                break;
            }

            CustomDamageEvent dmgEvent = combatLog.getEvent();

            if (!(dmgEvent.getFinalDamager() instanceof Player))
                continue;

            killer = (Player) dmgEvent.getFinalDamager();
        }

        getBountyManager().onDeath(player, killer);
    }

    @EventHandler
    public void onChat(ChatEvent event)
    {
        event.setPrefix(
                C.DGreen + "[" + C.Green + Currency.get(event.getPlayer(), CurrencyType.POINT) + C.DGreen + "] " + C.White);
    }

    @EventHandler
    public void onChestLoot(LootEvent event)
    {
        Block block = event.getBlock();

        if (block.getType() != Material.CHEST && block.getType() != Material.TRAPPED_CHEST)
            return;

        _looted--;
    }

    @EventHandler
    public void onCustomDamage(CustomDamageEvent event)
    {
        if (event.getDamager() == null && event.getFinalDamager() == null)
            return;

        if (!_frozen)
            return;

        // Deny deathmatch damage
        event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        Player player = event.getPlayer();

        FakeScoreboard board = getScoreboard().getScoreboard("Player-" + player.getName());

        if (board != null)
        {
            getScoreboard().discardScoreboard(board);
        }

        new BukkitRunnable()
        {
            public void run()
            {
                if (!isLive())
                    return;

                if (getGameTime() >= _deathmatchTime - 61)
                    return;

                if (getPlayers(true).size() > 4)
                    return;

                _deathmatchTime = getGameTime() + 60;
                _gameEndTime = getGameTime() + (5 * 60);

                Announce(C.Red + "The deathmatch will begin in " + UtilNumber.getTime(60));
            }
        }.runTask(getManager().getPlugin());
    }

    @EventHandler
    public void onGameDead(GameStateEvent event)
    {
        if (event.getState() != GameState.Dead)
            return;

        getManager().getCommand().unregisterCommand(_bountyCommand);
        getManager().getCommand().unregisterCommand(_pointsCommand);
    }

    @EventHandler
    public void onHideNametags(TimeEvent event)
    {
        if (!isLive())
            return;

        if (event.getType() != TimeType.TICK)
            return;

        ArrayList<Player> players = getPlayers(true);

        for (Player viewer : players)
        {
            _hiddenNames.putIfAbsent(viewer.getUniqueId(), new ArrayList<String>());

            ArrayList<String> names = _hiddenNames.get(viewer.getUniqueId());

            FakeScoreboard board = getScoreboard().getScoreboard("Player-" + viewer.getName());

            FakeTeam see = board.getTeam("See");
            FakeTeam invis = board.getTeam("Invis");

            for (Player viewed : players)
            {
                boolean canSee = UtilLoc.hasSight(viewer.getEyeLocation(), viewed.getEyeLocation())
                        && UtilLoc.getDistance(viewer, viewed) <= 10;

                String name = viewed.getName();

                if (names.contains(name) != canSee)
                    continue;

                if (canSee)
                {
                    names.remove(name);

                    invis.removePlayer(name);
                    see.addPlayer(name);
                }
                else
                {
                    names.add(name);

                    see.removePlayer(name);
                    invis.addPlayer(name);
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if (!_frozen)
            return;

        if (UtilLoc.getDistance2d(event.getFrom(), event.getTo()) == 0)
            return;

        if (!isAlive(event.getPlayer()))
            return;

        event.setCancelled(true);

        Location loc = event.getFrom();
        loc.setDirection(event.getTo().getDirection());
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event)
    {
        if (!_frozen)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        if (!isLive())
            return;

        drawScoreboard();

        int timeTillRefill = _chestRefillTime - getGameTime();
        int timeTillDeathmatch = _deathmatchTime - getGameTime();
        int timeTillEnd = _gameEndTime - getGameTime();
        int timeTillWarmup = timeTillDeathmatch + 15;

        if (timeTillDeathmatch > 60)
        {
            switch (timeTillRefill)
            {
            case 180:
            case 120:
            case 60:
            case 30:
            case 15:
            case 10:
            case 5:
            case 4:
            case 3:
            case 2:
            case 1:
                Announce(C.Gold + "The chests will be refilled in " + UtilNumber.getTime(timeTillRefill));
                break;
            case 0:

                for (Player player : UtilPlayer.getPlayers())
                {
                    player.playSound(player.getEyeLocation(), Sound.ENTITY_IRONGOLEM_DEATH, 1000.0F, 0.0F);
                }

                LootManager loot = getManager().getLoot();

                WorldData data = getData();

                LootTier tier1 = loot.getLoot("Tier1");

                for (Block block : data.getCustomBlocks("Chests"))
                {
                    if (!loot.isLooted(block))
                        continue;

                    _looted++;
                    loot.fillWithLoot(block, tier1);
                }

                LootTier tier2 = loot.getLoot("Tier2");

                for (Block block : data.getCustomBlocks("Special Chests"))
                {
                    if (!loot.isLooted(block))
                        continue;

                    _looted++;
                    loot.fillWithLoot(block, tier2);
                }

                LootTier furnace = loot.getLoot("Furnace");

                for (Block block : data.getCustomBlocks("Furnaces"))
                {
                    if (!loot.isLooted(block))
                        continue;

                    loot.fillWithLoot(block, furnace);
                }

                Announce(C.Gold + "The chests has been refilled!");
                break;
            default:
                break;
            }
        }

        switch (timeTillDeathmatch)
        {
        case 300:
        case 180:
        case 120:
        case 60:
        case 30:
        case 15:
        case 10:
        case 5:
        case 4:
        case 3:
        case 2:
        case 1:
            Announce(C.Red + "The deathmatch will begin in " + UtilNumber.getTime(timeTillDeathmatch));
            break;
        case 0:

            for (Player player : UtilPlayer.getPlayers())
            {
                Stats.add(player, "Game." + getName() + ".Deathmatch");

                player.playSound(player.getEyeLocation(), Sound.ENTITY_WITHER_DEATH, 1000.0F, 0.0F);
            }

            Announce(C.Red + "Welcome to the deathmatch!");

            ArrayList<String> names = new ArrayList<String>();

            for (Player player : getPlayers(true))
                names.add(player.getName());

            Announce(C.Red + "Featuring the combatants: " + C.DRed + UtilString.join(names, C.Red + ", " + C.DRed) + "!");

            _frozen = true;

            GameTeam team = getTeams().get(0);

            for (Player player : getPlayers(true))
            {
                UtilPlayer.tele(player, team.getSpawn());

                addPoints(player, "Deathmatch", 25);
                player.sendMessage(C.Blue + "Given 25 points for making it to the deathmatch!");
            }

            for (Player player : getPlayers(false))
            {
                UtilPlayer.tele(player, team.getSpawn());
            }

            LootManager loot = getManager().getLoot();

            WorldData data = getData();

            LootTier tier = loot.getLoot("Deathmatch");

            for (Block block : data.getCustomBlocks("Chests"))
            {
                loot.fillWithLoot(block, tier);
            }

            for (Block block : data.getCustomBlocks("Special Chests"))
            {
                loot.fillWithLoot(block, tier);
            }

            getData().setBorderRadius(Integer.parseInt(getData().getCustom("Deathmatch").get(0)));
            break;
        default:
            break;
        }

        if (timeTillWarmup > 0 && timeTillWarmup < 15)
        {
            for (Player player : UtilPlayer.getPlayers())
            {
                player.playSound(player.getEyeLocation(), Sound.BLOCK_LEVER_CLICK, 1000.0F, 0.4F + (timeTillWarmup * 0.175F));
            }
        }

        if (timeTillWarmup > 0 && (timeTillWarmup == 10 || timeTillWarmup <= 5))
        {
            Announce(C.Red + "Warmup period ends in " + UtilNumber.getTime(timeTillWarmup) + "!");
        }
        else if (timeTillWarmup == 0)
        {
            for (Player player : UtilPlayer.getPlayers())
            {
                player.playSound(player.getEyeLocation(), Sound.ENTITY_ZOMBIE_ATTACK_DOOR_WOOD, 1000.0F, 0F);
            }

            _frozen = false;

            Announce(C.Red + "The deathmatch has begun!");
        }

        switch (timeTillEnd)
        {
        case 180:
        case 120:
        case 60:
        case 30:
        case 15:
        case 10:
        case 5:
        case 4:
        case 3:
        case 2:
        case 1:
            Announce(C.Gray + "The game will end in " + UtilNumber.getTime(timeTillEnd));
            break;
        case 0:
            ArrayList<Player> players = getPlayers(true);

            Collections.sort(players, new Comparator<Player>()
            {
                @Override
                public int compare(Player o1, Player o2)
                {
                    return Double.compare(o1.getHealth(), o2.getHealth());
                }
            });

            for (int i = 0; i < players.size() - 1; i++)
            {
                getManager().getDamage().newDamage(players.get(i), END_OF_GAME, 1);
            }

            break;
        default:
            break;
        }
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event)
    {
        if (!event.isSneaking())
            return;

        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        HashMap<Entity, Pair<UUID, String>> deadBodies = getDeadBodies();

        for (Arrow arrow : UtilLoc.getInRadius(player.getLocation(), 0.4, Arrow.class))
        {
            if (!deadBodies.containsKey(arrow))
                continue;

            Pair<UUID, String> pair = deadBodies.get(arrow);

            if (_teabagged.containsKey(pair.getKey()))
            {
                if (_teabagged.get(pair.getKey()).contains(player.getUniqueId()))
                {
                    continue;
                }
            }

            if (Currency.get(player, CurrencyType.POINT) < 50)
            {
                player.sendMessage(C.Red + "You don't have enough points to teabag");
            }

            Currency.add(player, CurrencyType.POINT, "Teabag", -50);

            if (!_teabagged.containsKey(pair.getKey()))
            {
                _teabagged.put(pair.getKey(), new ArrayList<UUID>());
            }

            _teabagged.get(pair.getKey()).add(player.getUniqueId());

            arrow.getWorld().playSound(arrow.getLocation(), Sound.ENTITY_DONKEY_ANGRY, 1.0F, 1.0F);

            Announce(C.Gold + player.getName() + " teabagged " + pair.getValue() + "'s dead body!");

            player.sendMessage(C.Yellow + "You paid 50 points for the privilege of teabagging!");

            Stats.add(player, "Game." + getName() + ".Teabags");
        }
    }

    @EventHandler
    public void onVechileEnter(VehicleEnterEvent event)
    {
        if (!_frozen)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onWarningJoin(PlayerJoinEvent event)
    {
        if (!isPreGame())
            return;

        event.getPlayer().sendMessage(UtilError.format("WARNING", "If you are found teaming, you will be banned for a week!"));
        event.getPlayer().sendMessage(C.Gray + "This is not a teambased game, your goal is to kill everyone else.");
    }

    @EventHandler
    public void onWin(WinEvent event)
    {
        for (UUID winner : event.getWinners())
        {
            Player player = Bukkit.getPlayer(winner);

            if (player == null)
                continue;

            getBountyManager().onWin(player);
        }
    }

    @Override
    public void setupScoreboards()
    {
        getManager().getScoreboard().discardScoreboards();

        FakeScoreboard main = getManager().getScoreboard().getMainScoreboard();

        main.setSidebarTitle(C.Gray + "Survivalgames");

        FakeScoreboard specs = getScoreboard().createScoreboard("Spectators", (player) -> getTeam(player) == null);

        main.createTeam("Spectators").setPrefix(C.Gray);

        main.addChild(specs);

        ArrayList<Player> players = getPlayers(true);

        for (Player player : players)
        {
            FakeScoreboard board = getScoreboard().createScoreboard("Player-" + player.getName(), (p) -> player == p);

            main.addChild(board);

            FakeTeam see = board.createTeam("See");
            FakeTeam invis = board.createTeam("Invis");
            invis.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

            for (Player p : players)
            {
                see.addPlayer(p);
            }
        }

        main.makeScore(DisplaySlot.BELOW_NAME, "Generic Player", C.Green + "Kill Worth", 0);

        for (Player player : getPlayers(true))
        {
            displayPoints(player, getBountyManager().getKillworth(player));
        }

        drawScoreboard();
    }

    @Override
    public void spawnFireworks()
    {
        ArrayList<Location> locs = getTeams().get(0).getSpawns();

        Location loc = locs.get(_fireworkSpawn++ % locs.size());

        UtilFirework.spawnRandomFirework(loc, getManager().getWin().getColor());
    }
}
