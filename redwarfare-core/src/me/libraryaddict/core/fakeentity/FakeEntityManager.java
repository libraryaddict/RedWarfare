package me.libraryaddict.core.fakeentity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;
import com.comphenix.protocol.wrappers.EnumWrappers.Hand;

import me.libraryaddict.core.fakeentity.EntityInteract.InteractType;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;

public class FakeEntityManager extends MiniPlugin
{
    public static FakeEntityManager fakeEntity;
    private ArrayList<FakeEntity> _activeFakeEntities = new ArrayList<FakeEntity>();

    public FakeEntityManager(JavaPlugin plugin)
    {
        super(plugin, "Fake Entity Manager");

        fakeEntity = this;

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

                            InteractType interactType = packet.getEntityUseActions().read(0) == EntityUseAction.ATTACK
                                    ? InteractType.ATTACK
                                    : packet.getHands().read(0) == Hand.MAIN_HAND ? InteractType.MAIN_INTERACT
                                            : InteractType.OFFHAND_INTERACT;

                            for (FakeEntity fakeEntities : new ArrayList<FakeEntity>(_activeFakeEntities))
                            {
                                fakeEntities.onInteract(player, id, interactType);
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

    void addHologram(FakeEntity hologram)
    {
        _activeFakeEntities.add(hologram);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK || _activeFakeEntities.isEmpty())
            return;

        List<World> worlds = Bukkit.getWorlds();

        Iterator<FakeEntity> fakeEntityItel = _activeFakeEntities.iterator();

        while (fakeEntityItel.hasNext())
        {
            FakeEntity hologram = fakeEntityItel.next();

            if (!worlds.contains(hologram.getLocation().getWorld()))
            {
                fakeEntityItel.remove();
                hologram.stop();
            }
            else
            {
                ArrayList<Player> canSee = hologram.findNearbyPlayers();

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

    protected void removeHologram(FakeEntity hologram)
    {
        _activeFakeEntities.remove(hologram);
    }
}
