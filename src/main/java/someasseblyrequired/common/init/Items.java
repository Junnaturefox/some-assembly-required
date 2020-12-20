package someasseblyrequired.common.init;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Food;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import someasseblyrequired.SomeAssemblyRequired;
import someasseblyrequired.client.SandwichItemRenderer;
import someasseblyrequired.common.item.SandwichItem;
import someasseblyrequired.common.item.SpreadItem;

@ObjectHolder(SomeAssemblyRequired.MODID)
public class Items {

    @ObjectHolder("sandwich")
    public static Item SANDWICH;
    @ObjectHolder("spread")
    public static Item SPREAD;
    @ObjectHolder("bread_slice")
    public static Item BREAD_SLICE;

    public static void register(IForgeRegistry<Item> registry) {
        registry.registerAll(
                new BlockItem(Blocks.SANDWICH_ASSEMBLY_TABLE, new Item.Properties().group(SomeAssemblyRequired.CREATIVE_TAB)).setRegistryName(SomeAssemblyRequired.MODID, "sandwich_assembly_table"),

                new SandwichItem(Blocks.SANDWICH, new Item.Properties().maxStackSize(8).food(new Food.Builder().setAlwaysEdible().build()).setISTER(() -> SandwichItemRenderer::new)).setRegistryName(SomeAssemblyRequired.MODID, "sandwich"),

                new SpreadItem(new Item.Properties()).setRegistryName(SomeAssemblyRequired.MODID, "spread"),

                new Item(new Item.Properties().group(SomeAssemblyRequired.CREATIVE_TAB).food(new Food.Builder().hunger(2).saturation(0.5F).build())).setRegistryName(SomeAssemblyRequired.MODID, "bread_slice"),
                new Item(new Item.Properties().group(SomeAssemblyRequired.CREATIVE_TAB).food(new Food.Builder().hunger(3).saturation(0.6F).build())).setRegistryName(SomeAssemblyRequired.MODID, "toasted_bread_slice")
        );
    }
}
