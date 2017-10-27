package me.libraryaddict.core.rank;

import me.libraryaddict.core.C;

public enum Rank
{
    ADMIN("Admin", C.Red, 6),

    ALL("", C.Yellow, 0),

    BUILDER("BuildTeam", C.DGreen, 4),

    MAPMAKER("MapMaker", C.Aqua, 3),

    MOD("Mod", C.Red, 5),

    MVP("MVP", C.DAqua, 2),

    OWNER("Owner", C.DRed, 10),

    VIP("VIP", C.DPurple, 1);

    static
    {
        OWNER.setOwned(ADMIN);
        ADMIN.setOwned(MOD);
        MOD.setOwned(MVP, BUILDER);
        MVP.setOwned(VIP);
        BUILDER.setOwned(MAPMAKER);
        MAPMAKER.setOwned(ALL);
        VIP.setOwned(ALL);
    }

    private String _name;
    private String _prefix;
    private int _rating;
    private Rank[] _subranks = new Rank[0];

    private Rank(String name, String prefix, int rating)
    {
        _name = name;
        _prefix = prefix;
        _rating = rating;
    }

    public String getName()
    {
        return _name;
    }

    public String getPrefix()
    {
        return _prefix;
    }

    public boolean isBetterRating(Rank rank)
    {
        return _rating > rank._rating;
    }

    public boolean ownsRank(Rank rank)
    {
        if (this == rank)
            return true;

        for (Rank r : _subranks)
        {
            if (r.ownsRank(rank))
                return true;
        }

        return false;
    }

    private void setOwned(Rank... owns)
    {
        _subranks = owns;
    }
}
