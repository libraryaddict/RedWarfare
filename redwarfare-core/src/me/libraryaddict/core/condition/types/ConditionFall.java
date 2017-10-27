package me.libraryaddict.core.condition.types;

import org.bukkit.entity.Entity;

import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilTime;

public class ConditionFall extends Condition
{
    private boolean _stillValid = true;

    public ConditionFall(Entity victim, Entity cause)
    {
        super("Fall", victim, null, cause, 20 * 10);
    }

    @Override
    public boolean hasExpired()
    {
        if (super.hasExpired())
            return true;

        if (!_stillValid)
            return true;

        return false;
    }

    @Override
    public void tickCondition()
    {
        if (getVictim().getFallDistance() > 0 || !UtilEnt.isGrounded(getVictim())
                || !UtilTime.elasped(getCreated(), getVictim().getVelocity().length() < 0.05 ? 500 : 5000))
        {
            _stillValid = true;
            return;
        }

        _stillValid = false;
    }
}
