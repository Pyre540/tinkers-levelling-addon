package pyre.tinkerslevellingaddon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;
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
import slimeknights.tconstruct.library.modifiers.hook.interaction.BlockInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.EntityInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
import slimeknights.tconstruct.library.modifiers.hook.interaction.UsingToolModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.ProjectileLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.BlockTransformModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.PlantHarvestModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.ShearsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.special.sling.SlingLaunchModifierHook;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.CoverGroundWalkerModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.ReplaceBlockWalkerModule;
import slimeknights.tconstruct.library.modifiers.modules.armor.ToolActionWalkerTransformModule;
import slimeknights.tconstruct.library.module.ModuleHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.definition.module.ToolHooks;
import slimeknights.tconstruct.library.tools.definition.module.aoe.AreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.CircleAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.weapon.CircleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.module.weapon.MeleeHitToolHook;
import slimeknights.tconstruct.library.tools.definition.module.weapon.SweepWeaponAttack;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.*;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.MutableUseOnContext;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modules.armor.FireWalkerModule;
import slimeknights.tconstruct.tools.modules.armor.GlowWalkerModule;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static pyre.tinkerslevellingaddon.util.ToolLevellingUtil.*;
import static pyre.tinkerslevellingaddon.util.ToolUtil.*;
import static pyre.tinkerslevellingaddon.util.WorldUtil.*;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
        BlockBreakModifierHook, BlockTransformModifierHook, ProjectileLaunchModifierHook, OnAttackedModifierHook,
        MeleeHitModifierHook, ElytraFlightModifierHook, ArmorWalkModifierHook, UsingToolModifierHook,
        EntityInteractionModifierHook, SlingLaunchModifierHook, BlockInteractionModifierHook, ModifierRemovalHook,
        VolatileDataModifierHook, ToolStatsModifierHook {
    
    public static final TextColor IMPROVABLE_MODIFIER_COLOR = TextColor.fromRgb(9337340);
    
    public static final ResourceLocation EXPERIENCE_KEY = ModUtil.getResource("experience");
    public static final ResourceLocation LEVEL_KEY = ModUtil.getResource("level");
    public static final ResourceLocation SLOT_HISTORY_KEY = ModUtil.getResource("slot_history");
    public static final ResourceLocation STAT_HISTORY_KEY = ModUtil.getResource("stat_history");
    public static final ResourceLocation PENDING_LEVEL_UP_KEY = ModUtil.getResource("pending_level_up");

    @Override
    protected void registerHooks(ModuleHookMap.Builder hookBuilder) {
        super.registerHooks(hookBuilder);
        hookBuilder.addHook(this, ModifierHooks.PLANT_HARVEST, ModifierHooks.SHEAR_ENTITY,
                ModifierHooks.TOOL_USING, ModifierHooks.ENTITY_INTERACT, ModifierHooks.BLOCK_TRANSFORM,
                ModifierHooks.BLOCK_BREAK, ModifierHooks.BLOCK_INTERACT, ModifierHooks.ON_ATTACKED,
                ModifierHooks.MELEE_HIT, ModifierHooks.PROJECTILE_LAUNCH, ModifierHooks.ELYTRA_FLIGHT,
                ModifierHooks.BOOT_WALK, ModifierHooks.SLING_LAUNCH, ModifierHooks.VOLATILE_DATA,
                ModifierHooks.TOOL_STATS, ModifierHooks.REMOVE);
    }

    @Override
    public int getPriority() {
        return 300;
    }

    @Override
    public Component onRemoved(IToolStackView tool, Modifier modifier) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(SLOT_HISTORY_KEY);
        tool.getPersistentData().remove(STAT_HISTORY_KEY);
        tool.getPersistentData().remove(PENDING_LEVEL_UP_KEY);
        return null;
    }

    @Override
    public void addVolatileData(IToolContext context, ModifierEntry modifier, ToolDataNBT volatileData) {
        if (isSlotsLevellingEnabled(context)) {
            List<SlotType> slots = parseSlotsHistory(context.getPersistentData().getString(SLOT_HISTORY_KEY));
            for (SlotType slot : slots) {
                volatileData.addSlots(slot, 1);
            }
        }
    }

    @Override
    public void addToolStats(IToolContext context, ModifierEntry modifier, ModifierStatsBuilder builder) {
        if (isStatsLevellingEnabled(context)) {
            List<FloatToolStat> stats = parseStatsHistory(context.getPersistentData().getString(STAT_HISTORY_KEY));
            for (FloatToolStat stat : stats) {
                stat.add(builder, getStatValue(context, stat));
            }
        }
    }

    @SubscribeEvent
    static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) {
            return;
        }
        for (ItemStack itemStack : player.getInventory().armor) {
            processPendingLevelUp(player, itemStack);
        }
        for (ItemStack itemStack : player.getInventory().items) {
            processPendingLevelUp(player, itemStack);
        }
        for (ItemStack itemStack : player.getInventory().offhand) {
            processPendingLevelUp(player, itemStack);
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, ModifierEntry modifier, ToolHarvestContext context) {
        if (Config.enableMiningXp.get() && context.isEffective()) {
            addExperience(tool, 1 + Config.bonusMiningXp.get());
        }
    }

    @Override
    public void afterHarvest(IToolStackView tool, ModifierEntry modifier, UseOnContext context, ServerLevel world,
                             BlockState state, BlockPos pos) {
        if (Config.enableHarvestingXp.get()) {
            addExperience(tool, 1 + Config.bonusHarvestingXp.get());
        }
    }

    @Override
    public void afterShearEntity(IToolStackView tool, ModifierEntry modifier, Player player, Entity entity,
                                 boolean isTarget) {
        if (Config.enableShearingXp.get()) {
            addExperience(tool, 1 + Config.bonusShearingXp.get());
        }
    }

    @Override
    public void afterMeleeHit(IToolStackView tool, ModifierEntry modifier, ToolAttackContext context, float damageDealt) {
        if (!Config.enableAttackingXp.get() || !(context.getPlayerAttacker() instanceof ServerPlayer player) ||
                !isAttackAllowed(context, tool) || !isValidMeleeTarget(player, context)) {
            return;
        }
        int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + Config.bonusAttackingXp.get();
        addExperience(tool, xp);
        //handle aoe hits
        MeleeHitToolHook hook = tool.getDefinitionData().getHook(ToolHooks.MELEE_HIT);
        if (hook instanceof SweepWeaponAttack sweepHook) {
            handleSweepAttack(context, tool, player, damageDealt, sweepHook);
        }
        if (hook instanceof CircleWeaponAttack circleHook) {
            handleCircleAttack(context, tool, player, damageDealt, circleHook);
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
        float damageBlocked = getDamageBlockedAmount(player, source, amount);
        if (amount <= damageBlocked) {
            return;
        }
        int xp = (Config.damageTaken.get() ? Math.round(amount - damageBlocked) : 1) + Config.bonusTakingDamageXp.get()
                + getThornsBonus(tool);
        addExperience(tool, xp);
    }

    @Override
    public void onProjectileLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity shooter,
                                   Projectile projectile, @Nullable AbstractArrow arrow, ModDataNBT persistentData,
                                   boolean primary) {
        //no way to get tool context when the arrow lands, so reward xp on launch instead
        if (Config.enableShootingXp.get() && shooter instanceof ServerPlayer) {
            addExperience(tool, 1 + Config.bonusShootingXp.get());
        }
    }
    
    @Override
    public void afterTransformBlock(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                    BlockState state, BlockPos pos, ToolAction action) {
        if (!(context.getPlayer() instanceof ServerPlayer)) {
            return;
        }

        if (Config.enableStrippingXp.get() && action.equals(ToolActions.AXE_STRIP)) {
            addExperience(tool, 1 + Config.bonusStrippingXp.get());
        } else if(Config.enableScrappingXp.get() && action.equals(ToolActions.AXE_SCRAPE)) {
            addExperience(tool, 1 + Config.bonusScrappingXp.get());
        } else if(Config.enableWaxingOffXp.get() && action.equals(ToolActions.AXE_WAX_OFF)) {
            addExperience(tool, 1 + Config.bonusWaxingOffXp.get());
        } else if(Config.enableTillingXp.get() && action.equals(ToolActions.HOE_TILL)) {
            addExperience(tool, 1 + Config.bonusTillingXp.get());
        } else if(Config.enablePathMakingXp.get() && action.equals(ToolActions.SHOVEL_FLATTEN)) {
            addExperience(tool, 1 + Config.bonusPathMakingXp.get());
        }
    }
    
    @Override
    public boolean elytraFlightTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int flightTicks) {
        if (!Config.enableFlyingXp.get() || !(entity instanceof ServerPlayer)) {
            return false;
        }
        if (flightTicks > 0 && (flightTicks % Config.flyingTime.get()) == 0) {
            addExperience(tool, 1 + Config.bonusFlyingXp.get());
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
        if (!hasImprovable(activeStack) || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ToolStack tool = ToolStack.from(activeStack);
        float blockAngle = ConditionalStatModifierHook.getModifiedStat(tool, player, ToolStats.BLOCK_ANGLE) / 2;
        if (!tool.isBroken() && canBlock(player, event.getDamageSource().getSourcePosition(), blockAngle)) {
            int xp = (int) ((Config.damageBlocked.get() ? Math.min(event.getBlockedDamage(),
                    tool.getStats().get(ToolStats.BLOCK_AMOUNT)) : 1) + Config.bonusBlockingDamageXp.get());
            addExperience(tool, xp);
        }
    }
    
    //boots modifiers do not use blockTransform hook, so instead we check if modifier is present and validate, using
    //given modifier logic, if it will be applied
    @Override
    public void onWalk(IToolStackView tool, ModifierEntry modifier, LivingEntity living, BlockPos prevPos,
                       BlockPos newPos) {
        if (!(living instanceof ServerPlayer player) || !player.onGround() || tool.isBroken()) {
            return;
        }
        int xp = 0;
        for (ModifierEntry entry : tool.getModifierList()) {
            ArmorWalkModifierHook hook = entry.getHook(ModifierHooks.BOOT_WALK);
            if (hook instanceof ToolActionWalkerTransformModule toolAction) {
                if (Config.enablePlowingXp.get() && entry.getId().equals(ModifierIds.tilling)) {
                    xp = 1 + Config.bonusPlowingXp.get();
                }
                if (Config.enablePathMakerXp.get() && entry.getId().equals(ModifierIds.pathing)) {
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
                    handleWalkerCoverGround(tool, player, entry, coverGround, xp);
                    return;
                }
            }
            if (hook instanceof ReplaceBlockWalkerModule replaceBlock) {
                if (Config.enableFrostWalkerXp.get() && entry.getId().equals(ModifierIds.frostWalker)) {
                    xp = 1 + Config.bonusFrostWalkerXp.get();
                    handleWalkerReplaceBlock(tool, player, entry, replaceBlock, xp);
                    return;
                }
            }
            if (hook instanceof FireWalkerModule fireWalker) {
                if (Config.enableFlamewakeXp.get() && entry.getId().equals(ModifierIds.flamewake)) {
                    xp = 1 + Config.bonusFlamewakeXp.get();
                    handleWalkerFire(tool, player, entry, fireWalker, xp);
                    return;
                }
            }
            if (hook instanceof GlowWalkerModule glowWalker) {
                if (Config.enableGlowingXp.get() && entry.getId().equals(ModifierIds.glowing)) {
                    xp = 1 + Config.bonusGlowingXp.get();
                    handleWalkerGlowing(tool, player, entry, glowWalker, xp);
                    return;
                }
            }
        }
    }

    @Override
    public void afterSlingLaunch(IToolStackView tool, ModifierEntry modifier, LivingEntity holder, LivingEntity target,
                                 ModifierEntry slingSource, float force, float multiplier, Vec3 angle) {
        if (!(holder instanceof ServerPlayer)) {
            return;
        }

        if (Config.enableWarpingXp.get() && ModifierIds.warping.equals(slingSource.getId())) {
            addExperience(tool, 1 + Config.bonusWarpingXp.get());
        } else if (Config.enableFlingingXp.get() && ModifierIds.flinging.equals(slingSource.getId())) {
            addExperience(tool, 1 + Config.bonusFlingingXp.get());
        } else if (Config.enableSpringingXp.get() && ModifierIds.springing.equals(slingSource.getId())) {
            addExperience(tool, 1 + Config.bonusSpringingXp.get());
        } else if (Config.enableBonkingXp.get() && ModifierIds.bonking.equals(slingSource.getId())) {
            addExperience(tool, 1 + Config.bonusBonkingXp.get());
        }
    }

    //use beforeEntityUse because afterEntityUse consumes interaction and do not update tool data
    @Override
    public InteractionResult beforeEntityUse(IToolStackView tool, ModifierEntry modifier, Player player, Entity target, InteractionHand hand, InteractionSource source) {
        if (tool.isBroken() || player.level().isClientSide || !(player instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        if (Config.enableFirestarterXp.get() && tool.getModifier(ModifierIds.firestarter) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.firestarter, source)
                && target instanceof Creeper creeper && !creeper.isIgnited()) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get());
        }

        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult beforeBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                            InteractionSource source) {
        if (tool.isBroken() || context.getLevel().isClientSide || !(context.getPlayer() instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        if (context.getLevel().getBlockState(context.getClickedPos()).is(BlockTags.CANDLE_CAKES)) {
            return afterBlockUse(tool, modifier, context, source);
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResult afterBlockUse(IToolStackView tool, ModifierEntry modifier, UseOnContext context,
                                           InteractionSource source) {
        if (tool.isBroken() || context.getLevel().isClientSide || !(context.getPlayer() instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        if (Config.enableGlowingXp.get() && tool.getModifier(ModifierIds.glowing) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.glowing, source)) {
            handleGlowing(tool, context);
        }
        if (Config.enableFirestarterXp.get() && tool.getModifier(ModifierIds.firestarter) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.firestarter, source)) {
            handleFirestarter(tool, context);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration,
                            int timeLeft, ModifierEntry activeModifier) {
        if (tool.isBroken() || timeLeft < 0 || !(entity instanceof ServerPlayer player) ||
                activeModifier == ModifierEntry.EMPTY) {
            return;
        }

        if (Config.enableBrushingXp.get() && activeModifier.getId().equals(ModifierIds.brushing)) {
            handleBrushing(tool, entity, timeLeft, player, activeModifier);
        }
    }

    private void handleSweepAttack(ToolAttackContext context, IToolStackView toolStack, ServerPlayer player,
                                   float damageDealt, SweepWeaponAttack module) {
        //code based on SweepWeaponAttack.afterMeleeHit
        if (!context.isFullyCharged() || player.isSprinting() || context.isCritical() || context.isProjectile() ||
                !player.onGround() || (player.walkDist - player.walkDistO) >= player.getSpeed()) {
            return;
        }
        double range = module.range().compute(toolStack.getVolatileData().getInt(IModifiable.EXPANDED));
        //value - 2 - can be moved to datagen in the future
        double rangeSq = (2 + range);
        rangeSq *= rangeSq;
        float sweepDamage = SweepWeaponAttack.getSweepingDamage(toolStack, damageDealt);
        handleAOETargets(toolStack, player, context.getTarget(), sweepDamage, range, rangeSq);
    }

    private void handleCircleAttack(ToolAttackContext context, IToolStackView toolStack, ServerPlayer player,
                                    float damageDealt, CircleWeaponAttack module) {
        //code based on CircleWeaponAttack.afterMeleeHit
        double range = module.diameter().compute(toolStack.getVolatileData().getInt(IModifiable.EXPANDED));
        if (range <= 0) {
            return;
        }
        double rangeSq = range * range;
        handleAOETargets(toolStack, player, context.getTarget(), damageDealt, range, rangeSq);
    }

    private void handleAOETargets(IToolStackView toolStack, ServerPlayer player, Entity target, float damage, double range,
                                  double rangeSq) {
        for (LivingEntity aoeTarget : player.level().getEntitiesOfClass(LivingEntity.class,
                target.getBoundingBox().inflate(range, 0.25D, range))) {
            if (aoeTarget != player && aoeTarget != target && !player.isAlliedTo(aoeTarget)
                    && !(aoeTarget instanceof ArmorStand armorStand && armorStand.isMarker())
                    && player.distanceToSqr(aoeTarget) < rangeSq) {
                int xp = (Config.damageDealt.get() ? Math.round(damage) : 1) + Config.bonusAttackingXp.get();
                addExperience(toolStack, xp);
            }
        }
    }

    private void handleWalkerTransform(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                       ToolActionWalkerTransformModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and ToolActionWalkerTransformModule.walkOn
        if (!module.condition().matches(tool, entry)) {
            return;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        handleWalker(tool, player, entry, module, pos -> {
            if (player.level().getBlockState(pos).canBeReplaced()) {
                mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
                MutableUseOnContext context = module.getContext(tool, entry, player, pos, mutable);
                context.setOffsetPos(mutable);
                BlockState original = player.level().getBlockState(mutable);
                BlockState transformed = original.getToolModifiedState(context, module.action(), true);
                if (transformed != null) {
                    addExperience(tool, xp);
                }
            }
            return false;
        });
    }

    private void handleWalkerCoverGround(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                       CoverGroundWalkerModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and CoverGroundWalkerModule.walkOn
        if (!module.condition().matches(tool, entry)) {
            return;
        }
        Level world = player.level();
        handleWalker(tool, player, entry, module, pos -> {
            if (world.isEmptyBlock(pos) && module.state().canSurvive(world, pos)) {
                addExperience(tool, xp);
            }
            return false;
        });
    }

    private void handleWalkerReplaceBlock(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                          ReplaceBlockWalkerModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and ReplaceBlockWalkerModule.onWalk
        if (!module.tool().matches(tool)) {
            return;
        }
        Level world = player.level();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        handleWalker(tool, player, entry, module, pos -> {
            mutable.set(pos.getX(), pos.getY() - 1, pos.getZ());
            //cannot access replacements, hardcoded frost walker
            BlockState state = Blocks.FROSTED_ICE.defaultBlockState();
            if (world.getBlockState(mutable).is(Blocks.WATER) && state.canSurvive(world, mutable) &&
                    world.isUnobstructed(state, mutable, CollisionContext.empty()) &&
                    !ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(world.dimension(), world, mutable), Direction.UP)) {
                addExperience(tool, xp);
            }
            return false;
        });
    }

    private void handleWalkerFire(IToolStackView tool, ServerPlayer player, ModifierEntry entry, FireWalkerModule module,
                                  int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and FireWalkerModule.walkOn
        handleWalker(tool, player, entry, module, pos -> {
            if (BaseFireBlock.canBePlacedAt(player.level(), pos, player.getDirection())) {
                addExperience(tool, xp);
            }
            return false;
        });
    }

    private void handleWalkerGlowing(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                                            GlowWalkerModule module, int xp) {
        //code based on ArmorWalkRadiusModule.onWalk and GlowWalkerModule.walkOn
        Level world = player.level();
        handleWalker(tool, player, entry, module, pos -> {
            if (world.isEmptyBlock(pos) && world.getBrightness(LightLayer.BLOCK, pos) < module.minLight()) {
                if (canPlaceGlow(world, pos, Direction.DOWN)) {
                    addExperience(tool, xp);
                    return true;
                }
            }
            return false;
        });
    }

    private void handleWalker(IToolStackView tool, ServerPlayer player, ModifierEntry entry,
                              ArmorWalkRadiusModule<?> module, Function<BlockPos, Boolean> walkOn) {
        float trueRadius = getWalkerTrueRadius(tool, entry, module);
        int radius = Mth.floor(trueRadius);
        Vec3 posVec = player.position();
        BlockPos center = BlockPos.containing(posVec.x, posVec.y + 0.5, posVec.z);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, 0, -radius), center.offset(radius, 0, radius))) {
            if (pos.closerToCenterThan(player.position(), trueRadius)) {
                boolean doBreak = walkOn.apply(pos);
                if (doBreak) {
                    return;
                }
            }
        }
    }

    private void handleGlowing(IToolStackView tool, UseOnContext context) {
        Direction face = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(face);
        if (canPlaceGlow(context.getLevel(), pos, face.getOpposite())) {
            addExperience(tool, 1 + Config.bonusGlowingXp.get());
        }
    }

    private static void handleFirestarter(IToolStackView tool, UseOnContext context) {
        //code based on PlaceFireModule::afterBlockUse
        Level world = context.getLevel();
        UseOnContext targetContext = context;
        BlockPos pos = context.getClickedPos();
        Direction sideHit = context.getClickedFace();
        BlockState state = world.getBlockState(pos);

        boolean targetingFire = false;
        if (state.is(BlockTags.FIRE)) {
            pos = pos.relative(sideHit.getOpposite());
            targetContext = Util.offset(context, pos);
            targetingFire = true;
        }

        int range = tool.getVolatileData().getInt(IModifiable.EXPANDED);
        Iterable<BlockPos> targets = Collections.emptyList();
        if (range > 0) {
            targets = CircleAOEIterator.calculate(tool, targetContext, 1 + range,
                    true, AreaOfEffectIterator.AOEMatchType.TRANSFORM);
        }

        Direction horizontalFacing = context.getHorizontalDirection();
        if (!targetingFire && canIgnite(world, pos, state, sideHit, horizontalFacing)) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get());
            return;
        }
        for (BlockPos target : targets) {
            if (canIgnite(world, target, world.getBlockState(target), sideHit, horizontalFacing)) {
                addExperience(tool, 1 + Config.bonusFirestarterXp.get());
            }
        }
    }

    private void handleBrushing(IToolStackView tool, LivingEntity entity, int timeLeft, ServerPlayer player, ModifierEntry activeModifier) {
        //code based on BrushModule.onUsingTick
        HitResult hit = calculateHitResult(entity);
        if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
            if ((activeModifier.getHook(ModifierHooks.GENERAL_INTERACT).getUseDuration(tool, activeModifier) - timeLeft + 1) % 10 == 5) {
                Level level = entity.level();
                BlockPos pos = blockHit.getBlockPos();
                BlockState state = level.getBlockState(pos);
                InteractionHand hand = entity.getUsedItemHand();

                // we can only check if block is brushable
                if (isBrushable(blockHit, level)) {
                    addExperience(tool, 1 + Config.bonusBrushingXp.get());
                }

                // brush AOE blocks
                UseOnContext context = new UseOnContext(level, player, hand, player.getItemInHand(hand), blockHit);
                for (BlockPos extraPos : tool.getDefinition().getHook(ToolHooks.AOE_ITERATOR)
                        .getBlocks(tool, context, state, AreaOfEffectIterator.AOEMatchType.TRANSFORM)) {
                    if (isBrushable(Util.offset(blockHit, extraPos), level)) {
                        addExperience(tool, 1 + Config.bonusBrushingXp.get());
                    }
                }
            }
        }
    }
}
