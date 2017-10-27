package me.libraryaddict.core.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.bukkit.ChatColor;

import me.libraryaddict.core.C;

public class UtilText {
    public static ArrayList<String> splitLine(String string, LineFormat lineFormat) {
        ArrayList<String> strings = new ArrayList<String>();

        // Empty
        if (string.trim().isEmpty()) {
            strings.add(string);
            return strings;
        }

        String current = "";
        int currentLength = 0;
        String[] split = string.split(" ");
        String colors = "";

        for (int i = 0; i < split.length; i++) {
            String word = split[i];
            int wordLength = (colors + word).length();

            if (currentLength + wordLength + 4 > lineFormat.getLength() && !current.isEmpty()) {
                strings.add(current);
                current = colors + word;
                currentLength = wordLength + 1;
                continue;
            }

            if (i != 0) {
                current += " ";
                currentLength += 4;
            }

            current += word;
            currentLength += wordLength;
            colors = ChatColor.getLastColors(current);
        }

        if (!current.isEmpty()) {
            strings.add(current);
        }

        return strings;
    }

    public static ArrayList<String> splitLines(String[] strings, LineFormat lineFormat) {
        ArrayList<String> lines = new ArrayList<String>();

        for (String s : strings) {
            lines.addAll(splitLine(s, lineFormat));
        }

        return lines;
    }

    public static String[] splitLinesToArray(String[] strings, LineFormat lineFormat) {
        ArrayList<String> lineList = splitLines(strings, lineFormat);

        String[] lineArray = new String[lineList.size()];
        lineArray = lineList.toArray(lineArray);

        return lineArray;
    }

    public static String[] splitLineToArray(String string, LineFormat lineFormat) {
        ArrayList<String> lineList = splitLine(string, lineFormat);

        String[] lineArray = new String[lineList.size()];
        lineArray = lineList.toArray(lineArray);

        return lineArray;
    }

    public static String substringPixels(String string, int cutoff) {
        int len = 0;

        char[] array = string.toCharArray();
        boolean bold = false;

        for (int i = 0; i < array.length; i++) {
            char c = array[i];

            if (c == ChatColor.COLOR_CHAR) {
                if (++i < array.length) {
                    ChatColor color = ChatColor.getByChar(array[i]);

                    if (color != null) {
                        if (color.equals(ChatColor.BOLD)) {
                            bold = true;
                        } else if (color.equals(ChatColor.RESET) || color.isColor()) {
                            bold = false;
                        }
                    }
                }

                continue;
            }

            int toAdd = 1;

            if (bold) {
                toAdd++;
            }

            if (len + toAdd > cutoff) {
                return string.substring(0, Math.max(0, i - 1));
            }

            if (i + 1 < array.length) {
                len++;
            }
        }

        return string;
    }
}