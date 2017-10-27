package me.libraryaddict.arcade.managers;

public enum GameState
{
    /**
     * When the server is cleaning up
     */
    Dead(5), /**
              * When the game has ended
              */
    End(4),
    // Frozen(2),
    /**
     * When the server is now in progress
     */
    Live(3), /**
              * When the map has been picked and loaded
              */
    MapLoaded(1), /**
                   * When the map for the game hasn't been picked
                   */
    PreMap(0), /**
                * When the players are frozen and waiting to move. This state can be called multiple times.
                */
    PrepareGame(2);

    private int _order;

    private GameState(int order)
    {
        _order = order;
    }

    public int getOrder()
    {
        return _order;
    }

    public boolean isEnd()
    {
        return this == End || this == Dead;
    }

    public boolean isLive()
    {
        return this == Live;
    }

    public boolean isPreGame()
    {
        return this == PreMap || this == MapLoaded || this == PrepareGame;
    }
}
