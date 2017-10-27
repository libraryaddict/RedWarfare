package me.libraryaddict.arcade.kits;

import java.util.Arrays;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.player.types.Owned;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.network.Pref;

public abstract class Kit
{
    private Ability[] _abilities;
    private KitAvailibility _availibility = KitAvailibility.Free;
    private int _cost;
    private String[] _description;
    private ItemStack[] _items;
    private Pref<String> _kitLayout;
    private ArcadeManager _manager;
    private String _name;

    public Kit(String name, KitAvailibility availibilty, String[] desc, Ability... abilities)
    {
        _name = name;
        _availibility = availibilty;
        _description = desc;

        setAbilities(abilities);
    }

    public Kit(String name, String[] desc, Ability... abilities)
    {
        _name = name;
        _description = desc;

        setAbilities(abilities);
    }

    public void applyKit(Player player)
    {
        giveItems(player);
    }

    public boolean canTeleportTo()
    {
        return true;
    }

    public Ability[] getAbilities()
    {
        return _abilities;
    }

    public ItemStack[] getArmor()
    {
        Material[] mats = getArmorMats();

        ItemStack[] items = new ItemStack[4];

        for (int i = 0; i < 4; i++)
        {
            if (mats[i] == null)
                continue;

            items[i] = new ItemStack(mats[i]);
        }

        return items;
    }

    public abstract Material[] getArmorMats();

    public String[] getDescription()
    {
        return _description;
    }

    public Kit getHiddenKit()
    {
        return this;
    }

    public ItemStack getIcon()
    {
        return new ItemBuilder(getMaterial()).build();
    }

    public Material[] getItemMats()
    {
        return null;
    }

    public final ItemStack[] getItems()
    {
        if (_items == null)
        {
            if (getItemMats() == null)
                throw new IllegalArgumentException("No items specified for kit " + getName());

            Material[] mats = getItemMats();

            ItemStack[] items = new ItemStack[mats.length];

            for (int i = 0; i < items.length; i++)
            {
                if (mats[i] == null)
                    continue;

                items[i] = new ItemStack(mats[i]);
            }

            setItems(items);
        }

        return _items;
    }

    public ItemStack[] getItems(Player player)
    {
        return getItems();
    }

    public KitAvailibility getKitAvailibility()
    {
        return _availibility;
    }

    public Pref<String> getKitLayout()
    {
        if (_kitLayout == null)
        {
            HashMap<Integer, Integer> slots = new HashMap<Integer, Integer>();

            for (int i = 0; i < getItems().length; i++)
            {
                if (getItems()[i] == null || getItems()[i].getType() == Material.AIR)
                    continue;

                slots.put(i, i);
            }

            _kitLayout = new Pref<String>("Game." + getManager().getGame().getName() + "." + getName() + ".Item Layout",
                    new Gson().toJson(slots), new TypeToken<HashMap<Integer, Integer>>()
                    {
                    }.getType());
        }

        return _kitLayout;
    }

    public ArcadeManager getManager()
    {
        return _manager;
    }

    public abstract Material getMaterial();

    public String getName()
    {
        return _name;
    }

    public int getPrice()
    {
        return _cost;
    }

    protected ItemStack[] getUnbreakableArmor()
    {
        ItemStack[] items = getArmor();

        for (int i = 0; i < 4; i++)
        {
            if (items[i] == null)
                continue;

            items[i] = new ItemBuilder(items[i]).setUnbreakable(true).build();
        }

        return items;
    }

    public void giveItems(Player player)
    {
        HashMap<Integer, Integer> layout = new Gson().fromJson(Preference.getPreference(player, getKitLayout()),
                getKitLayout().getToken());
        HashMap<Integer, Integer> defaultLayout = new Gson().fromJson(getKitLayout().getDefault(), getKitLayout().getToken());

        if (layout.size() != defaultLayout.size())
        {
            layout = defaultLayout;
        }
        else
        {
            for (int key : defaultLayout.keySet())
            {
                if (!layout.containsKey(key))
                {
                    layout = defaultLayout;
                    break;
                }
            }
        }

        PlayerInventory inv = player.getInventory();

        inv.setArmorContents(getManager().getGame().getOption(GameOption.UNBREAKABLE) ? getUnbreakableArmor() : getArmor());

        ItemStack[] items = getItems(player);

        for (int i = 0; i < items.length; i++)
        {
            ItemStack item = items[i];

            if (item == null || item.getType() == Material.AIR)
                continue;

            if (item != null && item.getType().getMaxDurability() > 16)
            {
                ItemBuilder builder = new ItemBuilder(item);

                if (builder.getMaterial().getMaxDurability() > 16)
                {
                    builder.setUnbreakable(true);

                    builder.setItemFlags(ItemFlag.HIDE_UNBREAKABLE);

                    if (UtilInv.getDamage(item) > 1)
                    {
                        builder.setItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    }
                }

                item = builder.build();
            }

            int slot = layout.get(i);

            inv.setItem(slot, item);
        }
    }

    public boolean isBalancedTeams()
    {
        return false;
    }

    public boolean ownsKit(Player player)
    {
        if (getKitAvailibility() == KitAvailibility.Free)
            return true;

        else if (getKitAvailibility() == KitAvailibility.Purchase)
            return Owned.has(player, getManager().getGame().getName() + ".Kit." + getName());

        else if (getKitAvailibility() == KitAvailibility.Staff)
            return getManager().getRank().getRank(player).hasRank(Rank.ADMIN);

        else if (getKitAvailibility() == KitAvailibility.Locked)
            return getManager().getRank().getRank(player).hasRank(Rank.OWNER);

        return false;
    }

    public void registerAbilities()
    {
        for (Ability ability : getAbilities())
        {
            try
            {
                ability.registerAbility();
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }

            Bukkit.getPluginManager().registerEvents(ability, getManager().getPlugin());
        }
    }

    public void setAbilities(Ability... abilities)
    {
        _abilities = abilities;

        for (Ability ability : getAbilities())
        {
            ability.setKit(this);
        }
    }

    public void setItems(ItemStack... items)
    {
        assert _items == null;

        int arrows = -1;

        for (int i = 0; i < items.length; i++)
        {
            if (!UtilInv.isItem(items[i], Material.ARROW))
                continue;

            arrows = i;
        }

        if (arrows >= 0 && (items.length < 10 || items[9] == null))
        {
            if (items.length < 10)
            {
                items = Arrays.copyOf(items, 10);
            }

            items[9] = items[arrows];
            items[arrows] = null;
        }

        _items = items;
    }

    public void setManager(ArcadeManager manager)
    {
        _manager = manager;
    }

    public void setPrice(int newCost)
    {
        if (_availibility == KitAvailibility.Free)
            _availibility = KitAvailibility.Purchase;

        _cost = newCost;
    }

    public void unregisterAbilities()
    {
        for (Ability ability : getAbilities())
        {
            try
            {
                ability.unregisterAbility();
            }
            catch (Exception ex)
            {
                UtilError.handle(ex);
            }

            HandlerList.unregisterAll(ability);
        }
    }

    public boolean usingKit(Player player)
    {
        return _manager.getGame().getKit(player) == this;
    }
}
