package me.libraryaddict.hub.vote;

import java.util.Iterator;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.ConfirmInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.redeem.RedeemManager;
import me.libraryaddict.core.redeem.redeemcallbacks.RankRedeemCallback;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.vote.VoteManager;
import me.libraryaddict.mysql.operations.MysqlCheckCanVote;

public class VoteRewardInventory extends BasicInventory
{
    private RedeemManager _redeemManager;
    private VoteManager _voteManager;

    public VoteRewardInventory(Player player, RedeemManager redeemManager, VoteManager voteManager)
    {
        super(player, C.Gold + "Voting Rewards", 5 * 9);

        _redeemManager = redeemManager;
        _voteManager = voteManager;

        new BukkitRunnable()
        {
            public void run()
            {
                MysqlCheckCanVote checkCanVote = new MysqlCheckCanVote(getPlayer().getUniqueId(), _voteManager.getVoteNames());

                buildPage(checkCanVote);
            }
        }.runTaskAsynchronously(_redeemManager.getPlugin());
    }

    private void buildPage(MysqlCheckCanVote checkCanVote)
    {
        Iterator<Integer> layout = new ItemLayout("OXO XOX OXO", "XXX XXX XXX", "OXO XOX OXO", "XXX XXX XXX", "XXX XXX XXO")
                .getSlots().iterator();

        for (int i = 0; i < _voteManager.getVoteNames().length; i++)
        {
            String siteName = _voteManager.getVoteNames()[i];

            boolean voted = checkCanVote.displayedVoted(siteName);
            ItemBuilder builder = new ItemBuilder(Material.WOOL, 1, (short) i);

            builder.setTitle(C.Gold + "Vote" + (voted ? C.DRed + C.Bold + " VOTED" : ""));
            builder.addLore(C.Gray + "Vote for a token!");
            builder.addLore("");

            if (checkCanVote.getLastVote(siteName) == null)
                builder.addLore(C.Gray + "You have never voted at this site!");
            else
                builder.addLore(C.Gray + "Last voted " + UtilNumber.getTime(checkCanVote.getLastVote(siteName), 2));

            builder.addLore("");
            int streak = checkCanVote.getCurrentStreak(siteName);

            if (streak > 0)
                builder.addLore(C.Gold + "Current streak: " + C.Yellow + streak);
            else
                builder.addLore(C.Yellow + "No streak active..");

            if (voted)
            {
                builder.addDullEnchant();
            }

            int index = i;

            addButton(layout.next(), builder.build(), new IButton()
            {
                @Override
                public boolean onClick(ClickType clickType)
                {
                    getPlayer().sendMessage(_voteManager.getVoteSites()[index]);
                    return true;
                }
            });
        }

        addItem(layout.next(),
                new ItemBuilder(Material.DIRT).setTitle(C.Gold + "VIP")
                        .addLore(C.Yellow + "Redeem your tokens for VIP!",
                                "Each tier of VIP is 'cheaper' than the last, and when you redeem for VIP you will get a code you can give your friends!")
                .build());

        ItemStack vip1Week = new ItemBuilder(Material.INK_SACK, 1, (short) 5).setTitle(C.DPurple + "VIP 1 Week")
                .addLore(C.Gold + "Cost: " + C.Yellow + "50 tokens", C.Purple + "Click on me to purchase VIP for 1 week",
                        "You will be given a code that you can either redeem yourself, or give to a friend",
                        "Perhaps you will even give it to Sam, your local school bully who is a giant fan of Red Warfare and forces nerds to earn him free VIP",
                        "", "Nerd.")
                .build();

        addButton(layout.next(), vip1Week, new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                new ConfirmInventory(getPlayer(), vip1Week, new Runnable()
                {
                    public void run()
                    {
                        RankRedeemCallback callback = (RankRedeemCallback) _redeemManager.getCallback("VIP 1 Week");

                        if (Currency.get(getPlayer(), CurrencyType.TOKEN) < callback.getPrice())
                        {
                            getPlayer().sendMessage(C.Red + "You cannot afford this");
                            return;
                        }

                        Currency.add(getPlayer(), CurrencyType.TOKEN, "Purchased " + callback.getName(), -callback.getPrice());

                        _redeemManager.assignCode(getPlayer(), callback);

                        openInventory();
                    }
                }, new Runnable()
                {
                    public void run()
                    {
                        openInventory();
                    }
                }).openInventory();

                return true;
            }
        });

        ItemStack vip1Month = new ItemBuilder(Material.INK_SACK, 4, (short) 5).setTitle(C.DPurple + "VIP 1 Month")
                .addLore(C.Gold + "Cost: " + C.Yellow + "180 tokens", C.Purple + "Click on me to purchase VIP for 1 Month",
                        "You will be given a code that you can either redeem yourself, or give to a friend",
                        "If you have enough tokens, this is cheaper than the 1 week VIP by 10%!",
                        "Only the hardcore fans of Red Warfare would get this")
                .build();

        addButton(layout.next(), vip1Month, new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                new ConfirmInventory(getPlayer(), vip1Week, new Runnable()
                {
                    public void run()
                    {
                        RankRedeemCallback callback = (RankRedeemCallback) _redeemManager.getCallback("VIP 1 Month");

                        if (Currency.get(getPlayer(), CurrencyType.TOKEN) < callback.getPrice())
                        {
                            getPlayer().sendMessage(C.Red + "You cannot afford this");
                            return;
                        }

                        Currency.add(getPlayer(), CurrencyType.TOKEN, "Purchased " + callback.getName(), -callback.getPrice());

                        _redeemManager.assignCode(getPlayer(), callback);

                        openInventory();
                    }
                }, new Runnable()
                {
                    public void run()
                    {
                        openInventory();
                    }
                }).openInventory();

                return true;
            }
        });

        ItemStack vip3Months = new ItemBuilder(Material.INK_SACK, 12, (short) 5).setTitle(C.DPurple + "VIP 3 Months")
                .addLore(C.Gold + "Cost: " + C.Yellow + "450 tokens", C.Purple + "Click on me to purchase VIP for 3 Months",
                        "You will be given a code that you can either redeem yourself, or give to a friend",
                        "If you have enough tokens, this is cheaper than the 1 week VIP by 25%!",
                        "Only the loyal hardcore fans of Red Warfare would get this")
                .build();

        addButton(layout.next(), vip3Months, new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                new ConfirmInventory(getPlayer(), vip1Week, new Runnable()
                {
                    public void run()
                    {
                        RankRedeemCallback callback = (RankRedeemCallback) _redeemManager.getCallback("VIP 3 Months");

                        if (Currency.get(getPlayer(), CurrencyType.TOKEN) < callback.getPrice())
                        {
                            getPlayer().sendMessage(C.Red + "You cannot afford this");
                            return;
                        }

                        Currency.add(getPlayer(), CurrencyType.TOKEN, "Purchased " + callback.getName(), -callback.getPrice());

                        _redeemManager.assignCode(getPlayer(), callback);

                        openInventory();
                    }
                }, new Runnable()
                {
                    public void run()
                    {
                        openInventory();
                    }
                }).openInventory();

                return true;
            }
        });

        ItemStack vip6Months = new ItemBuilder(Material.INK_SACK, 24, (short) 5).setTitle(C.DPurple + "VIP 6 Months")
                .addLore(C.Gold + "Cost: " + C.Yellow + "750 tokens", C.Purple + "Click on me to purchase VIP for 6 Months",
                        "You will be given a code that you can either redeem yourself, or give to a friend",
                        "If you have enough tokens, this is cheaper than the 1 week VIP by 37.5%!",
                        "Only the true loyal hardcore fans of Red Warfare would get this")
                .build();

        addButton(layout.next(), vip6Months, new IButton()
        {
            @Override
            public boolean onClick(ClickType clickType)
            {
                new ConfirmInventory(getPlayer(), vip1Week, new Runnable()
                {
                    public void run()
                    {
                        RankRedeemCallback callback = (RankRedeemCallback) _redeemManager.getCallback("VIP 6 Months");

                        if (Currency.get(getPlayer(), CurrencyType.TOKEN) < callback.getPrice())
                        {
                            getPlayer().sendMessage(C.Red + "You cannot afford this");
                            return;
                        }

                        Currency.add(getPlayer(), CurrencyType.TOKEN, "Purchased " + callback.getName(), -callback.getPrice());

                        _redeemManager.assignCode(getPlayer(), callback);

                        openInventory();
                    }
                }, new Runnable()
                {
                    public void run()
                    {
                        openInventory();
                    }
                }).openInventory();

                return true;
            }
        });

        addItem(layout.next(),
                new ItemBuilder(Material.SKULL_ITEM, 1, (short) 3).setTitle(C.Blue + "Refer a player for more tokens!")
                        .addLore(C.Yellow + "Use /refer <Player> and you could earn 40 tokens!",
                                "All they need to do is play on the server for an hour!", "",
                                "The refered player will also get 20 tokens for free!")
                        .build());
    }
}
