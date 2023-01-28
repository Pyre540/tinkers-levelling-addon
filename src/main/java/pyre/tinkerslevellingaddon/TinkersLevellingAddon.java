package pyre.tinkerslevellingaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.slf4j.Logger;
import pyre.tinkerslevellingaddon.command.ModCommands;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.data.ModifierRecipeProvider;
import pyre.tinkerslevellingaddon.network.Messages;
import pyre.tinkerslevellingaddon.setup.Registration;

@Mod(TinkersLevellingAddon.MOD_ID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TinkersLevellingAddon {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "tinkerslevellingaddon";

    public TinkersLevellingAddon() {
        Config.init();
        Registration.init();
        Messages.register();
        ModCommands.init();
    }

    @SubscribeEvent
    static void gatherData(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        if (event.includeServer()) {
            generator.addProvider(new ModifierRecipeProvider(generator));
        }
    }
}
