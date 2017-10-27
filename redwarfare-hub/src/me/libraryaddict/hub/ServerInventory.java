package me.libraryaddict.hub;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.inventory.PageInventory;
import me.libraryaddict.core.inventory.utils.IButton;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilNumber;
import me.libraryaddict.hub.types.Server;
import me.libraryaddict.redis.operations.RedisSwitchServer;

public class ServerInventory extends PageInventory
{
    private ArrayList<Server> _servers;

    public ServerInventory(Player player, ArrayList<Server> servers)
    {
        super(player, C.Gold + "Server Navigator");

        _servers = servers;

        buildPages();
    }

    private void buildPages()
    {
        ArrayList<Pair<ItemStack, IButton>> items = new ArrayList<Pair<ItemStack, IButton>>();

        for (int i = 0; i < _servers.size(); i++)
        {
            Server server = _servers.get(i);

            if (i == 0 && _servers.size() > 1 && server.getPlayers() == 0)
            {
                Server serv = _servers.get(i + 1);

                if (!serv.isInProgress() && !serv.isFull())
                {
                    continue;
                }
            }

            ItemStack item = createItem(server);

            items.add(Pair.of(item, new IButton()
            {
                @Override
                public boolean onClick(ClickType clickType)
                {
                    new RedisSwitchServer(getPlayer().getUniqueId(), server.getName());

                    return true;
                }
            }));
        }

        setPages(items);
    }

    private ItemStack createItem(Server server)
    {
        ItemBuilder builder = new ItemBuilder(Material.STAINED_CLAY);

        ServerType type = server.getType();

        builder.setTitle(C.Gold + type.getName());
        builder.addLore(C.Gray + "Players: " + server.getPlayers() + "/" + type.getMaxPlayers());

        if (server.isInProgress())
        {
            builder.setData((short) 14);
            builder.addLore(C.Red + "In progress for "
                    + UtilNumber.getTime(-(server.getGameStarts() - System.currentTimeMillis()), TimeUnit.MILLISECONDS));
        }
        else if (server.isFull())
        {
            builder.setData((short) 1);
            builder.addLore(C.DGreen + "Game starts in "
                    + UtilNumber.getTime(server.getGameStarts() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
        }
        else
        {
            builder.setData((short) 5);
            builder.addLore(C.DGreen + "Game starts in "
                    + UtilNumber.getTime(server.getGameStarts() - System.currentTimeMillis(), TimeUnit.MILLISECONDS));
        }

        return builder.build();
    }

    @EventHandler
    public void onHalfSecond(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
        {
            return;
        }

        buildPages();
    }
}
