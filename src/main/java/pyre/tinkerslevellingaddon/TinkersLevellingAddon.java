package pyre.tinkerslevellingaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.slf4j.Logger;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.data.ModifierRecipeProvider;
import pyre.tinkerslevellingaddon.setup.Registration;

@Mod(TinkersLevellingAddon.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TinkersLevellingAddon {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "tinkerslevellingaddon";

    public TinkersLevellingAddon() {
        Config.init();
        Registration.init();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new ModifierRecipeProvider(generator));
        }
    }
}
