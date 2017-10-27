package me.libraryaddict.core.nbt.types;

/**
 * Copyright Mojang AB.
 * 
 * Don't do evil.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class EndTag extends Tag
{

    public EndTag()
    {
        super(null);
    }

    @Override
    public Tag copy()
    {
        return new EndTag();
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    public byte getId()
    {
        return TAG_End;
    }

    void load(DataInput dis) throws IOException
    {
    }

    public String toString()
    {
        return "END";
    }

    void write(DataOutput dos) throws IOException
    {
    }

}
