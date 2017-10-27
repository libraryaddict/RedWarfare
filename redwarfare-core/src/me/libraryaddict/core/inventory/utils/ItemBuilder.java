package me.libraryaddict.core.inventory.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.libraryaddict.core.C;
import me.libraryaddict.core.utils.LineFormat;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilText;
import net.minecraft.server.v1_12_R1.GameProfileSerializer;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.*;
import java.util.Map.Entry;

public class ItemBuilder {
    private int _amount;
    private Color _color;
    private short _data;
    private final HashMap<Enchantment, Integer> _enchants = new HashMap<Enchantment, Integer>();
    private ItemStack _item;
    private HashSet<ItemFlag> _itemFlags = new HashSet<ItemFlag>();
    private final ArrayList<String> _lore = new ArrayList<String>();
    private PotionType _mainEffect;
    private Material _mat;
    private boolean _modifyBase;
    private String _playerHeadName;
    private GameProfile _skullProfile;
    private String _title;
    private boolean _unbreakable;
    private boolean _unique;

    public ItemBuilder setSkull(GameProfile profile) {
        _skullProfile = profile;

        return this;
    }

    public ItemBuilder setSkull(String profileString) {
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(profileString);

            GameProfile profile = new GameProfile(
                    UUID.fromString(((String) obj.get("id")).replaceFirst(
                            "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5")),
                    (String) obj.get("name"));

            JSONArray properties = (JSONArray) ((JSONObject) obj).get("properties");

            for (int i = 0; i < properties.size(); i++) {
                JSONObject property = (JSONObject) properties.get(i);
                String name = (String) property.get("name");
                String value = (String) property.get("value");
                String signature = property.containsKey("signature") ? (String) property.get("signature") : null;
                if (signature != null) {
                    profile.getProperties().put(name, new Property(name, value, signature));
                }
                else {
                    profile.getProperties().put(name, new Property(value, name));
                }
            }

            setSkull(profile);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return this;
    }

    public ItemBuilder(ItemStack item) {
        this(item.getType(), item.getDurability());

        _item = item;

        _amount = item.getAmount();
        _enchants.putAll(item.getEnchantments());

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta.hasDisplayName()) {
                _title = meta.getDisplayName();
            }

            if (meta.hasLore()) {
                _lore.addAll(meta.getLore());
            }

            if (meta instanceof LeatherArmorMeta) {
                setColor(((LeatherArmorMeta) meta).getColor());
            }
            else if (meta instanceof PotionMeta) {
                setPotionType(((PotionMeta) meta).getBasePotionData().getType());
            }

            _itemFlags.addAll(meta.getItemFlags());

            _unbreakable = meta.spigot().isUnbreakable();
        }
    }

    public ItemBuilder(Material mat) {
        this(mat, 1);
    }

    public ItemBuilder(Material mat, int amount) {
        this(mat, amount, (short) 0);
    }

    public ItemBuilder(Material mat, int amount, short data) {
        _mat = mat;
        _amount = amount;
        _data = data;

        setHideInfo();
    }

    public ItemBuilder(Material mat, short data) {
        this(mat, 1, data);
    }

    public ItemBuilder addDullEnchant() {
        return addEnchantment(UtilInv.getVisual(), 1);
    }

    public ItemBuilder addEnchantment(Enchantment enchant, int level) {
        if (_enchants.containsKey(enchant)) {
            _enchants.remove(enchant);
        }

        if (level > 0) {
            _enchants.put(enchant, level);
        }

        return this;
    }

    public ItemBuilder addItemFlags(ItemFlag... flags) {
        getItemFlags().addAll(Arrays.asList(flags));

        return this;
    }

    public ItemBuilder addLore(Collection<String> lores) {
        return addLore(lores, LineFormat.LORE);
    }

    public ItemBuilder addLore(Collection<String> lores, LineFormat lineFormat) {
        for (String lore : lores) {
            addLore(lore, lineFormat);
        }

        return this;
    }

    public ItemBuilder addLore(String... lores) {
        return addLore(lores, LineFormat.LORE);
    }

    public ItemBuilder addLore(String lore, LineFormat lineFormat) {
        String color = ChatColor.getLastColors(_lore.isEmpty() ? "" : _lore.get(_lore.size() - 1));

        _lore.addAll(UtilText.splitLine(color + lore, lineFormat));

        return this;
    }

    public ItemBuilder addLore(String[] description, LineFormat lineFormat) {
        return addLore(Arrays.asList(description), lineFormat);
    }

    public ItemStack build() {
        Material mat = _mat;

        if (mat == null) {
            mat = Material.AIR;
        }

        ItemStack item;

        if (_modifyBase && _item != null) {
            item = _item;

            item.setType(mat);
            item.setAmount(_amount);
            item.setDurability(_data);
        }
        else {
            item = new ItemStack(mat, _amount, _data);
        }

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (_title != null) {
                meta.setDisplayName(_title);
            }

            if (isUnique()) {
                meta.setDisplayName(meta.getDisplayName() + UtilInv.getUniqueId());
            }

            if (!_lore.isEmpty()) {
                meta.setLore(_lore);
            }

            if (meta instanceof LeatherArmorMeta && _color != null) {
                ((LeatherArmorMeta) meta).setColor(_color);
            }
            else if (meta instanceof SkullMeta && _playerHeadName != null) {
                ((SkullMeta) meta).setOwner(_playerHeadName);
            }
            else if (meta instanceof FireworkEffectMeta && _color != null) {
                ((FireworkEffectMeta) meta).setEffect(FireworkEffect.builder().withColor(_color).build());
            }
            else if (meta instanceof BannerMeta) {
                ((BannerMeta) meta).setBaseColor(DyeColor.getByColor(_color));
            }
            else if (meta instanceof PotionMeta && getPotionType() != null) {
                ((PotionMeta) meta).setBasePotionData(new PotionData(getPotionType()));
            }

            meta.addItemFlags(getItemFlags().toArray(new ItemFlag[0]));

            meta.spigot().setUnbreakable(isUnbreakable());

            item.setItemMeta(meta);
        }

        item.addUnsafeEnchantments(_enchants);

        if (item.getType() == Material.SKULL_ITEM && _skullProfile != null) {
            net.minecraft.server.v1_12_R1.ItemStack nms = CraftItemStack.asNMSCopy(item);

            NBTTagCompound nbt = new NBTTagCompound();

            if (nms.getTag() == null) {
                nms.setTag(new NBTTagCompound());
            }

            GameProfileSerializer.serialize(nbt, _skullProfile);
            nms.getTag().set("SkullOwner", nbt);

            item = CraftItemStack.asBukkitCopy(nms);
        }

        return item;
    }

    public ItemBuilder clearLore() {
        _lore.clear();

        return this;
    }

    @Override
    public ItemBuilder clone() {
        ItemBuilder newBuilder = new ItemBuilder(_mat);

        newBuilder.setTitle(_title);

        for (String lore : _lore) {
            newBuilder.addLore(lore);
        }

        for (Entry<Enchantment, Integer> entry : _enchants.entrySet()) {
            newBuilder.addEnchantment(entry.getKey(), entry.getValue());
        }

        newBuilder.setColor(_color);
        // newBuilder.potion = potion;

        return newBuilder;
    }

    public HashMap<Enchantment, Integer> getAllEnchantments() {
        return _enchants;
    }

    public Color getColor() {
        return _color;
    }

    public short getData() {
        return _data;
    }

    public int getEnchantmentLevel(Enchantment enchant) {
        return _enchants.get(enchant);
    }

    public HashSet<ItemFlag> getItemFlags() {
        return _itemFlags;
    }

    public ArrayList<String> getLore() {
        return new ArrayList<String>(_lore);
    }

    public Material getMaterial() {
        return _mat;
    }

    public PotionType getPotionType() {
        return _mainEffect;
    }

    public String getTitle() {
        return _title;
    }

    public boolean hasEnchantment(Enchantment enchant) {
        return _enchants.containsKey(enchant);
    }

    public boolean isHideInfo() {
        return !getItemFlags().isEmpty();
    }

    public boolean isUnbreakable() {
        return _unbreakable;
    }

    private boolean isUnique() {
        return _unique;
    }

    public void removeEnchant(Enchantment visual) {
        _enchants.remove(visual);
    }

    public ItemBuilder setAmount(int amount) {
        _amount = amount;

        return this;
    }

    public ItemBuilder setColor(Color color) {
        _color = color;

        return this;
    }

    public ItemBuilder setData(short newData) {
        _data = newData;

        return this;
    }

    public ItemBuilder setHideInfo() {
        return setHideInfo(true);
    }

    public ItemBuilder setHideInfo(boolean hideInfo) {
        if (hideInfo) {
            ArrayList<ItemFlag> flags = new ArrayList(Arrays.asList(ItemFlag.values()));
            flags.remove(ItemFlag.HIDE_ENCHANTS);

            setItemFlags(flags);
        }
        else {
            getItemFlags().clear();
        }

        return this;
    }

    public ItemBuilder setItemFlags(Collection<ItemFlag> flags) {
        getItemFlags().clear();
        addItemFlags(flags.toArray(new ItemFlag[0]));

        return this;
    }

    public ItemBuilder setItemFlags(ItemFlag... flags) {
        getItemFlags().clear();
        addItemFlags(flags);

        return this;
    }

    public ItemBuilder setModifyBaseItem() {
        _modifyBase = true;

        return this;
    }

    public ItemBuilder setPlayerHead(String playerName) {
        _playerHeadName = playerName;

        return this;
    }

    public ItemBuilder setPotionType(PotionType potionEffect) {
        _mainEffect = potionEffect;

        return this;
    }

    public ItemBuilder setRawTitle(String title) {
        _title = title;

        return this;
    }

    public ItemBuilder setShowInfo() {
        return setHideInfo(false);
    }

    public ItemBuilder setTitle(String title) {
        if (ChatColor.stripColor(title).equals(title))
            title = C.Gray + title;

        _title = title;

        return this;
    }

    public ItemBuilder setTitle(String title, LineFormat lineFormat) {
        if (title != null) {
            String[] strings = UtilText.splitLineToArray(title, lineFormat);

            for (int i = 1; i < strings.length; i++) {
                _lore.add(strings[i]);
            }

            title = strings[0];
        }

        setTitle(title);

        return this;
    }

    public ItemBuilder setType(Material mat) {
        _mat = mat;

        return this;
    }

    public ItemBuilder setUnbreakable(boolean setUnbreakable) {
        _unbreakable = setUnbreakable;

        return this;
    }

    public ItemBuilder setUnique() {
        _unique = true;

        return this;
    }
}
