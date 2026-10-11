package pyre.tinkerslevellingaddon.setup;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import pyre.tinkerslevellingaddon.ImprovableModule;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.modifiers.modules.ModifierModule;

public class Registration {

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        bus.addListener(Registration::registerSerializers);
    }

    private static void registerSerializers(RegisterEvent event) {
        if (event.getRegistryKey() == Registries.RECIPE_SERIALIZER) {
            ModifierModule.LOADER.register(new ResourceLocation(TinkersLevellingAddon.MOD_ID, "improvable"), ImprovableModule.LOADER);
        }
    }

    private Registration() {
        //hide constructor
    }
}
