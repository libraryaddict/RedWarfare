package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.killstreak.StreakBase;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.server.v1_12_R1.EntityCreature;
import net.minecraft.server.v1_12_R1.NavigationAbstract;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftCreature;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WolvesKillstreak extends StreakBase
{
    private HashMap<Player, ArrayList<Wolf>> _wolfMap = new HashMap<Player, ArrayList<Wolf>>();

    public WolvesKillstreak(SearchAndDestroy manager)
    {
        super(manager, "attack dogs");
    }

    @EventHandler
    public void CubTargetCancel(EntityTargetEvent event)
    {
        if (!_wolfMap.containsKey(event.getTarget()))
            return;

        if (_wolfMap.get(event.getTarget()).contains(event.getEntity()))
            event.setCancelled(true);
    }

    @EventHandler
    public void CubUpdate(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        for (Player player : _wolfMap.keySet())
        {
            Iterator<Wolf> wolfIterator = _wolfMap.get(player).iterator();

            while (wolfIterator.hasNext())
            {
                Wolf wolf = wolfIterator.next();

                // Dead
                if (!wolf.isValid())
                {
                    wolfIterator.remove();
                    continue;
                }

                if (wolf.getTarget() != null)
                {
                    if (!isValidTarget(wolf, wolf.getTarget()))
                    {
                        wolf.setTarget(null);
                    }
                }

                if (wolf.getTarget() == null)
                {
                    Entity closest = null;
                    double dist = 0;

                    for (Entity entity : wolf.getNearbyEntities(14, 14, 14))
                    {
                        if (!isValidTarget(wolf, entity))
                            continue;

                        if (closest == null || dist > wolf.getLocation().distance(entity.getLocation()))
                        {
                            closest = entity;
                            dist = wolf.getLocation().distance(entity.getLocation());
                        }
                    }

                    if (closest != null)
                    {
                        wolf.setTarget((LivingEntity) closest);
                    }
                }

                // Return to Owner
                double range = 1.5;

                if (wolf.getTarget() != null)
                    range = 16;

                Location target = player.getLocation().add(player.getLocation().getDirection().multiply(3));
                target.setY(player.getLocation().getY());

                if (UtilLoc.getDistance(wolf.getLocation(), target) > range)
                {
                    float speed = 1f;
                    if (player.isSprinting())
                        speed = 1.4f;

                    // Move
                    EntityCreature ec = ((CraftCreature) wolf).getHandle();
                    NavigationAbstract nav = ec.getNavigation();
                    nav.a(target.getX(), target.getY(), target.getZ(), speed);

                    wolf.setTarget(null);
                }
            }
        }
    }

    @EventHandler
    public void Damage(CustomDamageEvent event)
    {
        if (!(event.getDamager() instanceof Wolf) || !isWolf(event.getDamager()))
            return;

        event.setInitDamage("Damage Correction", 6);
    }

    @Override
    public Material getFallingMaterial()
    {
        return Material.WOOL;
    }

    @Override
    public ItemStack getItem()
    {
        ItemBuilder builder = new ItemBuilder(Material.MONSTER_EGG, 1, EntityType.WOLF.getTypeId());
        builder.setTitle("Summon attack dogs");
        return builder.build();
    }

    @Override
    public int getKillsRequired()
    {
        return 4;
    }

    private boolean isValidTarget(Wolf wolf, Entity entity)
    {
        if (!(entity instanceof LivingEntity))
        {
            return false;
        }

        if (UtilLoc.getDistance(wolf, entity) > 14)
            return false;

        if (_manager.sameTeam(entity, wolf))
            return false;

        if (!_manager.isAlive(entity))
            return false;

        if (!wolf.hasLineOfSight(entity))
        {
            return false;
        }

        if (entity instanceof Player)
        {
            Player p = (Player) entity;
            Kit kit = _manager.getKit(p);

            if ((kit instanceof KitGhost || kit instanceof KitWraith) && !p.isSprinting() && UtilPlayer.getArrowsInBody(p) <= 0
                    && !UtilInv.isHoldingItem(p))
            {
                return false;
            }
        }

        return true;
    }

    public boolean isWolf(Entity ent)
    {
        for (ArrayList<Wolf> minions : _wolfMap.values())
        {
            for (Wolf minion : minions)
            {
                if (ent.equals(minion))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onLanded(Player player, Block block)
    {
        Location loc = block.getLocation().add(0.5, 0, 0.5);

        if (!_wolfMap.containsKey(player))
            _wolfMap.put(player, new ArrayList<Wolf>());

        for (int i = 0; i < 3; i++)
        {
            Wolf wolf = loc.getWorld().spawn(loc.clone().add(UtilMath.rr(-0.4, 0.4), 0, UtilMath.rr(-0.4, 0.4)), Wolf.class);

            GameTeam team = _manager.getTeam(player);

            wolf.setOwner(player);
            wolf.setCollarColor(team.getSettings().getDyeColor());
            wolf.playEffect(EntityEffect.WOLF_HEARTS);
            wolf.setAngry(true);
            wolf.setMaxHealth(14);
            wolf.setHealth(wolf.getMaxHealth());

            wolf.setCustomName(team.getColoring() + player.getName() + "'s Wolf");
            wolf.setCustomNameVisible(true);

            team.addToTeam(wolf, player);

            _wolfMap.get(player).add(wolf);
        }

        player.sendMessage(C.Blue + "Attack dogs have arrived!");
    }

    @EventHandler
    public void PlayerDeath(DeathEvent event)
    {
        ArrayList<Wolf> wolves = _wolfMap.remove(event.getPlayer());

        if (wolves == null)
            return;

        for (Wolf wolf : wolves)
            wolf.remove();

        wolves.clear();
    }
}
