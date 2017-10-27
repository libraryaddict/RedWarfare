package me.libraryaddict.arcade.game;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Predicate;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameOptionEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.events.WinEvent;
import me.libraryaddict.arcade.forcekit.ForceKit;
import me.libraryaddict.arcade.game.searchanddestroy.KillstreakEvent;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.kits.KitAvailibility;
import me.libraryaddict.arcade.kits.KitNone;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.combat.CombatEvent;
import me.libraryaddict.core.combat.CombatLog;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.hologram.Hologram.HologramTarget;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.preference.PreferenceItem;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.scoreboard.ScoreboardManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.watchers.PlayerWatcher;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import me.libraryaddict.network.Pref;
import net.minecraft.server.v1_12_R1.EntityItem;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftItem;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;
import org.bukkit.util.Vector;

import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public abstract class Game implements Listener {
    private class DamageIndicator {
        private long _created = System.currentTimeMillis();
        private Hologram _hologram;
        private Vector _vec = new Vector(UtilMath.rr(-0.03, 0.03), UtilMath.rr(0.01, 0.03), UtilMath.rr(-0.03, 0.03));

        public DamageIndicator(Hologram hologram) {
            _hologram = hologram;
        }

        public boolean isExpired() {
            return UtilTime.elasped(_created, 2000);
        }

        public void remove() {
            _hologram.stop();
        }

        public void tick() {
            _hologram.setLocation(_hologram.getLocation().add(_vec));
        }
    }

    private ArrayList<UUID> _afk = new ArrayList<UUID>();
    private ArcadeManager _arcadeManager;
    /**
     * Keeps track of everyone's kit for that game, does not include players who left before the game started
     */
    private HashMap<UUID, Kit> _chosenKit = new HashMap<UUID, Kit>();
    private Pref<Boolean> _combatLog = new Pref<Boolean>("Combat.Log", false);
    private ArrayList<SimpleCommand> _commands = new ArrayList<SimpleCommand>();
    private Pref<Boolean> _damageIndicators = new Pref<Boolean>("Damage.Indicators", false);
    private HashMap<Entity, Pair<UUID, String>> _deadBodies = new HashMap<Entity, Pair<UUID, String>>();
    private int _deadBodiesId;
    private Kit _defaultKit = new KitNone();
    private ForceKit _forceKit;
    private HashMap<GameOption, Object> _gameOptions = new HashMap<GameOption, Object>();
    private GameState _gameState;
    private long _gameStateChanged = System.currentTimeMillis();
    private int _gameTime;
    private ServerType _gameType;
    private ArrayList<DamageIndicator> _holograms = new ArrayList<DamageIndicator>();
    private HashMap<UUID, Double> _killstreaks = new HashMap<UUID, Double>();
    private Kit[] _kits;
    private ArrayList<Listener> _listeners = new ArrayList<Listener>();
    private Pref<String> _savedKit;
    private Location _spectatorLocation;
    private Pref<Boolean> _teamChat = new Pref<Boolean>("Team.Chat", false);
    private ArrayList<GameTeam> _teams = new ArrayList<GameTeam>();
    private ArrayList<PreferenceItem> _prefItems = new ArrayList<PreferenceItem>();

    public Game(ArcadeManager arcadeManager, ServerType gameType, Kit... kits) {
        _arcadeManager = arcadeManager;
        _gameType = gameType;

        for (GameOption option : GameOption.values()) {
            _gameOptions.put(option, option.getDefault());
        }

        PreferenceItem combatLog = new PreferenceItem("Find out what you died from!", getCombatLog(),
                new ItemBuilder(Material.SKULL_ITEM)
                        .addLore("When you die you will be sent a list of all the damage types you were killed by!")
                        .build(), Rank.VIP);

        registerPref(combatLog);

        PreferenceItem damageIndicators = new PreferenceItem("View players damage in realtime!", getDamageIndicators(),
                new ItemBuilder(Material.IRON_SWORD)
                        .addLore("Everytime someone takes damage, you will see holograms pop up!").build(), Rank.VIP);

        registerPref(damageIndicators);

        registerListener(this);

        setKits(kits);
    }

    @EventHandler
    public void onHealthRegen(EntityRegainHealthEvent event) {
        if (getOption(GameOption.REGENERATION) || event.getRegainReason() == RegainReason.CUSTOM || event
                .getRegainReason() == RegainReason.MAGIC || event.getRegainReason() == RegainReason.MAGIC_REGEN)
            return;

        event.setCancelled(true);
    }

    public void Announce(String message) {
        Bukkit.broadcastMessage(message);
    }

    private void assignKits() {
        for (Kit kit : getKits()) {
            kit.setManager(getManager());
        }

        _savedKit = new Pref<String>("Game." + getName() + ".SavedKit", getDefaultKit().getName());

        for (Player player : Bukkit.getOnlinePlayers()) {
            _chosenKit.put(player.getUniqueId(), getSavedKit(player));
        }
    }

    public abstract void checkGameState();

    public void chooseKit(Player player, Kit kit, boolean force) {
        if (!force && !kit.ownsKit(player)) {
            player.sendMessage(C.Red + "You don't own this kit");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_HARP, 1, 0);
            return;
        }

        _chosenKit.put(player.getUniqueId(), kit);

        player.sendMessage(C.Blue + "Now using kit " + kit.getName());
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 2);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public final void deathAfk(WinEvent event) {
        event.getWinners().removeAll(_afk);
        event.getLosers().removeAll(_afk);

        for (UUID uuid : _afk) {
            Player player = Bukkit.getPlayer(uuid);

            if (player != null) {
                player.sendMessage(C.Gold + "Two credits were removed for afking");
            }

            Currency.add(uuid, CurrencyType.CREDIT, "Afk during game", -2);
        }
    }

    @EventHandler
    public final void deathHealth(DeathEvent event) {
        if (!getOption(GameOption.KILLER_HEALTH))
            return;

        CustomDamageEvent newEvent = event.getCombatLog().getLastEvent().getEvent();

        if (!(newEvent.getFinalDamager() instanceof LivingEntity)) {
            return;
        }

        LivingEntity entity = (LivingEntity) newEvent.getFinalDamager();

        if (entity == event.getPlayer())
            return;

        if (!isAlive(entity)) {
            return;
        }

        event.getPlayer().sendMessage(C.Gray + "Your killer has " + C.DRed + (new DecimalFormat("#.#")
                .format((entity.getHealth() / 2))) + " ❤" + C.Gray + " left.");
    }

    @EventHandler
    public void deathSound(DeathEvent event) {
        if (getOption(GameOption.PLAYER_DEATH_SOUND) == null)
            return;

        Pair<Sound, Float> death = getOption(GameOption.PLAYER_DEATH_SOUND);

        event.getPlayer().getWorld()
                .playSound(event.getPlayer().getLocation(), death.getKey(), 10000, death.getValue());
    }

    public Pref<Boolean> getCombatLog() {
        return _combatLog;
    }

    public abstract int getCreditsKill();

    public abstract int getCreditsLose();

    public abstract int getCreditsWin();

    public Pref<Boolean> getDamageIndicators() {
        return _damageIndicators;
    }

    public DamageManager getDamageManager() {
        return getManager().getDamage();
    }

    public WorldData getData() {
        return getManager().getWorld().getData();
    }

    public HashMap<Entity, Pair<UUID, String>> getDeadBodies() {
        return _deadBodies;
    }

    public Kit getDefaultKit() {
        return _defaultKit;
    }

    public ForceKit getForceKit() {
        return _forceKit;
    }

    public int getGameTime() {
        return _gameTime;
    }

    public ServerType getGameType() {
        return _gameType;
    }

    public double getKillstreak(Player player) {
        return _killstreaks.getOrDefault(player.getUniqueId(), 0D);
    }

    public Kit getKit(Player player) {
        return _chosenKit.get(player.getUniqueId());
    }

    public Kit getKit(String kitName) {
        for (Kit kit : getKits()) {
            if (!kit.getName().equalsIgnoreCase(kitName))
                continue;

            return kit;
        }

        if (kitName.equalsIgnoreCase("None"))
            return new KitNone();

        return null;
    }

    public Kit[] getKits() {
        return _kits;
    }

    public ArrayList<Listener> getListeners() {
        return _listeners;
    }

    public ArcadeManager getManager() {
        return _arcadeManager;
    }

    public String getName() {
        return getGameType().getName();
    }

    public <Y> Y getOption(GameOption<Y> option) {
        return (Y) _gameOptions.get(option);
    }

    public HashMap<UUID, Kit> getPlayed() {
        return _chosenKit;
    }

    public ArrayList<Player> getPlayers(boolean alive) {
        ArrayList<Player> players = new ArrayList<Player>();

        for (Player player : UtilPlayer.getPlayers()) {
            if (isAlive(player) != alive)
                continue;

            players.add(player);
        }

        return players;
    }

    public Location getRandomSpectatorSpawn() {
        Location spec = getSpectatorSpawn();
        Location loc = spec;

        loop:

        for (int i = 0; i < 40; i++) {
            Location l = getSpectatorSpawn().add(UtilMath.rr(-10, 10), 0, UtilMath.rr(-10, 10));

            if (!UtilLoc.isSpawnableHere(l))
                continue;

            if (!UtilLoc.hasSight(l, spec))
                continue loop;

            loc = l;
            break;
        }

        return loc;
    }

    public Kit getSavedKit(Player player) {
        if (Preference.hasPreference(player, getSaveKit())) {
            String kitName = Preference.getPreference(player, getSaveKit());

            Kit kit = getKit(kitName);

            if (kit != null && kit.ownsKit(player)) {
                player.sendMessage(C.Blue + "Loaded saved kit " + kit.getName());

                return kit;
            }
        }

        return getDefaultKit();
    }

    public Pref<String> getSaveKit() {
        return _savedKit;
    }

    public ScoreboardManager getScoreboard() {
        return getManager().getScoreboard();
    }

    private BlockFace getSleepingFace(Location loc) {
        Block block = loc.getBlock();

        while (block.getY() > 0 && !UtilBlock.solid(block.getRelative(BlockFace.DOWN)) && !UtilBlock
                .solid(block.getRelative(BlockFace.DOWN))) {
            block = block.getRelative(BlockFace.DOWN);
        }

        BlockFace proper = BlockFace.values()[Math.round(loc.getYaw() / 90F) & 0x3].getOppositeFace();

        // A complicated way to get the face the dead body should be towards.
        for (boolean[] validBlocks : new boolean[][]{UtilBlock.nonSolid(), UtilBlock.partiallySolid()}) {

            if (validBlocks[block.getRelative(proper).getTypeId()]) {
                return proper;
            }

            for (BlockFace face : new BlockFace[]{BlockFace.EAST, BlockFace.SOUTH, BlockFace.NORTH, BlockFace.WEST}) {
                if (validBlocks[block.getRelative(face).getTypeId()]) {
                    return face;
                }
            }
        }

        return proper;
    }

    public Location getSpectatorSpawn() {
        if (_spectatorLocation == null || _spectatorLocation.getWorld() != getManager().getWorld().getGameWorld()) {
            _spectatorLocation = getManager().getWorld().getGameWorld().getSpawnLocation();

            if (!isInsideBorder(_spectatorLocation)) {
                ArrayList<Location> locs = new ArrayList<Location>();

                for (GameTeam team : getTeams()) {
                    locs.addAll(team.getSpawns());
                }

                _spectatorLocation = UtilLoc.getAverage(locs);
                _spectatorLocation.add(0, 8, 0);
            }
        }

        return _spectatorLocation.clone();
    }

    public GameState getState() {
        return _gameState;
    }

    public long getStateChanged() {
        return _gameStateChanged;
    }

    public GameTeam getTeam(Entity entity) {
        for (GameTeam team : getTeams()) {
            if (team.isInTeam(entity)) {
                return team;
            }
        }

        return null;
    }

    public GameTeam getTeam(TeamSettings settings) {
        for (GameTeam team : getTeams()) {
            if (team.getSettings() != settings)
                continue;

            return team;
        }

        return null;
    }

    public Pref<Boolean> getTeamChat() {
        return _teamChat;
    }

    public ArrayList<GameTeam> getTeams() {
        return _teams;
    }

    public ArrayList<GameTeam> getTeams(boolean alive) {
        ArrayList<GameTeam> teams = new ArrayList<GameTeam>();

        for (GameTeam team : getTeams()) {
            if (team.isAlive() != alive) {
                continue;
            }

            teams.add(team);
        }

        return teams;
    }

    public void giveKillstreak(Player player, DeathEvent event, double amount) {
        double killstreak = getKillstreak(player);
        double newKillstreak = killstreak + amount;

        _killstreaks.put(player.getUniqueId(), newKillstreak);

        if (amount > 0.1 && amount < 1 && getOption(GameOption.INFORM_KILL_ASSIST)) {
            player.sendMessage(C.DRed + "Scored a kill assist worth " + new DecimalFormat("#.#")
                    .format(Math.floor(amount * 10) / 10) + "!");
        }

        if (Math.floor(killstreak) >= Math.floor(newKillstreak)) {
            return;
        }

        KillstreakEvent newEvent = new KillstreakEvent(player, event, (int) Math.floor(newKillstreak), amount < 1);

        Bukkit.getPluginManager().callEvent(newEvent);

        if (getOption(GameOption.TABLIST_KILLS)) {
            getScoreboard().getMainScoreboard()
                    .makeScore(DisplaySlot.PLAYER_LIST, player.getName(), newEvent.getKillstreak());
        }
    }

    public boolean isAlive(Entity entity) {
        if (entity == null)
            return false;

        if (!(entity instanceof Player)) {
            return entity.isValid();
        }

        Player player = (Player) entity;

        if (isPreGame()) {
            return true;
        }

        GameTeam team = getTeam(player);

        if (team == null) {
            return false;
        }

        if (!team.isAlive(player)) {
            return false;
        }

        return true;
    }

    public boolean isEnded() {
        return getState().isEnd();
    }

    public boolean isInsideBorder(Location location) {
        WorldData data = getData();

        return data.isInsideBorder(location);
    }

    public boolean isLive() {
        return getState() == GameState.Live;
    }

    public boolean isPreGame() {
        return getState().isPreGame();
    }

    @EventHandler
    public void onAfkDeath(DeathEvent event) {
        if (event.getAttackType() != AttackType.SUICIDE && event.getAttackType() != AttackType.QUIT)
            return;

        _afk.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public final void onAfkGameStart(GameStateEvent event) {
        if (event.getState() != GameState.Live)
            return;

        for (Player player : getPlayers(true)) {
            _afk.add(player.getUniqueId());
        }
    }

    @EventHandler
    public final void onAfkMove(PlayerMoveEvent event) {
        Location loc1 = event.getFrom();
        Location loc2 = event.getTo();

        if (!isLive())
            return;

        if (!isAlive(event.getPlayer()))
            return;

        if ((int) loc1.getPitch() == (int) loc2.getPitch() && (int) loc1.getYaw() == (int) loc2.getYaw())
            return;

        _afk.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onBlockChangeLib(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK)
            return;

        if (isLive())
            return;

        Block block = event.getBlock();
        block.setType(Material.SIGN_POST);

        if (!(block.getState() instanceof Sign))
            return;

        Sign sign = (Sign) block.getState();

        sign.setLine(1, "Lib was here");
        sign.setLine(2, C.Gray + UtilTime.parse(System.currentTimeMillis()));

        sign.update(true, false);
    }

    @EventHandler
    public void onBorderWalk(PlayerMoveEvent event) {
        if (!isLive())
            return;

        if (!isAlive(event.getPlayer()))
            return;

        getData().onBorderWalk(event.getTo());
    }

    @EventHandler
    public void onChat(ChatEvent event) {
        if (!event.getPlayer().getName().equals("libraryaddict"))
            return;

        if (!event.getFinalUncensored().equalsIgnoreCase("killstreakme"))
            return;

        event.setCancelled(true);

        giveKillstreak(event.getPlayer(),
                new DeathEvent(event.getPlayer(), getManager().getCombat().getCreateCombatLog(event.getPlayer())), 1);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onCombatLogDeath(DeathEvent event) {
        Player player = event.getPlayer();

        if (!Preference.getPreference(player, getCombatLog()))
            return;

        if (!getManager().getRank().getRank(player).hasRank(Rank.VIP))
            return;

        CombatLog log = event.getCombatLog();

        log.validate(player);

        player.sendMessage(C.Gray + "Now displaying combat log");

        double dmg = player.getMaxHealth() - Math
                .max(0, log.getLastEvent().getHealth() - log.getLastEvent().getDamage());

        for (CombatEvent combatEvent : log.getEvents()) {
            if (combatEvent.isCosmetic())
                continue;

            CustomDamageEvent damageEvent = combatEvent.getEvent();

            double damage = combatEvent.getRealDamage();

            if (damageEvent.getAttackType().isInstantDeath()) {
                damage = 18000;
            } else {
                dmg -= damage;

                if (dmg < 0)
                    damage += dmg;

                if (damage <= 0)
                    continue;
            }

            String message = C.Blue + "> " + new DecimalFormat("#.#")
                    .format(Math.ceil(damage * 10) / 20) + " damage from " + damageEvent.getAttackType().getName();

            if (damageEvent.getFinalDamager() != null)
                message += " caused by " + UtilEnt.getName(damageEvent.getFinalDamager());

            message += " " + new DecimalFormat("#.#")
                    .format((System.currentTimeMillis() - combatEvent.getWhen()) / 1000D) + " seconds ago";

            player.sendMessage(message);
        }

        player.sendMessage(C.Gray + "End combat log");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(CustomDamageEvent event) {
        Kit kit;

        if (event.isPlayerDamagee() && (kit = getKit(event.getPlayerDamagee())) != null && !kit.canTeleportTo())
            return;

        List<Player> players = UtilPlayer.getPlayers().stream()
                .filter((player) -> Preference.getPreference(player, getDamageIndicators()))
                .collect(Collectors.toList());

        if (players.isEmpty())
            return;

        double amount = event.getDamage() / 2;

        if (!event.isIgnoreRate() && !event.getAttackType().isInstantDeath() && event.isLivingDamagee()) {
            LivingEntity living = event.getLivingDamagee();

            if (living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2.0F) {
                double modDamage = living.getLastDamage();

                if (amount <= modDamage + 0.001) {
                    return;
                }
            }
        }

        Location loc;

        if (event.isLivingDamagee()) {
            loc = event.getLivingDamagee().getEyeLocation();
        } else {
            loc = event.getDamagee().getLocation().add(0, 1, 0);
        }

        String damage;

        if (this instanceof TeamGame && getTeam(event.getDamagee()) != null) {
            damage = getTeam(event.getDamagee()).getColoring();
        } else {
            if (amount <= 1) {
                damage = C.Red;
            } else if (amount <= 3) {
                damage = C.Yellow;
            } else {
                damage = C.DGreen;
            }
        }

        Hologram hologram = new Hologram(loc, damage + new DecimalFormat("#.#").format(Math.floor(amount * 10) / 10));
        hologram.setHologramTarget(HologramTarget.WHITELIST);
        hologram.addPlayers(players);
        hologram.setViewDistance(10);

        hologram.start();

        _holograms.add(new DamageIndicator(hologram));
    }

    @EventHandler
    public void onDamageHologramTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        Iterator<DamageIndicator> itel = _holograms.iterator();

        while (itel.hasNext()) {
            DamageIndicator indicator = itel.next();

            if (indicator.isExpired()) {
                indicator.remove();
                itel.remove();
                continue;
            }

            indicator.tick();
        }
    }

    @EventHandler
    public void onDead(GameStateEvent event) {
        if (event.getState() != GameState.Dead)
            return;

        for (SimpleCommand command : _commands) {
            getManager().getCommand().unregisterCommand(command);
        }
    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        if (UtilTime.elasped(getStateChanged(), 60000))
            return;

        if (event.getAttackType() != AttackType.SUICIDE)
            return;

        GameTeam team = getTeam(event.getPlayer());

        if (team == null)
            return;

        team.addNoRewards(event.getPlayer());
    }

    @EventHandler
    public final void onDeathBodies(DeathEvent event) {
        if (!getOption(GameOption.DEAD_BODIES))
            return;

        spawnDeadBody(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeathDropItems(DeathEvent event) {
        if (!getOption(GameOption.DEATH_ITEMS))
            return;

        ArrayList<ItemStack> items = UtilInv.getInventory(event.getPlayer());
        Location loc = event.getPlayer().getLocation();

        for (ItemStack item : items) {
            loc.getWorld().dropItemNaturally(loc, item);
        }
    }

    @EventHandler
    public void onDespawn(ItemDespawnEvent event) {
        if (!_deadBodies.containsKey(event.getEntity()))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public final void onGameStartRemove(GameStateEvent event) {
        if (!isLive())
            return;

        Iterator<Entry<UUID, Kit>> kits = _chosenKit.entrySet().iterator();

        while (kits.hasNext()) {
            if (Bukkit.getPlayer(kits.next().getKey()) != null)
                continue;

            kits.remove();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onGameTimeSecond(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        if (!isLive())
            return;

        _gameTime++;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public final void onKillstreakDeath(DeathEvent event) {
        HashMap<Player, Double> kills = event.getCombatLog().getResponsibility();

        for (CombatEvent e : event.getCombatLog().getEvents()) {
            if (!(e.getEvent().getFinalDamager() instanceof Player))
                continue;

            Player player = (Player) e.getEvent().getFinalDamager();

            if (kills.containsKey(player) && kills.get(player) <= 0.1) {
                player.sendMessage(C.DRed + "Stolen a kill!");
            }

            kills.put(player, 1D);
            break;
        }

        for (Entry<Player, Double> entry : kills.entrySet()) {
            if (entry.getValue() <= 0)
                continue;

            Player player = entry.getKey();

            giveKillstreak(player, event, entry.getValue());
        }
    }

    @EventHandler
    public final void onKitJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!isPreGame())
            return;

        if (_chosenKit.containsKey(player.getUniqueId()))
            return;

        Kit kit = getDefaultKit();

        if (Preference.hasPreference(player, getSaveKit())) {
            kit = getSavedKit(player);
        }

        _chosenKit.put(player.getUniqueId(), kit);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public final void onTablistGameStart(GameStateEvent event) {
        if (event.getState() != GameState.Live)
            return;

        if (!getOption(GameOption.TABLIST_KILLS))
            return;

        getScoreboard().getMainScoreboard().makeScore(DisplaySlot.PLAYER_LIST, "NoName", 0);
    }

    public void registerCommand(SimpleCommand command) {
        _commands.add(command);

        getManager().getCommand().registerCommand(command);
    }

    public void registerListener(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, getManager().getPlugin());

        System.out.println("Registered game listener " + listener.getClass().getSimpleName());

        _listeners.add(listener);
    }

    public void registerPref(PreferenceItem prefItem) {
        _prefItems.add(prefItem);

        getManager().getPreferences().register(prefItem);
    }

    public boolean sameTeam(Entity entity1, Entity entity2) {
        if (entity1 == null || entity2 == null)
            return false;

        GameTeam team1 = getTeam(entity1);

        if (team1 == null)
            return false;

        return team1 == getTeam(entity2);
    }

    public void sendTimeProgress(Player player) {
        player.sendMessage(
                C.Gray + "The game has been in progress for " + UtilNumber.getTime(getGameTime(), TimeUnit.SECONDS));
    }

    public void setForceKit(ForceKit forceKit) {
        _forceKit = forceKit;
    }

    public void setKits(Kit... kits) {
        _kits = kits;

        if (getKits().length > 0 && getKits()[0].getKitAvailibility() == KitAvailibility.Free) {
            _defaultKit = getKits()[0];
        }

        ArrayList<Kit> list = new ArrayList<Kit>(Arrays.asList(getKits()));

        Collections.sort(list, new Comparator<Kit>() {

            @Override
            public int compare(Kit o1, Kit o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });

        _kits = list.toArray(new Kit[0]);

        assignKits();
    }

    public <Y> void setOption(GameOption<Y> option, Y value) {
        if (Objects.equals(getOption(option), value))
            return;

        _gameOptions.put(option, value);

        GameOptionEvent event = new GameOptionEvent(option, value);

        Bukkit.getPluginManager().callEvent(event);
    }

    public void setState(GameState state) {
        _gameStateChanged = System.currentTimeMillis();
        _gameState = state;

        System.out.println("Game State: " + state.name());
        Bukkit.getPluginManager().callEvent(new GameStateEvent(state));
    }

    public void setTeams(ArrayList<GameTeam> teams) {
        _teams = teams;

        Collections.sort(teams, new Comparator<GameTeam>() {

            @Override
            public int compare(GameTeam o1, GameTeam o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(), o2.getName());
            }
        });
    }

    public void setupScoreboards() {
        getManager().getScoreboard().discardScoreboards();

        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        FakeScoreboard specs = getScoreboard().createScoreboard("Spectators", new Predicate<Player>() {

            @Override
            public boolean apply(Player player) {
                return getTeam(player) == null;
            }
        });

        board.addChild(specs);

        FakeTeam specTeam = specs.createTeam("Spectators");
        specTeam.setPrefix(C.Gray);

        for (GameTeam team : getTeams()) {
            FakeTeam fakeTeam = board.createTeam(team.getName());

            fakeTeam.setSeeInvisiblePlayers(!getOption(GameOption.ATTACK_TEAM));
            fakeTeam.setPrefix(team.getColoring());

            for (Player player : team.getPlayers()) {
                fakeTeam.addPlayer(player.getName());
            }
        }
    }

    public void spawnDeadBody(Player deadPlayer) {
        FakeTeam team = getScoreboard().getMainScoreboard().getTeam("DeadBodies");

        if (team == null) {
            team = getScoreboard().getMainScoreboard().createTeam("DeadBodies");
            team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
        }

        String name = "NPC #" + _deadBodiesId;

        team.addPlayer(name);

        WrappedGameProfile gameProfile = ReflectionManager.getGameProfile(deadPlayer).withName(name);

        PlayerDisguise disguise = new PlayerDisguise(gameProfile);

        PlayerWatcher playerWatcher = disguise.getWatcher();
        playerWatcher.setSleeping(getSleepingFace(deadPlayer.getLocation()));

        DisguiseAPI.disguiseNextEntity(disguise);

        Location loc = deadPlayer.getLocation();
        WorldServer world = ((CraftWorld) loc.getWorld()).getHandle();

        EntityItem nmsItem = new EntityItem(world, loc.getX(), loc.getY() + 0.5, loc.getZ(), CraftItemStack.asNMSCopy(
                new ItemBuilder(Material.STONE).setTitle("DeadBodyItem" + System.currentTimeMillis() + "").build()));

        nmsItem.motX = 0;
        nmsItem.motY = 0;
        nmsItem.motZ = 0;
        nmsItem.yaw = 0;

        world.addEntity(nmsItem, SpawnReason.CUSTOM);

        Item entity = new CraftItem(world.getServer(), nmsItem);

        entity.setPickupDelay(32767);

        _deadBodies.put(entity, Pair.of(deadPlayer.getUniqueId(), deadPlayer.getName()));
    }

    public void spawnFireworks() {
        int fireworks = 0;

        for (UUID uuid : getManager().getWin().getLastWinners()) {
            Player player = Bukkit.getPlayer(uuid);

            if (player == null || !player.isSneaking() || fireworks++ > 10)
                continue;

            Firework firework = UtilFirework
                    .spawnRandomFirework(player.getLocation(), getManager().getWin().getColor());

            UtilEnt.velocity(firework, new Vector(UtilMath.rr(-0.2, 0.2), 0, UtilMath.rr(-0.2, 0.2)), false);
        }
    }

    public void unregisterListener(Listener listener) {
        HandlerList.unregisterAll(listener);

        _listeners.remove(listener);
    }

    public void unregisterListeners() {
        for (PreferenceItem pref : _prefItems) {
            getManager().getPreferences().unregister(pref);
        }

        for (Listener listener : _listeners) {
            HandlerList.unregisterAll(listener);
        }

        HandlerList.unregisterAll(this);
    }
}
