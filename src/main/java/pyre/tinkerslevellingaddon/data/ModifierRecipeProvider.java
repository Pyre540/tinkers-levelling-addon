package pyre.tinkerslevellingaddon.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.IFinishedRecipe;
import net.minecraft.item.Items;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import pyre.tinkerslevellingaddon.setup.Registration;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;

import java.util.function.Consumer;

public class ModifierRecipeProvider extends BaseRecipeProvider {

    public ModifierRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public String getName() {
        return "Tinkers' Levelling Addon Modifier Recipes";
    }

    @Override
    protected void buildShapelessRecipes(Consumer<IFinishedRecipe> consumer) {
        String abilityFolder = "tools/modifiers/ability/";
        String abilitySalvage = "tools/modifiers/salvage/ability/";

        ModifierRecipeBuilder.modifier(Registration.IMPROVABLE.get())
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.NETHER_STAR)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .setSlots(SlotType.ABILITY, 1)
                .setMaxLevel(1)
                .buildSalvage(consumer, prefix(Registration.IMPROVABLE, abilitySalvage))
                .build(consumer, prefix(Registration.IMPROVABLE, abilityFolder));
    }

    @Override
    public String getModId() {
        return TinkersLevellingAddon.MOD_ID;
    }
}
