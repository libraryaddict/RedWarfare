package me.libraryaddict.core.inventory;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import me.libraryaddict.core.C;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.utils.UtilError;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.Map.Entry;

public abstract class AnvilInventory extends BasicInventory {
    public enum AnvilSlot {
        INPUT_LEFT(0),
        INPUT_RIGHT(1),
        OUTPUT(2);

        private int slot;

        private AnvilSlot(int slot) {
            this.slot = slot;
        }

        public int getSlot() {
            return slot;
        }
    }

    private class FakeAnvil extends ContainerAnvil {
        public FakeAnvil(EntityHuman entityhuman) {
            super(entityhuman.inventory, entityhuman.world, new BlockPosition(0, 0, 0), entityhuman);
        }

        @Override
        public boolean canUse(EntityHuman human) {
            return true;
        }

        @Override
        public void a(String origString) {
            if (origString == null) {
                System.out.println("Null string in anvil :\"");

                origString = "";
            }

            _string = origString;

            onMessage(C.stripColor(_string));

            net.minecraft.server.v1_12_R1.ItemStack itemstack = getSlot(2).getItem();

            if (C.stripColor(_string).isEmpty()) {
                itemstack.s();
            } else {
                itemstack.g(_string);
            }

            e();
        }
    }

    private String _string;

    public AnvilInventory(Player player, String inputBox) {
        super(player, "NoTitle", 2);

        _string = inputBox;

        addItem(AnvilSlot.INPUT_LEFT, new ItemBuilder(Material.PAPER).setTitle(inputBox).build());

        addButton(AnvilSlot.OUTPUT, new ItemBuilder(Material.BOOK).setTitle(C.White + C.Bold + "SAVE").build(),
                new IButton() {

                    @Override
                    public boolean onClick(ClickType clickType) {
                        onSave(C.stripColor(_string));

                        return true;
                    }
                });
    }

    public AnvilInventory(Player player, String title, int size) {
        super(null, null);

        throw new RuntimeException("Bad constructor");
    }

    public void addButton(AnvilSlot slot, ItemStack item, IButton button) {
        if (slot == AnvilSlot.INPUT_LEFT) {
            ItemBuilder builder = new ItemBuilder(item);

            if (builder.getTitle() != null) {
                item = builder.setModifyBaseItem().setTitle(C.stripColor(builder.getTitle())).build();
            }
        }

        super.addButton(slot.getSlot(), item, button);
    }

    public void addItem(AnvilSlot slot, ItemStack item) {
        super.addItem(slot.getSlot(), item);
    }

    @Override
    public void closeInventory() {
        getInventory().clear();

        super.closeInventory();
    }

    public int getSize() {
        return 3;
    }

    public abstract void onMessage(String message);

    public abstract void onSave(String message);

    @Override
    public void openInventory() {
        EntityPlayer p = ((CraftPlayer) getPlayer()).getHandle();
        int c = p.nextContainerCounter();

        PacketContainer openCommand = new PacketContainer(PacketType.Play.Server.OPEN_WINDOW);
        openCommand.getIntegers().write(0, c);
        openCommand.getStrings().write(0, "minecraft:anvil");
        openCommand.getChatComponents().write(0, WrappedChatComponent.fromText("Anvil"));

        UtilPlayer.sendPacket(getPlayer(), openCommand);

        FakeAnvil container = new FakeAnvil(p);

        // Set their active container to the container
        p.activeContainer = container;

        // Set their active container window id to that counter stuff
        p.activeContainer.windowId = c;

        // Add the slot listener
        p.activeContainer.addSlotListener(p); // Set the items to the items from the inventory given

        _inventory = container.getBukkitView().getTopInventory();

        try {
            Field field = InventorySubcontainer.class.getDeclaredField("bukkitOwner");
            field.setAccessible(true);

            field.set(((CraftInventory) getInventory()).getInventory(), this);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        for (Entry<Integer, org.bukkit.inventory.ItemStack> entry : _items.entrySet()) {
            _inventory.setItem(entry.getKey(), entry.getValue());
        }

        getPlayer().updateInventory();

        InventoryManager.Manager.registerInventory(this);
    }
}
