package pyre.tinkerslevellingaddon;

import com.mojang.logging.LogUtils;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import pyre.tinkerslevellingaddon.command.ModCommands;
import pyre.tinkerslevellingaddon.config.Config;
import pyre.tinkerslevellingaddon.data.ColorProvider;
import pyre.tinkerslevellingaddon.data.ModifierProvider;
import pyre.tinkerslevellingaddon.data.ModifierRecipeProvider;
import pyre.tinkerslevellingaddon.data.ModifierTagProvider;
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
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();
        PackOutput packOutput = generator.getPackOutput();

        boolean server = event.includeServer();
        generator.addProvider(server, new ModifierTagProvider(packOutput, existingFileHelper));
        generator.addProvider(server, new ModifierProvider(packOutput));
        generator.addProvider(server, new ModifierRecipeProvider(packOutput));

        boolean client = event.includeClient();
        generator.addProvider(client, new ColorProvider(packOutput));
    }
}
