package pyre.tinkerslevellingaddon.data;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.crafting.conditions.IConditionBuilder;
import pyre.tinkerslevellingaddon.TinkersLevellingAddon;
import slimeknights.tconstruct.library.data.recipe.IRecipeHelper;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;

import java.util.function.Consumer;

public class ModifierRecipeProvider extends RecipeProvider implements IConditionBuilder, IRecipeHelper {

    public ModifierRecipeProvider(DataGenerator generator) {
        super(generator);
    }

    @Override
    public String getName() {
        return "Tinkers' Levelling Addon Modifier Recipes";
    }

    @Override
    protected void buildCraftingRecipes(Consumer<FinishedRecipe> consumer) {
        String abilityFolder = "tools/modifiers/ability/";
        String abilitySalvage = "tools/modifiers/salvage/ability/";

        ModifierId improvableId = new ModifierId(TinkersLevellingAddon.MOD_ID, "improvable");
        ModifierRecipeBuilder.modifier(improvableId)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.NETHER_STAR)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .addInput(Items.EXPERIENCE_BOTTLE)
                .setSlots(SlotType.ABILITY, 1)
                .saveSalvage(consumer, prefix(improvableId, abilitySalvage))
                .save(consumer, prefix(improvableId, abilityFolder));
    }

    @Override
    public String getModId() {
        return TinkersLevellingAddon.MOD_ID;
    }
}
