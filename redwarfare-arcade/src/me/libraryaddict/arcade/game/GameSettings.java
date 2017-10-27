package me.libraryaddict.arcade.game;

import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.survivalgames.SurvivalGames;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.utils.UtilError;

public enum GameSettings
{
   // Disasters(ServerType.Disaster, SurviveDisasters.class),

    SearchAndDestroy(ServerType.SearchAndDestroy, SearchAndDestroy.class),

    SurvivalGames(ServerType.SurvivalGames, SurvivalGames.class);

    public static GameSettings getGameType(ServerType gameDisplay)
    {
        for (GameSettings gameType : values())
        {
            if (gameType.getDisplay() == gameDisplay)
            {
                return gameType;
            }
        }

        return null;
    }

    private Class<? extends Game> _gameClass;

    private ServerType _gameDisplay;

    private GameSettings(ServerType gameDisplay, Class<? extends Game> gameClass)
    {
        _gameDisplay = gameDisplay;
        _gameClass = gameClass;
    }

    public Game createInstance(ArcadeManager arcadeManager)
    {
        try
        {
            System.out.println("Creating game " + name());
            return _gameClass.getConstructor(ArcadeManager.class).newInstance(arcadeManager);
        }
        catch (Throwable ex)
        {
            while (ex != null)
            {
                UtilError.handle(ex);

                ex = ex.getCause();
            }
        }

        return null;
    }

    public ServerType getDisplay()
    {
        return _gameDisplay;
    }
}
