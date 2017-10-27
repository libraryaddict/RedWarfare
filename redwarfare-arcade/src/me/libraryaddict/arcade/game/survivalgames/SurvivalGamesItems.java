package me.libraryaddict.arcade.game.survivalgames;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.LootTier;
import me.libraryaddict.arcade.managers.LootManager;
import me.libraryaddict.core.C;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.damage.AttackType;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.inventory.utils.RandomItem;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilString;

public class SurvivalGamesItems implements Listener
{
    private ItemStack _compass = new ItemBuilder(Material.COMPASS).setTitle(C.White + "Player Tracker")
            .addLore(C.Blue + "Uses remaining: " + C.White + "5",
                    "Use this to find the location and distance of the nearest player!")
            .build();
    private ItemStack _deathSkull = new ItemBuilder(Material.SKULL_ITEM, 1, (short) 1).setTitle(C.DGray + "Death Skull")
            .addLore(C.Gray + "Click on me to die!").build();
    private ItemStack _enchantingTable = new ItemBuilder(Material.ENCHANTMENT_TABLE)
            .setTitle(C.White + "Portable Enchantment Table")
            .addLore("This is a one use item to open up an enchantment table whereever you may be!").build();
    private ItemStack _shopIcon = new ItemBuilder(Material.MAGMA_CREAM).setTitle(C.White + "Portable Shop")
            .addLore("This is a one use item to access the shop").build();
    private ItemStack _snakeEyes = new ItemBuilder(Material.EYE_OF_ENDER).setTitle(C.White + "Snake Eyes")
            .addLore("This is a one use item which will give everyone in the area including you a blindness effect!").build();
    private ItemStack _speedBoost = new ItemBuilder(Material.QUARTZ).setTitle(C.White + "Speed Booster")
            .addLore(C.Purple + "This is a one use item which will give everyone in the area including you a nice speed boost!")
            .build();
    private SurvivalGames _survivalGames;
    private ItemStack _zombieCurse = new ItemBuilder(Material.INK_SACK, 1, (short) 2).setTitle(C.White + "Hunger Destroyer")
            .addLore(
                    C.Purple + "This is a one use item which will give everyone in the area including you a strong craving for food! Free nausea included!")
            .build();
    private AttackType DEATH_SKULL = new AttackType("Death Skull",
            "%Killed% was a little too hasty while looting and clicked on the death skull..").setNoKnockback().setInstantDeath();

    public SurvivalGamesItems(SurvivalGames survivalGames)
    {
        _survivalGames = survivalGames;

        setupLoot();
    }

    private boolean canUse(Player player, String name)
    {
        if (!Recharge.canUse(player, "Special Item"))
        {
            player.sendMessage(C.Red + "You can't use a special item again yet! Wait "
                    + UtilNumber.getTime(Recharge.getTimeLeft(player, "Special Item"), TimeUnit.MILLISECONDS) + "!");
            return false;
        }

        return true;
    }

    public ItemStack getDeathSkull()
    {
        return _deathSkull;
    }

    public Game getGame()
    {
        return _survivalGames;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event)
    {
        if (!(event.getInventory() instanceof EnchantingInventory))
            return;

        ((EnchantingInventory) event.getInventory()).setSecondary(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event)
    {
        if (getGame().isPreGame())
            return;

        if (!event.getAction().name().contains("RIGHT"))
            return;

        Player player = event.getPlayer();

        if (!getGame().isAlive(player))
            return;

        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR)
            return;

        if (item.getType() == Material.COMPASS)
        {
            int uses = 0;

            try
            {
                uses = Integer.parseInt(ChatColor.stripColor(item.getItemMeta().getLore().get(0)).replaceAll("\\D+", ""));
            }
            catch (Exception ex)
            {
            }

            if (uses > 0)
            {
                Player target = UtilLoc.getClosest(player.getLocation(), _survivalGames.getPlayers(true), 15, player);

                if (target == null)
                {
                    player.sendMessage(C.Red + "Can't find anyone, pointing to spawn!");
                    player.setCompassTarget(player.getWorld().getSpawnLocation());
                    return;
                }

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);
                target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);

                player.setCompassTarget(target.getLocation());

                player.sendMessage(
                        C.Red + "Located " + target.getName() + " " + (int) UtilLoc.getDistance(player, target) + " blocks away");
                target.sendMessage(C.Red + "Uh oh! " + player.getName() + " just used a compass on you!");

                uses--;

                if (uses > 0)
                {
                    player.sendMessage(C.Red + uses + " use" + (uses != 1 ? "s" : "") + " of the compass remaining");
                }
                else
                {
                    player.sendMessage(C.Red + "No remaining uses! Next use will break it!");
                }

                new ItemBuilder(item).clearLore().addLore(C.Blue + "Uses remaining: " + C.White + uses,
                        "Use this to find the location and distance of the nearest player!").setModifyBaseItem().build();
            }
            else
            {
                player.sendMessage(C.Red + "The compass breaks! No remaining uses!");
                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 2);

                UtilInv.remove(player, item, 1);
            }
        }
        else if (UtilInv.isSimilar(_snakeEyes, item))
        {
            if (!canUse(player, "Snake Eyes"))
                return;

            Stats.add(player, "Game." + getGame().getName() + ".Snake Eyes");

            Recharge.use(player, "Special Item", 30000);

            UtilInv.remove(player, item, 1);

            ArrayList<Player> effected = UtilLoc.getInRadius(player.getLocation(), _survivalGames.getPlayers(true), 15, 10,
                    Player.class);

            ArrayList<String> names = new ArrayList<String>();

            for (Player p : effected)
            {
                if (p != player)
                {
                    names.add(p.getName());
                }

                ConditionManager.addPotion(new PotionEffect(PotionEffectType.BLINDNESS, 15 * 20, 0), p, player, false);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SPIDER_AMBIENT, 2, 0);
            }

            if (names.isEmpty())
            {
                player.sendMessage(C.DGreen + "No one was effected by the snakes eyes but you!");
            }
            else
            {
                player.sendMessage(C.DGreen + "The following players were effected: " + C.Green
                        + UtilString.join(names, C.DGreen + ", " + C.Green) + C.DGreen + "!");
            }
        }
        else if (UtilInv.isSimilar(_speedBoost, item))
        {
            if (!canUse(player, "Speed Boost"))
                return;

            Stats.add(player, "Game." + getGame().getName() + ".Speed Boost");

            Recharge.use(player, "Special Item", 30000);

            UtilInv.remove(player, item, 1);

            ArrayList<Player> effected = UtilLoc.getInRadius(player.getLocation(), _survivalGames.getPlayers(true), 5, 5,
                    Player.class);

            int ticks = (int) ((30 * 20) * (1.1 - (effected.size() * 0.15D)));

            ArrayList<String> names = new ArrayList<String>();

            for (Player p : effected)
            {
                if (p != player)
                {
                    names.add(p.getName());
                }

                ConditionManager.addPotion(new PotionEffect(PotionEffectType.SPEED, ticks, 2), p, player, false);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BAT_LOOP, 1.2F, 2);
            }

            if (names.isEmpty())
            {
                player.sendMessage(C.DGreen + "No one was effected by the speed boost but you!");
            }
            else
            {
                player.sendMessage(C.DGreen + "The following players were effected: " + C.Green
                        + UtilString.join(names, C.DGreen + ", " + C.Green) + C.DGreen + "!");
            }
        }
        else if (UtilInv.isSimilar(_zombieCurse, item))
        {
            if (!canUse(player, "Hunger Buster"))
                return;

            Stats.add(player, "Game." + getGame().getName() + ".Hunger Busters");
            Recharge.use(player, "Special Item", 30000);

            UtilInv.remove(player, item, 1);

            ArrayList<Player> effected = UtilLoc.getInRadius(player.getLocation(), _survivalGames.getPlayers(true), 15, 10,
                    Player.class);

            ArrayList<String> names = new ArrayList<String>();

            for (Player p : effected)
            {
                if (p != player)
                {
                    names.add(p.getName());
                }

                ConditionManager.addPotion(new PotionEffect(PotionEffectType.HUNGER, 10 * 20, 6), p, player, false);
                ConditionManager.addPotion(new PotionEffect(PotionEffectType.CONFUSION, 10 * 20, 15), p, player, false);

                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PIG_DEATH, 2, 0);
            }

            if (names.isEmpty())
            {
                player.sendMessage(C.DGreen + "No one was effected by hunger buster but you!");
            }
            else
            {
                player.sendMessage(C.DGreen + "The following players were effected: " + C.Green
                        + UtilString.join(names, C.DGreen + ", " + C.Green) + C.DGreen + "!");
            }
        }
        else if (UtilInv.isSimilar(_shopIcon, item))
        {
            UtilInv.remove(player, item, 1);
        }
        else if (UtilInv.isSimilar(_enchantingTable, item))
        {
            Stats.add(player, "Game." + getGame().getName() + ".Enchanting Table");

            Block block = player.getWorld().getBlockAt(999999, 1, 999999);
            block.setType(Material.ENCHANTMENT_TABLE);

            InventoryView view = player.openEnchanting(block.getLocation(), true);

            ((EnchantingInventory) view.getTopInventory()).setSecondary(new ItemStack(Material.INK_SACK, 40, (short) 4));

            UtilInv.remove(player, item, 1);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        if (!UtilInv.isSimilar(event.getCurrentItem(), getDeathSkull()))
            return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();

        if (!getGame().isAlive(player))
            return;

        getGame().getDamageManager().newDamage(player, DEATH_SKULL, 1);
        Stats.add(player, "Game." + getGame().getName() + ".Death Skulls");
    }

    @EventHandler
    public void onSpeedParticles(PlayerMoveEvent event)
    {
        Player player = event.getPlayer();

        if (!getGame().isAlive(player))
            return;

        if (!player.hasPotionEffect(PotionEffectType.SPEED))
        {
            return;
        }

        UtilParticle.playParticle(ParticleType.CLOUD, player.getLocation().add(0, 0.1, 0), 0.1, 0, 0.1, 3);
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        for (Player player : getGame().getPlayers(true))
        {
            if (player.hasPotionEffect(PotionEffectType.BLINDNESS))
            {
                UtilParticle.playParticle(ParticleType.SUSPEND, player.getLocation().add(0, 1, 0), 0.4, 1, 0.4, 9);
            }
        }
    }

    private void setupLoot()
    {
        LootManager manager = getGame().getManager().getLoot();

        LootTier tier1 = manager.getLoot("Tier1");

        tier1.addLoot(new RandomItem(Material.RAW_FISH, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.RAW_BEEF, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.RAW_CHICKEN, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.POTATO_ITEM, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.CARROT_ITEM, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.WHEAT, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.APPLE, 1, 2, 0.05));
        // Below is cocoa beans
        tier1.addLoot(new RandomItem(new ItemStack(Material.INK_SACK, 1, (short) 3), 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.ROTTEN_FLESH, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.STICK, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.WOOD_AXE, 0.08));
        tier1.addLoot(new RandomItem(Material.STONE_AXE, 0.08));
        tier1.addLoot(new RandomItem(Material.WOOD_SWORD, 0.08));
        tier1.addLoot(new RandomItem(Material.BOAT, 0.05));
        tier1.addLoot(new RandomItem(Material.FLINT, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.FEATHER, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.GOLD_INGOT, 1, 2, 0.05));
        // tier1.addLoot(new RandomItem(Material.SHEARS, 0.05));
        tier1.addLoot(new RandomItem(Material.LEATHER_BOOTS, 0.05));
        tier1.addLoot(new RandomItem(Material.LEATHER_CHESTPLATE, 0.05));
        tier1.addLoot(new RandomItem(Material.LEATHER_HELMET, 0.05));
        tier1.addLoot(new RandomItem(Material.LEATHER_LEGGINGS, 0.05));
        tier1.addLoot(new RandomItem(Material.CHAINMAIL_BOOTS, 0.05));
        tier1.addLoot(new RandomItem(Material.CHAINMAIL_CHESTPLATE, 0.05));
        tier1.addLoot(new RandomItem(Material.CHAINMAIL_HELMET, 0.05));
        tier1.addLoot(new RandomItem(Material.CHAINMAIL_LEGGINGS, 0.05));
        tier1.addLoot(new RandomItem(Material.FISHING_ROD, 0.05));
        tier1.addLoot(new RandomItem(Material.BOW, 0.05));
        tier1.addLoot(new RandomItem(Material.ARROW, 1, 5, 0.05));
        tier1.addLoot(new RandomItem(Material.SNOW_BALL, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.EGG, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.PORK, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.EXP_BOTTLE, 1, 2, 0.05));
        tier1.addLoot(new RandomItem(Material.FLINT_AND_STEEL, 0.02));
        tier1.addLoot(new RandomItem(_compass, 0.03).setUnique());
        tier1.addLoot(new RandomItem(_enchantingTable, 0.03).setUnique());
        // tier1.addLoot(new RandomItem(_shopIcon, 0.02).setUnique());
        tier1.addLoot(new RandomItem(_snakeEyes, 0.03).setUnique());
        tier1.addLoot(new RandomItem(_speedBoost, 0.03).setUnique());
        tier1.addLoot(new RandomItem(_zombieCurse, 0.03).setUnique());

        LootTier tier2 = tier1.clone();

        manager.setLoot("Tier2", tier2);

        tier2.addLoot(new RandomItem(Material.BAKED_POTATO, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.CAKE, 0.05));
        tier2.addLoot(new RandomItem(Material.COOKED_BEEF, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.COOKED_CHICKEN, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.COOKED_FISH, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.GRILLED_PORK, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.COOKIE, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.PUMPKIN_PIE, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.APPLE, 1, 2, 0.05));
        // tier2.addLoot(new RandomItem(Material.BOWL, 1, 2, 0.05));
        tier2.addLoot(new RandomItem(Material.STONE_SWORD, 0.1));
        tier2.addLoot(new RandomItem(Material.IRON_INGOT, 1, 2, 0.1));
        tier2.addLoot(new RandomItem(Material.DIAMOND, 0.08));
        tier2.addLoot(new RandomItem(Material.IRON_BOOTS, 0.05));
        tier2.addLoot(new RandomItem(Material.IRON_CHESTPLATE, 0.05));
        tier2.addLoot(new RandomItem(Material.IRON_HELMET, 0.05));
        tier2.addLoot(new RandomItem(Material.IRON_LEGGINGS, 0.05));
        tier2.addLoot(new RandomItem(_deathSkull, 0.05));

        LootTier deathMatch = manager.getLoot("Deathmatch");

        deathMatch.addLoot(new RandomItem(Material.PUMPKIN_PIE, 0.2));
        deathMatch.addLoot(new RandomItem(Material.BAKED_POTATO, 0.2));
        deathMatch.addLoot(new RandomItem(Material.CAKE, 0.2));
        deathMatch.addLoot(new RandomItem(Material.APPLE, 0.2));
        deathMatch.addLoot(new RandomItem(Material.CARROT_ITEM, 1, 3, 0.2));
        deathMatch.addLoot(new RandomItem(Material.WOOD_SWORD, 0.2));
        deathMatch.addLoot(new RandomItem(Material.WOOD_AXE, 0.2));
        deathMatch.addLoot(new RandomItem(Material.STONE_AXE, 0.15));
        deathMatch.addLoot(new RandomItem(Material.STONE_SWORD, 0.05));

        LootTier furnace = manager.getLoot("Furnace");

        furnace.addSlotLoot(0, new RandomItem(Material.RAW_BEEF, 1, 2, 0));
        furnace.addSlotLoot(0, new RandomItem(Material.RAW_CHICKEN, 1, 2, 0));
        furnace.addSlotLoot(0, new RandomItem(Material.RAW_FISH, 1, 2, 0));
        furnace.addSlotLoot(0, new RandomItem(Material.PORK, 1, 2, 0));
        furnace.addSlotLoot(0, new RandomItem(Material.POTATO_ITEM, 1, 2, 0));
        furnace.addSlotLoot(1, new RandomItem(Material.STICK, 1, 2, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.COOKED_BEEF, 1, 2, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.COOKED_CHICKEN, 1, 2, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.COOKED_FISH, 1, 2, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.GRILLED_PORK, 1, 2, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.BAKED_POTATO, 1, 1, 0));
        furnace.addSlotLoot(2, new RandomItem(Material.PUMPKIN_PIE, 1, 1, 0));
    }

}
