package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import com.google.common.base.Predicate;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.TeamDeathEvent;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.TeamGame;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.KillstreakManager;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitBerserker;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitBleeder;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitDemolitions;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitDwarf;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitExplosive;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitFrost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitInertia;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitJuggernaut;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitLongbow;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitMedic;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitNinja;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitPyro;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitRewind;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitShortbow;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitSkinner;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitSpy;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitTeleporter;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitTrooper;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitVampire;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitVenom;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWarper;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;

public class SearchAndDestroy extends TeamGame
{
    private ArrayList<TeamBomb> _bombs = new ArrayList<TeamBomb>();
    private long _deathTimer = 600000;
    private KillstreakManager _killstreakManager;
    private long _lastDeath;
    private ArrayList<GameTeam> _lastManStanding = new ArrayList<GameTeam>();
    private int _poisonStage;
    private AttackType BOMB_EXPLODE = new AttackType("Bomb Exploded", "%Killed% was caught in the explosion of their bomb")
            .setInstantDeath();
    private AttackType END_OF_GAME = new AttackType("End of Game", "%Killed% was unable of escape the strings of time")
            .setIgnoreArmor().setNoKnockback();

    public SearchAndDestroy(ArcadeManager arcadeManager)
    {
        super(arcadeManager, ServerType.SearchAndDestroy);

        setKits(new Kit[]
            {
                    new KitTrooper(), new KitBerserker(), new KitDemolitions(), new KitDwarf(), new KitExplosive(),
                    new KitGhost(getManager().getPlugin()), new KitJuggernaut(getManager().getPlugin()), new KitLongbow(),
                    new KitMedic(), new KitPyro(), new KitRewind(), new KitShortbow(), new KitSpy(), new KitTeleporter(),
                    new KitVampire(), new KitVenom(), new KitWarper(), new KitNinja(), new KitFrost(), new KitSkinner(),
                    new KitWraith(getManager().getPlugin()), new KitBleeder()
            });

        _killstreakManager = new KillstreakManager(this);

        setOption(GameOption.STEAK_HEALTH, 8D);
        setOption(GameOption.HATS, true);
        setOption(GameOption.INFORM_KILL_ASSIST, true);
        setOption(GameOption.TABLIST_KILLS, true);
        setOption(GameOption.COLOR_CHAT_NAMES, false);
    }

    private void checkLastMan()
    {
        new BukkitRunnable()
        {
            public void run()
            {
                if (!isLive())
                    return;

                for (GameTeam team : getTeams())
                {
                    if (_lastManStanding.contains(team))
                        continue;

                    ArrayList<Player> players = team.getPlayers(true);

                    if (players.size() != 1)
                        continue;

                    _lastManStanding.add(team);

                    Player player = players.get(0);

                    for (ItemStack item : UtilInv.getNonClonedInventory(player))
                    {
                        if (item.getType() != Material.BLAZE_POWDER)
                            continue;

                        FuseType.BOMB_ARMING.setLevel(item, 10);
                    }

                    player.updateInventory();

                    Announce(team.getColoring() + player.getName() + " is last man standing!");
                }
            }
        }.runTask(getManager().getPlugin());
    }

    public void drawScoreboard()
    {
        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        ArrayList<GameTeam> teams = getTeams(true);

        Collections.sort(teams, GameTeam.COMPARE_PLAYERS);

        ArrayList<String> lines = new ArrayList<String>();

        Iterator<GameTeam> itel = teams.iterator();
        Iterator<TeamBomb> bombItel = getBombs().stream().filter((bomb) -> !bomb.isOwned()).iterator();

        while (itel.hasNext())
        {
            GameTeam team = itel.next();

            lines.add(team.getColoring() + C.Bold + team.getName());

            lines.add(team.getPlayers(true).size() + " alive");

            for (TeamBomb bomb : getBombs())
            {
                if (!bomb.isOwned() || bomb.getTeam() != team)
                {
                    continue;
                }

                if (bomb.isArmed())
                {
                    String disarm = bomb.getDisarmStatus();

                    if (disarm == null)
                    {
                        lines.add(team.getColoring() + "Bomb " + C.Bold + bomb.getTimeLeft());
                    }
                    else
                    {
                        lines.add(team.getColoring() + disarm + " " + team.getColoring() + C.Bold + bomb.getTimeLeft());
                    }
                }
                else
                {
                    lines.add("Bomb is Safe");
                }
            }

            if (itel.hasNext() || bombItel.hasNext())
                lines.add("");
        }

        while (bombItel.hasNext())
        {
            TeamBomb bomb = bombItel.next();

            if (!bomb.isArmed())
                lines.add(C.Bold + "Nuke");
            else
                lines.add(bomb.getTeam().getColoring() + C.Bold + "Nuke " + bomb.getTimeLeft());
        }

        if (lines.size() > 15)
        {
            while (lines.contains(""))
                lines.remove("");
        }

        if (!lines.isEmpty() && lines.get(lines.size() - 1).equals(""))
            lines.remove(lines.size() - 1);

        board.setSidebar(lines);
    }

    public ArrayList<TeamBomb> getBombs()
    {
        return _bombs;
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
        return 3;
    }

    public boolean isEndGame()
    {
        return UtilTime.elasped(getStateChanged(), _deathTimer + 60000);
    }

    @EventHandler
    public void onBombInteract(PlayerInteractEntityEvent event)
    {
        if (!isLive())
            return;

        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        for (TeamBomb bomb : getBombs())
        {
            if (!bomb.isArmed())
                continue;

            if (bomb.getBomb() != event.getRightClicked())
            {
                continue;
            }

            bomb.onInteract(player, UtilInv.getHolding(player, Material.BLAZE_POWDER));
        }
    }

    @EventHandler
    public void onBombInteract(PlayerInteractEvent event)
    {
        if (!isLive())
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        for (TeamBomb bomb : getBombs())
        {
            if (!bomb.getBlock().equals(event.getClickedBlock()))
            {
                continue;
            }

            bomb.onInteract(player, event.getItem());
        }
    }

    @EventHandler
    public void onBombTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        Collections.sort(getBombs(), new Comparator<TeamBomb>()
        {

            @Override
            public int compare(TeamBomb o1, TeamBomb o2)
            {
                return Long.compare(o1.getFused(), o2.getFused());
            }
        });

        ArrayList<TeamBomb> toCheck = new ArrayList<TeamBomb>(getBombs());

        while (!toCheck.isEmpty())
        {
            TeamBomb bomb = toCheck.remove(0);

            if (!getBombs().contains(bomb))
                continue;

            bomb.tickBomb();

            if (!bomb.isArmed())
                continue;

            if (bomb.getTimeLeft() > 0)
                continue;

            onExplode(bomb);
        }
    }

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        checkLastMan();

        _lastDeath = System.currentTimeMillis();
    }

    public void onExplode(TeamBomb teamBomb)
    {
        Location loc = teamBomb.getBomb().getLocation();

        loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 10000, 0);
        UtilParticle.playParticle(ParticleType.HUGE_EXPLOSION, loc, ViewDist.LONGER);

        Iterator<TeamBomb> itel = getBombs().iterator();

        while (itel.hasNext())
        {
            TeamBomb bomb = itel.next();

            if (bomb.getTeam() != teamBomb.getTeam())
                continue;

            if (!bomb.isOwned() && teamBomb.isOwned())
            {
                bomb.restore();
                continue;
            }

            bomb.remove();
            itel.remove();
        }

        for (Block block : UtilBlock.getBlocks(teamBomb.getBlock().getLocation().add(0.5, 0.5, 0.5), 6))
        {
            Material mat = block.getType();

            if (mat == Material.MONSTER_EGGS)
                continue;

            if (UtilBlock.solid(mat))
                block.setType(Material.COAL_BLOCK);
            else if (mat.name().contains("SLAB") || mat.name().contains("STEP"))
            {
                block.setType(Material.STEP);
            }
        }

        if (teamBomb.isOwned())
            Announce(teamBomb.getTeam().getColoring() + teamBomb.getTeam().getName() + "'s " + C.Gold + "bomb exploded!");
        else
            Announce(teamBomb.getTeam().getColoring() + teamBomb.getTeam().getName() + "'s " + C.Gold
                    + "nuke exploded! Everyone but them annihilated!");

        setOption(GameOption.DEATH_MESSAGES, false);

        for (Player player : (teamBomb.isOwned() ? teamBomb.getTeam().getPlayers(true)
                : getPlayers(true).stream().filter((player) -> !teamBomb.getTeam().isInTeam(player))
                        .collect(Collectors.toList())))
        {
            getDamageManager().newDamage(player, BOMB_EXPLODE, 0);
        }

        setOption(GameOption.DEATH_MESSAGES, true);
    }

    @EventHandler
    public void onGameEnd(GameStateEvent event)
    {
        if (event.getState() != GameState.End && event.getState() != GameState.Dead)
            return;

        _killstreakManager.unregister();
    }

    @EventHandler
    public void onGameStart(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        _lastDeath = System.currentTimeMillis();

        if (UtilMath.r(5) == 0)
            setOption(GameOption.TIME_OF_WORLD, 15000L);

        for (Player player : UtilPlayer.getPlayers())
        {
            GameTeam team = getTeam(player);

            player.sendMessage(C.Gold + "You are in " + team.getColoring() + team.getName());
        }

        for (GameTeam team : getTeams())
        {
            Announce(C.Gold + "There are " + team.getPlayers().size() + " player" + (team.getPlayers().size() == 1 ? "" : "s")
                    + " in " + team.getColoring() + team.getName());
        }
    }

    @EventHandler
    public void onKillstreak(KillstreakEvent killstreakEvent)
    {
        _killstreakManager.onKillstreak(killstreakEvent);
    }

    @EventHandler
    public void onPoison(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
        {
            return;
        }

        if (!isLive())
        {
            return;
        }

        if (_poisonStage == 0 && !UtilTime.elasped(getStateChanged(), _deathTimer))
        {
            if (System.currentTimeMillis() - _lastDeath > 90000)
            {
                _deathTimer -= 3000;
            }
        }

        if (_poisonStage == 0 && UtilTime.elasped(getStateChanged(), _deathTimer))
        {
            _poisonStage++;

            Announce(C.Red + "One minute until players start dying!");

            _deathTimer = System.currentTimeMillis() - getStateChanged();
        }
        else if (_poisonStage >= 1 && isEndGame())
        {
            if (_poisonStage == 1)
            {
                _poisonStage++;

                Announce(C.Red + "Don't say I didn't warn you!");

                for (Player player : getPlayers(true))
                {
                    UtilInv.remove(player, Material.GOLDEN_APPLE);
                    UtilInv.remove(player, Material.COOKED_BEEF);
                }
            }

            ArrayList<GameTeam> teams = new ArrayList<GameTeam>(getTeams());

            Collections.shuffle(teams);

            for (GameTeam team : teams)
            {
                for (Player player : team.getPlayers(true))
                {
                    double most = 0;

                    double extraDamage = Math.max(0, ((System.currentTimeMillis() - getStateChanged()) - (60000 * 11)) / 120000D)
                            + 0.75;

                    for (TeamBomb bomb : getBombs())
                    {
                        if (bomb.getTeam() != team || !bomb.isOwned())
                        {
                            continue;
                        }

                        double healthToTake = extraDamage;

                        if (bomb.getBlock().getLocation().add(0.5, 0.5, 0.5).distance(player.getLocation()) < 15)
                        {
                            healthToTake += UtilMath.rr(1);
                        }
                        else
                        {
                            healthToTake += UtilMath.rr(0.5);
                        }

                        if (healthToTake <= most)
                            continue;

                        most = healthToTake;
                    }

                    most *= Math.max(player.getMaxHealth(), 20) / 20;

                    getManager().getDamage().newDamage(player, END_OF_GAME, most);
                }
            }
        }
    }

    @EventHandler
    public void onScoreboardDraw(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (getState().isPreGame())
            return;

        drawScoreboard();
    }

    @EventHandler
    public void onTeamDeath(TeamDeathEvent event)
    {
        GameTeam team = event.getTeam();
        Announce(team.getColoring() + team.getName() + C.Gold + " was defeated!");

        Iterator<TeamBomb> itel = getBombs().iterator();

        while (itel.hasNext())
        {
            TeamBomb bomb = itel.next();

            if (bomb.getTeam() != event.getTeam())
                continue;

            if (!bomb.isOwned())
            {
                bomb.restore();
                continue;
            }

            bomb.remove();
            itel.remove();
        }
    }

    @EventHandler
    public void onTeamsCreation(GameStateEvent event)
    {
        if (event.getState() != GameState.Live)
            return;

        _killstreakManager.register();
        checkLastMan();
    }

    @EventHandler
    public void registerBombs(GameStateEvent event)
    {
        if (event.getState() != GameState.MapLoaded)
            return;

        for (TeamSettings settings : TeamSettings.values())
        {
            ArrayList<Block> bombs = getData().getCustomBlocks(settings.name() + " Bombs");

            for (Block b : bombs)
            {
                TeamBomb bomb = new TeamBomb(this, getTeam(settings), b);

                _bombs.add(bomb);

                bomb.drawHologram();
            }
        }
    }

    public void sendTimeProgress(Player player)
    {
        player.sendMessage(C.Gray + "The game has been in progress for " + UtilNumber.getTime(getGameTime(), TimeUnit.SECONDS));

        if (!isEndGame())
        {
            player.sendMessage(C.Gray + "Poison will begin in " + UtilNumber
                    .getTime((getStateChanged() + _deathTimer + 60000) - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void setupScoreboards()
    {
        getScoreboard().discardScoreboards();

        FakeScoreboard main = getScoreboard().getMainScoreboard();

        FakeScoreboard specs = getScoreboard().createScoreboard("Spectators", (player) -> getTeam(player) == null);

        for (GameTeam observerTeam : getTeams())
        {
            FakeTeam realTeam = specs.createTeam(observerTeam.getName());
            realTeam.setPrefix(observerTeam.getColoring());

            for (Player player : observerTeam.getPlayers())
            {
                realTeam.addPlayer(player);
            }
        }

        main.addChild(specs);

        FakeTeam specTeam = specs.createTeam("Spectators");
        specTeam.setPrefix(C.Gray);

        // Create all the teams
        for (GameTeam observerTeam : getTeams())
        {
            FakeScoreboard board = getScoreboard().createScoreboard(observerTeam.getName(), new Predicate<Player>()
            {
                @Override
                public boolean apply(Player input)
                {
                    return observerTeam.isInTeam(input);
                }
            });

            main.addChild(board);

            ArrayList<Player> spies = new ArrayList<Player>();

            for (GameTeam renderedTeam : getTeams())
            {
                FakeTeam realTeam = board.createTeam(renderedTeam.getName());
                FakeTeam ghostTeam = board.createTeam(renderedTeam.getName() + "Invis");
                FakeTeam spyTeam = board.createTeam(renderedTeam.getName() + "Spy");

                realTeam.setPrefix(renderedTeam.getColoring());
                spyTeam.setPrefix(renderedTeam.getColoring());
                ghostTeam.setPrefix(renderedTeam.getColoring());

                ghostTeam.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);

                realTeam.setSeeInvisiblePlayers(true);

                for (Player player : renderedTeam.getPlayers())
                {
                    if (observerTeam == renderedTeam)
                    {
                        realTeam.addPlayer(player);
                        continue;
                    }

                    Kit kit = getKit(player);

                    if (kit instanceof KitSpy)
                    {
                        spies.add(player);
                    }
                    else if (kit instanceof KitGhost || kit instanceof KitWraith)
                    {
                        ghostTeam.addPlayer(player);
                    }
                    else
                    {
                        realTeam.addPlayer(player);
                    }

                    realTeam.addPlayer(player.getName());
                }
            }

            for (Player player : spies)
            {
                board.getTeam(observerTeam.getName() + "Spy").addPlayer(player);
            }
        }

        main.setSidebarTitle(C.Gold + "Teams");
    }
}
