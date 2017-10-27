package me.libraryaddict.arcade.game;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.Sound;

import me.libraryaddict.core.Pair;

public class GameOption<Y>
{
    private static GameOption[] _values = new GameOption[0];

    public static GameOption<Boolean> ALLOW_ANVIL = new GameOption<Boolean>(false);
    public static GameOption<Boolean> ALLOW_CRAFTING = new GameOption<Boolean>(false);
    public static GameOption<Boolean> ATTACK_NON_TEAM = new GameOption<Boolean>(true);
    public static GameOption<Boolean> ATTACK_TEAM = new GameOption<Boolean>(true);
    public static GameOption<Boolean> BLOCK_BURN = new GameOption<Boolean>(false);
    public static GameOption<Boolean> BLOCK_IGNITE = new GameOption<Boolean>(false);
    public static GameOption<Boolean> BORDER_BLOCKS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> BREAK_BLOCK = new GameOption<Boolean>(false);
    public static GameOption<Boolean> BREAK_GRASS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> CHEST_LOOT = new GameOption<Boolean>(false);
    public static GameOption<Boolean> COLOR_CHAT_NAMES = new GameOption<Boolean>(false);
    public static GameOption<Boolean> DAMAGE_NON_LIVING = new GameOption<Boolean>(false);
    public static GameOption<Boolean> DEAD_BODIES = new GameOption(false);
    public static GameOption<Boolean> DEATH_ITEMS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> DEATH_MESSAGES = new GameOption<Boolean>(true);
    public static GameOption<Boolean> DEATH_OUT = new GameOption<Boolean>(true);
    public static GameOption<Boolean> EXPLORE_PREGAME = new GameOption<Boolean>(true);
    public static GameOption<Boolean> FLYING_PREGAME = new GameOption<Boolean>(true);
    public static GameOption<Boolean> FORCE_RESOURCE_PACK = new GameOption<Boolean>(false);
    public static GameOption<Pair<Sound, Float>> GAME_START_SOUND = new GameOption<Pair<Sound, Float>>(
            Pair.of(Sound.BLOCK_NOTE_HARP, 0F));
    public static GameOption<Boolean> HATS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> HUNGER = new GameOption<Boolean>(false);
    public static GameOption<Boolean> INFORM_KILL_ASSIST = new GameOption<Boolean>(false);
    public static GameOption<Boolean> INTERACT_DECORATIONS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> ITEMS_SPAWN = new GameOption<Boolean>(false);
    public static GameOption<Boolean> KILLER_HEALTH = new GameOption<Boolean>(true);
    public static GameOption<Boolean> KILLS_IN_TAB = new GameOption<Boolean>(false);
    public static GameOption<Boolean> LOCK_TO_SPAWN = new GameOption<Boolean>(true);
    public static GameOption<Pair<Integer, Integer>> LOOT_AMOUNT = new GameOption<Pair<Integer, Integer>>(Pair.of(4, 8));
    public static GameOption<Boolean> MAP_VOTE = new GameOption<Boolean>(true);
    public static GameOption<Boolean> NATURAL_MOBS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> OPEN_CHEST = new GameOption<Boolean>(false);
    public static GameOption<Boolean> PICKUP_ITEM = new GameOption<Boolean>(false);
    public static GameOption<Material[]> PLACABLE_BLOCKS = new GameOption<Material[]>(new Material[0]);
    public static GameOption<Boolean> PLACE_BLOCK = new GameOption<Boolean>(false);
    public static GameOption<Pair<Sound, Float>> PLAYER_DEATH_SOUND = new GameOption<Pair<Sound, Float>>(null);
    public static GameOption<Boolean> PLAYER_DROP_ITEM = new GameOption<Boolean>(false);
    public static GameOption<Boolean> PUSH = new GameOption<Boolean>(false);
    public static GameOption<Boolean> REMOVE_SEEDS_DROP = new GameOption<Boolean>(false);
    public static GameOption<String> RESOURCE_PACK = new GameOption<String>(null);
    public static GameOption<Boolean> SERVER_HANDLES_WORLDS = new GameOption<Boolean>(true);
    public static GameOption<Boolean> SPEC_CLICK_INFO = new GameOption<Boolean>(false);
    public static GameOption<Double> STEAK_HEALTH = new GameOption<Double>(0D);
    public static GameOption<Boolean> TABLIST_KILLS = new GameOption<Boolean>(false);
    public static GameOption<Boolean> TEAM_HOTBAR = new GameOption<Boolean>(false);
    public static GameOption<Integer> TIME_CYCLE = new GameOption<Integer>(0);
    public static GameOption<Long> TIME_OF_WORLD = new GameOption<Long>(0L);
    public static GameOption<Boolean> UNBREAKABLE = new GameOption<Boolean>(true);
    public static GameOption<Boolean> WEATHER = new GameOption<Boolean>(false);
    public static GameOption<Boolean> REGENERATION = new GameOption<Boolean>(true);

    public static GameOption[] values()
    {
        return _values;
    }

    private Y _default;

    private GameOption(Y defaultValue)
    {
        _default = defaultValue;

        _values = Arrays.copyOf(_values, _values.length + 1);
        _values[_values.length - 1] = this;
    }

    public Y getDefault()
    {
        return _default;
    }

}
