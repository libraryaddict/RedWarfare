package me.libraryaddict.core.inventory.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class ItemLayout
{
    private char _item = 'O';
    private ArrayList<Integer> _itemSlots = new ArrayList<Integer>();
    private String[] _lines;

    public ItemLayout(String... lines)
    {
        for (int i = 0; i < lines.length; i++)
        {
            lines[i] = lines[i].replaceAll(" ", "");
        }

        _lines = lines;

        parseLines();
    }

    public <Y> ArrayList<Y> format(Collection<Y> items)
    {
        return new ArrayList(Arrays.asList(parseItems(items.toArray(new Object[0]))));
    }

    public ArrayList<Integer> getSlots()
    {
        return _itemSlots;
    }

    public <Y> Y[] parseItems(Y... items)
    {
        Object[] newItems = new Object[_lines.length * 9];

        Iterator<Integer> itel = _itemSlots.iterator();
        int i = 0;

        while (itel.hasNext() && i < items.length)
        {
            newItems[itel.next()] = items[i++];
        }

        return (Y[]) newItems;
    }

    private void parseLines()
    {
        _itemSlots.clear();

        int slot = 0;

        for (String line : _lines)
        {
            if (line.length() != 9)
            {
                throw new IllegalArgumentException("Error while parsing ItemLayout, '" + line + "' is not 9 characters long");
            }

            for (char c : line.toCharArray())
            {
                if (c == _item)
                {
                    _itemSlots.add(slot);
                }

                slot++;
            }
        }
    }

}
