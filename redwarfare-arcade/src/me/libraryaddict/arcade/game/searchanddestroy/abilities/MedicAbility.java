package me.libraryaddict.arcade.game.searchanddestroy.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.injector.PacketConstructor;

import me.libraryaddict.arcade.events.DeathEvent;
import me.libraryaddict.arcade.events.GameStateEvent;
import me.libraryaddict.arcade.game.Game;
import me.libraryaddict.arcade.game.GameTeam;
import me.libraryaddict.arcade.game.searchanddestroy.SearchAndDestroy;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitMedic;
import me.libraryaddict.arcade.game.searchanddestroy.kits.KitSacrificial;
import me.libraryaddict.arcade.kits.Ability;
import me.libraryaddict.arcade.kits.Kit;
import me.libraryaddict.arcade.managers.GameState;
import me.libraryaddict.core.C;
import me.libraryaddict.core.condition.ConditionManager;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.scoreboard.FakeScoreboard;
import me.libraryaddict.core.scoreboard.ScoreboardManager;
import me.libraryaddict.core.stats.Stats;
import me.libraryaddict.core.time.TimeEvent;
import me.libraryaddict.core.time.TimeType;
import me.libraryaddict.core.utils.UtilEnt;
import me.libraryaddict.core.utils.UtilInv;
import me.libraryaddict.core.utils.UtilLoc;
import me.libraryaddict.core.utils.UtilParticle;
import me.libraryaddict.core.utils.UtilParticle.ParticleType;
import me.libraryaddict.core.utils.UtilPlayer;

public class MedicAbility extends Ability {
    private PacketConstructor _constructor = ProtocolLibrary.getProtocolManager()
            .createPacketConstructor(PacketType.Play.Server.UPDATE_HEALTH, 0f, 0, 0f);
    private HashMap<UUID, Double> _healsDone = new HashMap<UUID, Double>();
    private HashMap<UUID, Double> _playersHealed = new HashMap<UUID, Double>();
    private boolean _smallGame;

    public int getOtherMedics(Player player) {
        int amount = 0;

        for (Player p : UtilLoc.getInRadius(player.getLocation(), 8, Player.class)) {
            if (!getGame().sameTeam(p, player))
                continue;

            if (!hasAbility(p))
                continue;

            amount++;
        }

        return amount;
    }

    private void heal(Player player) {
        PacketContainer packet = _constructor.createPacket((float) player.getHealthScale(), player.getFoodLevel(),
                player.getSaturation());

        UtilPlayer.sendPacket(player, packet);
    }

    private void heal(Player player, Entity rightClicked, Location loc) {
        if (!isLive())
            return;

        if (!isAlive(player))
            return;

        if (!hasAbility(player))
            return;

        if (!UtilInv.isHolding(player, Material.BLAZE_ROD))
            return;

        if (getGame() instanceof SearchAndDestroy && ((SearchAndDestroy) getGame()).isEndGame())
            return;

        double healedDamage = 0;

        HashSet<Entity> list = new HashSet<Entity>(UtilLoc.getInRadius(loc, 5, LivingEntity.class));

        if (_smallGame) {
            return;
        } /*
          list.clear();
          
          if (rightClicked != null)
             list.add(rightClicked);
          }*/

        GameTeam team = getGame().getTeam(player);

        if (team == null)
            return;

        int nearby = this.getOtherMedics(player);

        for (Entity entity : list)

        {
            if (entity == player || !(entity instanceof LivingEntity)) {
                continue;
            }

            LivingEntity clicked = (LivingEntity) entity;

            if (!team.isInTeam(clicked))
                continue;

            if (clicked.getHealth() <= 0 || clicked.getHealth() >= clicked.getMaxHealth())
                continue;

            Kit kit = clicked instanceof Player ? getGame().getKit((Player) clicked) : null;

            if (kit instanceof KitMedic || kit instanceof KitSacrificial)
                continue;

            if (!Recharge.canUse(player, "Heal" + entity.getUniqueId()))
                continue;

            if (clicked.hasPotionEffect(PotionEffectType.POISON))
                continue;

            double heal = 7 - entity.getLocation().distance(player.getLocation());

            heal -= nearby;

            double healsLeft = 0;

            if (_healsDone.containsKey(clicked.getUniqueId())) {
                healsLeft = _healsDone.get(clicked.getUniqueId());
            }

            if (heal <= 0) {
                _healsDone.put(clicked.getUniqueId(), healsLeft + 0.2);

                if (healsLeft <= 0) {
                    clicked.sendMessage(C.Blue + "You have run out of heals!");
                    player.sendMessage(C.Blue + player.getName() + " has run out of heals!");
                }

                continue;
            }

            double toHeal = clicked.getMaxHealth() - clicked.getHealth();

            if (toHeal > heal) {
                toHeal = heal;
            }

            if (healsLeft >= 50) {
                UtilParticle.playParticle(ParticleType.BLACK_HEART, clicked.getEyeLocation().add(0, .4, 0));

                PotionEffect potion = UtilEnt.getPotion(clicked, PotionEffectType.REGENERATION);

                if (potion == null || (potion.getAmplifier() == 0 && potion.getDuration() < 20)) {
                    ConditionManager.addPotion(new PotionEffect(PotionEffectType.REGENERATION, 50, 0), clicked, player, false);
                }

                Recharge.use(player, "Heal" + entity.getUniqueId(), (int) (toHeal * 300));
                continue;
            }

            healsLeft += toHeal;

            _healsDone.put(clicked.getUniqueId(), healsLeft);

            if (healsLeft >= 50) {
                toHeal += (50 - healsLeft);
            }

            if (toHeal <= 0) {
                continue;
            }

            healedDamage += toHeal;
            UtilEnt.heal(clicked, toHeal);

            if (toHeal >= 1) {
                UtilParticle.playParticle(ParticleType.HEART, clicked.getEyeLocation().add(0, .4, 0));

                if (clicked instanceof Player) {
                    heal((Player) clicked);

                    ((Player) clicked).playSound(clicked.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.1F, 1);
                    clicked.sendMessage(C.Blue + player.getName() + " has healed you!");
                }
            }

            if (healsLeft <= 0) {
                clicked.sendMessage(C.Blue + "You have run out of heals!");
                player.sendMessage(C.Blue + player.getName() + " has run out of heals!");
            }

            Recharge.use(player, "Heal" + entity.getUniqueId(), (int) (toHeal * 300));
        }

        if (healedDamage > 3 && player.getHealth() * 1.2 < player.getMaxHealth())

        {
            double toHeal = player.getMaxHealth() - player.getHealth();

            if (toHeal > 1) {
                toHeal = 1;
            }

            UtilEnt.heal(player, toHeal);
            heal(player);
        }

        if (healedDamage >= 1)

        {
            player.sendMessage(C.Blue + "Healed " + (int) Math.round(healedDamage / 2) + " heart"
                    + ((int) Math.round(healedDamage / 2) == 1 ? "" : "s") + "!");
        }

        if (_playersHealed.containsKey(player.getUniqueId()))

        {
            healedDamage += _playersHealed.get(player.getUniqueId());
        }

        _playersHealed.put(player.getUniqueId(), healedDamage);

    }

    @EventHandler
    public void onDeath(DeathEvent event) {
        if (event.getDamageEvent().getAttackType().isInstantDeath())
            return;

        Player player = event.getPlayer();

        if (!hasAbility(player))
            return;

        scoldPlayer(player);
    }

    @EventHandler
    public void onGameState(GameStateEvent event) {
        if (event.getState() != GameState.End) {
            return;
        }

        for (Player player : getPlayers(true)) {
            scoldPlayer(player);
        }
    }

    @EventHandler
    public void onGameStateEnd(GameStateEvent event) {
        if (event.getState() != GameState.Dead) {
            for (UUID uuid : _playersHealed.keySet()) {
                int healed = (int) Math.floor(_playersHealed.get(uuid));

                if (healed <= 0)
                    continue;

                Stats.add(uuid, "Game." + getGame().getName() + "." + getKit().getName() + ".Heals", healed);
            }
        }
    }

    @EventHandler
    public void onHeal(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        PlayerInteractEvent newEvent = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                UtilInv.getHolding(player, Material.BLAZE_ROD), null, null);

        Bukkit.getPluginManager().callEvent(newEvent);

        if (newEvent.useItemInHand() == Result.DENY)
            return;

        heal(player, event.getRightClicked(), event.getRightClicked().getLocation());
    }

    @EventHandler
    public void onHeal(PlayerInteractEvent event) {
        if (event.useItemInHand() == Result.DENY || _smallGame)
            return;

        heal(event.getPlayer(), null, event.getPlayer().getLocation());
    }

    @EventHandler
    public void onHealthUpdate(TimeEvent event) {
        if (event.getType() != TimeType.TICK)
            return;

        if (!isLive())
            return;

        for (GameTeam team : getGame().getTeams()) {
            FakeScoreboard board = getManager().getScoreboard().getScoreboard(team.getName() + "Medic");

            for (Player player : getGame().getPlayers(true)) {
                board.makeScore(DisplaySlot.BELOW_NAME, player.getName(),
                        (int) Math.ceil((player.getHealth() / player.getMaxHealth()) * 100));
            }
        }
    }

    @Override
    public void registerAbility() {
        ScoreboardManager manager = getManager().getScoreboard();
        Game game = getManager().getGame();

        for (GameTeam team : game.getTeams()) {
            FakeScoreboard board = manager.getScoreboard(team.getName());

            FakeScoreboard newBoard = manager.createScoreboard(team.getName() + "Medic", (player) -> hasAbility(player));

            board.addChild(newBoard);

            for (Player player : UtilPlayer.getPlayers()) {
                newBoard.makeScore(DisplaySlot.BELOW_NAME, player.getName(), "% " + C.DRed + "❤", 100);
            }
        }

        _smallGame = UtilPlayer.getPlayers().size() <= 20;

        if (_smallGame) {
            new BukkitRunnable() {
                public void run() {
                    for (Player medic : getPlayers(true)) {
                        medic.sendMessage(C.Gold + "As this is a small game, your heals are disabled!");
                    }
                }
            }.runTaskLater(getPlugin(), 20);
        }
    }

    private void scoldPlayer(Player player) {
        double healed = 0;

        if (_playersHealed.containsKey(player.getUniqueId())) {
            healed = _playersHealed.get(player.getUniqueId());
        }

        if (healed <= 5) {
            player.sendMessage(C.Gold + "You need to improve your medic skills, heal more people!");
        }
        else {
            healed /= 2;

            player.sendMessage(
                    C.Gold + "You healed " + (int) healed + " heart" + ((int) healed == 1 ? "" : "s") + " this round!");
        }
    }

}
