package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.injector.PacketConstructor;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageMod;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

public class JuggernautAbility extends Ability
{
    private DamageMod _juggernaut = DamageMod.ARMOR_ENCHANTS.getSubMod("Juggernaut");
    private PacketListener _packetlistener;

    public JuggernautAbility(JavaPlugin plugin)
    {
        _packetlistener = new PacketAdapter(plugin, PacketType.Play.Server.UPDATE_HEALTH)
        {
            @Override
            public void onPacketSending(PacketEvent event)
            {
                try
                {
                    Player player = event.getPlayer();

                    if (!isLive())
                        return;

                    if (!isAlive(player))
                        return;

                    if (!hasAbility(player))
                        return;

                    PacketContainer packet = event.getPacket();

                    packet.getIntegers().write(0, 6);
                }
                catch (Exception ex)
                {
                    UtilError.handle(ex);
                }
            }
        };
    }

    @EventHandler
    public void onDamage(CustomDamageEvent event)
    {
        if (!event.isPlayerDamagee())
            return;

        if (event.getAttackType().isIgnoreArmor() || event.getDamager() == null)
            return;

        if (!hasAbility(event.getPlayerDamagee()))
            return;

        event.addMultiplier(_juggernaut, 0.6 + (event.getAttackType().isExplosion() ? 0.15 : 0));
    }

    @EventHandler
    public void onSprintToggle(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();

        if (!isLive())
            return;

        if (!isAlive(player))
            return;

        if (!hasAbility(player))
        {
            return;
        }

        if (!player.isSprinting())
        {
            return;
        }

        UtilPlayer.tele(player, player);
    }

    @EventHandler
    public void onVelocity(PlayerVelocityEvent event)
    {
        if (!isLive())
            return;

        if (!isAlive(event.getPlayer()))
            return;

        if (!hasAbility(event.getPlayer()))
            return;

        event.setVelocity(event.getVelocity().multiply(0.8));
    }

    @Override
    public void registerAbility()
    {
        ProtocolLibrary.getProtocolManager().addPacketListener(_packetlistener);

        for (Player player : getPlayers(true))
        {
            player.setMaxHealth(30);
            player.setHealth(30);
        }

        new BukkitRunnable()
        {
            public void run()
            {

                PacketConstructor constructor = ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(PacketType.Play.Server.UPDATE_HEALTH, 0f, 0, 0f);

                for (Player player : getPlayers(true))
                {
                    UtilPlayer.sendPacket(player,
                            constructor.createPacket((float) player.getHealth(), 3, (float) player.getSaturation()));

                    player.teleport(player.getLocation().add(new Location(player.getWorld(), 0, 0.1, 0)));
                    player.setVelocity(new Vector(0, 0.1, 0));
                }
            }
        }.runTaskLater(getPlugin(), 1);
    }

    @Override
    public void unregisterAbility()
    {
        ProtocolLibrary.getProtocolManager().removePacketListener(_packetlistener);

        for (Player player : getPlayers(true))
        {
            player.setFoodLevel(20);
        }
    }
}
