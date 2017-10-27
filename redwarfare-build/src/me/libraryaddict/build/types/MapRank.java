package me.libraryaddict.build.types;

import me.libraryaddict.core.C;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public enum MapRank {
    /**
     * Can add/remove builders
     */
    ADMIN("Admin", C.Red, new ItemStack(Material.REDSTONE_BLOCK),
            "This is the rank you give to your second in command!",
            "Admin's can promote and demote other builders, invite and fire them and has full access to everything!",
            "As VoxelSniper is incredibly powerful, only admin's have access to this tool in your world!",
            "Well, almost everything. They cannot restore backups, change the frequency of the backups, delete the "
                    + "map or replace the creator"),

    /**
     * Can only build in the map
     */
    BUILDER("Builder", C.DGreen, new ItemStack(Material.DIRT),
            "The builder rank can not only join the map, but he can build as well!",
            "However he cannot do much more than place blocks, destroy blocks, spawn and kill entities."),

    /**
     * Can use worldedit and the map tools such as spawnpoints, border
     */
    EDITOR("Editor", C.Aqua, new ItemStack(Material.MAP), "The editor rank is what you give to your trusted members!",
            "Editors have the power to use build tools from WorldEdit, as well as most of the map specific tools " +
                    "such" + " as creating team spawns and setting the borders!",
            "VoxelSniper is too powerful to leave in the control of mere editors!"),

    /**
     * Can only load the map
     */
    VISITOR("Visitor", C.Yellow, new ItemStack(Material.INK_SACK, 1, (short) 11),
            "The visitor rank can only join the maps and look around, they are unable to edit the map!");// ,

    // NONE("No Rank", C.White, new ItemStack(Material.GLASS), "This person has no rank");

    static {
        ADMIN.setOwns(EDITOR);
        EDITOR.setOwns(BUILDER);
        BUILDER.setOwns(VISITOR);
    }

    private String _color;
    private String[] _desc;
    private ItemStack _icon;
    private String _name;
    private MapRank[] _owns = new MapRank[0];

    private MapRank(String name, String color, ItemStack icon, String... desc) {
        _name = name;
        _icon = icon;
        _color = color;
        _desc = desc;
    }

    public String getColor() {
        return _color;
    }

    public String[] getDesc() {
        return _desc;
    }

    public ItemStack getIcon() {
        return _icon;
    }

    public String getName() {
        return _name;
    }

    public boolean has(MapRank rank) {
        for (MapRank r : _owns) {
            if (!r.has(rank))
                continue;

            return true;
        }

        return rank == this;
    }

    private void setOwns(MapRank... ranks) {
        _owns = ranks;
    }
}
