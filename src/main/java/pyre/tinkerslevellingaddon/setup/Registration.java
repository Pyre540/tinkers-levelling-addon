package pyre.tinkerslevellingaddon.setup;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pyre.tinkerslevellingaddon.ImprovementModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public class Registration {

    private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TinkersLevellingAddon.MOD_ID);

    public static final StaticModifier<ImprovementModifier> improvement = MODIFIERS.register("improvement", ImprovementModifier::new);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MODIFIERS.register(bus);
    }
}
