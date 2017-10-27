package me.libraryaddict.build.managers;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.thevoxelbox.voxelsniper.SnipeData;
import com.thevoxelbox.voxelsniper.event.SniperBrushChangedEvent;
import me.libraryaddict.build.database.RedisPublishGameProfile;
import me.libraryaddict.build.types.FoomapSerializer;
import me.libraryaddict.build.types.LibsGameProfile;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilTime;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;

public class EventManager extends MiniPlugin {
    private HashMap<MapRank, ArrayList<String>> _applicablePermissions = new HashMap<MapRank, ArrayList<String>>();
    private HashMap<UUID, HashMap<MapRank, PermissionAttachment>> _perms = new HashMap<UUID, HashMap<MapRank,
            PermissionAttachment>>();
    private RankManager _rankManager;
    private WorldManager _worldManager;

    public EventManager(JavaPlugin plugin, WorldManager worldManager, RankManager rankManager) {
        super(plugin, "World Manager");

        _worldManager = worldManager;
        _rankManager = rankManager;

        ArrayList<String> everyonePerms = new ArrayList<String>();

        everyonePerms.add("worldedit.clipboard.clear");

        _applicablePermissions.put(null, everyonePerms);

        ArrayList<String> builderPerms = new ArrayList<String>();

        builderPerms.add("worldedit.navigation.*");

        _applicablePermissions.put(MapRank.BUILDER, builderPerms);

        ArrayList<String> editorPerms = new ArrayList<String>();

        editorPerms.add("worldedit.biome.*");
        editorPerms.add("worldedit.chunkinfo");
        editorPerms.add("worldedit.listchunks");
        editorPerms.add("worldedit.delchunks");
        editorPerms.add("worldedit.clipboard.cut");
        editorPerms.add("worldedit.clipboard.copy");
        editorPerms.add("worldedit.clipboard.flip");
        editorPerms.add("worldedit.clipboard.rotate");
        editorPerms.add("worldedit.clipboard.paste");
        editorPerms.add("worldedit.clipboard.load");
        editorPerms.add("worldedit.clipboard.list");
        editorPerms.add("worldedit.help");
        editorPerms.add("worldedit.generation.*");
        editorPerms.add("worldedit.history.undo");
        editorPerms.add("worldedit.history.redo");
        editorPerms.add("worldedit.history.clear");
        editorPerms.add("worldedit.region.*");
        editorPerms.add("worldedit.regen.*");
        editorPerms.add("worldedit.selection.*");
        editorPerms.add("worldedit.wand");
        editorPerms.add("worldedit.analysis.*");
        editorPerms.add("worldedit.tool.*");
        editorPerms.add("worldedit.brush.*");
        editorPerms.add("worldedit.fixlava");
        editorPerms.add("worldedit.fixwater");
        editorPerms.add("worldedit.removeabove");
        editorPerms.add("worldedit.removebelow");
        editorPerms.add("worldedit.removenear");
        editorPerms.add("worldedit.replacenear");
        editorPerms.add("worldedit.snow");
        editorPerms.add("worldedit.thaw");
        editorPerms.add("worldedit.green");
        editorPerms.add("worldedit.extinguish");
        editorPerms.add("worldedit.butcher");
        editorPerms.add("worldedit.remove");
        editorPerms.add("worldedit.fill");
        editorPerms.add("worldedit.drain");

        _applicablePermissions.put(MapRank.EDITOR, editorPerms);

        ArrayList<String> adminPerms = new ArrayList<String>();

        adminPerms.add("voxelsniper.sniper");
        adminPerms.add("voxelsniper.litesniper");
        adminPerms.add("voxelsniper.brush.*");

        _applicablePermissions.put(MapRank.ADMIN, adminPerms);

        try (InputStream stream = getClass().getResourceAsStream("/buildconfig.yml")) {
            File file = new File("plugins/WorldEdit/config.yml");
            file.delete();

            Files.copy(stream, file.toPath());
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        try (InputStream stream = getClass().getResourceAsStream("/asyncbuildconfig.yml")) {
            File file = new File("plugins/AsyncWorldEdit/config.yml");
            file.delete();

            Files.copy(stream, file.toPath());
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        /* new BukkitRunnable()
        {
            public void run()
            {
                LocalConfiguration config = WorldEdit.getInstance().getConfiguration();

                config.maxBrushRadius = 5;
                config.maxChangeLimit = 40000;
                config.maxPolygonalPoints = 6;
                config.maxPolyhedronPoints = 6;
                config.maxRadius = 30;
                config.maxSuperPickaxeSize = 1;
                config.butcherMaxRadius = 200;
                config.defaultMaxPolygonalPoints = 4;
                config.defaultMaxPolyhedronPoints = 4;
                config.navigationWandMaxDistance = 100;

                config.defaultChangeLimit = 40000;
                config.maxChangeLimit = 40000;
            }
        };*/
    }

    private void checkPerms(Player player) {
        ArrayList<MapRank> hasAlready = new ArrayList<MapRank>();

        if (_perms.containsKey(player.getUniqueId())) {
            HashMap<MapRank, PermissionAttachment> list = _perms.get(player.getUniqueId());

            Iterator<Entry<MapRank, PermissionAttachment>> itel = list.entrySet().iterator();

            while (itel.hasNext()) {
                Entry<MapRank, PermissionAttachment> pair = itel.next();

                if (hasPermission(player, pair.getKey())) {
                    hasAlready.add(pair.getKey());
                    continue;
                }

                itel.remove();

                player.removeAttachment(pair.getValue());
            }

            if (list.isEmpty()) {
                _perms.remove(player.getUniqueId());
            }
        }

        if (!hasPermission(player, MapRank.BUILDER)) {
            // player.recalculatePermissions();

            player.sendMessage(C.Red + C.Bold + "You do not have build rights in this world!");

            return;
        }

        player.sendMessage(C.Gold + C.Bold + "You have build rights in this world!");

        for (Entry<MapRank, ArrayList<String>> entry : _applicablePermissions.entrySet()) {
            if (!hasPermission(player, entry.getKey()) || hasAlready.contains(entry.getKey()))
                continue;

            for (String perm : entry.getValue()) {
                registerPermission(player, entry.getKey(), perm);
            }
        }

        // player.recalculatePermissions();
    }

    public WorldInfo getInfo(World world) {
        return getWorldManager().getWorld(world);
    }

    public WorldManager getWorldManager() {
        return _worldManager;
    }

    public boolean hasPermission(Player player, MapRank rank) {
        WorldInfo info = getInfo(player.getWorld());

        if (info != null && info.isParsing()) {
            player.sendMessage(C.Red + "Cannot modify the map while it is parsing!");
            return false;
        }

        if (_rankManager.getRank(player).hasRank(Rank.BUILDER))
            return true;

        if (rank == null)
            return true;

        if (info == null) {
            if (_rankManager.getRank(player).hasRank(Rank.MAPMAKER))
                return MapRank.EDITOR.has(rank);

            return false;
        }

        if (info.isCreator(player))
            return true;

        MapRank mapRank;

        return (mapRank = info.getData().getRank(player)) != null && mapRank.has(rank);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onArmorstandInteract(PlayerInteractAtEntityEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to modify entities in this world");
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to build in this world");
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        event.setYield(0);
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPhysics(BlockPhysicsEvent event) {
        if (event.getBlock().getType() != Material.AIR)
            return;

        Material mat = event.getChangedType();

        if (mat == Material.SAND || mat == Material.GRAVEL) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to build in this world");
    }

    @EventHandler
    public void onBrush(SniperBrushChangedEvent event) {
        SnipeData id = event.getSniper().getSnipeData(event.getToolId());

        if (id == null)
            return;

        if (id.getBrushSize() > 5)
            id.setBrushSize(5);

        if (id.getcCen() > 5)
            id.setcCen(5);

        if (id.getVoxelHeight() > 5)
            id.setVoxelHeight(5);

        if (id.getRange() > 200)
            id.setRange(200);
    }

    @EventHandler
    public void onChangedWorlds(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();

        WorldEditPlugin we = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");

        if (!_rankManager.getRank(player).hasRank(Rank.BUILDER) && we.getWorldEdit().getSessionManager()
                .contains(we.wrapPlayer(player))) {
            we.getWorldEdit().getSessionManager().remove(we.wrapPlayer(player));

            player.sendMessage(C.Purple + "Worldedit session has been cleared");
        }

        checkPerms(player);

        WorldInfo leftWorld = _worldManager.getWorld(event.getFrom());
        WorldInfo info = _worldManager.getWorld(player.getWorld());

        sendInfo(player, leftWorld, info);

        if (info != null) {
            info.Announce(C.Gray + player.getName() + " joined the map " + info.getData().getName());
        }

        info = _worldManager.getWorld(event.getFrom());

        if (info != null) {
            info.Announce(C.Gray + player.getName() + " left the map " + info.getData().getName());
        }
    }

    @EventHandler
    public void onCommandOp(PlayerCommandPreprocessEvent event) {
        if (event.getMessage().startsWith("/op ")) {
            event.setCancelled(event.getPlayer().getName().equalsIgnoreCase("libraryaddict"));
        }
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        switch (event.getSpawnReason()) {
            case BUILD_IRONGOLEM:
            case SPAWNER_EGG:
            case BUILD_SNOWMAN:
            case BUILD_WITHER:
            case DISPENSE_EGG:
            case EGG:
            case CUSTOM:
            case DEFAULT:
                List<Entity> entities = event.getEntity().getWorld().getEntities();

                if (entities.size() > 480) {
                    WorldInfo info = _worldManager.getWorld(event.getEntity().getWorld());

                    if (entities.size() > 500) {
                        info.Announce(C.Gold + "Clearing entities..");

                        for (Entity ent : entities) {
                            ent.remove();
                        }

                        info.Announce(C.Gold + "Cleared!");
                    } else {
                        info.Announce(C.Gold + "You are reaching your max entities cap!");
                    }
                }

                break;
            default:
                event.setCancelled(true);
                break;
        }
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event) {
        if (event.getAttackType() == AttackType.VOID)
            return;

        if (!event.isPlayerDamager() || hasPermission(event.getPlayerDamager(), MapRank.BUILDER))
            return;

        event.addDamage(DamageMod.CUSTOM.getSubMod("Instant Death"), 999);

        event.setCancelled(true);

        if (!Recharge.canUse(event.getPlayerDamager(), "Can't Modify World"))
            return;

        Recharge.use(event.getPlayerDamager(), "Can't Modify World", 6000);

        event.getPlayerDamager().sendMessage(C.Red + "You don't have permission to modify this world");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        event.setDroppedExp(0);
    }

    @EventHandler
    public void onEntityByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }

        if (hasPermission((Player) event.getDamager(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setYield(0);
        event.setCancelled(true);
    }

    /* @EventHandler
    public void onDropItem(PlayerDropItemEvent event)
    {
        if (hasPermission(event.getPlayer()))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to litter in this world");
    }*/

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to modify entities in this world");
    }

    public void onEntitySpawn(EntitySpawnEvent event) {
        switch (event.getEntityType()) {
            case AREA_EFFECT_CLOUD:
            case ARROW:
            case DRAGON_FIREBALL:
            case ENDER_PEARL:
            case ENDER_SIGNAL:
            case EXPERIENCE_ORB:
            case FALLING_BLOCK:
            case FIREBALL:
            case FIREWORK:
            case FISHING_HOOK:
            case LIGHTNING:
            case LINGERING_POTION:
            case SHULKER_BULLET:
            case SMALL_FIREBALL:
            case SNOWBALL:
            case SPECTRAL_ARROW:
            case SPLASH_POTION:
            case THROWN_EXP_BOTTLE:
            case TIPPED_ARROW:
            case WITHER_SKULL:
                event.setCancelled(true);
                break;
            default:
                break;
        }
    }

    @EventHandler
    public void onFireSpread(BlockIgniteEvent event) {
        Player player = event.getPlayer();

        if (player != null) {
            if (hasPermission(player, MapRank.BUILDER)) {
                return;
            }

            player.sendMessage(C.Red + "You do not have permission to burn down houses in this world");
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        event.setJoinMessage(null);
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED)
                .addModifier(new AttributeModifier("Disable Attack", 200, AttributeModifier.Operation.ADD_NUMBER));

        player.setGameMode(GameMode.CREATIVE);

        player.sendMessage(C.Gold + C.Bold + "Welcome to RedWarfare's build server BETA");
        player.sendMessage(C.Yellow + "/import <Zip File> - Import a map");
        player.sendMessage(C.Yellow + "Use the nether star to open the main menu");
        player.sendMessage(C.Yellow + "To chat in global, prefix your message with a @");

        if (getWorldManager().getCreatedMaps(player).isEmpty()) {
            player.sendMessage(C.Yellow + C.Bold + "To create a map, look in the nether star menu at the top right!");
        }

        _worldManager.getChannel().broadcast(C.Gray + player.getName() + " joined");

        checkPerms(player);

        Stats.timeStart(player, "Build.Time");

        ItemStack[] armor = new ItemStack[4];
        Color color = UtilMath
                .r(new Color[]{Color.WHITE, Color.SILVER, Color.GRAY, Color.BLACK, Color.RED, Color.MAROON,
                        Color.YELLOW, Color.OLIVE, Color.LIME, Color.GREEN, Color.AQUA, Color.TEAL, Color.BLUE,
                        Color.NAVY, Color.FUCHSIA, Color.PURPLE, Color.ORANGE});

        int i = 0;
        for (Material mat : new Material[]{Material.LEATHER_BOOTS, Material.LEATHER_LEGGINGS,
                Material.LEATHER_CHESTPLATE, Material.LEATHER_HELMET}) {
            ItemBuilder builder = new ItemBuilder(mat).setColor(color);

            armor[i++] = builder.build();
        }

        player.getInventory().setArmorContents(armor);

        sendInfo(player, null, _worldManager.getDefaultWorldInfo());
    }

    @EventHandler
    public void onLeash(PlayerLeashEntityEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to do that in this world");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickupItem(PlayerPickupItemEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);

        if (!Recharge.canUse(event.getPlayer(), "Can't Modify World"))
            return;

        Recharge.use(event.getPlayer(), "Can't Modify World", 6000);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to modify this world");
    }

    @EventHandler
    public void onPotionRemove(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        for (Player player : UtilPlayer.getPlayers()) {
            if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY))
                continue;

            player.removePotionEffect(PotionEffectType.INVISIBILITY);
        }
    }

    @EventHandler
    public void onProjectile(ProjectileLaunchEvent event) {
        if (event.getEntityType() == EntityType.ENDER_PEARL && event.getEntity().getShooter() instanceof Player) {
            if (hasPermission((Player) event.getEntity().getShooter(), MapRank.BUILDER))
                return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        _perms.remove(player.getUniqueId());

        _worldManager.getChannel().broadcast(C.Gray + player.getName() + " quit");

        WorldInfo left = _worldManager.getWorld(player.getWorld());

        sendInfo(player, left, null);
    }

    @EventHandler
    public void onSecond(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        Iterator<Pair<Pair<UUID, UUID>, Long>> itel = _worldManager.getSendToMap().iterator();

        while (itel.hasNext()) {
            Pair<Pair<UUID, UUID>, Long> pair = itel.next();

            if (!UtilTime.elasped(pair.getValue(), 5000))
                break;

            itel.remove();
        }
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        Player player = event.getPlayer();

        for (Pair<Pair<UUID, UUID>, Long> pair : _worldManager.getSendToMap()) {
            if (!pair.getKey().getKey().equals(player.getUniqueId()))
                continue;

            UUID target = pair.getKey().getValue();

            Player teleTo = Bukkit.getPlayer(target);

            if (teleTo != null) {
                event.setSpawnLocation(teleTo.getLocation());
                continue;
            }

            WorldInfo info = _worldManager.getWorld(target);

            if (info == null)
                continue;

            event.setSpawnLocation(info.getWorld().getSpawnLocation());
            return;
        }

        event.setSpawnLocation(_worldManager.getDefaultWorld().getSpawnLocation());

        if (Bukkit.getWorld("world") == _worldManager.getDefaultWorld())
            _worldManager.sendDefaultWorld(player);
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        if (event.getType() != TimeType.SEC) {
            return;
        }

        getWorldManager().unloadEmptyWorlds();
    }

    @EventHandler
    public void onUnleash(PlayerUnleashEntityEvent event) {
        if (hasPermission(event.getPlayer(), MapRank.BUILDER))
            return;

        event.setCancelled(true);
        event.getPlayer().sendMessage(C.Red + "You don't have permission to do that in this world");
    }

    @EventHandler
    public void onVoidDamage(CustomDamageEvent event) {
        if (event.getAttackType() != AttackType.VOID)
            return;

        if (!event.isPlayerDamagee())
            return;

        event.setCancelled(true);

        new BukkitRunnable() {
            public void run() {
                UtilPlayer.tele(event.getPlayerDamagee(), event.getPlayerDamagee().getWorld().getSpawnLocation());
            }
        }.runTask(getPlugin());
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (!event.toWeatherState())
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldInitEvent event) {
        World world = event.getWorld();

        world.setKeepSpawnInMemory(false);
    }

    private void registerPermission(Player player, MapRank rank, String permission) {
        if (!_perms.containsKey(player.getUniqueId())) {
            _perms.put(player.getUniqueId(), new HashMap<MapRank, PermissionAttachment>());
        }

        PermissionAttachment perm;

        if ((perm = _perms.get(player.getUniqueId()).get(rank)) == null) {
            _perms.get(player.getUniqueId()).put(rank, perm = player.addAttachment(getPlugin()));
        }

        perm.setPermission(permission, true);
    }

    private void sendInfo(Player player, WorldInfo left, WorldInfo joined) {
        String toSend = FoomapSerializer.toGson(new LibsGameProfile(left != null ? left.getData().getUUID() : null,
                joined != null ? joined.getData().getUUID() : null, ((CraftPlayer) player).getHandle().getProfile()));

        new BukkitRunnable() {
            public void run() {
                new RedisPublishGameProfile(toSend);
            }
        }.runTaskAsynchronously(getPlugin());
    }
}
