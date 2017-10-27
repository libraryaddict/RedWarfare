package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteTag extends Tag
{
    public byte data;

    public ByteTag(String name)
    {
        super(name);
    }

    public ByteTag(String name, byte data)
    {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy()
    {
        return new ByteTag(getName(), data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            ByteTag o = (ByteTag) obj;
            return data == o.data;
        }
        return false;
    }

    public byte getId()
    {
        return TAG_Byte;
    }

    void load(DataInput dis) throws IOException
    {
        data = dis.readByte();
    }

    public String toString()
    {
        return "" + data;
    }

    void write(DataOutput dos) throws IOException
    {
        dos.writeByte(data);
    }
}
