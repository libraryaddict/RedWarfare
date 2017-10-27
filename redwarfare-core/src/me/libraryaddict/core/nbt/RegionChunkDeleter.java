package me.libraryaddict.core.nbt;

import java.io.File;
import java.io.IOException;

public class RegionChunkDeleter extends RegionFileBase
{
    public RegionChunkDeleter(WorldCache storage, File regionFile)
    {
        super(storage, regionFile, "rw");
    }

    /* delete a chunk at (x,z) */
    protected synchronized void delete(int rx, int rz)
    {
        if (outOfBounds(rx, rz))
            return;

        try
        {
            int offset = getOffset(rx, rz);
            int sectorNumber = offset >> 8;
            int sectorsAllocated = offset & 0xFF;

            if (sectorNumber != 0)
            {
                for (int i = 0; i < sectorsAllocated; ++i)
                {
                    sectorFree.set(sectorNumber + i, true);
                }
            }

            setOffset(rx, rz, 0);
            setTimestamp(rx, rz, (int) (System.currentTimeMillis() / 1000L));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
