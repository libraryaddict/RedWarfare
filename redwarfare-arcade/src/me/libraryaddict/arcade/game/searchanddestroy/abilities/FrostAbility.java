package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.C;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilTime;

public class FrostAbility extends Ability
{
    DamageMod _frostMod = DamageMod.ARMOR.getSubMod("Frost");
    private HashMap<UUID, Long> _frozen = new HashMap<UUID, Long>();

    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (event.getAttackType().isIgnoreArmor())
            return;

        if (!hasAbility(event.getPlayerDamagee()))
            return;

        int items = 0;

        for (ItemStack item : event.getPlayerDamagee().getInventory().getArmorContents())
        {
            if (item == null || item.getType() == Material.AIR)
                continue;

            items++;
        }

        event.addMultiplier(_frostMod, 1 - (0.125 * items));
    }

    @EventHandler
    public void onFreezeDamage(CustomDamageEvent event)
    {
        if (!event.isPlayerDamager())
            return;

        if (!event.isPlayerDamagee())
            return;

        if (!event.getAttackType().isMelee())
            return;

        Player player = event.getPlayerDamager();

        if (!hasAbility(event.getPlayerDamager()))
            return;

        if (!UtilInv.isHolding(event.getPlayerDamager(), EquipmentSlot.HAND, Material.IRON_SWORD))
            return;

        if (!Recharge.canUse(player, "Frost"))
            return;

        event.addRunnable(new DamageRunnable("Freeze")
        {
            public void run(CustomDamageEvent event2)
            {
                Player damagee = event.getPlayerDamagee();

                if (damagee == null || !isAlive(damagee))
                    return;

                Recharge.use(player, "Frost", 16000, true);

                for (int i = 0; i <= 1; i++)
                {
                    damagee.getWorld().playEffect(damagee.getLocation().add(0, i, 0), Effect.STEP_SOUND, Material.ICE);
                }

                _frozen.put(event.getDamagee().getUniqueId(), System.currentTimeMillis());

                damagee.sendMessage(C.Aqua + C.Magic + "ab" + C.Aqua + " Your hands were covered in frost!");
            }
        });
    }

    @EventHandler
    public void onHandSwitch(PlayerSwapHandItemsEvent event)
    {
        if (!isAlive(event.getPlayer()))
            return;

        UUID uuid = event.getPlayer().getUniqueId();

        if (!_frozen.containsKey(uuid))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onHeldItemSwitch(PlayerItemHeldEvent event)
    {
        if (!isAlive(event.getPlayer()))
            return;

        UUID uuid = event.getPlayer().getUniqueId();

        if (!_frozen.containsKey(uuid))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!isAlive(event.getWhoClicked()))
            return;

        UUID uuid = event.getWhoClicked().getUniqueId();

        if (!_frozen.containsKey(uuid))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event)
    {
        if (!isAlive(event.getWhoClicked()))
            return;

        UUID uuid = event.getWhoClicked().getUniqueId();

        if (!_frozen.containsKey(uuid))
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onRightHand(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();

        if (!isAlive(player))
            return;

        UUID uuid = player.getUniqueId();

        if (!_frozen.containsKey(uuid))
            return;

        if (event.getHand() != EquipmentSlot.OFF_HAND)
            return;

        event.setCancelled(true);

        if (event.getItem() == null || event.getItem().getType() == Material.AIR)
            return;

        player.sendMessage(C.Aqua + C.Magic + "ab" + C.Aqua + " Your weaker hand has been frozen!");
    }

    @EventHandler
    public void onThawDamage(CustomDamageEvent event)
    {
        if (!_frozen.containsKey(event.getDamagee().getUniqueId()))
            return;

        if (!event.getAttackType().isFire())
            return;

        Player player = event.getPlayerDamagee();

        if (!isAlive(player))
            return;

        event.addRunnable(new DamageRunnable("Frost Thaw")
        {

            @Override
            public void run(CustomDamageEvent event2)
            {
                _frozen.remove(player.getUniqueId());

                player.sendMessage(C.Red + C.Magic + "ab" + C.Red + " You thawed out");
            }
        });
    }

    @EventHandler
    public void onTickThaw(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        Iterator<Entry<UUID, Long>> itel = _frozen.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<UUID, Long> entry = itel.next();

            if (!UtilTime.elasped(entry.getValue(), 10000))
                continue;

            itel.remove();

            Player player = Bukkit.getPlayer(entry.getKey());

            if (player == null || !isAlive(player))
            {
                continue;
            }

            player.sendMessage(C.Red + C.Magic + "ab" + C.Red + " You thawed out");

            player.playSound(player.getEyeLocation(), Sound.BLOCK_LAVA_POP, 1.0F, 0.0F);
        }
    }
}
