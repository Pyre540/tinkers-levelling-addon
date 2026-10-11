package pyre.tinkerslevellingaddon.data;

import net.minecraft.data.PackOutput;
import pyre.tinkerslevellingaddon.ImprovableModule;
import pyre.tinkerslevellingaddon.setup.AddonModifierIds;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierProvider;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;

public class ModifierProvider extends AbstractModifierProvider {

    public ModifierProvider(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void addModifiers() {
        buildModifier(AddonModifierIds.IMPROVABLE)
                .levelDisplay(ModifierLevelDisplay.NO_LEVELS)
                .addModule(ImprovableModule.INSTANCE);
    }

    @Override
    public String getName() {
        return "Tinkers' Levelling Addon Modifier Provider";
    }
}
