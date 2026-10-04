package pyre.tinkerslevellingaddon.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.Registration;
import slimeknights.tconstruct.library.json.LevelingValue;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectContext;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffectManager;
import slimeknights.tconstruct.library.modifiers.fluid.FluidEffects;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;

import static slimeknights.tconstruct.library.tools.capability.fluid.ToolTankHelper.TANK_HELPER;

public class ToolUtil {

    public static float getWalkerTrueRadius(IToolStackView tool, ModifierEntry entry, ArmorWalkRadiusModule<?> module) {
        return Math.min(16, module.getRadius(tool, entry));
    }

    public static boolean isAttackAllowed(ToolAttackContext context, IToolStackView tool) {
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        return !context.isExtraAttack() || activeModifier == ModifierEntry.EMPTY ||
                ModifierIds.bonking.equals(activeModifier.getId());
    }

    public static boolean isValidMeleeTarget(ServerPlayer attacker, ToolAttackContext context) {
        return context.getLivingTarget() != null && !(context.getTarget() instanceof ArmorStand) &&
                (Config.enablePvp.get() || !(context.getLivingTarget() instanceof Player)) &&
                !context.getLivingTarget().equals(attacker);
    }

    public static boolean isValidLauncherTarget(ServerPlayer attacker, Entity target) {
        return !(target instanceof ArmorStand) && (Config.enablePvp.get() || !(target instanceof Player)) &&
                !target.equals(attacker);
    }

    public static boolean isValidDamageSource(DamageSource source, Player player) {
        return !source.is(DamageTypeTags.BYPASSES_ARMOR) && source.getEntity() instanceof LivingEntity attacker &&
                !attacker.equals(player) && (Config.enablePvp.get() || !(attacker instanceof Player));
    }

    public static float getDamageBlockedAmount(ServerPlayer player, DamageSource source, float amount) {
        ItemStack activeStack = player.getUseItem();
        if (hasImprovable(activeStack)) {
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

    public static boolean canBlock(ServerPlayer player, @Nullable Vec3 sourcePosition, float blockAngle) {
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

    public static boolean canSlurp(IToolStackView tool, ServerPlayer player, ModifierEntry slurping) {
        FluidStack fluid = TANK_HELPER.getFluid(tool);
        if (!fluid.isEmpty()) {
            FluidEffects recipe = FluidEffectManager.INSTANCE.find(fluid.getFluid());
            return recipe.hasEntityEffects() && recipe.applyToEntity(fluid, LevelingValue.eachLevel(1).compute(slurping),
                    FluidEffectContext.builder(player.level()).user(player, player).target(player),
                    IFluidHandler.FluidAction.SIMULATE) > 0;
        }
        return false;
    }

    public static boolean hasImprovable(ItemStack activeStack) {
        return ModifierUtil.getModifierLevel(activeStack, Registration.IMPROVABLE.get().getId()) > 0;
    }

    public static HitResult calculateHitResult(LivingEntity living) {
        return ProjectileUtil.getHitResultOnViewVector(living, entity -> !entity.isSpectator() && entity.isPickable(),
                living.getAttributeValue(ForgeMod.BLOCK_REACH.get()));
    }

    private ToolUtil() {
        //hide constructor
    }
}
