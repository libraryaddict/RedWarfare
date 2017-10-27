package me.libraryaddict.arcade.map;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.managers.WorldManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.utils.LineFormat;

public class MapInventory extends BasicInventory
{
    public MapInventory(WorldManager worldManager, Player player)
    {
        super(player, C.Gold + "Vote for a map", 18);

        updateChoices(worldManager);
    }

    public void updateChoices(WorldManager worldManager)
    {
        ItemLayout layout = new ItemLayout("XXXXOXXXX", "XOXXOXXOX");

        ArrayList<Pair<ItemStack, IButton>> list = new ArrayList<Pair<ItemStack, IButton>>();

        for (MapInfo info : worldManager.getMapVoting())
        {
            WorldData data = info.getData();

            ItemBuilder builder = new ItemBuilder(Material.MAP).setTitle(C.DGreen + "Map: " + C.Green + data.getName()).addLore(
                    C.DGreen + "Author: " + C.Green + data.getAuthor(), C.Gold + "Votes: " + C.Yellow + info.getVotes(), "");
            builder.addLore(data.getDescription(), LineFormat.LORE);

            list.add(Pair.of(builder.build(), new IButton()
            {

                @Override
                public boolean onClick(ClickType clickType)
                {
                    worldManager.voteFor(getPlayer(), info);
                    return true;
                }
            }));
        }

        ArrayList<Integer> slots = layout.getSlots();

        addItem(slots.get(0), new ItemBuilder(Material.PAPER).setTitle("Vote for a map").build());

        for (int i = 0; i < list.size(); i++)
        {
            Pair<ItemStack, IButton> entry = list.get(i);

            addButton(slots.get(i + 1), entry.getKey(), entry.getValue());
        }
    }

}
