package me.libraryaddict.core.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.base.Predicate;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.database.MysqlLogCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilString;

public abstract class SimpleCommand
{
    private String[] _aliases;
    private Predicate<Player> _canUse;
    private JavaPlugin _plugin;
    private Rank[] _ranks;

    public SimpleCommand(String commandAlias, Rank... allowedRanks)
    {
        this(new String[]
            {
                    commandAlias
            }, allowedRanks);
    }

    public SimpleCommand(String[] commandAliases, Rank... allowedRanks)
    {
        _ranks = allowedRanks;
        _aliases = commandAliases;
    }

    public boolean canUse(Player player, PlayerRank rank)
    {
        if (_canUse != null && _canUse.apply(player))
            return true;

        for (Rank r : _ranks)
        {
            if (rank.hasRank(r))
                return true;
        }

        return false;
    }

    public String[] getAliases()
    {
        return _aliases;
    }

    public ArrayList<String> getAliasesStarting(String alias)
    {
        ArrayList<String> aliases = new ArrayList<String>();

        for (String s : _aliases)
        {
            if (!s.toLowerCase().startsWith(alias.toLowerCase()))
            {
                continue;
            }

            aliases.add(s);
        }

        return aliases;
    }

    public ArrayList<String> getPlayers(String token)
    {
        ArrayList<String> players = new ArrayList<String>();

        for (Player p : Bukkit.getOnlinePlayers())
        {
            if (p.getName().toLowerCase().startsWith(token.toLowerCase()))
            {
                players.add(p.getName());
            }
        }

        return players;
    }

    protected JavaPlugin getPlugin()
    {
        return _plugin;
    }

    public Predicate<Player> getPredicate()
    {
        return _canUse;
    }

    public boolean isAdminCommand()
    {
        for (Rank rank : _ranks)
        {
            switch (rank)
            {
            case MOD:
            case ADMIN:
            case OWNER:
                return true;
            default:
                continue;
            }
        }
        
        return false;
    }

    public boolean isAlias(String alias)
    {
        for (String s : _aliases)
        {
            if (s.equalsIgnoreCase(alias))
            {
                return true;
            }
        }

        return false;
    }

    protected void log(Player player, String args)
    {
        UUID uuid = player.getUniqueId();
        String command = getClass().getSimpleName();

        new BukkitRunnable()
        {
            public void run()
            {
                new MysqlLogCommand(uuid, command, args);
            }
        }.runTaskAsynchronously(getPlugin());
    }

    protected void log(Player player, String[] args)
    {
        log(player, UtilString.join(args, " "));
    }

    public abstract void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions);

    public abstract void runCommand(Player player, PlayerRank rank, String alias, String[] args);

    public void runConsoleCommand(CommandSender sender, String alias, String[] args)
    {
        sender.sendMessage(C.DRed + "Command not supported");
    }

    protected void setAliases(String... aliases)
    {
        _aliases = aliases;
    }

    protected void setPlugin(JavaPlugin plugin)
    {
        _plugin = plugin;
    }

    public void setRanks(Rank... ranks)
    {
        _ranks = ranks;
    }
}
