package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumAnimation;
import net.minecraft.server.v1_12_R1.Item;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

public class WraithAbility extends Ability {
    private ArrayList<Pair<Pair<UUID, Integer>, Long>> _arrows = new ArrayList<Pair<Pair<UUID, Integer>, Long>>();
    private ArrayList<Pair<Arrow, Pair<Location, ParticleColor>>> _trail = new ArrayList<Pair<Arrow, Pair<Location,
            ParticleColor>>>();

    /***
     * If player isn't holding a bow that he's aiming
     * @param player
     * @return
     */
    private boolean isInvis(Player player) {
        EntityPlayer p = ((CraftPlayer) player).getHandle();

        if (!p.isHandRaised() || p.cJ() == null || !UtilInv.isHolding(player, EquipmentSlot.HAND, Material.BOW))
            return true;

        Item item = p.getItemInMainHand().getItem();

        return item.f(p.cJ()) != EnumAnimation.BOW;
        // return item.f(p.cA()) != EnumAnimation.BOW ? false : item.e(p.cA()) - p.cB() >= 5;
    }

    @EventHandler
    public void onTick(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getPlayers(true)) {
            if (isInvis(player))
                continue;

            GameTeam team = getGame().getTeam(player);

            if (team == null)
                continue;

            ParticleColor color = team.getSettings().getParticleColor();
            ArrayList<Player> enemies = UtilPlayer.getPlayers();

            enemies.remove(player);

            Player[] array = enemies.toArray(new Player[0]);

            for (int i = 0; i < 5; i++) {
                UtilParticle.playParticle(
                        player.getLocation().add(UtilMath.rr(-0.4, .4), UtilMath.rr(0, 1.9), UtilMath.rr(-0.4, .4)),
                        color, ViewDist.LONG, array);

                UtilParticle.playParticle(
                        player.getLocation().add(UtilMath.rr(-0.4, .4), UtilMath.rr(0, 1.4), UtilMath.rr(-0.4, .4)),
                        color, ViewDist.LONG, player);
            }
        }
    }

    @EventHandler
    public void onSecond(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        tickItem();
        tickArrow();
    }

    private void tickArrow() {
        Iterator<Pair<Arrow, Pair<Location, ParticleColor>>> itel = _trail.iterator();

        while (itel.hasNext()) {
            Pair<Arrow, Pair<Location, ParticleColor>> pair = itel.next();
            Arrow arrow = pair.getKey();

            if (!arrow.isValid() || ((CraftArrow) arrow).getHandle().inGround) {
                itel.remove();
                continue;
            }

            for (Location loc : UtilShapes.drawLineDistanced(pair.getValue().getKey(), arrow.getLocation(), 0.4)) {
                UtilParticle.playParticle(loc, pair.getValue().getValue());
            }

            pair.getValue().setKey(arrow.getLocation());
        }
    }

    private void tickItem() {
        Iterator<Pair<Pair<UUID, Integer>, Long>> itel = _arrows.iterator();

        while (itel.hasNext()) {
            Pair<Pair<UUID, Integer>, Long> pair = itel.next();

            if (!UtilTime.elasped(pair.getValue(), 2000))
                break;

            itel.remove();

            Player player = Bukkit.getPlayer(pair.getKey().getKey());

            if (player == null)
                continue;

            if (!isAlive(player))
                continue;

            if (!hasAbility(player))
                continue;

            player.getInventory().setItem(pair.getKey().getValue(), new ItemStack(Material.ARROW));
        }
    }

    public void registerAbility() {
        for (Player player : getPlayers(true)) {
            player.setExp(1);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onProjectileShoot(EntityShootBowEvent event) {
        if (event.isCancelled())
            return;

        if (!(event.getEntity() instanceof Player))
            return;

        Player player = (Player) event.getEntity();

        if (!hasAbility(player))
            return;

        _trail.add(Pair.of((Arrow) event.getProjectile(), Pair.of(event.getProjectile().getLocation(),
                getGame().getTeam(player).getSettings().getParticleColor())));

        _arrows.add(Pair.of(Pair.of(player.getUniqueId(), player.getInventory().first(Material.ARROW)),
                System.currentTimeMillis()));

        UtilInv.remove(player, Material.ARROW);

        Recharge.use(player, "Wraith", 2000, true);
    }
}
