package pyre.tinkerslevellingaddon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.Registration;
import pyre.tinkerslevellingaddon.util.ModUtil;
import pyre.tinkerslevellingaddon.util.ToolLevellingUtil;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.events.teleport.SlingModifierTeleportEvent;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorWalkModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ElytraFlightModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.BlockTransformModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.PlantHarvestModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.ShearsModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.modules.armor.CoverGroundWalkerModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.ReplaceBlockWalkerModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.ToolActionWalkerTransformModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.weapon.CircleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.module.weapon.MeleeHitToolHook;
import slimeknights.tconstruct.library.tools.definition.module.weapon.SweepWeaponAttack;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.*;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.MutableUseOnContext;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modifiers.ability.armor.walker.FlamewakeModifier;

import java.util.List;

import static pyre.tinkerslevellingaddon.util.ToolLevellingUtil.addExperience;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
        BlockBreakModifierHook, BlockTransformModifierHook, ProjectileLaunchModifierHook, OnAttackedModifierHook,
        MeleeHitModifierHook, ElytraFlightModifierHook, ModifierRemovalHook, VolatileDataModifierHook, ToolStatsModifierHook, ArmorWalkModifierHook {
    
    public static final TextColor IMPROVABLE_MODIFIER_COLOR = TextColor.fromRgb(9337340);
    
    public static final ResourceLocation EXPERIENCE_KEY = ModUtil.getResource("experience");
    public static final ResourceLocation LEVEL_KEY = ModUtil.getResource("level");
    public static final ResourceLocation SLOT_HISTORY_KEY = ModUtil.getResource("slot_history");
    public static final ResourceLocation STAT_HISTORY_KEY = ModUtil.getResource("stat_history");
    
    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.PLANT_HARVEST, ModifierHooks.SHEAR_ENTITY,
                ModifierHooks.BLOCK_TRANSFORM, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.BLOCK_BREAK,
                ModifierHooks.ON_ATTACKED, ModifierHooks.MELEE_HIT, ModifierHooks.ELYTRA_FLIGHT,
                ModifierHooks.VOLATILE_DATA, ModifierHooks.TOOL_STATS, ModifierHooks.REMOVE, ModifierHooks.BOOT_WALK);
    }

    @Override
    public Component onRemoved(IToolStackView tool, Modifier modifier) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(SLOT_HISTORY_KEY);
        tool.getPersistentData().remove(STAT_HISTORY_KEY);
        return null;
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ModDataNBT volatileData) {
        if (ToolLevellingUtil.isSlotsLevellingEnabled(context)) {
            List<SlotType> slots =
                    ToolLevellingUtil.parseSlotsHistory(context.getPersistentData().getString(SLOT_HISTORY_KEY));
            for (SlotType slot : slots) {
                volatileData.addSlots(slot, 1);
            }
        }
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        if (ToolLevellingUtil.isStatsLevellingEnabled(context)) {
            List<FloatToolStat> stats =
                    ToolLevellingUtil.parseStatsHistory(context.getPersistentData().getString(STAT_HISTORY_KEY));
            for (FloatToolStat stat : stats) {
                stat.add(builder, ToolLevellingUtil.getStatValue(context, stat));
            }
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
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
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!Config.enableAttackingXp.get() || !(context.getPlayerAttacker() instanceof ServerPlayer player) ||
                context.isExtraAttack() || (!Config.enablePvp.get() && context.getLivingTarget() instanceof Player) ||
                context.getLivingTarget() == null || context.getTarget() instanceof ArmorStand) {
            return;
        }
        int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + Config.bonusAttackingXp.get();
        ToolStack toolStack = getHeldTool(context.getPlayerAttacker(), context.getSlotType());
        addExperience(toolStack, xp, player);
        //handle aoe hits
        MeleeHitToolHook hook = tool.getDefinitionData().getHook(ToolHooks.MELEE_HIT);
        if (hook instanceof SweepWeaponAttack sweepHook) {
            handleSweepAttack(context, toolStack, player, damageDealt, sweepHook.range());
        }
        if (hook instanceof CircleWeaponAttack circleHook) {
            handleCircleAttack(context, toolStack, player, damageDealt, circleHook.diameter());
        }
    }

    @Override
    public void onAttacked(IToolStackView tool, ModifierEntry modifier, EquipmentContext context, EquipmentSlot slotType,
                           DamageSource source, float amount, boolean isDirectDamage) {
        if (!Config.enableTakingDamageXp.get() || slotType.getType() != EquipmentSlot.Type.ARMOR || !isDirectDamage ||
                !(context.getEntity() instanceof ServerPlayer player) || player.invulnerableTime > 10 ||
                !isValidDamageSource(source, player)) {
            return;
        }
        float damageBlocked = damageBlocked(player, source, amount);
        if (amount <= damageBlocked) {
            return;
        }
        int xp = (Config.damageTaken.get() ? Math.round(amount - damageBlocked) : 1) + Config.bonusTakingDamageXp.get()
                + getThornsBonus(tool);
        addExperience(getHeldTool(player, slotType), xp, player);
    }

    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter,
                                   Projectile projectile, @Nullable AbstractArrow arrow, NamespacedNBT persistentData,
                                   boolean primary) {
        //no way to get tool context when the arrow lands, so reward xp on launch instead
        if (!Config.enableShootingXp.get() || !(shooter instanceof ServerPlayer player)) {
            return;
        }
        ToolStack toolStack = getHeldTool(player, InteractionHand.MAIN_HAND);
        if (!isEqualTinkersItem(tool, toolStack)) {
            toolStack = getHeldTool(player, InteractionHand.OFF_HAND);
        }
        addExperience(toolStack, 1 + Config.bonusShootingXp.get(), player);
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
    
    @Override
    public boolean elytraFlightTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int flightTicks) {
        if (!Config.enableFlyingXp.get() || !(entity instanceof ServerPlayer player)) {
            return false;
        }
        if (flightTicks > 0 && (flightTicks % Config.flyingTime.get()) == 0) {
            addExperience(getHeldTool(player, EquipmentSlot.CHEST), 1 + Config.bonusFlyingXp.get(), player);
        }
        return false;
    }
    
    @SubscribeEvent
    static void onBlock(ShieldBlockEvent event) {
        if (!Config.enableBlockingDamageXp.get()) {
            return;
        }
        LivingEntity entity = event.getEntity();
        ItemStack activeStack = entity.getUseItem();
        if (ModifierUtil.getModifierLevel(activeStack, Registration.IMPROVABLE.get().getId()) <= 0) {
            return;
        }
        if (!activeStack.isEmpty() && activeStack.is(TinkerTags.Items.MODIFIABLE)
                && entity instanceof ServerPlayer player) {
            ToolStack tool = ToolStack.from(activeStack);
            float blockAngle = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.BLOCK_ANGLE) / 2;
            if (!tool.isBroken() && canBlock(player, event.getDamageSource().getSourcePosition(), blockAngle)) {
                int xp = (int) ((Config.damageBlocked.get() ? Math.min(event.getBlockedDamage(),
                        tool.getStats().get(ToolStats.BLOCK_AMOUNT)) : 1) + Config.bonusBlockingDamageXp.get());
                addExperience(tool, xp, player);
            }
        }
    }
    
    //boots modifiers do not use blockTransform hook, so instead we check if modifier is present and validate, using
    //given modifier logic, if it will be applied
    @Override
    public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos,
                       BlockPos newPos) {
        if (!(living instanceof ServerPlayer player) || !player.isOnGround() || tool.isBroken()) {
            return;
        }
        int xp = 0;
        for (ModifierEntry entry : tool.getModifierList()) {
            ArmorWalkModifierHook hook = entry.getHook(ModifierHooks.BOOT_WALK);
            if (hook instanceof ToolActionWalkerTransformModule toolAction) {
                if (Config.enablePlowingXp.get() && entry.getId().equals(ModifierIds.plowing)) {
                    xp = 1 + Config.bonusPlowingXp.get();
                }
                if (Config.enablePathMakerXp.get() && entry.getId().equals(ModifierIds.pathMaker)) {
                    xp = 1 + Config.bonusPathMakerXp.get();
                }
                if (xp > 0) {
                    handleWalkerTransform(tool, player, entry, toolAction, xp);
                }
                return;
            }
            if (hook instanceof CoverGroundWalkerModule coverGround) {
                if (Config.enableSnowdriftXp.get() && entry.getId().equals(ModifierIds.snowdrift)) {
                    xp = 1 + Config.bonusSnowdriftXp.get();
                }
                if (xp > 0) {
                    handleWalkerCoverGround(tool, player, entry, coverGround, xp);
                }
                return;
            }
            if (hook instanceof ReplaceBlockWalkerModule replaceBlock) {
                if (Config.enableFrostWalkerXp.get() && entry.getId().equals(ModifierIds.frostWalker)) {
                    xp = 1 + Config.bonusFrostWalkerXp.get();
                }
                if (xp > 0) {
                    handleWalkerReplaceBlock(tool, player, entry, replaceBlock, xp);
                }
                return;
            }
            if (hook instanceof FlamewakeModifier && Config.enableFlamewakeXp.get()) {
                xp = 1 + Config.bonusFlamewakeXp.get();
                handleFlamewake(tool, player, xp);
                return;
            }
        }
    }
    
    @SubscribeEvent
    static void onWarp(SlingModifierTeleportEvent event) {
        if (event.isCanceled()) {
            return;
        }
        IToolStackView tool = event.getTool();
        if (Config.enableWarpingXp.get() && event.getEntity() instanceof ServerPlayer player &&
                event.getEntry().getId().equals(TinkerModifiers.warping.getId()) &&
                tool.getModifierLevel(Registration.IMPROVABLE.get().getId()) > 0) {
            addExperience((ToolStack) tool, 1 + Config.bonusWarpingXp.get(), player);
        }
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
    
    private float damageBlocked(ServerPlayer player, DamageSource source, float amount) {
        ItemStack activeStack = player.getUseItem();
        if (activeStack.isEmpty()) {
            return 0;
        }
        if (activeStack.is(TinkerTags.Items.MODIFIABLE)) {
            ToolStack tool = ToolStack.from(activeStack);
            if (!tool.isBroken()) {
                float blockAngle = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.BLOCK_ANGLE) / 2;
                if (canBlock(player, source.getSourcePosition(), blockAngle)) {
                    return tool.getStats().get(ToolStats.BLOCK_AMOUNT);
                }
            }
        } else if (activeStack.getItem() instanceof ShieldItem && canBlock(player, source.getSourcePosition(), 90)) {
            return amount;
        }
        return 0;
    }
    
    private static boolean canBlock(ServerPlayer player, @Nullable Vec3 sourcePosition, float blockAngle) {
        if (sourcePosition == null) {
            return false;
        }
        Vec3 viewVector = player.getViewVector(1.0f);
        Vec3 entityPosition = player.position();
        Vec3 direction = new Vec3(entityPosition.x - sourcePosition.x, 0, entityPosition.z - sourcePosition.z);
        double length = viewVector.length() * direction.length();
        if (length < 1.0E-4D) {
            return false;
        }
        double angle = Math.abs(180 - Math.acos(direction.dot(viewVector) / length) * Mth.RAD_TO_DEG);
        return blockAngle >= angle;
    }
    
    private void handleSweepAttack(ToolAttackContext context, ToolStack toolStack, ServerPlayer player,
                                   float damageDealt, float baseRange) {
        //code based on SweepWeaponAttack.afterMeleeHit
        if (!context.isFullyCharged() || player.isSprinting() || context.isCritical() || !player.isOnGround() ||
                (player.walkDist - player.walkDistO) >= player.getSpeed()) {
            return;
        }
        //value - 2 - can be moved to datagen in the future
        double range = 2 + baseRange + toolStack.getModifierLevel(TinkerModifiers.expanded.getId());
        float sweepDamage = TinkerModifiers.sweeping.get().getSweepingDamage(toolStack, damageDealt);
        handleAOETargets(toolStack, player, context.getTarget(), sweepDamage, range);
    }
    
    private void handleCircleAttack(ToolAttackContext context, ToolStack toolStack, ServerPlayer player,
                                    float damageDealt, float baseDiameter) {
        //code based on SweepWeaponAttack.afterMeleeHit
        if (!context.isFullyCharged()) {
            return;
        }
        double range = baseDiameter + toolStack.getModifierLevel(TinkerModifiers.expanded.getId());
        if (range <= 0) {
            return;
        }
        handleAOETargets(toolStack, player, context.getTarget(), damageDealt, range);
    }
    
    private void handleAOETargets(ToolStack toolStack, ServerPlayer player, Entity target, float damage, double range) {
        for (LivingEntity aoeTarget : player.level.getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(range, 0.25D, range))) {
            if (aoeTarget != player && aoeTarget != target && !player.isAlliedTo(aoeTarget)
                    && !(aoeTarget instanceof ArmorStand) && target.distanceToSqr(aoeTarget) < range * range) {
                int xp = (Config.damageDealt.get() ? Math.round(damage) : 1) + Config.bonusAttackingXp.get();
                addExperience(toolStack, xp, player);
            }
        }
    }
    
    private void handleWalkerTransform(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                       ToolActionWalkerTransformModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and ToolActionWalkerTransformModule.onWalk
        float radius = Math.min(16, module.getRadius(tool, entry));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Vec3 posVec = player.position();
        BlockPos center = new BlockPos(posVec.x, posVec.y + 0.5, posVec.z);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            if (pos.closerToCenterThan(player.position(), radius)) {
                Material material = player.getLevel().getBlockState(pos).getMaterial();
                if (material.isReplaceable() || material == Material.PLANT) {
                    mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
                    MutableUseOnContext context = module.getContext(tool, entry, player, pos, mutable);
                    context.setOffsetPos(mutable);
                    BlockState original = player.getLevel().getBlockState(mutable);
                    BlockState transformed = original.getToolModifiedState(context, module.action(), true);
                    if (transformed != null) {
                        addExperience(getHeldTool(player, EquipmentSlot.FEET), xp, player);
                    }
                }
            }
        }
    }
    
    private void handleWalkerCoverGround(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                       CoverGroundWalkerModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and CoverGroundWalkerModule.onWalk
        float radius = Math.min(16, module.getRadius(tool, entry));
        Vec3 posVec = player.position();
        ServerLevel world = player.getLevel();
        BlockPos center = new BlockPos(posVec.x, posVec.y + 0.5, posVec.z);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            if (pos.closerToCenterThan(player.position(), radius)) {
                if (world.isEmptyBlock(pos) && module.state().canSurvive(world, pos)) {
                    addExperience(getHeldTool(player, EquipmentSlot.FEET), xp, player);
                }
            }
        }
    }
    
    private void handleWalkerReplaceBlock(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                          ReplaceBlockWalkerModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and ReplaceBlockWalkerModule.onWalk
        float radius = Math.min(16, module.getRadius(tool, entry));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        Level world = player.level;
        Vec3 posVec = player.position();
        BlockPos center = new BlockPos(posVec.x, posVec.y + 0.5, posVec.z);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0,
                radius))) {
            if (pos.closerToCenterThan(player.position(), radius) && world.isEmptyBlock(pos)) {
                mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
                //cannot access replacements, hardcoded frost walker
                BlockState state = Blocks.FROSTED_ICE.defaultBlockState();
                if (world.getBlockState(mutable).is(Blocks.WATER) && state.canSurvive(world, mutable) &&
                        world.isUnobstructed(state, mutable, CollisionContext.empty()) &&
                        !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(world.dimension(), world, mutable), Direction.UP)) {
                    addExperience(getHeldTool(player, EquipmentSlot.FEET), xp, player);
                }
            }
        }
    }
    
    private void handleFlamewake(IToolStackView tool, ServerPlayer player, int xp) {
        //code based on AbstractWalkerModifier.onWalk and FlamewakeModifier.onWalk
        //getRadius is protected, copied radius formula
        float radius = Math.min(16, 1.5f + tool.getModifierLevel(TinkerModifiers.expanded.getId()));
        Vec3 posVec = player.position();
        BlockPos center = new BlockPos(posVec.x, posVec.y + 0.5, posVec.z);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            if (pos.closerToCenterThan(player.position(), radius)) {
                if (BaseFireBlock.canBePlacedAt(player.getLevel(), pos, player.getDirection())) {
                    addExperience(getHeldTool(player, EquipmentSlot.FEET), xp, player);
                }
            }
        }
    }
}
