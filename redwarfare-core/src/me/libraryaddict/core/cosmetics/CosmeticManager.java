package me.libraryaddict.core.cosmetics;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.C;
import me.libraryaddict.core.CentralManager;
import me.libraryaddict.core.cosmetics.types.CosmeticBodyguards;
import me.libraryaddict.core.cosmetics.types.CosmeticCloud;
import me.libraryaddict.core.cosmetics.types.CosmeticCreeperAura;
import me.libraryaddict.core.cosmetics.types.disguises.CosmeticSheep;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.utils.UtilInv;

public class CosmeticManager extends MiniPlugin
{
    private CentralManager _centralManager;
    private ItemStack _cosmeticItem = new ItemBuilder(Material.GOLD_INGOT).setTitle(C.Blue + "Open Cosmetics").build();
    private ArrayList<Cosmetic> _cosmetics = new ArrayList<Cosmetic>();
    private boolean _enabled;

    public CosmeticManager(JavaPlugin plugin, CentralManager centralManager)
    {
        super(plugin, "Cosmetic Manager");

        _centralManager = centralManager;

        register(new CosmeticCloud());
        register(new CosmeticBodyguards());
        register(new CosmeticCreeperAura());
        register(new CosmeticSheep());
        // register(new CosmeticBlink());
    }

    public ItemStack getItem()
    {
        return _cosmeticItem;
    }

    public boolean isEnabled()
    {
        return _enabled;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (!UtilInv.isSimilar(event.getItem(), _cosmeticItem))
        {
            return;
        }

        if (!event.getAction().name().contains("RIGHT"))
            return;

        new CosmeticInventory(_cosmetics, event.getPlayer()).openInventory();
    }

    public void register(Cosmetic cosmetic)
    {
        _cosmetics.add(cosmetic);

        cosmetic.register(_centralManager);

        Bukkit.getPluginManager().registerEvents(cosmetic, getPlugin());
    }

    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }

}
