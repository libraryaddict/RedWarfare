package me.libraryaddict.core.time;

import me.libraryaddict.core.utils.UtilTime;

public enum TimeType
{
    HALF_SEC(500),

    HOUR(1000 * UtilTime.HOUR),

    MIN(1000 * UtilTime.MINUTE),

    SEC(1000),

    TEN_MIN(1000 * UtilTime.MINUTE * 10),

    TICK(0);

    private long last = System.currentTimeMillis();
    private long next;

    private TimeType(long timePasses)
    {
        next = timePasses;
    }

    public boolean isTime()
    {
        if (!UtilTime.elasped(last, next))
            return false;

        last = System.currentTimeMillis();
        return true;
    }
}
