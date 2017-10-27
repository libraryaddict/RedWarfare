package me.libraryaddict.arcade.game.searchanddestroy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.core.C;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilPlayer;
import me.libraryaddict.core.utils.UtilString;
import me.libraryaddict.core.utils.UtilTime;

public class TeamBomb
{
    private class ArmInfo
    {
        private ArrayList<UUID> _clickers = new ArrayList<UUID>();
        private long _lastFused = System.currentTimeMillis();
        private long _started = System.currentTimeMillis();
        private GameTeam _team;
        private int _timeToFuse = 10;// isArmed() ? 7 : 9;

        public ArmInfo(GameTeam team)
        {
            _team = team;
        }

        public void addArmer(Player player, ItemStack item)
        {
            if (_clickers.contains(player.getUniqueId()))
                return;

            _clickers.add(player.getUniqueId());
            _timeToFuse--;

            FuseType type = FuseType.getFuseType(item);

            if (type == FuseType.BOMB_SPEED || (type == (isArmed() ? FuseType.BOMB_DEFUSING : FuseType.BOMB_ARMING)))
            {
                _timeToFuse -= type.getLevel(item);
            }
        }

        public long getFinished()
        {
            return _started + (_timeToFuse * 1000);
        }

        public double getProgress()
        {
            return (System.currentTimeMillis() - _started) / (1000D * _timeToFuse);
        }

        public GameTeam getTeam()
        {
            return _team;
        }

        public boolean isValid()
        {
            return !UtilTime.elasped(_lastFused, 750);
        }

        public void setFused()
        {
            _lastFused = System.currentTimeMillis();
        }
    }

    private Comparator<ArmInfo> _armComparator = new Comparator<ArmInfo>()
    {

        @Override
        public int compare(ArmInfo o1, ArmInfo o2)
        {
            return Long.compare(o1.getFinished(), o2.getFinished());
        }
    };

    private ArrayList<ArmInfo> _armInfo = new ArrayList<ArmInfo>();
    private Block _block;
    private TNTPrimed _entity;
    private long _fused;
    private Hologram _hologram;
    private long _lastHiss;
    private boolean _owned;
    private GameTeam _owningTeam;
    private SearchAndDestroy _searchAndDestroy;
    private int _timeLeft = 60;

    public TeamBomb(SearchAndDestroy searchAndDestroy, GameTeam owningTeam, Block b)
    {
        _searchAndDestroy = searchAndDestroy;
        _owningTeam = owningTeam;
        _owned = getTeam() != null;
        _block = b;

        _hologram = new Hologram(b.getLocation().add(0.5, 1.1, 0.5), "Bomb");
        _hologram.start();
    }

    private void armBomb()
    {
        _fused = System.currentTimeMillis();
        _block.setType(Material.AIR);

        _entity = (TNTPrimed) _block.getWorld().spawnEntity(_block.getLocation().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
        _entity.setFuseTicks(Short.MAX_VALUE);
        _entity.setVelocity(new Vector(0, 0, 0));

        _hologram.setFollowEntity(_entity);
    }

    private void checkArmInfo()
    {
        Iterator<ArmInfo> itel = _armInfo.iterator();

        while (itel.hasNext())
        {
            ArmInfo info = itel.next();

            if (!info.isValid())
            {
                itel.remove();
            }
        }
    }

    public boolean checkBombArmState()
    {
        boolean empty = _armInfo.isEmpty();

        checkArmInfo();

        if (_armInfo.isEmpty())
        {
            if (!empty)
            {
                drawHologram();
            }

            return false;
        }

        Collections.sort(_armInfo, _armComparator);

        ArmInfo smallest = _armInfo.get(0);

        if (isArmed())
        {
            if (smallest != null && UtilTime.elasped(smallest.getFinished()))
            {
                if (isOwned())
                    getGame().Announce(getTeam().getColoring() + getTeam().getName() + C.Gold + " has just defused their bomb!");
                else
                    getGame().Announce(smallest.getTeam().getColoring() + smallest.getTeam().getName() + C.Gold
                            + " has just defused " + getTeam().getColoring() + getTeam().getName() + "'s " + C.Gold + "nuke!");

                restore();

                for (UUID defuser : smallest._clickers)
                {
                    Stats.add(defuser, "Game." + getGame().getName() + ".Defused");
                }

                return true;
            }
        }
        else if (smallest != null && UtilTime.elasped(smallest.getFinished()))
        {
            if (isOwned())
            {
                getGame().Announce(C.Gold + C.Magic + "ab" + smallest.getTeam().getColoring() + " " + smallest.getTeam().getName()
                        + C.Gold + " just armed " + getTeam().getColoring() + getTeam().getName() + "'s" + C.Gold + " bomb! "
                        + C.Magic + "ab");
            }
            else
            {
                _owningTeam = smallest.getTeam();

                getGame().Announce(C.Gold + C.Magic + "ab" + smallest.getTeam().getColoring() + " " + smallest.getTeam().getName()
                        + C.Gold + " just armed a nuke! " + C.Magic + "ab");
            }

            for (Player player : UtilPlayer.getPlayers())
            {
                if (getTeam().isInTeam(player) == isOwned())
                {
                    player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 10000, 0);
                }
                else
                {
                    player.playSound(player.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 10000, 0);
                }
            }

            for (UUID armer : smallest._clickers)
            {
                Stats.add(armer, "Game." + getGame().getName() + ".Armed");
            }

            _armInfo.clear();

            armBomb();
            return true;
        }

        return false;
    }

    public void drawHologram()
    {
        if (_armInfo.isEmpty())
        {
            if (isArmed())
            {
                _hologram.setText(getTeam().getColoring() + C.Bold + "Exploding in " + C.DRed + C.Bold + getTimeLeft());
            }
            else
            {
                if (isOwned())
                    _hologram.setText(getTeam().getColoring() + C.Bold + getTeam().getName() + "'s Bomb");
                else
                    _hologram.setText(C.White + "Unarmed nuke");
            }
        }
        else
        {
            ArrayList<String> lines = new ArrayList<String>();

            for (ArmInfo info : _armInfo)
            {
                if (!info.isValid())
                    continue;

                lines.add(info.getTeam().getColoring() + UtilTime.getProgress(UtilString.repeat('█', 10),
                        info.getTeam().getColoring(), C.DRed, info.getProgress() + 0.05));
            }

            _hologram.setText(lines);
        }
    }

    public Block getBlock()
    {
        return _block;
    }

    public TNTPrimed getBomb()
    {
        return _entity;
    }

    public String getDisarmStatus()
    {
        if (!isArmed())
            return null;

        for (ArmInfo info : _armInfo)
        {
            if (!info.isValid())
                continue;

            return info.getTeam().getColoring() + UtilTime.getProgress(UtilString.repeat('▍', 10), info.getTeam().getColoring(),
                    C.DRed, info.getProgress() + 0.05);
        }

        return null;
    }

    public long getFused()
    {
        return _fused;
    }

    private SearchAndDestroy getGame()
    {
        return _searchAndDestroy;
    }

    public GameTeam getTeam()
    {
        return _owningTeam;
    }

    public int getTimeLeft()
    {
        return _timeLeft;
    }

    public boolean isArmed()
    {
        return _entity != null;
    }

    public boolean isOwned()
    {
        return _owned;
    }

    public void onInteract(Player player, ItemStack item)
    {
        if (item == null || item.getType() != Material.BLAZE_POWDER)
            return;

        GameTeam team = getGame().getTeam(player);

        if (team == null)
            return;

        if (isOwned())
        {
            // If the player is on the same team, but the bomb isn't armed
            if ((_owningTeam == team) != isArmed())
            {
                if (Recharge.canUse(player, "Bomb Message"))
                {
                    if (isArmed())
                    {
                        player.sendMessage(C.Red + "You cannot disarm the enemies bomb!");
                    }
                    else
                    {
                        player.sendMessage(C.Red + "You cannot arm your own bomb!");
                    }

                    Recharge.use(player, "Bomb Message", 2000);
                }

                return;
            }
        }
        else if (getTeam() == team)
        {
            if (Recharge.canUse(player, "Bomb Message"))
            {
                player.sendMessage(C.Red + "You cannot disarm your team's nuke!");
                Recharge.use(player, "Bomb Message", 2000);
            }

            return;
        }

        checkArmInfo();

        ArmInfo info = null;

        for (ArmInfo armInfo : _armInfo)
        {
            if (armInfo.getTeam() != team)
                continue;

            info = armInfo;
        }

        if (info == null)
        {
            info = new ArmInfo(team);
            _armInfo.add(info);
        }

        Recharge.use(player, "Bomb Message", 2000);

        info.addArmer(player, item);
        info.setFused();

        Location loc = (isArmed() ? getBomb().getLocation() : getBlock().getLocation().add(0.5, 0, 0.5)).add(0, 1, 0);

        UtilParticle.playParticle(ParticleType.LARGE_SMOKE, loc);

        if (UtilTime.elasped(_lastHiss, 500))
        {
            loc.getWorld().playSound(loc, Sound.BLOCK_FIRE_EXTINGUISH, 1, 0);
            _lastHiss = System.currentTimeMillis();
        }

        checkBombArmState();
        drawHologram();
    }

    public void remove()
    {
        _armInfo.clear();

        if (isArmed())
        {
            _entity.remove();
            _entity = null;
        }
        else
        {
            _block.setType(Material.AIR);
        }

        _hologram.stop();
    }

    /**
     * Restore the bomb from its armed state
     */
    public void restore()
    {
        if (!isArmed())
            return;

        _armInfo.clear();

        _entity.remove();
        _entity = null;

        _block.setType(Material.TNT);
        _timeLeft = 60;

        _hologram.setFollowEntity(null);
        _hologram.setLocation(_block.getLocation().add(0.5, 1.1, 0.5));

        if (!isOwned())
            _owningTeam = null;

        drawHologram();
    }

    public void tickBomb()
    {
        if (checkBombArmState())
            return;

        if (!isArmed())
        {
            return;
        }

        if (!UtilTime.elasped(getFused(), (60 - getTimeLeft()) * 1000))
            return;

        getBomb().getWorld().playSound(getBomb().getLocation(), Sound.ENTITY_CREEPER_DEATH, 1.9F, 1.2F);

        _timeLeft--;

        if (getTimeLeft() <= 0)
            return;

        drawHologram();

        if (!(getTimeLeft() <= 5 || (getTimeLeft() <= 30 && getTimeLeft() % 10 == 0)))
            return;

        getGame().Announce(C.Gold + UtilNumber.getTime(getTimeLeft()) + " left until " + getTeam().getColoring()
                + getTeam().getName() + "'s " + C.Gold + (isOwned() ? "bomb" : "nuke") + " goes off!");

        for (Player player : UtilPlayer.getPlayers())
        {
            if (getTeam().isInTeam(player) == isOwned())
            {
                player.playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 10000, 2);
            }
        }
    }
}
