package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.SquareBorder;
import me.libraryaddict.build.types.WorldInfo;

public class GameHubCustomData extends BorderCustomData {
    public GameHubCustomData(WorldInfo world) {
        super(world, SquareBorder.class);
    }
}
