package pyre.tinkerslevellingaddon.util;

import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.modules.armor.ArmorWalkRadiusModule;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

public class ToolUtil {

    public static float getWalkerTrueRadius(IToolStackView tool, ModifierEntry entry, ArmorWalkRadiusModule<?> module) {
        return Math.min(16, module.getRadius(tool, entry));
    }

    private ToolUtil() {
        //hide constructor
    }
}
