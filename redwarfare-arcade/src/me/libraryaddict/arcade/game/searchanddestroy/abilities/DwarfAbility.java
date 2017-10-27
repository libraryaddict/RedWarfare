package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.HashMap;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilInv;

public class DwarfAbility extends Ability
{
    private HashMap<Enchantment, Integer> _levelEvery = new HashMap<Enchantment, Integer>();
    private HashMap<Enchantment, Integer> _maxLevel = new HashMap<Enchantment, Integer>();

    public DwarfAbility()
    {
        _maxLevel.put(Enchantment.DAMAGE_ALL, 5);
        _maxLevel.put(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
        _maxLevel.put(Enchantment.PROTECTION_EXPLOSIONS, 6);

        _levelEvery.put(Enchantment.DAMAGE_ALL, 3);
        _levelEvery.put(Enchantment.PROTECTION_ENVIRONMENTAL, 6);
        _levelEvery.put(Enchantment.PROTECTION_EXPLOSIONS, 8);
    }

    private void onChange(Player player, boolean levelUp)
    {
        int level = player.getLevel();

        if (level > 5)
        {
            int potionLevel = level - 5;

            if (!levelUp)
            {
                potionLevel--;
            }

            if (potionLevel < 0)
            {
                ConditionManager.removeCondition(player, "SLOW");
            }
            else
            {
                ConditionManager.addPotion(new PotionEffect(PotionEffectType.SLOW, 999999, potionLevel), player, false);
            }
        }

        for (ItemStack item : UtilInv.getNonClonedInventory(player))
        {
            if (item.getType() == Material.LEATHER_CHESTPLATE)
                continue;

            for (Entry<Enchantment, Integer> entry : _levelEvery.entrySet())
            {
                Enchantment enchant = entry.getKey();

                if (!enchant.canEnchantItem(item))
                    continue;

                if (level % entry.getValue() != 0)
                    continue;

                int newLevel = level / entry.getValue();

                if (levelUp)
                {
                    if (newLevel > _maxLevel.get(enchant))
                        continue;

                    if (newLevel <= 0)
                        continue;

                    item.addUnsafeEnchantment(enchant, newLevel);
                }
                else
                {
                    newLevel--;

                    if (newLevel < 0 || newLevel > _maxLevel.get(enchant))
                        continue;

                    if (newLevel == 0)
                    {
                        item.removeEnchantment(enchant);
                    }
                    else
                    {
                        item.addUnsafeEnchantment(enchant, newLevel);
                    }
                }
            }
        }

        player.updateInventory();
    }

    @EventHandler
    public void onTick(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (Player player : getPlayers(true))
        {
            float playerExp = player.getExp();
            float expToGive = 0;

            if (player.isSprinting())
            {
                expToGive = -0.02F;
            }
            else if (player.isSneaking())
            {
                expToGive = 0.005F;
            }
            else
            {
                expToGive = -0.005F;
            }

            if (expToGive < 0)
                expToGive += (expToGive * player.getLevel() * 0.02);

            if (expToGive < 0 && playerExp <= 0 && player.getLevel() == 0)
                continue;

            /*if (expToGive > 0)
            {
                GameTeam team = getGame().getTeam(player);

                for (TeamBomb bomb : ((SearchAndDestroy) getGame()).getBombs())
                {
                    if (bomb.getTeam() != team)
                        continue;

                    if (UtilLoc.getDistance(player.getLocation(),
                            bomb.isArmed() ? bomb.getBomb().getLocation() : bomb.getBlock().getLocation()) > 8)
                    {
                        continue;
                    }

                    if (Recharge.canUse(player, "DwarfBomb"))
                    {
                        Recharge.use(player, "DwarfBomb", 15000);
                        player.sendMessage(C.Red + "Due to being close to the bomb, leveling speed decreased!");
                    }

                    expToGive = 0.003F;
                    break;
                }
            }*/

            playerExp += expToGive;

            if (playerExp <= 0)
            {
                if (player.getLevel() > 0)
                {
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 1, 0);

                    onChange(player, false);

                    player.setLevel(player.getLevel() - 1);

                    playerExp = 1;
                }
                else
                {
                    playerExp = 0;
                }
            }
            else if (playerExp >= 1)
            {
                if (player.getLevel() < 48)
                {
                    player.setLevel(player.getLevel() + 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 0);

                    onChange(player, true);

                    playerExp = 0;
                }
                else
                {
                    playerExp = 1;
                }
            }

            player.setExp(playerExp);
        }
    }

}
