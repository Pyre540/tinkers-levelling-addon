package pyre.tinkerslevellingaddon;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.data.ModifierRecipeProvider;
import pyre.tinkerslevellingaddon.network.Messages;
import pyre.tinkerslevellingaddon.setup.Registration;

@Mod(TinkersLevellingAddon.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TinkersLevellingAddon {

    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "tinkerslevellingaddon";

    public TinkersLevellingAddon() {
        Config.init();
        Registration.init();
        Messages.register();
    }

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new ModifierRecipeProvider(generator));
        }
    }
}
