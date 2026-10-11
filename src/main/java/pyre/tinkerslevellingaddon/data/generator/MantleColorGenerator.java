package pyre.tinkerslevellingaddon.data.generator;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.library.modifiers.ModifierId;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class MantleColorGenerator extends GenericDataProvider {

    private final Map<String, String> modifiers = new HashMap<>();
    private final String modId;

    public MantleColorGenerator(PackOutput packOutput, String modId) {
        super(packOutput, PackOutput.Target.RESOURCE_PACK, "mantle");
        this.modId = modId;
    }

    protected abstract void addColors();

    protected void addModifier(ModifierId id, String color) {
        modifiers.put(id.getPath(), color);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        addColors();

        Gson gson = JsonHelper.DEFAULT_GSON;

        JsonObject object = new JsonObject();
        JsonElement modifiersElement = gson.toJsonTree(this.modifiers);

        object.add("modifier." + modId, modifiersElement);

        return saveJson(cachedOutput, new ResourceLocation(modId, "colors"), object);
    }

    @Override
    public String getName() {
        return "Mantle Colors Datagen";
    }
}
