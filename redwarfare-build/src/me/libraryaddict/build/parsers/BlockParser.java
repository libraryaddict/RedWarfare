package me.libraryaddict.build.parsers;

import me.libraryaddict.build.customdata.BorderCustomData;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.utils.UtilTime;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.UUID;

public abstract class BlockParser extends BasicParser {
    private int _blockStage;
    private ArrayList<Pair<Integer, Integer>> _chunks;
    private long _lastAnnounced = System.currentTimeMillis();
    private Material[] _lookFor;
    private int _orig;

    public BlockParser(WorldInfo worldInfo, UUID publisher, Material... toLookFor) {
        super(worldInfo, publisher);

        _lookFor = toLookFor;

        BorderCustomData data = (BorderCustomData) getInfo().getCustomData();

        _chunks = data.getChunksToScan();
        _orig = _chunks.size();
    }

    public boolean isFinished() {
        return super.isFinished() && _chunks.isEmpty();
    }

    public abstract void onFind(Material mat, String block);

    @Override
    public void tick() {
        if (_chunks.isEmpty()) {
            super.tick();
            return;
        }

        if (_blockStage == 0) {
            _blockStage++;
            getInfo().Announce(C.Gold + "Now scanning the map for certain blocks..");
        }

        Pair<Integer, Integer> loc = _chunks.remove(0);

        ChunkSnapshot chunk = getWorld().getChunkAt(loc.getKey(), loc.getValue()).getChunkSnapshot(false, false, false);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    int type = chunk.getBlockTypeId(x, y, z);

                    for (Material mat : _lookFor) {
                        if (mat.getId() != type)
                            continue;

                        onFind(mat, ((loc.getKey() * 16) + x) + "," + y + "," + ((loc.getValue() * 16) + z));
                    }
                }
            }
        }

        int current = _chunks.size();

        if (current != 0 && UtilTime.elasped(_lastAnnounced, 2000)) {
            _lastAnnounced = System.currentTimeMillis();

            getInfo().Announce(C.Gold + (int) ((current / (double) _orig) * 100) + "%");
        }

        if (current == 0)
            getInfo().Announce(C.Gold + "Finished scanning the map for blocks");
    }
}
