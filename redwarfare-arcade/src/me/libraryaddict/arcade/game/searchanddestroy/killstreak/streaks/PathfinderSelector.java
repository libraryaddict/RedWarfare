package me.libraryaddict.arcade.game.searchanddestroy.killstreak.streaks;

import com.google.common.base.Predicate;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitGhost;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitWraith;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.server.v1_12_R1.Entity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Player;

public class PathfinderSelector implements Predicate<Entity>
{
    private SearchAndDestroy _searchAndDestroy;
    private GameTeam _team;

    public PathfinderSelector(SearchAndDestroy searchAndDestroy, GameTeam team)
    {
        _team = team;
        _searchAndDestroy = searchAndDestroy;
    }

    public boolean a(Entity nmsEntity)
    {
        CraftEntity entity = nmsEntity.getBukkitEntity();

        if (!entity.isValid())
            return false;

        if (!_searchAndDestroy.isAlive(entity))
            return false;

        Kit kit;

        if (entity instanceof Player && ((kit = _searchAndDestroy.getKit((Player) entity)) instanceof KitGhost || kit instanceof KitWraith))
        {
            Player player = (Player) entity;

            if (!player.isSprinting() && !UtilInv.isHoldingItem(player) && UtilPlayer.getArrowsInBody(player) <= 0) // No arrows
                                                                                                                    // sticking
                                                                                                                    // out
            {
                return false;
            }
        }

        if (entity.hasMetadata("GameTeam") && entity.getMetadata("GameTeam").get(0).value() == _team)
        {
            return false;
        }
        else if (entity instanceof Player && _searchAndDestroy.getTeam(entity) == _team)
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean apply(Entity object)
    {
        return a(object);
    }

}
