package me.libraryaddict.core.nbt;

import java.io.File;

public class ChunkHandle
{
    protected int cx;
    protected int cz;
    protected boolean hasChunkPos;
    protected File regionFile;
    protected int rx;
    protected int rz;
    protected WorldCache worldCache;

    public ChunkHandle(int rx, int rz, File regionFile, WorldCache worldCache)
    {
        this.rx = rx;
        this.rz = rz;
        this.regionFile = regionFile;
        this.worldCache = worldCache;
        this.hasChunkPos = false;
    }

    public void delete()
    {
        worldCache.deleteChunk(this);
    }

    protected AnvilChunkData getChunkData()
    {
        AnvilChunkData chunkData = worldCache.getChunkData(this);
        initChunkPos(chunkData);

        return chunkData;
    }

    public int getChunkX()
    {
        initChunkPos();

        return this.cx;
    }

    public int getChunkZ()
    {
        initChunkPos();

        return this.cz;
    }

    protected void initChunkPos()
    {
        if (hasChunkPos)
            return;

        getChunkData();
    }

    protected void initChunkPos(AnvilChunkData chunkData)
    {
        if (hasChunkPos)
            return;

        cx = chunkData.getXPos();
        cz = chunkData.getZPos();
        hasChunkPos = true;
    }
}
