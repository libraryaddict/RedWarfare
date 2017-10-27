package me.libraryaddict.redis;

import me.libraryaddict.core.utils.UtilString;

public class RedisKey
{
    public static RedisKey BUNGEE_STATUS = new RedisKey("Bungee.Status.%s");
    public static RedisKey KILL_SERVER = new RedisKey("notify.KillServer");
    public static RedisKey NOTIFY_BUILD_GAMEPROFILE = new RedisKey("notify.GameProfileBuild");
    public static RedisKey NOTIFY_BUILD_JOIN_MAP = new RedisKey("notify.JoinMap");
    public static RedisKey NOTIFY_BUILD_SERVER_REQUEST = new RedisKey("notify.ServerRequest");
    public static RedisKey NOTIFY_BUNGEE_SETTINGS = new RedisKey("notify.BungeeSettings");
    public static RedisKey NOTIFY_BUNGEE_STATUS = new RedisKey("notify.BungeeStatus");
    public static RedisKey NOTIFY_CHAT = new RedisKey("notify.Chat");
    public static RedisKey NOTIFY_CURRENCY = new RedisKey("notify.Currency");
    public static RedisKey NOTIFY_JOIN_GAME = new RedisKey("notify.JoinGame");
    public static RedisKey NOTIFY_KICK_PLAYER = new RedisKey("notify.KickPlayer");
    public static RedisKey NOTIFY_MANAGER_OFFER = new RedisKey("notify.ServerConfirm");
    public static RedisKey NOTIFY_MAP_INFO = new RedisKey("notify.MapInfo.%s");
    public static RedisKey NOTIFY_MUTE = new RedisKey("notify.Mute");
    public static RedisKey NOTIFY_OWNED = new RedisKey("notify.Owned");
    public static RedisKey NOTIFY_RANK_UPDATE = new RedisKey("notify.RankUpdate");
    public static RedisKey NOTIFY_REQUEST_MAP_INFO = new RedisKey("notify.RequestMapInfo");
    public static RedisKey NOTIFY_SAVE_PLAYER = new RedisKey("notify.SavePlayer.%s");
    public static RedisKey NOTIFY_SAVED_PLAYER = new RedisKey("notify.SavedPlayer");
    public static RedisKey NOTIFY_SEND_MESSAGE = new RedisKey("notify.SendMessage");
    public static RedisKey NOTIFY_SERVER_MERGE = new RedisKey("notify.ServerMerge.%s");
    public static RedisKey NOTIFY_SERVER_STATUS = new RedisKey("notify.ServerStatus");
    public static RedisKey NOTIFY_SERVER_SWITCH = new RedisKey("notify.ServerSwitch");
    public static RedisKey NOTIFY_SPINNERS_SERVER = new RedisKey("notify.SpinnerServer");
    public static RedisKey NOTIFY_UPDATE = new RedisKey("notify.Update");
    public static RedisKey PLAYER_DATA = new RedisKey("PlayerData.Info.%s");

    private String _message;

    public RedisKey(String message)
    {
        _message = message;
    }

    public RedisKey fromParams(String... strings)
    {
        RedisKey newRedis = new RedisKey(_message);

        if (strings.length != UtilString.count(_message, "%s"))
            throw new RuntimeException("Invalid number of arguements");

        for (String s : strings)
        {
            newRedis._message = newRedis._message.replaceFirst("%s", s);
        }

        return newRedis;
    }

    public String getKey()
    {
        if (_message.contains("%s"))
            throw new RuntimeException("Invalid redisnotify " + _message);

        return _message;
    }

    public boolean isPattern()
    {
        return false;
    }
}
