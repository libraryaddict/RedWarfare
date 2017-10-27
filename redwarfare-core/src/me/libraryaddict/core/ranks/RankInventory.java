package me.libraryaddict.core.ranks;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.KeyMappings;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.network.PlayerData;

public class RankInventory extends BasicInventory
{
    private PlayerData _data;

    public RankInventory(Player player, PlayerData playerData, RankManager rankManager)
    {
        super(player, "Ranks", 9);

        _data = playerData;

        int i = 0;

        for (RankInfo rankInfo : rankManager.getRankInfo(player).getInfo())
        {
            if (rankInfo.hasExpired())
                continue;

            addButton(i++, rankInfo);
        }

        openInventory();
    }

    private void addButton(int slot, RankInfo rankInfo)
    {
        ItemBuilder builder = new ItemBuilder(getItem(rankInfo.getRank()))
                .setTitle(C.Red + "Rank: " + C.Red + rankInfo.getRank().name())
                .addLore(C.Blue + "Display: " + rankInfo.isDisplay());

        builder.addLore(C.DGreen + "Expires: " + C.Green + (rankInfo.getExpires() == 0 ? "Never"
                : UtilNumber.getTime(rankInfo.getExpires() - System.currentTimeMillis(), TimeUnit.MILLISECONDS)));

        addButton(slot, builder.build(), new IButton()
        {

            @Override
            public boolean onClick(ClickType clickType)
            {
                rankInfo.setDisplay(!rankInfo.isDisplay());

                int id = KeyMappings.getKey(rankInfo.getRank().name());

                _data.getDisplayedRanks().remove((Integer) id);

                if (rankInfo.isDisplay())
                {
                    _data.getDisplayedRanks().add(id);
                }

                _data.save();

                getPlayer().sendMessage(C.Red + "Display " + rankInfo.getRank().name() + ": " + rankInfo.isDisplay());

                addButton(slot, rankInfo);
                return true;
            }
        });
    }

    private ItemStack getItem(Rank rank)
    {
        switch (rank)
        {
        case OWNER:
            return new ItemStack(Material.DIAMOND);
        case ADMIN:
            return new ItemStack(Material.REDSTONE_BLOCK);
        case MOD:
            return new ItemStack(Material.REDSTONE);
        case MAPMAKER:
            return new ItemStack(Material.MAP);
        case BUILDER:
            return new ItemStack(Material.ARMOR_STAND);
        case MVP:
            return new ItemStack(Material.INK_SACK, 1, (short) 6);
        case VIP:
            return new ItemStack(Material.INK_SACK, 1, (short) 5);
        default:
            return null;
        }
    }
}
