package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import java.util.ArrayList;
import java.util.Iterator;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.core.C;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.explosion.CustomExplosion;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilPlayer;

public class NapalmKillstreak extends StreakBase
{
    private ArrayList<Item> _fire = new ArrayList<Item>();
    private int _tick;
    private AttackType NAPALM = new AttackType("Napalm Fire", "%Killed% was charred to a crisp by %Killer%'s napalm").setFire();
    private AttackType NAPALM_EXPLOSION = new AttackType("Napalm Explosion",
            "%Killed% was charred to a crisp by %Killer%'s napalm");

    public NapalmKillstreak(SearchAndDestroy manager)
    {
        super(manager, "Napalm");
    }

    @Override
    public void dropKillstreak(Player player, Location loc)
    {
        super.dropKillstreak(player, loc);

        GameTeam team = _manager.getTeam(player);

        for (Player p : UtilPlayer.getPlayers())
        {
            /*
             * if (!_manager.IsAlive(p) || !UtilPlayer.isSpectator(p)) {
             * continue; }
             */

            if (p.getLocation().distance(player.getLocation()) > 40)
            {
                continue;
            }

            if (team != _manager.getTeam(p))
            {
                p.sendMessage(C.DRed + "Napalm has been unleased in the area by " + player.getName() + "! Get out!");
            }
            else
            {
                p.sendMessage(C.DRed + "Your team released napalm in the area!");
            }
        }
    }

    @Override
    public Material getFallingMaterial()
    {
        return Material.TNT;
    }

    @Override
    public ItemStack getItem()
    {
        return new ItemBuilder(Material.TNT).setTitle(C.DRed + "Napalm").addLore(C.Red + "Use to drop napalm in the area!")
                .build();
    }

    @Override
    public int getKillsRequired()
    {
        return 11;
    }

    @Override
    public boolean getProperBlock()
    {
        return false;
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (event.getAttackType() != NAPALM_EXPLOSION)
            return;

        if (!_manager.sameTeam(event.getDamagee(), event.getDamager()))
            return;

        event.setCancelled(true);
    }

    @Override
    public void onLanded(Player player, Block block)
    {
        Location loc = block.getLocation().add(0.5, 0.5, 0.5);

        loc.getWorld().playSound(loc, Sound.ENTITY_CREEPER_HURT, 3F, 1.2F);

        for (Entity entity : loc.getWorld().getEntities())
        {
            if (!(entity instanceof LivingEntity) || entity.getLocation().distance(loc) > 20)
            {
                continue;
            }

            if (!_manager.isAlive(entity))
                continue;

            if (_manager.sameTeam(player, entity))
                continue;

            int fireTicks = (int) (20 - Math.ceil(entity.getLocation().distance(loc))) * 5;

            if (fireTicks <= 0 || entity.getFireTicks() > fireTicks)
                continue;

            ConditionManager.setFire(entity, NAPALM, player, fireTicks, false);
        }

        player.sendMessage(C.DRed + "Napalm has arrived!");

        CustomExplosion explosion = new CustomExplosion(loc, 9, NAPALM_EXPLOSION);

        explosion.setDamager(player);

        explosion.setDamageBlocks(false);

        explosion.setIgnoreNonLiving(true);

        explosion.explode();

        for (float x = -1; x <= 1; x += 0.25F)
        {
            for (float z = -1; z <= 1; z += 0.25F)
            {
                if ((x * x) + (z * z) > 0.75F)
                    continue;

                ItemBuilder builder = new ItemBuilder(Material.FIREBALL).setTitle("" + System.nanoTime());

                Item item = loc.getWorld().dropItem(loc, builder.build());

                item.setPickupDelay(999999);
                item.setFireTicks(19);
                item.setVelocity(new Vector(x / 1.5, 0.5, z / 1.5));

                _fire.add(item);
            }
        }
    }

    @EventHandler
    public void onUpdate(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
        {
            return;
        }

        _tick++;

        for (Entity entity : _fallingBlocks.keySet())
        {
            entity.setVelocity(new Vector(0, -0.6, 0));

            if (_tick % 5 == 0)
            {
                entity.getWorld().playSound(entity.getLocation(), Sound.ENTITY_CAT_HISS, 2F, 0);
            }
        }

        Iterator<Item> itel = _fire.iterator();

        while (itel.hasNext())
        {
            Item item = itel.next();

            if (_tick % 10 == 0)
            {
                item.setFireTicks(19);
            }

            if (!item.isValid() || item.getLocation().getY() < 0)
            {
                item.remove();
                itel.remove();
                continue;
            }

            item.getWorld().playEffect(item.getLocation(), Effect.EXPLOSION, 0);

            if (item.getVelocity().length() > 0.05)
            {
                continue;
            }

            item.remove();
            itel.remove();

            Block block = item.getLocation().getBlock();

            if (block.getType() != Material.AIR)
            {
                continue;
            }

            block.setType(Material.FIRE);
        }
    }

}
