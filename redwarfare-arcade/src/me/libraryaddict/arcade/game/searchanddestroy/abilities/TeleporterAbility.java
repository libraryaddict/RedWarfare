package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.ArrayList;
import java.util.Objects;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.TeamBomb;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilShapes;

public class TeleporterAbility extends Ability {
    private int _side = 0;
    private ArrayList<Teleporter> _teleporters = new ArrayList<Teleporter>();

    public void addTeleporter(Player player, Teleporter tele) {
        _teleporters.add(tele);

        ArrayList<Teleporter> teleporters = getTeleporters(player);

        for (Teleporter teleporter : teleporters) {
            teleporter.setConnected(teleporters.size() == 2);
            teleporter.updateHolograms();
        }

        tele.start();
    }

    public Teleporter getTeleporter(Block block) {
        for (Teleporter teleporter : _teleporters) {
            if (Objects.equals(teleporter.getBlock(), block))
                return teleporter;
        }

        return null;
    }

    public ArrayList<Teleporter> getTeleporters() {
        return _teleporters;
    }

    public ArrayList<Teleporter> getTeleporters(Player player) {
        ArrayList<Teleporter> teles = new ArrayList<Teleporter>();

        for (Teleporter tele : _teleporters) {
            if (!tele.owns(player))
                continue;

            teles.add(tele);
        }

        return teles;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isLive()) {
            return;
        }

        if (!isAlive(event.getPlayer())) {
            return;
        }

        onRemove(event.getPlayer(), event.getBlock());
    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        ArrayList<Teleporter> teles = getTeleporters(event.getPlayer());

        for (Teleporter tele : teles) {
            tele.remove();
        }

        _teleporters.removeAll(teles);
    }

    @EventHandler
    public void onInteractBlock(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        if (!Recharge.canUse(player, "TeleClick"))
            return;

        Recharge.use(player, "TeleClick", 200);

        boolean holding = hasAbility(player) && UtilInv.isHolding(player, Material.QUARTZ);

        Block block = event.getClickedBlock();
        Teleporter tele = getTeleporter(block);
        Player owner = null;

        if (tele != null) {
            owner = tele.getPlayer();

            if (holding && (player == owner || !getGame().sameTeam(player, owner))) {
                onRemove(player, block);
            }
            else if (Recharge.canUse(player, "Tele Info")) {
                Recharge.use(player, "Tele Info", 500);

                if (player == owner) {
                    player.sendMessage(C.Gold + "This is your teleporter!");
                }
                else {
                    player.sendMessage(C.Gold + "This is " + getGame().getTeam(owner).getColoring() + owner.getName() + "'s"
                            + C.Gold + " teleporter!");
                }
            }
        }
        else {
            if (!holding)
                return;

            if (!UtilBlock.solid(block)) {
                player.sendMessage(C.Red + "This block cannot be transformed into a teleporter!");
                return;
            }

            if (getGame() instanceof SearchAndDestroy) {
                for (TeamBomb bomb : ((SearchAndDestroy) getGame()).getBombs()) {
                    if (UtilLoc.getDistance2d(bomb.getBlock(), block) == 0
                            && Math.abs(bomb.getBlock().getY() - block.getY()) <= 1) {
                        player.sendMessage(C.Red + "Teleporter cannot be placed this close to a bomb!");
                        return;
                    }
                }
            }

            if (event.getBlockFace() != BlockFace.UP) {
                player.sendMessage(C.Red + "Must place the teleporter on the top of a block! Not the sides or bottom!");
                return;
            }

            if (getTeleporters(player).size() >= 2) {
                player.sendMessage(C.Red + "You already have two teleporters placed!");
                return;
            }

            if (!UtilLoc.isLookingAt(player, block)) {
                player.sendMessage(C.Red + "Line of sight is obstructed!");
                return;
            }

            tele = new Teleporter(getGame().getTeam(player), player, block);

            if (!tele.isValid()) {
                player.sendMessage(C.Red + "Teleporter is in an invalid space!");
                return;
            }

            addTeleporter(player, tele);
        }
    }

    @EventHandler
    public void onInteractRemote(PlayerInteractEvent event) {
        if (!isLive())
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!UtilInv.isItem(event.getItem(), Material.EMPTY_MAP) && !UtilInv.isItem(event.getItem(), Material.MAP))
            return;

        event.setCancelled(true);

        player.updateInventory();

        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (!Recharge.canUse(player, "Remote Teleporters")) {
            player.sendMessage(C.Red + "You cannot do another remote teleporter this soon!");
            return;
        }

        new TeleporterInventory(player, this, getGame()).openInventory();
    }

    @EventHandler
    public void onPush(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (getTeleporter(block) == null)
                continue;

            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPush(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (getTeleporter(block) == null)
                continue;

            event.setCancelled(true);
            return;
        }
    }

    private void onRemove(Player remover, Block block) {
        Teleporter tele = getTeleporter(block);

        if (tele == null)
            return;

        Player player = tele.getPlayer();

        if (remover != player && getGame().sameTeam(remover, player)) {

            if (player == remover) {
                remover.sendMessage(C.Gold + "This is your teleporter!");
            }
            else {
                remover.sendMessage(C.Gold + "This is " + getGame().getTeam(player).getColoring() + player.getName() + "'s"
                        + C.Gold + " teleporter!");
            }

            return;
        }

        tele.remove();

        _teleporters.remove(tele);

        if (remover == player) {
            remover.sendMessage(C.Gold + "You have broken your teleporter!");
        }
        else {
            remover.sendMessage(C.Gold + "You have broken " + getGame().getTeam(player).getColoring() + player.getName() + "'s"
                    + C.Gold + " teleporter!");
            player.sendMessage(
                    getGame().getTeam(remover).getColoring() + remover.getName() + C.Gold + " has broken your teleporter!");
        }

        ArrayList<Teleporter> teleporters = getTeleporters(player);

        for (Teleporter teleporter : teleporters) {
            teleporter.setConnected(false);
            teleporter.updateHolograms();
        }
    }

    @EventHandler
    public void onTeleUnstable(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        _side = (_side + 1) % 6;

        if (_side % 2 != 0)
            return;

        for (Teleporter tele : _teleporters) {
            if (!tele.isUnstable())
                continue;

            Block b = tele.getBlock().getRelative(0, 1 + (_side / 3), 0);

            b.getWorld().playEffect(b.getLocation(), Effect.STEP_SOUND, Material.LOG_2);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getGame().getPlayers(true)) {
            if (Recharge.canUse(player, "Teleporter")) {
                loop:

                for (Teleporter tele : _teleporters) {
                    if (!player.isSneaking()
                            || !Objects.equals(tele.getBlock(), player.getLocation().getBlock().getRelative(BlockFace.DOWN))) {
                        continue;
                    }

                    if (!getGame().sameTeam(player, tele.getPlayer())) {
                        continue;
                    }

                    ArrayList<Teleporter> teles = getTeleporters(tele.getPlayer());

                    if (teles.size() != 2)
                        continue;

                    for (Teleporter t : teles) {
                        if (t.isUnstable())
                            continue loop;
                    }

                    Recharge.use(player, "Teleporter", 2000);

                    teles.remove(tele);

                    tele = teles.get(0);

                    if (!tele.isValid()) {
                        player.sendMessage(C.Red + "The other teleporter is blocked off!");
                        break;
                    }

                    Location loc = tele.getBlock().getLocation().add(0.5, 1.2, 0.5)
                            .setDirection(player.getLocation().getDirection());

                    UtilPlayer.tele(player, loc);

                    player.getWorld().playEffect(player.getLocation(), Effect.ENDER_SIGNAL, 9);
                    player.playSound(player.getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 3, 0);
                    break;
                }
            }

            for (Teleporter tele : _teleporters) {
                if (!tele.connected())
                    continue;

                Location tele1 = tele.getBlock().getLocation().add(0.5, 1, 0.5);
                Location tele2 = player.getLocation();

                if (Math.abs(tele1.getY() - tele2.getY()) > 1.5 || UtilLoc.getDistance2d(tele1, tele2) > 3.5)
                    continue;

                Teleporter[] tps = getTeleporters(tele.getPlayer()).toArray(new Teleporter[0]);

                tele1 = tps[0].getBlock().getLocation().add(0.5, 1.6, 0.5);
                tele2 = tps[1].getBlock().getLocation().add(0.5, 1.6, 0.5);

                Vector vec = tele2.toVector().subtract(tele1.toVector()).setY(0).normalize();

                Location tele1start = tele1.clone().add(vec);
                Location tele2start = tele2.clone().subtract(vec);

                ParticleColor color = tele.getTeam().getSettings().getParticleColor();

                ArrayList<Location> locs = UtilShapes.drawLineDistanced(tele1, tele1start, 0.15);
                locs.addAll(UtilShapes.drawLineDistanced(tele2, tele2start, 0.15));
                locs.addAll(UtilShapes.drawLineDistanced(tele1start, tele2start, 0.4 + UtilMath.rr(-0.05, 0.05)));

                for (int i = 0; i < locs.size(); i++) {
                    Location l = locs.get(i);

                    if (UtilLoc.getDistance(player.getLocation(), l) > 16)
                        continue;

                    UtilParticle.playParticle(l, color, player);
                }
            }
        }
    }
}
