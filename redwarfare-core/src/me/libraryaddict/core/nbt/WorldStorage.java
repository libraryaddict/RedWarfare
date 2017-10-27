package me.libraryaddict.core.nbt;

import java.io.Closeable;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import me.libraryaddict.core.utils.UtilError;

public class WorldStorage implements Closeable
{
    protected static final int ANVIL_VERSION_ID = 0x4abd;
    protected static final int MCREGION_VERSION_ID = 0x4abc;

    private static ArrayList<File> getRegionFiles(File regionFolder, String extName)
    {
        File[] list = regionFolder.listFiles(new FilenameFilter()
        {

            @Override
            public boolean accept(File dir, String name)
            {
                return name.endsWith(extName);
            }
        });

        if (list != null)
        {
            ArrayList<File> regionFiles = new ArrayList<File>();

            for (File file : list)
            {
                regionFiles.add(file);
            }

            return regionFiles;
        }

        return null;
    }

    public static WorldStorage load(File regionFolder)
    {
        try
        {
            ArrayList<File> regionFiles = null;
            WorldCache worldCache = null;

            regionFiles = getRegionFiles(regionFolder, ".mca");
            worldCache = new WorldCache();

            return new WorldStorage(regionFiles, worldCache);
        }
        catch (Exception e)
        {
            UtilError.handle(e);
        }

        return null;
    }

    protected ArrayList<File> regionFiles;
    protected WorldCache worldCache;

    protected WorldStorage(ArrayList<File> regionFiles, WorldCache worldCache)
    {
        this.regionFiles = regionFiles;
        this.worldCache = worldCache;
    }

    @Override
    public void close()
    {
        this.worldCache.clearRegionCache();
    }

    @Override
    protected void finalize() throws Throwable
    {
        try
        {
            close();
        }
        catch (Exception e)
        {
        }
        finally
        {
            super.finalize();
        }
    }

    public ArrayList<ChunkHandle> getAllChunks()
    {
        ArrayList<ChunkHandle> chunks = new ArrayList<ChunkHandle>();

        for (int i = 0; i < this.regionFiles.size(); ++i)
        {
            File regionFile = this.regionFiles.get(i);
            RegionFileForRead r = this.worldCache.getRegionFileForRead(regionFile);

            for (int rx = 0; rx < 32; ++rx)
            {
                for (int rz = 0; rz < 32; ++rz)
                {
                    if (r.hasChunk(rx, rz))
                    {
                        chunks.add(new ChunkHandle(rx, rz, regionFile, this.worldCache));
                    }
                }
            }
        }

        return chunks;
    }
}
