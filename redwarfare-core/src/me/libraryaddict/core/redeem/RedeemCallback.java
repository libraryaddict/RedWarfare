package me.libraryaddict.core.redeem;

import org.bukkit.entity.Player;

public interface RedeemCallback
{
    public boolean canRedeem(Player player);

    public String getName();

    public void onCodeAssign(Player player, String code);
    
    public void onCodeAssignFailure(Player player);
    
    public void onRedeem(Player player);
}
