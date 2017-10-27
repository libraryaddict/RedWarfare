package me.libraryaddict.core.preference;

import java.util.ArrayList;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.CommandManager;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.messaging.MessageManager;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.preference.commands.CommandPreference;
import me.libraryaddict.core.ranks.RankManager;
import me.libraryaddict.core.utils.UtilInv;

public class PreferenceManager extends MiniPlugin
{
    private ArrayList<PreferenceItem> _items = new ArrayList<PreferenceItem>();
    private ItemStack _openPreferences = new ItemBuilder(Material.SUGAR).setTitle(C.Blue + "Open Preferences").build();
    private RankManager _rankManager;

    public PreferenceManager(JavaPlugin plugin, RankManager rankManager, CommandManager commandManager,
            MessageManager messageManager)
    {
        super(plugin, "Preference Manager");

        _rankManager = rankManager;

        commandManager.registerCommand(new CommandPreference(this));

        PreferenceItem recieveMessages = new PreferenceItem("Receive Messages", messageManager.getReceiveMessages(),
                new ItemBuilder(Material.PAPER).addLore("Receive messages from friends, allies, enemies and more!").build());

        register(recieveMessages);
    }

    public ItemStack getIcon()
    {
        return _openPreferences;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!event.getAction().name().contains("RIGHT"))
            return;

        if (!UtilInv.isSimilar(event.getItem(), getIcon()))
            return;

        openInventory(event.getPlayer());
    }

    public void openInventory(Player player)
    {
        new PreferenceInventory(player, _rankManager.getRank(player), _items).openInventory();
    }

    public void register(PreferenceItem item)
    {
        _items.add(item);
    }

    public void unregister(PreferenceItem pref)
    {
        _items.remove(pref);
    }
}
