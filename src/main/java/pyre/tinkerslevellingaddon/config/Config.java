package pyre.tinkerslevellingaddon.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.RegistryObject;
import pyre.tinkerslevellingaddon.setup.Registration;
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
    public static final ForgeConfigSpec CLIENT_CONFIG;

    static {
        ForgeConfigSpec.Builder commonConfigBuilder = new ForgeConfigSpec.Builder();
        generaConfig(commonConfigBuilder);
        toolLevellingConfig(commonConfigBuilder);
        COMMON_CONFIG = commonConfigBuilder.build();
        ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
        clientConfig(clientConfigBuilder);
        CLIENT_CONFIG = clientConfigBuilder.build();
    }

    //COMMON
    public static ForgeConfigSpec.IntValue maxLevel;
    public static ForgeConfigSpec.IntValue baseExperience;
    public static ForgeConfigSpec.DoubleValue levelMultiplier;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsModifierTypeRotation;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorModifierTypeRotation;

    public static ForgeConfigSpec.BooleanValue damageDealt;
    public static ForgeConfigSpec.BooleanValue damageTaken;
    public static ForgeConfigSpec.BooleanValue enablePvp;

    public static ForgeConfigSpec.BooleanValue enableMiningXp;
    public static ForgeConfigSpec.BooleanValue enableHarvestingXp;
    public static ForgeConfigSpec.BooleanValue enableShearingXp;
    public static ForgeConfigSpec.BooleanValue enableAttackingXp;
    public static ForgeConfigSpec.BooleanValue enableTakingDamageXp;
    public static ForgeConfigSpec.BooleanValue enableThornsXp;

    public static ForgeConfigSpec.IntValue bonusMiningXp;
    public static ForgeConfigSpec.IntValue bonusHarvestingXp;
    public static ForgeConfigSpec.IntValue bonusShearingXp;
    public static ForgeConfigSpec.IntValue bonusAttackingXp;
    public static ForgeConfigSpec.IntValue bonusTakingDamageXp;
    public static ForgeConfigSpec.IntValue bonusThornsXp;

    //CLIENT
    public static ForgeConfigSpec.BooleanValue enableLevelUpMessage;
    public static ForgeConfigSpec.EnumValue<LevelUpSound> levelUpSound;

    private static void generaConfig(ForgeConfigSpec.Builder builder) {
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

        toolsModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", toolSlotTypes.keySet()))
                .translation("config.tinkerslevellingaddon.general.toolsModifierTypeRotation")
                .defineList("toolsModifierTypeRotation", DEFAULT_TOOLS_SLOTS_ROTATION, t -> toolSlotTypes.containsKey(t));

        armorModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", armorSlotTypes.keySet()))
                .translation("config.tinkerslevellingaddon.general.armorModifierTypeRotation")
                .defineList("armorModifierTypeRotation", DEFAULT_ARMOR_SLOTS_ROTATION, t -> armorSlotTypes.containsKey(t));

        builder.pop();
    }

    private static void toolLevellingConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Tool levelling settings").push("toolLevelling");

        damageDealt = builder.comment("If true, base experience value gained for attacking is equal to damage dealt (rounded to whole number), otherwise 1.")
                .translation("config.tinkerslevellingaddon.levelling.damageDealt")
                .define("damageDealt", true);
        damageTaken = builder.comment("If true, base experience value gained for taking damage is equal to damage taken (rounded to whole number), otherwise 1.")
                .translation("config.tinkerslevellingaddon.levelling.damageTaken")
                .define("damageTaken", true);
        enablePvp = builder.comment("If true, allows to gain experience from dealing damage to or taking damage from other players.")
                .translation("config.tinkerslevellingaddon.levelling.enablePvp")
                .define("pvp", true);

        actionsConfig(builder);
        bonusesConfig(builder);

        builder.pop();
    }

    private static void actionsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("If true, given action will yield tool experience.").push("actions");

        enableMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.mining")
                .define("mining", true);

        enableHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.harvesting")
                .define("harvesting", true);

        enableShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.shearing")
                .define("shearing", true);

        enableAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.attacking")
                .define("attacking", true);

        enableTakingDamageXp = builder.comment("Applies to armor only.")
                .translation("config.tinkerslevellingaddon.levelling.actions.takingDamage")
                .define("takingDamage", true);

        enableThornsXp = builder.comment("Applies to armor only. Thorns modifier gives 15% chance per level to gain experience.")
                .translation("config.tinkerslevellingaddon.levelling.actions.thorns")
                .define("thorns", true);

        builder.pop();
    }

    private static void bonusesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Amount of bonus experience for performing actions. If set to 0 there is no bonus experience.").push("bonuses");

        bonusMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.mining")
                .defineInRange("mining", 0, 0, Integer.MAX_VALUE);

        bonusHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.harvesting")
                .defineInRange("harvesting", 0, 0, Integer.MAX_VALUE);

        bonusShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.shearing")
                .defineInRange("shearing", 0, 0, Integer.MAX_VALUE);

        bonusAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.attacking")
                .defineInRange("attacking", 0, 0, Integer.MAX_VALUE);

        bonusTakingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.takingDamage")
                .defineInRange("takingDamage", 0, 0, Integer.MAX_VALUE);

        bonusThornsXp = builder.comment("As with Thorns damage calculations, this is the upper bound of the bonus experience that could be granted.",
                        "For example, for the value of 3 (default), we get: 1 (base xp) + 0 to 3 (bonus xp) = 1 to 4 (result xp)")
                .translation("config.tinkerslevellingaddon.levelling.bonuses.thorns")
                .defineInRange("thorns", 3, 0, Integer.MAX_VALUE);

        builder.pop();
    }

    private static void clientConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Client only settings").push("client");

        enableLevelUpMessage = builder.comment("If true, shows chat message on tool level ups.")
                .translation("config.tinkerslevellingaddon.client.messages")
                .define("message", true);

        levelUpSound = builder.comment("")
                .translation("config.tinkerslevellingaddon.client.sound")
                .defineEnum("sound", LevelUpSound.SNARE_DRUM, EnumGetMethod.NAME_IGNORECASE, LevelUpSound.values());
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, COMMON_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
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

    public enum LevelUpSound {
        NONE(null),
        CHIME(Registration.SOUND_TOOL_LEVEL_UP_CHIME),
        SNARE_DRUM(Registration.SOUND_TOOL_LEVEL_UP_SNARE_DRUM),
        YAY(Registration.SOUND_TOOL_LEVEL_UP_YAY);

        RegistryObject<SoundEvent> soundEvent;

        LevelUpSound(RegistryObject<SoundEvent> soundEvent) {
            this.soundEvent = soundEvent;
        }

        public SoundEvent getSoundEvent() {
            return soundEvent == null ? null : soundEvent.get();
        }
    }
}
