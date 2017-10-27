package me.libraryaddict.core.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

public class UtilInv {
    private static int _uniqueName;
    private static Enchantment _visual;

    static {
        try {
            Field field = Enchantment.class.getDeclaredField("acceptingNew");
            field.setAccessible(true);
            field.setBoolean(null, true);

            int i = 0;

            while (true) {
                if (Enchantment.getById(++i) != null)
                    continue;

                _visual = new Enchantment(i) {

                    @Override
                    public boolean canEnchantItem(ItemStack item) {
                        return true;
                    }

                    @Override
                    public boolean conflictsWith(Enchantment other) {
                        return false;
                    }

                    @Override
                    public EnchantmentTarget getItemTarget() {
                        return EnchantmentTarget.ALL;
                    }

                    @Override
                    public int getMaxLevel() {
                        return 1;
                    }

                    @Override
                    public String getName() {
                        return "Dull";
                    }

                    @Override
                    public int getStartLevel() {
                        return 1;
                    }

                    @Override
                    public boolean isTreasure() {
                        return false;
                    }

                    @Override
                    public boolean isCursed() {
                        return false;
                    }
                };

                Enchantment.registerEnchantment(_visual);
                break;
            }
        }
        catch (Exception e) {
            UtilError.handle(e);
        }
    }

    public static void addItem(Player player, ItemStack itemstack) {
        if (itemstack == null || itemstack.getType() == Material.AIR)
            return;

        itemstack = itemstack.clone();

        for (ItemStack item : getNonClonedInventory(player)) {
            if (itemstack.getAmount() <= 0)
                break;

            if (!item.isSimilar(itemstack)) {
                continue;
            }

            if (item.getMaxStackSize() <= item.getAmount()) {
                continue;
            }

            int canGive = item.getMaxStackSize() - item.getAmount();

            canGive = Math.min(canGive, itemstack.getAmount());

            if (canGive > 0) {
                item.setAmount(item.getAmount() + canGive);
                itemstack.setAmount(itemstack.getAmount() - canGive);
            }
        }

        while (itemstack.getAmount() > 0) {
            ItemStack toGive = itemstack.clone();
            toGive.setAmount(Math.min(toGive.getAmount(), toGive.getType().getMaxStackSize()));

            player.getInventory().addItem(toGive);

            itemstack.setAmount(itemstack.getAmount() - toGive.getAmount());
        }

        player.updateInventory();
    }

    public static void clearInventory(Player player) {
        player.getInventory().clear();
        player.setItemOnCursor(new ItemStack(Material.AIR));
        player.getInventory().setArmorContents(new ItemStack[4]);

        Inventory topInv = player.getOpenInventory().getTopInventory();

        if (topInv.getHolder() == player && topInv.getType() == InventoryType.CRAFTING) {
            topInv.clear();
        }
    }

    public static boolean contains(Player player, ItemStack toCompare) {
        for (ItemStack item : getInventory(player)) {
            if (isSimilar(item, toCompare))
                return true;
        }

        return false;
    }

    public static boolean contains(Player player, Material material) {
        for (ItemStack item : getInventory(player)) {
            if (item.getType() == material)
                return true;
        }

        return false;
    }

    public static int count(Player player, Material material) {
        int amount = 0;

        for (ItemStack item : getInventory(player)) {
            if (item.getType() != material) {
                continue;
            }

            amount += item.getAmount();
        }

        return amount;
    }

    public static double getDamage(ItemStack item) {
        if (item == null)
            return 0;

        Material mat = item.getType();

        double dmg = 1;

        if (mat.name().contains("DIAMOND_"))
            dmg = 3;
        else if (mat.name().contains("IRON_"))
            dmg = 2;
        else if (mat.name().contains("STONE_"))
            dmg = 1;
        else if (mat.name().contains("GOLD_"))
            dmg = 0.5;
        else if (mat.name().contains("WOOD_"))
            dmg = 0;

        if (mat.name().contains("_SWORD"))
            dmg += 4;
        else if (mat.name().contains("_AXE"))
            dmg += 3;
        else if (mat.name().contains("_PICKAXE"))
            dmg += 2;
        else if (mat.name().contains("_SPADE"))
            dmg += 1;
        else
            dmg = 1;

        return dmg;
    }

    public static ItemStack getHolding(Player player, EquipmentSlot slot, Material material) {
        PlayerInventory inv = player.getInventory();

        if (slot == EquipmentSlot.HAND && inv.getItemInMainHand() != null && inv.getItemInMainHand().getType() == material)
            return inv.getItemInMainHand();

        if (slot == EquipmentSlot.OFF_HAND && inv.getItemInOffHand() != null && inv.getItemInOffHand().getType() == material)
            return inv.getItemInOffHand();

        return null;
    }

    public static ItemStack getHolding(Player player, Material material) {
        PlayerInventory inv = player.getInventory();

        if (inv.getItemInMainHand() != null && inv.getItemInMainHand().getType() == material)
            return inv.getItemInMainHand();

        if (inv.getItemInOffHand() != null && inv.getItemInOffHand().getType() == material)
            return inv.getItemInOffHand();

        return null;
    }

    public static ArrayList<ItemStack> getInventory(Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        for (ItemStack item : getNonClonedInventory(player)) {
            items.add(item.clone());
        }

        return items;
    }

    public static ItemStack getItem(Player player, EquipmentSlot equipSlot) {
        PlayerInventory inv = player.getInventory();

        switch (equipSlot) {
        case HAND:
            return inv.getItemInMainHand();
        case OFF_HAND:
            return inv.getItemInOffHand();
        case FEET:
            return inv.getBoots();
        case LEGS:
            return inv.getLeggings();
        case CHEST:
            return inv.getChestplate();
        case HEAD:
            return inv.getHelmet();
        default:
            break;
        }

        return null;
    }

    public static Material getLeatherItem(EquipmentSlot slot) {
        switch (slot) {
        case HEAD:
            return Material.LEATHER_HELMET;
        case CHEST:
            return Material.LEATHER_CHESTPLATE;
        case LEGS:
            return Material.LEATHER_LEGGINGS;
        case FEET:
            return Material.LEATHER_BOOTS;
        default:
            return null;
        }
    }

    public static ArrayList<ItemStack> getNonClonedInventory(Player player) {
        ArrayList<ItemStack> items = new ArrayList<ItemStack>();

        items.addAll(Arrays.asList(player.getInventory().getContents()));

        items.add(player.getItemOnCursor());

        // items.addAll(Arrays.asList(player.getInventory().getArmorContents()));

        Inventory topInv = player.getOpenInventory().getTopInventory();

        if (topInv.getHolder() == player && topInv.getType() == InventoryType.CRAFTING) {
            items.addAll(Arrays.asList(topInv.getContents()));
        }

        // Do this so the two held items are the first items the plugin checks.
        ItemStack handItem = player.getInventory().getItemInMainHand();

        items.remove(handItem);

        items.add(1, handItem);

        Iterator<ItemStack> itel = items.iterator();

        // Remove non-items
        while (itel.hasNext()) {
            ItemStack item = itel.next();

            if (item != null && item.getType() != Material.AIR) {
                continue;
            }

            itel.remove();
        }

        return items;

    }

    public static String getUniqueId() {
        String string = "";

        for (char c : ("" + _uniqueName++).toCharArray()) {
            string += ChatColor.COLOR_CHAR + "" + c;
        }

        return string;
    }

    public static Enchantment getVisual() {
        return _visual;
    }

    public static boolean isHolding(Player player, EquipmentSlot slot, Material material) {
        return getHolding(player, slot, material) != null;
    }

    public static boolean isHolding(Player player, ItemStack item) {
        return isSimilar(player.getInventory().getItemInMainHand(), item)
                || isSimilar(player.getInventory().getItemInOffHand(), item);
    }

    public static boolean isHolding(Player player, Material material) {
        return getHolding(player, material) != null;
    }

    public static boolean isHoldingItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item != null && item.getType() != Material.AIR)
            return true;

        item = player.getInventory().getItemInOffHand();

        if (item != null && item.getType() != Material.AIR)
            return true;

        return false;
    }

    public static boolean isItem(ItemStack item, Material material) {
        return item != null && item.getType() == material;
    }

    public static boolean isSimilar(ItemStack item, ItemStack toCompare) {
        if (item == null || toCompare == null)
            return item == toCompare;

        if (item.getType() != toCompare.getType())
            return false;

        if (item.getDurability() != toCompare.getDurability())
            return false;

        if (!item.hasItemMeta() || !toCompare.hasItemMeta())
            return false;

        ItemMeta meta1 = item.getItemMeta();
        ItemMeta meta2 = toCompare.getItemMeta();

        if (meta1.hasDisplayName() != meta2.hasDisplayName())
            return false;

        if (meta1.hasDisplayName()
                && !ChatColor.stripColor(meta1.getDisplayName()).equals(ChatColor.stripColor(meta2.getDisplayName())))
            return false;

        if (meta1.hasLore() != meta2.hasLore())
            return false;

        if (meta1.hasLore()) {
            List<String> lore1 = meta1.getLore();
            List<String> lore2 = meta2.getLore();

            if (lore1.size() != lore2.size())
                return false;

            for (int i = 0; i < lore1.size(); i++) {
                String l1 = lore1.get(i);
                String l2 = lore2.get(i);

                if ((l1 == null) != (l2 == null))
                    return false;

                if (l1 == null)
                    continue;

                if (!ChatColor.stripColor(l1).equals(ChatColor.stripColor(l2)))
                    return false;
            }
        }

        return true;
    }

    private static boolean isSimilar(ItemStack toCheck, ItemStack removeItem, Material removeMat) {
        if (toCheck == null || (removeItem == null && removeMat == null))
            return false;

        if (removeItem != null)
            return isSimilar(toCheck, removeItem);

        return toCheck.getType() == removeMat;
    }

    public static int remove(Player player, ItemStack toRemove) {
        return remove(player, toRemove, null, 99999);
    }

    public static int remove(Player player, ItemStack toRemove, int amount) {
        return remove(player, toRemove, null, amount);
    }

    public static int remove(Player player, ItemStack removeItem, Material removeMat, int amount) {
        PlayerInventory inv = player.getInventory();

        for (EquipmentSlot heldSlot : new EquipmentSlot[] {
                EquipmentSlot.OFF_HAND, EquipmentSlot.HAND
        }) {
            if (amount <= 0)
                break;

            ItemStack item = getItem(player, heldSlot);

            if (!isSimilar(item, removeItem, removeMat))
                continue;

            if (item.getAmount() > amount) {
                item.setAmount(item.getAmount() - amount);
                amount = 0;
            }
            else {
                setItem(player, heldSlot, new ItemStack(Material.AIR));

                amount -= item.getAmount();
            }
        }

        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (amount <= 0)
                break;

            ItemStack item = inv.getItem(slot);

            if (!isSimilar(item, removeItem, removeMat))
                continue;

            if (item.getAmount() > amount) {
                item.setAmount(item.getAmount() - amount);

                amount = 0;
            }
            else {
                inv.setItem(slot, new ItemStack(Material.AIR));

                amount -= item.getAmount();
            }
        }

        if (amount > 0 && isSimilar(player.getItemOnCursor(), removeItem, removeMat)) {
            ItemStack item = player.getItemOnCursor();

            if (item.getAmount() > amount) {
                item.setAmount(item.getAmount() - amount);

                amount = 0;
            }
            else {
                player.setItemOnCursor(new ItemStack(Material.AIR));

                amount -= item.getAmount();
            }
        }

        if (player.getOpenInventory().getTopInventory().getType() == InventoryType.CRAFTING) {
            Inventory topInv = player.getOpenInventory().getTopInventory();

            for (int slot = 0; slot < topInv.getSize(); slot++) {
                if (amount <= 0)
                    break;

                ItemStack item = topInv.getItem(slot);

                if (!isSimilar(item, removeItem, removeMat))
                    continue;

                if (item.getAmount() > amount) {
                    item.setAmount(item.getAmount() - amount);

                    amount = 0;
                }
                else {
                    topInv.setItem(slot, new ItemStack(Material.AIR));

                    amount -= item.getAmount();
                }
            }
        }

        ItemStack[] armor = inv.getArmorContents();

        for (int slot = 0; slot < armor.length; slot++) {
            if (amount <= 0)
                break;

            ItemStack item = armor[slot];

            if (!isSimilar(item, removeItem, removeMat))
                continue;

            if (item.getAmount() > amount) {
                item.setAmount(item.getAmount() - amount);

                amount = 0;
            }
            else {
                armor[slot] = new ItemStack(Material.AIR);

                amount -= item.getAmount();
            }
        }

        inv.setArmorContents(armor);

        player.updateInventory();

        return amount;
    }

    public static int remove(Player player, Material material) {
        return remove(player, material, 99999);
    }

    public static int remove(Player player, Material toRemove, int amount) {
        return remove(player, null, toRemove, amount);
    }

    public static void setItem(Player player, EquipmentSlot equipSlot, ItemStack item) {
        PlayerInventory inv = player.getInventory();

        switch (equipSlot) {
        case HAND:
            inv.setItemInMainHand(item);
            break;
        case OFF_HAND:
            inv.setItemInOffHand(item);
            break;
        case FEET:
            inv.setBoots(item);
            break;
        case LEGS:
            inv.setLeggings(item);
            break;
        case CHEST:
            inv.setChestplate(item);
            break;
        case HEAD:
            inv.setHelmet(item);
            break;
        default:
            break;
        }
    }
}
