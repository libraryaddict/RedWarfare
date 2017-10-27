package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class DoubleTag extends Tag
{
    public double data;

    public DoubleTag(String name)
    {
        super(name);
    }

    public DoubleTag(String name, double data)
    {
        super(name);
        this.data = data;
    }

    @Override
    public Tag copy()
    {
        return new DoubleTag(getName(), data);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (super.equals(obj))
        {
            DoubleTag o = (DoubleTag) obj;
            return data == o.data;
        }
        return false;
    }

    public byte getId()
    {
        return TAG_Double;
    }

    void load(DataInput dis) throws IOException
    {
        data = dis.readDouble();
    }

    public String toString()
    {
        return "" + data;
    }

    void write(DataOutput dos) throws IOException
    {
        dos.writeDouble(data);
    }

}
