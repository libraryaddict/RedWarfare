package me.libraryaddict.core.damage;

import java.util.Objects;

public class DamageMod
{
    public static DamageMod ARMOR = new DamageMod("Armor");
    public static DamageMod ARMOR_ENCHANTS = new DamageMod("Armor Enchants");
    public static DamageMod CUSTOM = new DamageMod("Custom");
    public static DamageMod WEAPON = new DamageMod("Weapon");
    public static DamageMod WEAPON_ENCHANTS = new DamageMod("Weapon Enchants");

    private String _modName;
    private DamageMod _parent;

    public DamageMod(DamageMod parent, String name)
    {
        _parent = parent;
        _modName = name;
    }

    public DamageMod(String name)
    {
        _modName = name;
    }

    @Override
    public boolean equals(Object object)
    {
        return object instanceof DamageMod && Objects.equals(_modName, ((DamageMod) object)._modName)
                && Objects.equals(_parent, ((DamageMod) object)._parent);
    }

    public String getName()
    {
        return _modName;
    }

    public DamageMod getSubMod(String name)
    {
        return new DamageMod(this, name);
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    /**
     * Check if the paremeter is the dad of this damagemod
     */
    public boolean isParent(DamageMod parent)
    {
        if (parent == this)
            return true;

        return _parent != null && _parent.isParent(parent);
    }

    public String toString()
    {
        return "DamageMod[" + getName() + "," + _parent + "]";
    }
}
