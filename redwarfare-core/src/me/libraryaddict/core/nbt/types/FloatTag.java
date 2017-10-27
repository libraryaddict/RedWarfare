package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class FloatTag extends Tag
{
    public float data;

    public FloatTag(String name)
    {
        super(name);
    }

    public FloatTag(String name, float data)
    {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy()
    {
        return new FloatTag(getName(), data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            FloatTag o = (FloatTag) obj;
            return data == o.data;
        }
        return false;
    }

    public byte getId()
    {
        return TAG_Float;
    }

    void load(DataInput dis) throws IOException
    {
        data = dis.readFloat();
    }

    public String toString()
    {
        return "" + data;
    }

    void write(DataOutput dos) throws IOException
    {
        dos.writeFloat(data);
    }

}
