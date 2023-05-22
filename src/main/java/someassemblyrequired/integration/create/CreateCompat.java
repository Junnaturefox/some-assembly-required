package someassemblyrequired.integration.create;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.compat.jei.CreateJEI;
import com.simibubi.create.content.fluids.potion.PotionFluidHandler;
import com.simibubi.create.content.fluids.transfer.FillingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerRecipeSearchEvent;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import someassemblyrequired.common.ingredient.Ingredients;
import someassemblyrequired.common.init.ModItems;
import someassemblyrequired.common.init.ModRecipeTypes;
import someassemblyrequired.common.item.sandwich.SandwichItem;
import someassemblyrequired.common.item.sandwich.SandwichItemHandler;
import someassemblyrequired.common.util.Util;
import someassemblyrequired.integration.create.ingredient.BuildersTeaBehavior;
import someassemblyrequired.integration.create.recipe.SandwichFluidSpoutingRecipe;
import someassemblyrequired.integration.create.recipe.deployer.SandwichDeployingRecipe;

import java.util.HashSet;
import java.util.stream.Stream;

public class CreateCompat {

    public static void setup() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(CreateCompat::onCommonSetup);

        MinecraftForge.EVENT_BUS.addListener(CreateCompat::onDeployerRecipeSearch);
    }

    public static void onCommonSetup(FMLCommonSetupEvent event) {
        Ingredients.addBehavior(AllItems.BUILDERS_TEA.get(), new BuildersTeaBehavior());
    }

    public static void onDeployerRecipeSearch(DeployerRecipeSearchEvent event) {
        event.addRecipe(() -> SandwichDeployingRecipe.createRecipe(event.getInventory()), 150);
    }

    public static HashSet<Recipe<?>> createSandwichAssemblingRecipes() {
        NonNullList<ItemStack> sandwiches = NonNullList.create();
        ModItems.SANDWICH.get().fillItemCategory(ModItems.CREATIVE_TAB, sandwiches);
        HashSet<Recipe<?>> recipes = new HashSet<>();

        for (ItemStack sandwich : sandwiches) {
            recipes.add(createSandwichRecipe(sandwich, "sandwich_deploying"));
        }

        Stream.of(
                Potions.NIGHT_VISION,
                Potions.INVISIBILITY,
                Potions.LEAPING,
                Potions.FIRE_RESISTANCE,
                Potions.SWIFTNESS,
                Potions.SLOWNESS,
                Potions.TURTLE_MASTER,
                Potions.WATER_BREATHING,
                Potions.HEALING,
                Potions.HARMING,
                Potions.POISON,
                Potions.REGENERATION,
                Potions.STRENGTH,
                Potions.WEAKNESS,
                Potions.SLOW_FALLING
        )
                .map(SandwichItem::makeSandwich)
                .map(sandwich -> createSandwichRecipe(sandwich, "sequenced_assembly/sandwich_potions"))
                .forEach(recipes::add);

        CreateJEI.getTypedRecipesExcluding(ModRecipeTypes.SANDWICH_SPOUTING, recipe -> recipe.getSerializer() != ModRecipeTypes.SANDWICH_FLUID_SPOUTING_SERIALIZER.get())
                .stream()
                .map(recipe -> (SandwichFluidSpoutingRecipe) recipe)
                .map(recipe -> builder(SandwichItem.makeSandwich(recipe.getResultItem()), "sandwich_spouting")
                        .addStep(FillingRecipe::new, r -> r.require(recipe.getIngredient()))
                        .addStep(DeployerApplicationRecipe::new, r -> r.require(ModItems.BREAD_SLICE.get()))
                        .build()
                ).forEach(recipes::add);

        Stream.of(
                ModItems.TOMATO_SLICES.get(),
                ModItems.CHOPPED_BEETROOT.get(),
                ModItems.CHOPPED_CARROT.get(),
                ModItems.CHOPPED_GOLDEN_CARROT.get(),
                ModItems.APPLE_SLICES.get(),
                ModItems.GOLDEN_APPLE_SLICES.get(),
                ModItems.ENCHANTED_GOLDEN_APPLE_SLICES.get()
        )
                .map(SandwichItem::makeSandwich)
                .map(sandwich -> createSandwichRecipe(sandwich, "sandwich_deploying"))
                .forEach(recipes::add);

        return recipes;
    }

    private static Recipe<?> createSandwichRecipe(ItemStack sandwich, String name) {
        SequencedAssemblyRecipeBuilder builder = builder(sandwich, name);
        SandwichItemHandler handler = SandwichItemHandler.get(sandwich).orElseThrow();

        for (int j = 1; j < handler.getItems().size(); j++) {
            ItemStack ingredient = handler.getStackInSlot(j);
            if (ingredient.is(Items.POTION)) {
                Potion potion = PotionUtils.getPotion(ingredient);
                builder.addStep(FillingRecipe::new, recipe -> recipe.require(PotionFluidHandler.potionIngredient(potion, PotionFluidHandler.getRequiredAmountForFilledBottle(null, null))));
            } else if (ingredient.is(Items.HONEY_BOTTLE)) {
                builder.addStep(FillingRecipe::new, recipe -> recipe.require(AllFluids.HONEY.get(), 250));
            } else {
                builder.addStep(DeployerApplicationRecipe::new, recipe -> recipe.require(Ingredient.of(ingredient)));
            }
        }

        return builder.build();
    }

    private static SequencedAssemblyRecipeBuilder builder(ItemStack sandwich, String name) {
        return new SequencedAssemblyRecipeBuilder(Util.id("sequenced_assembly/%s".formatted(name)))
                .require(Ingredient.of(SandwichItemHandler.get(sandwich).map(s -> s.getStackInSlot(0)).orElseThrow()))
                .transitionTo(ModItems.SANDWICH.get())
                .loops(1)
                .addOutput(sandwich, 1);
    }
}
