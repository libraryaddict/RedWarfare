package me.libraryaddict.core.utils;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.fancymessage.FancyMessage;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.disguise.utilities.ReflectionManager;
import net.minecraft.server.v1_12_R1.FoodMetaData;
import net.minecraft.server.v1_12_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class UtilPlayer {
    private static GameMode _defaultGamemode = GameMode.SURVIVAL;
    private static ConcurrentLinkedQueue<Pair<Player, FancyMessage>> _fancyMessages = new
            ConcurrentLinkedQueue<Pair<Player, FancyMessage>>();
    private static Field _foodTicks;

    private static ConcurrentLinkedQueue<Pair<Player, String>> _messages = new ConcurrentLinkedQueue<Pair<Player,
            String>>();

    static {
        try {
            _foodTicks = FoodMetaData.class.getDeclaredField("foodTickTimer");
            _foodTicks.setAccessible(true);
        }
        catch (Exception e) {
            UtilError.handle(e);
        }
    }

    public static int feed(Player player, int food) {
        int toFeed = Math.min(20 - player.getFoodLevel(), food);

        player.setFoodLevel(player.getFoodLevel() + toFeed);

        return toFeed;
    }

    public static int getArrowsInBody(Player player) {
        return ((CraftPlayer) player).getHandle().getArrowCount();
    }

    public static GameMode getDefaultGamemode() {
        return _defaultGamemode;
    }

    public static int getFoodTicks(Player player) {
        try {
            return _foodTicks.getInt(((CraftPlayer) player).getHandle().getFoodData());
        }
        catch (Exception e) {
            UtilError.handle(e);
        }

        return 0;
    }

    public static ArrayList<Player> getPerverts(Entity entity) {
        try {
            ArrayList<Player> perverts = new ArrayList<Player>();

            Object entityTrackerEntry = ReflectionManager.getEntityTrackerEntry(entity);

            if (entityTrackerEntry != null) {
                Set trackedPlayers = (Set) ReflectionManager.getNmsField("EntityTrackerEntry", "trackedPlayers")
                        .get(entityTrackerEntry);
                trackedPlayers = (Set) new HashSet(trackedPlayers).clone(); // Copy before iterating to prevent
                // ConcurrentModificationException
                for (Object p : trackedPlayers) {
                    Player player = (Player) ReflectionManager.getBukkitEntity(p);

                    perverts.add(player);
                }
            }

            return perverts;
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static Player getPlayer(int entityId) {
        for (Player player : getPlayers()) {
            if (player.getEntityId() == entityId) {
                return player;
            }
        }

        return null;
    }

    public static ArrayList<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<Player>(Bukkit.getOnlinePlayers());

        Collections.shuffle(players, UtilMath.random());

        return players;
    }

    public static void hideToAll(Player player) {
        for (Player otherPlayer : getPlayers()) {
            otherPlayer.hidePlayer(player);
        }
    }

    public static void init(JavaPlugin plugin) {
        new BukkitRunnable() {
            public void run() {
                while (_messages.peek() != null) {
                    Pair<Player, String> message = _messages.poll();

                    message.getKey().sendMessage(message.getValue());
                }

                while (_fancyMessages.peek() != null) {
                    Pair<Player, FancyMessage> message = _fancyMessages.poll();

                    message.getValue().send(message.getKey());
                }
            }
        }.runTaskTimer(plugin, 0, 0);
    }

    /**
     * Reset all his info, like health, hunger, etc.
     */
    public static void resetState(Player player) {
        player.setExp(0);
        player.setLevel(0);

        player.setFoodLevel(20);

        player.setMaxHealth(20);
        player.setHealth(player.getMaxHealth());

        player.setFireTicks(0);

        player.setSneaking(false);
        player.setSprinting(false);

        if (player.isGlowing())
            player.setGlowing(false);

        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }

        player.setGameMode(getDefaultGamemode());

        player.setAllowFlight(false);
        player.setFlying(false);

        player.setWalkSpeed(0.2F);
        player.setFlySpeed(0.1F);

        player.setFallDistance(0);

        UtilPlayer.setArrowsInBody(player, 0);

        UtilInv.clearInventory(player);

        if (player.getOpenInventory().getTopInventory().getHolder() != player)
            player.closeInventory();

        player.setCollidable(true);

        ConditionManager.clearConditions(player);

        Recharge.clear(player);
    }

    public static void sendMessage(Player player, FancyMessage message) {
        _fancyMessages.add(Pair.of(player, message));
    }

    public static void sendMessage(Player player, String message) {
        _messages.add(Pair.of(player, message));
    }

    public static void sendPacket(Player player, Collection<PacketContainer> packets) {
        try {
            for (PacketContainer packet : packets) {
                if (packet == null)
                    continue;

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            }
        }
        catch (InvocationTargetException e) {
            UtilError.handle(e);
        }
    }

    public static void sendPacket(Player player, Packet... packets) {
        for (Packet packet : packets) {
            if (packet == null)
                continue;

            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void sendPacket(Player player, PacketContainer... packets) {
        try {
            for (PacketContainer packet : packets) {
                if (packet == null)
                    continue;

                ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet, false);
            }
        }
        catch (InvocationTargetException e) {
            UtilError.handle(e);
        }
    }

    public static void setArrowDespawnTimer(Player player, int timer) {
        ((CraftPlayer) player).getHandle().ax = timer;
    }

    public static void setArrowsInBody(Player player, int arrows) {
        ((CraftPlayer) player).getHandle().setArrowCount(Math.max(arrows, 0));
    }

    public static void setDefaultGamemode(GameMode gamemode) {
        _defaultGamemode = gamemode;
    }

    public static void setFoodTicks(Player player, int newTicks) {
        try {
            _foodTicks.set(((CraftPlayer) player).getHandle().getFoodData(), newTicks);
        }
        catch (Exception e) {
            UtilError.handle(e);
        }
    }

    public static void setSpectator(Player player) {
        resetState(player);

        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCollidable(false);

        hideToAll(player);
    }

    public static void showToAll(Player player) {
        System.out.println("All players can now see " + player.getName());
        for (Player otherPlayer : getPlayers()) {
            otherPlayer.showPlayer(player);
        }
    }

    public static void tele(Entity player, Entity destination) {
        tele(player, destination.getLocation());
    }

    public static void tele(Entity player, Location location) {
        location = location.clone();

        player.eject();
        player.leaveVehicle();

        List<Entity> entities = player.getNearbyEntities(1, 1, 1);

        loop:

        while (true) {
            for (Entity entity : entities) {
                if (UtilLoc.getDistance(location, entity.getLocation()) > 0.1) {
                    continue;
                }

                location.add(UtilMath.rr(-0.01, 0.01), UtilMath.rr(0.01), UtilMath.rr(-0.01, 0.01));

                continue loop;
            }

            break;
        }

        player.teleport(location);
    }
}
