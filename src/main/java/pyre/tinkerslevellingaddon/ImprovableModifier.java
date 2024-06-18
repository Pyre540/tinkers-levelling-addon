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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
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
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.InteractionSource;
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
import slimeknights.tconstruct.library.tools.definition.module.aoe.AreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.module.aoe.CircleAOEIterator;
import slimeknights.tconstruct.library.tools.definition.module.weapon.CircleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.module.weapon.MeleeHitToolHook;
import slimeknights.tconstruct.library.tools.definition.module.weapon.SweepWeaponAttack;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.nbt.*;
import slimeknights.tconstruct.library.tools.stat.FloatToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.MutableUseOnContext;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.GlowBlock;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modifiers.ability.armor.walker.FlamewakeModifier;

import java.util.Collections;
import java.util.List;

import static pyre.tinkerslevellingaddon.util.ToolLevellingUtil.addExperience;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ImprovableModifier extends NoLevelsModifier implements PlantHarvestModifierHook, ShearsModifierHook,
        BlockBreakModifierHook, BlockTransformModifierHook, ProjectileLaunchModifierHook, OnAttackedModifierHook,
        MeleeHitModifierHook, ElytraFlightModifierHook, ArmorWalkModifierHook, ModifierRemovalHook,
        VolatileDataModifierHook, ToolStatsModifierHook {
    
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
                ModifierHooks.BOOT_WALK, ModifierHooks.VOLATILE_DATA, ModifierHooks.TOOL_STATS, ModifierHooks.REMOVE);
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
                !isAttackAllowed(context, tool) || (!Config.enablePvp.get() && context.getLivingTarget() instanceof Player) ||
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
    
    //many interaction modifiers do not use blockTransform hook, so we need to use other methods
    @SubscribeEvent
    static void onStopUsing(LivingEntityUseItemEvent.Stop event) {
        ItemStack item = event.getItem();
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || item.isEmpty()
                || !item.is(TinkerTags.Items.MODIFIABLE)) {
            return;
        }
        ToolStack tool = ToolStack.from(item);
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        if (activeModifier == ModifierEntry.EMPTY) {
            return;
        }
        if (Config.enableFlingingXp.get() && TinkerModifiers.flinging.getId().equals(activeModifier.getId())
                && player.isOnGround()) {
            handleFlinging(player, tool);
            return;
        }
        if (Config.enableSpringingXp.get() && TinkerModifiers.springing.getId().equals(activeModifier.getId())
                && !player.isFallFlying()) {
            handleSpringing(player, tool);
            return;
        }
        if (Config.enableBonkingXp.get() && TinkerModifiers.bonking.getId().equals(activeModifier.getId())) {
            handleBonking(player, tool);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onClickEntity(PlayerInteractEvent.EntityInteract event) {
        //right click entity
        ItemStack item = event.getItemStack();
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || item.isEmpty()
                || !item.is(TinkerTags.Items.MODIFIABLE)) {
            return;
        }
        ToolStack tool = ToolStack.from(item);
        
        if (Config.enableFirestarterXp.get() && !tool.isBroken() && event.getTarget() instanceof Creeper
                && tool.getModifier(TinkerModifiers.firestarter.getId()) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, TinkerModifiers.firestarter.getId(),
                InteractionSource.RIGHT_CLICK)) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get(), player);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onAttackEntity(AttackEntityEvent event) {
        //left click entity
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || player.isSpectator()) {
            return;
        }
        ItemStack item = player.getMainHandItem();
        if (item.isEmpty() || !item.is(TinkerTags.Items.MODIFIABLE) || !item.is(TinkerTags.Items.INTERACTABLE_LEFT)
                || player.getCooldowns().isOnCooldown(item.getItem())) {
            return;
        }
        ToolStack tool = ToolStack.from(item);
        
        if (Config.enableFirestarterXp.get() && !tool.isBroken() && event.getTarget() instanceof Creeper
                && tool.getModifier(TinkerModifiers.firestarter.getId()) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, TinkerModifiers.firestarter.getId(),
                InteractionSource.LEFT_CLICK)) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get(), player);
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        ItemStack item = event.getItemStack();
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || item.isEmpty()
                || !item.is(TinkerTags.Items.MODIFIABLE)) {
            return;
        }
        
        handleBlockClick(event, player, ToolStack.from(item), InteractionSource.RIGHT_CLICK);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack item = event.getItemStack();
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || item.isEmpty()
                || !item.is(TinkerTags.Items.MODIFIABLE) || !item.is(TinkerTags.Items.INTERACTABLE_LEFT)) {
            return;
        }
        
        handleBlockClick(event, player, ToolStack.from(item), InteractionSource.LEFT_CLICK);
    }
    
    private boolean isEqualTinkersItem(IToolStackView item1, IToolStackView item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        return item1.getModifiers().equals(item2.getModifiers()) && item1.getMaterials().equals(item2.getMaterials());
    }
    
    private boolean isAttackAllowed(ToolAttackContext context, IToolStackView tool) {
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        return !context.isExtraAttack() || activeModifier == ModifierEntry.EMPTY ||
                TinkerModifiers.bonking.getId().equals(activeModifier.getId());
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
    
    private static void handleFlinging(ServerPlayer player, ToolStack tool) {
        //code based on FlingingModifier.onStoppedUsing
        BlockHitResult mop = ModifiableItem.blockRayTrace(player.getLevel(), player, ClipContext.Fluid.NONE);
        if (mop.getType() == HitResult.Type.BLOCK) {
            //ignoring 'force' check since we cannot access it
            addExperience(tool, 1 + Config.bonusFlingingXp.get(), player);
        }
    }
    
    private static void handleSpringing(ServerPlayer player, ToolStack tool) {
        //code based on SpringingModifier.onStoppedUsing
        //ignoring 'force' check since we cannot access it
        addExperience(tool, 1 + Config.bonusSpringingXp.get(), player);
    }
    
    private static void handleBonking(ServerPlayer player, ToolStack tool) {
        //code based on BonkingModifier.onStoppedUsing
        //ignoring 'force' check since we cannot access it
        float range = 5F;
        Vec3 start = player.getEyePosition(1F);
        Vec3 look = player.getLookAngle();
        Vec3 direction = start.add(look.x * range, look.y * range, look.z * range);
        AABB bb = player.getBoundingBox().expandTowards(look.x * range, look.y * range, look.z * range)
                .expandTowards(1, 1, 1);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player.level, player, start, direction, bb,
                (e) -> e instanceof LivingEntity);
        if (hit != null) {
            LivingEntity target = (LivingEntity) hit.getEntity();
            double targetDist = start.distanceToSqr(target.getEyePosition(1F));
            BlockHitResult mop = ModifiableItem.blockRayTrace(player.level, player, ClipContext.Fluid.NONE);
            if (mop.getType() != HitResult.Type.BLOCK || targetDist < mop.getBlockPos().distToCenterSqr(start)) {
                addExperience(tool, 1 + Config.bonusBonkingXp.get(), player);
            }
        }
    }
    
    private static void handleBlockClick(PlayerInteractEvent event, ServerPlayer player, ToolStack tool, InteractionSource clickSource) {
        if (Config.enableGlowingXp.get() && tool.getCurrentDurability() >= 10 && event.getFace() != null
                && tool.getModifier(TinkerModifiers.glowing.getId()) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, TinkerModifiers.glowing.getId(),
                clickSource)) {
            handleGlowing(event, player, tool);
            return;
        }
        if (Config.enableFirestarterXp.get() && !tool.isBroken() && event.getFace() != null
                && tool.getModifier(TinkerModifiers.firestarter.getId()) != ModifierEntry.EMPTY
                && tool.getHook(ToolHooks.INTERACTION).canInteract(tool, TinkerModifiers.firestarter.getId(),
                clickSource)) {
            handleFirestarter(event, player, tool);
        }
    }
    
    private static void handleGlowing(PlayerInteractEvent event, ServerPlayer player, ToolStack tool) {
        Level world = event.getLevel();
        Direction face = event.getFace();
        BlockPos pos = event.getPos().relative(face);
        if (canPlaceGlow(world, pos, face.getOpposite())) {
            addExperience(tool, 1 + Config.bonusGlowingXp.get(), player);
        }
    }
    
    private static boolean canPlaceGlow(Level world, BlockPos pos, Direction direction) {
        BlockState state = world.getBlockState(pos);
        GlowBlock glowBlock = TinkerCommons.glow.get();
        if (state.getBlock() != glowBlock && state.getMaterial().isReplaceable()) {
            if (canGlowBlockStay(world, pos, direction)) {
                return true;
            } else {
                for (Direction direction1 : Direction.values()) {
                    if (canGlowBlockStay(world, pos, direction1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private static boolean canGlowBlockStay(Level world, BlockPos pos, Direction facing) {
        BlockPos placedOn = pos.relative(facing);
        
        boolean isSolidSide = Block.isFaceFull(world.getBlockState(placedOn)
                .getOcclusionShape(world, pos), facing.getOpposite());
        boolean isLiquid = world.getBlockState(pos).getBlock() instanceof LiquidBlock;
        
        return !isLiquid && isSolidSide;
    }
    
    private static void handleFirestarter(PlayerInteractEvent event, ServerPlayer player, ToolStack tool) {
        Level world = event.getLevel();
        BlockPos pos = event.getPos();
        Direction sideHit = event.getFace();
        BlockState state = world.getBlockState(pos);
        
        boolean targetingFire = false;
        if (state.is(BlockTags.FIRE)) {
            pos = pos.relative(sideHit.getOpposite());
            targetingFire = true;
        }
        
        int range = tool.getModifierLevel(TinkerModifiers.fireprimer.getId()) + tool.getModifierLevel(TinkerModifiers.expanded.getId());
        Iterable<BlockPos> targets = Collections.emptyList();
        if (range > 0) {
            targets = CircleAOEIterator.calculate(tool, ItemStack.EMPTY, world, player, pos, sideHit, 1 + range,
                    true, AreaOfEffectIterator.AOEMatchType.TRANSFORM);
        }
        
        Direction horizontalFacing = player.getDirection();
        if (!targetingFire && canIgnite(world, pos, state, sideHit, horizontalFacing)) {
            addExperience(tool, 1 + Config.bonusFirestarterXp.get(), player);
        }
        for (BlockPos target : targets) {
            if (canIgnite(world, target, world.getBlockState(target), sideHit, horizontalFacing)) {
                addExperience(tool, 1 + Config.bonusFirestarterXp.get(), player);
            }
        }
    }
    
    private static boolean canIgnite(Level world, BlockPos pos, BlockState state, Direction sideHit, Direction facing) {
        return CampfireBlock.canLight(state) || CandleBlock.canLight(state) || CandleCakeBlock.canLight(state)
                || state.getBlock() instanceof TntBlock || BaseFireBlock.canBePlacedAt(world, pos.relative(sideHit), facing);
    }
}
