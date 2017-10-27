package me.libraryaddict.core.hologram;

import org.bukkit.entity.Player;

public interface HologramInteract
{
    public enum InteractType
    {
        ATTACK, MAIN_INTERACT, OFFHAND_INTERACT;
    }

    public void onInteract(Player player, InteractType interactType);
}
