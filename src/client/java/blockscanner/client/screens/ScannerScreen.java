package blockscanner.client.screens;

import blockscanner.BlockScanner;
import blockscanner.menus.ScannerMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class ScannerScreen extends AbstractContainerScreen<ScannerMenu>  {
    private static final Identifier BACKGROUND_TEXTURE = BlockScanner.id("textures/gui/container/scanner.png");
    private static final Identifier SCAN_ACTIVE_TEXTURE = BlockScanner.id("textures/gui/container/scan_active.png");

    public ScannerScreen(ScannerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title,176, 166);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (menu.isScannerActive()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, SCAN_ACTIVE_TEXTURE, leftPos + 71, topPos + 11, 0, 0, 32, 32, 32, 32);
        }
    }
}
