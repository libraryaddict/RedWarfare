package me.libraryaddict.core.cosmetics.types;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.ScheduledPacket;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.cosmetics.Cosmetic;
import me.libraryaddict.core.player.events.PlayerUnloadEvent;
import me.libraryaddict.core.player.events.PreferenceSetEvent;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;

public class CosmeticCreeperAura extends Cosmetic
{
    private HashMap<Integer, Integer> _auras = new HashMap<Integer, Integer>();

    public CosmeticCreeperAura()
    {
        super("Creepers Aura");
    }

    private PacketContainer createPacket(Player player, int creeper)
    {
        PacketContainer newPacket = ProtocolLibrary.getProtocolManager()
                .createPacketConstructor(PacketType.Play.Server.SPAWN_ENTITY_LIVING, player).createPacket(player);
        newPacket.getIntegers().write(0, creeper);
        newPacket.getIntegers().write(1, (int) EntityType.CREEPER.getTypeId());

        newPacket.getBytes().write(0, (byte) 0);
        newPacket.getBytes().write(1, (byte) 0);

        WrappedDataWatcher dataWatcher = new WrappedDataWatcher();

        dataWatcher.setObject(MetaIndex.ENTITY_META.getIndex(), Registry.get(Byte.class), (byte) 32);
        dataWatcher.setObject(MetaIndex.CREEPER_POWERED.getIndex(), Registry.get(Boolean.class), Boolean.TRUE);
        // dataWatcher.setObject(FlagType.CREEPER_IGNITED.getIndex(), Registry.get(Boolean.class), true);

        newPacket.getDataWatcherModifier().write(0, dataWatcher);

        return newPacket;
    }

    @Override
    public String[] getDescription()
    {
        return new String[]
            {
                    "A creepers aura surrounds you, warning others that you are far and beyond them!"
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemStack(Material.SULPHUR);
    }

    @EventHandler
    public void onToggle(PreferenceSetEvent event)
    {
        if (event.getPreference() != getToggled())
            return;

        Player player = event.getPlayer();

        if (isEnabled(event.getPlayer()))
        {
            int creeper;

            if (!_auras.containsKey(player.getEntityId()))
            {
                _auras.put(player.getEntityId(), creeper = UtilEnt.getNewEntityId());
            }
            else
            {
                creeper = _auras.get(player.getEntityId());
            }

            PacketContainer newPacket = createPacket(player, creeper);

            for (Player p : UtilPlayer.getPlayers())
            {
                if (p == player)
                    continue;

                UtilPlayer.sendPacket(p, newPacket);
            }
        }
        else
        {
            if (!_auras.containsKey(player.getEntityId()))
                return;

            PacketContainer destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

            destroyPacket.getIntegerArrays().write(0, new int[]
                {
                        _auras.get(player.getEntityId())
                });

            for (Player p : UtilPlayer.getPlayers())
            {
                if (p == player)
                    continue;

                UtilPlayer.sendPacket(p, destroyPacket);
            }
        }
    }

    @EventHandler
    public void onUnload(PlayerUnloadEvent event)
    {
        _auras.remove(event.getPlayer().getEntityId());
    }

    @Override
    public void register(CentralManager manager)
    {
        super.register(manager);

        ProtocolLibrary.getProtocolManager()
                .addPacketListener(new PacketAdapter(manager.getPlugin(), PacketType.Play.Server.REL_ENTITY_MOVE,
                        PacketType.Play.Server.REL_ENTITY_MOVE_LOOK, PacketType.Play.Server.ENTITY_TELEPORT,
                        PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.ENTITY_LOOK,
                        PacketType.Play.Server.ENTITY_DESTROY)
                {

                    @Override
                    public void onPacketSending(PacketEvent event)
                    {
                        if (event.getPacketType() == PacketType.Play.Server.ENTITY_DESTROY)
                        {
                            int[] toDestroy = event.getPacket().getIntegerArrays().read(0);

                            for (int i = 0; i < toDestroy.length; i++)
                            {
                                if (_auras.containsKey(toDestroy[i]))
                                {
                                    toDestroy = Arrays.copyOf(toDestroy, toDestroy.length + 1);
                                    toDestroy[toDestroy.length - 1] = _auras.get(toDestroy[i]);
                                }
                            }

                            event.getPacket().getIntegerArrays().write(0, toDestroy);

                            return;
                        }

                        StructureModifier<Integer> ints = event.getPacket().getIntegers();

                        Player mover = UtilPlayer.getPlayer(ints.read(0));

                        if (mover == null || !isEnabled(mover))
                            return;

                        int creeper;

                        if (_auras.containsKey(mover.getEntityId()))
                        {
                            creeper = _auras.get(mover.getEntityId());
                        }
                        else
                        {
                            _auras.put(mover.getEntityId(), creeper = UtilEnt.getNewEntityId());
                        }

                        if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN)
                        {

                            event.schedule(new ScheduledPacket(createPacket(mover, creeper), event.getPlayer(), false));
                        }
                        else
                        {
                            PacketContainer newPacket = event.getPacket().shallowClone();
                            newPacket.getIntegers().write(0, creeper);

                            newPacket.getBytes().write(0, (byte) 0);
                            newPacket.getBytes().write(1, (byte) 0);

                            event.schedule(new ScheduledPacket(newPacket, event.getPlayer(), false));
                        }
                    }
                });
    }

}
