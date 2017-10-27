package me.libraryaddict.build.inventories;

import me.libraryaddict.build.database.MysqlAddReview;
import me.libraryaddict.build.database.MysqlRemoveReview;
import me.libraryaddict.build.managers.WorldManager;
import me.libraryaddict.build.types.MapInfo;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.player.types.Currency;
import me.libraryaddict.core.player.types.Currency.CurrencyType;
import me.libraryaddict.core.recharge.Recharge;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;

public class ReviewMapInventory extends BasicInventory {
    private WorldInfo _worldInfo;
    private WorldManager _worldManager;

    public ReviewMapInventory(Player player, WorldManager worldManager, WorldInfo worldInfo) {
        super(player, "Review Map", 36);

        _worldManager = worldManager;
        _worldInfo = worldInfo;

        buildPage();
    }

    protected void buildPage() {
        Iterator<Integer> itel = new ItemLayout("XXX XOX XXX", "XXX XXX XXX", "XXO OOO OXX", "XXX XXX XXO").getSlots()
                .iterator();

        MapInfo mapInfo = _worldInfo.getData();

        addItem(itel.next(), mapInfo.getIcon().build());

        addButton(itel.next(), new ItemBuilder(Material.INK_SACK, 1, (short) 3)
                .setTitle(C.DAqua + C.Bold + "RATING: " + C.Aqua + "POOP")
                .addLore(C.Yellow + "This map is terrible! You never want to see this map again!",
                        C.Yellow + "Costs: " + C.Gold + C.Bold + "3 credits").build(), getButton(1, "Poop"));

        addButton(itel.next(),
                new ItemBuilder(Material.DIRT, 2).setTitle(C.DAqua + C.Bold + "RATING: " + C.Aqua + "MEH")
                        .addLore(C.Yellow + "Not a very good map..").build(), getButton(2, "Meh"));

        addButton(itel.next(),
                new ItemBuilder(Material.INK_SACK, 3).setTitle(C.DAqua + C.Bold + "RATING: " + C.Aqua + "ALRIGHT")
                        .addLore(C.Yellow + "This map is alright, maybe when you're bored of all the other maps")
                        .build(), getButton(3, "Alright"));

        addButton(itel.next(),
                new ItemBuilder(Material.GOLD_INGOT, 4).setTitle(C.DAqua + C.Bold + "RATING: " + C.Aqua + "GREAT")
                        .addLore(C.Yellow + "What a great map! Its well designed and fun to play on!").build(),
                getButton(4, "Great"));

        addButton(itel.next(),
                new ItemBuilder(Material.DIAMOND, 5).setTitle(C.DAqua + C.Bold + "RATING: " + C.Aqua + "LEGENDARY")
                        .addLore(C.Yellow + "Map is legendary!", C.Yellow + "Costs: " + C.Gold + C.Bold + "3 credits")
                        .build(), getButton(5, "Legendary"));

        addButton(itel.next(), new ItemBuilder(Material.REDSTONE).setTitle(C.Red + "Delete review").build(),
                new IButton() {

                    @Override
                    public boolean onClick(ClickType clickType) {
                        getPlayer().sendMessage(C.Red + "Your review has been deleted!");

                        new BukkitRunnable() {
                            public void run() {
                                new MysqlRemoveReview(mapInfo.getUUID(), getPlayer().getUniqueId());
                                mapInfo.recalculateReviews();
                            }
                        }.runTaskAsynchronously(_worldManager.getPlugin());

                        return true;
                    }
                });
    }

    private IButton getButton(int rating, String type) {
        return new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                if (!Recharge.canUse(getPlayer(), "Review Map")) {
                    getPlayer().sendMessage(C.Red + "Small delay between reviewing maps!");

                    return true;
                }

                Recharge.use(getPlayer(), "Review Map", 4000);

                new BukkitRunnable() {
                    public void run() {
                        new MysqlAddReview(_worldInfo.getData().getUUID(), getPlayer().getUniqueId(), rating);

                        _worldInfo.getData().recalculateReviews();
                    }
                }.runTaskAsynchronously(_worldManager.getPlugin());

                _worldInfo.getData().recalculateReviews();

                // Prevents review spam
                for (int i = 0; i < 3; i++) {
                    if (!Recharge.canUse(getPlayer(), "Review Announce " + i))
                        continue;

                    Recharge.use(getPlayer(), "Review Announce " + i, 30000);

                    _worldInfo.Announce(C.Gold + getPlayer()
                            .getName() + " has rated this map " + C.Yellow + C.Bold + type + C.Gold + "!");
                    break;
                }

                if (rating == 5) {
                    Currency.add(getPlayer(), CurrencyType.CREDIT, "Rate map 5", -3);

                    getPlayer().sendMessage(C.Gold + "3 credits removed");
                } else if (rating == 1) {
                    Currency.add(getPlayer(), CurrencyType.CREDIT, "Rate map 1", -3);

                    getPlayer().sendMessage(C.Gold + "3 credits removed");
                }

                return true;
            }
        };
    }
}
