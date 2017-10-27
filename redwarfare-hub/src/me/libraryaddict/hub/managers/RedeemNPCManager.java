package me.libraryaddict.hub.managers;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import me.libraryaddict.core.C;
import me.libraryaddict.core.fakeentity.FakeEntity;
import me.libraryaddict.core.fakeentity.FakeEntityData;
import me.libraryaddict.core.map.WorldData;
import me.libraryaddict.core.plugin.MiniPlugin;
import me.libraryaddict.disguise.disguisetypes.MetaIndex;
import me.libraryaddict.hub.vote.VoteInteractListener;

public class RedeemNPCManager extends MiniPlugin
{
    private FakeEntity[] _voteNPC = new FakeEntity[0];

    public RedeemNPCManager(JavaPlugin plugin, HubManager hubManager)
    {
        super(plugin, "Vote Manager");

        new BukkitRunnable()
        {
            public void run()
            {
                spawnNPC(hubManager);
            }
        }.runTaskLater(getPlugin(), 2);
    }

    private void spawnNPC(HubManager hubManager)
    {
        WorldData worldData = new WorldData(Bukkit.getWorld("world"));

        ArrayList<Location> voteNPC = worldData.getCustomLocs("Redeem NPC");

        if (voteNPC == null)
            return;

        _voteNPC = new FakeEntity[voteNPC.size()];
        VoteInteractListener entityInteract = new VoteInteractListener(hubManager.getRedeem(), hubManager.getVote());

        int i = 0;

        for (Location loc : voteNPC)
        {
            System.out.println("Spawned at " + loc);
            FakeEntity fakeEntity = new FakeEntity(loc);

            FakeEntityData villager = new FakeEntityData(EntityType.BLAZE);

            FakeEntityData holo1 = new FakeEntityData(EntityType.ARMOR_STAND, new Vector(0, 2.3, 0));
            FakeEntityData holo2 = new FakeEntityData(EntityType.ARMOR_STAND, new Vector(0, 2, 0));

            holo1.setMetadata(MetaIndex.ENTITY_META, (byte) 32);
            holo1.setMetadata(MetaIndex.ARMORSTAND_META, (byte) 16);
            holo1.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME, C.Blue + C.Bold + "Your friendly token shop!");
            holo1.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE, true);

            holo2.setMetadata(MetaIndex.ENTITY_META, (byte) 32);
            holo2.setMetadata(MetaIndex.ARMORSTAND_META, (byte) 16);
            holo2.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME, C.Red + C.Bold + "Blazing hot deals to be found!");
            holo2.setMetadata(MetaIndex.ENTITY_CUSTOM_NAME_VISIBLE, true);

            fakeEntity.addFakeData(villager, holo1, holo2);
            fakeEntity.setInteract(entityInteract);
            fakeEntity.start();

            _voteNPC[i] = fakeEntity;
        }
    }

}
