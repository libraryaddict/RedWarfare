package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilMath;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftIronGolem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

public class IronGolemKillstreak extends StreakBase
{
    private HashMap<Entity, PathfinderGoalMeleeAttack> _melee = new HashMap<Entity, PathfinderGoalMeleeAttack>();
    private Field _timer;

    public IronGolemKillstreak(SearchAndDestroy manager)
    {
        super(manager, "iron golem");
        try
        {
            _timer = PathfinderGoalMeleeAttack.class.getDeclaredField("c");
            _timer.setAccessible(true);
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    @Override
    public Material getFallingMaterial()
    {
        return Material.IRON_BLOCK;
    }

    @Override
    public ItemStack getItem()
    {
        ItemBuilder builder = new ItemBuilder(Material.MONSTER_EGG, 1, EntityType.IRON_GOLEM.getTypeId());
        builder.setTitle("Summon Iron Golem");
        return builder.build();
    }

    @Override
    public int getKillsRequired()
    {
        return 8;
    }

    @EventHandler
    public void onDamageCorrection(CustomDamageEvent event)
    {
        Entity entity = event.getDamager();

        if (entity == null || !(entity instanceof IronGolem))
        {
            return;
        }

        event.setInitDamage("Damage Correction", 13 + UtilMath.r(2));

        event.addRunnable(new DamageRunnable("Iron Golem")
        {

            @Override
            public void run(CustomDamageEvent event2)
            {
                if (!_melee.containsKey(entity))
                {
                    return;
                }

                try
                {
                    PathfinderGoalMeleeAttack goal = _melee.get(entity);

                    _timer.set(goal, 30);
                }
                catch (Exception ex)
                {
                    UtilError.handle(ex);
                }
            }
        });
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamageKnockback(CustomDamageEvent event)
    {
        if (!(event.getDamager() instanceof IronGolem))
            return;

        event.setKnockback(new Vector(0, 1, 0));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(EntityDeathEvent event)
    {
        event.getDrops().clear();
    }

    @Override
    public void onLanded(Player player, Block block)
    {
        IronGolem ironGolem = (IronGolem) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5),
                EntityType.IRON_GOLEM);

        ironGolem.setMaxHealth(40);
        ironGolem.setHealth(20);
        GameTeam team = _manager.getTeam(player);

        team.addToTeam(ironGolem, player);

        ironGolem.setCustomName(team.getColoring() + player.getName() + "'s Iron Golem");
        ironGolem.setCustomNameVisible(true);

        EntityIronGolem golem = ((CraftIronGolem) ironGolem).getHandle();

        try
        {
            Field goalSelector = EntityInsentient.class.getDeclaredField("goalSelector");
            goalSelector.setAccessible(true);

            Field targetSelector = EntityInsentient.class.getDeclaredField("targetSelector");
            targetSelector.setAccessible(true);

            Field b = PathfinderGoalSelector.class.getDeclaredField("b");
            b.setAccessible(true);

            Field c = PathfinderGoalSelector.class.getDeclaredField("c");
            c.setAccessible(true);

            PathfinderGoalSelector goal = (PathfinderGoalSelector) goalSelector.get(golem);
            PathfinderGoalSelector target = (PathfinderGoalSelector) targetSelector.get(golem);
            PathfinderSelector selector = new PathfinderSelector(_manager, team);

            ((LinkedHashSet) b.get(goal)).clear();
            ((LinkedHashSet) c.get(goal)).clear();
            ((LinkedHashSet) b.get(target)).clear();
            ((LinkedHashSet) c.get(target)).clear();

            PathfinderGoalMeleeAttack melee = new PathfinderGoalMeleeAttack(golem, 1.0D, false);
            // melee[1] = new PathfinderGoalMeleeAttack(golem, 1.0D, true);

            _melee.put(ironGolem, melee);

            goal.a(0, new PathfinderGoalFloat(golem));
            goal.a(2, melee);
            goal.a(5, new PathfinderGoalMoveTowardsRestriction(golem, 1.0D));
            goal.a(6, new PathfinderGoalMoveThroughVillage(golem, 1.0D, false));
            goal.a(7, new PathfinderGoalRandomStroll(golem, 1.0D));
            goal.a(8, new PathfinderGoalLookAtPlayer(golem, EntityHuman.class, 8.0F));
            goal.a(8, new PathfinderGoalRandomLookaround(golem));

            target.a(1, new PathfinderGoalHurtByTarget(golem, true));
            target.a(2, new PathfinderGoalNearestAttackableTarget(golem, EntityHuman.class, 0, true, false, selector));
            target.a(2, new PathfinderGoalNearestAttackableTarget(golem, EntityInsentient.class, 0, false, false, selector));
        }
        catch (Exception ex)
        {
            UtilError.handle(ex);
        }
    }

    @EventHandler
    public void onMeleeTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
        {
            return;
        }

        Iterator<Entry<Entity, PathfinderGoalMeleeAttack>> itel = _melee.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<Entity, PathfinderGoalMeleeAttack> entry = itel.next();

            Entity entity = entry.getKey();

            if (!entity.isValid())
            {
                itel.remove();
                continue;
            }
        }
    }

}
