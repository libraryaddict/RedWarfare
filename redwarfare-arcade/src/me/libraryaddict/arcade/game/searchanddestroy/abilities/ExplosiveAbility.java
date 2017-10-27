package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.explosion.CustomExplosion;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import net.minecraft.server.v1_12_R1.EntityArrow;
import net.minecraft.server.v1_12_R1.EntityTippedArrow;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEgg;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Iterator;

public class ExplosiveAbility extends Ability
{
    private ArrayList<Pair<Long, Pair<Entity, Player>>> _grenades = new ArrayList<Pair<Long, Pair<Entity, Player>>>();
    private ArrayList<Arrow> _rpgs = new ArrayList<Arrow>();
    private DamageMod _selfDamage = DamageMod.CUSTOM.getSubMod("Self RPG");
    private int _smokeTick;
    private AttackType GRENADE = new AttackType("Explosive Grenade", "%Killed% was demolished by %Killer%'s grenades")
            .setExplosion();
    private AttackType RPG = new AttackType("Explosive RPG", "%Killed% was caught by %Killer%'s RPG").setExplosion();
    private AttackType RPG_SELF = new AttackType("Explosive RPG-Self", "%Killed% was caught in their own RPG explosion")
            .setExplosion();

    private void explode(Projectile projectile)
    {
        if (!_rpgs.remove(projectile))
        {
            return;
        }

        projectile.remove();

        Player thrower = (Player) projectile.getShooter();

        if (!isLive())
            return;

        if (!isAlive(thrower))
            return;

        new CustomExplosion(projectile.getLocation().subtract(projectile.getLocation().getDirection().normalize().multiply(0.1)),
                6.5F, RPG, RPG_SELF)

        .setDamageBlocks(false)

        .setMaxDamage(20)

        .setDamager(thrower)

        .setIgnoreNonLiving(true)

        .explode();
    }

    @EventHandler
    public void launch(ProjectileLaunchEvent event)
    {
        if (!isLive())
            return;

        if (!(event.getEntity() instanceof Egg))
            return;

        Egg egg = (Egg) event.getEntity();

        if (!(egg.getShooter() instanceof Player))
            return;

        Player shooter = (Player) egg.getShooter();

        if (!isAlive(shooter))
            return;

        egg.remove();

        Location loc = egg.getLocation();
        Vector vec = egg.getVelocity();

        DisguiseAPI.disguiseNextEntity(new MiscDisguise(DisguiseType.EGG));

        EntityArrow eArrow = new EntityTippedArrow(((CraftEgg) egg).getHandle().getWorld(), loc.getX(), loc.getY(), loc.getZ());
        eArrow.fromPlayer = EntityArrow.PickupStatus.DISALLOWED;
        eArrow.shooter = ((CraftPlayer) shooter).getHandle();
        eArrow.getBukkitEntity().setVelocity(vec);

        Arrow arrow = (Arrow) eArrow.getBukkitEntity();
        arrow.setShooter(shooter);

        ((CraftEgg) egg).getHandle().getWorld().addEntity(eArrow, SpawnReason.CUSTOM);

        _rpgs.add(arrow);

        UtilEnt.velocity(shooter, shooter.getLocation().getDirection().normalize().multiply(shooter.isSneaking() ? -0.4 : -0.8),
                true);

        Recharge.use(shooter, "RPG", 30000, true);
    }

    @EventHandler
    public void onArrowDamage(CustomDamageEvent event)
    {
        if (!_rpgs.contains(event.getDamager()))
            return;

        event.setCancelled(true);

        explode((Projectile) event.getDamager());
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(CustomDamageEvent event)
    {
        if (event.getAttackType() != RPG_SELF)
            return;

        Entity damagee = event.getDamagee();

        if (!isAlive(damagee))
            return;

        if (!isLive())
            return;

        event.setDamager(null, null);
        event.addDamage(_selfDamage, 4);
    }

    @EventHandler
    public void onGrenadeTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        _smokeTick++;

        Iterator<Pair<Long, Pair<Entity, Player>>> itel = _grenades.iterator();

        while (itel.hasNext())
        {
            Pair<Long, Pair<Entity, Player>> entry = itel.next();

            Entity entity = entry.getValue().getKey();

            if (!entity.isValid())
            {
                itel.remove();
                continue;
            }

            if (!UtilTime.elasped(entry.getKey(), 3000))
            {
                if (_smokeTick % 5 == 0)
                    UtilParticle.playParticle(ParticleType.SMOKE, entity.getLocation().add(0, 0.5, 0));

                continue;
            }

            itel.remove();
            entity.remove();

            new CustomExplosion(entity.getLocation().add(0, 0.2, 0), isAlive(entry.getValue().getValue()) ? 5.5F : 0, GRENADE)

            .setDamageBlocks(false)

            .setMaxDamage(16)

            .setDamager(entry.getValue().getValue())

            .setIgnoreNonLiving(true)

            .explode();
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!isLive())
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!event.getAction().name().contains("RIGHT"))
            return;

        ItemStack itemStack = event.getItem();

        if (!UtilInv.isItem(itemStack, Material.FIREWORK_CHARGE))
            return;

        long shortest = -1;

        for (int i = 0; i < 5; i++)
        {
            if (!Recharge.canUse(player, "Grenade" + i))
            {
                long timeLeft = Recharge.getTimeLeft(player, "Grenade" + i);

                if (shortest < 0 || timeLeft < shortest)
                {
                    shortest = timeLeft;
                }

                continue;
            }

            shortest = -1;

            Recharge.use(player, "Grenade" + i, 5000);
            break;
        }

        if (shortest > 0)
        {
            player.sendMessage(C.Red + "You cannot throw another grenade for "
                    + UtilNumber.getTime((int) Math.ceil(shortest / 1000D)) + "!");
            return;
        }

        UtilInv.remove(player, itemStack, 1);

        ItemStack item = new ItemBuilder(Material.FIREWORK_CHARGE).setColor(getGame().getTeam(player).getColor())
                .setTitle(System.currentTimeMillis() + "").build();

        Item drop = event.getPlayer().getWorld().dropItem(player.getEyeLocation().subtract(0, 0.2, 0), item);

        Vector vec = player.getLocation().getDirection().normalize().multiply(0.8);

        UtilEnt.velocity(drop, vec, false);

        _grenades.add(Pair.of(System.currentTimeMillis(), Pair.of(drop, player)));
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event)
    {
        explode(event.getEntity());
    }

    @EventHandler
    public void onRPGTick(TimeEvent event)
    {
        if (!isLive())
            return;

        if (event.getType() != TimeType.TICK)
            return;

        Iterator<Arrow> itel = _rpgs.iterator();

        while (itel.hasNext())
        {
            Arrow egg = itel.next();

            if (!egg.isValid())
            {
                itel.remove();
                continue;
            }

            UtilParticle.playParticle(ParticleType.LARGE_CLOUD, egg.getLocation(), ViewDist.LONG);
        }
    }

    @EventHandler
    public void onTime(TimeEvent event)
    {
        if (!isLive())
            return;

        if (event.getType() != TimeType.SEC)
            return;

        for (Player player : getPlayers(true))
        {
            if (!Recharge.canUse(player, "GrenadeGive"))
                continue;

            if (UtilInv.count(player, Material.FIREWORK_CHARGE) >= 20)
                continue;

            Recharge.use(player, "GrenadeGive", 13000);

            UtilInv.addItem(player, new ItemBuilder(Material.FIREWORK_CHARGE).setTitle(C.Yellow + "Grenade")
                    .setColor(getGame().getTeam(player).getColor()).build());
        }
    }

    @EventHandler
    public void preLaunch(PlayerInteractEvent event)
    {
        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (!isLive())
            return;

        if (!UtilInv.isItem(event.getItem(), Material.EGG))
            return;

        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        if (!hasAbility(player))
            return;

        if (Recharge.canUse(player, "RPG") && event.useItemInHand() != Result.DENY)
            return;

        event.setCancelled(true);

        player.updateInventory();
    }

    @Override
    public void registerAbility()
    {
        for (Player player : getPlayers(true))
        {
            Recharge.use(player, "GrenadeGive", 8000);
        }
    }

}
