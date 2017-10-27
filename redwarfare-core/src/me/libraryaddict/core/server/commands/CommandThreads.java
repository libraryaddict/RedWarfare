package me.libraryaddict.core.server.commands;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import org.bukkit.entity.Player;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.Collection;
import java.util.Collections;

public class CommandThreads extends SimpleCommand
{
    private static void dumpThread(ThreadInfo thread, StringBuilder string)
    {
        string.append("------------------------------\n");
        //
        string.append("Current Thread: " + thread.getThreadName() + "\n");
        string.append("\tPID: " + thread.getThreadId() + " | Suspended: " + thread.isSuspended() + " | Native: "
                + thread.isInNative() + " | State: " + thread.getThreadState() + "\n");
        if (thread.getLockedMonitors().length != 0)
        {
            string.append("\tThread is waiting on monitor(s):\n");
            for (MonitorInfo monitor : thread.getLockedMonitors())
            {
                string.append("\t\tLocked on:" + monitor.getLockedStackFrame() + "\n");
            }
        }
        string.append("\tStack:\n");
        //
        for (StackTraceElement stack : thread.getStackTrace())
        {
            string.append("\t\t" + stack + "\n");
        }
    }

    public CommandThreads()
    {
        super(new String[]
            {
                    "threads", "thread", "threaddump"
            }, Rank.OWNER);
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {

        try
        {
            GitHubClient client = new GitHubClient();
            Gist gist = new Gist().setPublic(false).setDescription("Private threads dump");

            StringBuilder string = new StringBuilder("");

            string.append("Server thread dump (Look for plugins here before reporting to Spigot!):\n");
            dumpThread(ManagementFactory.getThreadMXBean().getThreadInfo(
                    MinecraftServer.getServer().primaryThread.getId(),
                    Integer.MAX_VALUE), string);
            string.append("------------------------------\n");
            //
            string.append("Entire Thread Dump:\n");
            ThreadInfo[] threads = ManagementFactory.getThreadMXBean().dumpAllThreads(true, true);
            for (ThreadInfo thread : threads)
            {
                dumpThread(thread, string);
            }
            string.append("------------------------------\n");

            GistFile file = new GistFile().setContent(string.toString());

            gist.setFiles(Collections.singletonMap("threads", file));

            gist = new GistService(client).createGist(gist);

            UtilPlayer.sendMessage(player, C.Blue + gist.getHtmlUrl());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
