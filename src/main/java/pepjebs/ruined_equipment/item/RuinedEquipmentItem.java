package pepjebs.ruined_equipment.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import pepjebs.ruined_equipment.RuinedEquipmentMod;
import pepjebs.ruined_equipment.recipe.RuinedEquipmentSmithingEmpowerRecipe;
import pepjebs.ruined_equipment.utils.RuinedEquipmentUtils;

import java.util.List;
import java.util.Map;

public class RuinedEquipmentItem extends Item {

    public RuinedEquipmentItem(Settings settings) {
        super(settings);
    }

    @Override
    public String getTranslationKey() {
        return "item.ruined_equipment.ruined_prefix";
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        appendRuinedTooltip(stack, tooltip);
    }

    public static void appendRuinedTooltip(
            ItemStack stack,
            List<Text> tooltip) {
        if (stack.getNbt() == null) return;
        String tagString = stack.getNbt().getString(RuinedEquipmentUtils.RUINED_ENCHTS_TAG);
        Map<Enchantment, Integer> enchantMap = RuinedEquipmentUtils.processEncodedEnchantments(tagString);
        if (enchantMap != null) {
            if (stack.hasGlint()) {
                tooltip.set(0, tooltip.get(0).copy().formatted(Formatting.AQUA));
            }

            for (Map.Entry<Enchantment, Integer> enchant : enchantMap.entrySet()) {
                tooltip.add(MutableText.of(new LiteralTextContent(
                        enchant.getKey().getName(enchant.getValue()).getString())).formatted(Formatting.GRAY));
            }
        }
        if (stack.getNbt() != null && stack.getNbt().contains(RuinedEquipmentSmithingEmpowerRecipe.RUINED_MAX_ENCHT_TAG)
                && stack.getNbt().getBoolean(RuinedEquipmentSmithingEmpowerRecipe.RUINED_MAX_ENCHT_TAG)) {
            tooltip.add(MutableText.of(new TranslatableTextContent("item.ruined_equipment.ruined_upgrading"))
                    .formatted(Formatting.GRAY));
        }
        if (stack.getItem() == ForgeRegistries.ITEMS.getValue(new Identifier(RuinedEquipmentMod.MOD_ID, "ruined_shield"))
                && stack.getNbt().contains("BlockEntityTag")) {
            tooltip.add(MutableText.of(new TranslatableTextContent("item.ruined_equipment.ruined_shield.banner"))
                    .formatted(Formatting.GRAY)
                    .formatted(Formatting.ITALIC));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Text getName() {
        // Get existing text
        MutableText supered = super.getName().copyContentOnly();
        // Append vanilla item's name
        Item vanillaItem = RuinedEquipmentItems.getVanillaItemMap().get(this);
        supered = supered.append(MutableText.of(new TranslatableTextContent(vanillaItem.getTranslationKey())));
        return supered;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Text getName(ItemStack stack) {
        // Get existing text
        MutableText supered = super.getName().copyContentOnly();
        // Append vanilla item's name
        Item vanillaItem = RuinedEquipmentItems.getVanillaItemMap().get(this);
        supered = supered.append(MutableText.of(new TranslatableTextContent(vanillaItem.getTranslationKey())));
        // Add the Aqua text if it has a glint
        if (hasGlint(stack)) {
            supered = supered.formatted(Formatting.AQUA);
        }
        return supered;
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return hasRuinedGlint(stack);
    }

    public static boolean hasRuinedGlint(ItemStack stack) {
        return stack != null && stack.getNbt() != null
                && stack.getNbt().getString(RuinedEquipmentUtils.RUINED_ENCHTS_TAG) != null
                && !stack.getNbt().getString(RuinedEquipmentUtils.RUINED_ENCHTS_TAG).isEmpty();
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return getRuinedRarity(stack);
    }


    public static Rarity getRuinedRarity(ItemStack stack) {
        return hasRuinedGlint(stack) ? Rarity.RARE : Rarity.COMMON;
    }
}
