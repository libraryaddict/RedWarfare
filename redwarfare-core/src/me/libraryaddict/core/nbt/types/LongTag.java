package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class LongTag extends Tag
{
    public long data;

    public LongTag(String name)
    {
        super(name);
    }

    public LongTag(String name, long data)
    {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy()
    {
        return new LongTag(getName(), data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            LongTag o = (LongTag) obj;
            return data == o.data;
        }
        return false;
    }

    public byte getId()
    {
        return TAG_Long;
    }

    void load(DataInput dis) throws IOException
    {
        data = dis.readLong();
    }

    public String toString()
    {
        return "" + data;
    }

    void write(DataOutput dos) throws IOException
    {
        dos.writeLong(data);
    }

}
