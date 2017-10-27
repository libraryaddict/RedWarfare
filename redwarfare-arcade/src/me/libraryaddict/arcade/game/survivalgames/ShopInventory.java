package me.libraryaddict.arcade.game.survivalgames;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.ItemLayout;

public class ShopInventory extends BasicInventory
{
    public ShopInventory(Player player, SurvivalGamesItems sgItems)
    {
        super(player, "Shop", 54);

        ItemLayout layout = new ItemLayout("OOXXXXXOO", "OXXXOXXXO", "XXOOOOOXX", "OXXXOXXXO", "OOXXOXXOO");

        ArrayList<ItemStack> items = new ArrayList<ItemStack>();
        // items.add(Pair.of(new ItemStack(Material.EXP_BOTTLE), 0));
    }

}
