package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.CircleBorder;
import me.libraryaddict.build.customdata.borders.SquareBorder;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilError;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class BorderCustomData extends WorldCustomData {
    private CustomData _border;

    public BorderCustomData(WorldInfo world, Class<? extends CustomData> defaultBorder) {
        super(world);

        try {
            _border = defaultBorder.getConstructor(WorldInfo.class).newInstance(world);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public CustomData getBorder() {
        return _border;
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToKeep() {
        if (_border instanceof CircleBorder) {
            return ((CircleBorder) _border).getChunksToKeep();
        } else {
            return ((SquareBorder) _border).getChunksToKeep();
        }
    }

    public ArrayList<Pair<Integer, Integer>> getChunksToScan() {
        if (_border instanceof CircleBorder) {
            return ((CircleBorder) _border).getChunksToScan();
        } else {
            return ((SquareBorder) _border).getChunksToScan();
        }
    }

    public String getMissing() {
        String missing = _border.getMissing();

        if (missing != null)
            return missing;

        return super.getMissing();
    }

    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = super.getTools();

        tools.addAll(_border.getTools());

        return tools;
    }

    public String getWarningCode() {
        return _border.getWarningCode();
    }

    public boolean isBorderSet() {
        return getBorder().isBorderSet();
    }

    public boolean isInside(Location location) {
        if (_border instanceof CircleBorder) {
            return ((CircleBorder) _border).isInside(location);
        } else {
            return ((SquareBorder) _border).isInside(location);
        }
    }

    public void loadConfig(YamlConfiguration config) {
        super.loadConfig(config);

       /* if (config.contains("Border.Type"))
        {
            if (config.get("Border.Type").equals("Square"))
            {
                _border = new SquareBorder(getInfo());
            }
            else
            {
                _border = new CircleBorder(getInfo());
            }
        }*/

        _border.loadConfig(config);
    }

    @EventHandler
    public void onHalfSec(TimeEvent event) {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (_border instanceof CircleBorder)
            ((CircleBorder) _border).onTick();
        else
            ((SquareBorder) _border).onTick();
    }

    @EventHandler
    public void onInteractBorder(PlayerInteractEvent event) {
        if (_border instanceof CircleBorder) {
            ((CircleBorder) _border).onInteractBorder(event);
        } else {
            ((SquareBorder) _border).onInteractBorder(event);
        }
    }

    public void saveConfig(YamlConfiguration config) {
        super.saveConfig(config);

        _border.saveConfig(config);
    }

    public void unloadData() {
        super.unloadData();

        _border.unloadData();
    }
}
