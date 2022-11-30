package pyre.tinkerslevellingaddon.util;

import pyre.tinkerslevellingaddon.config.Config;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.*;

public class ModifierUtil {
    //modifier types
    public static final String UPGRADE = "upgrade";
    public static final String ABILITY = "ability";
    public static final String SOUL = "soul";
    public static final String DEFENSE = "defense";

    //stat types
    public static final String DURABILITY = "durability";
    public static final String ATTACK_DAMAGE = "attackDamage";
    public static final String ATTACK_SPEED = "attackSpeed";
    public static final String MINING_SPEED = "miningSpeed";
    public static final String ARMOR = "armor";
    public static final String ARMOR_TOUGHNESS = "armorToughness";
    public static final String KNOCKBACK_RESISTANCE = "knockbackResistance";

    private static final Map<String, SlotType> TOOL_SLOT_TYPES;
    private static final Map<String, SlotType> ARMOR_SLOT_TYPES;
    private static final Map<String, SlotType> ALL_SLOT_TYPES;

    private static final Map<String, FloatToolStat> TOOL_STAT_TYPES;
    private static final  Map<String, FloatToolStat> ARMOR_STAT_TYPES;
    private static final  Map<String, FloatToolStat> ALL_STAT_TYPES;

    private static final Random RANDOM = new Random();

    static {
        TOOL_SLOT_TYPES = new LinkedHashMap<>();
        TOOL_SLOT_TYPES.put(UPGRADE, SlotType.UPGRADE);
        TOOL_SLOT_TYPES.put(ABILITY, SlotType.ABILITY);
        TOOL_SLOT_TYPES.put(SOUL, SlotType.SOUL);

        ARMOR_SLOT_TYPES = new LinkedHashMap<>(TOOL_SLOT_TYPES);
        ARMOR_SLOT_TYPES.put(DEFENSE, SlotType.DEFENSE);

        ALL_SLOT_TYPES = new HashMap<>(TOOL_SLOT_TYPES);
        ALL_SLOT_TYPES.putAll(ARMOR_SLOT_TYPES);

        TOOL_STAT_TYPES = new LinkedHashMap<>();
        TOOL_STAT_TYPES.put(DURABILITY, ToolStats.DURABILITY);
        TOOL_STAT_TYPES.put(ATTACK_DAMAGE, ToolStats.ATTACK_DAMAGE);
        TOOL_STAT_TYPES.put(ATTACK_SPEED, ToolStats.ATTACK_SPEED);
        TOOL_STAT_TYPES.put(MINING_SPEED, ToolStats.MINING_SPEED);

        ARMOR_STAT_TYPES = new LinkedHashMap<>();
        ARMOR_STAT_TYPES.put(DURABILITY, ToolStats.DURABILITY);
        ARMOR_STAT_TYPES.put(ARMOR, ToolStats.ARMOR);
        ARMOR_STAT_TYPES.put(ARMOR_TOUGHNESS, ToolStats.ARMOR_TOUGHNESS);
        ARMOR_STAT_TYPES.put(KNOCKBACK_RESISTANCE, ToolStats.KNOCKBACK_RESISTANCE);

        ALL_STAT_TYPES = new HashMap<>(TOOL_STAT_TYPES);
        ALL_STAT_TYPES.putAll(ARMOR_STAT_TYPES);
    }

    public static Set<String> getToolSlotTypes() {
        return TOOL_SLOT_TYPES.keySet();
    }

    public static Set<String> getArmorSlotTypes() {
        return ARMOR_SLOT_TYPES.keySet();
    }

    public static Set<String> getToolStatTypes() {
        return TOOL_STAT_TYPES.keySet();
    }

    public static Set<String> getArmorStatTypes() {
        return ARMOR_STAT_TYPES.keySet();
    }

    public static String getToolSlotForLevel(int level) {
        List<String> toolsSlotsRotation = Config.getToolsSlotsRotation();
        return toolsSlotsRotation.get((level - 1) % toolsSlotsRotation.size());
    }

    public static String getRandomToolSlot() {
        List<String> toolsSlotsRandomPool = Config.getToolsSlotsRandomPool();
        return toolsSlotsRandomPool.get(RANDOM.nextInt(toolsSlotsRandomPool.size()));
    }

    public static String getArmorSlotForLevel(int level) {
        List<String> armorSlotsRotation = Config.getArmorSlotsRotation();
        return armorSlotsRotation.get((level - 1) % armorSlotsRotation.size());
    }

    public static String getRandomArmorSlot() {
        List<String> armorSlotsRandomPool = Config.getArmorSlotsRandomPool();
        return armorSlotsRandomPool.get(RANDOM.nextInt(armorSlotsRandomPool.size()));
    }

    public static String getToolStatForLevel(int level) {
        List<String> toolsStatsRotation = Config.getToolsStatsRotation();
        return toolsStatsRotation.get((level - 1) % toolsStatsRotation.size());
    }

    public static String getRandomToolStat() {
        List<String> toolsStatsRandomPool = Config.getToolsStatsRandomPool();
        return toolsStatsRandomPool.get(RANDOM.nextInt(toolsStatsRandomPool.size()));
    }

    public static String getArmorStatForLevel(int level) {
        List<String> armorStatsRotation = Config.getArmorStatsRotation();
        return armorStatsRotation.get((level - 1) % armorStatsRotation.size());
    }

    public static String getRandomArmorStat() {
        List<String> armorStatsRandomPool = Config.getArmorStatsRandomPool();
        return armorStatsRandomPool.get(RANDOM.nextInt(armorStatsRandomPool.size()));
    }

    public static List<SlotType> parseSlotsHistory(String historyString) {
        ArrayList<SlotType> result = new ArrayList<>();
        if (!historyString.isBlank()) {
            for (String statString : historyString.split(";")) {
                SlotType stat = ALL_SLOT_TYPES.get(statString);
                if (stat != null) {
                    result.add(stat);
                } else {
                    throw new UnsupportedOperationException("Unsupported slot type " + statString);
                }
            }
        }
        return result;
    }

    public static List<FloatToolStat> parseStatsHistory(String historyString) {
        ArrayList<FloatToolStat> result = new ArrayList<>();
        if (!historyString.isBlank()) {
            for (String statString : historyString.split(";")) {
                FloatToolStat stat = ALL_STAT_TYPES.get(statString);
                if (stat != null) {
                    result.add(stat);
                } else {
                    throw new UnsupportedOperationException("Unsupported stat type " + statString);
                }
            }
        }
        return result;
    }

    private ModifierUtil() {
        //hide constructor
    }
}
