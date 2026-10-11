package pyre.tinkerslevellingaddon.setup;

import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.modifiers.ModifierId;

public class AddonModifierIds {

    public static ModifierId IMPROVABLE = create("improvable");

    public static ModifierId create(String name) {
        return new ModifierId(TinkersLevellingAddon.MOD_ID, name);
    }

    private AddonModifierIds() {
        //hide constructor
    }
}
