package me.libraryaddict.core.cosmetics.types;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import me.libraryaddict.core.cosmetics.Cosmetic;
import me.libraryaddict.core.player.events.PlayerLoadEvent;
import me.libraryaddict.core.player.events.PreferenceSetEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.Disguise;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MiscDisguise;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;

public abstract class CosmeticDisguise extends Cosmetic
{
    public CosmeticDisguise(String name)
    {
        super(name);
    }

    private void disguiseEntity(Player player)
    {
        Disguise disguise;

        if (getDisguise().isMob())
            disguise = new MobDisguise(getDisguise());
        else
            disguise = new MiscDisguise(getDisguise());

        disguise.getWatcher().setCustomName(getManager().getRank().getDisplayedRank(player).getPrefix() + player.getName());
        disguise.getWatcher().setCustomNameVisible(true);

        disguise.setEntity(player).startDisguise();
    }

    public abstract DisguiseType getDisguise();

    @EventHandler
    public void onPlayerLoad(PlayerLoadEvent event)
    {
        if (!isEnabled(event.getPlayer()))
            return;

        disguiseEntity(event.getPlayer());
    }

    @EventHandler
    public void onToggle(PreferenceSetEvent event)
    {
        if (event.getPreference() != getToggled())
            return;

        if (isEnabled(event.getPlayer()))
        {
            disguiseEntity(event.getPlayer());
        }
        else
        {
            DisguiseAPI.undisguiseToAll(event.getPlayer());
        }
    }
}
