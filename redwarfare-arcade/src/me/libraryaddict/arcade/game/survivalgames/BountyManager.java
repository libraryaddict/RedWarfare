package me.libraryaddict.arcade.game.survivalgames;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.stats.Stats;

public class BountyManager
{
    private HashMap<UUID, ArrayList<Pair<String, Long>>> _bounties = new HashMap<UUID, ArrayList<Pair<String, Long>>>();
    private SurvivalGames _survivalGames;

    public BountyManager(SurvivalGames survivalGames)
    {
        _survivalGames = survivalGames;
    }

    public void addBounty(Player player, Player toBounty, long bounty)
    {
        if (!_bounties.containsKey(toBounty.getUniqueId()))
        {
            _bounties.put(toBounty.getUniqueId(), new ArrayList<Pair<String, Long>>());
        }

        _bounties.get(toBounty.getUniqueId()).add(Pair.of(player.getName(), bounty));

        _survivalGames.addPoints(player, "Placed Bounty", -bounty);
    }

    public long claimBounty(Player killed, Player claimer)
    {
        ArrayList<Pair<String, Long>> placed = _bounties.remove(killed.getUniqueId());

        if (placed == null)
            return 0;

        long bounty = 0;

        for (Pair<String, Long> pair : placed)
        {
            bounty += pair.getValue();
        }

        if (bounty > 0)
        {
            _survivalGames.Announce(
                    C.Gold + claimer.getName() + " has claimed the bounty of " + bounty + " on " + killed.getName() + "'s head!");
            Stats.add(claimer, "Game." + _survivalGames.getName() + ".Bounties Claimed");
        }

        return bounty;
    }

    public int getBountiesPlaced(Player player, Player toBounty)
    {
        if (!_bounties.containsKey(toBounty.getUniqueId()))
            return 0;

        int amount = 0;

        for (Pair<String, Long> pair : _bounties.get(toBounty.getUniqueId()))
        {
            if (!player.getName().equals(pair.getKey()))
                continue;

            amount++;
        }

        return amount;
    }

    public long getKillworth(Player player)
    {
        long points = getUsablePoints(player);

        if (_bounties.containsKey(player.getUniqueId()))
        {
            for (Pair<String, Long> bounty : _bounties.get(player.getUniqueId()))
            {
                points += bounty.getValue();
            }
        }

        points += 5;

        if (_survivalGames.getPlayers(true).size() <= 2)
            points += 50;

        return points;
    }

    public long getPointsLostOnDeath(Player player)
    {
        long points = Math.max(0L, getUsablePoints(player));

        int maxLoss = Integer.MAX_VALUE;

        if (points < 5000L)
        {
            maxLoss = 150;
        }
        else if (points < 10000L)
        {
            maxLoss = 250;
        }
        else
        {
            maxLoss = (int) ((points - 10000L) / 15L);

            if (maxLoss < 250)
            {
                maxLoss = 250;
            }
        }

        long toLose = points / 10L;

        if (toLose > maxLoss)
        {
            toLose = maxLoss;
        }

        return toLose;
    }

    public long getUsablePoints(Player player)
    {
        long points = Currency.get(player, CurrencyType.POINT);

        for (ArrayList<Pair<String, Long>> bounties : _bounties.values())
        {
            for (Pair<String, Long> bounty : bounties)
            {
                if (!bounty.getKey().equals(player.getUniqueId()))
                    continue;

                points -= bounty.getValue();
            }
        }

        return Math.max(0, points);
    }

    public void onDeath(Player player, Player killer)
    {
        long killworth = getPointsLostOnDeath(player);

        Stats.add(player, "Game." + _survivalGames.getName() + ".Points Lost", killworth);

        player.sendMessage(C.Blue + "Lost " + killworth + " points!");

        _survivalGames.addPoints(player, "Player Death", -killworth);

        if (killer == null)
        {
            refundBounties(player);
        }
        else
        {
            Stats.add(killer, "Game." + _survivalGames.getName() + ".Points Stolen", killworth);
            killworth += 5;

            killworth += claimBounty(player, killer);

            _survivalGames.addPoints(killer, "Player Kill", killworth);

            killer.sendMessage(C.Blue + "Gained " + killworth + " points!");
        }
    }

    public void onWin(Player player)
    {
        ArrayList<Pair<String, Long>> placed = _bounties.remove(player.getUniqueId());

        long reward = 50;

        if (placed != null)
        {
            long bounty = 0;

            for (Pair<String, Long> pair : placed)
            {
                bounty += pair.getValue();
            }

            if (bounty > 0)
            {
                _survivalGames.Announce(C.Gold + player.getName() + " has claimed the bounty of " + bounty + " on their head!");
            }

            player.sendMessage(C.Blue + "Given " + bounty + " points from your bounties!");

            reward += bounty;
        }

        _survivalGames.addPoints(player, "Winning", reward);
        player.sendMessage(C.Blue + "Given 50 points for winning the game!");
    }

    public void refundBounties(Player player)
    {
        ArrayList<Pair<String, Long>> placed = _bounties.remove(player.getUniqueId());

        if (placed == null)
            return;

        HashMap<String, Long> values = new HashMap<String, Long>();

        for (Pair<String, Long> pair : placed)
        {
            values.put(pair.getKey(), values.getOrDefault(pair.getKey(), 0L) + pair.getValue());
        }

        for (Entry<String, Long> entry : values.entrySet())
        {
            Player p = Bukkit.getPlayer(entry.getKey());

            if (p == null)
                continue;

            p.sendMessage(C.Gold + "Refunded " + entry.getValue() + " points from " + player.getName() + "'s bounty!");

            _survivalGames.addPoints(p, "Bounty Refund", entry.getValue());
        }
    }
}
