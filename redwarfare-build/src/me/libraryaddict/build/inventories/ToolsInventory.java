package me.libraryaddict.build.inventories;

import me.libraryaddict.build.customdata.CustomData;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;

public class ToolsInventory extends BasicInventory {
    public ToolsInventory(Player player, CustomData customData) {
        super(player, "Tools", (int) ((Math.ceil(customData.getTools().size() / 9D) * 9) + 18 + (Math
                .ceil(customData.getButtons().size() / 9D) * 9)));

        ArrayList<ItemStack> buttons = customData.getButtons();
        ArrayList<ItemStack> tools = customData.getTools();

        int i = 0;

        for (; i < tools.size(); i++) {
            ItemStack item = tools.get(i).clone();

            addButton(i, item, new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    PlayerInventory inv = player.getInventory();

                    if (inv.firstEmpty() > 8) {
                        inv.setItemInMainHand(item);
                    } else {
                        inv.addItem(item);
                    }

                    return true;
                }
            });
        }

        i = (int) (Math.ceil(i / 9D) * 9) + 18;

        for (int a = 0; a < buttons.size(); ) {
            ItemStack item = buttons.get(a).clone();

            addButton(i, item, new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    customData.onButtonClick(player, item);

                    return true;
                }
            });

            a++;
            i++;
        }
    }
}
