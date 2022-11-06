package pyre.tinkerslevellingaddon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.context.ToolHarvestContext;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.RestrictedCompoundTag;

public class ImprovementModifier extends NoLevelsModifier {

    public static final ResourceLocation EXPERIENCE_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "experience");
    public static final ResourceLocation LEVEL_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "level");
    public static final ResourceLocation BONUS_SLOTS_KEY = new ResourceLocation(TinkersLevellingAddon.MOD_ID, "bonus_slots");

    @Override
    public void addRawData(IToolStackView tool, int level, RestrictedCompoundTag tag) {
        if (tool.getPersistentData().getInt(LEVEL_KEY) <= 0) {
            tool.getPersistentData().putInt(LEVEL_KEY, 0);
        }
    }

    @Override
    public void beforeRemoved(IToolStackView tool, RestrictedCompoundTag tag) {
        tool.getPersistentData().remove(EXPERIENCE_KEY);
        tool.getPersistentData().remove(LEVEL_KEY);
        tool.getPersistentData().remove(BONUS_SLOTS_KEY);
    }

    @Override
    public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
        IModDataView data = context.getPersistentData();
        int bonusSlots = data.getInt(BONUS_SLOTS_KEY);
        //todo add different slots
        volatileData.addSlots(SlotType.UPGRADE, bonusSlots);
    }

    @Override
    public void afterBlockBreak(IToolStackView tool, int level, ToolHarvestContext context) {
        if (context.isEffective() && context.getPlayer() != null) {
            addExperience(tool, 1, context.getPlayer());
        }
    }

    private void addExperience(IToolStackView tool, int amount, ServerPlayer player) {
        ModDataNBT data = tool.getPersistentData();
        //todo check can level up

        int currentExperience = data.getInt(EXPERIENCE_KEY) + amount;
        data.putInt(EXPERIENCE_KEY, currentExperience);
        int experienceNeeded = getXpForLevel(data.getInt(LEVEL_KEY) + 1);
        if (currentExperience >= experienceNeeded) {
            data.putInt(EXPERIENCE_KEY, data.getInt(EXPERIENCE_KEY) - experienceNeeded);
            data.putInt(LEVEL_KEY, data.getInt(LEVEL_KEY) + 1);
            data.putInt(BONUS_SLOTS_KEY, data.getInt(BONUS_SLOTS_KEY) + 1);

            //todo add player feedback (message, sound)
            ToolStack heldTool = getHeldTool(player, player.getUsedItemHand());
            if (isEqualTinkersItem(tool, heldTool)) {
                heldTool.rebuildStats();
            }
        }
    }

    private boolean isEqualTinkersItem(IToolStackView item1, IToolStackView item2) {
        if(item1 == null || item2 == null || item1.getItem() != item2.getItem()) {
            return false;
        }
        return item1.getModifiers().equals(item2.getModifiers()) && item1.getMaterials().equals(item2.getMaterials());
    }

    public static int getXpForLevel(int level) {
        return level * 10; //todo config
    }
}
