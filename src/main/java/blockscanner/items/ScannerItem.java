package blockscanner.items;

import blockscanner.BlockScanner;
import blockscanner.menus.ScannerMenu;
import blockscanner.menus.ScannerScreenData;
import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

import static blockscanner.items.BlockScannerItems.SCANNER_TOOLTIP_APPENDER;

public class ScannerItem extends Item {
    public static DataComponentType<Boolean> ACTIVE;
    public static DataComponentType<Identifier> BLOCK;
    public static final TagKey<Block> ORES_TAG = TagKey.create(Registries.BLOCK, BlockScanner.id( "ores"));

    public ScannerItem(Item.Properties properties) {
        super(properties
                .stacksTo(1)
                .component(SCANNER_TOOLTIP_APPENDER, new ToolTipAppender()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return super.useOn(context);
        }
        context.getPlayer().openMenu(new MenuProvider(this, context.getItemInHand()));
        return InteractionResult.SUCCESS_SERVER;
    }

    public void setActive(ItemStack stack, Boolean active) {
        if (active) {
            stack.set(ACTIVE, true);
        }
        else {
            stack.remove(ACTIVE);
        }
    }

    public Boolean getActive(ItemStack stack) {
        return stack.getOrDefault(ACTIVE, false);
    }

    public interface ScannerVisitor {
        void visit(ItemStack stack, ScannerItem item, Boolean active);
    }

    public static ItemStack getActiveScanner(Player player) {
        final ItemStack[] result = { ItemStack.EMPTY };
        visitScanners(player, (stack, item, active) -> {
            if (active && result[0].isEmpty()) {
                result[0] = stack;
            }
        });
        return result[0];
    }

    public static ItemStack getFirstScanner(Player player) {
        final ItemStack[] result = { ItemStack.EMPTY };
        visitScanners(player, (stack, item, active) -> {
            if (result[0].isEmpty()) {
                result[0] = stack;
            }
        });
        return result[0];
    }

    public static boolean playerHasActiveScanner(Player player) {
        return visitScanners(player, null);
    }

    public static boolean visitScanners(Player player, ScannerVisitor visitor) {
        var inventory = player.getInventory();
        var active = false;

        // Get the current mode. This is the mode of the first magnet in the inventory.
        for (var slot=0; slot<inventory.getContainerSize(); ++slot) {
            var stack = inventory.getItem(slot);
            if (stack.getItem() instanceof ScannerItem item) {
                active = item.getActive(stack);
                break;
            }
        }

        // If a visitor was specified, run it against all the scanners in the
        // inventory, passing in the mode of the first one as a "source of truth".
        if (visitor != null) {
            for (var slot=0; slot<inventory.getContainerSize(); ++slot) {
                var stack = inventory.getItem(slot);
                if (stack.getItem() instanceof ScannerItem item) {
                    visitor.visit(stack, item, active);
                }
            }
        }

        return active;
    }

    public record ToolTipAppender() implements TooltipProvider {
        @Override
        public void addToTooltip(Item.TooltipContext context, Consumer<Component> textConsumer, TooltipFlag type, DataComponentGetter components) {
            var scansFor = Component.translatable("tooltip.blockscanner.scanner.scans_for");
            scansFor.append(" ");

            var found = false;
            var scannedBlockId = components.get(ScannerItem.BLOCK);
            if (scannedBlockId != null) {
                var block = BuiltInRegistries.BLOCK.get(scannedBlockId);
                if (block.isPresent()) {
                    scansFor.append(block.get().value().getName().withStyle(ChatFormatting.AQUA));
                    found = true;
                }
            }
            if (!found) {
                scansFor.append(Component.translatable("tooltip.blockscanner.scanner.nothing").withStyle(ChatFormatting.GRAY));
            }
            textConsumer.accept(scansFor);

            var state = components.getOrDefault(ScannerItem.ACTIVE, false);
            textConsumer.accept(Component.translatable(state ? "tooltip.blockscanner.scanner.active" : "tooltip.blockscanner.scanner.inactive")
                    .withStyle(state ? ChatFormatting.GREEN : ChatFormatting.RED));
        }
    }

    private class MenuProvider implements ExtendedMenuProvider<ScannerScreenData> {
        private final ScannerItem item;
        private final ItemStack stack;

        public MenuProvider(ScannerItem item, ItemStack stack) {
            this.item = item;
            this.stack = stack;
        }

        @Override
        public ScannerScreenData getScreenOpeningData(ServerPlayer player) {
            return new ScannerScreenData(stack);
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable("menu.blockscanner.scanner.title");
        }

        @Override
        public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (!stack.isEmpty()) {
                    return new ScannerMenu(containerId, inventory, getScreenOpeningData(serverPlayer));
                }
            }
            return null;
        }
    }

    public static void registerDataComponents() {
        ACTIVE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                BlockScanner.id("active"),
                DataComponentType.<Boolean>builder().persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL).build()
        );
        BLOCK = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                BlockScanner.id("block"),
                DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).networkSynchronized(ByteBufCodecs.fromCodec(Identifier.CODEC)).build()
        );
    }
}
