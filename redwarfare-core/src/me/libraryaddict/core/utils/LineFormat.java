package me.libraryaddict.core.utils;

public enum LineFormat
{
    CHAT(319), LORE(50), NONE(99999);

    private int _length;

    private LineFormat(int length)
    {
        _length = length;
    }

    public int getLength()
    {
        return _length;
    }
}
