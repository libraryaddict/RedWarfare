package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;

import com.comphenix.protocol.wrappers.WrappedGameProfile;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.EquipmentEvent;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.TeamGame;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.combat.CombatEvent;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilTime;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise;
import me.libraryaddict.disguise.disguisetypes.TargetedDisguise.TargetType;
import me.libraryaddict.disguise.utilities.ReflectionManager;

public class SkinnerAbility extends Ability
{
    public class Skin
    {
        private long _created = System.currentTimeMillis();
        private ItemStack[] items = new ItemStack[4];
        private WrappedGameProfile profile;
        private GameTeam team;

        public Skin(Player skinned)
        {
            team = getGame().getTeam(skinned);
            profile = ReflectionManager.getGameProfileWithThisSkin(UUID.randomUUID(), skinned.getName(),
                    ReflectionManager.getGameProfile(skinned));

            items = skinned.getInventory().getArmorContents();

            Kit kit = getGame().getKit(skinned);

            if (kit != null && !kit.canTeleportTo())
            {
                items = getGame().getKit("Trooper").getArmor();
            }
        }

        public ItemStack[] getItems()
        {
            return items;
        }

        public WrappedGameProfile getSkin()
        {
            return profile;
        }

        public GameTeam getTeam()
        {
            return team;
        }

        public boolean isReady()
        {
            return UtilTime.elasped(_created, 3000);
        }
    }

    private ArrayList<Pair<Player, Skin>> _skinning = new ArrayList<Pair<Player, Skin>>();
    private HashMap<Player, Skin> disguises = new HashMap<Player, Skin>();

    // @EventHandler
    public void onDeath(DeathEvent event)
    {
        if (!isLive())
            return;

        Player killed = event.getPlayer();

        {
            Skin skin = disguises.remove(killed);

            if (skin != null)
            {
                DisguiseAPI.undisguiseToAll(killed);
            }
        }

        if (!(event.getLastAttacker() instanceof Player))
            return;

        HashMap<Player, Double> kills = event.getCombatLog().getResponsibility();

        for (CombatEvent e : event.getCombatLog().getEvents())
        {
            if (!(e.getEvent().getFinalDamager() instanceof Player))
                continue;

            Player player = (Player) e.getEvent().getFinalDamager();

            kills.put(player, 1D);
            break;
        }

        for (Entry<Player, Double> entry : kills.entrySet())
        {
            if (entry.getValue() < 0.5)
                continue;

            Player killer = entry.getKey();

            if (!hasAbility(killer) || !isAlive(killer))
                continue;

            killer.sendMessage(C.Blue + "You rip " + killed.getName() + "'s skin off their body and start getting dressed!");
            killed.sendMessage(C.DRed + "You scream as " + killer.getName() + " skins you!");

            Skin skin = new Skin(killed);

            _skinning.add(Pair.of(killer, skin));
        }
    }

    @EventHandler
    public void onNewDeath(DeathEvent event)
    {
        if (!isLive())
            return;

        Player killed = event.getPlayer();

        {
            Skin skin = disguises.remove(killed);

            if (skin != null)
            {
                DisguiseAPI.undisguiseToAll(killed);
            }
        }

        for (Player player : getPlayers(true))
        {
            if (getGame().sameTeam(player, killed))
                continue;

            double dist = UtilLoc.getDistance(player, killed);

            if (dist > 7)
                continue;

            player.sendMessage(C.Blue + "You rip " + killed.getName() + "'s skin off their body and start getting dressed!");
            killed.sendMessage(C.DRed + "You scream as " + player.getName() + " skins you!");

            Skin skin = new Skin(killed);

            _skinning.add(Pair.of(player, skin));
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEnquipReceive(EquipmentEvent event)
    {
        if (!hasAbility(event.getWearer()))
            return;

        if (getGame().sameTeam(event.getWearer(), event.getViewer()))
            return;

        if (event.getSlot() == EquipmentSlot.HAND || event.getSlot() == EquipmentSlot.OFF_HAND)
            return;

        Skin disguise = disguises.get(event.getWearer());

        if (disguise == null)
            return;

        ItemStack item = ((TeamGame) getGame()).getCosmeticGearItem(event.getViewer(), disguise.getTeam(), event.getSlot());

        if (item != null)
        {
            event.setHat(item);
        }
        else
        {
            event.setHat(disguise.getItems()[event.getSlot().ordinal() - 2]);
        }
    }

    @EventHandler
    public void onHealthUpdate(TimeEvent event)
    {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (GameTeam team : getGame().getTeams())
        {
            FakeScoreboard board = getManager().getScoreboard().getScoreboard(team.getName() + "Medic");

            for (Player player : getPlayers(true))
            {
                Skin skin = disguises.get(player);

                if (skin == null)
                    continue;

                board.makeScore(DisplaySlot.BELOW_NAME, skin.getSkin().getName(),
                        (int) Math.ceil((player.getHealth() / player.getMaxHealth()) * 100));
            }
        }
    }

    @Override
    public void unregisterAbility()
    {
        for (Player player : getPlayers(true))
        {
            DisguiseAPI.undisguiseToAll(player);
        }
    }

    @EventHandler
    public void onSkinTick(TimeEvent event)
    {
        if (event.getType() != TimeType.HALF_SEC)
            return;

        if (!isLive())
            return;

        Iterator<Pair<Player, Skin>> itel = _skinning.iterator();

        while (itel.hasNext())
        {
            Pair<Player, Skin> pair = itel.next();

            Player player = pair.getKey();
            Skin skin = pair.getValue();

            if (!isAlive(player))
            {
                itel.remove();
                continue;
            }

            Location l = player.getLocation();

            l.getWorld().playEffect(l, Effect.STEP_SOUND, Material.REDSTONE_WIRE, 16);
            l.getWorld().playEffect(l.add(0, 1, 0), Effect.STEP_SOUND, Material.REDSTONE_WIRE, 16);

            if (!pair.getValue().isReady())
                continue;

            itel.remove();

            disguises.put(player, skin);

            PlayerDisguise disguise = new PlayerDisguise(skin.getSkin()).setEntity(player)
                    .setDisguiseTarget(TargetType.SHOW_TO_EVERYONE_BUT_THESE_PLAYERS);

            for (Player p : getGame().getTeam(player).getPlayers())
            {
                disguise.addPlayer(p);
            }

            disguise.startDisguise();

            player.sendMessage(C.Blue + "You finish getting dressed!");
        }
    }
}
