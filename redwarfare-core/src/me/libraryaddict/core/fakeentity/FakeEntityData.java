package me.libraryaddict.core.fakeentity;

import java.util.ArrayList;
import java.util.HashMap;

import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class FakeEntityData
{
    private int _data;
    private int _entityId = UtilEnt.getNewEntityId();
    private EntityType _entityType;
    private FakeEntity _fakeEntity;
    private HashMap<MetaIndex, Object> _flags = new HashMap<MetaIndex, Object>();
    private EntityInteract _interact;
    private HashMap<EquipmentSlot, ItemStack> _items = new HashMap<EquipmentSlot, ItemStack>();
    private Location _location;
    private boolean _sendMovement = true;

    public FakeEntityData(EntityType entityType)
    {
        this(entityType, new Vector());
    }

    public FakeEntityData(EntityType entityType, int data)
    {
        this(entityType, new Vector(), data);
    }

    public FakeEntityData(EntityType entityType, Location relLoc)
    {
        this(entityType, relLoc, 0);
    }

    public FakeEntityData(EntityType entityType, Location relLoc, int data)
    {
        _entityType = entityType;
        _location = relLoc;
        _data = data;
    }

    public FakeEntityData(EntityType entityType, Vector vector)
    {
        this(entityType, vector, 0);
    }

    public FakeEntityData(EntityType entityType, Vector vector, int data)
    {
        this(entityType, new Location(null, vector.getX(), vector.getY(), vector.getZ()), data);
    }

    public int getData()
    {
        return _data;
    }

    public DisguiseType getDisguise()
    {
        return DisguiseType.getType(getEntityType());
    }

    private FakeEntity getEntity()
    {
        return _fakeEntity;
    }

    public int getEntityId()
    {
        return _entityId;
    }

    public EntityType getEntityType()
    {
        return _entityType;
    }

    public PacketContainer getEquip(EquipmentSlot slot, ItemStack itemStack)
    {
        PacketContainer packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);

        StructureModifier<Object> mods = packet.getModifier();

        mods.write(0, _entityId);
        mods.write(1, ReflectionManager.createEnumItemSlot(slot));
        mods.write(2, ReflectionManager.getNmsItem(itemStack));

        return packet;
    }

    protected HashMap<MetaIndex, Object> getFlags()
    {
        return _flags;
    }

    public EntityInteract getInteract()
    {
        return _interact;
    }

    public HashMap<EquipmentSlot, ItemStack> getItems()
    {
        return _items;
    }

    public Location getLocation()
    {
        return _location;
    }

    public ArrayList<WrappedWatchableObject> getWatchables()
    {
        ArrayList<WrappedWatchableObject> list = new ArrayList<WrappedWatchableObject>();

        ArrayList<MetaIndex> types = MetaIndex.getFlags(DisguiseType.getType(_entityType).getWatcherClass());

        for (MetaIndex type : types)
        {
            Object obj = type.getDefault();

            if (_flags.containsKey(type))
            {
                obj = _flags.get(type);
            }

            WrappedWatchableObject watchable = ReflectionManager.createWatchable(type.getIndex(), obj);

            if (watchable == null || Registry.get(watchable.getValue().getClass()) == null)
                continue;

            list.add(watchable);
        }

        return list;
    }

    public boolean isId(int id)
    {
        return _entityId == id;
    }

    public boolean isSendMovement()
    {
        return _sendMovement;
    }

    protected void setFakeEntity(FakeEntity fakeEntity)
    {
        assert getEntity() == null;

        _fakeEntity = fakeEntity;
    }

    public void setInteract(EntityInteract entityInteract)
    {
        _interact = entityInteract;
    }

    protected void setInternalMetadata(MetaIndex type, Object object)
    {
        setInternalMetadata(Pair.of(type, object));
    }

    protected void setInternalMetadata(Pair<MetaIndex, Object>... flags)
    {
        for (Pair<MetaIndex, Object> pair : flags)
        {
            if (pair.getValue() == null)
            {
                _flags.remove(pair.getKey());
            }
            else
            {
                _flags.put(pair.getKey(), pair.getValue());
            }
        }
    }

    public void setMetadata(MetaIndex type, Object object)
    {
        setMetadata(Pair.of(type, object));
    }

    public void setMetadata(Pair<MetaIndex, Object>... flags)
    {
        if (getEntity() != null)
        {
            getEntity().setMetadata(this, flags);
        }
        else
        {
            setInternalMetadata(flags);
        }
    }

    public void setSendMovement(boolean movement)
    {
        _sendMovement = movement;
    }

    protected void setInternalLocation(Location location)
    {
        _location = location;
    }

    public void setLocation(Location loc)
    {
        if (getEntity() != null)
        {
            getEntity().setLocation(this, loc);
        }
        else
        {
            setInternalLocation(loc);
        }
    }
}
