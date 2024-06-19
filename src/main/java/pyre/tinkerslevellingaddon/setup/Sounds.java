package pyre.tinkerslevellingaddon.setup;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import pyre.tinkerslevellingaddon.util.ModUtil;

@Mod.EventBusSubscriber(modid = TinkersLevellingAddon.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public enum Sounds {
    CHIME("tool_level_up_chime"),
    SNARE_DRUM("tool_level_up_snare_drum"),
    YAY("tool_level_up_yay");
    
    private final SoundEvent sound;
    
    Sounds(String name) {
        ResourceLocation registryName = ModUtil.getResource(name);
        sound = new SoundEvent(registryName);
    }
    
    @SubscribeEvent
    public static void registerSounds(RegisterEvent event) {
        if (event.getRegistryKey() == Registry.SOUND_EVENT_REGISTRY) {
            for (Sounds sound : values()) {
                ForgeRegistries.SOUND_EVENTS.register(sound.sound.getLocation(), sound.getSound());
            }
        }
    }
    
    public SoundEvent getSound() {
        return sound;
    }
}
