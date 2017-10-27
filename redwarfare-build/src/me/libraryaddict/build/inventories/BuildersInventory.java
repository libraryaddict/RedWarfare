package me.libraryaddict.build.inventories;

import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.UUID;

public class BuildersInventory extends PageInventory {
    private MapInfo _mapInfo;
    private RankManager _rankManager;

    public BuildersInventory(RankManager rankManager, Player player, MapInfo info) {
        super(player, "Map Builders", 45);

        _mapInfo = info;
        _rankManager = rankManager;

        buildPages();
    }

    private void buildManage(Pair<UUID, String> pair, MapRank currentRank) {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        ItemLayout layout = new ItemLayout("OXX XXX XXO", "XXX XXX XXX", "XXX XXX XXX", "XOX OXO XOX");

        items.add(Pair.of(new ItemBuilder(Material.PAPER).setTitle(C.White + "Go back").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                buildPages();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.GLASS).setTitle(C.Red + "Remove rank").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                if (_mapInfo.hasRank(getPlayer(), MapRank.ADMIN)) {
                    _mapInfo.removeBuilder(pair);

                    Player player = Bukkit.getPlayer(pair.getKey());

                    if (player != null) {
                        player.sendMessage(
                                C.Blue + getPlayer().getName() + " has removed your rank for the map " + _mapInfo
                                        .getName());
                    }

                    for (Player p : getPlayer().getWorld().getPlayers()) {
                        p.sendMessage(C.Blue + getPlayer().getName() + " removed " + pair
                                .getValue() + "'s rank from the map " + _mapInfo.getName());
                    }

                    _mapInfo.save();
                }

                buildPages();

                return true;
            }
        }));

        for (MapRank rank : new MapRank[]{MapRank.VISITOR, MapRank.BUILDER, MapRank.EDITOR, MapRank.ADMIN}) {

            ItemBuilder builder = new ItemBuilder(rank.getIcon()).setTitle(
                    rank.getColor() + (currentRank != null && currentRank.has(rank) ? "Demote" :
                            "Promote") + " to " + rank.getName());

            if (currentRank == rank)
                builder.setTitle(rank.getColor() + rank.getName());

            for (String desc : rank.getDesc()) {
                builder.addLore(C.Gray + desc);
            }

            if (rank == currentRank) {
                builder.addLore("", C.Blue + C.Bold + "CURRENT RANK");
            }

            items.add(Pair.of(builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (_mapInfo.hasRank(getPlayer(), MapRank.ADMIN)) {
                        _mapInfo.addBuilder(pair, rank);

                        Player player = Bukkit.getPlayer(pair.getKey());

                        if (player != null) {
                            player.sendMessage(C.Blue + getPlayer().getName() + " has set your rank to " + rank
                                    .getName() + " for the map " + _mapInfo.getName());
                        }

                        for (Player p : getPlayer().getWorld().getPlayers()) {
                            p.sendMessage(C.Blue + getPlayer().getName() + " has set " + pair
                                    .getValue() + "'s rank to " + rank.getName());
                        }

                        _mapInfo.save();
                    }

                    buildPages();

                    return true;
                }
            }));
        }

        items = layout.format(items);

        setPages(items);
    }

    private void buildPages() {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (Entry<Pair<UUID, String>, MapRank> entry : _mapInfo.getBuilders().entrySet()) {
            if (entry.getValue() == null)
                continue;

            Pair<UUID, String> pair = entry.getKey();
            MapRank admin = entry.getValue();

            ItemStack item = new ItemBuilder(admin.getIcon()).setTitle(C.Aqua + pair.getValue())
                    .addLore("", C.Aqua + C.Bold + "Click on builder to see options").build();

            items.add(Pair.of(item, new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    buildManage(pair, admin);

                    return true;
                }
            }));
        }

        while (items.isEmpty() || items.size() % 27 != 0) {
            items.add(null);
        }

        ItemLayout layout = new ItemLayout("OXX XXX XXO");

        ArrayList<Pair<ItemStack, IButton>> items1 = new ArrayList<Pair<ItemStack, IButton>>();

        items1.add(Pair.of(new ItemBuilder(Material.ANVIL).setTitle(C.Blue + "Add new builder").build(), new IButton() {

            @Override
            public boolean onClick(ClickType clickType) {
                new AddBuilderInventory(getPlayer(), _rankManager, _mapInfo).openInventory();

                return true;
            }
        }));

        if (_rankManager.getRank(getPlayer()).hasRank(Rank.ADMIN)) {
            items1.add(Pair.of(new ItemBuilder(Material.REDSTONE_BLOCK).setTitle(C.Red + "Set creator").build(),
                    new IButton() {

                        @Override
                        public boolean onClick(ClickType clickType) {
                            new SetCreatorInventory(getPlayer(), _rankManager, _mapInfo).openInventory();

                            return true;
                        }
                    }));
        }

        items.addAll(layout.format(items1));

        setPages(items);
    }
}
