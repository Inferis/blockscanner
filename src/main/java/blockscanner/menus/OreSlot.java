package blockscanner.menus;

import blockscanner.items.ScannerItem;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class OreSlot extends Slot {
    public OreSlot(Container container, int slot, int x, int y) {
        super(container, slot, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem blockItem) {
            return blockItem.getBlock().defaultBlockState().is(ScannerItem.ORES_TAG);
        }
        return false;
    }
}
