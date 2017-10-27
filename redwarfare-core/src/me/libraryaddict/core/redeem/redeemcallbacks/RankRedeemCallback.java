package me.libraryaddict.core.redeem.redeemcallbacks;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.ranks.RankInfo;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.redeem.RedeemCallback;

public class RankRedeemCallback implements RedeemCallback
{
    private long _length;
    private String _name;
    private int _price;
    private Rank _rank;
    private RankManager _rankManager;

    public RankRedeemCallback(RankManager rankManager, Rank rank, String name, int price, long length)
    {
        _rankManager = rankManager;
        _rank = rank;
        _name = name;
        _price = price;
        _length = length * 1000;
    }

    @Override
    public boolean canRedeem(Player player)
    {
        PlayerRank playerRank = _rankManager.getRank(player);

        RankInfo rank = playerRank.getRank(_rank);

        if (rank != null && rank.getExpires() == 0)
        {
            player.sendMessage(C.Red + "You cannot redeem this as you already own the lifetime rank");
            return false;
        }

        return true;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    public long getPrice()
    {
        return _price;
    }

    @Override
    public void onCodeAssign(Player player, String code)
    {
        player.sendMessage(C.Gold + "Congrats! Here's yer new code for " + getName() + ": " + C.Yellow + code);
        player.sendMessage(C.Gold + "You can redeem it by using /redeem <Code>");
        player.sendMessage(C.Gold + "To see your codes, use /redeemed or /codes");
    }

    @Override
    public void onCodeAssignFailure(Player player)
    {
        player.sendMessage(C.Red + "Weird error! Try again!");

        Currency.add(player, CurrencyType.TOKEN, "Error " + getName(), _price);
    }

    @Override
    public void onRedeem(Player player)
    {
        _rankManager.setRank(player, _rank, _length + System.currentTimeMillis());

        player.sendMessage(C.Gold + "Redeemed " + getName() + "!");
    }

}
