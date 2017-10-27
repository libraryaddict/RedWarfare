package me.libraryaddict.build.inventories;

import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.MapRank;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.network.Pref;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoadedMapInventory extends PageInventory implements Comparator<MapInfo> {
    private enum SortMapBy {
        CREATED,
        MODIFIED,
        NAME,
        RATING;
    }

    private boolean _permissionsOnly;
    private RankManager _rankManager;
    private SortMapBy _sortBy = SortMapBy.MODIFIED;
    private Pref<String> _sortMapBy;
    private WorldManager _worldManager;

    public LoadedMapInventory(WorldManager worldManager, RankManager rankManager, Player player) {
        super(player, "Select map");

        _worldManager = worldManager;
        _rankManager = rankManager;
        _sortMapBy = new Pref<String>("Map Sort By", SortMapBy.MODIFIED.name());
        _sortBy = SortMapBy.valueOf(Preference.getPreference(getPlayer(), _sortMapBy));

        buildPage();
    }

    public void buildPage() {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        ArrayList<MapInfo> maps = new ArrayList<MapInfo>(_worldManager.getMaps());

        Stream<MapInfo> stream = maps.stream().filter((mapinfo) -> mapinfo.isWorldLoaded());

        if (_permissionsOnly) {
            stream = stream.filter((mapInfo) -> mapInfo.getRank(getPlayer()) != null || mapInfo.isCreator(getPlayer()));
        }

        maps = (ArrayList<MapInfo>) stream.sorted(this).collect(Collectors.toList());

        getTopItems(items);

        for (MapInfo info : maps) {
            if (items.size() % 45 == 0)
                getTopItems(items);

            ItemBuilder builder = info.getIcon();

            if (info.isCreator(getPlayer()) || info.isReleasable()) {
                builder.addLore("");
            }

            if (info.isCreator(getPlayer())) {
                builder.addLore(C.Red + C.Bold + "RIGHT CLICK TO MANAGE OPTIONS");
            }

            if (info.isReleasable()) {
                builder.addLore(C.Aqua + C.Bold + "Map has been marked releasable");
            }

            items.add(Pair.of(builder.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    if (!info.isAllowVisitors() && !info.hasRank(getPlayer(), MapRank.VISITOR) && !_rankManager
                            .getRank(getPlayer()).hasRank(Rank.BUILDER)) {
                        getPlayer().sendMessage(C.Red + "You don't have the rights to access that map");
                        return true;
                    }

                    if (clickType.isLeftClick()) {
                        _worldManager.loadWorld(getPlayer(), info);

                        closeInventory();
                    } else if (clickType.isRightClick()) {
                        if (!info.isCreator(getPlayer())) {
                            getPlayer().sendMessage(C.Red + "You don't have the rights to access the options");
                            return true;
                        }

                        getPlayer().sendMessage(C.Blue + "Set the options for the map " + C.Aqua + info.getName());

                        new MapOptionInventory(_worldManager, _rankManager, getPlayer(), info, LoadedMapInventory.this)
                                .openInventory();
                    }

                    return true;
                }
            }));
        }

        setPages(items);
    }

    @Override
    public int compare(MapInfo o1, MapInfo o2) {
        switch (_sortBy) {
            case MODIFIED:
                if (o1.isWorldLoaded() != o2.isWorldLoaded())
                    return Boolean.compare(o2.isWorldLoaded(), o1.isWorldLoaded());

                return o2.getTimeModified().compareTo(o1.getTimeModified());
            case NAME:
                return o1.getName().compareToIgnoreCase(o2.getName());
            case CREATED:
                return o2.getTimeCreated().compareTo(o1.getTimeCreated());
            case RATING:
                return Double.compare(o2.getRating(), o1.getRating());
            default:
                return 0;
        }
    }

    private void getTopItems(ArrayList<Pair<ItemStack, IButton>> returns) {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        ItemLayout layout = new ItemLayout("OXX XOX XXO", "XXX XXX XXX");

        // Set map name
        // Set map creator
        // Order by
        // Toggle permissions only

        items.add(Pair.of(new ItemBuilder(Material.REDSTONE).setTitle(C.Red + "Go Back").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                new MainBuildInventory(getPlayer(), _worldManager, _rankManager).openInventory();

                return true;
            }
        }));

        ItemBuilder togglePerms = new ItemBuilder(_permissionsOnly ? Material.REDSTONE_BLOCK : Material.GLASS)
                .setTitle(C.Blue + (_permissionsOnly ? "Showing your maps" : "Showing everyone's maps"));

        items.add(Pair.of(togglePerms.build(), new IButton() {

            @Override
            public boolean onClick(ClickType clickType) {
                _permissionsOnly = !_permissionsOnly;

                buildPage();

                return true;
            }
        }));

        ItemBuilder sorter = new ItemBuilder(Material.HOPPER);

        for (int i = 0; i < SortMapBy.values().length; i++) {
            SortMapBy sort = SortMapBy.values()[i];

            String s = C.White + (sort == _sortBy ? C.Bold : "") + "Order by: " + C.Blue + (sort == _sortBy ? C.Bold :
                    "") + (sort == SortMapBy.MODIFIED ? "Loaded" : sort.name());

            if (i == 0)
                sorter.setTitle(s);
            else
                sorter.addLore(s);
        }

        items.add(Pair.of(sorter.build(), new IButton() {

            @Override
            public boolean onClick(ClickType clickType) {
                _sortBy = SortMapBy.values()[(_sortBy.ordinal() + 1) % SortMapBy.values().length];
                Preference.setPreference(getPlayer(), _sortMapBy, _sortBy.name());

                buildPage();

                return true;
            }
        }));

        returns.addAll(layout.format(items));
    }
}
