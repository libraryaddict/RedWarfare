package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitDwarf;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitSacrificial;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilParticle.ViewDist;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class SacrificialAbility extends Ability
{
    private HashMap<UUID, Double> _damageTaken = new HashMap<UUID, Double>();
    private HashMap<UUID, ArrayList<UUID>> _sacrificed = new HashMap<UUID, ArrayList<UUID>>();
    private DamageMod _sacrificial = DamageMod.WEAPON_ENCHANTS.getSubMod("Sacrificial");

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDamage(CustomDamageEvent event)
    {
        if (event.getAttackType().isInstantDeath())
            return;

        if (hasAbility(event.getPlayerDamagee()))
            return;

        if (event.getAttackType() == AttackType.VOID)
            return;

        if (!isLive())
            return;

        if (!event.isLivingDamagee())
            return;

        if (!event.isIgnoreRate())
        {
            LivingEntity living = event.getLivingDamagee();

            if (living.getNoDamageTicks() > living.getMaximumNoDamageTicks() / 2.0F
                    && event.getDamage() <= living.getLastDamage() + 0.001)
            {
                return;
            }
        }

        LivingEntity dmgd = event.getLivingDamagee();
        Player sac = null;

        for (Entry<UUID, ArrayList<UUID>> entry : _sacrificed.entrySet())
        {
            if (!entry.getValue().contains(dmgd.getUniqueId()))
                continue;

            sac = Bukkit.getPlayer(entry.getKey());

            if (sac == null)
                continue;

            break;
        }

        if (sac == null)
            return;

        if (!isAlive(sac))
            return;

        event.setCancelled(true);

        UtilParticle.playParticle(ParticleType.BLOCK_CRACK.getParticle(Material.EMERALD_BLOCK, 0),
                dmgd.getLocation().add(0, 1, 0), 0.4, 0.4, 0.4, 7);
        UtilParticle.playParticle(ParticleType.BLOCK_CRACK.getParticle(Material.LAPIS_BLOCK, 0), sac.getLocation().add(0, 1, 0),
                0.4, 0.4, 0.4, 7);

        boolean longPart = Recharge.canUse(sac, "LongParticles");

        if (longPart)
        {
            Recharge.use(sac, "LongParticles", 3000);
        }

        int b = 0;

        if (UtilLoc.getDistance(dmgd, sac) > 1)
        {
            Location loc1 = dmgd.getEyeLocation().subtract(0, 0.3, 0);
            Location loc2 = sac.getEyeLocation().subtract(0, 0.3, 0);

            Vector vec = UtilLoc.getDirection(loc1, loc2).multiply(0.4);
            loc1.add(vec);
            loc2.subtract(vec);

            for (Location loc : UtilShapes.drawLineDistanced(loc1, loc2, 0.4D))
            {
                if ((!longPart || b++ % 2 != 0) && UtilLoc.getDistance(loc1, loc) > 4 && UtilLoc.getDistance(loc2, loc) > 4)
                    continue;

                UtilParticle.playParticle(ParticleType.RED_DUST, loc, ViewDist.SHORT);
            }
        }

        UtilEnt.velocity(dmgd, event.getFinalKnockback(), false);
        ConditionManager.addFall(dmgd, event.getFinalDamager());

        dmgd.setNoDamageTicks(dmgd.getMaximumNoDamageTicks());
        dmgd.setLastDamage(event.getInitialDamage());

        ((CraftLivingEntity) dmgd).getHandle().aH = 1.5F;
        ((CraftLivingEntity) dmgd).getHandle().hurtTicks = 10;

        getManager().getDamage().playDamage(dmgd);

        AttackType attackType = new AttackType("Sacrificial",
                "%Killed% was killed protecting " + getGame().getTeam(dmgd).getColoring() + UtilEnt.getName(dmgd))
                        .cloneData(event.getAttackType()).setNoKnockback();

        CustomDamageEvent damageEvent = getManager().getDamage().createEvent(sac, attackType,
                event.getDamage(DamageMod.ARMOR, DamageMod.ARMOR_ENCHANTS), event.getDamager(), event.getFinalDamager(), true);

        damageEvent.setIgnoreConditions(true);

        if (event.getFinalKnockback().length() > 0)
            damageEvent.setKnockback(event.getFinalKnockback().clone().normalize().multiply(0.2));

        damageEvent.addRunnables(event.getRunnables());

        getManager().getDamage().callDamage(damageEvent);

        _damageTaken.put(sac.getUniqueId(), _damageTaken.getOrDefault(sac.getUniqueId(), 0D) + event.getDamage());
    }

    @EventHandler
    public void onDeath(DeathEvent event)
    {
        UUID uuid = event.getPlayer().getUniqueId();

        if (_sacrificed.containsKey(uuid))
        {
            for (UUID id : _sacrificed.remove(uuid))
            {
                Player player = Bukkit.getPlayer(id);

                if (player == null)
                    continue;

                player.sendMessage(C.Blue + "Your bond with " + event.getPlayer().getName() + C.Blue + " was severed!");
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 0F);
            }
        }
        else
        {
            for (Entry<UUID, ArrayList<UUID>> entry : _sacrificed.entrySet())
            {
                if (!entry.getValue().contains(uuid))
                {
                    continue;
                }

                Player player = Bukkit.getPlayer(entry.getKey());

                entry.getValue().remove(uuid);

                if (player == null)
                    continue;

                player.sendMessage(C.Blue + "Your bond with " + event.getPlayer().getName() + " was destroyed!");
                player.playSound(player.getLocation(), Sound.ENTITY_GHAST_SCREAM, 1, 0F);

                player.setLevel(entry.getValue().size());
            }
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        if (!isLive())
            return;

        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!UtilInv.isHolding(player, event.getHand(), Material.INK_SACK))
            return;

        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        if (!getGame().sameTeam(player, event.getRightClicked()))
        {
            player.sendMessage(C.Red + "They are not on your team! Run away!");
            return;
        }

        if (!isAlive(event.getRightClicked()))
            return;

        Kit kit = (event.getRightClicked() instanceof Player) ? getGame().getKit((Player) event.getRightClicked()) : null;

        if (kit instanceof KitSacrificial || kit instanceof KitDwarf)
        {
            player.sendMessage(C.Blue + "You cannot sacrifice a " + kit.getName() + "!");
            return;
        }

        UUID uuid = player.getUniqueId();
        UUID claim = event.getRightClicked().getUniqueId();

        if (_sacrificed.containsKey(uuid))
        {
            if (_sacrificed.get(uuid).contains(claim))
            {
                player.sendMessage(C.Blue + "You've already claimed this warrior!");
                return;
            }
        }

        for (ArrayList<UUID> list : _sacrificed.values())
        {
            if (list.contains(claim))
            {
                player.sendMessage(C.Blue + "This warrior has already been claimed by another sacrificial!");
                return;
            }
        }

        if (!_sacrificed.containsKey(uuid))
        {
            _sacrificed.put(uuid, new ArrayList<UUID>());
        }

        _sacrificed.get(uuid).add(claim);

        player.setLevel(_sacrificed.get(uuid).size());

        player.sendMessage(C.Blue + "You have cast sacrifice on " + UtilEnt.getName(event.getRightClicked()));
        event.getRightClicked().sendMessage(C.Blue + player.getName() + " has cast sacrifice on you!");
    }

    @EventHandler
    public void onGameStateEnd(GameStateEvent event)
    {
        if (event.getState() != GameState.Dead)
        {
            for (UUID uuid : _damageTaken.keySet())
            {
                int healed = (int) Math.floor(_damageTaken.get(uuid));

                if (healed <= 0)
                    continue;

                Stats.add(uuid, "Game." + getGame().getName() + "." + getKit().getName() + ".Sacrificed", healed);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!isLive())
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        if (!UtilInv.isItem(event.getItem(), Material.SHEARS))
            return;

        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        if (!event.getAction().name().contains("RIGHT"))
            return;

        UUID uuid = player.getUniqueId();

        if (!_sacrificed.containsKey(uuid) || _sacrificed.get(uuid).isEmpty())
        {
            player.sendMessage(C.Red + "You do not have any bonds to sever!");
            return;
        }

        player.setLevel(0);

        for (UUID id : _sacrificed.get(uuid))
        {
            Player p = Bukkit.getPlayer(id);

            if (p == null)
                continue;

            player.sendMessage(C.Blue + "Snipped the sacrificial bond with " + p.getName() + "!");
            player.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 0F);

            p.sendMessage(C.Blue + "Your bond with " + player.getName() + " was snipped!");
            p.playSound(player.getLocation(), Sound.ENTITY_SHEEP_SHEAR, 1, 0F);
        }

        _sacrificed.remove(uuid);

        UtilInv.remove(player, Material.SHEARS, 1);
    }

    @EventHandler
    public void onSacDamage(CustomDamageEvent event)
    {
        if (!isLive())
            return;

        if (event.getAttackType().getName().equals("Sacrificial"))
            return;

        if (event.getDamager() == null)
            return;

        if (!event.isPlayerDamagee())
            return;

        Player player = event.getPlayerDamagee();

        if (!isAlive(player))
            return;

        if (!hasAbility(player))
            return;

        event.addMultiplier(_sacrificial, 4);
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        for (Player player : getPlayers(true))
        {
            if (!Recharge.canUse(player, "SacSteak"))
                continue;

            UUID uuid = player.getUniqueId();

            int amount = UtilInv.count(player, Material.COOKED_BEEF);

            if (amount > 4 && (amount >= 20 || !_sacrificed.containsKey(uuid) || _sacrificed.get(uuid).isEmpty()))
                continue;

            amount++;

            UtilInv.addItem(player, new ItemStack(Material.COOKED_BEEF));

            Recharge.use(player, "SacSteak", 20000, true);

            if (!_sacrificed.containsKey(uuid))
                continue;

            for (UUID id : _sacrificed.get(uuid))
            {
                Player p = Bukkit.getPlayer(id);

                if (p == null)
                    continue;

                p.sendMessage(C.Red + "The sacrificial sacrificing you has " + amount + " steak" + (amount == 1 ? "" : "s"));

                if (amount < 5)
                {
                    p.sendMessage(C.Red + "May want to ease up on the fighting warrior!");
                }
            }
        }
    }
}
