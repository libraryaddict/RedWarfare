package me.libraryaddict.hub.vote;

import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.fakeentity.EntityInteract;
import me.libraryaddict.core.redeem.RedeemManager;
import me.libraryaddict.core.vote.VoteManager;

public class VoteInteractListener implements EntityInteract
{
    private RedeemManager _redeemManager;
    private VoteManager _voteManager;

    public VoteInteractListener(RedeemManager redeemManager, VoteManager voteManager)
    {
        _redeemManager = redeemManager;
        _voteManager = voteManager;
    }

    @Override
    public void onInteract(Player player, InteractType interactType)
    {
        if (interactType == InteractType.ATTACK)
        {
            player.sendMessage(C.Red + "Ow!");
        }
        else
        {
            new VoteRewardInventory(player, _redeemManager, _voteManager).openInventory();
        }
    }

}
