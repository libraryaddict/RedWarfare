package me.libraryaddict.core.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;

import me.libraryaddict.core.hologram.HologramInteract.InteractType;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;

public class Hologram
{
    public enum HologramTarget
    {
        BLACKLIST, WHITELIST;
    }

    private PacketContainer _destroyPacket;
    private ArrayList<Integer> _entityIds = new ArrayList<Integer>();
    private Entity _followEntity;
    private HologramInteract _hologramInteract;
    private String[] _hologramText = new String[0];
    private boolean _isInUse;
    private Location _location;
    private boolean _makeSpawnPackets = true;
    private HashSet<String> _playersInList = new HashSet<String>();
    private ArrayList<Player> _playersTracking = new ArrayList<Player>();
    private boolean _removeEntityDeath;
    private ArrayList<PacketContainer> _spawnMetaPackets = new ArrayList<PacketContainer>();
    private ArrayList<PacketContainer> _spawnPackets = new ArrayList<PacketContainer>();
    private HologramTarget _target = HologramTarget.BLACKLIST;
    private int _viewDistance = 70;
    protected Vector relativeToEntity;

    public Hologram(Location location, String... text)
    {
        _location = location.clone();

        setText(text);
    }

    /**
     * Adds the player to the Hologram to be effected by Whitelist or Blacklist
     */
    public Hologram addPlayer(Player... players)
    {
        for (Player player : players)
            addPlayer(player.getName());

        return this;
    }

    /**
     * Adds the player to the Hologram to be effected by Whitelist or Blacklist
     */
    public Hologram addPlayer(String player)
    {
        _playersInList.add(player);
        return this;
    }

    public Hologram addPlayers(Collection<Player> players)
    {
        for (Player player : players)
        {
            addPlayer(player);
        }

        return this;
    }

    /**
     * Adds the player to the Hologram to be effected by Whitelist or Blacklist
     */
    public Hologram addPlayers(Player... players)
    {
        for (Player player : players)
            addPlayer(player.getName());

        return this;
    }

    private PacketContainer constructMetaPacket(int entityId, String lineOfText)
    {
        PacketContainer meta = new PacketContainer(PacketType.Play.Server.ENTITY_METADATA);
        meta.getIntegers().write(0, entityId);
        meta.getWatchableCollectionModifier().write(0, getWatcher(lineOfText, true).getWatchableObjects());

        return meta;
    }

    private PacketContainer constructSpawnPacket(int textRow, String lineOfText, boolean display)
    {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);

        StructureModifier<Integer> ints = packet.getIntegers();

        ints.write(0, _entityIds.get(textRow));
        ints.write(1, (int) EntityType.ARMOR_STAND.getTypeId());

        StructureModifier<Double> doubles = packet.getDoubles();

        doubles.write(0, getLocation().getX());
        doubles.write(1, getLocation().getY() + (textRow * 0.285D));
        doubles.write(2, getLocation().getZ());

        packet.getModifier().write(1, UUID.randomUUID());

        packet.getDataWatcherModifier().write(0, getWatcher(lineOfText, display));

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

    protected PacketContainer getDestroyPacket()
    {
        return _destroyPacket;
    }

    public Entity getEntityFollowing()
    {
        return _followEntity;
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

    protected ArrayList<Player> getNearbyPlayers()
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

    protected ArrayList<Player> getPlayersTracking()
    {
        return _playersTracking;
    }

    /**
     * Get the text in the hologram
     */
    public String[] getText()
    {
        // We reverse it again as the hologram would otherwise display the text from the bottom row to the top row
        String[] reversed = new String[_hologramText.length];

        for (int i = 0; i < reversed.length; i++)
        {
            reversed[i] = _hologramText[reversed.length - (i + 1)];
        }

        return reversed;
    }

    /**
     * Get the view distance the hologram is viewable from. Default is 70
     */
    public int getViewDistance()
    {
        return _viewDistance;
    }

    private WrappedDataWatcher getWatcher(String lineOfText, boolean visible)
    {
        WrappedDataWatcher watcher = new WrappedDataWatcher();

        watcher.setObject(
                new WrappedDataWatcher.WrappedDataWatcherObject(MetaIndex.ENTITY_META.getIndex(), Registry.get(Byte.class)),
                (byte) 32); // Invisibility
        watcher.setObject(
                new WrappedDataWatcher.WrappedDataWatcherObject(MetaIndex.ARMORSTAND_META.getIndex(), Registry.get(Byte.class)),
                (byte) 16); // Boundingbox less

        if (visible)
        {
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(MetaIndex.ENTITY_CUSTOM_NAME.getIndex(),
                    Registry.get(String.class)), lineOfText); // CustomName
            watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE.getIndex(),
                    Registry.get(Boolean.class)), visible); // CustomName-Visibility
        }

        return watcher;
    }

    public boolean isId(int id)
    {
        return _entityIds.contains(id);
    }

    /**
     * Is the hologram holograming?
     */
    public boolean isInUse()
    {
        return _isInUse;
    }

    public boolean isRemoveOnEntityDeath()
    {
        return _removeEntityDeath;
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
                if (getLocation().distance(player.getLocation()) < getViewDistance())
                {
                    return true;
                }
            }
        }

        return false;
    }

    private void makeDestroyPacket()
    {
        while (_hologramText.length > _entityIds.size())
        {
            _entityIds.add(UtilEnt.getNewEntityId());
        }

        _destroyPacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

        int[] ids = new int[0];

        for (int i = 0; i < _hologramText.length; i++)
        {
            if (_hologramText[i] == null)
                continue;

            ids = Arrays.copyOf(ids, ids.length + 1);
            ids[ids.length - 1] = _entityIds.get(i);
        }

        _destroyPacket.getIntegerArrays().write(0, ids);
    }

    private void makeSpawnPackets()
    {
        while (_hologramText.length > _entityIds.size())
        {
            _entityIds.add(UtilEnt.getNewEntityId());
        }

        _spawnPackets.clear();
        _spawnMetaPackets.clear();

        for (int index = 0; index < _hologramText.length; index++)
        {
            String line = _hologramText[index];

            if (line == null)
                continue;

            _spawnPackets.add(constructSpawnPacket(index, _hologramText[index], false));
            _spawnMetaPackets.add(constructMetaPacket(_entityIds.get(index), _hologramText[index]));
        }
    }

    public void onInteract(Player player, InteractType interactType)
    {
        if (_hologramInteract == null)
            return;

        _hologramInteract.onInteract(player, interactType);
    }

    /**
     * Removes the player from the Hologram so they are no longer effected by Whitelist or Blacklist
     */
    public Hologram removePlayer(Player player)
    {
        return removePlayer(player.getName());
    }

    /**
     * Removes the player from the Hologram so they are no longer effected by Whitelist or Blacklist
     */
    public Hologram removePlayer(String player)
    {
        _playersInList.remove(player);

        return this;
    }

    protected void sendSpawnPackets(Player... players)
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

        String[] text = getText().clone();

        new BukkitRunnable()
        {
            public void run()
            {
                if (!Arrays.equals(text, getText()))
                    return;

                for (Player player : players)
                {
                    UtilPlayer.sendPacket(player, _spawnMetaPackets);
                }
            }
        }.runTaskLater(HologramManager.hologramManager.getPlugin(), 1);
    }

    /**
     * If the entity moves, the hologram will update its position to appear relative to the movement.
     *
     * @Please note the hologram updates every tick.
     */
    public Hologram setFollowEntity(Entity entityToFollow)
    {
        _followEntity = entityToFollow;
        relativeToEntity = entityToFollow == null ? null : _location.clone().subtract(entityToFollow.getLocation()).toVector();

        return this;
    }

    /**
     * Set who can see the hologram
     *
     * @Whitelist = Only people added can see the hologram
     * @Blacklist = Anyone but people added can see the hologram
     */
    public Hologram setHologramTarget(HologramTarget newTarget)
    {
        _target = newTarget;

        if (isInUse())
        {
            stop();
            start();
        }

        return this;
    }

    public Hologram setInteract(HologramInteract hologramInteract)
    {
        _hologramInteract = hologramInteract;

        return this;
    }

    /**
     * Sets the hologram to appear at this location
     */
    public Hologram setLocation(Location newLocation)
    {
        _makeSpawnPackets = true;

        Location oldLocation = getLocation();
        _location = newLocation.clone();

        if (getEntityFollowing() != null)
        {
            relativeToEntity = _location.clone().subtract(getEntityFollowing().getLocation()).toVector();
        }

        if (isInUse())
        {
            ArrayList<Player> canSee = getNearbyPlayers();
            Iterator<Player> itel = _playersTracking.iterator();

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

                if (!_playersTracking.contains(player))
                {
                    _playersTracking.add(player);
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
                    for (int index = 0; index < _hologramText.length; index++)
                    {
                        String line = _hologramText[index];

                        if (line == null)
                            continue;

                        PacketContainer packet = new PacketContainer(PacketType.Play.Server.REL_ENTITY_MOVE);
                        packets.add(packet);

                        StructureModifier<Integer> ints = packet.getIntegers();
                        ints.write(0, _entityIds.get(index));
                        ints.write(1, (int) x);
                        ints.write(2, (int) y);
                        ints.write(3, (int) z);
                    }
                }
                else
                {
                    for (int index = 0; index < _hologramText.length; index++)
                    {
                        String line = _hologramText[index];

                        if (line == null)
                            continue;

                        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_TELEPORT);
                        packets.add(packet);

                        packet.getIntegers().write(0, _entityIds.get(index));

                        StructureModifier<Double> doubles = packet.getDoubles();
                        doubles.write(0, getLocation().getX());
                        doubles.write(1, getLocation().getY() + (index * 0.285D));
                        doubles.write(2, getLocation().getZ());
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

    public Hologram setRemoveOnEntityDeath()
    {
        _removeEntityDeath = true;
        return this;
    }

    public Hologram setText(Collection<String> newLines)
    {
        return setText(newLines.toArray(new String[0]));
    }

    /**
     * Set the hologram text
     */
    public Hologram setText(String... newLines)
    {
        String[] newText = new String[newLines.length];

        for (int i = 0; i < newText.length; i++)
        {
            newText[i] = newLines[newText.length - (i + 1)];
        }

        if (newText.equals(_hologramText))
            return this;

        if (isInUse())
        {
            String[] oldText = _hologramText;

            while (newText.length > _entityIds.size())
            {
                _entityIds.add(UtilEnt.getNewEntityId());
            }

            int[] deleteIds = new int[0];

            for (int index = 0; index < oldText.length; index++)
            {
                if (oldText[index] == null)
                    continue;

                if (index < newText.length && newText[index] != null)
                    continue;

                deleteIds = Arrays.copyOf(deleteIds, deleteIds.length + 1);
                deleteIds[deleteIds.length - 1] = _entityIds.get(index);
            }

            ArrayList<PacketContainer> packets = new ArrayList<PacketContainer>();

            if (deleteIds.length > 0)
            {
                PacketContainer deletePacket = new PacketContainer(PacketType.Play.Server.ENTITY_DESTROY);

                deletePacket.getIntegerArrays().write(0, deleteIds);

                packets.add(deletePacket);
            }

            for (int index = 0; index < newText.length; index++)
            {
                if (newText[index] == null)
                    continue;

                if (index >= oldText.length || oldText[index] == null)
                {
                    packets.add(constructSpawnPacket(index, newText[index], true));
                }
                else if (!newText[index].equals(oldText[index]))
                {
                    packets.add(constructMetaPacket(_entityIds.get(index), newText[index]));
                }
            }

            for (Player player : _playersTracking)
            {
                UtilPlayer.sendPacket(player, packets);
            }
        }

        _hologramText = newText;

        _makeSpawnPackets = true;
        makeDestroyPacket();

        return this;
    }

    /**
     * Set the distance the hologram is viewable from. Default is 70
     */
    public Hologram setViewDistance(int newDistance)
    {
        _viewDistance = newDistance;

        return setLocation(getLocation());
    }

    /**
     * Start the hologram
     */
    public Hologram start()
    {
        if (!isInUse())
        {
            HologramManager.hologramManager.addHologram(this);

            _playersTracking.addAll(getNearbyPlayers());

            sendSpawnPackets(_playersTracking.toArray(new Player[0]));

            _isInUse = true;
        }

        return this;
    }

    /**
     * Stop the hologram
     */
    public Hologram stop()
    {
        if (isInUse())
        {
            HologramManager.hologramManager.removeHologram(this);

            for (Player player : _playersTracking)
            {
                UtilPlayer.sendPacket(player, getDestroyPacket());
            }

            _playersTracking.clear();

            _isInUse = false;
        }

        return this;
    }
}
