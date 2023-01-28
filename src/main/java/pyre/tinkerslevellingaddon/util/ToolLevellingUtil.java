package pyre.tinkerslevellingaddon.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.network.LevelUpPacket;
import pyre.tinkerslevellingaddon.network.Messages;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.*;

import static pyre.tinkerslevellingaddon.ImprovableModifier.*;

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
    //ranged
    public static final String DRAW_SPEED = "drawSpeed";
    public static final String VELOCITY = "velocity";
    public static final String ACCURACY = "accuracy";
    public static final String PROJECTILE_DAMAGE = "projectileDamage";
    
    private static final Map<String, SlotType> TOOL_SLOT_TYPES;
    private static final Map<String, SlotType> ARMOR_SLOT_TYPES;
    private static final Map<String, SlotType> ALL_SLOT_TYPES;
    
    private static final Map<String, FloatToolStat> TOOL_STAT_TYPES;
    private static final Map<String, FloatToolStat> RANGED_STAT_TYPES;
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
    
        RANGED_STAT_TYPES = new LinkedHashMap<>();
        RANGED_STAT_TYPES.put(DRAW_SPEED, ToolStats.DRAW_SPEED);
        RANGED_STAT_TYPES.put(VELOCITY, ToolStats.VELOCITY);
        RANGED_STAT_TYPES.put(ACCURACY, ToolStats.ACCURACY);
        RANGED_STAT_TYPES.put(PROJECTILE_DAMAGE, ToolStats.PROJECTILE_DAMAGE);
        RANGED_STAT_TYPES.put(DURABILITY, ToolStats.DURABILITY);
        RANGED_STAT_TYPES.put(ATTACK_DAMAGE, ToolStats.ATTACK_DAMAGE);
        RANGED_STAT_TYPES.put(ATTACK_SPEED, ToolStats.ATTACK_SPEED);
        
        ARMOR_STAT_TYPES = new LinkedHashMap<>();
        ARMOR_STAT_TYPES.put(DURABILITY, ToolStats.DURABILITY);
        ARMOR_STAT_TYPES.put(ARMOR, ToolStats.ARMOR);
        ARMOR_STAT_TYPES.put(ARMOR_TOUGHNESS, ToolStats.ARMOR_TOUGHNESS);
        ARMOR_STAT_TYPES.put(KNOCKBACK_RESISTANCE, ToolStats.KNOCKBACK_RESISTANCE);
        
        ALL_STAT_TYPES = new HashMap<>(TOOL_STAT_TYPES);
        ALL_STAT_TYPES.putAll(RANGED_STAT_TYPES);
        ALL_STAT_TYPES.putAll(ARMOR_STAT_TYPES);
    }
    
    public static Set<String> getToolSlotTypes() {
        return TOOL_SLOT_TYPES.keySet();
    }
    
    public static Set<String> getRangedSlotTypes() {
        //this probably always be the same as tools
        return TOOL_SLOT_TYPES.keySet();
    }
    
    public static Set<String> getArmorSlotTypes() {
        return ARMOR_SLOT_TYPES.keySet();
    }
    
    public static Set<String> getToolStatTypes() {
        return TOOL_STAT_TYPES.keySet();
    }
    
    public static Set<String> getRangedStatTypes() {
        return RANGED_STAT_TYPES.keySet();
    }
    
    public static Set<String> getArmorStatTypes() {
        return ARMOR_STAT_TYPES.keySet();
    }
    
    public static boolean isSlotsLevellingEnabled(ToolRebuildContext context) {
        if (isArmor(context)) {
            return Config.armorSlotGainingMethod.get() != Config.GainingMethod.NONE;
        }
        if (isRanged(context)) {
            return Config.rangedSlotGainingMethod.get() != Config.GainingMethod.NONE;
        }
        return Config.toolsSlotGainingMethod.get() != Config.GainingMethod.NONE;
    }
    
    public static boolean isStatsLevellingEnabled(ToolRebuildContext context) {
        if (isArmor(context)) {
            return Config.armorStatGainingMethod.get() != Config.GainingMethod.NONE;
        }
        if (isRanged(context)) {
            return Config.rangedStatGainingMethod.get() != Config.GainingMethod.NONE;
        }
        return Config.toolsStatGainingMethod.get() != Config.GainingMethod.NONE;
    }
    
    public static boolean canPredictNextSlot(ToolStack tool) {
        if (isArmor(tool)) {
            return Config.armorSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
        }
        if (isRanged(tool)) {
            return Config.rangedSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
        }
        return Config.toolsSlotGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
    }
    
    public static boolean canPredictNextStat(ToolStack tool) {
        if (isArmor(tool)) {
            return Config.armorStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
        }
        if (isRanged(tool)) {
            return Config.rangedStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
        }
        return Config.toolsStatGainingMethod.get() == Config.GainingMethod.PREDEFINED_ORDER;
    }
    
    public static String getSlot(ToolStack tool, int level) {
        if (isArmor(tool)) {
            return switch (Config.armorSlotGainingMethod.get()) {
                case NONE -> null;
                case PREDEFINED_ORDER -> getArmorSlotForLevel(level);
                case RANDOM -> getRandomArmorSlot();
            };
        }
    
        if (isRanged(tool)) {
            return switch (Config.rangedSlotGainingMethod.get()) {
                case NONE -> null;
                case PREDEFINED_ORDER -> getRangedSlotForLevel(level);
                case RANDOM -> getRandomRangedSlot();
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
    
        if (isRanged(tool)) {
            return switch (Config.rangedStatGainingMethod.get()) {
                case NONE -> null;
                case PREDEFINED_ORDER -> getRangedStatForLevel(level);
                case RANDOM -> getRandomRangedStat();
            };
        }
        
        return switch (Config.toolsStatGainingMethod.get()) {
            case NONE -> null;
            case PREDEFINED_ORDER -> getToolStatForLevel(level);
            case RANDOM -> getRandomToolStat();
        };
    }
    
    public static double getStatValue(IToolContext tool, String stat) {
        return getStatValue(tool, ALL_STAT_TYPES.get(stat));
    }
    
    public static double getStatValue(IToolContext tool, FloatToolStat stat) {
        if (isArmor(tool)) {
            return Config.getArmorStatValue(stat);
        }
        if (isRanged(tool)){
            return Config.getRangedStatValue(stat);
        }
        return Config.getToolStatValue(stat);
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
    
    public static void addExperience(ToolStack tool, int amount, ServerPlayer player) {
        if (tool == null) {
            return;
        }
        
        ModDataNBT data = tool.getPersistentData();
        int currentLevel = data.getInt(LEVEL_KEY);
        int currentExperience = data.getInt(EXPERIENCE_KEY) + amount;
        boolean isBroadTool = ToolLevellingUtil.isBroadTool(tool);
        int experienceNeeded = ToolLevellingUtil.getXpNeededForLevel(currentLevel + 1, isBroadTool);
        
        while (currentExperience >= experienceNeeded) {
            if (!ToolLevellingUtil.canLevelUp(currentLevel)) {
                return;
            }
            data.putInt(LEVEL_KEY, ++currentLevel);
            currentExperience -= experienceNeeded;
            experienceNeeded = ToolLevellingUtil.getXpNeededForLevel(currentLevel + 1, isBroadTool);
            
            String slotName = ToolLevellingUtil.getSlot(tool, currentLevel);
            if (slotName != null) {
                appendHistory(SLOT_HISTORY_KEY, slotName, data);
            }
            String statName = ToolLevellingUtil.getStat(tool, currentLevel);
            if (statName != null) {
                appendHistory(STAT_HISTORY_KEY, statName, data);
            }
            
            //temporarily set xp to 0, so it displays nicely in chat message
            data.putInt(EXPERIENCE_KEY, 0);
            tool.rebuildStats();
            if (player != null) {
                Component toolName = tool.createStack().getDisplayName();
                Messages.sendToPlayer(new LevelUpPacket(currentLevel, toolName), player);
            }
        }
        data.putInt(EXPERIENCE_KEY, currentExperience);
    }
    
    public static boolean isArmor(IToolContext tool) {
        return tool.hasTag(TinkerTags.Items.ARMOR);
    }
    
    public static boolean isRanged(IToolContext tool) {
        return tool.hasTag(TinkerTags.Items.RANGED);
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
    
    private static String getRangedSlotForLevel(int level) {
        List<String> toolsSlotsOrder = Config.getToolsSlotsOrder();
        return toolsSlotsOrder.get((level - 1) % toolsSlotsOrder.size());
    }
    
    private static String getRandomRangedSlot() {
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
    
    private static String getRangedStatForLevel(int level) {
        List<String> rangedStatsRotation = Config.getRangedStatsOrder();
        return rangedStatsRotation.get((level - 1) % rangedStatsRotation.size());
    }
    
    private static String getRandomRangedStat() {
        List<String> rangedStatsRandomPool = Config.getRangedStatsRandomPool();
        return rangedStatsRandomPool.get(RANDOM.nextInt(rangedStatsRandomPool.size()));
    }
    
    private static String getArmorStatForLevel(int level) {
        List<String> armorStatsOrder = Config.getArmorStatsOrder();
        return armorStatsOrder.get((level - 1) % armorStatsOrder.size());
    }
    
    private static String getRandomArmorStat() {
        List<String> armorStatsRandomPool = Config.getArmorStatsRandomPool();
        return armorStatsRandomPool.get(RANDOM.nextInt(armorStatsRandomPool.size()));
    }
    
    private static void appendHistory(ResourceLocation historyKey, String value, ModDataNBT data) {
        String modifierHistory = data.getString(historyKey);
        modifierHistory = modifierHistory + value + ";";
        data.putString(historyKey, modifierHistory);
    }
    
    private ToolLevellingUtil() {
        //hide constructor
    }
}
