package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.PacketType.Play.Server;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.PacketListener;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Registry;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.Serializer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher.WrappedDataWatcherObject;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.CustomDamageEvent.DamageRunnable;
import me.libraryaddict.core.data.ParticleColor;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.disguise.utilities.DisguiseUtilities;
import net.minecraft.server.v1_12_R1.SoundEffect;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.UUID;

public class GhostAbility extends Ability {
    private ArrayList<Pair<String, Long>> _arrows = new ArrayList<Pair<String, Long>>();
    private ArrayList<UUID> _ignore = new ArrayList<UUID>();
    private PacketListener _packetlistener;

    public GhostAbility(JavaPlugin plugin) {
        _packetlistener = new PacketAdapter(plugin, PacketType.Play.Server.ENTITY_METADATA,
                PacketType.Play.Server.NAMED_ENTITY_SPAWN, PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                try {
                    PacketContainer packet = event.getPacket();

                    if (event.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                        SoundEffect effect = (SoundEffect) event.getPacket().getModifier().read(0);
                        String sound = SoundEffect.a.b(effect).b();

                        if (!sound.startsWith("block.") || !sound.endsWith(".step")) {
                            return;
                        }

                        StructureModifier<Integer> ints = packet.getIntegers();

                        int x = ints.read(0);
                        int y = ints.read(1);
                        int z = ints.read(2);

                        for (Player player : getPlayers(true)) {
                            Location loc = player.getLocation();

                            int pX = (int) (loc.getX() * 8);
                            int pY = (int) (loc.getY() * 8);
                            int pZ = (int) (loc.getZ() * 8);

                            if (x != pX || y != pY || z != pZ)
                                continue;

                            event.setCancelled(true);
                            return;
                        }

                        return;
                    }

                    Player player = UtilPlayer.getPlayer(packet.getIntegers().read(0));

                    if (player == null)
                        return;

                    if (!isAlive(player))
                        return;

                    if (!hasAbility(player))
                        return;

                    event.setPacket(packet = packet.deepClone());

                    if (_ignore.contains(event.getPlayer().getUniqueId()))
                        return;

                    Serializer byteSerializer = Registry.get(Byte.class);
                    WrappedDataWatcherObject invis = new WrappedDataWatcherObject(MetaIndex.ENTITY_META.getIndex(),
                            byteSerializer);

                    if (event.getPacketType() == PacketType.Play.Server.NAMED_ENTITY_SPAWN) {
                        WrappedDataWatcher watcher = packet.getDataWatcherModifier().read(0);

                        watcher.setObject(invis, (byte) (((Byte) watcher.getObject(invis)) | 32));
                    } else {
                        for (WrappedWatchableObject watcher : packet.getWatchableCollectionModifier().read(0)) {
                            if (watcher.getIndex() != MetaIndex.ENTITY_META.getIndex())
                                continue;

                            watcher.setValue((byte) ((Byte) (watcher.getValue()) | 32));
                        }
                    }
                }
                catch (Exception ex) {
                    UtilError.handle(ex);
                }
            }
        };
    }

    @EventHandler(ignoreCancelled = true)
    public void onArrowHit(CustomDamageEvent event) {
        event.addRunnable(new DamageRunnable(getKit().getName() + " Hit") {
            @Override
            public void run(CustomDamageEvent event2) {
                if (!isLive())
                    return;

                Player player = event.getPlayerDamagee();

                if (player == null) {
                    return;
                }

                if (!hasAbility(player))
                    return;

                if (!isAlive(player))
                    return;

                GameTeam team = getGame().getTeam(player);

                if (team == null)
                    return;

                ParticleColor color = team.getSettings().getParticleColor();
                ArrayList<Player> enemies = UtilPlayer.getPlayers();

                enemies.remove(player);

                if (event.getAttackType() != AttackType.POISON && event.getAttackType() != AttackType.WITHER_POISON) {
                    Player[] array = enemies.toArray(new Player[0]);

                    for (int i = 0; i < 40; i++) {
                        UtilParticle.playParticle(player.getLocation()
                                .add(UtilMath.rr(-0.4, .4), UtilMath.rr(0, 1.9), UtilMath.rr(-0.4, .4)), color, array);
                    }
                }

                if (!(event.getDamager() instanceof Arrow)) {
                    return;
                }

                player.sendMessage(C.Aqua + C.Magic + "sd" + C.Aqua + "You were slammed with a arrow!");

                _arrows.add(Pair.of(player.getName(), System.currentTimeMillis()));
            }
        });
    }

    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        if (!getManager().getRank().getRank(player).hasRank(Rank.MOD))
            return;

        if (!event.getMessage().equalsIgnoreCase("/ghost"))
            return;

        event.setCancelled(true);

        if (_ignore.contains(player.getUniqueId())) {
            _ignore.remove(player.getUniqueId());
            player.sendMessage(C.Blue + "Hidden ghosts!");
        } else {
            _ignore.add(player.getUniqueId());
            player.sendMessage(C.Blue + "Seeing ghosts!");
        }

        for (Player p : getPlayers(true)) {
            DisguiseUtilities.refreshTrackers(p);
            // Resend his own metadata
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(p, ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(Server.ENTITY_METADATA, p.getEntityId(),
                                WrappedDataWatcher.getEntityWatcher(p), true)
                        .createPacket(p.getEntityId(), WrappedDataWatcher.getEntityWatcher(p), true));
            }
            catch (Exception ex) {
                UtilError.handle(ex);
            }
        }
    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!event.getPlayer().isOnline())
                    return;

                ProtocolLibrary.getProtocolManager().updateEntity(event.getPlayer(), Arrays.asList(event.getPlayer()));
            }
        }.runTask(getManager().getPlugin());
    }

    @EventHandler
    public void onGameEnd(GameStateEvent event) {
        if (event.getState() != GameState.End)
            return;

        unregisterAbility();
    }

    @EventHandler
    public void onHatEvent(EquipmentEvent event) {
        if (!hasAbility(event.getWearer()))
            return;

        if (getGame().getTeam(event.getViewer()) == null || getGame().sameTeam(event.getWearer(), event.getViewer())) {
            if (event.isModified()) {
                ItemStack item = event.getItem();

                if (item.getItemMeta() instanceof LeatherArmorMeta) {
                    LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

                    Color color = meta.getColor();
                    meta.setColor(color.mixColors(color, Color.GRAY));

                    item.setItemMeta(meta);
                }
            }

            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onUpdate(TimeEvent event) {
        if (event.getType() != TimeType.TICK) {
            return;
        }

        if (!isLive())
            return;

        while (!_arrows.isEmpty() && UtilTime.elasped(_arrows.get(0).getValue(), 10000)) {
            Player player = Bukkit.getPlayerExact(_arrows.remove(0).getKey());

            if (player == null) {
                continue;
            }

            if (!isAlive(player))
                continue;

            UtilPlayer.setArrowDespawnTimer(player, 500);

            int arrows = UtilPlayer.getArrowsInBody(player) - 1;

            String message = "An arrow has disappeared.. ";

            if (arrows > 0) {
                message += "You have " + arrows + " arrow" + (arrows != 1 ? "s" : "") + " stuck in you!";
            } else {
                message += "No remaining arrows stuck in you!";
            }

            player.sendMessage(C.Aqua + C.Magic + "sd " + C.Aqua + message);

            UtilPlayer.setArrowsInBody(player, arrows);

            if (arrows <= 0) {
                Iterator<Pair<String, Long>> itel = _arrows.iterator();

                while (itel.hasNext()) {
                    if (itel.next().getKey().equals(player.getName())) {
                        itel.remove();
                    }
                }
            }
        }
    }

    @Override
    public void registerAbility() {
        ProtocolLibrary.getProtocolManager().addPacketListener(_packetlistener);

        for (Player player : getPlayers(true)) {
            DisguiseUtilities.refreshTrackers(player);
            // Resend his own metadata
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(),
                                WrappedDataWatcher.getEntityWatcher(player), true)
                        .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
            }
            catch (Exception ex) {
                UtilError.handle(ex);
            }
        }
    }

    @Override
    public void unregisterAbility() {
        ProtocolLibrary.getProtocolManager().removePacketListener(_packetlistener);

        for (Player player : getPlayers(true)) {
            DisguiseUtilities.refreshTrackers(player);
            // Resend his own metadata
            try {
                ProtocolLibrary.getProtocolManager().sendServerPacket(player, ProtocolLibrary.getProtocolManager()
                        .createPacketConstructor(Server.ENTITY_METADATA, player.getEntityId(),
                                WrappedDataWatcher.getEntityWatcher(player), true)
                        .createPacket(player.getEntityId(), WrappedDataWatcher.getEntityWatcher(player), true));
            }
            catch (Exception ex) {
                UtilError.handle(ex);
            }
        }
    }
}
