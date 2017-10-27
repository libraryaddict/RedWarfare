package me.libraryaddict.build.managers;

import me.libraryaddict.build.commands.*;
import me.libraryaddict.build.database.MysqlDeleteDeletedMaps;
import me.libraryaddict.build.inventories.MainBuildInventory;
import me.libraryaddict.core.C;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.preference.PreferenceItem;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.server.ServerManager;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.network.Pref;
import me.libraryaddict.network.ServerInfo;
import me.libraryaddict.network.ServerInfo.ServerState;
import me.libraryaddict.redis.operations.RedisPublishServerInfo;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class BuildManager extends CentralManager {
    public static UUID getMainHub() {
        return UUID.fromString("1769f276-974c-4f18-bb3b-bdb9444aa43c");
    }

    private ItemStack _openMaps = new ItemBuilder(Material.NETHER_STAR).setTitle(C.Gold + "Open Maps").build();
    private Pref<Boolean> _globalChat = new Pref("Build.GlobalChat", false);
    private WorldManager _worldManager;

    public BuildManager(JavaPlugin plugin) {
        super(plugin);

        UtilPlayer.setDefaultGamemode(GameMode.CREATIVE);

        _worldManager = new WorldManager(plugin, this, getRank(), getServer(), getCensor(), getChat(), getScoreboard());

        new EventManager(getPlugin(), getWorld(), getRank());

        CommandManager commandManager = getCommand();

        commandManager.registerCommand(new CommandBuilder(getWorld(), getRank()));
        commandManager.registerCommand(new CommandCreate(getWorld()));
        commandManager.registerCommand(new CommandDelete(getWorld()));
        commandManager.registerCommand(new CommandMap(getWorld(), getRank()));
        commandManager.registerCommand(new CommandParse(getWorld()));
        commandManager.registerCommand(new CommandTools(getWorld()));
        commandManager.registerCommand(new CommandMapType(getWorld()));
        commandManager.registerCommand(new CommandAddWorldFile(getWorld()));
        commandManager.registerCommand(new CommandSetInfo(getWorld()));
        commandManager.registerCommand(new CommandDeleteMaps(getPlugin(), getRank()));
        commandManager.registerCommand(new CommandSetCreator(getWorld()));
        commandManager.registerCommand(new CommandSkull());
        commandManager.registerCommand(new CommandImport(getWorld()));
        commandManager.registerCommand(new CommandMainWorld(getWorld()));
        commandManager.registerCommand(new CommandSetBlock(getWorld()));
        commandManager.getCommand("ci").setRanks(Rank.ALL);
        commandManager.registerCommand(new CommandTime(getWorld()));
        commandManager.registerCommand(new CommandSpawn());
        commandManager.registerCommand(new CommandSaveAll(getWorld()));
        commandManager.registerCommand(new CommandExport(getWorld()));

        commandManager.unregisterCommand(commandManager.getCommand("teleport"));
        commandManager.unregisterCommand(commandManager.getCommand("hub"));

        commandManager.registerCommand(new CommandBuildJoinServer(getPlugin(), getWorld()));
        commandManager.registerCommand(new CommandGoTo(getWorld(), getRank()));

        commandManager.getCommand("give").setRanks(Rank.ALL);

        commandManager.addBypasses("whitelist", "op", "deop");

        commandManager
                .addBypasses("biomeinfo", "/setbiome", "biomelist", "biomels", "chunkinfo", "listchunks", "delchunks",
                        "/cut", "/paste", "/schematic", "/schem", "clearclipboard", "/load", "/save", "/copy", "/flip",
                        "/rotate", "we", "worldedit", "/fast", "/gmask", "gmask", "/toggleplace", "toggleplace",
                        "/searchitem", "/l", "/search", "searchitem", "/limit", "/hcyl", "/cyl", "/hsphere", "/sphere",
                        "forestgen", "pumpkins", "/pyramid", "/hpyramid", "/generate", "/gen", "/g", "/generatebiome",
                        "/genbiome", "/gb", "/undo", "undo", "/redo", "redo", "/clearhistory", "clearhistory",
                        "unstuck", "!", "ascend", "asc", "descend", "desc", "ceil", "thru", "jumpto", "j", "up",
                        "/hollow", "/line", "/curve", "/overlay", "/center", "/middle", "/naturalize", "/walls",
                        "/faces", "/outline", "/smooth", "/move", "/regen", "/deform", "/forest", "/replace", "/re",
                        "/rep", "/stack", "/set", ".s", "cs", "/pos2", "/chunk", "/pos1", "/hpos1", "/hpos2", "/wand",
                        "toggleeditwand", "/contract", "/outset", "/inset", "/distr", "/sel", ";", "/desel",
                        "/deselect", "/count", "/size", "/expand", "/shift", "snapshot", "snap", "restore", "/restore",
                        "/", ",", "superpickaxe", "pickaxe", "sp", "tool", "lrbuild", "mat", "material", "range",
                        "size", "mask", "none", "tree", "repl", "cycler", "floodfill", "flood", "brush", "br",
                        "deltree", "farwand", "/lrbuild", "info", "/fillr", "/drain", "/fixlava", "fixlava",
                        "/fixwater", "fixwater", "/removeabove", "removeabove", "/removebelow", "removebelow",
                        "/removenear", "removenear", "/replacenear", "replacenear", "/snow", "snow", "/thaw", "thaw",
                        "/green", "green", "/ex", "/ext", "/extinguish", "ex", "ext", "extinguish", "butcher", "remove",
                        "rem", "rement", "/fill", "/help");

        commandManager
                .addBypasses("paint", "u", "d", "p", "perf", "performer", "vs", "vc", "vh", "vi", "vr", "vl", "vir",
                        "v", "b", "btool", "vchunk");

        new BukkitRunnable() {
            public void run() {
                getRank().setupScoreboard(getScoreboard().getMainScoreboard());
                getScoreboard().getMainScoreboard().setSidebarTitle(C.DRed + "RwF Build");
            }
        }.runTaskLater(getPlugin(), 2);

        getPreferences().register(new PreferenceItem("Global Chat", getGlobalChat(),
                new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3)
                        .addLore(C.Italic + C.Purple + "Toggle this to always talk in global chat!",
                                "To talk in local chat prefix your messages with a @ like you would do for global " +
                                        "chat!")
                        .build()));
        final String ip = this.getWorld().getIP();
        new BukkitRunnable() {
            public void run() {
                new MysqlDeleteDeletedMaps(ip);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    public Pref<Boolean> getGlobalChat() {
        return _globalChat;
    }

    public WorldManager getWorld() {
        return _worldManager;
    }

    @EventHandler
    public void onChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntityType() != EntityType.FALLING_BLOCK)
            return;

        if (event.getEntity().getWorld().getEntitiesByClass(FallingBlock.class).size() < 10)
            return;

        event.getEntity().remove();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (!UtilInv.isSimilar(event.getItem(), _openMaps))
            return;

        new MainBuildInventory(event.getPlayer(), getWorld(), getRank()).openInventory();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        p.getInventory().setItem(8, _openMaps);

        FakeScoreboard mainBoard = getScoreboard().getMainScoreboard();

        Rank rank = getRank().getDisplayedRank(p);

        FakeTeam team = mainBoard.getTeam(rank.name());

        team.addPlayer(p);

        Stats.timeStart(p, "Build.Time");
    }

    // @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (getRank().getRank(event.getPlayer()).hasRank(Rank.OWNER)) {
            return;
        }

        event.setResult(Result.KICK_WHITELIST);
        event.setKickMessage(C.Blue + "In testing!");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();

        FakeScoreboard mainBoard = getScoreboard().getMainScoreboard();

        Rank rank = getRank().getDisplayedRank(p);

        FakeTeam team = mainBoard.getTeam(rank.name());

        team.removePlayer(p.getName());
    }

    @EventHandler
    public void onSendInfo(TimeEvent event) {
        if (event.getType() != TimeType.SEC)
            return;

        if (!getServer().isPublic())
            return;

        ServerInfo serverInfo = new ServerInfo(
                getWorld().getLoadedWorlds().isEmpty() ? ServerState.JOINABLE : ServerState.JOINABLE_NO_MERGE,
                ServerManager.getServerName(), ServerType.Build, Bukkit.getOnlinePlayers().size(), 0,
                getServer().getIP() + ":" + Bukkit.getPort(), getServer().isUpdateReady());

        new BukkitRunnable() {
            public void run() {
                new RedisPublishServerInfo(serverInfo);
            }
        }.runTaskAsynchronously(getPlugin());
    }
}
