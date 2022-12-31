package pyre.tinkerslevellingaddon;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemUseContext;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.server.ServerWorld;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.network.LevelUpPacket;
import pyre.tinkerslevellingaddon.network.Messages;
import pyre.tinkerslevellingaddon.util.SlotAndStatUtil;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.SingleUseModifier;
import slimeknights.tconstruct.library.modifiers.hooks.IHarvestModifier;
import slimeknights.tconstruct.library.modifiers.hooks.IShearModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.ToolDefinition;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IModifierToolStack;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.ToolDefinitions;

import javax.annotation.Nullable;
import java.util.*;

import static pyre.tinkerslevellingaddon.util.SlotAndStatUtil.parseSlotsHistory;
import static pyre.tinkerslevellingaddon.util.SlotAndStatUtil.parseStatsHistory;

public class ImprovableModifier extends SingleUseModifier implements IHarvestModifier, IShearModifier {

    public static final ResourceLocation EXPERIENCE_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "experience");
    public static final ResourceLocation LEVEL_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "level");
    public static final ResourceLocation MODIFIER_HISTORY_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "modifier_history");
    public static final ResourceLocation STAT_HISTORY_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "stat_history");

    private static final Set<ToolDefinition> BROAD_TOOLS = new HashSet<>(Arrays.asList(ToolDefinitions.SLEDGE_HAMMER,
            ToolDefinitions.VEIN_HAMMER, ToolDefinitions.EXCAVATOR, ToolDefinitions.BROAD_AXE, ToolDefinitions.SCYTHE,
            ToolDefinitions.CLEAVER));

    public ImprovableModifier() {
        super(9337340);
    }

    @Override
    public void beforeRemoved(IModifierToolStack tool, RestrictedCompoundTag tag) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(MODIFIER_HISTORY_KEY);
        tool.getPersistentData().remove(STAT_HISTORY_KEY);
    }

    @Override
    public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
        if (Config.enableModifierSlots.get()) {
            List<SlotType> slots = parseSlotsHistory(context.getPersistentData().getString(MODIFIER_HISTORY_KEY));
            for (SlotType slot : slots) {
                volatileData.addSlots(slot, 1);
            }
        }
    }

    @Override
    public void addToolStats(ToolRebuildContext context, int level, ModifierStatsBuilder builder) {
        if (Config.enableStats.get()) {
            List<FloatToolStat> stats = parseStatsHistory(context.getPersistentData().getString(STAT_HISTORY_KEY));
            boolean isArmor = context.hasTag(TinkerTags.Items.ARMOR);
            for (FloatToolStat stat : stats) {
                stat.add(builder, isArmor ? Config.getArmorStatValue(stat) : Config.getToolStatValue(stat));
            }
        }
    }

    @Override
    public void afterBlockBreak(IModifierToolStack tool, int level, ToolHarvestContext context) {
        ServerPlayerEntity player = context.getPlayer();
        if (!Config.enableMiningXp.get() || !context.isEffective() || player == null) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, Hand.MAIN_HAND);
        if (!isEqualTinkersItem(tool, toolStack)) {
            toolStack = getHeldTool(player, Hand.OFF_HAND);
        }
        addExperience(toolStack, 1 + Config.bonusMiningXp.get(), player);
    }

    @Override
    public void afterHarvest(IModifierToolStack tool, int level, ItemUseContext context, ServerWorld world,
                             BlockState state, BlockPos pos) {
        if (!Config.enableHarvestingXp.get() || !(context.getPlayer() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayer();
        ToolStack toolStack = getHeldTool(player, context.getHand());
        addExperience(toolStack, 1 + Config.bonusHarvestingXp.get(), player);
    }

    @Override
    public void afterShearEntity(IModifierToolStack tool, int level, PlayerEntity player, Entity entity, boolean isTarget) {
        if (!Config.enableShearingXp.get() || !(player instanceof ServerPlayerEntity)) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, Hand.MAIN_HAND);
        if (!isEqualTinkersItem(tool, toolStack)) {
            toolStack = getHeldTool(player, Hand.OFF_HAND);
        }
        addExperience(toolStack, 1 + Config.bonusShearingXp.get(), (ServerPlayerEntity) player);
    }

    @Override
    public int afterEntityHit(IModifierToolStack tool, int level, ToolAttackContext context, float damageDealt) {
        if (!Config.enableAttackingXp.get() || !(context.getPlayerAttacker() instanceof ServerPlayerEntity) ||
                (!Config.enablePvp.get() && context.getLivingTarget() instanceof PlayerEntity) || context.getLivingTarget() == null) {
            return 0;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getPlayerAttacker();
        int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + Config.bonusAttackingXp.get();
        ToolStack toolStack = getHeldTool(context.getPlayerAttacker(), context.getSlotType());
        addExperience(toolStack, xp, player);
        return 0;
    }

    @Override
    public void onAttacked(IModifierToolStack tool, int level, EquipmentContext context, EquipmentSlotType slotType,
                           DamageSource source, float amount, boolean isDirectDamage) {
        if (!Config.enableTakingDamageXp.get() || slotType.getType() != EquipmentSlotType.Group.ARMOR || !isDirectDamage ||
                !(context.getEntity() instanceof ServerPlayerEntity)) {
            return;
        }
        ServerPlayerEntity player = (ServerPlayerEntity) context.getEntity();
        if (player.invulnerableTime > 10 || !isValidDamageSource(source, player)) {
            return;
        }
        int xp = (Config.damageTaken.get() ? Math.round(amount) : 1) + Config.bonusTakingDamageXp.get() + getThornsBonus(tool);
        addExperience(getHeldTool(player, slotType), xp, player);
    }

    //currently no hooks for tilling, striping wood, making paths...

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T> T getModule(Class<T> type) {
        if (type == IHarvestModifier.class || type == IShearModifier.class) {
            return (T) this;
        }
        return null;
    }

    private void addExperience(ToolStack tool, int amount, ServerPlayerEntity player) {
        if (tool == null) {
            return;
        }

        ModDataNBT data = tool.getPersistentData();
        int currentLevel = data.getInt(LEVEL_KEY);
        int currentExperience = data.getInt(EXPERIENCE_KEY) + amount;
        boolean isBroadTool = isBroadTool(tool);
        int experienceNeeded = getXpNeededForLevel(currentLevel + 1, isBroadTool);

        while (currentExperience >= experienceNeeded) {
            if (!canLevelUp(currentLevel)) {
                return;
            }
            data.putInt(LEVEL_KEY, ++currentLevel);
            currentExperience -= experienceNeeded;
            experienceNeeded = getXpNeededForLevel(currentLevel + 1, isBroadTool);

            boolean isArmor = tool.hasTag(TinkerTags.Items.ARMOR);
            if (Config.enableModifierSlots.get()) {
                String slotName = getSlotName(currentLevel, isArmor);
                appendHistory(MODIFIER_HISTORY_KEY, slotName, data);
            }
            if (Config.enableStats.get()) {
                String statName = getStatName(currentLevel, isArmor);
                appendHistory(STAT_HISTORY_KEY, statName, data);
            }

            //temporarily set xp to 0, so it displays nicely in chat message
            data.putInt(EXPERIENCE_KEY, 0);
            tool.rebuildStats();
            ITextComponent toolName = tool.createStack().getDisplayName();
            Messages.sendToPlayer(new LevelUpPacket(currentLevel, toolName), player);
        }
        data.putInt(EXPERIENCE_KEY, currentExperience);
    }

    private String getSlotName(int level, boolean isArmor) {
        if (!isArmor && !Config.toolsModifierTypeRandomOrder.get()) {
            return SlotAndStatUtil.getToolSlotForLevel(level);
        } else if (!isArmor && Config.toolsModifierTypeRandomOrder.get()) {
            return SlotAndStatUtil.getRandomToolSlot();
        } else if (isArmor && !Config.armorModifierTypeRandomOrder.get()) {
            return SlotAndStatUtil.getArmorSlotForLevel(level);
        } else { //random armor
            return SlotAndStatUtil.getRandomArmorSlot();
        }
    }

    private String getStatName(int level, boolean isArmor) {
        if (!isArmor && !Config.toolsStatTypeRandomOrder.get()) {
            return SlotAndStatUtil.getToolStatForLevel(level);
        } else if (!isArmor && Config.toolsStatTypeRandomOrder.get()) {
            return SlotAndStatUtil.getRandomToolStat();
        } else if (isArmor && !Config.armorStatTypeRandomOrder.get()) {
            return SlotAndStatUtil.getArmorStatForLevel(level);
        } else { //random armor
            return SlotAndStatUtil.getRandomArmorStat();
        }
    }

    private void appendHistory(ResourceLocation historyKey, String value, ModDataNBT data) {
        String modifierHistory = data.getString(historyKey);
        modifierHistory = modifierHistory + value + ";";
        data.putString(historyKey, modifierHistory);
    }

    private boolean isEqualTinkersItem(IModifierToolStack item1, IModifierToolStack item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        return item1.getModifiers().equals(item2.getModifiers()) && item1.getMaterials().equals(item2.getMaterials());
    }

    private boolean isValidDamageSource(DamageSource source, PlayerEntity player) {
        if (!source.isBypassArmor() && source.getEntity() instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity) source.getEntity();
            return !attacker.equals(player) && (Config.enablePvp.get() || !(attacker instanceof PlayerEntity));
        }
        return false;
    }

    private int getThornsBonus(IModifierToolStack tool) {
        int thornsLevel = tool.getModifierLevel(TinkerModifiers.thorns.get());
        if (!Config.enableThornsXp.get() || thornsLevel == 0) {
            return 0;
        }
        return RANDOM.nextFloat() < (thornsLevel * 0.15f) ? 1 + RANDOM.nextInt(Config.bonusThornsXp.get() + 1) : 0;
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

    public static boolean canLevelUp(int level) {
        return Config.maxLevel.get() == 0 || Config.maxLevel.get() > level;
    }

    public static boolean isBroadTool(IModifierToolStack tool) {
        return BROAD_TOOLS.contains(tool.getDefinition());
    }
}
