package me.libraryaddict.arcade.game;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.events.TeamDeathEvent;
import me.libraryaddict.arcade.managers.ArcadeManager;
import me.libraryaddict.core.ServerType;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.preference.Preference;
import me.libraryaddict.core.preference.PreferenceItem;
import me.libraryaddict.core.rank.Rank;
import me.libraryaddict.network.Pref;

public abstract class TeamGame extends Game
{
    private Pref<Boolean> _toggleBlackHats;
    private Pref<Boolean> _toggleHats;

    public TeamGame(ArcadeManager arcadeManager, ServerType gameType)
    {
        super(arcadeManager, gameType);

        setOption(GameOption.ATTACK_TEAM, false);
        setOption(GameOption.TEAM_HOTBAR, true);
        setOption(GameOption.COLOR_CHAT_NAMES, true);

        _toggleHats = new Pref<Boolean>(getName() + ".Hats", true);
        _toggleBlackHats = new Pref<Boolean>(getName() + ".BlackHats", false);


        PreferenceItem teamChat = new PreferenceItem("Automatically talk in team chat!", getTeamChat(),
                new ItemBuilder(Material.STAINED_CLAY, 1, (short) 11)
                        .addLore(
                                "You will automatically talk in team chat during the game, use @message or /t message to talk in public!")
                        .build());

        registerPref(teamChat);
        
        PreferenceItem teamHats = new PreferenceItem("View Hats in " + getName(), getTeamHats(),
                new ItemBuilder(Material.DIAMOND_ORE).addLore("Each player wears a hat signifying the team they are aligned to!")
                        .build(),
                getBlackHats());

        registerPref(teamHats);

        PreferenceItem blackHats = new PreferenceItem("Display black and white helmets!", getBlackHats(),
                new ItemBuilder(Material.LEATHER_HELMET).setColor(Color.BLACK)
                        .addLore("All your allies wear white hats, all your enemies wear black!").build(),
                Rank.VIP, getTeamHats());

        registerPref(blackHats);
    }

    @Override
    public void checkGameState()
    {
        int alive = 0;

        for (GameTeam team : getTeams(true))
        {
            if (team.getPlayers().isEmpty() || (getOption(GameOption.DEATH_OUT) && team.getPlayers(true).isEmpty()))
            {
                TeamDeathEvent event = new TeamDeathEvent(team);

                Bukkit.getPluginManager().callEvent(event);

                if (!event.isCancelled())
                {
                    team.setDead(true);
                    continue;
                }
            }

            alive++;
        }

        if (alive == 1)
        {
            getManager().getWin().setWin(getTeams(true).get(0));
            return;
        }
    }

    public Pref<Boolean> getBlackHats()
    {
        return _toggleBlackHats;
    }

    /**
     * Return black hat, white hat, colored gear, etc.
     */
    public ItemStack getCosmeticGearItem(Player pervert, GameTeam victimTeam, EquipmentSlot slot)
    {
        if (slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND)
            return null;

        if (Preference.getPreference(pervert, getTeamHats()))
        {
            if (slot != EquipmentSlot.HEAD)
                return null;

            if (victimTeam == null)
                return null;

            return victimTeam.getHat();
        }

        if (Preference.getPreference(pervert, getBlackHats()))
        {
            if (slot != EquipmentSlot.HEAD)
                return null;

            GameTeam pervertTeam = getTeam(pervert);

            if (pervertTeam == null || victimTeam == null)
                return null;

            return new ItemBuilder(Material.WOOL, 1, (short) (pervertTeam == victimTeam ? 0 : 15)).build();
        }

        return null;
    }

    public Pref<Boolean> getTeamHats()
    {
        return _toggleHats;
    }

    @EventHandler
    public void onDeathPrefix(DeathEvent event)
    {
        GameTeam team1 = getTeam(event.getPlayer());

        if (team1 != null)
        {
            event.setKilledPrefix(team1.getColoring());
        }

        if (event.getLastAttacker() == null)
            return;

        GameTeam team2 = getTeam(event.getLastAttacker());

        if (team2 != null)
        {
            event.setKillerPrefix(team2.getColoring());
        }
    }

    @EventHandler
    public void onHats(EquipmentEvent event)
    {
        ItemStack item = getCosmeticGearItem(event.getViewer(), getTeam(event.getWearer()), event.getSlot());

        if (item == null)
            return;

        event.setHat(item);
    }
}
