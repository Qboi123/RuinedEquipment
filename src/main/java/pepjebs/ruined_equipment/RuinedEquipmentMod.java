package pepjebs.ruined_equipment;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.SpecialRecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.collection.DefaultedList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pepjebs.ruined_equipment.client.RuinedEquipmentClient;
import pepjebs.ruined_equipment.config.RuinedEquipmentConfig;
import pepjebs.ruined_equipment.item.RuinedDyeableEquipmentItem;
import pepjebs.ruined_equipment.item.RuinedEquipmentItem;
import pepjebs.ruined_equipment.item.RuinedEquipmentItems;
import pepjebs.ruined_equipment.recipe.RuinedEquipmentCraftRepair;
import pepjebs.ruined_equipment.recipe.RuinedEquipmentSmithingEmpowerRecipe;
import pepjebs.ruined_equipment.utils.RuinedEquipmentUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mod(RuinedEquipmentMod.MOD_ID)
public class RuinedEquipmentMod {

    public static final String MOD_ID = "ruined_equipment";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public static final String RUINED_PREFIX = "ruined_";

    public static RuinedEquipmentConfig CONFIG = null;

    public static SpecialRecipeSerializer<RuinedEquipmentCraftRepair> RUINED_CRAFT_REPAIR_RECIPE;
    public static RuinedEquipmentSmithingEmpowerRecipe.Serializer RUINED_SMITH_SET_EMPOWER;

    public static final HashMap<String, Pair<Integer, ItemStack>> ruinedEquipmentSetter = new HashMap<>();
    private ItemGroup itemGroup;

    public RuinedEquipmentMod() {
        init();
    }

    public void init() {
        FMLJavaModLoadingContext context = FMLJavaModLoadingContext.get();
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            RuinedEquipmentClient client = new RuinedEquipmentClient();
            client.init(context);
        });

        AutoConfig.register(RuinedEquipmentConfig.class, JanksonConfigSerializer::new);

        RuinedEquipmentConfig config = AutoConfig.getConfigHolder(RuinedEquipmentConfig.class).getConfig();
        CONFIG = config;
        if (config.enableCraftingGridRuinedRepair) {
            modEventBus.addGenericListener(RecipeSerializer.class, EventPriority.LOW, this::registerRepairRecipe);
        }
        if (config.enableSmithingRuinedEmpowered) {
            modEventBus.addGenericListener(RecipeSerializer.class, EventPriority.LOW, this::registerSetEmpowerRecipe);
        }

        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class, () -> new ConfigGuiHandler.ConfigGuiFactory((minecraftClient, parent) -> {
            Supplier<Screen> configScreen = AutoConfig.getConfigScreen(RuinedEquipmentConfig.class, parent);
            return configScreen.get();
        }));

        itemGroup = ItemGroup.MISC;
        if (config.enableCreativeInventoryTab) {
            itemGroup = new ItemGroup(MOD_ID + ".ruined_items") {
                @Override
                public ItemStack createIcon() {
                    return new ItemStack(ForgeRegistries.ITEMS.getValue(new Identifier(MOD_ID, "ruined_diamond_pickaxe")));
                }

                @Override
                public void appendStacks(DefaultedList<ItemStack> stacks) {
                    for (Item item : RuinedEquipmentItems.getVanillaItemMap().keySet().stream()
                            .sorted(RuinedEquipmentUtils::compareItemsById).toList()) {
                        stacks.add(new ItemStack(item));
                    }
                }
            };
        }

        modEventBus.addGenericListener(Item.class, EventPriority.LOW, true, this::onRegisterItems);
        forgeEventBus.addListener(this::onPlayerTick);
    }

    private void onPlayerTick(TickEvent.PlayerTickEvent event) {
        PlayerEntity player = event.player;
        String key = player.getName().getString();
        if (ruinedEquipmentSetter.containsKey(key)) {
            RuinedEquipmentMod.LOGGER.info("ServerTickEvents.START_SERVER_TICK: " + key);
            Pair<Integer, ItemStack> entry = ruinedEquipmentSetter.get(key);
            int slot = entry.getLeft();
            ItemStack ruinedItem = entry.getRight();
            boolean didRemove = false;
            if (slot == 0) {
                if (player.getInventory().offHand.get(slot).isEmpty()){
                    player.getInventory().offHand.set(slot, ruinedItem);
                    didRemove = true;
                }
            } else {
                slot--;
                if (player.getInventory().main.get(slot).isEmpty()) {
                    player.getInventory().main.set(slot, ruinedItem);
                    didRemove = true;
                }
            }
            if (didRemove) ruinedEquipmentSetter.remove(key);
        }
    }

    private void registerRepairRecipe(RegistryEvent.Register<RecipeSerializer<?>> event) {
        var serializer = new SpecialRecipeSerializer<>(RuinedEquipmentCraftRepair::new);
        serializer.setRegistryName(new Identifier(MOD_ID, "ruined_repair"));
        RUINED_CRAFT_REPAIR_RECIPE = serializer;
        event.getRegistry().register(serializer);
    }

    private void registerSetEmpowerRecipe(RegistryEvent.Register<RecipeSerializer<?>> event) {
        var serializer = new RuinedEquipmentSmithingEmpowerRecipe.Serializer();
        serializer.setRegistryName(new Identifier(MOD_ID, "ruined_set_empower"));
        RUINED_SMITH_SET_EMPOWER = serializer;
        event.getRegistry().register(serializer);
    }

    private void onRegisterItems(RegistryEvent.Register<Item> event) {
        registerItems(itemGroup, event.getRegistry());
    }

    private static void registerItems(ItemGroup itemGroup, IForgeRegistry<Item> registry) {
        Map<Item, Item> vanillaItemMap = new HashMap<>();

        Item.Settings set = new Item.Settings().maxCount(1).group(itemGroup);
        for (Item i : RuinedEquipmentItems.SUPPORTED_VANILLA_ITEMS) {
            Identifier key = ForgeRegistries.ITEMS.getKey(i);
            if (key == null) continue;

            if (key.getPath().contains("leather")) {
                vanillaItemMap.put(new RuinedDyeableEquipmentItem(set), i);
            } else {
                vanillaItemMap.put(new RuinedEquipmentItem(set), i);
            }
        }
        for (Map.Entry<Item, Item> item : vanillaItemMap.entrySet()) {
            Identifier key = ForgeRegistries.ITEMS.getKey(item.getValue());

            if (key == null) continue;

            String vanillaItemIdPath = key.getPath();
            item.getKey().setRegistryName(new Identifier(MOD_ID, RUINED_PREFIX + vanillaItemIdPath));
            registry.register(item.getKey());
        }
    }
}
