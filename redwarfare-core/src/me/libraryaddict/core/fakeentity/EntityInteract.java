package me.libraryaddict.core.fakeentity;

import org.bukkit.entity.Player;

public interface EntityInteract
{
    public enum InteractType
    {
        ATTACK, MAIN_INTERACT, OFFHAND_INTERACT;
    }

    public void onInteract(Player player, InteractType interactType);
}
