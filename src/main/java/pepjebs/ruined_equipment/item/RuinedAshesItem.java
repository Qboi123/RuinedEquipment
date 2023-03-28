package pepjebs.ruined_equipment.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import pepjebs.ruined_equipment.utils.RuinedEquipmentUtils;

import java.util.List;

public class RuinedAshesItem extends Item {
    public RuinedAshesItem(Settings settings) {
        super(settings);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        Identifier id = RuinedEquipmentUtils.getItemKeyIdFromItemStack(stack);
        if (id == null) return;
        Item item = ForgeRegistries.ITEMS.getValue(id);
        if (stack.hasCustomName()) {
            tooltip.add(new TranslatableText(this.getTranslationKey()).formatted(Formatting.GRAY));
        }
        assert item != null;
        tooltip.add(new TranslatableText(item.getTranslationKey(stack)).formatted(Formatting.GRAY));
        RuinedEquipmentItem.appendRuinedTooltip(stack, tooltip);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return RuinedEquipmentItem.hasRuinedGlint(stack);
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return RuinedEquipmentItem.getRuinedRarity(stack);
    }
}
