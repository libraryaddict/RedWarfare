package me.libraryaddict.core.server.commands;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.eclipse.egit.github.core.Gist;
import org.eclipse.egit.github.core.GistFile;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.GistService;

import me.libraryaddict.core.C;
import me.libraryaddict.core.command.SimpleCommand;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.core.ranks.PlayerRank;
import me.libraryaddict.core.utils.UtilPlayer;

public class CommandPaste extends SimpleCommand
{
    private JavaPlugin _plugin;

    public CommandPaste(JavaPlugin plugin)
    {
        super(new String[]
            {
                    "paste", "pastebin", "getlog", "serverlog"
            }, Rank.OWNER);

        _plugin = plugin;
    }

    @Override
    public void onTab(Player player, PlayerRank rank, String[] args, String token, Collection<String> completions)
    {
    }

    @Override
    public void runCommand(Player player, PlayerRank rank, String alias, String[] args)
    {
        new BukkitRunnable()
        {
            public void run()
            {
                try
                {
                    GitHubClient client = new GitHubClient();
                    Gist gist = new Gist().setPublic(false).setDescription("Private serverlog");

                    byte[] encoded = Files.readAllBytes(new File("logs/latest.log").toPath());

                    GistFile file = new GistFile().setContent(new String(encoded, Charset.defaultCharset()));

                    gist.setFiles(Collections.singletonMap("latest.log", file));

                    gist = new GistService(client).createGist(gist);

                    UtilPlayer.sendMessage(player, C.Blue + gist.getHtmlUrl());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(_plugin);
    }
}
