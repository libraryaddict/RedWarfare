package me.libraryaddict.core.cosmetics.types;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.injector.PacketConstructor;
import com.comphenix.protocol.reflect.StructureModifier;

import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.cosmetics.Cosmetic;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilPlayer;

public class CosmeticBlink extends Cosmetic
{
    private HashMap<Player, Integer[]> _catchup = new HashMap<Player, Integer[]>();
    private PacketConstructor _relMove;
    private PacketConstructor _teleport;

    public CosmeticBlink()
    {
        super("Enderblink");
    }

    private void create()
    {
        if (_relMove != null)
            return;

        _relMove = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.REL_ENTITY_MOVE, 0, 0L, 0L,
                0L, true);
        _teleport = ProtocolLibrary.getProtocolManager().createPacketConstructor(PacketType.Play.Server.ENTITY_TELEPORT,
                UtilPlayer.getPlayers().get(0));
    }

    @Override
    public String[] getDescription()
    {
        return new String[]
            {
                    "Freak out your fellow humans with this super handy trick!",
                    "When you move, you will appear to blink forwards every second!"
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(Material.ENDER_PEARL);
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.SEC)
            return;

        ArrayList<Player> players = UtilPlayer.getPlayers();

        if (players.isEmpty())
            return;

        create();

        for (Player player : _catchup.keySet())
        {
            if (!player.isOnline())
                continue;

            for (Player p : players)
            {
                if (p == player)
                    continue;

                p.hidePlayer(player);
                p.showPlayer(player);
            }

            /* Integer[] toMove = _catchup.get(player);
            
            PacketContainer[] packets = new PacketContainer[8];
            packets[0] = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
            
            packets[0].getIntegers().write(0, player.getEntityId());
            packets[0].getWatchableCollectionModifier().write(0,
                    new ArrayList(Arrays.asList(ReflectionManager.createWatchable(0, (byte) 32))));
            
            packets[1] = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
            packets[1].getIntegers().write(0, player.getEntityId());
            packets[1].getItemModifier().write(0, null);
            packets[1].getItemSlots().write(0, ItemSlot.MAINHAND);
            packets[2] = packets[1].shallowClone();
            packets[2].getItemSlots().write(0, ItemSlot.OFFHAND);
            
            PacketContainer[] delayed = new PacketContainer[3];
            
            delayed[0] = packets[1].shallowClone();
            delayed[0].getItemModifier().write(0, player.getInventory().getItemInMainHand());
            delayed[1] = packets[2].shallowClone();
            delayed[1].getItemModifier().write(0, player.getInventory().getItemInOffHand());
            delayed[2] = packets[0].shallowClone();
            delayed[2].getWatchableCollectionModifier().write(0,
                    new ArrayList(WrappedDataWatcher.getEntityWatcher(player).getWatchableObjects()));
            
            if (toMove[0] >= 32768L || toMove[0] < -32768L || toMove[1] >= 32768L || toMove[1] < -32768L || toMove[2] >= 32768L
                    || toMove[2] < -32768L)
            {
                packets[3] = _teleport.createPacket(player);
            }
            else
            {
                packets[3] = _relMove.createPacket(player.getEntityId(), toMove[0], toMove[1], toMove[2], player.isOnGround());
            }
            
            for (Player p : players)
            {
                if (player == p)
                    continue;
            
                UtilPlayer.sendPacket(p, packets);
            }
            
            new BukkitRunnable()
            {
                public void run()
                {
                    for (Player p : players)
                    {
                        if (player == p)
                            continue;
            
                        UtilPlayer.sendPacket(p, delayed);
                    }
                }
            }.runTaskLater(getManager().getPlugin(), 2);*/
        }

        _catchup.clear();
    }

    @Override
    public void register(CentralManager manager)
    {
        super.register(manager);

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(manager.getPlugin(), PacketType.Play.Server.REL_ENTITY_MOVE,
                        PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_TELEPORT)
                {

                    @Override
                    public void onPacketSending(PacketEvent event)
                    {
                        StructureModifier<Integer> ints = event.getPacket().getIntegers();

                        Player mover = UtilPlayer.getPlayer(ints.read(0));

                        if (mover == null || !isEnabled(mover))
                            return;

                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_TELEPORT)
                        {
                            _catchup.remove(mover);
                            return;
                        }

                        int origX = ints.read(1);
                        int origY = ints.read(2);
                        int origZ = ints.read(3);

                        int newX = (int) (origX * 0.3D);
                        int newY = (int) (origY * 0.3D);
                        int newZ = (int) (origZ * 0.3D);

                        ints.write(1, newX);
                        ints.write(2, newY);
                        ints.write(3, newZ);

                        origX -= newX;
                        origY -= newY;
                        origZ -= newZ;

                        if (origX == 0 && origY == 0 && origZ == 0)
                            return;

                        Integer[] existing;

                        if (!_catchup.containsKey(mover))
                        {
                            _catchup.put(mover, existing = new Integer[]
                                {
                                        0, 0, 0
                                });
                        }
                        else
                        {
                            existing = _catchup.get(mover);
                        }

                        existing[0] += origX;
                        existing[1] += origY;
                        existing[2] += origZ;
                    }
                });
    }

}
