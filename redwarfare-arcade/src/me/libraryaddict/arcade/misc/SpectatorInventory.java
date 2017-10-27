package me.libraryaddict.arcade.misc;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.TeamBomb;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;

public class SpectatorInventory extends PageInventory
{
    private Game _game;

    public SpectatorInventory(Player player, Game game)
    {
        super(player, "Spectator Inventory");

        _game = game;

        buildPages();
    }

    public void buildPages()
    {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        HashMap<Player, GameTeam> teams = new HashMap<Player, GameTeam>();
        ArrayList<Player> players = _game.getPlayers(true);

        for (Player player : players)
        {
            teams.put(player, _game.getTeam(player));
        }

        Collections.sort(players, new Comparator<Player>()
        {
            @Override
            public int compare(Player o1, Player o2)
            {
                int diff = teams.get(o1).getName().compareToIgnoreCase(teams.get(o2).getName());

                if (diff == 0)
                {
                    diff = o1.getName().compareToIgnoreCase(o2.getName());
                }

                return diff;
            }
        });

        GameTeam hisTeam = _game.getTeam(getPlayer());
        GameTeam currentTeam = null;

        for (int i = 0; i < players.size(); i++)
        {
            Player player = players.get(i);
            GameTeam team = teams.get(player);

            if (currentTeam != team)
            {
                while (items.size() % 9 != 0)
                    items.add(null);

                items.add(Pair.of(
                        new ItemBuilder(team.getSettings().getHat()).setTitle(team.getColoring() + team.getName()).build(),
                        new IButton()
                        {
                            @Override
                            public boolean onClick(ClickType clickType)
                            {
                                if (_game instanceof SearchAndDestroy)
                                {
                                    for (TeamBomb bomb : ((SearchAndDestroy) _game).getBombs())
                                    {
                                        if (!bomb.isOwned())
                                            continue;

                                        if (bomb.getTeam() != team)
                                            continue;

                                        getPlayer().teleport(bomb.getBlock().getLocation().add(0.5, 0, 0.5));
                                        break;
                                    }
                                }

                                return true;
                            }
                        }));

                currentTeam = team;
            }

            Kit tkit = _game.getKit(player);

            if (team != hisTeam)
                tkit = tkit.getHiddenKit();

            Kit kit = tkit;

            ItemBuilder builder = new ItemBuilder(kit.getIcon());

            builder.setTitle(team.getColoring() + player.getName());
            builder.addLore(C.Blue + "Kit: " + C.White + kit.getName());
            builder.addLore(
                    C.Blue + "Kills: " + C.White + new DecimalFormat("#.#").format(Math.floor(_game.getKillstreak(player))));
            builder.addLore("");
            builder.addLore(C.Green + C.Bold + "CLICK TO TELEPORT");

            items.add(Pair.of(builder.build(), new IButton()
            {
                @Override
                public boolean onClick(ClickType clickType)
                {
                    if (!kit.canTeleportTo() && team != hisTeam && hisTeam != null
                            && !_game.getManager().getRank().getRank(getPlayer()).hasRank(Rank.MOD))
                    {
                        getPlayer().sendMessage(C.Red + "You cannot teleport to enemy " + kit.getName() + "s!");
                    }
                    else
                    {
                        getPlayer().teleport(player);
                    }

                    return true;
                }
            }));
        }

        setPages(items);
    }

    @EventHandler
    public void onSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (!_game.isLive())
            return;

        buildPages();
    }
}
