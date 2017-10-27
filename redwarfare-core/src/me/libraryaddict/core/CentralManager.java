package me.libraryaddict.core;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import me.libraryaddict.core.antiafk.AntiAfkManager;
import me.libraryaddict.core.bans.BanManager;
import me.libraryaddict.core.censor.CensorManager;
import me.libraryaddict.core.chat.ChatManager;
import me.libraryaddict.core.combat.CombatManager;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.cosmetics.CosmeticManager;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.DamageManager;
import me.libraryaddict.core.explosion.ExplosionManager;
import me.libraryaddict.core.fakeentity.FakeEntityManager;
import me.libraryaddict.core.hologram.HologramManager;
import me.libraryaddict.core.inventory.InventoryManager;
import me.libraryaddict.core.messaging.MessageManager;
import me.libraryaddict.core.player.PlayerDataManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.PreferenceManager;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.redeem.RedeemManager;
import me.libraryaddict.core.referal.ReferalManager;
import me.libraryaddict.core.scoreboard.ScoreboardManager;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.stats.StatsManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeManager;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.vote.VoteManager;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.mysql.MysqlManager;
import me.libraryaddict.redis.RedisManager;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.DedicatedServer;
import net.minecraft.server.v1_12_R1.EnumGamemode;
import net.minecraft.server.v1_12_R1.SoundEffect;
import net.minecraft.server.v1_12_R1.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.SpigotConfig;
import org.spigotmc.SpigotWorldConfig;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public abstract class CentralManager extends MiniPlugin {
    private static CentralManager _centralManager;

    public static CentralManager getManager() {
        return _centralManager;
    }

    private AntiAfkManager _antiAfk;
    private BanManager _banManager;
    private CensorManager _censorManager;
    private ChatManager _chatManager;
    private CombatManager _combatManager;
    private CommandManager _commandManager;
    private ConditionManager _conditionManager;
    private CosmeticManager _cosmeticsManager;
    private DamageManager _damageManager;
    private ExplosionManager _explosionManager;
    private FakeEntityManager _fakeEntity;
    private HashMap<Integer, Integer[]> _fakePigIds = new HashMap<Integer, Integer[]>();
    private HologramManager _hologramManager;
    private InventoryManager _inventoryManager;
    private MessageManager _messageManager;
    private MysqlManager _mysqlManager;
    private PlayerDataManager _playerDataManager;
    private PreferenceManager _preferenceManager;
    private RankManager _rankManager;
    private RedeemManager _redeemManager;
    private RedisManager _redisManager;
    private ReferalManager _referalManager;
    private ScoreboardManager _scoreboardManager;
    private ServerManager _serverManager;
    private StatsManager _statsManager;
    private TimeManager _timeManager;
    private VoteManager _voteManager;
    private boolean _disabledFakePlayers;

    public void setFakePlayersDisabled(boolean disablePlayers) {
        if (disablePlayers == _disabledFakePlayers)
            return;

        _disabledFakePlayers = disablePlayers;
    }

    public CentralManager(JavaPlugin plugin) {
        super(plugin, "Central Manager");

        _centralManager = this;

        UtilPlayer.init(plugin);

        _scoreboardManager = new ScoreboardManager(plugin);

        _redisManager = new RedisManager();
        _mysqlManager = new MysqlManager(2);

        _timeManager = new TimeManager(plugin);

        _commandManager = new CommandManager(plugin);

        _banManager = new BanManager(plugin, getCommand());
        _statsManager = new StatsManager(plugin, getCommand());

        _voteManager = new VoteManager(getCommand());
        _serverManager = new ServerManager(plugin, getCommand());
        _playerDataManager = new PlayerDataManager(plugin, getCommand(), getServer());

        _rankManager = new RankManager(plugin, getPlayerData(), getCommand());

        _censorManager = new CensorManager(plugin);

        _combatManager = new CombatManager(plugin);
        _conditionManager = new ConditionManager(plugin);

        _damageManager = new DamageManager(plugin, getCombat());
        _inventoryManager = new InventoryManager(plugin);
        _hologramManager = new HologramManager(plugin);
        _fakeEntity = new FakeEntityManager(plugin);
        _explosionManager = new ExplosionManager(plugin, getDamage());

        getCommand().setRankManager(getRank());

        _chatManager = new ChatManager(plugin, getRank(), getPlayerData(), getServer());

        _messageManager = new MessageManager(plugin, getCommand(), getCensor(), getRank(), getChat(), getPlayerData());
        _cosmeticsManager = new CosmeticManager(plugin, this);
        _preferenceManager = new PreferenceManager(plugin, getRank(), getCommand(), getMessage());
        _redeemManager = new RedeemManager(plugin, getCommand(), getRank());
        _referalManager = new ReferalManager(plugin, getCommand());

        _antiAfk = new AntiAfkManager(plugin, getRank());

        Bukkit.setSpawnRadius(0);

        DedicatedServer server = ((CraftServer) Bukkit.getServer()).getHandle().getServer();
        server.a("allow-nether", false);
        server.a();

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("bukkit.yml"));

            config.set("settings.allow-end", false);

            config.save(new File("bukkit.yml"));
        }
        catch (IOException e) {
            UtilError.handle(e);
        }

        SpigotConfig.disableStatSaving = true;

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        SoundEffect effect = (SoundEffect) event.getPacket().getModifier().read(0);
                        String sound = SoundEffect.a.b(effect).b();

                        if (sound.startsWith("entity.player.attack.") || sound.equals("item.armor.equip_generic")) {
                            event.setCancelled(true);
                        }
                    }
                });

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, ListenerPriority.LOWEST, PacketType.Play.Server.NAMED_ENTITY_SPAWN,
                        PacketType.Play.Client.USE_ENTITY) {
                    @Override
                    public void onPacketReceiving(PacketEvent event) {
                        if (_disabledFakePlayers)
                            return;

                        int id = event.getPacket().getIntegers().read(0);

                        for (Entry<Integer, Integer[]> entry : _fakePigIds.entrySet()) {
                            Integer[] ids = entry.getValue();

                            for (int i = 0; i < ids.length; i++) {
                                if (id != ids[i])
                                    continue;

                                event.getPacket().getIntegers().write(0, entry.getKey());
                                return;
                            }
                        }
                    }

                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (_disabledFakePlayers)
                            return;

                        PacketContainer spawnPlayer = event.getPacket();

                        int entityId = spawnPlayer.getIntegers().read(0);

                        Player viewed = UtilPlayer.getPlayer(entityId);

                        if (viewed == null)
                            return;

                        // if (event.getPlayer().getName().equals("libraryaddict"))
                        // System.out.println("1 Teams showing " + viewed.getName());

                        if (!getScoreboard().spawnBoundingBox(event.getPlayer(), viewed))
                            return;

                        StructureModifier<Double> doubles = spawnPlayer.getDoubles();

                        UtilPlayer.sendPacket(event.getPlayer(),
                                getFakePlayerSpawn(spawnPlayer.getUUIDs().read(0), entityId,
                                        new Vector(doubles.read(0), doubles.read(1), doubles.read(2))));
                    }
                });

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Server.REL_ENTITY_MOVE,
                        PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_TELEPORT) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (_disabledFakePlayers)
                            return;

                        PacketContainer packet = event.getPacket();

                        int id = packet.getIntegers().read(0);
                        Integer[] ids;

                        if ((ids = _fakePigIds.get(id)) == null)
                            return;

                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT) {
                            UtilPlayer.sendPacket(event.getPlayer(), writeDoubles(ids, packet));
                        } else {
                            PacketContainer toSend = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);

                            StructureModifier<Integer> pInts = packet.getIntegers();
                            StructureModifier<Integer> ints = toSend.getIntegers();

                            ints.write(1, pInts.read(1));
                            ints.write(2, pInts.read(2));
                            ints.write(3, pInts.read(3));

                            PacketContainer[] move = new PacketContainer[4];

                            for (int i = 0; i < 4; i++) {
                                move[i] = toSend.shallowClone();
                                move[i].getIntegers().write(0, ids[i]);
                            }

                            UtilPlayer.sendPacket(event.getPlayer(), move);
                        }
                    }
                });

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_DESTROY) {
                    @Override
                    public void onPacketSending(PacketEvent event) {
                        if (_disabledFakePlayers)
                            return;

                        int[] entityIds = event.getPacket().getIntegerArrays().read(0);
                        int length = entityIds.length;

                        for (int i = 0; i < length; i++) {
                            Integer[] ids;

                            if ((ids = _fakePigIds.get(entityIds[i])) == null)
                                continue;

                            entityIds = Arrays.copyOf(entityIds, entityIds.length + 4);

                            for (int b = 0; b < ids.length; b++) {
                                entityIds[(entityIds.length - 4) + b] = ids[b];
                            }
                        }

                        event.getPacket().getIntegerArrays().write(0, entityIds);
                    }
                });
    }

    @EventHandler
    public void onElytraBoost(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        for (Player player : UtilPlayer.getPlayers()) {
            if (!player.isSneaking() || !player.isGliding())
                continue;

            Vector vec = player.getLocation().getDirection().normalize().multiply(0.025);

            vec.add(player.getVelocity());

            player.setVelocity(vec);
        }
    }

    public AntiAfkManager getAntiAfk() {
        return _antiAfk;
    }

    public BanManager getBan() {
        return _banManager;
    }

    public CensorManager getCensor() {
        return _censorManager;
    }

    public ChatManager getChat() {
        return _chatManager;
    }

    public CombatManager getCombat() {
        return _combatManager;
    }

    public CommandManager getCommand() {
        return _commandManager;
    }

    public ConditionManager getCondition() {
        return _conditionManager;
    }

    public CosmeticManager getCosmetics() {
        return _cosmeticsManager;
    }

    public DamageManager getDamage() {
        return _damageManager;
    }

    public PacketContainer getDelete(int entityId) {
        PacketContainer delete = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        int[] array;

        if (_fakePigIds.containsKey(entityId)) {
            Integer[] integers = _fakePigIds.get(entityId);

            array = new int[integers.length];

            for (int a = 0; a < integers.length; a++) {
                array[a] = integers[a];
            }
        } else {
            array = new int[0];
        }

        delete.getIntegerArrays().write(0, array);

        return delete;
    }

    public ExplosionManager getExplosion() {
        return _explosionManager;
    }

    public FakeEntityManager getFakeEntity() {
        return _fakeEntity;
    }

    public PacketContainer[] getFakePlayerSpawn(UUID uuid, int entityId, Vector location) {
        PacketContainer spawnPig = new PacketContainer(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
        spawnPig.getUUIDs().write(0, uuid);

        StructureModifier<Double> doubles = spawnPig.getDoubles();

        doubles.write(0, location.getX());
        doubles.write(1, location.getY());
        doubles.write(2, location.getZ());

        WrappedDataWatcher wrapped = new WrappedDataWatcher();

        wrapped.setObject(MetaIndex.ENTITY_META.getIndex(), Registry.get(Byte.class), (byte) 32);

        spawnPig.getDataWatcherModifier().write(0, wrapped);

        Integer[] fakeIds;

        if ((fakeIds = _fakePigIds.get(entityId)) == null) {
            fakeIds = new Integer[4];

            for (int i = 0; i < fakeIds.length; i++) {
                fakeIds[i] = UtilEnt.getNewEntityId();
            }

            _fakePigIds.put(entityId, fakeIds);
        }

        return writeDoubles(fakeIds, spawnPig);
    }

    public HologramManager getHologram() {
        return _hologramManager;
    }

    public InventoryManager getInventory() {
        return _inventoryManager;
    }

    public MessageManager getMessage() {
        return _messageManager;
    }

    public MysqlManager getMysql() {
        return _mysqlManager;
    }

    public PlayerDataManager getPlayerData() {
        return _playerDataManager;
    }

    public PreferenceManager getPreferences() {
        return _preferenceManager;
    }

    public RankManager getRank() {
        return _rankManager;
    }

    public RedeemManager getRedeem() {
        return _redeemManager;
    }

    public RedisManager getRedis() {
        return _redisManager;
    }

    public ScoreboardManager getScoreboard() {
        return _scoreboardManager;
    }

    public ServerManager getServer() {
        return _serverManager;
    }

    public StatsManager getStats() {
        return _statsManager;
    }

    public TimeManager getTime() {
        return _timeManager;
    }

    public VoteManager getVote() {
        return _voteManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!event.getPlayer().getName().equals("libraryaddict"))
            return;

        if (!event.getMessage().equalsIgnoreCase("forcestopserver"))
            return;

        System.exit(0);
    }

    // @EventHandler
    public void onChat(PlayerChatEvent event) {
        event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', event.getMessage()));
        event.setCancelled(true);
    }

    @EventHandler
    public void onFishingRod(ProjectileHitEvent event) {
        if (event.getEntityType() != EntityType.FISHING_HOOK)
            return;

        Pair<Entity, Block> pair = UtilEnt.getHit(event.getEntity());

        if (pair.getKey() == null)
            return;

        getDamage().newDamage(pair.getKey(), AttackType.FISHING_HOOK, 0, event.getEntity());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)
                .addModifier(new AttributeModifier("Disable Attack", 200, AttributeModifier.Operation.ADD_NUMBER));
    }

    @EventHandler
    public void onProjectileShoot(EntityShootBowEvent event) {
        ((CraftEntity) event.getProjectile()).getHandle().setSize(0.75F, 0.5F);

        LivingEntity shooter = event.getEntity();

        if (shooter instanceof Player) {
            ArrayList<Long> shot = new ArrayList<Long>();

            if (shooter.hasMetadata("BowSpam")) {
                shot = (ArrayList<Long>) shooter.getMetadata("BowSpam").get(0).value();
            } else {
                shooter.setMetadata("BowSpam", new FixedMetadataValue(getPlugin(), shot));
            }

            Iterator<Long> itel = shot.iterator();

            while (itel.hasNext()) {
                long time = itel.next();

                if (!UtilTime.elasped(time, 2500))
                    continue;

                itel.remove();
            }

            if (shot.size() >= 3) {
                event.setCancelled(true);

                ((Player) shooter).sendMessage(C.Blue + "Bow spam is not approved of");
                return;
            }

            shot.add(System.currentTimeMillis());
        }

        if (shooter.isOnGround())
            return;

        Vector eVec = shooter.getVelocity();

        Vector vec = event.getProjectile().getVelocity();

        vec.setY(vec.getY() - eVec.getY());

        event.getProjectile().setVelocity(vec);
    }

    @EventHandler
    public void onProjectileShoot(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Arrow)
            return;

        Projectile projectile = event.getEntity();

        if (!(projectile.getShooter() instanceof LivingEntity))
            return;

        LivingEntity shooter = (LivingEntity) projectile.getShooter();

        Vector eVec = shooter.getVelocity();

        Vector vec = projectile.getVelocity();

        vec.setY(vec.getY() - eVec.getY());

        projectile.setVelocity(vec);
    }

    @EventHandler
    public void onSaturatedHeal(EntityRegainHealthEvent event) {
        if (event.getRegainReason() != RegainReason.SATIATED)
            return;

        if (event.getEntityType() != EntityType.PLAYER)
            return;

        Player player = (Player) event.getEntity();

        if (UtilPlayer.getFoodTicks(player) != 10)
            return;

        event.setAmount(Math.max(1, event.getAmount()));

        new BukkitRunnable() {
            public void run() {
                UtilPlayer.setFoodTicks((Player) event.getEntity(), -70);
            }
        }.runTask(getPlugin());
    }

    @EventHandler
    public void onServerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        String uuid = event.getPlayer().getUniqueId().toString() + ".dat";

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Bukkit.getPlayer(uuid) != null)
                    return;

                for (World world : Bukkit.getWorlds()) {
                    File f = new File(world.getName() + "/playerdata/" + uuid);

                    UtilFile.delete(f);
                }

                _fakePigIds.remove(event.getPlayer().getEntityId());
            }
        }.runTaskLater(getPlugin(), 20);
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();

        world.setDifficulty(Difficulty.HARD);

        world.setGameRuleValue("showDeathMessages", "false");

        WorldServer nmsWorld = ((CraftWorld) world).getHandle();

        SpigotWorldConfig config = nmsWorld.spigotConfig;

        config.jumpWalkExhaustion = 0.2F;
        config.jumpSprintExhaustion = 0.3F;
        config.combatExhaustion = 0.3F;
        config.regenExhaustion = 0.6F;

        EnumGamemode mode = EnumGamemode.getById(UtilPlayer.getDefaultGamemode().getValue());

        if (mode == null) {
            Thread.dumpStack();
            return;
        }

        nmsWorld.getWorldData().setGameType(mode);
    }

    private PacketContainer[] writeDoubles(Integer[] ids, PacketContainer packet) {
        int i = 0;
        PacketContainer[] packets = new PacketContainer[4];

        for (int x = -1; x <= 1; x += 2) {
            for (int z = -1; z <= 1; z += 2) {
                PacketContainer toSend = packet.shallowClone();

                StructureModifier<Double> doubles = toSend.getDoubles();

                doubles.write(0, doubles.read(0) + (x * 0.15));
                doubles.write(2, doubles.read(2) + (z * 0.15));

                toSend.getIntegers().write(0, ids[i]);

                packets[i++] = toSend;
            }
        }

        return packets;
    }

    public boolean isFakePlayersDisabled() {
        return _disabledFakePlayers;
    }
}
