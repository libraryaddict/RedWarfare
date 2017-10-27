package me.libraryaddict.arcade.forcekit;

import java.util.ArrayList;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Kit;

public interface ForceKit
{
    public ArrayList<Kit> getKits(GameTeam team);
    
    public String parse(String kits);
}
