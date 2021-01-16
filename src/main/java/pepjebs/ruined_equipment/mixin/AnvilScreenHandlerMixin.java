package pepjebs.ruined_equipment.mixin;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.*;
import net.minecraft.tag.ItemTags;
import net.minecraft.text.LiteralText;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.ruined_equipment.RuinedEquipmentMod;
import pepjebs.ruined_equipment.item.RuinedEquipmentItem;
import pepjebs.ruined_equipment.item.RuinedEquipmentItems;
import pepjebs.ruined_equipment.recipe.RuinedEquipmentSmithingEmpowerRecipe;
import pepjebs.ruined_equipment.utils.RuinedEquipmentUtils;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin extends ForgingScreenHandler {

    private static final double REPAIR_MODIFIER = 0.25;

    @Shadow
    @Final
    private Property levelCost;

    @Shadow
    private int repairItemUsage;

    @Shadow
    private String newItemName;


    public AnvilScreenHandlerMixin(
            @Nullable ScreenHandlerType<?> type,
            int syncId,
            PlayerInventory playerInventory,
            ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    @Inject(method = "updateResult", at = @At(value = "RETURN"))
    private void updateRuinedRepair(CallbackInfo ci) {
        ItemStack leftStack = this.input.getStack(0).copy();
        ItemStack rightStack = this.input.getStack(1).copy();
        if (leftStack.getItem() instanceof RuinedEquipmentItem) {
            if (RuinedEquipmentMod.CONFIG != null &&
                    !RuinedEquipmentMod.CONFIG.enableAnvilRuinedRepair) return;
            RuinedEquipmentItem ruinedItem = (RuinedEquipmentItem) leftStack.getItem();
            boolean isMaxEnch = leftStack.getTag() != null &&
                    leftStack.getTag().contains(RuinedEquipmentSmithingEmpowerRecipe.RUINED_MAX_ENCHT_TAG)
                    && leftStack.getTag().getBoolean(RuinedEquipmentSmithingEmpowerRecipe.RUINED_MAX_ENCHT_TAG);
            Item vanillaItem = RuinedEquipmentItems.getVanillaItemMap().get(ruinedItem);
            int vanillaMaxDamage = vanillaItem.getMaxDamage() - 1;
            // Check right stack for matching repair item
            Ingredient repairIngredient = null;
            if(vanillaItem instanceof ArmorItem) {
                repairIngredient = ((ArmorItem) vanillaItem).getMaterial().getRepairIngredient();
            } else if (vanillaItem instanceof ToolItem) {
                repairIngredient = ((ToolItem) vanillaItem).getMaterial().getRepairIngredient();
            } else if (vanillaItem == Items.SHIELD) {
                repairIngredient = Ingredient.fromTag(ItemTags.PLANKS);
            }

            ItemStack repaired = ItemStack.EMPTY;
            int maxLevel = 4;
            if (repairIngredient != null && repairIngredient.test(rightStack)) {
                double targetFraction = 1.0 - (rightStack.getCount() * REPAIR_MODIFIER);
                repaired = RuinedEquipmentUtils.generateRepairedItemForAnvilByFraction(
                        leftStack,
                        Math.min(targetFraction, 1.0),
                        isMaxEnch);
                this.repairItemUsage = 4;
            } else if (rightStack.getItem() == vanillaItem) {
                // Check right stack for corresponding vanilla item
                int targetDamage = rightStack.getDamage() - (int)(REPAIR_MODIFIER * rightStack.getMaxDamage());
                repaired = RuinedEquipmentUtils.generateRepairedItemForAnvilByDamage(
                        leftStack,
                        Math.min(targetDamage, vanillaMaxDamage),
                        isMaxEnch);
                maxLevel = 2;
                this.repairItemUsage = 0;
            }
            // Set the output
            if (!repaired.isEmpty()) {
                int levelCost = RuinedEquipmentUtils.generateRepairLevelCost(repaired, maxLevel);
                if (this.newItemName.compareTo(leftStack.getName().getString()) != 0) {
                    if (StringUtils.isBlank(this.newItemName)) {
                        repaired.removeCustomName();
                    } else {
                        repaired.setCustomName(new LiteralText(this.newItemName));
                        levelCost++;
                    }
                }
                this.levelCost.set(levelCost);
            }
            this.output.setStack(0, repaired);
            this.sendContentUpdates();
        }
    }
}
