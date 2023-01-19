package pyre.tinkerslevellingaddon;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.network.LevelUpPacket;
import pyre.tinkerslevellingaddon.network.Messages;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.modifiers.hook.BlockTransformModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.PlantHarvestModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ShearsModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.List;

public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
        BlockTransformModifierHook {

    public static final ResourceLocation EXPERIENCE_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "experience");
    public static final ResourceLocation LEVEL_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "level");
    public static final ResourceLocation MODIFIER_HISTORY_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "modifier_history");
    public static final ResourceLocation STAT_HISTORY_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "stat_history");

    @Override
    protected void registerHooks(ModifierHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, TinkerHooks.PLANT_HARVEST, TinkerHooks.SHEAR_ENTITY, TinkerHooks.BLOCK_TRANSFORM);
    }

    @Override
    public void beforeRemoved(IToolStackView tool, RestrictedCompoundTag tag) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(MODIFIER_HISTORY_KEY);
        tool.getPersistentData().remove(STAT_HISTORY_KEY);
    }

    @Override
    public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
        if (ToolLevellingUtil.isSlotsLevellingEnabled(context)) {
            List<SlotType> slots =
                    ToolLevellingUtil.parseSlotsHistory(context.getPersistentData().getString(MODIFIER_HISTORY_KEY));
            for (SlotType slot : slots) {
                volatileData.addSlots(slot, 1);
            }
        }
    }

    @Override
    public void addToolStats(ToolRebuildContext context, int level, ModifierStatsBuilder builder) {
        if (ToolLevellingUtil.isStatsLevellingEnabled(context)) {
            List<FloatToolStat> stats =
                    ToolLevellingUtil.parseStatsHistory(context.getPersistentData().getString(STAT_HISTORY_KEY));
            boolean isArmor = ToolLevellingUtil.isArmor(context);
            for (FloatToolStat stat : stats) {
                stat.add(builder, isArmor ? Config.getArmorStatValue(stat) : Config.getToolStatValue(stat));
            }
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, int level, ToolHarvestContext context) {
        ServerPlayer player = context.getPlayer();
        if (!Config.enableMiningXp.get() || !context.isEffective() || player == null) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, InteractionHand.MAIN_HAND);
        if (!isEqualTinkersItem(tool, toolStack)) {
            toolStack = getHeldTool(player, InteractionHand.OFF_HAND);
        }
        addExperience(toolStack, 1 + Config.bonusMiningXp.get(), player);
    }

    @Override
    public void afterHarvest(IToolStackView tool, ModifierEntry modifier, UseOnContext context, ServerLevel world,
                             BlockState state, BlockPos pos) {
        if (!Config.enableHarvestingXp.get() || !(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, context.getHand());
        addExperience(toolStack, 1 + Config.bonusHarvestingXp.get(), player);
    }

    @Override
    public void afterShearEntity(IToolStackView tool, ModifierEntry modifier, Player player, Entity entity,
                                 boolean isTarget) {
        if (!Config.enableShearingXp.get() || !(player instanceof ServerPlayer)) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, InteractionHand.MAIN_HAND);
        if (!isEqualTinkersItem(tool, toolStack)) {
            toolStack = getHeldTool(player, InteractionHand.OFF_HAND);
        }
        addExperience(toolStack, 1 + Config.bonusShearingXp.get(), (ServerPlayer) player);
    }

    @Override
    public int afterEntityHit(IToolStackView tool, int level, ToolAttackContext context, float damageDealt) {
        if (!Config.enableAttackingXp.get() || !(context.getPlayerAttacker() instanceof ServerPlayer player) ||
                (!Config.enablePvp.get() && context.getLivingTarget() instanceof Player) || context.getLivingTarget() == null) {
            return 0;
        }
        int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + Config.bonusAttackingXp.get();
        ToolStack toolStack = getHeldTool(context.getPlayerAttacker(), context.getSlotType());
        addExperience(toolStack, xp, player);
        return 0;
    }

    @Override
    public void onAttacked(IToolStackView tool, int level, EquipmentContext context, EquipmentSlot slotType,
                           DamageSource source, float amount, boolean isDirectDamage) {
        if (!Config.enableTakingDamageXp.get() || slotType.getType() != EquipmentSlot.Type.ARMOR || !isDirectDamage ||
                !(context.getEntity() instanceof ServerPlayer player) || player.invulnerableTime > 10 ||
                !isValidDamageSource(source, player)) {
            return;
        }
        int xp = (Config.damageTaken.get() ? Math.round(amount) : 1) + Config.bonusTakingDamageXp.get() + getThornsBonus(tool);
        addExperience(getHeldTool(player, slotType), xp, player);
    }

    //todo currently flint and brick and boots modifiers do not use blockTransform hook
    @Override
    public void afterTransformBlock(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                    BlockState state, BlockPos pos, ToolAction action) {
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        ToolStack toolStack = getHeldTool(player, context.getHand());
        if (Config.enableStrippingXp.get() && action.equals(ToolActions.AXE_STRIP)) {
            addExperience(toolStack, 1 + Config.bonusStrippingXp.get(), player);
        } else if(Config.enableScrappingXp.get() && action.equals(ToolActions.AXE_SCRAPE)) {
            addExperience(toolStack, 1 + Config.bonusScrappingXp.get(), player);
        } else if(Config.enableWaxingOffXp.get() && action.equals(ToolActions.AXE_WAX_OFF)) {
            addExperience(toolStack, 1 + Config.bonusWaxingOffXp.get(), player);
        } else if(Config.enableTillingXp.get() && action.equals(ToolActions.HOE_TILL)) {
            addExperience(toolStack, 1 + Config.bonusTillingXp.get(), player);
        } else if(Config.enablePathMakingXp.get() && action.equals(ToolActions.SHOVEL_FLATTEN)) {
            addExperience(toolStack, 1 + Config.bonusPathMakingXp.get(), player);
        }
    }

    private void addExperience(ToolStack tool, int amount, ServerPlayer player) {
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
                appendHistory(MODIFIER_HISTORY_KEY, slotName, data);
            }
            String statName = ToolLevellingUtil.getStat(tool, currentLevel);
            if (statName != null) {
                appendHistory(STAT_HISTORY_KEY, statName, data);
            }

            //temporarily set xp to 0, so it displays nicely in chat message
            data.putInt(EXPERIENCE_KEY, 0);
            tool.rebuildStats();
            Component toolName = tool.createStack().getDisplayName();
            Messages.sendToPlayer(new LevelUpPacket(currentLevel, toolName), player);
        }
        data.putInt(EXPERIENCE_KEY, currentExperience);
    }

    private void appendHistory(ResourceLocation historyKey, String value, ModDataNBT data) {
        String modifierHistory = data.getString(historyKey);
        modifierHistory = modifierHistory + value + ";";
        data.putString(historyKey, modifierHistory);
    }

    private boolean isEqualTinkersItem(IToolStackView item1, IToolStackView item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        return item1.getModifiers().equals(item2.getModifiers()) && item1.getMaterials().equals(item2.getMaterials());
    }

    private boolean isValidDamageSource(DamageSource source, Player player) {
        return !source.isBypassArmor() && source.getEntity() instanceof LivingEntity attacker &&
                !attacker.equals(player) && (Config.enablePvp.get() || !(attacker instanceof Player));
    }

    private int getThornsBonus(IToolStackView tool) {
        int thornsLevel = tool.getModifierLevel(TinkerModifiers.thorns.getId());
        if (!Config.enableThornsXp.get() || thornsLevel == 0) {
            return 0;
        }
        return RANDOM.nextFloat() < (thornsLevel * 0.15f) ? 1 + RANDOM.nextInt(Config.bonusThornsXp.get() + 1) : 0;
    }
}
