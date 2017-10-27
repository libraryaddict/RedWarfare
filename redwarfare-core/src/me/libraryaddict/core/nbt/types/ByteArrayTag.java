package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class ByteArrayTag extends Tag
{
    public byte[] data;

    public ByteArrayTag(String name)
    {
        super(name);
    }

    public ByteArrayTag(String name, byte[] data)
    {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy()
    {
        byte[] cp = new byte[data.length];
        System.arraycopy(data, 0, cp, 0, data.length);
        return new ByteArrayTag(getName(), cp);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            ByteArrayTag o = (ByteArrayTag) obj;
            return ((data == null && o.data == null) || (data != null && data.equals(o.data)));
        }
        return false;
    }

    public byte getId()
    {
        return TAG_Byte_Array;
    }

    void load(DataInput dis) throws IOException
    {
        int length = dis.readInt();
        data = new byte[length];
        dis.readFully(data);
    }

    public String toString()
    {
        return "[" + data.length + " bytes]";
    }

    void write(DataOutput dos) throws IOException
    {
        dos.writeInt(data.length);
        dos.write(data);
    }
}
