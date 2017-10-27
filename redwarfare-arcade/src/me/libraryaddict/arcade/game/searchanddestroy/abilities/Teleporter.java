package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.core.C;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.hologram.Hologram.HologramTarget;
import me.libraryaddict.core.utils.UtilBlock;
import me.libraryaddict.core.utils.UtilTime;

public class Teleporter
{
    private Block _block;
    private boolean _connected;
    private Hologram[] _holograms = new Hologram[2];
    private UUID _owner;
    private BlockState _state;
    private GameTeam _team;
    private boolean _unstable;
    private long _unstableSince;

    public Teleporter(GameTeam team, Player player, Block block)
    {
        _team = team;
        _owner = player.getUniqueId();
        _block = block;
        _state = block.getState();

        for (int i = 0; i < 2; i++)
        {
            _holograms[i] = new Hologram(block.getLocation().add(0.5, 2, 0.5)).addPlayers(team.getPlayers());
        }

        _holograms[0].setHologramTarget(HologramTarget.WHITELIST);

        _holograms[1].setText(team.getColoring() + C.Bold + "Enemy Teleporter");
        _holograms[1].setViewDistance(12);
    }

    public boolean connected()
    {
        return _connected;
    }

    public Block getBlock()
    {
        return _block;
    }

    public Player getPlayer()
    {
        return Bukkit.getPlayer(_owner);
    }

    public GameTeam getTeam()
    {
        return _team;
    }

    public boolean isUnstable()
    {
        if (!_unstable)
            return false;

        if (UtilTime.elasped(_unstableSince, 8000))
        {
            _unstable = false;
            updateHolograms();
        }

        return _unstable;
    }

    public boolean isValid()
    {
        if (!UtilBlock.solid(_block))
            return false;

        for (int y = 1; y <= 2; y++)
        {
            if (!UtilBlock.nonSolid(_block.getRelative(BlockFace.UP, y)))
                return false;
        }

        return true;
    }

    public boolean owns(Player player)
    {
        return _owner.equals(player.getUniqueId());
    }

    public void remove()
    {
        _state.getWorld().playEffect(_state.getLocation(), Effect.STEP_SOUND, _state.getBlock().getType());

        _state.update(true);

        for (Hologram hologram : _holograms)
        {
            hologram.stop();
        }
    }

    public void setConnected(boolean connected)
    {
        _connected = connected;
    }

    public void setUnstable()
    {
        _unstable = true;
        _unstableSince = System.currentTimeMillis();
    }

    public void start()
    {
        for (Hologram hologram : _holograms)
        {
            hologram.start();
        }

        _block.setType(Material.MONSTER_EGGS);
        _block.setData((byte) 5);
        _block.getWorld().playEffect(_block.getLocation(), Effect.STEP_SOUND, _block.getType());
    }

    public void updateHolograms()
    {
        if (_unstable)
        {
            _holograms[0].setText(getTeam().getColoring() + C.Bold + "!!Stabalizing!!");
        }
        else if (connected())
        {
            _holograms[0].setText(getTeam().getColoring() + C.Bold + "Shift to teleport");
        }
        else
        {
            _holograms[0].setText(getTeam().getColoring() + C.Bold + "Not Connected");
        }
    }
}
