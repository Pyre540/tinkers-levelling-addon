package pyre.tinkerslevellingaddon.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import slimeknights.tconstruct.library.tools.SlotType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config {

    private static Map<String, SlotType> toolSlotTypes;
    private static Map<String, SlotType> armorSlotTypes;

    private static final String UPGRADE = "upgrade";
    private static final String ABILITY = "ability";

    private static final String SOUL = "soul";
    private static final String DEFENSE = "defense";

    private static final List<String> DEFAULT_TOOLS_SLOTS_ROTATION = List.of(UPGRADE, UPGRADE, UPGRADE, ABILITY, UPGRADE);
    private static final List<String> DEFAULT_ARMOR_SLOTS_ROTATION = List.of(UPGRADE, DEFENSE, UPGRADE, ABILITY, DEFENSE);

    static {
        toolSlotTypes = new LinkedHashMap<>();
        toolSlotTypes.put(UPGRADE, SlotType.UPGRADE);
        toolSlotTypes.put(ABILITY, SlotType.ABILITY);
        toolSlotTypes.put(SOUL, SlotType.SOUL);

        armorSlotTypes = new LinkedHashMap<>(toolSlotTypes);
        armorSlotTypes.put(DEFENSE, SlotType.DEFENSE);
    }

    public static final ForgeConfigSpec COMMON_CONFIG;

    static {
        ForgeConfigSpec.Builder configBuilder = new ForgeConfigSpec.Builder();
        setupCommonConfig(configBuilder);
        COMMON_CONFIG = configBuilder.build();
    }

    public static ForgeConfigSpec.IntValue maxLevel;
    public static ForgeConfigSpec.IntValue baseExperience;
    public static ForgeConfigSpec.DoubleValue levelMultiplier;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsModifierTypeRotation;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorModifierTypeRotation;

    private static void setupCommonConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("General addon settings").push("general");

        maxLevel = builder.comment("Maximum tool level tha could be achieved. If set to 0 there is no upper limit.")
                .translation("config.tinkerslevellingaddon.general.maxLevel")
                .defineInRange("maxLevel", 5, 0, Integer.MAX_VALUE);

        baseExperience = builder.comment("Base amount of experience needed to reach next level.")
                .translation("config.tinkerslevellingaddon.general.baseExperience")
                .defineInRange("baseExperience", 2500, 1, Integer.MAX_VALUE);

        levelMultiplier = builder.comment("How much the amount of experience needed to reach next level will be multiplied per level.")
                .translation("config.tinkerslevellingaddon.general.levelMultiplier")
                .defineInRange("levelMultiplier", 2D, 1D, 10D);

        toolsModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.", "If empty default rotation will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_ROTATION) + ").", "Possible values: " + String.join(", ", toolSlotTypes.keySet()))
                .translation("config.tinkerslevellingaddon.general.toolsModifierTypeRotation")
                .defineList("toolsModifierTypeRotation", DEFAULT_TOOLS_SLOTS_ROTATION, t -> toolSlotTypes.containsKey(t));

        armorModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.", "If empty default rotation will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ROTATION) + ").", "Possible values: " + String.join(", ", armorSlotTypes.keySet()))
                .translation("config.tinkerslevellingaddon.general.armorModifierTypeRotation")
                .defineList("armorModifierTypeRotation", DEFAULT_ARMOR_SLOTS_ROTATION, t -> armorSlotTypes.containsKey(t));
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
    }

    public static List<SlotType> getToolsSlotsRotation() {
        List<? extends String> toolsRotation = toolsModifierTypeRotation.get();
        if (toolsRotation.isEmpty()) {
            toolsRotation = DEFAULT_TOOLS_SLOTS_ROTATION;
        }

        return toolsRotation.stream()
                .map(s -> toolSlotTypes.get(s))
                .toList();
    }

    public static List<SlotType> getArmorSlotsRotation() {
        List<? extends String> toolsRotation = armorModifierTypeRotation.get();
        if (toolsRotation.isEmpty()) {
            toolsRotation = DEFAULT_ARMOR_SLOTS_ROTATION;
        }

        return toolsRotation.stream()
                .map(s -> armorSlotTypes.get(s))
                .toList();
    }
}
