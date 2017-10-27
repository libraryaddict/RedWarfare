package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.TeamGame;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.core.chat.ChatEvent;

public class SpyAbility extends Ability
{
    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(ChatEvent event)
    {
        Player player = event.getPlayer();

        GameTeam team = getGame().getTeam(player);

        if (team == null)
            return;

        if (hasAbility(player))
        {
            event.setCancelled(true);

            Bukkit.getLogger().log(Level.INFO, event.getFinalUncensored());

            for (Player receiver : event.getRecipients())
            {
                GameTeam hisTeam = getGame().getTeam(receiver);

                event.setDisplayName((hisTeam != null ? hisTeam : team).getColoring() + player.getName());

                if (receiver == event.getPlayer())
                {
                    receiver.sendMessage(event.getFinalUncensored());
                }
                else
                {
                    receiver.sendMessage(event.getFinalCensored());
                }
            }
        }
        else
        {
            event.setDisplayName(team.getColoring() + event.getDisplayName());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onHatEvent(EquipmentEvent event)
    {
        if (!hasAbility(event.getWearer()))
            return;

        ItemStack item = ((TeamGame) getGame()).getCosmeticGearItem(event.getViewer(), getGame().getTeam(event.getViewer()),
                event.getSlot());

        if (item == null)
            return;

        if (event.getSlot() == EquipmentSlot.FEET && item.getItemMeta() instanceof LeatherArmorMeta)
        {
            LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();

            Color color = meta.getColor();
            meta.setColor(color.mixColors(color, color, color, Color.GRAY));

            item.setItemMeta(meta);
        }

        event.setHat(item);
    }
}
