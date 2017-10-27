package me.libraryaddict.core.explosion;

import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.damage.DamageManager;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.event.CraftEventFactory;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

import java.util.*;

public class CustomExplosion extends Explosion {
    private AttackType _attackType;
    private float _blockExplosionSize;
    private boolean _createFire;
    private float _damage;
    private boolean _damageBlocks = true;
    private boolean _damageBlocksEqually;
    private boolean _dropItems = true;
    private boolean _ignoreNonLiving;
    private boolean _ignoreRate = true;
    private float _maxDamage = 1000;
    private org.bukkit.entity.LivingEntity _owner;
    private AttackType _selfAttackType;
    private float _size;
    private boolean _useCustomDamage;
    private World _world;
    private double posX, posY, posZ;

    public CustomExplosion(Location loc, float explosionSize, AttackType attackType) {
        super(((CraftWorld) loc.getWorld()).getHandle(), null, loc.getX(), loc.getY(), loc.getZ(), explosionSize, false,
                false);

        posX = loc.getX();
        posY = loc.getY();
        posZ = loc.getZ();

        _world = ((CraftWorld) loc.getWorld()).getHandle();
        _blockExplosionSize = _size = explosionSize;
        _attackType = attackType;
        _selfAttackType = attackType;
    }

    public CustomExplosion(Location loc, float explosionSize, AttackType attackType, AttackType selfAttackType) {
        this(loc, explosionSize, attackType);

        _selfAttackType = selfAttackType;
    }

    @Override
    public void a() {
        if (Math.max(_blockExplosionSize, _size) < 0.1F) {
            return;
        }

        HashSet hashset = new HashSet();

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if ((x == 0) || (x == 15) || (y == 0) || (y == 15) || (z == 0) || (z == 15)) {
                        double d0 = x / 15.0F * 2.0F - 1.0F;
                        double d1 = y / 15.0F * 2.0F - 1.0F;
                        double d2 = z / 15.0F * 2.0F - 1.0F;
                        double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                        d0 /= d3;
                        d1 /= d3;
                        d2 /= d3;
                        float f1 = this._blockExplosionSize * (0.7F + this._world.random.nextFloat() * 0.6F);
                        double d4 = this.posX;
                        double d5 = this.posY;
                        double d6 = this.posZ;

                        for (; f1 > 0.0F; f1 -= 0.225F) {
                            BlockPosition blockposition = new BlockPosition(d4, d5, d6);
                            IBlockData iblockdata = this._world.getType(blockposition);

                            if (iblockdata.getMaterial() != Material.AIR) // XXX
                            {
                                float f2 = this.source != null ?
                                        this.source.a(this, this._world, blockposition, iblockdata) :
                                        (_damageBlocksEqually ? Blocks.DIRT : iblockdata.getBlock()).a((Entity) null);

                                f1 -= (f2 + 0.3F) * 0.3F;
                            }

                            if ((f1 > 0.0F) && ((this.source == null) || (this.source
                                    .a(this, this._world, blockposition, iblockdata, f1))) && (blockposition
                                    .getY() < 256) && (blockposition.getY() >= 0)) {
                                hashset.add(blockposition);
                            }

                            d4 += d0 * 0.300000011920929D;
                            d5 += d1 * 0.300000011920929D;
                            d6 += d2 * 0.300000011920929D;
                        }
                    }
                }
            }
        }

        this.getBlocks().addAll(hashset);

        float f3 = _size * 2F;

        int i = MathHelper.floor(this.posX - f3 - 1.0D);
        int j = MathHelper.floor(this.posX + f3 + 1.0D);
        int k = MathHelper.floor(this.posY - f3 - 1.0D);
        int k1 = MathHelper.floor(this.posY + f3 + 1.0D);
        int l1 = MathHelper.floor(this.posZ - f3 - 1.0D);
        int i2 = MathHelper.floor(this.posZ + f3 + 1.0D);
        List list = this._world.getEntities(this.source, new AxisAlignedBB(i, k, l1, j, k1, i2));
        Vec3D vec3d = new Vec3D(this.posX, this.posY, this.posZ);

        for (int j2 = 0; j2 < list.size(); j2++) {
            Entity entity = (Entity) list.get(j2);

            /*{
                double d7 = entity.f(this.posX, this.posY, this.posZ) / this._size;

            	double d9 = this._world.a(vec3d, entity.getBoundingBox());
            	double d10 = (1.0D - d7) * d9;
            	double tdamage = (int) ((d10 * d10 + d10) / 2.0D * 8.0D * this._size + 1.0D);
            	double tdamage2 = Math.min(tdamage, _maxDamage);
            	Hologram holo = new Hologram(entity.getBukkitEntity().getLocation(),
            			"D: " + tdamage + " R: " + tdamage2).start();
            	new BukkitRunnable()
            	{
            		public void run()
            		{
            			holo.stop();
            		}
            	}.runTaskLater(Bukkit.getPluginManager().getPlugins()[0], 20 * 30);
            }*/

            if (!(entity.getBukkitEntity() instanceof LivingEntity) && _ignoreNonLiving)
                continue;

            double d7 = entity.e(this.posX, this.posY, this.posZ) / this._size; // XXX

            if (d7 <= 1.0D) {
                double d0 = entity.locX - this.posX;
                double d1 = entity.locY + entity.getHeadHeight() - this.posY;
                double d2 = entity.locZ - this.posZ;
                double d8 = MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

                if (d8 != 0.0D) {
                    d0 /= d8;
                    d1 /= d8;
                    d2 /= d8;

                    // Performs a raytrace that determines the percentage of solid blocks between the two
                    double d9 = this._world.a(vec3d, entity.getBoundingBox()); // XXX
                    double d10 = (1.0D - d7) * d9;
                    float damage;

                    if (_useCustomDamage) {
                        damage = Math.max(0, (int) ((_damage * d9) * (d8 / _size)));
                    } else {
                        damage = (int) ((d10 * d10 + d10) / 2.0D * 8.0D * this._size + 1.0D);
                        damage = Math.min(damage, _maxDamage);
                    }

                    if (entity.getBukkitEntity() instanceof Damageable) {

                        DamageManager manager = ExplosionManager.explosionManager.getDamageManager();

                        CustomDamageEvent event = manager.createEvent(entity.getBukkitEntity(),
                                entity.getBukkitEntity() == _owner ? _selfAttackType : _attackType, damage, _owner);

                        event.setIgnoreRate(_ignoreRate);

                        Vector vec = new Vector(d0 * d10, d1 * d10, d2 * d10);

                        event.setKnockback(vec);
                        manager.callDamage(event);
                    } else {
                        CraftEventFactory.entityDamage = this.source;
                        entity.damageEntity(DamageSource.explosion(this), damage);
                        CraftEventFactory.entityDamage = null;
                    }

                    if (((entity instanceof EntityHuman)) && (!((EntityHuman) entity).abilities.isInvulnerable)) {
                        this.b().put((EntityHuman) entity, new Vec3D(d0 * d10, d1 * d10, d2 * d10)); // XXX
                    }
                }
            }
        }
    }

    @Override
    public void a(boolean flag) {
        Location loc = new Location(_world.getWorld(), posX, posY, posZ);

        _world.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 4F,
                (1.0F + (this._world.random.nextFloat() - this._world.random.nextFloat()) * 0.2F) * 0.7F);

        if ((this._blockExplosionSize >= 2.0F) && (this._damageBlocks)) {
            _world.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 50);
        } else {
            _world.getWorld().playEffect(loc, Effect.EXPLOSION_LARGE, 50);
        }

        if (_damageBlocks) {
            org.bukkit.World bworld = this._world.getWorld();

            ArrayList blockList = new ArrayList();

            for (int i1 = this.getBlocks().size() - 1; i1 >= 0; i1--) {
                BlockPosition cpos = this.getBlocks().get(i1);

                org.bukkit.block.Block bblock = bworld.getBlockAt(cpos.getX(), cpos.getY(), cpos.getZ());

                if (bblock.getType() != org.bukkit.Material.AIR) {
                    blockList.add(bblock);
                }
            }

            ExplosionEvent event = new ExplosionEvent(this, blockList);

            this._world.getServer().getPluginManager().callEvent(event);

            this.getBlocks().clear();

            for (org.bukkit.block.Block bblock : event.getBlocks()) {
                BlockPosition coords = new BlockPosition(bblock.getX(), bblock.getY(), bblock.getZ());
                this.getBlocks().add(coords);
            }

            if (getBlocks().isEmpty()) {
                this.wasCanceled = true;
                return;
            }

            /*if (_fallingBlockExplosion)
            {
            	Collection<org.bukkit.block.Block> blocks = event.getBlocks();

            	if (blocks.size() > _maxFallingBlocks)
            	{
            		blocks = new ArrayList<org.bukkit.block.Block>(blocks);

            		Collections.shuffle((ArrayList) blocks);

            		int toRemove = blocks.size() - _maxFallingBlocks;

            		for (int i = 0; i < toRemove; i++)
            		{
            			blocks.remove(0);
            		}
            	}

            	_explosion.BlockExplosion(blocks, new Location(_world.getWorld(), posX, posY, posZ), false, false);
            }*/

            Iterator iterator = this.getBlocks().iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) iterator.next();
                IBlockData block = this._world.getType(blockposition);

                if (flag) {
                    double d0 = blockposition.getX() + this._world.random.nextFloat();
                    double d1 = blockposition.getY() + this._world.random.nextFloat();
                    double d2 = blockposition.getZ() + this._world.random.nextFloat();
                    double d3 = d0 - this.posX;
                    double d4 = d1 - this.posY;
                    double d5 = d2 - this.posZ;
                    double d6 = MathHelper.sqrt(d3 * d3 + d4 * d4 + d5 * d5);

                    d3 /= d6;
                    d4 /= d6;
                    d5 /= d6;
                    double d7 = 0.5D / (d6 / this._blockExplosionSize + 0.1D);

                    d7 *= (this._world.random.nextFloat() * this._world.random.nextFloat() + 0.3F);
                    d3 *= d7;
                    d4 *= d7;
                    d5 *= d7;
                    this._world.addParticle(EnumParticle.EXPLOSION_NORMAL, (d0 + this.posX * 1.0D) / 2.0D,
                            (d1 + this.posY * 1.0D) / 2.0D, (d2 + this.posZ * 1.0D) / 2.0D, d3, d4, d5, new int[0]);
                    this._world.addParticle(EnumParticle.SMOKE_NORMAL, d0, d1, d2, d3, d4, d5, new int[0]);
                }

                if (block.getMaterial() != Material.AIR) {
                    if (block.getBlock().a(this) && _dropItems) {
                        block.getBlock().dropNaturally(this._world, blockposition, this._world.getType(blockposition),
                                _blockExplosionSize, 0);
                    }

                    this._world.setTypeAndData(blockposition, Blocks.AIR.getBlockData(), 3);
                    block.getBlock().wasExploded(this._world, blockposition, this);
                }
            }
        }

        if (this._createFire) {
            Iterator iterator = this.getBlocks().iterator();

            while (iterator.hasNext()) {
                BlockPosition blockposition = (BlockPosition) iterator.next();
                if ((this._world.getType(blockposition).getMaterial() == Material.AIR) && (this._world
                        .getType(blockposition.down()).b()) && (new Random().nextBoolean())) {
                    if (!CraftEventFactory.callBlockIgniteEvent(this._world, blockposition.getX(), blockposition.getY(),
                            blockposition.getZ(), this).isCancelled())
                        this._world.setTypeUpdate(blockposition, Blocks.FIRE.getBlockData());
                }
            }
        }
    }

    public CustomExplosion explode() {
        /*new BukkitRunnable()
        {
        	long started = System.currentTimeMillis();

        	public void run()
        	{
        		if (UtilTime.elasped(started, 20000))
        		{
        			this.cancel();
        		}

        		UtilParticle.playParticle(ParticleType.FLAME, new Location(_world.getWorld(), posX, posY, posZ));
        	}
        }.runTaskTimer(Bukkit.getPluginManager().getPlugins()[0], 0, 5);*/
        // Explode
        a();
        a(true);

        return this;
    }

    public float getSize() {
        return _size;
    }

    public void setAttackType(AttackType attackType) {
        _attackType = attackType;
    }

    public CustomExplosion setBlockExplosionSize(float explosionSize) {
        _blockExplosionSize = explosionSize;

        return this;
    }

    public CustomExplosion setBlocksDamagedEqually(boolean damageEqually) {
        _damageBlocksEqually = damageEqually;

        return this;
    }

    public CustomExplosion setDamageBlocks(boolean damageBlocks) {
        _damageBlocks = damageBlocks;

        return this;
    }

    public CustomExplosion setDamager(org.bukkit.entity.Player player) {
        _owner = player;

        return this;
    }

    public CustomExplosion setDropItems(boolean dropItems) {
        _dropItems = dropItems;

        return this;
    }

    /**
     * Center of explosion does this much damage
     */
    public CustomExplosion setExplosionDamage(float damage) {
        _damage = damage;
        _useCustomDamage = true;

        return this;
    }

    public CustomExplosion setIgnoreNonLiving(boolean ignoreNonLiving) {
        _ignoreNonLiving = ignoreNonLiving;

        return this;
    }

    public CustomExplosion setIgnoreRate(boolean ignoreRate) {
        _ignoreRate = ignoreRate;

        return this;
    }

    public void setIncinderary(boolean fire) {
        _createFire = fire;
    }

    public CustomExplosion setMaxDamage(float maxDamage) {
        _maxDamage = maxDamage;

        return this;
    }
}
