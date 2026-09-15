package blockscanner.menus;

import blockscanner.BlockScanTracker;
import blockscanner.BlockScanner;
import blockscanner.items.ScannerItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;

public class ScannerMenu extends AbstractContainerMenu implements ContainerListener {
    private final Container container;
    private final ScannerScreenData data;
    private ServerPlayer player;

    public ScannerMenu(int containerId, Inventory playerInventory, ScannerScreenData data) {
        this(containerId, playerInventory, new SimpleContainer(1), data);
    }

    public ScannerMenu(int containerId, Inventory playerInventory, Container scannerInventory, ScannerScreenData data) {
        super(BlockScannerMenuTypes.SCANNER, containerId);
        container = scannerInventory;
        this.data = data;

        addSlot(new OreSlot(container, 0, 80, 46));
        addStandardInventorySlots(playerInventory, 8, 84);
        addSlotListener(this);

        var scanningBlockId = data.stack().get(ScannerItem.BLOCK);
        if (scanningBlockId != null) {
            var scanningBlock = BuiltInRegistries.BLOCK.get(scanningBlockId);
            if (scanningBlock.isPresent()) {
                getSlot(0).set(new ItemStack(scanningBlock.get().value().asItem()));
            }
        }
    }

    public boolean isScannerActive() {
        return data.stack().get(ScannerItem.ACTIVE) != null;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        var stack = getSlot(slotIndex).getItem();
        if (!stack.isEmpty()) {
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock().defaultBlockState().is(ScannerItem.ORES_TAG)) {
                if (slotIndex != 0 && getSlot(0).getItem().isEmpty()) {
                    if (stack.count() > 1) {
                        stack.shrink(1);
                        stack = new ItemStack(stack.getItem(), 1);
                    }
                    if (moveItemStackTo(stack, 0, 1, false)) {
                        return stack;
                    }
                }
                else if (slotIndex == 0) {
                    if (moveItemStackTo(stack, 28, 37, false)) {
                        return stack;
                    } else if (moveItemStackTo(stack, 1, 28, false)) {
                        return stack;
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            this.player = serverPlayer;
        }
        return true;
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
        if (slotIndex == 0) {
            if (itemStack.isEmpty()) {
                data.stack().remove(ScannerItem.BLOCK);
            }
            else {
                if (itemStack.getItem() instanceof BlockItem blockItem) {
                    var blockId = BuiltInRegistries.BLOCK.getKey(blockItem.getBlock());
                    data.stack().set(ScannerItem.BLOCK, blockId);
                }
            }
            BlockScanTracker.TRACKER.resetPlayerHighlight(player);
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int id, int value) {

    }
}
