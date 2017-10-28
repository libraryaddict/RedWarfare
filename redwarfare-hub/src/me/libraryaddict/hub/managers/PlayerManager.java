package me.libraryaddict.hub.managers;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import me.libraryaddict.core.C;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.command.commands.CommandRefundMe;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.FakeTeam;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.hub.types.Portal;

public class PlayerManager extends MiniPlugin {
    private ArrayList<UUID> _hidingPlayers = new ArrayList<UUID>();
    private HubManager _hubManager;
    private ItemStack _openSnD = new ItemBuilder(Material.BLAZE_POWDER).setTitle(C.Gold + "Join Search and Destroy")
            .addLore(C.Aqua + C.Bold + "LEFT CLICK TO INSTANT JOIN", C.Purple + C.Bold + "RIGHT CLICK TO OPEN MENU").build();
    private ItemStack _togglePlayers = new ItemBuilder(Material.TORCH).setTitle(C.Blue + "Toggle Players")
            .addLore(C.Green + "Right click with this to toggle visibility of other players").build();

    public PlayerManager(JavaPlugin plugin, HubManager hubManager) {
        super(plugin, "Player Manager");

        _hubManager = hubManager;

        new BukkitRunnable() {
            public void run() {
                getManager().getScoreboard().getMainScoreboard().setSidebarTitle(C.Gold + "Players");
                getManager().getRank().setupScoreboard(getManager().getScoreboard().getMainScoreboard());
            }
        }.runTaskLater(getPlugin(), 2);
    }

    public HubManager getManager() {
        return _hubManager;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!UtilInv.isSimilar(event.getItem(), _openSnD))
            return;

        if (event.getAction() == Action.PHYSICAL)
            return;

        if (event.getAction().name().contains("LEFT")) {
            getManager().getPortal().joinGame(event.getPlayer().getUniqueId(), ServerType.SearchAndDestroy);
        }
        else {
            for (Portal portal : getManager().getPortal().getPortals()) {
                if (portal.getType() != ServerType.SearchAndDestroy)
                    continue;

                portal.openMenu(event.getPlayer());
            }
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();

        p.setGameMode(GameMode.ADVENTURE);

        p.getInventory().setItem(0, _togglePlayers.clone());
        p.getInventory().setItem(1, _openSnD.clone());

        if (_hubManager.getRank().getRank(p).hasRank(Rank.MOD)) {
            p.getInventory().setItem(8, _hubManager.getCosmetics().getItem());
        }

        for (Player player : UtilPlayer.getPlayers()) {
            if (!_hidingPlayers.contains(player.getUniqueId()))
                continue;

            player.hidePlayer(p);
        }

        Stats.timeStart(p, "Hub.Time");

        FakeScoreboard mainBoard = getManager().getScoreboard().getMainScoreboard();

        Rank rank = getManager().getRank().getDisplayedRank(p);

        FakeTeam team = mainBoard.getTeam(rank.name());

        team.addPlayer(p);

/*        p.sendMessage(C.Red
                + "Red Warfare will be shutting down in the coming few months, it costs money to run and I'm currently paying for it out of my own pocket.");

        if (getManager().getRank().getRank(p).hasRank(Rank.VIP)) {
            p.sendMessage(C.Gold + "When Red Warfare closes down, do you want a refund? Click below to toggle it off");

            FancyMessage message = new FancyMessage(C.Gold + "Refund me: "
                    + (Preference.getPreference(p, CommandRefundMe.REFUND) ? C.DGreen + "Yes" : C.DRed + "No"))
                            .command("/refundme");
            message.send(p);
        }*/
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        _hidingPlayers.remove(player.getUniqueId());

        FakeScoreboard mainBoard = getManager().getScoreboard().getMainScoreboard();

        Rank rank = getManager().getRank().getDisplayedRank(player);

        FakeTeam team = mainBoard.getTeam(rank.name());

        team.removePlayer(player.getName());
    }

    @EventHandler
    public void onScoreboardTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        ArrayList<String> lines = new ArrayList<String>();

        lines.add(C.Bold + "Players: " + C.Reset + UtilPlayer.getPlayers().size());

        board.setSidebar(lines);
    }

    @EventHandler
    public void onSpawn(PlayerSpawnLocationEvent event) {
        Location spawn = Bukkit.getWorld("world").getSpawnLocation();
        spawn.setYaw(90);

        event.setSpawnLocation(spawn);
    }

    @EventHandler
    public void onToggle(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        Player player = event.getPlayer();

        if (item == null)
            return;

        if (item.getType() == Material.TORCH) {
            _hidingPlayers.add(player.getUniqueId());

            for (Player p : UtilPlayer.getPlayers()) {
                player.hidePlayer(p);
            }

            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2, 0);

            item.setType(Material.REDSTONE_TORCH_ON);
            player.updateInventory();
        }
        else if (item.getType() == Material.REDSTONE_TORCH_ON) {
            _hidingPlayers.remove(player.getUniqueId());

            for (Player p : UtilPlayer.getPlayers()) {
                player.showPlayer(p);
            }

            player.playSound(player.getLocation(), Sound.ENTITY_CHICKEN_EGG, 2, 0);

            item.setType(Material.TORCH);
            player.updateInventory();
        }
    }
}
