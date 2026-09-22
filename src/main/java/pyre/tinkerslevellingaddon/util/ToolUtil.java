package pyre.tinkerslevellingaddon.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.setup.Registration;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.hook.build.ConditionalStatModifierHook;
import slimeknights.tconstruct.library.modifiers.hook.interaction.GeneralInteractionModifierHook;
import slimeknights.tconstruct.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.data.ModifierIds;

public class ToolUtil {

    public static float getWalkerTrueRadius(IToolStackView tool, ModifierEntry entry, ArmorWalkRadiusModule<?> module) {
        return Math.min(16, module.getRadius(tool, entry));
    }

    public static boolean isAttackAllowed(ToolAttackContext context, IToolStackView tool) {
        ModifierEntry activeModifier = GeneralInteractionModifierHook.getActiveModifier(tool);
        return !context.isExtraAttack() || activeModifier == ModifierEntry.EMPTY ||
                ModifierIds.bonking.equals(activeModifier.getId());
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

    public static boolean isDifferentTinkersItem(IToolStackView item1, IToolStackView item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return true;
        }
        return !item1.getModifiers().equals(item2.getModifiers()) || !item1.getMaterials().equals(item2.getMaterials());
    }

    public static boolean hasImprovable(ItemStack activeStack) {
        return ModifierUtil.getModifierLevel(activeStack, Registration.IMPROVABLE.get().getId()) > 0;
    }

    private ToolUtil() {
        //hide constructor
    }
}
