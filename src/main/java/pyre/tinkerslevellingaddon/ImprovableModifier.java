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
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
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
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.util.ModUtil;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHooks;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;
import slimeknights.tconstruct.library.modifiers.hook.armor.ArmorWalkModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.ElytraFlightModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.armor.OnAttackedModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.behavior.ProcessLootModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ModifierRemovalHook;
import slimeknights.tconstruct.library.modifiers.hook.build.ToolStatsModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.build.VolatileDataModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.combat.MeleeHitModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.*;
import slimeknights.tconstruct.library.modifiers.hook.mining.BlockBreakModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.ranged.LauncherHitModifierHook;
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
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipe;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipeCache;
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
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.nbt.*;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.MutableUseOnContext;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.smeltery.block.entity.module.EntityMeltingModule;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.entity.CustomFireball;
import slimeknights.tconstruct.tools.entity.FluidEffectProjectile;
import slimeknights.tconstruct.tools.modules.MeltingModule;
import slimeknights.tconstruct.tools.modules.armor.FireWalkerModule;
import slimeknights.tconstruct.tools.modules.armor.GlowWalkerModule;
import slimeknights.tconstruct.tools.modules.combat.SpillingModule;
import slimeknights.tconstruct.tools.modules.interaction.SplashingModule;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static pyre.tinkerslevellingaddon.util.ToolLevellingUtil.*;
import static pyre.tinkerslevellingaddon.util.ToolUtil.*;
import static pyre.tinkerslevellingaddon.util.WorldUtil.*;
import static slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper.TANK_HELPER;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
        BlockBreakModifierHook, BlockTransformModifierHook, ProjectileLaunchModifierHook, OnAttackedModifierHook,
        MeleeHitModifierHook, LauncherHitModifierHook, ElytraFlightModifierHook, ArmorWalkModifierHook,
        UsingToolModifierHook, InventoryTickModifierHook, EntityInteractionModifierHook, SlingLaunchModifierHook,
        BlockInteractionModifierHook, ProcessLootModifierHook, ModifierRemovalHook, VolatileDataModifierHook,
        ToolStatsModifierHook {
    
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
                ModifierHooks.MELEE_HIT, ModifierHooks.LAUNCHER_HIT, ModifierHooks.PROJECTILE_LAUNCH,
                ModifierHooks.ELYTRA_FLIGHT, ModifierHooks.BOOT_WALK, ModifierHooks.SLING_LAUNCH,
                ModifierHooks.INVENTORY_TICK, ModifierHooks.PROCESS_LOOT, ModifierHooks.VOLATILE_DATA,
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
        if (!(context.getPlayerAttacker() instanceof ServerPlayer player)) {
            return;
        }

        if (Config.enableAttackingXp.get() && isAttackAllowed(context, tool) && isValidMeleeTarget(player, context)) {
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
        if (Config.enableSpillingXp.get() && damageDealt > 0 && context.isFullyCharged()) {
            handleSpilling(tool, tool.getModifier(ModifierIds.spilling), context.getAttacker(),
                    context.getPlayerAttacker(), context.getTarget(), context.getLivingTarget(), context.getProjectile());
        }
        if (Config.enableMeltingXp.get() && damageDealt > 0 &&
                tool.getModifier(TinkerModifiers.melting.getId()) != ModifierEntry.EMPTY) {
            handleTargetMelting(tool, context.getLivingTarget());
        }
    }

    @Override
    public void onLauncherHitEntity(IToolStackView tool, ModifierEntry modifier, Projectile projectile,
                                    LivingEntity attacker, Entity target, @Nullable LivingEntity livingTarget,
                                    float damageDealt) {
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }
        if (Config.enableAttackingXp.get() && !tool.isBroken() && isValidLauncherTarget(player, target)) {
            int xp = (Config.damageDealt.get() ? Math.round(damageDealt) : 1) + Config.bonusAttackingXp.get();
            addExperience(tool, xp);
        }
        if (Config.enableSpillingXp.get() && damageDealt > 0) {
            handleSpilling(tool, tool.getModifier(ModifierIds.spilling), attacker, ModifierUtil.asPlayer(attacker),
                    target, livingTarget, projectile);
        }
        if (Config.enableMeltingXp.get() && damageDealt > 0 &&
                tool.getModifier(TinkerModifiers.melting.getId()) != ModifierEntry.EMPTY) {
            handleTargetMelting(tool, livingTarget);
        }
    }

    @Override
    public void onLauncherHitBlock(IToolStackView tool, ModifierEntry modifier, Projectile projectile,
                                   LivingEntity owner, BlockPos target) {
        if (!tool.isBroken() && owner instanceof ServerPlayer && Config.enableGrapplingXp.get() &&
                projectile instanceof FishingHook) {
            addExperience(tool, 1 + Config.bonusGrapplingXp.get());
        }
    }

    @SubscribeEvent
    static void onItemFished(ItemFishedEvent event) {
        if (Config.enableFishingXp.get() && event.getEntity() instanceof ServerPlayer player) {
            ItemStack stack = player.getMainHandItem();
            if (!stack.canPerformAction(ToolActions.FISHING_ROD_CAST)) {
                stack = player.getOffhandItem();
                if (!stack.canPerformAction(ToolActions.FISHING_ROD_CAST)) {
                    return;
                }
            }
            if (hasImprovable(stack)) {
                addExperience(ToolStack.from(stack), 1 + Config.bonusFishingXp.get());
            }
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
        if (!(shooter instanceof ServerPlayer) || projectile instanceof FishingHook) {
            //we can get tool context when fishing/grappling, so we do not reward xp on lunching fishing hook
            return;
        }
        //no way to get tool context when the arrow lands, so reward xp on launch instead
        if (Config.enableShootingXp.get() && arrow != null) {
            addExperience(tool, 1 + Config.bonusShootingXp.get());
        }
        if (Config.enableSpittingXp.get() && projectile instanceof FluidEffectProjectile) {
            addExperience(tool, 1 + Config.bonusSpittingXp.get());
        }
        if (Config.enableSlimeballXp.get() && projectile instanceof CustomFireball) {
            addExperience(tool, 1 + Config.bonusSlimeballXp.get());
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

    @Override
    public InteractionResult beforeEntityUse(IToolStackView tool, ModifierEntry modifier, Player player, Entity target,
                                             InteractionHand hand, InteractionSource source) {
        if (tool.isBroken() || !(player instanceof ServerPlayer)) {
            return InteractionResult.PASS;
        }
        //use beforeEntityUse because afterEntityUse consumes interaction and do not update tool data
        if (Config.enableFirestarterXp.get() && tool.getModifier(ModifierIds.firestarter) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.firestarter, source)
                && target instanceof Creeper creeper && !creeper.isIgnited()) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get());
        }
        ModifierEntry splashing = tool.getModifier(ModifierIds.splashing);
        if (Config.enableSplashingXp.get() && splashing != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.splashing, source)) {
            handleEntitySplashing(tool, player, target, splashing);
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
        ModifierEntry splashing = tool.getModifier(ModifierIds.splashing);
        if (Config.enableSplashingXp.get() && splashing != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, ModifierIds.splashing, source)) {
            handleBlockSplashing(tool, context, splashing);
        }

        return InteractionResult.PASS;
    }

    @Override
    public void onUsingTick(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration,
                            int timeLeft, ModifierEntry activeModifier) {
        if (tool.isBroken() || timeLeft < 0 || !(entity instanceof ServerPlayer player)) {
            return;
        }

        if (Config.enableBrushingXp.get() && ModifierIds.brushing.equals(activeModifier.getId())) {
            handleBrushing(tool, entity, timeLeft, player, activeModifier);
        }
        ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
        if (Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && slurping.equals(activeModifier)) {
            handleToolSlurping(tool, useDuration - timeLeft, player, slurping);
        }
    }

    @Override
    public void beforeReleaseUsing(IToolStackView tool, ModifierEntry modifier, LivingEntity entity, int useDuration,
                                   int timeLeft, ModifierEntry activeModifier) {
        if (tool.isBroken() || timeLeft < 0 || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
        if (Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && slurping.equals(activeModifier)) {
            handleToolSlurping(tool, useDuration - timeLeft, player, slurping);
        }
    }

    @Override
    public void onInventoryTick(IToolStackView tool, ModifierEntry modifier, Level world, LivingEntity holder,
                                int itemSlot, boolean isSelected, boolean isCorrectSlot, ItemStack stack) {
        if (tool.isBroken() || !(holder instanceof ServerPlayer player)) {
            return;
        }
        ModifierEntry slurping = tool.getModifier(ModifierIds.slurping);
        if (Config.enableSlurpingXp.get() && slurping != ModifierEntry.EMPTY && isCorrectSlot) {
            handleArmorSlurping(tool, player, slurping);
        }
    }

    @Override
    public void processLoot(IToolStackView tool, ModifierEntry modifier, List<ItemStack> generatedLoot,
                            LootContext context) {
        ModifierEntry melting = tool.getModifier(TinkerModifiers.melting.getId());
        if (Config.enableMeltingXp.get() && melting != ModifierEntry.EMPTY) {
            handleLootMelting(tool, generatedLoot, context, melting);
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

    private void handleSpilling(IToolStackView tool, ModifierEntry spilling, LivingEntity attacker,
                                       Player playerAttacker, Entity target, LivingEntity livingTarget,
                                       Projectile projectile) {
        if (spilling == ModifierEntry.EMPTY) {
            return;
        }

        MeleeHitModifierHook hook = spilling.getModifier().getHook(ModifierHooks.MELEE_HIT);
        if (hook instanceof SpillingModule spillingModule && spillingModule.condition().matches(tool, spilling)) {
            FluidStack fluid = TANK_HELPER.getFluid(tool);
            if (!fluid.isEmpty()) {
                FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
                if (recipe.hasEntityEffects()) {
                    int consumed = recipe.applyToEntity(fluid, spillingModule.level().compute(spilling.getEffectiveLevel()),
                            FluidEffectContext.builder(attacker.level())
                                    .user(attacker, playerAttacker)
                                    .projectile(projectile)
                                    .target(target, livingTarget),
                            IFluidHandler.FluidAction.SIMULATE);
                    if (consumed > 0) {
                        addExperience(tool, 1 + Config.bonusSpillingXp.get());
                    }
                }
            }
        }
    }

    private void handleEntitySplashing(IToolStackView tool, Player player, Entity target, ModifierEntry splashing) {
        //code based on SplashingModule.beforeEntityUse
        EntityInteractionModifierHook hook = splashing.getModifier().getHook(ModifierHooks.ENTITY_INTERACT);
        if (!(hook instanceof SplashingModule splashingModule)) {
            return;
        }

        FluidStack fluid = TANK_HELPER.getFluid(tool);
        if (!fluid.isEmpty()) {
            FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
            if (recipe.hasEntityEffects()) {
                Level world = player.level();
                float level = splashingModule.strength().compute(splashing);
                int consumed = recipe.applyToEntity(fluid, level, FluidEffectContext.builder(world).user(player).target(target), IFluidHandler.FluidAction.SIMULATE);
                if (consumed > 0) {
                    addExperience(tool, 1 + Config.bonusSplashingXp.get());
                    fluid.shrink(consumed);
                }
                // AoE
                if (!fluid.isEmpty()) {
                    float range = 1 + tool.getVolatileData().getInt(IModifiable.EXPANDED);
                    float rangeSq = range * range;
                    for (Entity aoeTarget : world.getEntitiesOfClass(Entity.class, target.getBoundingBox().inflate(range, 0.25, range))) {
                        if (aoeTarget != player && aoeTarget != target && !(aoeTarget instanceof ArmorStand stand && stand.isMarker()) &&
                                target.distanceToSqr(aoeTarget) < rangeSq && (Config.enablePvp.get() || !(aoeTarget instanceof Player))) {
                            consumed = recipe.applyToEntity(fluid, level, FluidEffectContext.builder(world).user(player).target(aoeTarget), IFluidHandler.FluidAction.SIMULATE);
                            if (consumed > 0) {
                                addExperience(tool, 1 + Config.bonusSplashingXp.get());
                                fluid.shrink(consumed);
                                if (fluid.isEmpty()) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleBlockSplashing(IToolStackView tool, UseOnContext context, ModifierEntry splashing) {
        //code based on SplashingModule.afterBlockUse
        EntityInteractionModifierHook hook = splashing.getModifier().getHook(ModifierHooks.ENTITY_INTERACT);
        if (!(hook instanceof SplashingModule splashingModule)) {
            return;
        }

        FluidStack fluid = TANK_HELPER.getFluid(tool);
        if (!fluid.isEmpty()) {
            FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
            if (recipe.hasBlockEffects()) {
                Level world = context.getLevel();
                Player player = context.getPlayer();

                float level = splashingModule.strength().compute(splashing);
                BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                        context.getClickedPos(), context.isInside()
                );
                BlockState state = world.getBlockState(hit.getBlockPos());
                int consumed = recipe.applyToBlock(fluid, level,
                        FluidEffectContext.builder(world)
                                .user(player)
                                .block(hit),
                        IFluidHandler.FluidAction.EXECUTE);
                if (consumed > 0) {
                    addExperience(tool, 1 + Config.bonusSplashingXp.get());
                    fluid.shrink(consumed);
                }

                // AoE
                if (!fluid.isEmpty()) {
                    for (BlockPos offset : tool.getHook(ToolHooks.AOE_ITERATOR)
                            .getBlocks(tool, context, state, AreaOfEffectIterator.AOEMatchType.TRANSFORM)) {
                        BlockHitResult offsetHit = Util.offset(hit, offset);
                        consumed = recipe.applyToBlock(fluid, level,
                                FluidEffectContext.builder(world)
                                        .user(player)
                                        .block(offsetHit),
                                IFluidHandler.FluidAction.EXECUTE);
                        if (consumed > 0) {
                            addExperience(tool, 1 + Config.bonusSplashingXp.get());
                            fluid.shrink(consumed);
                            if (fluid.isEmpty()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleToolSlurping(IToolStackView tool, int useTime, ServerPlayer player,
                                    ModifierEntry slurping) {
        int duration = slurping.getHook(ModifierHooks.GENERAL_INTERACT).getUseDuration(tool, slurping);
        if (useTime == duration) {
            if (canSlurp(tool, player, slurping)) {
                addExperience(tool, 1 + Config.bonusSlurpingXp.get());
            }
        }
    }

    private void handleArmorSlurping(IToolStackView tool, ServerPlayer player, ModifierEntry slurping) {
        if (tool.hasTag(TinkerTags.Items.WORN_ARMOR)) {
            int finishTime = tool.getPersistentData().getInt(ModifierIds.slurping);
            if (finishTime > 0 && finishTime - player.tickCount < 0) {
                if (canSlurp(tool, player, slurping)) {
                    addExperience(tool, 1 + Config.bonusSlurpingXp.get());
                }
            }
        }
    }

    private static void handleTargetMelting(IToolStackView tool, LivingEntity target) {
        if (target == null) {
            return;
        }
        EntityMeltingRecipe recipe = EntityMeltingRecipeCache.findRecipe(target.level().getRecipeManager(), target.getType());
        FluidStack output;
        if (recipe != null) {
            output = recipe.getOutput(target);
        } else {
            output = EntityMeltingModule.getDefaultFluid();
        }
        FluidStack fluid = TANK_HELPER.getFluid(tool);
        if (fluid.isEmpty() || fluid.isFluidEqual(output)) {
            addExperience(tool, 1 + Config.bonusMeltingXp.get());
        }
    }

    private void handleLootMelting(IToolStackView tool, List<ItemStack> generatedLoot, LootContext context, ModifierEntry melting) {
        FluidStack current = TANK_HELPER.getFluid(tool);
        int capacity = TANK_HELPER.getCapacity(tool);
        if (current.getAmount() >= capacity) {
            return;
        }

        boolean forceMelt = tool.getVolatileData().getBoolean(MeltingModule.FORCE_MELTING);
        if (forceMelt && context.hasParam(LootContextParams.BLOCK_STATE)) {
            BlockState state = context.getParam(LootContextParams.BLOCK_STATE);
            forceMelt = tool.getHook(ToolHooks.IS_EFFECTIVE).isToolEffective(tool, state);
        }

        Level world = context.getLevel();
        for (ItemStack stack : generatedLoot) {
            FluidStack output = getMeltItemResult(melting, stack, world);
            if (!output.isEmpty() && (current.isEmpty() || current.isFluidEqual(output))) {
                if (forceMelt) {
                    addExperience(tool, 1 + Config.bonusMeltingXp.get());
                } else {
                    int maxCopies = Math.min((capacity - current.getAmount()) / output.getAmount(), stack.getCount());
                    if (maxCopies <= 0) {
                        continue;
                    }
                    addExperience(tool, 1 + Config.bonusMeltingXp.get());
                }
            } else if (forceMelt) {
                addExperience(tool, 1 + Config.bonusMeltingXp.get());
            }
        }
    }
}
