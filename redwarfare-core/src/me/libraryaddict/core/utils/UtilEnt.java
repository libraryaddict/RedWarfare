package me.libraryaddict.core.utils;

import com.google.common.base.Predicate;
import me.libraryaddict.core.Pair;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftArrow;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.entity.Entity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;
import java.util.List;

public class UtilEnt {
    public static double getAbsorptionHearts(Entity entity) {
        if (!(entity instanceof LivingEntity))
            return 0;

        return ((CraftLivingEntity) entity).getHandle().getAbsorptionHearts();
    }

    public static int getArmorRating(LivingEntity entity) {
        EntityLiving nms = ((CraftLivingEntity) entity).getHandle();
        int rating = 0;

        for (ItemStack itemstack : nms.getArmorItems()) {
            if (itemstack != null && itemstack.getItem() instanceof ItemArmor) {
                int l = ((ItemArmor) itemstack.getItem()).d;

                rating += l;
            }
        }

        return rating;
    }

    public static Arrow.PickupStatus getArrowPickupStatus(Entity entity) {
        if (!(entity instanceof Arrow))
            return null;

        return ((Arrow) entity).getPickupStatus();
    }

    public static Pair<Entity, Block> getHit(Entity movingEntity) {
        return getHit(movingEntity, null);
    }

    public static Pair<Entity, Block> getHit(Entity movingEntity, Predicate<Entity> predicate) {
        net.minecraft.server.v1_12_R1.Entity nms = ((CraftEntity) movingEntity).getHandle();

        Vec3D vec3d = new Vec3D(nms.locX, nms.locY, nms.locZ);
        Vec3D vec3d1 = new Vec3D(nms.locX + nms.motX, nms.locY + nms.motY, nms.locZ + nms.motZ);
        MovingObjectPosition movingobjectposition = nms.world.rayTrace(vec3d, vec3d1);

        vec3d = new Vec3D(nms.locX, nms.locY, nms.locZ);
        vec3d1 = new Vec3D(nms.locX + nms.motX, nms.locY + nms.motY, nms.locZ + nms.motZ);

        if (movingobjectposition != null) {
            vec3d1 = new Vec3D(movingobjectposition.pos.x, movingobjectposition.pos.y, movingobjectposition.pos.z);
        }

        net.minecraft.server.v1_12_R1.Entity entity = null;
        List list = nms.world.getEntities(nms, nms.getBoundingBox().a(nms.motX, nms.motY, nms.motZ).g(1.0D));

        double d5 = 0.0D;

        for (int j = 0; j < list.size(); j++) {
            net.minecraft.server.v1_12_R1.Entity entity1 = (net.minecraft.server.v1_12_R1.Entity) list.get(j);

            if (predicate != null ? predicate.apply(entity1.getBukkitEntity()) :
                    (entity1.isInteractable() || entity1 instanceof EntityItem)) {
                AxisAlignedBB axisalignedbb = entity1.getBoundingBox().g(0.30000001192092896D);

                MovingObjectPosition movingobjectposition1 = axisalignedbb.b(vec3d, vec3d1);

                if (movingobjectposition1 != null) {
                    double d6 = vec3d.distanceSquared(movingobjectposition1.pos);
                    if ((d6 < d5) || (d5 == 0.0D)) {
                        entity = entity1;
                        d5 = d6;
                    }
                }
            }
        }

        Entity hitEntity = entity != null ? entity.getBukkitEntity() : null;
        Block block = movingobjectposition != null && movingobjectposition.a() != null ? movingEntity.getWorld()
                .getBlockAt(movingobjectposition.a().getX(), movingobjectposition.a().getY(),
                        movingobjectposition.a().getZ()) : null;

        return Pair.of(hitEntity, block);
    }

    public static String getName(Entity entity) {
        if (entity == null)
            return "Unknown";

        if (entity instanceof Player)
            return ((Player) entity).getName();

        if (entity.getCustomName() != null)
            return entity.getCustomName();

        return entity.getType().getName();
    }

    public static int getNewEntityId() {
        return getNewEntityId(true);
    }

    public static int getNewEntityId(boolean increment) {
        try {
            Field field = net.minecraft.server.v1_12_R1.Entity.class.getDeclaredField("entityCount");
            field.setAccessible(true);

            int id = field.getInt(null);

            if (increment) {
                field.setInt(null, id + 1);
            }

            return id;
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        return 0;
    }

    public static PotionEffect getPotion(Entity entity, PotionEffectType potionType) {
        if (!(entity instanceof LivingEntity))
            return null;

        LivingEntity living = (LivingEntity) entity;

        for (PotionEffect effect : living.getActivePotionEffects()) {
            if (effect.getType().equals(potionType)) {
                return effect;
            }
        }

        return null;
    }

    public static double heal(Entity entity, double health) {
        if (!(entity instanceof Damageable)) {
            return 0;
        }

        Damageable ent = (Damageable) entity;

        double toHeal = ent.getMaxHealth() - ent.getHealth();

        toHeal = Math.min(toHeal, health);

        ent.setHealth(ent.getHealth() + toHeal);

        return toHeal;
    }

    public static boolean isGrounded(Entity entity) {
        if (entity.isOnGround())
            return true;

        /*for (Block block : UtilBlock.getBlocks(entity.getLocation().subtract(0.3, 0.2, 0.3),
                entity.getLocation().add(0.3, 0, 0.3)))
        {
        	if (!UtilBlock.nonSolid(block))
        		return true;
        }*/

        return false;
    }

    public static void setAbsorptionHearts(LivingEntity entity, double hearts) {
        ((CraftLivingEntity) entity).getHandle().setAbsorptionHearts((float) hearts);
    }

    public static void setArrowLived(Entity entity, int lived) {
        if (!(entity instanceof Arrow))
            return;

        try {
            EntityArrow arrow = ((CraftArrow) entity).getHandle();

            Field field = EntityArrow.class.getDeclaredField("ay");
            field.setAccessible(true);

            field.setInt(arrow, lived);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }
    }

    public static void setArrowPickupStatus(Entity entity, Arrow.PickupStatus newStatus) {
        if (!(entity instanceof Arrow))
            return;

        ((Arrow) entity).setPickupStatus(newStatus);
    }

    public static void velocity(Entity ent, Vector vec, boolean groundBoost) {
        if (isGrounded(ent) && groundBoost && vec.getY() <= 0.01) {
            vec.setY(0.2);
        }

        if (!Double.isFinite(vec.getX()) || !Double.isFinite(vec.getY()) || !Double.isFinite(vec.getZ())) {
            UtilError.handle(new Exception("Illegal double"));
            return;
        }

        if (ent instanceof LivingEntity) {
            // vec = reduceVelocity((LivingEntity) ent, vec.getX(), vec.getY(), vec.getZ());
        }

        ent.setVelocity(vec);
    }
}
