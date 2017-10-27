package me.libraryaddict.core.player.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandHits extends SimpleCommand
{
    private ArrayList<UUID> _detect = new ArrayList<UUID>();

    public CommandHits(JavaPlugin plugin)
    {
        super("hitdetection", Rank.ALL);

        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, PacketType.Play.Client.USE_ENTITY)
        {
            @Override
            public void onPacketReceiving(PacketEvent event)
            {
                synchronized (_detect)
                {
                    if (!_detect.contains(event.getPlayer().getUniqueId()))
                        return;
                }

                if (event.getPacket().getEntityUseActions().read(0) != EntityUseAction.ATTACK)
                    return;

                int entityId = event.getPacket().getIntegers().read(0);

                new BukkitRunnable()
                {
                    public void run()
                    {
                        Player hit = UtilPlayer.getPlayer(entityId);

                        ItemStack item = hit.getInventory().getArmorContents()[2];

                        if (item == null || item.getType() == Material.AIR)
                            return;

                        UtilPlayer.sendMessage(event.getPlayer(), C.Green + "You've landed a hit");
                    }
                }.runTask(plugin);
            }
        });
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        synchronized (_detect)
        {
            if (!_detect.contains(player.getUniqueId()))
            {
                _detect.add(player.getUniqueId());
                player.sendMessage(C.Red + "You will now be told when you hit someone");
            }
            else
            {
                _detect.remove(player.getUniqueId());
                player.sendMessage(C.Red + "No longer telling you when you hit someone..");
            }
        }
    }

}
