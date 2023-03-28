package pepjebs.ruined_equipment.mixin;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pepjebs.ruined_equipment.RuinedEquipmentMod;
import pepjebs.ruined_equipment.utils.RuinedEquipmentUtils;

@Mixin(ExperienceOrbEntity.class)
public abstract class ExperienceOrbEntityMixin {

    @Shadow
    private int amount;

    @Inject(method = "onPlayerCollision", at = @At("INVOKE"))
    private void doRuinedRepairOnPlayerCollision(PlayerEntity player, CallbackInfo ci) {
        if (!player.world.isClient) {
            if (RuinedEquipmentMod.CONFIG != null && !RuinedEquipmentMod.CONFIG.enableRuinedMendingRepair)
                return;
            doPlayerHandRuinedMendingRepair(player, Hand.MAIN_HAND);
            doPlayerHandRuinedMendingRepair(player, Hand.OFF_HAND);
        }
    }

    private void doPlayerHandRuinedMendingRepair(PlayerEntity player, Hand hand) {
        ItemStack handStack = player.getStackInHand(hand);
        if (RuinedEquipmentUtils.isRuinedItem(handStack.getItem())
                && RuinedEquipmentUtils.ruinedItemHasEnchantment(handStack, Enchantments.MENDING)) {
            player.experiencePickUpDelay = 2;
            player.sendPickup((ExperienceOrbEntity)(Object)this, 1);
            if (this.amount > 0) {
                Item vanillaItem = RuinedEquipmentUtils.getRepairItemForItemStack(handStack);
                int repairAmount = getMendingRepairAmount(this.amount);
                ItemStack repaired = RuinedEquipmentUtils.generateRepairedItemForAnvilByDamage(
                        handStack, vanillaItem.getMaxDamage(handStack) - repairAmount);
                if (RuinedEquipmentMod.CONFIG != null && RuinedEquipmentMod.CONFIG.enableSetRuinedItemInHand) {
                    if (hand == Hand.MAIN_HAND) {
                        player.getInventory().main.set(player.getInventory().selectedSlot, repaired);
                    } else {
                        player.getInventory().offHand.set(0, repaired);
                    }
                } else {
                    player.getInventory().offerOrDrop(repaired);
                }
                this.amount -= getMendingRepairCost(repairAmount);
            }
        }
    }

    @Shadow abstract int getMendingRepairCost(int repairAmount);

    @Shadow abstract int getMendingRepairAmount(int experienceAmount);
}
