package me.libraryaddict.build.customdata;

import me.libraryaddict.build.customdata.borders.SquareBorder;
import me.libraryaddict.build.types.WorldInfo;
import me.libraryaddict.core.C;
import me.libraryaddict.core.Pair;
import me.libraryaddict.core.data.TeamSettings;
import me.libraryaddict.core.hologram.Hologram;
import me.libraryaddict.core.inventory.utils.ItemBuilder;
import me.libraryaddict.core.recharge.Recharge;
import me.libraryaddict.core.utils.UtilInv;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class SearchAndDestroyCustomData extends GameCustomData {
    private HashMap<TeamSettings, ArrayList<Pair<Block, Hologram>>> _bombs = new HashMap<TeamSettings,
            ArrayList<Pair<Block, Hologram>>>();
    private ItemStack[] _modifyBomb;
    private boolean _random;
    private ItemStack _randomTeams = new ItemBuilder(Material.ARMOR_STAND).setTitle(C.Gold + "Random Teams")
            .addLore("Click on this to make the game randomly switch around the teams").build();

    public SearchAndDestroyCustomData(WorldInfo world) {
        super(world, SquareBorder.class, TeamSettings.RED, TeamSettings.BLUE, TeamSettings.GREEN, TeamSettings.PURPLE,
                TeamSettings.PLAYER);

        _modifyBomb = new ItemStack[getUsableTeams().length];

        for (int i = 0; i < _modifyBomb.length; i++) {
            TeamSettings team = getUsableTeams()[i];

            if (team == TeamSettings.PLAYER)
                _modifyBomb[i] = new ItemBuilder(Material.BLAZE_POWDER).setTitle(C.Gold + "Modify Nuke")
                        .addLore("Right click on a TNT block to add or remove this nuke", "",
                                "An nuke is one where your team explodes it to kill all the other teams").build();
            else
                _modifyBomb[i] = new ItemBuilder(Material.BLAZE_POWDER)
                        .setTitle(C.Gold + "Modify " + team.getColor() + team.getName() + "'s" + C.Gold + " Bomb")
                        .addLore("Right click on a TNT block to add or remove this bomb").build();
        }
    }

    @Override
    public ArrayList<ItemStack> getButtons() {
        ArrayList<ItemStack> tools = super.getButtons();

        tools.add(_randomTeams);

        return tools;
    }

    @Override
    public int getMaxSpawns() {
        return 120;
    }

    @Override
    public int getMaxTeams() {
        return 10;
    }

    @Override
    public int getMinSpawns() {
        return 12;
    }

    @Override
    public int getMinTeams() {
        return 2;
    }

    public String getMissing() {
        if (_bombs.containsKey(TeamSettings.PLAYER))
            return super.getMissing();

        for (TeamSettings team : getUsedTeams()) {
            if (!_bombs.containsKey(team) && team != TeamSettings.PLAYER) {
                return team.getName() + " does not have a bomb set";
            }
        }

        return super.getMissing();
    }

    @Override
    public ArrayList<ItemStack> getTools() {
        ArrayList<ItemStack> tools = super.getTools();

        tools.remove(getAddSpawns()[getUsableTeams().length - 1]);

        for (ItemStack item : _modifyBomb) {
            tools.add(item);
        }

        return tools;
    }

    public void loadConfig(HashMap<String, ArrayList<String>> hashmap) {
        super.loadConfig(hashmap);

        for (Entry<String, ArrayList<String>> entry : hashmap.entrySet()) {
            if (!entry.getKey().endsWith(" Bombs")) {
                continue;
            }

            TeamSettings team = TeamSettings.valueOf(entry.getKey().replace(" Bombs", ""));

            _bombs.put(team, new ArrayList<Pair<Block, Hologram>>());

            for (String bomb : entry.getValue()) {
                Block block = getBlock(bomb);

                Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5),
                        team.getColor() + C.Bold + "Bomb");
                hologram.start();

                _bombs.get(team).add(Pair.of(block, hologram));
            }
        }

        if (hashmap.containsKey("Random Teams")) {
            _random = true;
        }

        new ItemBuilder(_randomTeams).setModifyBaseItem().setTitle(C.Gold + "Random Teams: " + _random).build();
    }

    @Override
    public void onButtonClick(Player player, ItemStack item) {
        super.onButtonClick(player, item);

        if (item.getType() != Material.ARMOR_STAND)
            return;

        if (!isBuilder(player)) {
            player.sendMessage(C.Red + "You do not have permission to modify this map!");
            return;
        }

        _random = !_random;

        Announce(C.Gold + player.getName() + " has changed random teams: " + _random);

        new ItemBuilder(_randomTeams).setModifyBaseItem().setTitle(C.Gold + "Random Teams: " + _random).build();
    }

    @EventHandler
    public void onInteractBomb(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();

        if (!isInMap(player))
            return;

        Block block = event.getClickedBlock();

        if (block.getType() != Material.TNT)
            return;

        if (!Recharge.canUse(player, "Tool"))
            return;

        for (int i = 0; i < _modifyBomb.length; i++) {
            if (!UtilInv.isSimilar(_modifyBomb[i], event.getItem()))
                continue;

            Recharge.use(player, "Tool", 100);

            if (!isBuilder(player)) {
                player.sendMessage(C.Red + "You do not have permission to use this tool");
                return;
            }

            event.setCancelled(true);

            for (TeamSettings team : this.getUsableTeams()) {
                if (!_bombs.containsKey(team))
                    continue;

                Iterator<Pair<Block, Hologram>> itel = _bombs.get(team).iterator();

                while (itel.hasNext()) {
                    Pair<Block, Hologram> pair = itel.next();

                    if (!pair.getKey().equals(block))
                        continue;

                    pair.getValue().stop();
                    itel.remove();

                    if (_bombs.get(team).isEmpty()) {
                        _bombs.remove(team);
                    }

                    if (team == TeamSettings.PLAYER)
                        Announce(C.Gold + player.getName() + " has removed an unclaimed bomb");
                    else
                        Announce(C.Gold + player.getName() + " has removed a bomb from " + team.getName());
                    return;
                }
            }

            TeamSettings team = getUsableTeams()[i];

            if (!_bombs.containsKey(team)) {
                _bombs.put(team, new ArrayList<Pair<Block, Hologram>>());
            }

            Hologram hologram = new Hologram(block.getLocation().add(0.5, 1, 0.5), team.getColor() + C.Bold + "Bomb");
            hologram.start();

            Pair<Block, Hologram> pair = Pair.of(block, hologram);

            _bombs.get(team).add(pair);

            if (team == TeamSettings.PLAYER)
                Announce(C.Gold + player.getName() + " has added an unclaimed bomb");
            else
                Announce(C.Gold + player.getName() + " has added a bomb to " + team.getName());
        }
    }

    public void saveConfig(HashMap<String, ArrayList<String>> hashmap) {
        super.saveConfig(hashmap);

        for (Entry<TeamSettings, ArrayList<Pair<Block, Hologram>>> team : _bombs.entrySet()) {
            ArrayList<String> bombs = new ArrayList<String>();

            for (Pair<Block, Hologram> pair : team.getValue()) {
                bombs.add(toString(pair.getKey()));
            }

            hashmap.put(team.getKey().name() + " Bombs", bombs);
        }

        if (_random) {
            hashmap.put("Random Teams", new ArrayList(Arrays.asList("true")));
        }
    }

    public void unloadData() {
        super.unloadData();

        for (ArrayList<Pair<Block, Hologram>> list : _bombs.values()) {
            for (Pair<Block, Hologram> pair : list) {
                pair.getValue().stop();
            }
        }
    }
}
