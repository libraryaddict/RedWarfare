package me.libraryaddict.build.types;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.Iterator;

public class BlockLooper extends BlockPos implements Comparable {
    private Block _block;
    private ArrayList<BlockLooper> _childLoopers = new ArrayList<BlockLooper>();
    private int _faceIndex;
    private BlockFace[] _faces;

    public BlockLooper(Block block, BlockFace[] faces) {
        super(block.getX(), block.getY(), block.getZ());

        _block = block;
        _faces = faces;
    }

    public void addLooper(BlockLooper looper) {
        _childLoopers.add(looper);
    }

    public boolean canPointAt(BlockLooper looper) {
        for (int i = _faceIndex; i < _faces.length; i++) {
            BlockFace face = _faces[i];

            if (getX() + face.getModX() != looper.getX())
                continue;

            if (getY() + face.getModY() != looper.getY())
                continue;

            if (getZ() + face.getModZ() != looper.getZ())
                continue;

            return true;
        }

        return false;
    }

    @Override
    public int compareTo(Object o) {
        BlockLooper looper = (BlockLooper) o;

        if (looper.getX() != getX())
            return Integer.compare(getX(), looper.getX());

        if (looper.getY() != getY())
            return Integer.compare(getY(), looper.getY());

        return Integer.compare(getZ(), looper.getZ());
    }

    public Block getNextBlock() {
        while (isValid()) {
            BlockFace face = _faces[_faceIndex++];

            int y = face.getModY() + getY();

            if (y < 0 || y > 255)
                continue;

            return _block.getRelative(face);
        }

        return null;
    }

    public BlockLooper getOldestChild() {
        Iterator<BlockLooper> itel = _childLoopers.iterator();
        BlockLooper oldest = null;

        while (itel.hasNext()) {
            BlockLooper looper = itel.next();

            if (!looper.canPointAt(this)) {
                itel.remove();
                continue;
            }

            oldest = looper;
        }

        return oldest;
    }

    public boolean isValid() {
        return _faceIndex < _faces.length;
    }

    @Override
    public String toString() {
        return "BlockLooper[" + getX() + "," + getY() + "," + getZ() + "]";
    }
}
