package me.libraryaddict.build.types;

import org.bukkit.block.Block;

public class BlockPos {
    private int _x, _y, _z;

    public BlockPos(Block block) {
        this(block.getX(), block.getY(), block.getZ());
    }

    public BlockPos(int x, int y, int z) {
        _x = x;
        _y = y;
        _z = z;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && obj.hashCode() == hashCode();
    }

    public int getX() {
        return _x;
    }

    public int getY() {
        return _y;
    }

    public int getZ() {
        return _z;
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    @Override
    public String toString() {
        return "BlockPos[" + getX() + "," + getY() + "," + getZ() + "]";
    }
}