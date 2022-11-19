package pyre.tinkerslevellingaddon.setup;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.modifiers.util.ModifierDeferredRegister;
import slimeknights.tconstruct.library.modifiers.util.StaticModifier;

public class Registration {

    private static final ModifierDeferredRegister MODIFIERS = ModifierDeferredRegister.create(TinkersLevellingAddon.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TinkersLevellingAddon.MOD_ID);

    public static final StaticModifier<ImprovableModifier> IMPROVABLE = MODIFIERS.register("improvable", ImprovableModifier::new);

    public static final RegistryObject<SoundEvent> SOUND_TOOL_LEVEL_UP_CHIME = registerSoundEvent("tool_level_up_chime");
    public static final RegistryObject<SoundEvent> SOUND_TOOL_LEVEL_UP_SNARE_DRUM = registerSoundEvent("tool_level_up_snare_drum");
    public static final RegistryObject<SoundEvent> SOUND_TOOL_LEVEL_UP_YAY = registerSoundEvent("tool_level_up_yay");

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        MODIFIERS.register(bus);
        SOUND_EVENTS.register(bus);
    }

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name,
                () -> new SoundEvent(new ResourceLocation(TinkersLevellingAddon.MOD_ID, name)));
    }
}
