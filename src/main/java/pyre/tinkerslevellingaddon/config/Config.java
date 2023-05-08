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

import static pyre.tinkerslevellingaddon.util.ToolLevellingUtil.*;

public class Config {

    private static final List<String> DEFAULT_TOOLS_SLOTS_ORDER = List.of(UPGRADE, UPGRADE, UPGRADE, ABILITY, UPGRADE);
    private static final List<String> DEFAULT_TOOLS_SLOTS_RANDOM_POOL = List.of(UPGRADE, UPGRADE, UPGRADE, UPGRADE, ABILITY);
    private static final List<String> DEFAULT_RANGED_SLOTS_ORDER = List.of(UPGRADE, UPGRADE, UPGRADE, ABILITY, UPGRADE);
    private static final List<String> DEFAULT_RANGED_SLOTS_RANDOM_POOL = List.of(UPGRADE, UPGRADE, UPGRADE, UPGRADE, ABILITY);
    private static final List<String> DEFAULT_ARMOR_SLOTS_ORDER = List.of(UPGRADE, DEFENSE, UPGRADE, ABILITY, DEFENSE);
    private static final List<String> DEFAULT_ARMOR_SLOTS_RANDOM_POOL = List.of(UPGRADE, UPGRADE, DEFENSE, DEFENSE, ABILITY);

    private static final List<String> DEFAULT_TOOLS_STATS_ORDER = List.of(DURABILITY, ATTACK_DAMAGE, ATTACK_SPEED, MINING_SPEED);
    private static final List<String> DEFAULT_TOOLS_STATS_RANDOM_POOL = List.of(DURABILITY, ATTACK_DAMAGE, ATTACK_SPEED, MINING_SPEED);
    private static final List<String> DEFAULT_RANGED_STATS_ORDER = List.of(DURABILITY, DRAW_SPEED, VELOCITY, ACCURACY, PROJECTILE_DAMAGE);
    private static final List<String> DEFAULT_RANGED_STATS_RANDOM_POOL = List.of(DURABILITY, DRAW_SPEED, VELOCITY, ACCURACY, PROJECTILE_DAMAGE);
    private static final List<String> DEFAULT_ARMOR_STATS_ORDER = List.of(DURABILITY, ARMOR, ARMOR_TOUGHNESS, KNOCKBACK_RESISTANCE);
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
    public static ForgeConfigSpec.EnumValue<GainingMethod> toolsSlotGainingMethod;
    public static ForgeConfigSpec.EnumValue<GainingMethod> toolsStatGainingMethod;
    public static ForgeConfigSpec.EnumValue<GainingMethod> rangedSlotGainingMethod;
    public static ForgeConfigSpec.EnumValue<GainingMethod> rangedStatGainingMethod;
    public static ForgeConfigSpec.EnumValue<GainingMethod> armorSlotGainingMethod;
    public static ForgeConfigSpec.EnumValue<GainingMethod> armorStatGainingMethod;
    public static ForgeConfigSpec.IntValue maxLevel;
    public static ForgeConfigSpec.IntValue baseExperience;
    public static ForgeConfigSpec.DoubleValue requiredXpMultiplier;
    public static ForgeConfigSpec.DoubleValue broadToolRequiredXpMultiplier;

    //general.slots
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsSlotTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsSlotTypeOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rangedSlotTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rangedSlotTypeOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorSlotTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorSlotTypeOrder;

    //general.stats
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsStatTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> toolsStatTypeOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rangedStatTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> rangedStatTypeOrder;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorStatTypeRandomPool;
    private static ForgeConfigSpec.ConfigValue<List<? extends String>> armorStatTypeOrder;

    //general.stats.toolValues
    public static ForgeConfigSpec.IntValue toolDurabilityValue;
    public static ForgeConfigSpec.DoubleValue toolAttackDamageValue;
    public static ForgeConfigSpec.DoubleValue toolAttackSpeedValue;
    public static ForgeConfigSpec.DoubleValue toolMiningSpeedValue;
    
    //general.stats.rangedValues
    public static ForgeConfigSpec.IntValue rangedDurabilityValue;
    public static ForgeConfigSpec.DoubleValue rangedDrawSpeedValue;
    public static ForgeConfigSpec.DoubleValue rangedVelocityValue;
    public static ForgeConfigSpec.DoubleValue rangedAccuracyValue;
    public static ForgeConfigSpec.DoubleValue rangedProjectileDamageValue;
    public static ForgeConfigSpec.DoubleValue rangedAttackDamageValue;
    public static ForgeConfigSpec.DoubleValue rangedAttackSpeedValue;

    //general.stats.armorValues
    public static ForgeConfigSpec.IntValue armorDurabilityValue;
    public static ForgeConfigSpec.DoubleValue armorArmorValue;
    public static ForgeConfigSpec.DoubleValue armorArmorToughnessValue;
    public static ForgeConfigSpec.DoubleValue armorKnockbackResistanceValue;

    //toolLevelling
    public static ForgeConfigSpec.BooleanValue damageDealt;
    public static ForgeConfigSpec.BooleanValue damageTaken;
    public static ForgeConfigSpec.BooleanValue damageBlocked;
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
    public static ForgeConfigSpec.BooleanValue enableShootingXp;
    public static ForgeConfigSpec.BooleanValue enableTakingDamageXp;
    public static ForgeConfigSpec.BooleanValue enableBlockingDamageXp;
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
    public static ForgeConfigSpec.IntValue bonusShootingXp;
    public static ForgeConfigSpec.IntValue bonusTakingDamageXp;
    public static ForgeConfigSpec.IntValue bonusBlockingDamageXp;
    public static ForgeConfigSpec.IntValue bonusThornsXp;

    //CLIENT
    public static ForgeConfigSpec.BooleanValue enableLevelUpMessage;
    public static ForgeConfigSpec.BooleanValue squashLevelPluses;
    public static ForgeConfigSpec.EnumValue<LevelUpSound> levelUpSound;

    private static void generalConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("General addon settings").push("general");

        toolsSlotGainingMethod = builder.comment("Method of gaining modifier slots for tools.")
                .translation("config.tinkerslevellingaddon.general.toolsSlotGainingMethod")
                .defineEnum("toolsSlotGainingMethod", GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());
    
        rangedSlotGainingMethod = builder.comment("Method of gaining modifier slots for ranged weapons.")
                .translation("config.tinkerslevellingaddon.general.ranged_slot_gaining_method")
                .defineEnum("rangedSlotGainingMethod", GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());

        armorSlotGainingMethod = builder.comment("Method of gaining modifier slots for armor.")
                .translation("config.tinkerslevellingaddon.general.armorSlotGainingMethod")
                .defineEnum("armorSlotGainingMethod", GainingMethod.PREDEFINED_ORDER, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());

        slotsConfig(builder);

        toolsStatGainingMethod = builder.comment("Method of gaining stats for tools.")
                .translation("config.tinkerslevellingaddon.general.toolsStatGainingMethod")
                .defineEnum("toolsStatGainingMethod", GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());
    
        rangedStatGainingMethod = builder.comment("Method of gaining stats for tools.")
                .translation("config.tinkerslevellingaddon.general.ranged_stat_gaining_method")
                .defineEnum("rangedStatGainingMethod", GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());

        armorStatGainingMethod = builder.comment("Method of gaining stats for armor.")
                .translation("config.tinkerslevellingaddon.general.armorStatGainingMethod")
                .defineEnum("armorStatGainingMethod", GainingMethod.NONE, EnumGetMethod.NAME_IGNORECASE, GainingMethod.values());

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

        broadToolRequiredXpMultiplier = builder.comment("Additional multiplier for broad tools for experience required to level up.")
                .translation("config.tinkerslevellingaddon.general.broadToolRequiredXpMultiplier")
                .defineInRange("broadToolRequiredXpMultiplier", 3D, 1D, 10D);

        builder.pop();
    }

    private static void slotsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Modifier slots settings").push("slots");

        toolsSlotTypeRandomPool = builder.comment("Set of modifier slot types from which random slot will be awarded when leveling up tools.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_RANDOM_POOL) + "). 80% chance for upgrade and 20% chance for ability.",
                        "Allowed values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.slots.toolsSlotTypeRandomPool")
                .defineList("toolsSlotTypeRandomPool", DEFAULT_TOOLS_SLOTS_RANDOM_POOL, t -> getToolSlotTypes().contains(t));

        toolsSlotTypeOrder = builder.comment("List of modifier slot types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
                        "If empty default order will be used (" + String.join(", ", DEFAULT_TOOLS_SLOTS_ORDER) + ").",
                        "Allowed values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.slots.toolsSlotTypeOrder")
                .defineList("toolsSlotTypeOrder", DEFAULT_TOOLS_SLOTS_ORDER, t -> getToolSlotTypes().contains(t));
    
        rangedSlotTypeRandomPool = builder.comment("Set of modifier slot types from which random slot will be awarded when leveling up ranged weapons.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_RANGED_SLOTS_RANDOM_POOL) + "). 80% chance for upgrade and 20% chance for ability.",
                        "Allowed values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.slots.ranged_slot_type_random_pool")
                .defineList("rangedSlotTypeRandomPool", DEFAULT_RANGED_SLOTS_RANDOM_POOL, t -> getRangedSlotTypes().contains(t));
    
        rangedSlotTypeOrder = builder.comment("List of modifier slot types (in order) that will be awarded when leveling up ranged weapons. If level is higher than list size the mod will start over.",
                        "If empty default order will be used (" + String.join(", ", DEFAULT_RANGED_SLOTS_ORDER) + ").",
                        "Allowed values: " + String.join(", ", getToolSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.slots.ranged_slot_type_order")
                .defineList("rangedSlotTypeOrder", DEFAULT_RANGED_SLOTS_ORDER, t -> getRangedSlotTypes().contains(t));

        armorSlotTypeRandomPool = builder.comment("Set of modifier slot types from which random slot will be awarded when leveling up armor.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_RANDOM_POOL) + ").",
                        "Allowed values: " + String.join(", ", getArmorSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.modifiers.armorModifierTypeRandomPool")
                .defineList("armorSlotTypeRandomPool", DEFAULT_ARMOR_SLOTS_RANDOM_POOL, t -> getArmorSlotTypes().contains(t));

        armorSlotTypeOrder = builder.comment("List of modifier slot types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
                        "If empty default order will be used (" + String.join(", ", DEFAULT_ARMOR_SLOTS_ORDER) + ").",
                        "Allowed values: " + String.join(", ", getArmorSlotTypes()))
                .translation("config.tinkerslevellingaddon.general.slots.armorSlotTypeOrder")
                .defineList("armorSlotTypeOrder", DEFAULT_ARMOR_SLOTS_ORDER, t -> getArmorSlotTypes().contains(t));

        builder.pop();
    }

    private static void statsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Stats settings").push("stats");

        toolsStatTypeRandomPool = builder.comment("Set of stat types from which random stat will be awarded when leveling up tools.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_RANDOM_POOL) + "). 25% chance for every stat.",
                        "Allowed values: " + String.join(", ", getToolStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.toolsStatTypeRandomPool")
                .defineList("toolsStatTypeRandomPool", DEFAULT_TOOLS_STATS_RANDOM_POOL, t -> getToolStatTypes().contains(t));

        toolsStatTypeOrder = builder.comment("List of stat types (in order) that will be awarded when leveling up tools. If level is higher than list size the mod will start over.",
                        "If empty default order will be used (" + String.join(", ", DEFAULT_TOOLS_STATS_ORDER) + ").",
                        "Allowed values: " + String.join(", ", getToolStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.toolsStatTypeOrder")
                .defineList("toolsStatTypeOrder", DEFAULT_TOOLS_STATS_ORDER, t -> getToolStatTypes().contains(t));

        toolStatsValuesConfig(builder);
    
    
        rangedStatTypeRandomPool = builder.comment("Set of stat types from which random stat will be awarded when leveling up ranged weapons.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_RANGED_STATS_RANDOM_POOL) + ").",
                        "Possible values: " + String.join(", ", getRangedStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.ranged_stat_type_random_pool")
                .defineList("rangedStatTypeRandomPool", DEFAULT_RANGED_STATS_RANDOM_POOL, t -> getRangedStatTypes().contains(t));
    
        rangedStatTypeOrder = builder.comment("List of stat types (in order) that will be awarded when leveling up ranged weapons. If level is higher than list size the mod will start over.",
                        "If empty default rotation will be used (" + String.join(", ", DEFAULT_RANGED_STATS_ORDER) + ").",
                        "Possible values: " + String.join(", ", getRangedStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.ranged_stat_type_order")
                .defineList("rangedStatTypeRotation", DEFAULT_RANGED_STATS_ORDER, t -> getRangedStatTypes().contains(t));
    
        rangedStatsValuesConfig(builder);

        armorStatTypeRandomPool = builder.comment("Set of stat types from which random stat will be awarded when leveling up armor.",
                        "If empty default pool will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_RANDOM_POOL) + ").",
                        "Allowed values: " + String.join(", ", getArmorStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.armorStatTypeRandomPool")
                .defineList("armorStatTypeRandomPool", DEFAULT_ARMOR_STATS_RANDOM_POOL, t -> getArmorStatTypes().contains(t));

        armorStatTypeOrder = builder.comment("List of stat types (in order) that will be awarded when leveling up armor. If level is higher than list size the mod will start over.",
                        "If empty default order will be used (" + String.join(", ", DEFAULT_ARMOR_STATS_ORDER) + ").",
                        "Allowed values: " + String.join(", ", getArmorStatTypes()))
                .translation("config.tinkerslevellingaddon.general.stats.armorStatTypeOrder")
                .defineList("armorStatTypeOrder", DEFAULT_ARMOR_STATS_ORDER, t -> getArmorStatTypes().contains(t));

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
    
    private static void rangedStatsValuesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Ranged weapons stat values rewarded on level ups").push("rangedValues");
        
        rangedDurabilityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.durability")
                .defineInRange(DURABILITY, 30, 1, 1000);
    
        rangedDrawSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stat.draw_speed")
                .defineInRange(DRAW_SPEED, 0.1D, 0.1D, 10D);
    
        rangedVelocityValue = builder.translation("tooltip.tinkerslevellingaddon.stat.velocity")
                .defineInRange(VELOCITY, 0.1D, 0.1D, 10D);
    
        rangedAccuracyValue = builder.translation("tooltip.tinkerslevellingaddon.stat.accuracy")
                .defineInRange(ACCURACY, 0.01D, 0.01D, 1D);
    
        rangedProjectileDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stat.projectile_damage")
                .defineInRange(PROJECTILE_DAMAGE, 0.1D, 0.1D, 10D);
    
        rangedAttackDamageValue = builder.translation("tooltip.tinkerslevellingaddon.stat.attack_damage")
                .defineInRange(ATTACK_DAMAGE, 0.5D, 0.1D, 10D);
    
        rangedAttackSpeedValue = builder.translation("tooltip.tinkerslevellingaddon.stat.attack_speed")
                .defineInRange(ATTACK_SPEED, 0.25D, 0.1D, 10D);
        
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

        damageDealt = builder.comment("Melee only!", "If true, base experience value gained for attacking is equal to damage dealt (rounded to whole number), otherwise 1.")
                .translation("config.tinkerslevellingaddon.levelling.damageDealt")
                .define("damageDealt", true);
        damageTaken = builder.comment("If true, base experience value gained for taking damage is equal to damage taken (rounded to whole number), otherwise 1.")
                .translation("config.tinkerslevellingaddon.levelling.damageTaken")
                .define("damageTaken", true);
        damageBlocked = builder.comment("If true, base experience value gained for blocking damage is equal to damage blocked (rounded to whole number), otherwise 1.")
                .translation("config.tinkerslevellingaddon.levelling.damageBlocked")
                .define("damageBlocked", true);
        enablePvp = builder.comment("If true, allows to gain experience from dealing damage to or taking damage from other players.")
                .translation("config.tinkerslevellingaddon.levelling.enablePvp")
                .define("pvp", true);

        actionsConfig(builder);
        bonusesConfig(builder);

        builder.pop();
    }

    private static void actionsConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("If true, given action will yield tool experience.").push("actions");

        enableMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.mining")
                .define("mining", true);

        enableHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.harvesting")
                .define("harvesting", true);

        enableShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.shearing")
                .define("shearing", true);

        enableStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.stripping")
                .define("stripping", true);

        enableScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.scrapping")
                .define("scrapping", true);

        enableWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.waxingOff")
                .define("waxingOff", true);

        enableTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.tilling")
                .define("tilling", true);

        enablePathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.pathMaking")
                .define("pathMaking", true);

        enableAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.attacking")
                .define("attacking", true);

        enableShootingXp = builder.comment("Applies to ranged weapons like longbows and crossbows.")
                .translation("config.tinkerslevellingaddon.levelling.shooting")
                .define("shooting", true);

        enableTakingDamageXp = builder.comment("Applies to armor only.")
                .translation("config.tinkerslevellingaddon.levelling.takingDamage")
                .define("takingDamage", true);
        
        enableBlockingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.blockingDamage")
                .define("blockingDamage", true);

        enableThornsXp = builder.comment("Applies to armor only. Thorns modifier gives 15% chance per level to gain experience.")
                .translation("config.tinkerslevellingaddon.levelling.thorns")
                .define("thorns", true);

        builder.pop();
    }

    private static void bonusesConfig(ForgeConfigSpec.Builder builder) {
        builder.comment("Amount of bonus experience for performing actions. If set to 0 there is no bonus experience.").push("bonuses");

        bonusMiningXp = builder.translation("config.tinkerslevellingaddon.levelling.mining")
                .defineInRange("mining", 0, 0, Integer.MAX_VALUE);

        bonusHarvestingXp = builder.translation("config.tinkerslevellingaddon.levelling.harvesting")
                .defineInRange("harvesting", 0, 0, Integer.MAX_VALUE);

        bonusShearingXp = builder.translation("config.tinkerslevellingaddon.levelling.shearing")
                .defineInRange("shearing", 0, 0, Integer.MAX_VALUE);

        bonusStrippingXp = builder.translation("config.tinkerslevellingaddon.levelling.stripping")
                .defineInRange("stripping", 0, 0, Integer.MAX_VALUE);

        bonusScrappingXp = builder.translation("config.tinkerslevellingaddon.levelling.scrapping")
                .defineInRange("scrapping", 0, 0, Integer.MAX_VALUE);

        bonusWaxingOffXp = builder.translation("config.tinkerslevellingaddon.levelling.waxingOff")
                .defineInRange("waxingOff", 0, 0, Integer.MAX_VALUE);

        bonusTillingXp = builder.translation("config.tinkerslevellingaddon.levelling.tilling")
                .defineInRange("tilling", 0, 0, Integer.MAX_VALUE);

        bonusPathMakingXp = builder.translation("config.tinkerslevellingaddon.levelling.pathMaking")
                .defineInRange("pathMaking", 0, 0, Integer.MAX_VALUE);

        bonusAttackingXp = builder.translation("config.tinkerslevellingaddon.levelling.attacking")
                .defineInRange("attacking", 0, 0, Integer.MAX_VALUE);

        bonusShootingXp = builder.translation("config.tinkerslevellingaddon.levelling.shooting")
                .defineInRange("shooting", 0, 0, Integer.MAX_VALUE);

        bonusTakingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.takingDamage")
                .defineInRange("takingDamage", 0, 0, Integer.MAX_VALUE);
        
        bonusBlockingDamageXp = builder.translation("config.tinkerslevellingaddon.levelling.blockingDamage")
                .defineInRange("blockingDamage", 0, 0, Integer.MAX_VALUE);

        bonusThornsXp = builder.comment("As with Thorns damage calculations, this is the upper bound of the bonus experience that could be granted.",
                        "For example, for the value of 3 (default), we get: 1 (base xp) + 0 to 3 (bonus xp) = 1 to 4 (result xp)")
                .translation("config.tinkerslevellingaddon.levelling.thorns")
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

    public static List<String> getToolsSlotsOrder() {
        List<? extends String> slotsOrder = toolsSlotTypeOrder.get();
        if (slotsOrder.isEmpty()) {
            slotsOrder = DEFAULT_TOOLS_SLOTS_ORDER;
        }
        return (List<String>) slotsOrder;
    }

    public static List<String> getToolsSlotsRandomPool() {
        List<? extends String> slotsRandomPool = toolsSlotTypeRandomPool.get();
        if (slotsRandomPool.isEmpty()) {
            slotsRandomPool = DEFAULT_TOOLS_SLOTS_RANDOM_POOL;
        }
        return (List<String>) slotsRandomPool;
    }

    public static List<String> getToolsStatsOrder() {
        List<? extends String> statsOrder = toolsStatTypeOrder.get();
        if (statsOrder.isEmpty()) {
            statsOrder = DEFAULT_TOOLS_STATS_ORDER;
        }
        return (List<String>) statsOrder;
    }

    public static List<String> getToolsStatsRandomPool() {
        List<? extends String> statsRandomPool = toolsStatTypeRandomPool.get();
        if (statsRandomPool.isEmpty()) {
            statsRandomPool = DEFAULT_TOOLS_STATS_RANDOM_POOL;
        }
        return (List<String>) statsRandomPool;
    }

    public static List<String> getArmorSlotsOrder() {
        List<? extends String> slotsOrder = armorSlotTypeOrder.get();
        if (slotsOrder.isEmpty()) {
            slotsOrder = DEFAULT_ARMOR_SLOTS_ORDER;
        }
        return (List<String>) slotsOrder;
    }

    public static List<String> getArmorSlotsRandomPool() {
        List<? extends String> slotsRandomPool = armorSlotTypeRandomPool.get();
        if (slotsRandomPool.isEmpty()) {
            slotsRandomPool = DEFAULT_ARMOR_SLOTS_RANDOM_POOL;
        }
        return (List<String>) slotsRandomPool;
    }

    public static List<String> getArmorStatsOrder() {
        List<? extends String> statsOrder = armorStatTypeOrder.get();
        if (statsOrder.isEmpty()) {
            statsOrder = DEFAULT_ARMOR_STATS_ORDER;
        }
        return (List<String>) statsOrder;
    }

    public static List<String> getArmorStatsRandomPool() {
        List<? extends String> statsRandomPool = armorStatTypeRandomPool.get();
        if (statsRandomPool.isEmpty()) {
            statsRandomPool = DEFAULT_ARMOR_STATS_RANDOM_POOL;
        }
        return (List<String>) statsRandomPool;
    }

    public static List<String> getRangedStatsOrder() {
        List<? extends String> statsOrder = rangedStatTypeOrder.get();
        if (statsOrder.isEmpty()) {
            statsOrder = DEFAULT_RANGED_STATS_ORDER;
        }
        return (List<String>) statsOrder;
    }

    public static List<String> getRangedStatsRandomPool() {
        List<? extends String> statsRandomPool = rangedStatTypeRandomPool.get();
        if (statsRandomPool.isEmpty()) {
            statsRandomPool = DEFAULT_RANGED_STATS_RANDOM_POOL;
        }
        return (List<String>) statsRandomPool;
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
    
    public static double getRangedStatValue(FloatToolStat stat) {
        if (stat.equals(ToolStats.DRAW_SPEED)) {
            return rangedDrawSpeedValue.get();
        }
        if (stat.equals(ToolStats.VELOCITY)) {
            return rangedVelocityValue.get();
        }
        if (stat.equals(ToolStats.ACCURACY)) {
            return rangedAccuracyValue.get();
        }
        if (stat.equals(ToolStats.PROJECTILE_DAMAGE)) {
            return rangedProjectileDamageValue.get();
        }
        if (stat.equals(ToolStats.DURABILITY)) {
            return rangedDurabilityValue.get();
        }
        if (stat.equals(ToolStats.ATTACK_DAMAGE)) {
            return rangedAttackDamageValue.get();
        }
        if (stat.equals(ToolStats.ATTACK_SPEED)) {
            return rangedAttackSpeedValue.get();
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

    public enum GainingMethod {
        NONE,
        PREDEFINED_ORDER,
        RANDOM
    }
}
