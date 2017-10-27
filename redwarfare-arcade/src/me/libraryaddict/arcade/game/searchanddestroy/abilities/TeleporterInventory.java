package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.TeamBomb;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilLoc;

public class TeleporterInventory extends PageInventory {
    private Game _game;
    private GameTeam _team;
    private TeleporterAbility _teleporter;

    public TeleporterInventory(Player player, TeleporterAbility ability, Game game) {
        super(player, "Teleporter Inventory");

        _game = game;
        _team = game.getTeam(player);
        _teleporter = ability;

        buildPages();
    }

    public void buildPages() {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        ArrayList<Player> players = _team.getPlayers(true);

        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player o1, Player o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });

        ArrayList<TeamBomb> bombs = null;
        if (_game instanceof SearchAndDestroy) {
            bombs = new ArrayList<TeamBomb>(((SearchAndDestroy) _game).getBombs());

            Collections.sort(bombs, new Comparator<TeamBomb>() {

                @Override
                public int compare(TeamBomb o1, TeamBomb o2) {
                    String t1 = o1.getTeam() == null ? "Nuke" : o1.getTeam().getName();
                    String t2 = o2.getTeam() == null ? "Nuke" : o2.getTeam().getName();

                    return t1.compareToIgnoreCase(t2);
                }
            });
        }

        for (int i = 0; i < players.size(); i++) {
            Player clickedPlayer = players.get(i);

            if (clickedPlayer == getPlayer())
                continue;

            Kit kit = _game.getKit(clickedPlayer);

            ItemBuilder builder = new ItemBuilder(kit.getIcon());

            builder.setTitle(_team.getColoring() + clickedPlayer.getName());
            builder.addLore(C.Blue + "Kit: " + C.White + kit.getName());
            builder.addLore(C.Blue + "Distance: " + C.White
                    + new DecimalFormat("#.#").format(UtilLoc.getDistance(clickedPlayer, getPlayer())));

            if (_game instanceof SearchAndDestroy) {
                for (TeamBomb bomb : bombs) {
                    builder.addLore(C.Blue + "Distance from "
                            + (bomb.isOwned() ? bomb.getTeam().getColoring() + bomb.getTeam().getName() : "nuke") + ": " + C.White
                            + new DecimalFormat("#.#")
                                    .format(UtilLoc.getDistance(bomb.getBlock().getLocation(), clickedPlayer.getLocation())));
                }
            }

            builder.addLore("");
            builder.addLore(C.Green + C.Bold + "Click to establish teleporter");

            items.add(Pair.of(builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (!_game.isAlive(clickedPlayer))
                        return true;

                    if (!_game.isAlive(getPlayer()))
                        return true;

                    if (!_game.isLive())
                        return true;

                    Location loc = clickedPlayer.getLocation();

                    if (!UtilLoc.isSafeTeleport(loc)) {
                        getPlayer().sendMessage(C.Blue + "Your target is not in a valid space!");
                        return true;
                    }

                    Block block = loc.getBlock().getRelative(BlockFace.DOWN);

                    if (!UtilBlock.solid(block)) {
                        getPlayer().sendMessage(C.Blue + "Your target is not in a valid space!");
                        return true;
                    }

                    if (_game instanceof SearchAndDestroy) {
                        for (TeamBomb bomb : ((SearchAndDestroy) _game).getBombs()) {
                            if (UtilLoc.getDistance2d(bomb.getBlock(), block) == 0
                                    && Math.abs(bomb.getBlock().getY() - block.getY()) <= 1) {
                                getPlayer().sendMessage(C.Red + "Teleporter cannot be placed this close to a bomb!");
                                return true;
                            }
                        }
                    }

                    if (_teleporter.getTeleporters(getPlayer()).size() >= 2) {
                        getPlayer().sendMessage(C.Red + "You already have two teleporters placed!");
                        return true;
                    }

                    Teleporter tele = _teleporter.getTeleporter(block);

                    if (tele != null) {
                        getPlayer().sendMessage(C.Red + "There is already a teleporter here!");
                        return true;
                    }

                    tele = new Teleporter(_team, getPlayer(), block);

                    tele.setUnstable();

                    if (!tele.isValid()) {
                        getPlayer().sendMessage(C.Red + "Teleporter is in an invalid space!");
                        return true;
                    }

                    _teleporter.addTeleporter(getPlayer(), tele);

                    Recharge.use(getPlayer(), "Remote Teleporters",
                            (long) Math.min(90000, UtilLoc.getDistance(getPlayer(), clickedPlayer) * 500), true);

                    getPlayer().sendMessage(C.Blue + "Your remote teleporter has been established!");
                    clickedPlayer.sendMessage(C.Blue + getPlayer().getName() + " established a teleporter at your location!");

                    closeInventory();
                    return true;
                }
            }));
        }

        while (items.size() % 9 != 0)
            items.add(null);

        for (int i = 0; i < 8; i++)
            items.add(null);

        items.add(Pair.of(new ItemBuilder(Material.WOOL, 1, (short) 14).setTitle(C.Red + "Destroy Teleporters").build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        if (!_game.isAlive(getPlayer()))
                            return true;

                        if (!_game.isLive())
                            return true;

                        ArrayList<Teleporter> teleporters = _teleporter.getTeleporters(getPlayer());

                        if (teleporters.isEmpty()) {
                            getPlayer().sendMessage(C.Red + "You have no existing teleporters!");
                            return true;
                        }

                        double dist = 0;

                        for (Teleporter tele : teleporters) {
                            dist += UtilLoc.getDistance(tele.getPlayer().getLocation(), tele.getBlock().getLocation());

                            tele.remove();
                            _teleporter.getTeleporters().remove(tele);
                        }

                        Recharge.use(getPlayer(), "Remote Teleporters", (long) Math.min(90000, dist * 500), true);

                        getPlayer().sendMessage(
                                C.Blue + "You have broken your teleporter" + (teleporters.size() == 1 ? "" : "s") + "!");

                        closeInventory();

                        return true;
                    }
                }));

        setPages(items);
    }

    @EventHandler
    public void onSecond(TimeEvent event) {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (!_game.isLive())
            return;

        buildPages();
    }
}