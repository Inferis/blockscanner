package blockscanner.mixin;

import blockscanner.BlockScanTracker;
import blockscanner.BlockScanner;import blockscanner.items.BlockScannerItems;import blockscanner.items.ScannerItem;import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class InventoryMixin {
	@Inject(at = @At("HEAD"), method = "setItem(ILnet/minecraft/world/item/ItemStack;)V")
	public void setItem(final int slot, final ItemStack itemStack, CallbackInfo info) {
		var inventory = (Inventory)(Object)this;
		if (inventory.player instanceof ServerPlayer serverPlayer) {
			if (inventory == inventory.player.getInventory()) {
				var wasStack = inventory.getItem(slot);
				if (wasStack.is(BlockScannerItems.SCANNER) || itemStack.is(BlockScannerItems.SCANNER)) {
					BlockScanTracker.TRACKER.resetPlayerHighlight(serverPlayer);
				}
			}
		}
	}
}