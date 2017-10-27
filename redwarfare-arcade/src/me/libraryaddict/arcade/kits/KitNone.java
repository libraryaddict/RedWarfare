package me.libraryaddict.arcade.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class KitNone extends Kit {

    public KitNone() {
        super("No Kit", KitAvailibility.Free, new String[] {
                "You shouldn't see this"
        });
    }

    @Override
    public Material[] getArmorMats() {
        return new Material[4];
    }

    @Override
    public Material getMaterial() {
        return Material.APPLE;
    }

    @Override
    public void giveItems(Player player) {
    }
}
