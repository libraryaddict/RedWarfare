package me.libraryaddict.core;

import java.util.regex.Pattern;

public class C
{
    public static String Aqua = "§b";
    public static String Black = "§0";
    public static String Blue = "§9";
    public static String Bold = "§l";
    public static String ColorChar = "§";
    public static String DAqua = "§3";
    public static String DBlue = "§1";
    public static String DGray = "§8";
    public static String DGreen = "§2";
    public static String DPurple = "§5";
    public static String DRed = "§4";
    public static String Gold = "§6";
    public static String Gray = "§7";
    public static String Green = "§a";
    public static String Italic = "§o";
    public static String Magic = "§k";
    public static String Purple = "§d";
    public static String Red = "§c";
    public static String Reset = "§r";
    public static String Strike = "§m";
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('§') + "[0-9A-FK-OR]");
    public static String Underline = "§n";
    public static String White = "§f";
    public static String Yellow = "§e";

    public static String stripColor(String string)
    {
        if (string == null)
        {
            return null;
        }

        return STRIP_COLOR_PATTERN.matcher(string).replaceAll("");
    }
}
