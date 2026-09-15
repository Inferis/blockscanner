package blockscanner.menus;

import blockscanner.BlockScanner;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class BlockScannerMenuTypes {
    public static MenuType<ScannerMenu> SCANNER;

    public static void registerMenuTypes() {
        SCANNER = Registry.register(
                BuiltInRegistries.MENU,
                BlockScanner.id("scanner"),
                new ExtendedMenuType<>(ScannerMenu::new, ScannerScreenData.CODEC)
        );
    }
}
