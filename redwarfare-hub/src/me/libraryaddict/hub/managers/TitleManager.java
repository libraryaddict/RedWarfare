package me.libraryaddict.hub.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;

import me.libraryaddict.core.C;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilMath;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class TitleManager extends MiniPlugin
{
    private HashMap<UUID, HashMap<Integer, Integer[]>> _titles = new HashMap<UUID, HashMap<Integer, Integer[]>>();

    @EventHandler
    public void onQuit(PlayerQuitEvent event)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                _titles.remove(event.getPlayer().getUniqueId());
            }
        }.runTaskLater(getPlugin(), 1);
    }

    public TitleManager(JavaPlugin plugin)
    {
        super(plugin, "Title Manager");

        ProtocolLibrary.getProtocolManager().addPacketListener(
                new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_DESTROY, PacketType.Play.Server.NAMED_ENTITY_SPAWN)
                {
                    @Override
                    public void onPacketSending(PacketEvent event)
                    {
                        UUID uuid = event.getPlayer().getUniqueId();

                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY)
                        {
                            if (!_titles.containsKey(uuid))
                                return;

                            HashMap<Integer, Integer[]> map = _titles.get(uuid);
                            ArrayList<Integer> ids = new ArrayList<Integer>();

                            for (int id : event.getPacket().getIntegerArrays().read(0))
                            {
                                ids.add(id);
                                Integer[] array = map.get(id);

                                if (array == null)
                                    continue;

                                ids.add(array[0]);
                                ids.add(array[1]);
                            }

                            int[] idArray = new int[ids.size()];

                            for (int i = 0; i < ids.size(); i++)
                                idArray[i] = ids.get(i);

                            event.getPacket().getIntegerArrays().write(0, idArray);
                        }
                        else
                        {
                            if (!_titles.containsKey(uuid))
                                _titles.put(uuid, new HashMap<Integer, Integer[]>());

                            HashMap<Integer, Integer[]> map = _titles.get(uuid);
                            int pId = event.getPacket().getIntegers().read(0);

                            if (UtilPlayer.getPlayer(pId) == null)
                                return;

                            Integer[] ids = map.get(pId);

                            if (ids == null)
                            {
                                ids = new Integer[]
                                    {
                                            UtilEnt.getNewEntityId(), UtilEnt.getNewEntityId()
                                    };

                                map.put(pId, ids);
                            }

                            String lineOfText = C.Gold + C.Bold + UtilMath.r(new String[]
                                {
                                        "King", "Prince", "Princess", "Magician", "Wizard", "Jester"
                                });

                            PacketContainer newPacket = ProtocolLibrary.getProtocolManager()
                                    .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_LIVING, event.getPlayer())
                                    .createPacket(event.getPlayer());
                            newPacket.getIntegers().write(0, ids[0]);
                            newPacket.getIntegers().write(1, (int) EntityType.SLIME.getTypeId());

                            WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

                            dataWatcher.setObject(MetaIndex.ENTITY_META.getIndex(), Registry.get(Byte.class), (byte) 32);
                            dataWatcher.setObject(MetaIndex.SLIME_SIZE.getIndex(), Registry.get(Integer.class), 2);

                            newPacket.getDataWatcherModifier().write(0, dataWatcher);

                            PacketContainer newPacket1 = ProtocolLibrary.getProtocolManager()
                                    .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_LIVING, event.getPlayer())
                                    .createPacket(event.getPlayer());
                            newPacket1.getIntegers().write(0, ids[1]);
                            newPacket1.getIntegers().write(1, (int) EntityType.ARMOR_STAND.getTypeId());

                            dataWatcher = new WrappedDataWatcher();

                            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(MetaIndex.ENTITY_META.getIndex(),
                                    Registry.get(Byte.class)), (byte) 32); // Invisibility
                            dataWatcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(
                                    MetaIndex.ARMORSTAND_META.getIndex(), Registry.get(Byte.class)), (byte) 16); // Boundingbox
                                                                                                                // less

                            dataWatcher
                                    .setObject(
                                            new WrappedDataWatcher.WrappedDataWatcherObject(
                                                    MetaIndex.ENTITY_CUSTOM_NAME.getIndex(), Registry.get(String.class)),
                                            lineOfText); // CustomName
                            dataWatcher
                                    .setObject(
                                            new WrappedDataWatcher.WrappedDataWatcherObject(
                                                    MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE.getIndex(), Registry.get(Boolean.class)),
                                            true); // CustomName-Visibility

                            newPacket1.getDataWatcherModifier().write(0, dataWatcher);

                            PacketContainer mount = new PacketContainer(PacketType.Play.Server.MOUNT);
                            mount.getIntegers().write(0, pId);
                            mount.getIntegerArrays().write(0, new int[]
                                {
                                        ids[0]
                                });

                            PacketContainer mount1 = new PacketContainer(PacketType.Play.Server.MOUNT);
                            mount1.getIntegers().write(0, ids[0]);
                            mount1.getIntegerArrays().write(0, new int[]
                                {
                                        ids[1]
                                });

                            new BukkitRunnable()
                            {
                                public void run()
                                {
                                    UtilPlayer.sendPacket(event.getPlayer(), newPacket, newPacket1, mount, mount1);
                                }
                            }.runTaskLater(getPlugin(), 2);
                        }
                    }
                });
    }

}
