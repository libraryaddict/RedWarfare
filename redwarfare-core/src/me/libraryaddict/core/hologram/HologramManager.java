package me.libraryaddict.core.hologram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

import me.libraryaddict.core.hologram.HologramInteract.InteractType;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

public class HologramManager extends MiniPlugin
{
    public static HologramManager hologramManager;
    private ArrayList<Hologram> _activeHolograms = new ArrayList<Hologram>();

    public HologramManager(JavaPlugin plugin)
    {
        super(plugin, "Hologram Manager");

        hologramManager = this;

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                new BukkitRunnable()
                {
                    public void run()
                    {
                        try
                        {
                            Player player = event.getPlayer();

                            PacketContainer packet = event.getPacket();

                            int id = packet.getIntegers().read(0);

                            for (Hologram hologram : _activeHolograms)
                            {
                                if (!hologram.isId(id))
                                    continue;

                                hologram.onInteract(player,
                                        packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK ? InteractType.ATTACK
                                                : packet.getHands().read(0) == Hand.MAIN_HAND ? InteractType.MAIN_INTERACT
                                                        : InteractType.OFFHAND_INTERACT);
                            }
                        }
                        catch (Exception ex)
                        {
                            UtilError.handle(ex);
                        }
                    }
                }.runTask(getPlugin());
            }
        });

    }

    void addHologram(Hologram hologram)
    {
        _activeHolograms.add(hologram);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK || _activeHolograms.isEmpty())
            return;

        List<World> worlds = Bukkit.getWorlds();

        Iterator<Hologram> hologramItel = _activeHolograms.iterator();

        while (hologramItel.hasNext())
        {
            Hologram hologram = hologramItel.next();

            if (!worlds.contains(hologram.getLocation().getWorld()))
            {
                hologramItel.remove();
                hologram.stop();
            }
            else
            {
                if (hologram.getEntityFollowing() != null)
                {
                    Entity following = hologram.getEntityFollowing();

                    if (hologram.isRemoveOnEntityDeath() && !following.isValid())
                    {
                        hologramItel.remove();
                        hologram.stop();
                        continue;
                    }

                    if (!hologram.relativeToEntity.equals(following.getLocation().subtract(hologram.getLocation()).toVector()))
                    {
                        // And we do this so in the rare offchance it changes by a decimal. It doesn't start turning wonky.
                        Vector vec = hologram.relativeToEntity.clone();

                        hologram.setLocation(following.getLocation().add(hologram.relativeToEntity));

                        hologram.relativeToEntity = vec;

                        continue; // No need to do the rest of the code as setLocation does it.
                    }
                }

                ArrayList<Player> canSee = hologram.getNearbyPlayers();

                Iterator<Player> trackedPlayersItel = hologram.getPlayersTracking().iterator();

                while (trackedPlayersItel.hasNext())
                {
                    Player player = trackedPlayersItel.next();

                    if (canSee.contains(player))
                    {
                        continue;
                    }

                    trackedPlayersItel.remove();

                    if (player.getWorld() == hologram.getLocation().getWorld())
                    {
                        UtilPlayer.sendPacket(player, hologram.getDestroyPacket());
                    }
                }

                for (Player player : canSee)
                {
                    if (hologram.getPlayersTracking().contains(player))
                    {
                        continue;
                    }

                    hologram.getPlayersTracking().add(player);

                    hologram.sendSpawnPackets(player);
                }
            }
        }
    }

    void removeHologram(Hologram hologram)
    {
        _activeHolograms.remove(hologram);
    }
}
