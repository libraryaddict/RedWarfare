package me.libraryaddict.network;

import java.lang.reflect.Type;

public class Pref<T>
{
    private T _default;
    private String _pref;
    private Type _type;

    public Pref(String name, T def)
    {
        _pref = name;
        _default = def;
    }

    public Pref(String name, T def, Type type)
    {
        _pref = name;
        _type = type;
        _default = def;
    }

    public T getDefault()
    {
        return _default;
    }

    public String getName()
    {
        return _pref;
    }

    public Type getToken()
    {
        return _type;
    }

    public boolean isPreload()
    {
        return true;
    }
}
