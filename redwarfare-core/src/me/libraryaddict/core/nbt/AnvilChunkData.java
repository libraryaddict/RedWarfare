package me.libraryaddict.core.nbt;

import java.io.DataInputStream;
import java.util.ArrayList;

import me.libraryaddict.core.nbt.types.CompoundTag;
import me.libraryaddict.core.nbt.types.ListTag;
import me.libraryaddict.core.nbt.types.NbtIo;
import me.libraryaddict.core.nbt.types.Tag;
import me.libraryaddict.core.utils.UtilError;

public class AnvilChunkData
{
    protected static final int SECTION_SIZE = 16 * 16 * 16;

    protected byte[][] add;
    protected byte[][] blocks;
    protected int xPos;
    protected int zPos;

    public AnvilChunkData(DataInputStream chunkDataInputStream)
    {
        try
        {
            CompoundTag chunkTag = NbtIo.read(chunkDataInputStream);
            CompoundTag levelTag = chunkTag.getCompound("Level");

            ListTag<? extends Tag> sectionTags = levelTag.getList("Sections");
            ArrayList<byte[]> sectionBlocks = new ArrayList<byte[]>();
            ArrayList<byte[]> sectionAdds = new ArrayList<byte[]>();
            ArrayList<Integer> yBases = new ArrayList<Integer>();

            int maxYBase = -1;

            for (int i = 0; i < sectionTags.size(); ++i)
            {
                CompoundTag sectionTag = (CompoundTag) sectionTags.get(i);
                int yBase = sectionTag.getByte("Y") & 0xff;
                yBases.add(yBase);
                maxYBase = Math.max(maxYBase, yBase);
                sectionBlocks.add(sectionTag.getByteArray("Blocks"));
                sectionAdds.add(sectionTag.getByteArray("Add"));
            }

            blocks = new byte[maxYBase + 1][];
            add = new byte[maxYBase + 1][];

            for (int i = 0; i < yBases.size(); ++i)
            {
                blocks[yBases.get(i)] = sectionBlocks.get(i);
                add[yBases.get(i)] = sectionAdds.get(i);
            }

            xPos = levelTag.getInt("xPos");
            zPos = levelTag.getInt("zPos");
        }
        catch (Exception e)
        {
            UtilError.handle(e);
        }
    }

    public int getXPos()
    {
        return this.xPos;
    }

    public int getZPos()
    {
        return this.zPos;
    }
}
