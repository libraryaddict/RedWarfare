package me.libraryaddict.core.nbt;

import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WorldCache
{
    protected static final int MAX_CHUNK_CACHE_SIZE = 256;
    protected static final int MAX_REGION_CACHE_SIZE = 256;
    protected Map<ChunkHandle, Reference<AnvilChunkData>> chunkCache = new HashMap<ChunkHandle, Reference<AnvilChunkData>>();
    protected boolean forDelete = false;
    protected Map<File, Reference<RegionFileBase>> regionCache = new HashMap<File, Reference<RegionFileBase>>();
    private ArrayList<File> toDelete = new ArrayList<File>();

    public synchronized void clearRegionCache()
    {
        for (Reference<RegionFileBase> ref : regionCache.values())
        {
            try
            {
                if (ref.get() != null)
                {
                    ref.get().close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        regionCache.clear();

        for (File file : toDelete)
        {
            file.delete();
        }

        toDelete.clear();
    }

    public synchronized void deleteChunk(ChunkHandle chunk)
    {
        if (!this.forDelete)
        {
            this.clearRegionCache();
            this.chunkCache.clear();
            this.forDelete = true;
        }

        RegionChunkDeleter deleter = null;

        Reference<RegionFileBase> ref = regionCache.get(chunk.regionFile);

        if (ref != null && ref.get() != null)
        {
            deleter = (RegionChunkDeleter) ref.get();
        }
        else
        {
            if (regionCache.size() >= MAX_REGION_CACHE_SIZE)
            {
                this.clearRegionCache();
            }

            deleter = new RegionChunkDeleter(this, chunk.regionFile);
            regionCache.put(chunk.regionFile, new SoftReference<RegionFileBase>(deleter));
        }

        deleter.delete(chunk.rx, chunk.rz);
    }

    public void deleteFile(File fileName)
    {
        this.toDelete.add(fileName);
    }

    public synchronized AnvilChunkData getChunkData(ChunkHandle chunk)
    {
        if (this.forDelete)
        {
            this.clearRegionCache();
            this.chunkCache.clear();
            this.forDelete = false;
        }

        Reference<AnvilChunkData> refChunkData = this.chunkCache.get(chunk);

        if (refChunkData != null && refChunkData.get() != null)
        {
            return refChunkData.get();
        }

        if (this.chunkCache.size() >= MAX_CHUNK_CACHE_SIZE)
        {
            this.chunkCache.clear();
        }

        RegionFileForRead rf = getRegionFileForRead(chunk.regionFile);
        AnvilChunkData chunkData = new AnvilChunkData(rf.getChunkDataInputStream(chunk.rx, chunk.rz));
        this.chunkCache.put(chunk, new SoftReference<AnvilChunkData>(chunkData));

        return chunkData;
    }

    public synchronized RegionFileForRead getRegionFileForRead(File path)
    {
        if (this.forDelete)
        {
            this.clearRegionCache();

            this.chunkCache.clear();
            this.forDelete = false;
        }

        RegionFileForRead rf = null;
        Reference<RegionFileBase> refRegion = regionCache.get(path);

        if (refRegion != null && refRegion.get() != null)
        {
            return (RegionFileForRead) refRegion.get();
        }

        if (regionCache.size() >= MAX_REGION_CACHE_SIZE)
        {
            this.clearRegionCache();
        }

        rf = new RegionFileForRead(this, path);
        regionCache.put(path, new SoftReference<RegionFileBase>(rf));

        return rf;
    }
}
