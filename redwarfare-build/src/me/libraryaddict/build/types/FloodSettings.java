package me.libraryaddict.build.types;

import me.libraryaddict.core.Pair;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.*;

public class FloodSettings {
    private int _blocksDone;
    private Pair<UUID, String> _creator;
    private BlockFace[] _floodDirections;
    private int _maxFloods;
    private Pair<Material, Byte>[] _replace;
    private Pair<Material, Byte> _replaceWith;
    private ArrayList<BlockLooper> _toProcess = new ArrayList<BlockLooper>();

    public FloodSettings(Player player, BlockFace[] floodDirections, int maxFloods, Pair<Material, Byte>[] replace,
            Pair<Material, Byte> replaceWith) {
        _maxFloods = maxFloods;
        _creator = Pair.of(player.getUniqueId(), player.getName());
        _replace = replace;
        _replaceWith = replaceWith;

        ArrayList<BlockFace> directions = new ArrayList<BlockFace>(Arrays.asList(floodDirections));

        Collections.sort(directions, new Comparator<BlockFace>() {
            @Override
            public int compare(BlockFace o1, BlockFace o2) {
                if (o1.getModX() != o2.getModX())
                    return Integer.compare(o1.getModX(), o2.getModX());

                if (o1.getModY() != o2.getModY())
                    return Integer.compare(o1.getModY(), o2.getModY());

                return Integer.compare(o1.getModZ(), o2.getModZ());
            }
        });
    }

    public int getBlocksDone() {
        return _blocksDone;
    }

    public int getBlocksRemaining() {
        return _toProcess.size();
    }

    public Pair<UUID, String> getCreator() {
        return _creator;
    }

    public int getMaxFlood() {
        return _maxFloods;
    }

    public boolean isFinished() {
        return _toProcess.isEmpty();
    }

    private boolean isReplacable(Block block) {
        Material type = block.getType();
        byte data = block.getData();

        for (Pair<Material, Byte> pair : _replace) {
            if (type != pair.getKey() || (pair.getValue() != -1 && pair.getValue() != data))
                continue;

            return true;
        }

        return false;
    }

    public void onTick() {
        for (int i = 0; i < 5 && !_toProcess.isEmpty(); i++) {
            BlockLooper looper = _toProcess.get(0);

            Block block = looper.getNextBlock();

            if (!looper.isValid()) {
                BlockLooper oldest = looper.getOldestChild();

                if (oldest == null) {
                    _toProcess.remove(0);
                } else {
                    _toProcess.remove(0);
                    _toProcess.add(_toProcess.indexOf(oldest), looper);
                }

                continue;
            }

            scanBlocks(block);

            if (!isReplacable(block))
                continue;

            _blocksDone++;

            block.setTypeIdAndData(_replaceWith.getKey().getId(), _replaceWith.getValue(), false);

            BlockLooper newLooper = new BlockLooper(block, _floodDirections);

            looper.addLooper(newLooper);

            _toProcess.add(newLooper);
        }
    }

    private void scanBlocks(Block block) {
        for (BlockFace face : _floodDirections) {
            BlockPos pos = new BlockPos(block.getX() + face.getModX(), block.getY() + face.getModY(),
                    block.getZ() + face.getModZ());

            if (pos.getY() < 0 || pos.getY() > 255)
                continue;

            if (_toProcess.contains(pos))
                continue;

            Block b = block.getRelative(face);

            if (!isReplacable(b))
                continue;

            BlockLooper looper = new BlockLooper(b, _floodDirections);

            _toProcess.add(looper);
        }
    }
}
