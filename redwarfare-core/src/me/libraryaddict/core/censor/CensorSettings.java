package me.libraryaddict.core.censor;

import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Pattern;

public class CensorSettings
{
    private static HashMap<Pattern, String> _censored = new HashMap<Pattern, String>();

    private static HashMap<String, String> _correctWords = new HashMap<String, String>();

    private static Vector<String> _ignoreCaps = new Vector();

    static
    {
        ignoreCaps("xd", "DX", ":D", ":p", ";P", ":s", ":o", ";o", "d:", "d;", "o.o", "0.o", "o.0", "=d", "d=", "=p", "p=", "=o",
                "gg");

        correctWords("im", "I'm", "i'm", "I'm", "i", "I", "c", "see", "f", "love", "k", "ok", "u", "you", "sry", "sorry", "ur",
                "your", "il", "I'll", "i'l", "I'll", "i'll", "I'll", "cant", "can't", "wasnt", "wasn't", "yh", "yeah", "dont",
                "don't", "every1", "everyone", "evry1", "everyone", "youre", "you're", "suk", "suck", "didnt", "didn't", "r",
                "are", "u", "you", "any1", "anyone", "guna", "gonna", "ass", "horse", "dafuq", "what!", "fuc", "love", "fck",
                "love", "fuked", "cellphone", "fuking", "loving", "fcking", "loving", "fuk", "love", "fack", "love", "cyka",
                "kisses", "blyat", "huggies", "bleach", "my love", "sex", "dinner", "gay", "happy", "kys", "be strong",
                "force kit", "molest me", "force", "I like");

        censor("gayness", "very happy", "faggot", "cool guy", "fag", "cool", "fag", "pro", "vagina", "sweets", "penis",
                "lollipop", "penis", "airplane", "cock", "lollipop", "cock", "monster car", "rectum", "kangaroo", "whore",
                "mummy", "fat", "cute", "cunt", "sweets", "fucking", "loving", "f*ck", "love", "fuck", "candy", "fuck",
                "rainbows", "fuck", "unicorns", "fuck", "darling", "rape", "candy", "dick", "icecream", "dick", "sugar", "slut",
                "my mum", "ugly", "handsome", "bitch", "sister", "bitch", "doggie", "bastard", "brother", "bastard", "son",
                "shit", "crap", "shit", "aw man", "damn", "aw man", "badass", "awesome", "asshole", "toilet", "horny", "stupid",
                "horny", "magical", "'a$$", "lips'", "'a$$", "cutie'", "'a$$", "doll'", "rape", "make love", "rape", "touch me",
                "nigger", "honorable black man", "nigger", "cutie", "nigga", "honorable black man", "pussy", "my cat", "snatch",
                "steal", "homo", "happy", "homo", "crazy", "wanker", "young man", "yolo", "punch me", "'y()l()", "punch me'",
                "swag", "money", "suck balls", "rules my world", "fukk", "eeeek", "fckk", "eeeek", "fack", "dear", "feck", "dear",
                "'lolo", "hue'", "punch me you", "touch me handsome", "titties", "chest", "dumbass", "smartypants", "dumb@$$",
                "smartypants", "dumb@$s", "smartypants", "dumb@ss", "smartypants", "dumba$$", "smartypants", "dumbas$",
                "smartypants", "dumb@s$", "smartypants", "  ", " ", "puss", "cat", "dumbass", "hot stuff", "-> me]",
                "I'm really hot", "bleach", "potion of my love", "kill yourself", "be strong");
    }

    private static void censor(String... strings)
    {
        for (int i = 0; i < strings.length; i += 2)
        {
            String pattern = "(?i)";

            char[] chars = strings[i].toCharArray();

            for (int pos = 0; pos < chars.length; pos++)
            {
                pattern += Pattern.quote("" + chars[pos]) + (pos + 1 == chars.length ? "+" : "+[^A-Za-z0-9]*");
            }

            _censored.put(Pattern.compile(pattern), strings[i + 1]);
        }
    }

    private static void correctWords(String... strings)
    {
        for (int i = 0; i < strings.length; i += 2)
            _correctWords.put(strings[i], strings[i + 1]);
    }

    public static HashMap<Pattern, String> getCensored()
    {
        return _censored;
    }

    public static HashMap<String, String> getCorrectWords()
    {
        return _correctWords;
    }

    public static Vector<String> getIgnoreCaps()
    {
        return _ignoreCaps;
    }

    private static void ignoreCaps(String... strings)
    {
        for (String string : strings)
            _ignoreCaps.add(string);
    }
}
