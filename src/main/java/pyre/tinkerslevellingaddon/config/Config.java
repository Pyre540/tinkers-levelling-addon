package pyre.tinkerslevellingaddon.config;

import com.electronwill.nightconfig.core.EnumGetMethod;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.registries.RegistryObject;
import pyre.tinkerslevellingaddon.setup.Registration;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ToolStats;

import java.util.List;

import static pyre.tinkerslevellingaddon.util.SlotAndStatUtil.*;

public class Config {

    private static final List<String> DEFAULT_TOOLS_SLOTS_ROTATION = List.of(UPGRADE, UPGRADE, UPGRADE, ABILITY, UPGRADE);
    private static final List<String> DEFAULT_TOOLS_SLOTS_RANDOM_POOL = List.of(UPGRADE, UPGRADE, UPGRADE, UPGRADE, ABILITY);
    private static final List<String> DEFAULT_ARMOR_SLOTS_ROTATION = List.of(UPGRADE, DEFENSE, UPGRADE, ABILITY, DEFENSE);
    private static final List<String> DEFAULT_ARMOR_SLOTS_RANDOM_POOL = List.of(UPGRADE, UPGRADE, DEFENSE, DEFENSE, ABILITY);

    private static final List<String> DEFAULT_TOOLS_STATS_ROTATION = List.of(DURABILITY, ATTACK_DAMAGE, ATTACK_SPEED, MINING_SPEED);
    private static final List<String> DEFAULT_TOOLS_STATS_RANDOM_POOL = List.of(DURABILITY, ATTACK_DAMAGE, ATTACK_SPEED, MINING_SPEED);
    private static final List<String> DEFAULT_ARMOR_STATS_ROTATION = List.of(DURABILITY, ARMOR, ARMOR_TOUGHNESS, KNOCKBACK_RESISTANCE);
    private static final List<String> DEFAULT_ARMOR_STATS_RANDOM_POOL = List.of(DURABILITY, ARMOR, ARMOR_TOUGHNESS, KNOCKBACK_RESISTANCE);

    public static final ForgeConfigSpec SERVER_CONFIG;
    public static final ForgeConfigSpec CLIENT_CONFIG;

    static {
        ForgeConfigSpec.Builder serverConfigBuilder = new ForgeConfigSpec.Builder();
        generalConfig(serverConfigBuilder);
        toolLevellingConfig(serverConfigBuilder);
        SERVER_CONFIG = serverConfigBuilder.build();
        ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
        clientConfig(clientConfigBuilder);
        CLIENT_CONFIG = clientConfigBuilder.build();
    }

    //SERVER
    //General
    public static ForgeConfigSpec.BooleanValue enableModifierSlots;
    public static ForgeConfigSpec.BooleanValue enableStats;
    public static ForgeConfigSpec.IntValue maxLevel;
    public static ForgeConfigSpec.IntValue baseExperience;
    public static ForgeConfigSpec.DoubleValue requiredXpMultiplier;
    public static ForgeConfigSpec.DoubleValue broadToolRequiredXpMultiplier;

    //general.modifiers
    public static ForgeConfigSpec.BooleanValue toolsModifierTypeRandomOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsModifierTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsModifierTypeRotation;
    public static ForgeConfigSpec.BooleanValue armorModifierTypeRandomOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorModifierTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorModifierTypeRotation;

    //general.stats
    public static ForgeConfigSpec.BooleanValue toolsStatTypeRandomOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsStatTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsStatTypeRotation;
    public static ForgeConfigSpec.BooleanValue armorStatTypeRandomOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorStatTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorStatTypeRotation;

    //general.stats.toolValues
    public static ForgeConfigSpec.IntValue toolDurabilityValue;
    public static ForgeConfigSpec.DoubleValue toolAttackDamageValue;
    public static ForgeConfigSpec.DoubleValue toolAttackSpeedValue;
    public static ForgeConfigSpec.DoubleValue toolMiningSpeedValue;

    //general.stats.armorValues
    public static ForgeConfigSpec.IntValue armorDurabilityValue;
    public static ForgeConfigSpec.DoubleValue armorArmorValue;
    public static ForgeConfigSpec.DoubleValue armorArmorToughnessValue;
    public static ForgeConfigSpec.DoubleValue armorKnockbackResistanceValue;

    //toolLevelling
    public static ForgeConfigSpec.BooleanValue damageDealt;
    public static ForgeConfigSpec.BooleanValue damageTaken;
    public static ForgeConfigSpec.BooleanValue enablePvp;

    //toolLevelling.actions
    public static ForgeConfigSpec.BooleanValue enableMiningXp;
    public static ForgeConfigSpec.BooleanValue enableHarvestingXp;
    public static ForgeConfigSpec.BooleanValue enableShearingXp;
    public static ForgeConfigSpec.BooleanValue enableStrippingXp;
    public static ForgeConfigSpec.BooleanValue enableScrappingXp;
    public static ForgeConfigSpec.BooleanValue enableWaxingOffXp;
    public static ForgeConfigSpec.BooleanValue enableTillingXp;
    public static ForgeConfigSpec.BooleanValue enablePathMakingXp;
    public static ForgeConfigSpec.BooleanValue enableAttackingXp;
    public static ForgeConfigSpec.BooleanValue enableTakingDamageXp;
    public static ForgeConfigSpec.BooleanValue enableThornsXp;

    //toolLevelling.bonuses
    public static ForgeConfigSpec.IntValue bonusMiningXp;
    public static ForgeConfigSpec.IntValue bonusHarvestingXp;
    public static ForgeConfigSpec.IntValue bonusShearingXp;
    public static ForgeConfigSpec.IntValue bonusStrippingXp;
    public static ForgeConfigSpec.IntValue bonusScrappingXp;
    public static ForgeConfigSpec.IntValue bonusWaxingOffXp;
    public static ForgeConfigSpec.IntValue bonusTillingXp;
    public static ForgeConfigSpec.IntValue bonusPathMakingXp;
    public static ForgeConfigSpec.IntValue bonusAttackingXp;
    public static ForgeConfigSpec.IntValue bonusTakingDamageXp;
    public static ForgeConfigSpec.IntValue bonusThornsXp;

    //CLIENT
    public static ForgeConfigSpec.BooleanValue enableLevelUpMessage;
    public static ForgeConfigSpec.BooleanValue squashLevelPluses;
    public static ForgeConfigSpec.EnumValue<LevelUpSound> levelUpSound;

    private static void generalConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("General addon settings").push("general");

        enableModifierSlots = builder.comment("If true, modifier slots will be rewarded on level ups.")
                .translation("config.tinkerslevellingaddon.general.enableModifierSlots")
                .define("enableModifierSlots", true);

        modifiersConfig(builder);

        enableStats = builder.comment("If true, raw stats will be rewarded on level ups.")
                .translation("config.tinkerslevellingaddon.general.enableStats")
                .define("enableStats", false);

        statsConfig(builder);

        maxLevel = builder.comment("Maximum tool level that could be achieved. If set to 0 there is no upper limit.")
                .translation("config.tinkerslevellingaddon.general.maxLevel")
                .defineInRange("maxLevel", 5, 0, Integer.MAX_VALUE);

        baseExperience = builder.comment("Base amount of experience required to reach next level.")
                .translation("config.tinkerslevellingaddon.general.baseExperience")
                .defineInRange("baseExperience", 500, 1, Integer.MAX_VALUE);

        requiredXpMultiplier = builder.comment("How much the amount of experience required to reach next level will be multiplied per level.")
                .translation("config.tinkerslevellingaddon.general.requiredXpMultiplier")
                .defineInRange("requiredXpMultiplier", 2D, 1D, 10D);

        broadToolRequiredXpMultiplier = builder.comment("Additional modifier for broad tools for experience required to level up.")
                .translation("config.tinkerslevellingaddon.general.broadToolRequiredXpMultiplier")
                .defineInRange("broadToolRequiredXpMultiplier", 3D, 1D, 10D);

        builder.pop();
    }

    private static void modifiersConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Modifiers settings").push("modifiers");

        toolsModifierTypeRandomOrder = builder.comment("If true, instead of defined rotation order, tool modifiers will be awarded randomly on level ups.")
                .translation("config.tinkerslevellingaddon.general.modifiers.toolsModifierTypeRandomOrder")
                .define("toolsModifierTypeRandomOrder", false);

        toolsModifierTypeRandomPool = builder.comment("Set of modifier slot types from which random modifier will be awarded when leveling up tools.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_RANDOM_POOL) + "). 80% chance for upgrade and 20% chance for ability.",
                        "Possible values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.modifiers.toolsModifierTypeRandomPool")
                .defineList("toolsModifierTypeRandomPool", DEFAULT_TOOLS_SLOTS_RANDOM_POOL, t -> getToolSlotTypes().contains(t));

        toolsModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.modifiers.toolsModifierTypeRotation")
                .defineList("toolsModifierTypeRotation", DEFAULT_TOOLS_SLOTS_ROTATION, t -> getToolSlotTypes().contains(t));

        armorModifierTypeRandomOrder = builder.comment("If true, instead of defined rotation order, armor modifiers will be awarded randomly on level ups.")
                .translation("config.tinkerslevellingaddon.general.modifiers.armorModifierTypeRandomOrder")
                .define("armorModifierTypeRandomOrder", false);

        armorModifierTypeRandomPool = builder.comment("Set of stat types from which random modifier will be awarded when leveling up armor.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", getArmorSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.modifiers.armorModifierTypeRandomPool")
                .defineList("armorModifierTypeRandomPool", DEFAULT_ARMOR_SLOTS_ROTATION, t -> getArmorSlotTypes().contains(t));

        armorModifierTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", getArmorSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.modifiers.armorModifierTypeRotation")
                .defineList("armorModifierTypeRotation", DEFAULT_ARMOR_SLOTS_ROTATION, t -> getArmorSlotTypes().contains(t));

        builder.pop();
    }

    private static void statsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Stats settings").push("stats");

        toolsStatTypeRandomOrder = builder.comment("If true, instead of defined rotation order, tool stats will be awarded randomly on level ups.")
                .translation("config.tinkerslevellingaddon.general.stats.toolsStatTypeRandomOrder")
                .define("toolsStatTypeRandomOrder", false);

        toolsStatTypeRandomPool = builder.comment("Set of stat types from which random stat will be awarded when leveling up tools.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_RANDOM_POOL) + "). 25% chance for every stat.",
                        "Possible values: " + String.join(", ", getToolStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.toolsStatTypeRandomPool")
                .defineList("toolsStatTypeRandomPool", DEFAULT_TOOLS_STATS_RANDOM_POOL, t -> getToolStatTypes().contains(t));

        toolsStatTypeRotation = builder.comment("List of slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", getToolStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.toolsStatTypeRotation")
                .defineList("toolsStatTypeRotation", DEFAULT_TOOLS_STATS_ROTATION, t -> getToolStatTypes().contains(t));

        toolStatsValuesConfig(builder);

        armorStatTypeRandomOrder = builder.comment("If true, instead of defined rotation order, armor modifiers will be awarded randomly on level ups.")
                .translation("config.tinkerslevellingaddon.general.stats.armorStatTypeRandomOrder")
                .define("armorStatTypeRandomOrder", false);

        armorStatTypeRandomPool = builder.comment("Set of stat types from which random modifier will be awarded when leveling up armor.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_RANDOM_POOL) + ").",
                        "Possible values: " + String.join(", ", getArmorStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.armorStatTypeRandomPool")
                .defineList("armorStatTypeRandomPool", DEFAULT_ARMOR_STATS_RANDOM_POOL, t -> getArmorStatTypes().contains(t));

        armorStatTypeRotation = builder.comment("List of stat types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_ROTATION) + ").",
                        "Possible values: " + String.join(", ", getArmorStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.armorStatTypeRotation")
                .defineList("armorStatTypeRotation", DEFAULT_ARMOR_STATS_ROTATION, t -> getArmorStatTypes().contains(t));

        armorStatsValuesConfig(builder);

        builder.pop();
    }

    private static void toolStatsValuesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Tool stat values rewarded on level ups").push("toolValues");

        toolDurabilityValue = builder.translation("config.tinkerslevellingaddon.general.stats.toolValues.durability")
                .defineInRange(DURABILITY, 50, 1, 1000);

        toolAttackDamageValue = builder.translation("config.tinkerslevellingaddon.general.stats.toolValues.attackDamage")
                .defineInRange(ATTACK_DAMAGE, 0.5D, 0.1D, 10D);

        toolAttackSpeedValue = builder.translation("config.tinkerslevellingaddon.general.stats.toolValues.attackSpeed")
                .defineInRange(ATTACK_SPEED, 0.25D, 0.1D, 10D);

        toolMiningSpeedValue = builder.translation("config.tinkerslevellingaddon.general.stats.toolValues.miningSpeed")
                .defineInRange(MINING_SPEED, 1D, 0.1D, 10D);

        builder.pop();
    }

    private static void armorStatsValuesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Armor stat values rewarded on level ups").push("armorValues");

        armorDurabilityValue = builder.translation("config.tinkerslevellingaddon.general.stats.armorValues.durability")
                .defineInRange(DURABILITY, 50, 1, 1000);

        armorArmorValue = builder.translation("config.tinkerslevellingaddon.general.stats.armorValues.armor")
                .defineInRange(ARMOR, 0.25D, 0.1D, 10D);

        armorArmorToughnessValue = builder.translation("config.tinkerslevellingaddon.general.stats.armorValues.armorToughness")
                .defineInRange(ARMOR_TOUGHNESS, 0.1D, 0.1D, 10D);

        armorKnockbackResistanceValue = builder.translation("config.tinkerslevellingaddon.general.stats.armorValues.knockbackResistance")
                .defineInRange(KNOCKBACK_RESISTANCE, 0.1D, 0.1D, 1D);

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

        enableStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.stripping")
                .define("stripping", true);

        enableScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.scrapping")
                .define("scrapping", true);

        enableWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.waxingOff")
                .define("waxingOff", true);

        enableTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.tilling")
                .define("tilling", true);

        enablePathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.actions.pathMaking")
                .define("pathMaking", true);

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

        bonusStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.stripping")
                .defineInRange("stripping", 0, 0, Integer.MAX_VALUE);

        bonusScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.scrapping")
                .defineInRange("scrapping", 0, 0, Integer.MAX_VALUE);

        bonusWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.waxingOff")
                .defineInRange("waxingOff", 0, 0, Integer.MAX_VALUE);

        bonusTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.tilling")
                .defineInRange("tilling", 0, 0, Integer.MAX_VALUE);

        bonusPathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.bonuses.pathMaking")
                .defineInRange("pathMaking", 0, 0, Integer.MAX_VALUE);

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

        squashLevelPluses = builder.comment("If true, uses alternative level name suffix for high level tools.",
                "Instead of appending '+' signs for each consecutive level appearance, +1, +2, +3, etc. notation will be used.")
                .translation("config.tinkerslevellingaddon.client.squashPluses")
                .define("squashPluses", false);

        levelUpSound = builder.comment("")
                .translation("config.tinkerslevellingaddon.client.sound")
                .defineEnum("sound", LevelUpSound.SNARE_DRUM, EnumGetMethod.NAME_IGNORECASE, LevelUpSound.values());
    }

    public static void init() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG);
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, CLIENT_CONFIG);
    }

    public static List<String> getToolsSlotsRotation() {
        List<? extends String> toolsRotation = toolsModifierTypeRotation.get();
        if (toolsRotation.isEmpty()) {
            toolsRotation = DEFAULT_TOOLS_SLOTS_ROTATION;
        }
        return (List<String>) toolsRotation;
    }

    public static List<String> getToolsSlotsRandomPool() {
        List<? extends String> toolsRandomPool = toolsModifierTypeRandomPool.get();
        if (toolsRandomPool.isEmpty()) {
            toolsRandomPool = DEFAULT_TOOLS_SLOTS_RANDOM_POOL;
        }
        return (List<String>) toolsRandomPool;
    }

    public static List<String> getToolsStatsRotation() {
        List<? extends String> toolsRotation = toolsStatTypeRotation.get();
        if (toolsRotation.isEmpty()) {
            toolsRotation = DEFAULT_TOOLS_STATS_ROTATION;
        }
        return (List<String>) toolsRotation;
    }

    public static List<String> getToolsStatsRandomPool() {
        List<? extends String> toolsRandomPool = toolsStatTypeRandomPool.get();
        if (toolsRandomPool.isEmpty()) {
            toolsRandomPool = DEFAULT_TOOLS_STATS_RANDOM_POOL;
        }
        return (List<String>) toolsRandomPool;
    }

    public static List<String> getArmorSlotsRotation() {
        List<? extends String> armorRotation = armorModifierTypeRotation.get();
        if (armorRotation.isEmpty()) {
            armorRotation = DEFAULT_ARMOR_SLOTS_ROTATION;
        }
        return (List<String>) armorRotation;
    }

    public static List<String> getArmorSlotsRandomPool() {
        List<? extends String> armorRandomPool = armorModifierTypeRandomPool.get();
        if (armorRandomPool.isEmpty()) {
            armorRandomPool = DEFAULT_ARMOR_SLOTS_RANDOM_POOL;
        }
        return (List<String>) armorRandomPool;
    }

    public static List<String> getArmorStatsRotation() {
        List<? extends String> armorRotation = armorStatTypeRotation.get();
        if (armorRotation.isEmpty()) {
            armorRotation = DEFAULT_ARMOR_STATS_ROTATION;
        }
        return (List<String>) armorRotation;
    }

    public static List<String> getArmorStatsRandomPool() {
        List<? extends String> armorRandomPool = armorStatTypeRandomPool.get();
        if (armorRandomPool.isEmpty()) {
            armorRandomPool = DEFAULT_ARMOR_STATS_RANDOM_POOL;
        }
        return (List<String>) armorRandomPool;
    }

    public static double getToolStatValue(FloatToolStat stat) {
        if (stat.equals(ToolStats.DURABILITY)) {
            return toolDurabilityValue.get();
        }
        if (stat.equals(ToolStats.ATTACK_DAMAGE)) {
            return toolAttackDamageValue.get();
        }
        if (stat.equals(ToolStats.ATTACK_SPEED)) {
            return toolAttackSpeedValue.get();
        }
        if (stat.equals(ToolStats.MINING_SPEED)) {
            return toolMiningSpeedValue.get();
        }
        return 0;
    }

    public static double getArmorStatValue(FloatToolStat stat) {
        if (stat.equals(ToolStats.DURABILITY)) {
            return armorDurabilityValue.get();
        }
        if (stat.equals(ToolStats.ARMOR)) {
            return armorArmorValue.get();
        }
        if (stat.equals(ToolStats.ARMOR_TOUGHNESS)) {
            return armorArmorToughnessValue.get();
        }
        if (stat.equals(ToolStats.KNOCKBACK_RESISTANCE)) {
            return armorKnockbackResistanceValue.get();
        }
        return 0;
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
