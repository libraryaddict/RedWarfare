package me.libraryaddict.core.fakeentity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.fakeentity.EntityInteract.InteractType;
import me.libraryaddict.core.hologram.Hologram.HologramTarget;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class FakeEntity
{
    private PacketContainer _destroyPacket;
    private ArrayList<PacketContainer> _extraPackets = new ArrayList<PacketContainer>();
    private FakeEntityData[] _fakeData = new FakeEntityData[0];
    private boolean _isInUse;
    private Location _location;
    private boolean _makeSpawnPackets = true;
    private HashSet<String> _playersInList = new HashSet<String>();
    private ArrayList<Player> _playersTracking = new ArrayList<Player>();
    private ArrayList<PacketContainer> _spawnDelayedPackets = new ArrayList<PacketContainer>();
    private ArrayList<PacketContainer> _spawnPackets = new ArrayList<PacketContainer>();
    private HologramTarget _target = HologramTarget.BLACKLIST;
    private int _viewDistance = 70;

    public FakeEntity(Location location)
    {
        _location = location.clone();
    }

    public FakeEntity(Location location, EntityType entityType)
    {
        this(location, entityType, 0);
    }

    public FakeEntity(Location location, EntityType entityType, int data)
    {
        _location = location.clone();

        addFakeData(new FakeEntityData(entityType, new Location(null, 0, 0, 0)));

        if (entityType.isAlive())
        {
            setMetadata(MetaIndex.LIVING_HEALTH, 1F);
            setMetadata(MetaIndex.ENTITY_NO_GRAVITY, true);
        }
    }

    public FakeEntity(Location location, FakeEntityData... fakeEntity)
    {
        _location = location.clone();

        addFakeData(fakeEntity);

        for (FakeEntityData entity : fakeEntity)
        {
            if (entity.getEntityType().isAlive())
            {
                entity.setMetadata(MetaIndex.LIVING_HEALTH, 1F);
                entity.setMetadata(MetaIndex.ENTITY_NO_GRAVITY, true);
            }
        }
    }

    public FakeEntity addExtraSpawnPacket(PacketContainer... packets)
    {
        _extraPackets.addAll(Arrays.asList(packets));

        return this;
    }

    public void addFakeData(FakeEntityData... fakeData)
    {
        _fakeData = Arrays.copyOf(_fakeData, _fakeData.length + fakeData.length);

        for (int i = 0; i < fakeData.length; i++)
        {
            _fakeData[(_fakeData.length - fakeData.length) + i] = fakeData[i];
            fakeData[i].setFakeEntity(this);
        }
    }

    /**
     * Adds the player to the Hologram to be effected by Whitelist or Blacklist
     */
    public FakeEntity addPlayer(Player player)
    {
        return addPlayer(player.getName());
    }

    /**
     * Adds the player to the Hologram to be effected by Whitelist or Blacklist
     */
    public FakeEntity addPlayer(String player)
    {
        _playersInList.add(player);
        return this;
    }

    public FakeEntity addPlayers(ArrayList<Player> players)
    {
        for (Player player : players)
        {
            addPlayer(player);
        }

        return this;
    }

    private PacketContainer constructSpawnPacket(FakeEntityData fakeData)
    {
        DisguiseType disguise = fakeData.getDisguise();

        PacketContainer packet;

        if (disguise.isMob())
        {
            packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        }
        else
        {
            packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY);
        }

        StructureModifier<Integer> ints = packet.getIntegers();

        ints.write(0, fakeData.getEntityId());

        packet.getUUIDs().write(0, UUID.randomUUID());

        StructureModifier<Double> doubles = packet.getDoubles();

        Location fakeLoc = fakeData.getLocation();

        doubles.write(0, getLocation().getX() + fakeLoc.getX());
        doubles.write(1, getLocation().getY() + fakeLoc.getY());
        doubles.write(2, getLocation().getZ() + fakeLoc.getZ());

        if (fakeLoc.getWorld() == null)
            fakeLoc = _location;

        if (disguise.isMob())
        {
            ints.write(1, (int) fakeData.getEntityType().getTypeId());

            StructureModifier<Byte> bytes = packet.getBytes();

            bytes.write(0, getRotation(fakeLoc.getYaw()));
            bytes.write(1, getRotation(fakeLoc.getPitch()));
            bytes.write(2, getRotation(fakeLoc.getYaw()));

            WrappedDataWatcher watcher = new WrappedDataWatcher();

            for (WrappedWatchableObject watchable : fakeData.getWatchables())
            {
                WrappedDataWatcherObject obj = new WrappedDataWatcherObject(watchable.getIndex(),
                        Registry.get(watchable.getValue().getClass()));

                if (Registry.get(watchable.getValue().getClass()) == null)
                    continue;

                watcher.setObject(obj, watchable.getValue());
            }

            packet.getDataWatcherModifier().write(0, watcher);
        }
        else
        {
            ints.write(4, (int) getRotation(fakeLoc.getYaw()));
            ints.write(5, (int) getRotation(fakeLoc.getPitch()));

            ints.write(6, disguise.getObjectId());
            ints.write(7, fakeData.getData());
        }

        return packet;
    }

    /**
     * Is there a player entry in the hologram for Whitelist and Blacklist
     */
    public boolean containsPlayer(Player player)
    {
        return _playersInList.contains(player.getName());
    }

    /**
     * Is there a player entry in the hologram for Whitelist and Blacklist
     */
    public boolean containsPlayer(String player)
    {
        return _playersInList.contains(player);
    }

    public ArrayList<Player> findNearbyPlayers()
    {
        ArrayList<Player> nearbyPlayers = new ArrayList<Player>();

        for (Player player : getLocation().getWorld().getPlayers())
        {
            if (isVisible(player))
            {
                nearbyPlayers.add(player);
            }
        }

        return nearbyPlayers;
    }

    public FakeEntityData[] getData()
    {
        return _fakeData;
    }

    public PacketContainer getDestroyPacket()
    {
        if (_destroyPacket == null)
        {
            makeDestroyPacket();
        }

        return _destroyPacket;
    }

    /**
     * Get who can see the hologram
     *
     * @Whitelist = Only people added can see the hologram
     * @Blacklist = Anyone but people added can see the hologram
     */
    public HologramTarget getHologramTarget()
    {
        return _target;
    }

    /**
     * Get the hologram location
     */
    public Location getLocation()
    {
        return _location.clone();
    }

    private long getLong(double d)
    {
        return Math.round(d * 4096);
    }

    public ArrayList<Player> getPlayersTracking()
    {
        return _playersTracking;
    }

    protected byte getRotation(double rotation)
    {
        return (byte) (rotation * 256.0F / 360.0F);
    }

    /**
     * Get the view distance the hologram is viewable from. Default is 70
     */
    public int getViewDistance()
    {
        return _viewDistance;
    }

    /**
     * Is the hologram holograming?
     */
    public boolean isInUse()
    {
        return _isInUse;
    }

    private boolean isValid(long number)
    {
        return number <= 32768L && number >= -32768L;
    }

    public boolean isVisible(Player player)
    {
        if (getLocation().getWorld() == player.getWorld())
        {
            if ((getHologramTarget() == HologramTarget.WHITELIST) == containsPlayer(player))
            {
                if (getLocation().distanceSquared(player.getLocation()) < getViewDistance() * getViewDistance())
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void makeDestroyPacket()
    {
        _destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        int[] ids = new int[getData().length];

        for (int i = 0; i < getData().length; i++)
        {
            ids[i] = getData()[i].getEntityId();
        }

        _destroyPacket.getIntegerArrays().write(0, ids);
    }

    private void makeSpawnPackets()
    {
        _spawnPackets.clear();
        _spawnDelayedPackets.clear();

        for (FakeEntityData fakeData : getData())
        {
            _spawnPackets.add(constructSpawnPacket(fakeData));

            for (Entry<EquipmentSlot, ItemStack> pair : fakeData.getItems().entrySet())
            {
                _spawnPackets.add(fakeData.getEquip(pair.getKey(), pair.getValue()));
            }
        }
    }

    public void onInteract(Player player, int entityId, InteractType interactType)
    {
        for (FakeEntityData fakeData : getData())
        {
            if (!fakeData.isId(entityId))
                continue;

            if (fakeData.getInteract() != null)
            {
                fakeData.getInteract().onInteract(player, interactType);
            }
            break;
        }
    }

    /**
     * Removes the player from the Hologram so they are no longer effected by Whitelist or Blacklist
     */
    public FakeEntity removePlayer(Player player)
    {
        return removePlayer(player.getName());
    }

    /**
     * Removes the player from the Hologram so they are no longer effected by Whitelist or Blacklist
     */
    public FakeEntity removePlayer(String player)
    {
        _playersInList.remove(player);

        return this;
    }

    public FakeEntity sendSpawnPackets(Player... players)
    {
        if (_makeSpawnPackets)
        {
            makeSpawnPackets();
            _makeSpawnPackets = false;
        }

        for (Player player : players)
        {
            UtilPlayer.sendPacket(player, _spawnPackets);
        }

        new BukkitRunnable()
        {
            public void run()
            {
                for (Player player : players)
                {
                    UtilPlayer.sendPacket(player, _spawnDelayedPackets);
                    UtilPlayer.sendPacket(player, _extraPackets);
                }
            }
        }.runTaskLater(FakeEntityManager.fakeEntity.getPlugin(), 2);

        return this;
    }

    public FakeEntity setArmor(ItemStack[] armor)
    {
        assert armor.length == 4;

        setItem(EquipmentSlot.HEAD, armor[3]);
        setItem(EquipmentSlot.CHEST, armor[2]);
        setItem(EquipmentSlot.LEGS, armor[1]);
        setItem(EquipmentSlot.FEET, armor[0]);

        return this;
    }

    /**
     * Set who can see the hologram
     *
     * @Whitelist = Only people added can see the hologram
     * @Blacklist = Anyone but people added can see the hologram
     */
    public FakeEntity setHologramTarget(HologramTarget newTarget)
    {
        _target = newTarget;

        if (isInUse())
        {
            stop();
            start();
        }

        return this;
    }

    public FakeEntity setInteract(EntityInteract hologramInteract)
    {
        for (FakeEntityData fakeData : getData())
            fakeData.setInteract(hologramInteract);

        return this;
    }

    public FakeEntity setItem(EquipmentSlot slot, ItemStack itemStack)
    {
        _makeSpawnPackets = true;

        for (FakeEntityData fakeData : getData())
        {
            ItemStack previous;

            if (itemStack == null)
            {
                previous = fakeData.getItems().remove(slot);
            }
            else
            {
                previous = fakeData.getItems().put(slot, itemStack);
            }

            if (isInUse() && (previous != null || itemStack != null))
            {
                PacketContainer packet = fakeData.getEquip(slot, itemStack);

                for (Player player : getPlayersTracking())
                {
                    UtilPlayer.sendPacket(player, packet);
                }
            }
        }

        return this;
    }

    /**
     * Sets the hologram to appear at this location
     */
    public FakeEntity setLocation(Location newLocation)
    {
        _makeSpawnPackets = true;

        Location oldLocation = getLocation();
        _location = newLocation.clone();

        if (isInUse())
        {
            ArrayList<Player> canSee = findNearbyPlayers();

            Iterator<Player> itel = getPlayersTracking().iterator();

            while (itel.hasNext())
            {
                Player player = itel.next();

                if (!canSee.contains(player))
                {
                    itel.remove();

                    if (player.getWorld() == getLocation().getWorld())
                    {
                        UtilPlayer.sendPacket(player, getDestroyPacket());
                    }
                }
            }

            itel = canSee.iterator();

            while (itel.hasNext())
            {
                Player player = itel.next();

                if (!getPlayersTracking().contains(player))
                {
                    getPlayersTracking().add(player);
                    itel.remove();

                    sendSpawnPackets(player);
                }
            }

            if (!canSee.isEmpty())
            {
                ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();

                Vector vec = getLocation().toVector().subtract(oldLocation.toVector());

                long x = getLong(vec.getX());
                long y = getLong(vec.getY());
                long z = getLong(vec.getZ());

                if (isValid(x) && isValid(y) && isValid(z))
                {
                    for (FakeEntityData fakeData : getData())
                    {
                        if (!fakeData.isSendMovement())
                            continue;

                        Location rel = fakeData.getLocation();

                        if (rel.getWorld() == null)
                            rel = _location;

                        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

                        StructureModifier<Integer> ints = packet.getIntegers();

                        ints.write(0, fakeData.getEntityId());
                        ints.write(1, (int) x);
                        ints.write(2, (int) y);
                        ints.write(3, (int) z);

                        StructureModifier<Byte> bytes = packet.getBytes();

                        bytes.write(0, getRotation(rel.getYaw()));
                        bytes.write(1, getRotation(rel.getPitch()));

                        packets.add(packet);
                    }
                }
                else
                {
                    for (FakeEntityData fakeData : getData())
                    {
                        if (!fakeData.isSendMovement())
                            continue;

                        Location rel = fakeData.getLocation();

                        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

                        StructureModifier<Double> doubles = packet.getDoubles();

                        packet.getIntegers().write(0, fakeData.getEntityId());
                        doubles.write(0, getLocation().getX() + rel.getX());
                        doubles.write(1, getLocation().getY() + rel.getY());
                        doubles.write(2, getLocation().getZ() + rel.getZ());

                        StructureModifier<Byte> bytes = packet.getBytes();

                        if (rel.getWorld() == null)
                            rel = _location;

                        bytes.write(0, getRotation(rel.getYaw()));
                        bytes.write(1, getRotation(rel.getPitch()));

                        packets.add(packet);
                    }
                }

                for (Player player : canSee)
                {
                    UtilPlayer.sendPacket(player, packets);
                }
            }
        }

        return this;
    }

    /**
     * Sets the hologram to appear at this location
     */
    public FakeEntity setLocation(FakeEntityData fakeData, Location newRelLocation)
    {
        _makeSpawnPackets = true;

        Location oldLocation = fakeData.getLocation();
        Location current = newRelLocation.clone();

        if (isInUse())
        {
            ArrayList<Player> canSee = findNearbyPlayers();

            if (!canSee.isEmpty())
            {
                ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();

                Vector vec = current.toVector().subtract(oldLocation.toVector());

                long x = getLong(vec.getX());
                long y = getLong(vec.getY());
                long z = getLong(vec.getZ());

                if (isValid(x) && isValid(y) && isValid(z))
                {
                    Location rel = fakeData.getLocation();

                    if (rel.getWorld() == null)
                        rel = _location;

                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);

                    StructureModifier<Integer> ints = packet.getIntegers();

                    ints.write(0, fakeData.getEntityId());
                    ints.write(1, (int) x);
                    ints.write(2, (int) y);
                    ints.write(3, (int) z);

                    StructureModifier<Byte> bytes = packet.getBytes();

                    bytes.write(0, getRotation(rel.getYaw()));
                    bytes.write(1, getRotation(rel.getPitch()));

                    packets.add(packet);
                }
                else
                {
                    Location rel = fakeData.getLocation();

                    PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);

                    StructureModifier<Double> doubles = packet.getDoubles();

                    packet.getIntegers().write(0, fakeData.getEntityId());
                    doubles.write(0, getLocation().getX() + rel.getX());
                    doubles.write(1, getLocation().getY() + rel.getY());
                    doubles.write(2, getLocation().getZ() + rel.getZ());

                    StructureModifier<Byte> bytes = packet.getBytes();

                    if (rel.getWorld() == null)
                        rel = _location;

                    bytes.write(0, getRotation(rel.getYaw()));
                    bytes.write(1, getRotation(rel.getPitch()));

                    packets.add(packet);
                }

                for (Player player : canSee)
                {
                    UtilPlayer.sendPacket(player, packets);
                }
            }
        }

        return this;
    }

    public FakeEntity setMetadata(FakeEntityData fakeData, MetaIndex MetaIndex, Object value)
    {
        return setMetadata(fakeData, Pair.of(MetaIndex, value));
    }

    public FakeEntity setMetadata(FakeEntityData fakeData, Pair<MetaIndex, Object>... flags)
    {
        _makeSpawnPackets = true;

        fakeData.setInternalMetadata(flags);

        if (isInUse() && fakeData.getEntityType().isAlive())
        {
            PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

            packetContainer.getIntegers().write(0, fakeData.getEntityId());

            packetContainer.getWatchableCollectionModifier().write(0, fakeData.getWatchables());

            for (Player player : getPlayersTracking())
            {
                UtilPlayer.sendPacket(player, packetContainer);
            }
        }

        return this;
    }

    public <Y> FakeEntity setMetadata(MetaIndex<Y> type, Y object)
    {
        setMetadata(Pair.of(type, object));

        return this;
    }

    public FakeEntity setMetadata(Pair<MetaIndex, Object>... flags)
    {
        _makeSpawnPackets = true;

        for (FakeEntityData fakeData : getData())
        {
            fakeData.setMetadata(flags);

            if (isInUse() && fakeData.getEntityType().isAlive())
            {
                PacketContainer packetContainer = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);

                packetContainer.getIntegers().write(0, fakeData.getEntityId());

                packetContainer.getWatchableCollectionModifier().write(0, fakeData.getWatchables());

                for (Player player : getPlayersTracking())
                {
                    UtilPlayer.sendPacket(player, packetContainer);
                }
            }
        }

        return this;
    }

    /**
     * Set the distance the hologram is viewable from. Default is 70
     */
    public FakeEntity setViewDistance(int newDistance)
    {
        _viewDistance = newDistance;

        return setLocation(getLocation());
    }

    /**
     * Start the hologram
     */
    public FakeEntity start()
    {
        if (!isInUse())
        {
            if (getData().length == 0)
            {
                throw new IllegalArgumentException("Cannot start an empty fake entity");
            }

            FakeEntityManager.fakeEntity.addHologram(this);

            getPlayersTracking().addAll(findNearbyPlayers());

            sendSpawnPackets(getPlayersTracking().toArray(new Player[0]));

            _isInUse = true;
        }

        return this;
    }

    /**
     * Stop the hologram
     */
    public FakeEntity stop()
    {
        if (isInUse())
        {
            FakeEntityManager.fakeEntity.removeHologram(this);

            for (Player player : getPlayersTracking())
            {
                UtilPlayer.sendPacket(player, getDestroyPacket());
            }

            getPlayersTracking().clear();

            _isInUse = false;
        }

        return this;
    }
}
