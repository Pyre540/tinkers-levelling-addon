package pyre.tinkerslevellingaddon;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(TinkersLevellingAddon.MOD_ID)
public class TinkersLevellingAddon {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String MOD_ID = "tinkerslevellingaddon";

    public TinkersLevellingAddon() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
    }

    private void setup(final FMLCommonSetupEvent event) {

    }
}
