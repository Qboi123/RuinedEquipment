package pepjebs.ruined_equipment;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pepjebs.ruined_equipment.item.RuinedEquipmentItems;
import pepjebs.ruined_equipment.recipe.RuinedEquipmentCraftRepair;

import java.util.Map;

public class RuinedEquipmentMod implements ModInitializer {

    public static final String MOD_ID = "ruined_equipment";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String RUINED_PREFIX = "ruined_";

    public static SpecialRecipeSerializer<RuinedEquipmentCraftRepair> RUINED_CRAFT_REPAIR_RECIPE;

    public static final ItemGroup RUINED_GROUP = FabricItemGroupBuilder.create(new Identifier(MOD_ID, "ruined_items"))
            .icon(() -> new ItemStack(RuinedEquipmentItems.RUINED_DIAMOND_PICK))
            .appendItems(stacks -> {
                for (Map.Entry<Item, Item> item : RuinedEquipmentItems.VANILLA_ITEM_MAP.entrySet()) {
                    stacks.add(new ItemStack(item.getKey()));
                }
            })
            .build();

    @Override
    public void onInitialize() {
        RUINED_CRAFT_REPAIR_RECIPE = Registry.register(
                Registry.RECIPE_SERIALIZER,
                new Identifier(MOD_ID, "ruined_repair"),
                new SpecialRecipeSerializer<>(RuinedEquipmentCraftRepair::new));

        for (Map.Entry<Item, Item> item : RuinedEquipmentItems.VANILLA_ITEM_MAP.entrySet()) {
            String vanillaItemIdPath = Registry.ITEM.getId(item.getValue()).getPath();
            Registry.register(Registry.ITEM, new Identifier(MOD_ID,
                    RUINED_PREFIX + vanillaItemIdPath), item.getKey());
        }
    }
}
