package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.SquareBorder;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.data.TeamSettings;

public class GenericGameCustomData extends GameCustomData {
    public GenericGameCustomData(WorldInfo world) {
        super(world, SquareBorder.class, TeamSettings.values());
    }

    @Override
    public int getMaxSpawns() {
        return 400;
    }

    @Override
    public int getMaxTeams() {
        return 100;
    }

    @Override
    public int getMinSpawns() {
        return 1;
    }

    @Override
    public int getMinTeams() {
        return 1;
    }
}
