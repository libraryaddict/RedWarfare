package me.libraryaddict.build.inventories;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.inventory.BasicInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.ItemLayout;
import me.libraryaddict.core.utils.UtilError;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArmorStand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;

public class ArmorstandInventory extends BasicInventory {
    private enum BodyType {
        AHEAD {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setHeadPose(rotateType.rotate(stand.getHeadPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.LEATHER_HELMET;
            }

            @Override
            public String getName() {
                return "Head";
            }
        },

        BLEFT_ARM {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setLeftArmPose(rotateType.rotate(stand.getLeftArmPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.WOOD_SWORD;
            }

            @Override
            public String getName() {
                return "Left Arm";
            }
        },

        CCHESTPLATE {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setBodyPose(rotateType.rotate(stand.getBodyPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.LEATHER_CHESTPLATE;
            }

            @Override
            public String getName() {
                return "Chestplate";
            }
        },

        DRIGHT_ARM {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setRightArmPose(rotateType.rotate(stand.getRightArmPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.SHIELD;
            }

            @Override
            public String getName() {
                return "Right Arm";
            }
        },

        ELEFT_LEG {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setLeftLegPose(rotateType.rotate(stand.getLeftLegPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.STICK;
            }

            @Override
            public String getName() {
                return "Left Leg";
            }
        },

        FRIGHT_LEG {
            @Override
            public void rotate(ArmorStand stand, RotateType rotateType, double amount) {
                stand.setRightLegPose(rotateType.rotate(stand.getRightLegPose(), amount));
            }

            @Override
            public Material getBodyPart() {
                return Material.STICK;
            }

            @Override
            public String getName() {
                return "Right Leg";
            }
        };

        public abstract void rotate(ArmorStand stand, RotateType rotateType, double amount);

        public abstract Material getBodyPart();

        public abstract String getName();
    }

    private enum RotateType {
        PITCH {
            @Override
            public EulerAngle rotate(EulerAngle vec, double amount) {
                vec.setX(vec.getX() + amount);

                return vec;
            }
        },

        YAW {
            @Override
            public EulerAngle rotate(EulerAngle vec, double amount) {
                vec.setY(vec.getY() + amount);

                return vec;
            }
        },

        ROLL {
            @Override
            public EulerAngle rotate(EulerAngle vec, double amount) {
                vec.setZ(vec.getZ() + amount);

                return vec;
            }
        };

        public abstract EulerAngle rotate(EulerAngle vec, double amount);
    }

    private enum SlotPart {
        MAIN_HAND("Left Hand", 0),
        OFFHAND("Right Hand", 5),
        FEET("Feet", 1),
        LEGS("Leggings", 2),
        CHEST("Chestplate", 3),
        HEAD("Head", 4);

        private int _id;
        private String _name;

        private SlotPart(String name, int id) {
            _name = name;
            _id = id;
        }

        public String getName() {
            return _name;
        }

        public int getId() {
            return _id;
        }
    }

    private ArmorStand _armorstand;
    private BodyType _bodyType = BodyType.AHEAD;
    private RotateType _rotateType = RotateType.PITCH;
    private int _rotateAmount = 90;
    private SlotPart _slotPart = SlotPart.MAIN_HAND;

    public ArmorstandInventory(Player player, ArmorStand armorstand) {
        super(player, "Modify Armorstand");

        _armorstand = armorstand;

        registerButtons();
    }

    private ArmorStand getStand() {
        return _armorstand;
    }

    private ItemStack getItem(ItemStack equip, Material mat) {
        if (equip != null && equip.getType() != Material.AIR)
            return equip;

        return new ItemStack(mat);
    }

    private void registerButtons() {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        items.add(Pair.of(new ItemBuilder(Material.ARMOR_STAND).setTitle(C.Gold + "Clone")
                .addLore(C.Green + "Clone this armorstand!").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                getPlayer().sendMessage(C.Aqua + "TODO");
                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(getItem(getStand().getHelmet(), Material.SKULL_ITEM))
                .setTitle("Click with item to set head").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                ItemStack cursor = getPlayer().getItemOnCursor();

                if (cursor != null && cursor.getType() != Material.AIR)
                    cursor = null;

                getStand().setHelmet(cursor);

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(getItem(getStand().getEquipment().getItemInMainHand(), Material.STICK))
                .setTitle("Click with item to set left arm").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                ItemStack cursor = getPlayer().getItemOnCursor();

                if (cursor != null && cursor.getType() != Material.AIR)
                    cursor = null;

                getStand().getEquipment().setItemInMainHand(cursor);

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(getItem(getStand().getChestplate(), Material.LEATHER_CHESTPLATE))
                .setTitle("Click with item to set chestplate").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                ItemStack cursor = getPlayer().getItemOnCursor();

                if (cursor != null && cursor.getType() != Material.AIR)
                    cursor = null;

                getStand().setChestplate(cursor);

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(getItem(getStand().getEquipment().getItemInOffHand(), Material.STICK))
                .setTitle("Click with item to set right arm").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                ItemStack cursor = getPlayer().getItemOnCursor();

                if (cursor != null && cursor.getType() != Material.AIR)
                    cursor = null;

                getStand().getEquipment().setItemInOffHand(cursor);

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.REDSTONE).setTitle("Decrease rotation").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                _bodyType.rotate(getStand(), _rotateType, -_rotateAmount);

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(getItem(getStand().getEquipment().getBoots(), Material.LEATHER_BOOTS))
                .setTitle("Click with item to set boots").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                ItemStack cursor = getPlayer().getItemOnCursor();

                if (cursor != null && cursor.getType() != Material.AIR)
                    cursor = null;

                getStand().setBoots(cursor);

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(_bodyType.getBodyPart()).setTitle("Current Part: " + _bodyType.getName())
                .build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                _bodyType = BodyType.values()[(BodyType.values().length + 1) % BodyType.values().length];

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.APPLE).setTitle("Rotation Amount").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                if (clickType.isLeftClick()) {
                    if (_rotateAmount >= 90) {
                        getPlayer().sendMessage(C.Red + "Cannot increase it any further!");
                    } else {
                        switch (_rotateAmount) {
                            case 1:
                                _rotateAmount = 5;
                                break;
                            case 5:
                                _rotateAmount = 30;
                                break;
                            case 30:
                                _rotateAmount = 90;
                                break;
                            default:
                                _rotateAmount = 90;
                                break;
                        }
                    }
                } else {
                    if (_rotateAmount <= 1) {
                        getPlayer().sendMessage(C.Red + "Cannot decrease it any further!");
                    } else {
                        switch (_rotateAmount) {
                            case 90:
                                _rotateAmount = 30;
                                break;
                            case 30:
                                _rotateAmount = 5;
                                break;
                            case 5:
                                _rotateAmount = 1;
                                break;
                            default:
                                _rotateAmount = 1;
                                break;
                        }
                    }
                }

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.STICK).setTitle("Increase rotation").build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                _bodyType.rotate(getStand(), _rotateType, _rotateAmount);

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.POTION).setTitle("Invisibility: " + !getStand().isVisible()).build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setVisible(!getStand().isVisible());

                        registerButtons();

                        return true;
                    }
                }));

        items.add(Pair.of(new ItemBuilder(Material.COMMAND).setTitle("Invulnerable: " + getStand().isInvulnerable())
                .build(), new IButton() {
            @Override
            public boolean onClick(ClickType clickType) {
                getStand().setInvulnerable(!getStand().isInvulnerable());

                registerButtons();

                return true;
            }
        }));

        items.add(Pair.of(new ItemBuilder(Material.STEP).setTitle("Base Plate: " + getStand().hasBasePlate()).build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setBasePlate(!getStand().hasBasePlate());

                        registerButtons();

                        return true;
                    }
                }));

        items.add(
                Pair.of(new ItemBuilder(Material.CHORUS_FRUIT).setTitle("Gravity Affected: " + getStand().hasGravity())
                        .build(), new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setGravity(!getStand().hasGravity());

                        registerButtons();

                        return true;
                    }
                }));

        items.add(Pair.of(new ItemBuilder(Material.STICK).setTitle("Show Arms: " + getStand().hasArms()).build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setArms(!getStand().hasArms());

                        registerButtons();

                        return true;
                    }
                }));

        items.add(Pair.of(new ItemBuilder(Material.SEEDS).setTitle("Small: " + getStand().isSmall()).build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setSmall(!getStand().isSmall());

                        registerButtons();

                        return true;
                    }
                }));

        try {
            Field slotsField = EntityArmorStand.class.getDeclaredField("bB");
            slotsField.setAccessible(true);

            ItemBuilder lockSlots = new ItemBuilder(Material.BARRIER).setTitle("Lock Slots")
                    .addLore(C.Green + "Right click to navigate list, left click to toggle!");

            final int value = slotsField.getInt(((CraftArmorStand) getStand()).getHandle());

            for (SlotPart slot : SlotPart.values()) {
                lockSlots.addLore((_slotPart == slot ? C.White + C.Bold : C.Green) + slot.getName() + ": " + (
                        (value & 1 << slot.getId()) != 0 ? "Locked" : "Unlocked"));
            }

            items.add(Pair.of(lockSlots.build(), new IButton() {
                @Override
                public boolean onClick(ClickType clickType) {
                    boolean locked = (value & 1 << _slotPart.getId()) != 0;

                    int newValue = value;

                    if (locked)
                        newValue = newValue | 1 << _slotPart.getId();
                    else
                        newValue = newValue & (1 << _slotPart.getId() ^ 0xFFFFFFFF);

                    try {

                        slotsField.set(((CraftArmorStand) getStand()).getHandle(), newValue);
                    }
                    catch (Exception ex) {
                        UtilError.handle(ex);
                    }

                    registerButtons();

                    return true;
                }
            }));
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        items.add(Pair.of(new ItemBuilder(Material.SUGAR).setTitle("Interactable: " + !getStand().isMarker()).build(),
                new IButton() {
                    @Override
                    public boolean onClick(ClickType clickType) {
                        getStand().setMarker(!getStand().isMarker());

                        registerButtons();

                        return true;
                    }
                }));

        Iterator<Integer> itel = new ItemLayout("XXXXXXXXO", "XXOXXXXXX", "XOOOXXOXX", "XXOXXOOXX", "XOXOXXOXX",
                "XXXXXXXXX", "OOOOOOOOO").getSlots().iterator();

        for (Pair<ItemStack, IButton> pair : items) {
            addButton(itel.next(), pair.getKey(), pair.getValue());
        }
    }
}
