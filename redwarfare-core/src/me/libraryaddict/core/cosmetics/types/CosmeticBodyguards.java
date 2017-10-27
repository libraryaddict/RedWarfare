package me.libraryaddict.core.cosmetics.types;

import me.libraryaddict.core.Pair;
import me.libraryaddict.core.cosmetics.Cosmetic;
import me.libraryaddict.core.fakeentity.FakeEntity;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import net.minecraft.server.v1_12_R1.Vector3f;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class CosmeticBodyguards extends Cosmetic
{
    private HashMap<Player, ArrayList<FakeEntity>> _bodyguards = new HashMap<Player, ArrayList<FakeEntity>>();
    private int _direction = 10;
    private HashMap<Player, Location> _last = new HashMap<Player, Location>();
    private HashMap<FakeEntity, Location> _moveTo = new HashMap<FakeEntity, Location>();
    private int _tick;
    private HashMap<FakeEntity, Long> _walking = new HashMap<FakeEntity, Long>();

    public CosmeticBodyguards()
    {
        super("Bodyguards");
    }

    @Override
    public String[] getDescription()
    {
        return new String[]
            {
                    "Hire your own detail protection!"
            };
    }

    @Override
    public ItemStack getIcon()
    {
        return new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.BLACK).build();
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        _tick += _direction;

        if (Math.abs(_tick) > 50)
            _direction = -_direction;

        ArrayList<Player> enabled = getPlayers();
        Iterator<Entry<Player, ArrayList<FakeEntity>>> itel = _bodyguards.entrySet().iterator();

        while (itel.hasNext())
        {
            Entry<Player, ArrayList<FakeEntity>> entry = itel.next();

            if (enabled.contains(entry.getKey()))
                continue;

            itel.remove();

            entry.getValue().stream().forEach((entity) -> {

                UtilParticle.playParticle(ParticleType.CLOUD, entity.getLocation(), 0.3, 0.8, 0.3, 10);

                entity.stop();

                _moveTo.remove(entity);
                _walking.remove(entity);
            });

            _last.remove(entry.getKey());
        }

        for (Player player : enabled)
        {
            if (_bodyguards.containsKey(player))
                continue;

            Location pLoc = player.getLocation();

            pLoc.setY(pLoc.getBlockY());

            while (pLoc.getBlockY() > 0 && UtilBlock.nonSolid(pLoc.getBlock().getRelative(BlockFace.DOWN)))
            {
                pLoc.add(0, -1, 0);
            }

            _last.put(player, pLoc);

            int totalYaw = (int) ((pLoc.getYaw() + 720 + 45) % 360);
            int yaw = totalYaw % 90;
            int off = Math.floorDiv(totalYaw, 90);

            ArrayList<Location> positions = UtilShapes.getPointsInCircle(pLoc, 1.7, 4, yaw);

            ArrayList<FakeEntity> fakeEntities = new ArrayList<FakeEntity>();

            for (int i = 0; i < 4; i++)
            {
                FakeEntity guard = new FakeEntity(positions.get((off + i) % positions.size()), EntityType.ARMOR_STAND);

                guard.setArmor(new ItemStack[]
                    {
                            new ItemBuilder(Material.LEATHER_BOOTS).setColor(Color.BLACK).build(),
                            new ItemBuilder(Material.LEATHER_LEGGINGS).setColor(Color.BLACK).build(),
                            new ItemBuilder(Material.LEATHER_CHESTPLATE).setColor(Color.BLACK).build(),
                            new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.BLACK).build()
                    });

                guard.setItem(EquipmentSlot.OFF_HAND, new ItemStack(Material.BOW));
                guard.setItem(EquipmentSlot.HAND, new ItemStack(Material.IRON_SWORD));

                guard.setMetadata(MetaIndex.ARMORSTAND_META, (byte) 28);

                UtilParticle.playParticle(ParticleType.CLOUD, guard.getLocation(), 0.3, 0.8, 0.3, 10);

                guard.start();

                fakeEntities.add(guard);
            }

            _bodyguards.put(player, fakeEntities);
        }

        HashMap<FakeEntity, Long> walking = new HashMap<FakeEntity, Long>();

        for (Entry<Player, ArrayList<FakeEntity>> entry : _bodyguards.entrySet())
        {
            Player player = entry.getKey();

            Location pLoc = player.getLocation();

            pLoc.setY(pLoc.getBlockY());

            while (pLoc.getBlockY() > 0 && UtilBlock.nonSolid(pLoc.getBlock().getRelative(BlockFace.DOWN)))
            {
                pLoc.add(0, -1, 0);
            }

            if (_last.containsKey(player) && UtilLoc.getDistance(pLoc, _last.get(player)) < 0.1)
                continue;

            _last.put(player, pLoc);

            int totalYaw = (int) ((pLoc.getYaw() + 720 + 45) % 360);

            ArrayList<Location> positions = UtilShapes.getPointsInCircle(pLoc, 1.7, entry.getValue().size(), totalYaw);

            int best = -1;
            double dist = 0;

            for (int a = 0; a < positions.size(); a++)
            {
                double d = 0;

                for (int i = 0; i < entry.getValue().size(); i++)
                {
                    FakeEntity entity = entry.getValue().get(i);
                    Location pos = positions.get((i + a) % positions.size());

                    d += UtilLoc.getDistance(entity.getLocation(), pos);
                }

                if (best == -1 || dist > d)
                {
                    best = a;
                    dist = d;
                }
            }

            for (int i = 0; i < entry.getValue().size(); i++)
            {
                FakeEntity entity = entry.getValue().get(i);
                Location eLoc = entity.getLocation();

                Location pos = positions.get((best + i) % positions.size());

                Location toMove = eLoc.clone();

                boolean hasSight = UtilLoc.hasSight(eLoc, pos);
                boolean hasSight2 = UtilLoc.hasSight(eLoc.clone().add(0, 1, 0), pos);

                if (hasSight || hasSight2 || (!UtilLoc.hasSight(eLoc, pLoc) && UtilLoc.hasSight(pos, pLoc)))
                {
                    toMove = pos;
                }

                if (UtilLoc.getDistance(eLoc, toMove) < 0.7
                        && (UtilLoc.getDistance(eLoc, toMove) < 0.3 || !Recharge.canUse(player, "BG Move " + i)))
                {
                    if (Recharge.getTimeLeft(player, "BG Move " + i) < 3000)
                    {
                        Player lookAt = UtilLoc.getClosest(eLoc, UtilLoc.getInRadius(eLoc, 14, Player.class), 2, player);

                        if (lookAt != null)
                        {
                            eLoc.setDirection(UtilLoc.getDirection(eLoc, lookAt.getLocation()));

                            if (entity.getLocation().getDirection().subtract(eLoc.getDirection()).length() < 0.1)
                                continue;

                            entity.setLocation(eLoc);
                        }
                    }

                    continue;
                }

                Recharge.use(player, "BG Move " + i, 6000);

                boolean tele = !UtilLoc.hasSight(eLoc, pLoc) || Math.abs(toMove.getY() - eLoc.getY()) > 1.1
                        || UtilLoc.getDistance(pLoc, eLoc) > 4;

                if (tele)
                {
                    if (toMove.getBlockY() > 0 && !UtilBlock.nonSolid(toMove.getBlock().getRelative(BlockFace.DOWN)))
                    {
                        entity.setLocation(toMove);
                    }
                }
                else if (hasSight && hasSight2 && UtilLoc.getDistance(toMove, eLoc) > 0.1)
                {
                    _moveTo.put(entity, toMove);
                }
            }
        }

        Iterator<Entry<FakeEntity, Location>> eItel = _moveTo.entrySet().iterator();

        while (eItel.hasNext())
        {
            Entry<FakeEntity, Location> entry = eItel.next();

            FakeEntity entity = entry.getKey();
            Location toMove = entry.getValue();
            Location eLoc = entity.getLocation();

            double dist = Math.min(UtilLoc.getDistance(eLoc, toMove), 0.5);

            if (dist < 0.5)
            {
                eItel.remove();
            }

            Vector dir = UtilLoc.getDirection(entity.getLocation(), toMove).multiply(dist);

            Location loc = eLoc.add(dir);
            loc.setDirection(dir);

            while (!UtilBlock.nonSolid(loc.getBlock()))
            {
                loc.add(0, 1, 0);
            }

            while (loc.getBlockY() > 0 && UtilBlock.nonSolid(loc.getBlock().getRelative(BlockFace.DOWN)))
            {
                loc.add(0, -1, 0);
            }

            if (loc.getBlockY() > 0)
            {
                entity.setLocation(loc);
                walking.put(entity, System.currentTimeMillis());
            }
        }

        _walking.putAll(walking);

        Iterator<Entry<FakeEntity, Long>> itel2 = _walking.entrySet().iterator();

        while (itel2.hasNext())
        {
            Entry<FakeEntity, Long> entry = itel2.next();

            if (UtilTime.elasped(entry.getValue(), 150))
            {
                itel2.remove();
                entry.getKey().setMetadata(Pair.of(MetaIndex.ARMORSTAND_LEFT_LEG, new Vector3f(0, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_RIGHT_LEG, new Vector3f(0, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_RIGHT_ARM, new Vector3f(0, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_LEFT_ARM, new Vector3f(0, 0, 0)));
            }
            else
            {
                entry.getKey().setMetadata(Pair.of(MetaIndex.ARMORSTAND_LEFT_LEG, new Vector3f(-_tick, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_RIGHT_LEG, new Vector3f(_tick, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_RIGHT_ARM, new Vector3f(-_tick / 2F, 0, 0)),
                        Pair.of(MetaIndex.ARMORSTAND_LEFT_ARM, new Vector3f(_tick / 2F, 0, 0)));
            }
        }
    }

}
