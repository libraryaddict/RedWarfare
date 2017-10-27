package me.libraryaddict.core.chat;

import java.util.UUID;

public class ChatMessage
{
    private String _censored;
    private String _channel;
    private String _message;
    private String _originServer;
    private UUID _sender;

    public ChatMessage(String server, String channel, UUID sender, String message, String censored)
    {
        _originServer = server;
        _sender = sender;
        _channel = channel;
        _message = message;
        _censored = censored;
    }
    
    public String getCensored()
    {
        return _censored;
    }
    
    public String getChannel()
    {
        return _channel;
    }

    public String getMessage()
    {
        return _message;
    }

    public UUID getPlayer()
    {
        return _sender;
    }

    public String getServer()
    {
        return _originServer;
    }

    public boolean hasPlayer()
    {
        return getPlayer() != null;
    }
}
