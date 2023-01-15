package pyre.tinkerslevellingaddon.util;

import net.minecraft.network.chat.TextColor;
import pyre.tinkerslevellingaddon.config.Config;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.*;

public class ToolLevellingUtil {
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
    private static final Map<String, FloatToolStat> ARMOR_STAT_TYPES;
    private static final Map<String, FloatToolStat> ALL_STAT_TYPES;
    
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
    
    public static boolean isSlotsLevellingEnabled(ToolRebuildContext context) {
        if (isArmor(context)) {
            return Config.armorSlotGainingMethod.get() != Config.GainingMethod.NONE;
        }
        return Config.toolsSlotGainingMethod.get() != Config.GainingMethod.NONE;
    }
    
    public static boolean isStatsLevellingEnabled(ToolRebuildContext context) {
        if (isArmor(context)) {
            return Config.armorStatGainingMethod.get() != Config.GainingMethod.NONE;
        }
        return Config.toolsStatGainingMethod.get() != Config.GainingMethod.NONE;
    }
    
    public static boolean canPredictNextSlot(ToolStack tool) {
        return isArmor(tool) ?
                Config.armorSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER :
                Config.toolsSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
    }
    
    public static boolean canPredictNextStat(ToolStack tool) {
        return isArmor(tool) ?
                Config.armorStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER :
                Config.toolsStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
    }
    
    public static String getSlot(ToolStack tool, int level) {
        if (isArmor(tool)) {
            return switch (Config.armorSlotGainingMethod.get()) {
                case NONE -> null;
                case PREDEFINED_ORDER -> getArmorSlotForLevel(level);
                case RANDOM -> getRandomArmorSlot();
            };
        }
        
        return switch (Config.toolsSlotGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getToolSlotForLevel(level);
            case RANDOM -> getRandomToolSlot();
        };
    }
    
    public static String getStat(ToolStack tool, int level) {
        if (isArmor(tool)) {
            return switch (Config.armorStatGainingMethod.get()) {
                case NONE -> null;
                case PREDEFINED_ORDER -> getArmorStatForLevel(level);
                case RANDOM -> getRandomArmorStat();
            };
        }
        
        return switch (Config.toolsStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getToolStatForLevel(level);
            case RANDOM -> getRandomToolStat();
        };
    }
    
    public static double getStatValue(ToolStack tool, String stat) {
        return isArmor(tool) ?
                Config.getArmorStatValue(ALL_STAT_TYPES.get(stat)) :
                Config.getToolStatValue(ALL_STAT_TYPES.get(stat));
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
    
    public static boolean canLevelUp(int level) {
        return Config.maxLevel.get() == 0 || Config.maxLevel.get() > level;
    }
    
    public static int getXpNeededForLevel(int level, boolean isBroadTool) {
        int experienceNeeded = Config.baseExperience.get();
        if (level > 1) {
            experienceNeeded = (int) (getXpNeededForLevel(level - 1, false) * Config.requiredXpMultiplier.get());
        }
        if (isBroadTool) {
            experienceNeeded *= Config.broadToolRequiredXpMultiplier.get();
        }
        return experienceNeeded;
    }
    
    public static boolean isArmor(ToolStack tool) {
        return tool.hasTag(TinkerTags.Items.ARMOR);
    }
    
    public static boolean isArmor(ToolRebuildContext context) {
        return context.hasTag(TinkerTags.Items.ARMOR);
    }
    
    public static boolean isBroadTool(IToolStackView tool) {
        return tool.getMaterials().size() > 3;
    }
    
    public static TextColor getSlotColor(String slotName) {
        return ALL_SLOT_TYPES.get(slotName).getColor();
    }
    
    public static TextColor getStatColor(String statName) {
        return ALL_STAT_TYPES.get(statName).getColor();
    }
    
    private static String getToolSlotForLevel(int level) {
        List<String> toolsSlotsOrder = Config.getToolsSlotsOrder();
        return toolsSlotsOrder.get((level - 1) % toolsSlotsOrder.size());
    }
    
    private static String getRandomToolSlot() {
        List<String> toolsSlotsRandomPool = Config.getToolsSlotsRandomPool();
        return toolsSlotsRandomPool.get(RANDOM.nextInt(toolsSlotsRandomPool.size()));
    }
    
    private static String getArmorSlotForLevel(int level) {
        List<String> armorSlotsOrder = Config.getArmorSlotsOrder();
        return armorSlotsOrder.get((level - 1) % armorSlotsOrder.size());
    }
    
    private static String getRandomArmorSlot() {
        List<String> armorSlotsRandomPool = Config.getArmorSlotsRandomPool();
        return armorSlotsRandomPool.get(RANDOM.nextInt(armorSlotsRandomPool.size()));
    }
    
    private static String getToolStatForLevel(int level) {
        List<String> toolsStatsOrder = Config.getToolsStatsOrder();
        return toolsStatsOrder.get((level - 1) % toolsStatsOrder.size());
    }
    
    private static String getRandomToolStat() {
        List<String> toolsStatsRandomPool = Config.getToolsStatsRandomPool();
        return toolsStatsRandomPool.get(RANDOM.nextInt(toolsStatsRandomPool.size()));
    }
    
    private static String getArmorStatForLevel(int level) {
        List<String> armorStatsOrder = Config.getArmorStatsOrder();
        return armorStatsOrder.get((level - 1) % armorStatsOrder.size());
    }
    
    private static String getRandomArmorStat() {
        List<String> armorStatsRandomPool = Config.getArmorStatsRandomPool();
        return armorStatsRandomPool.get(RANDOM.nextInt(armorStatsRandomPool.size()));
    }
    
    private ToolLevellingUtil() {
        //hide constructor
    }
}
