package pepjebs.ruined_equipment.client;

import net.minecraft.item.DyeableItem;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import pepjebs.ruined_equipment.item.RuinedEquipmentItems;

import java.util.Map;

public class RuinedEquipmentClient {
    public void init(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.register(this);
    }

    @SubscribeEvent
    public void registerColors(ColorHandlerEvent.Item event) {
        for (Map.Entry<Item, Item> i : RuinedEquipmentItems.getVanillaDyeableItemMap().entrySet()) {
            event.getItemColors().register((stack, tintIndex) -> {
                if (tintIndex == 0) {
                    return ((DyeableItem)stack.getItem()).getColor(stack);
                } else {
                    return -1;
                }
            }, i.getKey());
        }
    }
}
