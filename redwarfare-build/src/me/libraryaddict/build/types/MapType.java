package me.libraryaddict.build.types;

import me.libraryaddict.build.customdata.*;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.utils.UtilError;
import org.bukkit.Material;

public enum MapType {
    Custom("Generic Game", ServerType.Vanilla, Material.CARROT_ITEM, GenericGameCustomData.class),

    Disasters("Disasters", ServerType.Disaster, Material.MAGMA, GenericGameCustomData.class),

    Hub("Main Hub", ServerType.Hub, Material.ENDER_PEARL, HubCustomData.class),

    SearchAndDestroy("Search and Destroy", ServerType.SearchAndDestroy, Material.BLAZE_POWDER,
            SearchAndDestroyCustomData.class),

    SearchAndDestroyHub("Search and Destroy Hub", ServerType.SearchAndDestroy, Material.BLAZE_ROD,
            GameHubCustomData.class),

    SurvivalGames("SurvivalGames", ServerType.SurvivalGames, Material.IRON_SWORD, SurvivalGamesCustomData.class),

    SurvivalGamesHub("SurvivalGames Hub", ServerType.SurvivalGames, Material.WOOD_SWORD, GameHubCustomData.class),

    Unknown("Normal Map", ServerType.Vanilla, Material.MAP, WorldCustomData.class);

    public static MapType getType(String name) {
        for (MapType type : MapType.values()) {
            if (!type.name().equalsIgnoreCase(name))
                continue;

            return type;
        }

        return MapType.Unknown;
    }

    private Class<? extends WorldCustomData> _customData;
    private Material _material;
    private String _name;
    private ServerType _serverType;

    private MapType(String name, ServerType serverType, Material material,
            Class<? extends WorldCustomData> customData) {
        _name = name;
        _material = material;
        _serverType = serverType;
        _customData = customData;
    }

    public WorldCustomData createData(WorldInfo worldInfo) {
        try {
            return _customData.getConstructor(WorldInfo.class).newInstance(worldInfo);
        }
        catch (Exception ex) {
            UtilError.handle(ex);
        }

        return null;
    }

    public Class<? extends WorldCustomData> getCustomData() {
        return _customData;
    }

    public Material getIcon() {
        return _material;
    }

    public String getName() {
        return _name;
    }

    public ServerType getServerType() {
        return _serverType;
    }
}
