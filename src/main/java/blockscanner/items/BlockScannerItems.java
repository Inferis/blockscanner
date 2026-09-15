package blockscanner.items;

import blockscanner.BlockScanner;
import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.item.v1.ItemComponentTooltipProviderRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.Scanner;
import java.util.function.Function;

public class BlockScannerItems {
    public static Item SCANNER;

    public static DataComponentType<ScannerItem.ToolTipAppender> SCANNER_TOOLTIP_APPENDER;

    public static void registerItems() {
        registerToolTipAppenders();

        ScannerItem.registerDataComponents();
        SCANNER = registerItem("scanner", ScannerItem::new, new Item.Properties());

        registerCreativeTabs();
    }

    private static void registerToolTipAppenders() {
        SCANNER_TOOLTIP_APPENDER = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                BlockScanner.id("scanner_tooltip_appender"),
                DataComponentType.<ScannerItem.ToolTipAppender>builder().persistent(MapCodec.unit(new ScannerItem.ToolTipAppender()).codec()).build());
        ItemComponentTooltipProviderRegistry.addLast(SCANNER_TOOLTIP_APPENDER);
    }

    private static void registerCreativeTabs() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(content -> {
            content.accept(SCANNER);
        });
    }

    private static Item registerItem(final String identifier, final Function<Item.Properties, Item> factory, final Item.Properties properties) {
        var key = ResourceKey.create(Registries.ITEM, BlockScanner.id(identifier));
        var item = factory.apply(properties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }
}
