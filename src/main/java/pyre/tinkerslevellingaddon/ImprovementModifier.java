package pyre.tinkerslevellingaddon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import pyre.tinkerslevellingaddon.config.Config;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

import java.util.List;

public class ImprovementModifier extends NoLevelsModifier {

    public static final ResourceLocation EXPERIENCE_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "experience");
    public static final ResourceLocation LEVEL_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "level");

    @Override
    public void beforeRemoved(IToolStackView tool, RestrictedCompoundTag tag) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
    }

    @Override
    public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
        IModDataView data = context.getPersistentData();
        List<SlotType> slotRotation = context.hasTag(TinkerTags.Items.ARMOR) ? Config.getArmorSlotsRotation() :
                Config.getToolsSlotsRotation();
        int lvl = data.getInt(LEVEL_KEY);
        for (int i = 0; i < lvl; i++) {
            volatileData.addSlots(slotRotation.get(i % slotRotation.size()), 1);
        }
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, int level, ToolHarvestContext context) {
        if (context.isEffective() && context.getPlayer() != null) {
            addExperience(tool, 1, context.getPlayer());
        }
    }

    private void addExperience(IToolStackView tool, int amount, ServerPlayer player) {
        ModDataNBT data = tool.getPersistentData();
        int currentLevel = data.getInt(LEVEL_KEY);

        if (!canLevelUp(currentLevel)) {
            return;
        }

        int currentExperience = data.getInt(EXPERIENCE_KEY) + amount;
        int experienceNeeded = getXpNeededForLevel(currentLevel + 1);
        if (currentExperience >= experienceNeeded) {
            data.putInt(EXPERIENCE_KEY, currentExperience - experienceNeeded);
            data.putInt(LEVEL_KEY, currentLevel + 1);

            //todo add player feedback (message, sound)
            ToolStack heldTool = getHeldTool(player, player.getUsedItemHand());
            if (isEqualTinkersItem(tool, heldTool)) {
                heldTool.rebuildStats();
            }
        } else {
            data.putInt(EXPERIENCE_KEY, currentExperience);
        }
    }

    private boolean isEqualTinkersItem(IToolStackView item1, IToolStackView item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        return item1.getModifiers().equals(item2.getModifiers()) && item1.getMaterials().equals(item2.getMaterials());
    }

    public static int getXpNeededForLevel(int level) {
        int experienceNeeded = Config.baseExperience.get();
        if (level > 1) {
            experienceNeeded = (int) (getXpNeededForLevel(level - 1) * Config.levelMultiplier.get());
        }
        return experienceNeeded;
    }

    public static boolean canLevelUp(int level) {
        return Config.maxLevel.get() == 0 || Config.maxLevel.get() > level;
    }
}
