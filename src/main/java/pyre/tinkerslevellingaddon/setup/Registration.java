package pyre.tinkerslevellingaddon.setup;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pyre.tinkerslevellingaddon.ImprovableModifier;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.modifiers.Modifier;

public class Registration {

    private static final DeferredRegister<Modifier> MODIFIERS = DeferredRegister.create(Modifier.class, TinkersLevellingAddon.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, TinkersLevellingAddon.MOD_ID);

    public static final RegistryObject<ImprovableModifier> IMPROVABLE = MODIFIERS.register("improvable", ImprovableModifier::new);

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
