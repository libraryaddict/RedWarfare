package me.libraryaddict.core.chat;

import java.util.ArrayList;

import me.libraryaddict.core.utils.UtilTime;

public class Message
{
    private long _lastChatted;
    private ArrayList<String> _messages = new ArrayList<String>();

    public void addMessage(String message)
    {
        _messages.add(0, message);

        if (_messages.size() > 3)
            _messages.remove(_messages.get(3));
    }

    private boolean equal(String one, String two)
    {
        if (two.length() == 1 && one.length() == 1)
        {
            return true;
        }
        char[] c1 = one.toLowerCase().toCharArray();
        char[] c2 = two.toLowerCase().toCharArray();
        int i = 0;
        for (; i < Math.min(one.length(), two.length()); i++)
        {
            if (c1[i] != c2[i])
            {
                break;
            }
        }
        return (i >= 8 || i >= Math.min(one.length(), two.length()));
    }

    public boolean isAlmostSpamming(String message)
    {
        if (_messages.size() < 2)
            return false;

        if (equal(message, _messages.get(0)) && equal(message, _messages.get(1)) && !UtilTime.elasped(_lastChatted, 20000))
            return true;

        return false;
    }

    public boolean isFast()
    {
        boolean fast = !UtilTime.elasped(_lastChatted, 100);
        _lastChatted = System.currentTimeMillis();

        return fast;
    }

    public boolean isSpamming(String message)
    {
        return isAlmostSpamming(message) && _messages.size() >= 3 && equal(_messages.get(2), message);
    }
}