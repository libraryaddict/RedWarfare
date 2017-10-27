package me.libraryaddict.bungee;

import net.md_5.bungee.api.plugin.Plugin;

public class Bungee extends Plugin
{
    public void onEnable()
    {
        new BungeeManager(this);
    }
}
