package me.libraryaddict.core.censor;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.java.JavaPlugin;

import me.libraryaddict.core.chat.ChatEvent;
import me.libraryaddict.core.plugin.MiniPlugin;

public class CensorManager extends MiniPlugin
{
    public CensorManager(JavaPlugin plugin)
    {
        super(plugin, "Censor Manager");
    }

    private String censor(String toCensor)
    {
        for (Entry<Pattern, String> entry : CensorSettings.getCensored().entrySet())
        {
            toCensor = entry.getKey().matcher(toCensor).replaceAll(entry.getValue());
        }

        return toCensor;
    }

    public String[] censorMessage(String censoredMessage)
    {
        char a = censoredMessage.length() > 0 ? censoredMessage.charAt(0) : 'a';

        if (!Character.isLetterOrDigit(a))
            censoredMessage = censoredMessage.substring(1);

        String hisMessage = censoredMessage;
        censoredMessage = censor(censoredMessage);

        for (Entry<String, String> st : CensorSettings.getCorrectWords().entrySet())
        {
            censoredMessage = getWord(censoredMessage, st.getKey(), st.getValue());
            hisMessage = getWord(hisMessage, st.getKey(), st.getValue());
        }

        hisMessage = removeRepetitions(hisMessage);
        censoredMessage = removeRepetitions(censoredMessage);

      /*  if (((double) UtilString.countCapitalization(censoredMessage) / censoredMessage.length()) * 100 >= 40
                && censoredMessage.length() > 6)
            censoredMessage = correctCaps(censoredMessage);

        if (((double) UtilString.countCapitalization(hisMessage) / hisMessage.length()) * 100 >= 3 || hisMessage.length() > 3)
            hisMessage = correctCaps(hisMessage);*/

        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (!p.isOnline())
                continue;

            String name = p.getName();

            hisMessage = hisMessage.replaceAll("\\w*" + name, name);
            censoredMessage = censoredMessage.replaceAll("\\w*" + name, name);
        }

        if (!Character.isLetterOrDigit(a))
        {
            censoredMessage = a + censoredMessage;
            hisMessage = a + hisMessage;
        }

        return new String[]
            {
                    censoredMessage, hisMessage
            };
    }

    public String correctCaps(String stringToCorrect)
    {
        char[] chars = stringToCorrect.toCharArray();
        boolean nextLetterIsCaps = false;

        if (chars.length > 0)
            nextLetterIsCaps = Character.isLetter(chars[0]);

        int lettersToSkip = 0;

        for (int posInLine = 0; posInLine < chars.length; posInLine++)
        {
            boolean skipDecaps = false;

            if (lettersToSkip > 0)
            {
                lettersToSkip--;
                skipDecaps = true;
            }

            for (String ex : CensorSettings.getIgnoreCaps())
            {
                if (stringToCorrect.length() >= posInLine + ex.length())
                {
                    if (stringToCorrect.substring(posInLine, posInLine + ex.length()).equalsIgnoreCase(ex))
                    {
                        if (ex.length() > lettersToSkip)
                            lettersToSkip = ex.length();

                        skipDecaps = true;
                        nextLetterIsCaps = false;
                    }
                }
            }

            if (skipDecaps)
                continue;

            if (nextLetterIsCaps && Character.isLetter(chars[posInLine]))
            {
                nextLetterIsCaps = false;
                chars[posInLine] = Character.toUpperCase(chars[posInLine]);
                // If the next letter is this. And the letter after isnt a
                // letter.
            }
            else if ((chars[posInLine] == '.' /*|| chars[posInLine] == ','*/ || chars[posInLine] == '?' || chars[posInLine] == '!'
                    || chars[posInLine] == '"') && (posInLine + 1 < chars.length && !Character.isLetter(chars[posInLine + 1])))
            {
                nextLetterIsCaps = true;
            }
            else if (Character.isLetter(chars[posInLine]) && Character.isUpperCase(chars[posInLine]))
            {
                chars[posInLine] = Character.toLowerCase(chars[posInLine]);
            }
        }

        stringToCorrect = new String(chars);

        return stringToCorrect;
    }

    public String getWord(String sentance, String string, String replace)
    {
        ArrayList<Character> chars = new ArrayList<Character>();

        int foundMatches = 0;
        char[] cArray = sentance.toCharArray();
        sentance = "";

        for (int p = 0; p < cArray.length; p++)
        {
            char currentChar = cArray[p];
            chars.add(currentChar);
            // If it hasn't found something. Is the previous character a valid
            // char? Else true
            // Also, if the current char of 'string' matches the current char in
            // cArray
            if ((foundMatches == 0 ? isSpecialChar(cArray, p - 1) : true)
                    && Character.toLowerCase(string.toCharArray()[foundMatches]) == Character.toLowerCase(currentChar))
            {
                // It found one.
                foundMatches++;
                // Theres enough matches that the word must be it!
                if (foundMatches == string.length())
                {
                    // If the next char after this isnt a valid ending.
                    if (!isSpecialChar(cArray, p + 1))
                    {
                        foundMatches = 0;
                    }
                    else
                    {
                        // Replace those chars and add the replacement!
                        while (foundMatches > 0)
                        {
                            foundMatches--;
                            chars.remove(chars.size() - 1);
                        }
                        for (char c : replace.toCharArray())
                        {
                            chars.add(c);
                        }
                    }
                }
            }
            else
                foundMatches = 0;
        }

        for (Character c : chars)
            sentance += c;

        return sentance;
    }

    public boolean isDirty(String string)
    {
        if (!censor(string).equalsIgnoreCase(string))
            return true;

        for (Entry<String, String> st : CensorSettings.getCorrectWords().entrySet())
        {
            if (!string.equalsIgnoreCase(getWord(string, st.getKey(), st.getValue())))
                return true;
        }

        return false;
    }

    /**
     * Checks the letter at its current position if it is a non-letter
     */
    private boolean isSpecialChar(char[] cArray, int pos)
    {
        // If pos is outside the char[] range. Return true;
        if (pos < 0 || pos > cArray.length - 1 || cArray[pos] == ' ' || cArray[pos] == '.' || cArray[pos] == ','
                || cArray[pos] == '!' || cArray[pos] == '?')
            return true;

        return false;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(ChatEvent event)
    {
        String[] censored = censorMessage(event.getMessage()[0]);

        event.setMessage(censored);
    }

    public String removeRepetitions(String string)
    {
        String toReturn = "";
        int found = 0;
        char[] chars = string.toCharArray();

        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];

            if (Character.isDigit(c) || !(i != 0 && c == chars[i - 1]))
                found = 0;
            else
                found++;

            if (found >= 3 && i != 0 && c == chars[i - 1])
                continue;

            toReturn += c;
        }

        return toReturn;
    }
}
