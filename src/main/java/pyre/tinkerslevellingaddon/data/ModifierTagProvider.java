package pyre.tinkerslevellingaddon.data;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import pyre.tinkerslevellingaddon.setup.AddonModifierIds;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierTagProvider;

public class ModifierTagProvider extends AbstractModifierTagProvider {

    public ModifierTagProvider(PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, TinkersLevellingAddon.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(TinkerTags.Modifiers.GENERAL_ABILITIES).add(AddonModifierIds.IMPROVABLE);
    }

    @Override
    public String getName() {
        return "Tinkers' Levelling Addon Modifier Tags";
    }
}
