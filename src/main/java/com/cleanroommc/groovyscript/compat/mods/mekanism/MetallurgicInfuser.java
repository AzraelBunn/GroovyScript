package com.cleanroommc.groovyscript.compat.mods.mekanism;

import com.cleanroommc.groovyscript.api.GroovyLog;
import com.cleanroommc.groovyscript.api.IIngredient;
import com.cleanroommc.groovyscript.api.documentation.annotations.*;
import com.cleanroommc.groovyscript.compat.mods.ModSupport;
import com.cleanroommc.groovyscript.compat.mods.mekanism.recipe.VirtualizedMekanismRegistry;
import com.cleanroommc.groovyscript.helper.ingredient.IngredientHelper;
import com.cleanroommc.groovyscript.helper.recipe.AbstractRecipeBuilder;
import mekanism.api.infuse.InfuseRegistry;
import mekanism.api.infuse.InfuseType;
import mekanism.common.recipe.RecipeHandler;
import mekanism.common.recipe.inputs.InfusionInput;
import mekanism.common.recipe.machines.MetallurgicInfuserRecipe;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

@RegistryDescription
public class MetallurgicInfuser extends VirtualizedMekanismRegistry<MetallurgicInfuserRecipe> {

    public MetallurgicInfuser() {
        super(RecipeHandler.Recipe.METALLURGIC_INFUSER);
    }

    @RecipeBuilderDescription(example = @Example(".input(item('minecraft:nether_star')).infuse(infusionType('groovy_example')).amount(50).output(item('minecraft:clay'))"))
    public RecipeBuilder recipeBuilder() {
        return new RecipeBuilder();
    }

    @MethodDescription(description = "groovyscript.wiki.mekanism.metallurgic_infuser.add0", type = MethodDescription.Type.ADDITION, example = @Example(value = "item('minecraft:nether_star'), infusionType('groovy_example'), 50, item('minecraft:clay')", commented = true))
    public MetallurgicInfuserRecipe add(IIngredient ingredient, InfuseType infuseType, int infuseAmount, ItemStack output) {
        return recipeBuilder().infuse(infuseType).amount(infuseAmount).output(output).input(ingredient).register();
    }

    @MethodDescription(description = "groovyscript.wiki.mekanism.metallurgic_infuser.add1")
    public MetallurgicInfuserRecipe add(IIngredient ingredient, String infuseType, int infuseAmount, ItemStack output) {
        return recipeBuilder().infuse(infuseType).amount(infuseAmount).output(output).input(ingredient).register();
    }

    @MethodDescription(example = @Example("ore('dustObsidian'), 'DIAMOND'"))
    public boolean removeByInput(IIngredient ingredient, InfuseType infuseType) {
        GroovyLog.Msg msg = GroovyLog.msg("Error removing Mekanism Metallurgic Infuser recipe").error();
        msg.add(IngredientHelper.isEmpty(ingredient), () -> "input must not be empty");
        msg.add(infuseType == null, () -> "invalid infusion type");
        if (msg.postIfNotEmpty()) return false;

        boolean found = false;
        for (ItemStack itemStack : ingredient.getMatchingStacks()) {
            // infuse amount is not hashed so we don't need it
            MetallurgicInfuserRecipe recipe = recipeRegistry.get().remove(new InfusionInput(infuseType, 1, itemStack));
            if (recipe != null) {
                addBackup(recipe);
                found = true;
            }
        }
        if (!found) {
            removeError("could not find recipe for {}", ingredient);
        }
        return found;
    }

    public boolean removeByInput(IIngredient ingredient, String infuseType) {
        return removeByInput(ingredient, InfuseRegistry.get(infuseType));
    }


    @Property(property = "input", valid = @Comp("1"))
    @Property(property = "output", valid = @Comp("1"))
    public static class RecipeBuilder extends AbstractRecipeBuilder<MetallurgicInfuserRecipe> {

        @Property(valid = @Comp(type = Comp.Type.NOT, value = "null"))
        private InfuseType infuse;
        @Property(valid = @Comp(type = Comp.Type.GT, value = "0"))
        private int amount;

        @RecipeBuilderMethodDescription
        public RecipeBuilder infuse(InfuseType infuse) {
            this.infuse = infuse;
            return this;
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder infuse(String infuse) {
            return infuse(InfuseRegistry.get(infuse));
        }

        @RecipeBuilderMethodDescription
        public RecipeBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        @Override
        public String getErrorMsg() {
            return "Error adding Mekanism Metallurgic Infuser recipe";
        }

        @Override
        public void validate(GroovyLog.Msg msg) {
            validateItems(msg, 1, 1, 1, 1);
            validateFluids(msg);
            msg.add(infuse == null, "infuse must be defined");
            msg.add(amount <= 0, "amount must be an integer greater than 0, yet it was {}", amount);
        }

        @Override
        @RecipeBuilderRegistrationMethod
        public @Nullable MetallurgicInfuserRecipe register() {
            if (!validate()) return null;
            MetallurgicInfuserRecipe recipe = null;
            for (ItemStack itemStack : input.get(0).getMatchingStacks()) {
                MetallurgicInfuserRecipe r = new MetallurgicInfuserRecipe(new InfusionInput(infuse, amount, itemStack), output.get(0));
                if (recipe == null) recipe = r;
                ModSupport.MEKANISM.get().metallurgicInfuser.add(r);
            }
            return recipe;
        }
    }
}
