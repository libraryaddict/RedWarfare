package me.libraryaddict.arcade.managers;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameOptionEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameOption;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.misc.PlayerSpecInventory;
import me.libraryaddict.core.C;
import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.damage.CustomDamageEvent;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.*;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftFish;
import org.bukkit.entity.*;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.Iterator;
import java.util.Objects;

public class EventManager extends MiniPlugin
{
    private ArcadeManager _arcadeManager;

    public EventManager(JavaPlugin plugin, ArcadeManager arcadeManager)
    {
        super(plugin, "Event Manager");

        _arcadeManager = arcadeManager;
    }

    @EventHandler
    public void breakDoor(EntityBreakDoorEvent event)
    {
        event.setCancelled(true);
    }

    public Game getGame()
    {
        return getManager().getGame();
    }

    public ArcadeManager getManager()
    {
        return _arcadeManager;
    }

    public <Y> Y getOption(GameOption<Y> option)
    {
        return getGame().getOption(option);
    }

    public boolean isAlive(Entity entity)
    {
        return getGame().isAlive(entity);
    }

    public boolean isLive()
    {
        return getGame().isLive();
    }

    @EventHandler
    public void onAnvilUse(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getClickedBlock().getType() != Material.ANVIL)
            return;

        if (getOption(GameOption.ALLOW_ANVIL))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onArmorStandInteract(PlayerArmorStandManipulateEvent event)
    {
        if (getOption(GameOption.INTERACT_DECORATIONS))
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (!isLive())
        {
            event.setCancelled(true);
            return;
        }

        if (!isAlive(event.getPlayer()))
        {
            event.setCancelled(true);
            return;
        }

        // If you're allowed to break the instant-breakables
        if (!getOption(GameOption.BREAK_BLOCK) && getOption(GameOption.BREAK_GRASS))
        {
            switch (event.getBlock().getType())
            {
            case MELON_BLOCK:
                event.setCancelled(true);

                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.MELON, 1));
                event.getBlock().setType(Material.AIR);
            case LONG_GRASS:
            case CROPS:
            case LEAVES:
            case LEAVES_2:
            case BROWN_MUSHROOM:
            case RED_MUSHROOM:
            case DEAD_BUSH:
            case CARROT:
            case POTATO:
            case DOUBLE_PLANT:
            case FIRE:
            case RED_ROSE:
            case YELLOW_FLOWER:
            case WHEAT:
                return;
            default:
                event.setCancelled(true);
                break;
            }
        }
        else if (!getOption(GameOption.BREAK_BLOCK))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event)
    {
        if (!isLive())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event)
    {
        if (!isLive())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(event.getPlayer()))
        {
            event.setCancelled(true);
        }
        else
        {
            Material mat = event.getBlock().getType();

            for (Material m : getOption(GameOption.PLACABLE_BLOCKS))
            {
                if (m == mat)
                    return;
            }

            if (!getOption(GameOption.PLACE_BLOCK) && !UtilInv.isItem(event.getItemInHand(), Material.FLINT_AND_STEEL))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBowDrag(InventoryDragEvent event)
    {
        if (event.getView().getTopInventory().getHolder() != event.getWhoClicked())
            return;

        if (!event.getRawSlots().contains(45))
            return;

        if (!UtilInv.isItem(event.getCursor(), Material.BOW))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event)
    {
        if (getOption(GameOption.BLOCK_BURN))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onChat(ChatEvent event)
    {
        if (!getOption(GameOption.COLOR_CHAT_NAMES))
            return;

        GameTeam team = getGame().getTeam(event.getPlayer());

        if (team == null)
            return;

        event.setDisplayName(team.getColoring() + event.getDisplayName());
    }

    // @EventHandler
    public void onConsume(PlayerItemConsumeEvent event)
    {
        if (isLive() && isAlive(event.getPlayer()) && getOption(GameOption.HUNGER))
        {
            return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onCropsBreak(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.PHYSICAL)
            return;

        if (event.getClickedBlock() == null)
            return;

        if (event.getClickedBlock().getType() != Material.SOIL)
            return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(CustomDamageEvent event)
    {
        Entity damagee = event.getDamagee();
        Entity damager = event.getFinalDamager();

        if ((damagee instanceof ArmorStand || damagee instanceof ItemFrame) && !getOption(GameOption.INTERACT_DECORATIONS))
        {
            event.setCancelled(true);
        }
        else if (!(damagee instanceof LivingEntity) && !getOption(GameOption.DAMAGE_NON_LIVING))
        {
            event.setCancelled(true);
        }
        else if (!isLive())
        {
            event.setCancelled(true);
            damagee.setFireTicks(0);
        }
        else if (!isAlive(damagee))
        {
            event.setCancelled(true);
            damagee.setFireTicks(0);
        }
        else if (damager != null)
        {
            if (!isAlive(damager) && event.getAttackType().isMelee())
            {
                event.setCancelled(true);
            }

            GameTeam team1 = getGame().getTeam(damagee);
            GameTeam team2 = getGame().getTeam(damager);

            boolean sameTeam = Objects.equals(team1, team2);

            if (team1 != null)
            {
                if (sameTeam && !getOption(GameOption.ATTACK_TEAM))
                {
                    System.out.println("Cancelled as you cannot attack your own team");
                    event.setCancelled(true);
                }
                else if (!sameTeam && !getOption(GameOption.ATTACK_NON_TEAM))
                {
                    System.out.println("Cancelled as you cannot attack non-team");
                    event.setCancelled(true);
                }
            }

            if (team2 != null && team2.getOwner(damager) != null)
            {
                Player owner = Bukkit.getPlayer(team2.getOwner(damager));

                if (owner != null)
                {
                    event.setRealDamager(owner);
                }
            }
        }

        if (event.getAttackType() == AttackType.VOID && event.isPlayerDamagee())
        {
            if (getGame().isLive() && isAlive(damagee))
            {
                return;
            }

            new BukkitRunnable()
            {
                public void run()
                {
                    Location loc;

                    if (getGame().getState() == GameState.PreMap)
                    {
                        loc = getManager().getWorld().getRandomHubSpawn();
                    }
                    else
                    {
                        loc = getGame().getRandomSpectatorSpawn();
                    }

                    UtilPlayer.tele(damagee, loc);
                }
            }.runTaskLater(getPlugin(), 1);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDeath(DeathEvent event)
    {
        if (!getGame().getOption(GameOption.KILLS_IN_TAB) || event.getLastAttacker() == event.getPlayer()
                || !(event.getLastAttacker() instanceof Player))
            return;

        Player player = (Player) event.getLastAttacker();

        FakeScoreboard board = getManager().getScoreboard().getMainScoreboard();

        if (board == null)
            return;

        board.makeScore(DisplaySlot.PLAYER_LIST, player.getName(), board.getScore(DisplaySlot.PLAYER_LIST, player.getName()) + 1);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event)
    {
        if (!getGame().getOption(GameOption.TEAM_HOTBAR))
            return;

        ItemStack item = event.getItemDrop().getItemStack();

        if (item == null || item.getType() != Material.LEATHER_CHESTPLATE)
            return;

        if (event.getPlayer().getInventory().getHeldItemSlot() != 8)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event)
    {
        if (getGame().getState().isPreGame())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(event.getPlayer()))
        {
            event.setCancelled(true);
        }
        else if (!getOption(GameOption.PLAYER_DROP_ITEM))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEat(PlayerInteractEvent event)
    {
        if (!event.getAction().name().contains("RIGHT_"))
            return;

        if (!isLive())
            return;

        if (getOption(GameOption.STEAK_HEALTH) <= 0)
            return;

        if (event.useItemInHand() == Result.DENY)
            return;

        Player player = event.getPlayer();

        if (player.getHealth() + 1 > player.getMaxHealth())
            return;

        if (!UtilInv.isItem(event.getItem(), Material.COOKED_BEEF))
            return;

        UtilEnt.heal(player, getOption(GameOption.STEAK_HEALTH));
        UtilInv.remove(player, Material.COOKED_BEEF, 1);

        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1.4F, 0);

        Stats.add(player, "Game." + getGame().getName() + ".Steaks");

        event.setCancelled(true);
    }

    @EventHandler
    public void onEnderTeleport(PlayerTeleportEvent event)
    {
        if (event.getCause() != TeleportCause.ENDER_PEARL)
            return;

        if (isLive() && isAlive(event.getPlayer()))
        {
            if (getGame().isInsideBorder(event.getTo()))
            {
                return;
            }

            event.getPlayer().sendMessage(C.Blue + "One of your enderpearls went outside the border");
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event)
    {
        if (event.getEntityType() != EntityType.ARROW)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (!Recharge.canUse(player, "Spec Info"))
            return;

        Recharge.use(player, "Spec Info", 500);

        if (!(event.getRightClicked() instanceof LivingEntity))
            return;

        if (isAlive(player))
            return;

        if (getGame().isPreGame())
            return;

        GameTeam team1 = getGame().getTeam(event.getRightClicked());

        if (team1 == null)
            return;

        GameTeam team = getGame().getTeam(player);

        if (team != null && team != team1)
        {
            // player.sendMessage(C.Red + "You are not on the same team!");
            // return;
        }

        if (!getManager().getRank().getRank(player).hasRank(Rank.VIP))
        {
            player.sendMessage(C.Red + "You need VIP to open their inventory!");
            return;
        }

        LivingEntity right = (LivingEntity) event.getRightClicked();

        player.sendMessage(C.Gold + "Name: " + C.Yellow + UtilEnt.getName(right));
        player.sendMessage(C.Gold + "Health: " + C.Red + (int) Math.ceil(right.getHealth()) + "/" + (int) right.getMaxHealth()
                + C.DRed + " ❤");

        if (getOption(GameOption.HUNGER) && right instanceof Player)
        {
            player.sendMessage(
                    C.Gold + "Hunger: " + C.Yellow + ((Player) right).getFoodLevel() + "/20 " + C.Gold + C.Italic + "❦");
        }

        player.sendMessage(C.Gold + "Armor: " + C.Aqua + UtilEnt.getArmorRating(right));

        if (!player.isSneaking() && right instanceof Player)
        {
            new PlayerSpecInventory(player, (Player) right).openInventory();
        }
    }

    @EventHandler
    public void onExpChange(PlayerExpChangeEvent event)
    {
        if (isLive() || isAlive(event.getPlayer()))
            return;

        event.setAmount(0);
    }

    @EventHandler
    public void onExplosion(EntityExplodeEvent event)
    {
        event.blockList().clear();
    }

    @EventHandler
    public void onFade(BlockFadeEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFishingRod(ProjectileHitEvent event)
    {
        if (event.getEntityType() != EntityType.FISHING_HOOK)
            return;

        net.minecraft.server.v1_12_R1.EntityFishingHook nms = ((CraftFish) event.getEntity()).getHandle();

        new BukkitRunnable()
        {
            public void run()
            {
                if (nms.hooked == null || isAlive(nms.hooked.getBukkitEntity()))
                {
                    return;
                }

                event.getEntity().remove();
            }
        }.runTask(getPlugin());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onFlintAndSteel(BlockPlaceEvent event)
    {
        if (event.isCancelled())
            return;

        ItemStack item = event.getItemInHand();

        if (!UtilInv.isItem(item, Material.FLINT_AND_STEEL))
            return;

        if (item.hasItemMeta() && item.getItemMeta().spigot().isUnbreakable())
            return;

        new BukkitRunnable()
        {
            public void run()
            {
                item.setDurability((short) (item.getDurability() + 7));

                if (item.getDurability() <= 63)
                {
                    return;
                }

                UtilInv.remove(event.getPlayer(), item);
            }
        }.runTask(getPlugin());
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event)
    {
        if (!isLive())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(event.getEntity()))
        {
            event.setCancelled(true);
        }
        else if (!getOption(GameOption.HUNGER))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onGameOption(GameOptionEvent event)
    {
        if (event.getOption() != GameOption.TIME_OF_WORLD)
            return;

        if (getGame().getState() == GameState.PreMap)
            return;

        World world = getManager().getWorld().getGameWorld();

        world.setFullTime(getOption(GameOption.TIME_OF_WORLD));
    }

    @EventHandler
    public void onGamePremap(GameStateEvent event)
    {
        if (getGame().getState() != GameState.MapLoaded)
            return;

        World world = getManager().getWorld().getGameWorld();

        world.setFullTime(getOption(GameOption.TIME_OF_WORLD));
    }

    @EventHandler
    public void onGameState(GameStateEvent event)
    {
        if (event.getState() == GameState.Dead)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                Stats.timeEnd(player, "Game." + getGame().getName() + ".Time");
            }
        }
        else if (event.getState() == GameState.PreMap)
        {
            for (Player player : Bukkit.getOnlinePlayers())
            {
                Stats.timeStart(player, "Game." + getGame().getName() + ".Time");
            }
        }
    }

    @EventHandler
    public void onGrow(StructureGrowEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakEvent event)
    {
        if (getOption(GameOption.DAMAGE_NON_LIVING))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event)
    {
        if (getOption(GameOption.DAMAGE_NON_LIVING))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onIgnite(BlockIgniteEvent event)
    {
        if (event.getPlayer() != null || getOption(GameOption.BLOCK_IGNITE))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (getOption(GameOption.OPEN_CHEST) || getOption(GameOption.CHEST_LOOT))
            return;

        Material mat = event.getClickedBlock().getType();

        switch (mat)
        {
        case ENDER_CHEST:
        case ENCHANTMENT_TABLE:
        case ANVIL:
        case COMMAND:
        case DIODE_BLOCK_OFF:
        case DIODE_BLOCK_ON:
        case DIODE:
        case BED_BLOCK:
        case WORKBENCH:
        case CAKE_BLOCK:
            event.setCancelled(true);
            break;
        default:
            if (event.getClickedBlock().getState() instanceof InventoryHolder)
            {
                event.setCancelled(true);
            }

            break;
        }
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent event)
    {
        Player player = event.getPlayer();

        if (event.getRightClicked() instanceof ItemFrame && !getOption(GameOption.INTERACT_DECORATIONS))
        {
            // ItemStack item = ((ItemFrame) event.getRightClicked()).getItem();

            // if (item == null || item.getType() == Material.AIR)
            // {
            event.setCancelled(true);
            // }
        }

        if (!getGame().isPreGame() && getGame().isAlive(player))
            return;

        event.setCancelled(true);

        if (!(event.getRightClicked() instanceof LivingEntity) || !getOption(GameOption.SPEC_CLICK_INFO))
        {
            return;
        }

        LivingEntity entity = (LivingEntity) event.getRightClicked();

        if (!getGame().isAlive(entity))
            return;

        player.sendMessage(C.Gold + "Name: " + C.Yellow + UtilEnt.getName(entity));
        player.sendMessage(C.Gold + "Health: " + C.Red + (int) Math.ceil(entity.getHealth()) + "/"
                + (int) Math.ceil(entity.getMaxHealth()) + C.DRed + " ❤");

        if (entity instanceof Player && getOption(GameOption.HUNGER))
            player.sendMessage(C.Gold + "Hunger: " + C.Yellow + ((Player) entity).getFoodLevel() + "/20 " + C.Gold + "❦");

        int rating = UtilEnt.getArmorRating(entity);

        if (rating > 0)
        {
            rating = Math.min(100, rating * 5);
        }

        player.sendMessage(C.Gold + "Armor: " + C.Aqua + rating + "%");
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event)
    {
        ItemStack item = event.getEntity().getItemStack();

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().contains("DeadBodyItem"))
        {
            return;
        }

        if (getGame() == null || getGame().isPreGame() || getOption(GameOption.ITEMS_SPAWN))
        {
            event.setCancelled(true);
            return;
        }

        if (getOption(GameOption.REMOVE_SEEDS_DROP))
        {
            Material mat = event.getEntity().getItemStack().getType();

            switch (mat)
            {
            case SEEDS:
            case SAPLING:
            case VINE:
            case LEAVES:
            case LONG_GRASS:
            case RED_ROSE:
            case YELLOW_FLOWER:
            case DEAD_BUSH:
            case WATER_LILY:
                event.setCancelled(true);
                break;
            case ENCHANTMENT_TABLE:
            case RED_MUSHROOM:
            case BROWN_MUSHROOM:
                break;
            default:
                if (mat.isBlock())
                {
                    event.setCancelled(true);
                }
                break;
            }
        }
    }

    @EventHandler
    public void onMobsSpawn(CreatureSpawnEvent event)
    {
        switch (event.getSpawnReason())
        {
        case DEFAULT:
        case NATURAL:
        case JOCKEY:
        case CHUNK_GEN:
        case SPAWNER:
        case EGG:
        case SPAWNER_EGG:
        case LIGHTNING:
        case BUILD_SNOWMAN:
        case BUILD_IRONGOLEM:
        case BUILD_WITHER:
        case VILLAGE_DEFENSE:
        case VILLAGE_INVASION:
        case BREEDING:
        case SLIME_SPLIT:
        case REINFORCEMENTS:
        case NETHER_PORTAL:
        case DISPENSE_EGG:
        case INFECTION:
        case CURED:
        case OCELOT_BABY:
        case SILVERFISH_BLOCK:
        case MOUNT:
        case TRAP:
        case ENDER_PEARL:
            if (getGame() == null || !getGame().getOption(GameOption.NATURAL_MOBS))
            {
                event.setCancelled(true);
            }
            break;
        case CUSTOM:
            break;
        default:
            UtilError.log("SpawnReason " + event.getSpawnReason() + " is not handled");
            break;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event)
    {
        if (!getManager().getLobby().isFrozen() || !getOption(GameOption.LOCK_TO_SPAWN))
            return;

        if (UtilLoc.getDistance2d(event.getFrom(), event.getTo()) == 0)
            return;

        event.setCancelled(true);

        Location loc = event.getFrom();
        loc.setDirection(event.getTo().getDirection());
    }

    @EventHandler
    public void onOffhandBowClick(InventoryClickEvent event)
    {
        if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked())
            return;

        if (event.getClick().isKeyboardClick())
        {
            if (event.getHotbarButton() == -1)
                return;

            ItemStack item = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());

            if (event.getSlotType() != SlotType.QUICKBAR || event.getSlot() != 9 || !UtilInv.isItem(item, Material.BOW))
                return;
        }
        else
        {
            if (event.getSlot() != 9 || event.getSlotType() != SlotType.QUICKBAR
                    || !UtilInv.isItem(event.getCursor(), Material.BOW))
                return;
        }

        event.setCancelled(true);
    }

    @EventHandler
    public void onPickupItem(PlayerPickupItemEvent event)
    {
        Player player = event.getPlayer();

        if (isAlive(player) && getOption(GameOption.PICKUP_ITEM) && isLive())
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (getGame().getState().isPreGame())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(event.getPlayer()))
        {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event)
    {
        Iterator<LivingEntity> itel = event.getAffectedEntities().iterator();

        while (itel.hasNext())
        {
            Entity entity = itel.next();

            if (!(entity instanceof Player))
            {
                continue;
            }

            if (!getGame().isAlive(entity))
            {
                itel.remove();
            }
        }
    }

    @EventHandler
    public void onPreCraft(PrepareItemCraftEvent event)
    {
        if (getOption(GameOption.ALLOW_CRAFTING))
            return;

        event.getInventory().setResult(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onRegeneration(EntityRegainHealthEvent event)
    {
        if (!getGame().isEnded())
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event)
    {
        if (!getGame().getOption(GameOption.TEAM_HOTBAR))
            return;

        ItemStack item = event.getItem();

        if (item == null || item.getType() != Material.LEATHER_CHESTPLATE)
            return;

        if (event.getPlayer().getInventory().getHeldItemSlot() != 8)
            return;

        event.setCancelled(true);
        event.getPlayer().updateInventory();
    }

    @EventHandler
    public void onShoot(ProjectileLaunchEvent event)
    {
        if (!(event.getEntity() instanceof Arrow))
            return;

        if (!(event.getEntity().getShooter() instanceof Player))
            return;

        Player player = (Player) event.getEntity().getShooter();

        if (!UtilInv.isHolding(player, EquipmentSlot.OFF_HAND, Material.BOW))
            return;

        event.setCancelled(true);

        player.sendMessage(C.Red + "You cannot use a bow with your weak hand!");
    }

    @EventHandler
    public void onSpread(BlockSpreadEvent event)
    {
        event.setCancelled(true);
    }

    @EventHandler
    public void onSwitchBowHands(PlayerSwapHandItemsEvent event)
    {
        if (!UtilInv.isItem(event.getOffHandItem(), Material.BOW))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onSwitchTeamHotbar(PlayerSwapHandItemsEvent event)
    {
        if (!getGame().getOption(GameOption.TEAM_HOTBAR))
            return;

        if (event.getPlayer().getInventory().getHeldItemSlot() != 8)
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event)
    {
        Entity damager = event.getEntity();
        Entity damagee = event.getTarget();

        if (damagee == null)
            return;

        if (getGame().isPreGame())
        {
            event.setCancelled(true);
        }
        else if (!isAlive(damagee))
        {
            event.setCancelled(true);
        }
        else
        {
            GameTeam team1 = getGame().getTeam(damagee);
            GameTeam team2 = getGame().getTeam(damager);

            boolean sameTeam = Objects.equals(team1, team2);

            if (team1 != null && sameTeam && !getOption(GameOption.ATTACK_TEAM))
            {
                event.setCancelled(true);
            }
            else if (team1 != null && !sameTeam && !getOption(GameOption.ATTACK_NON_TEAM))
            {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTeamInventoryClick(InventoryClickEvent event)
    {
        if (!getGame().getOption(GameOption.TEAM_HOTBAR))
            return;

        if (event.getClickedInventory() == null || event.getClickedInventory().getHolder() != event.getWhoClicked())
            return;

        if ((event.getSlot() != 8) && (!event.getClick().isKeyboardClick() || event.getHotbarButton() != 8))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onTimeChange(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        if (getOption(GameOption.TIME_CYCLE) == 0)
            return;

        World world = getManager().getWorld().getGameWorld();

        long newTime = world.getFullTime() + getOption(GameOption.TIME_CYCLE);

        newTime = (newTime + 24000) % 24000;

        world.setFullTime(newTime);
    }

    @EventHandler
    public void onVechileEnter(VehicleEnterEvent event)
    {
        if (isAlive(event.getEntered()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onVechileEvent(VehicleDestroyEvent event)
    {
        if (event.getAttacker() == null || isAlive(event.getAttacker()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onVechileMove(VehicleEntityCollisionEvent event)
    {
        if (isAlive(event.getEntity()))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event)
    {
        if (getGame() == null || getOption(GameOption.WEATHER))
        {
            return;
        }

        event.setCancelled(true);

        /*  if (event.getWorld() == getManager().getWorld().getGameWorld() && getGame().isLive() && getOption(GameOption.WEATHER))
            return;

        if (event.getWorld() == getManager().getWorld().getGameWorld() || event.toWeatherState())
            event.setCancelled(true);*/
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onWorldInit(WorldInitEvent event)
    {
        World world = event.getWorld();

        world.setKeepSpawnInMemory(false);
    }

}
